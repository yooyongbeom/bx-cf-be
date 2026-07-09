package com.bwg.channel.backend.common.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 외부 HTTP API 호출을 위한 공통 유틸리티.
 * <p>
 * 기존 코드 호환을 위한 static client와 Spring bean으로 주입받는 client를 함께 제공하며,
 * 동기 호출(RestTemplate)은 단순 재시도, 비동기 호출(WebClient)은 Reactor retry를 적용한다.
 */
@Slf4j
@Component
public class ApiCallUtil {
    // --------------------------
    // Static 접근용
    // --------------------------
    private static final RestTemplate staticRestTemplate;
    private static final WebClient staticWebClient;

    static {
        // Spring bean 주입을 받기 어려운 레거시/정적 호출부에서 사용할 기본 client.
        staticRestTemplate = new RestTemplate();
        staticWebClient = WebClient.builder()
                .build();
    }

    public static RestTemplate getStaticRestTemplate() {
        return staticRestTemplate;
    }

    public static WebClient getStaticWebClient() {
        return staticWebClient;
    }

    // --------------------------
    // Bean 사용용
    // --------------------------
    private final RestTemplate restTemplate;
    private final WebClient webClient;

    public ApiCallUtil(WebClient.Builder webClientBuilder) {
        this.restTemplate = new RestTemplate();
        this.webClient = webClientBuilder.build();

        // UTF-8 적용: 문자열 응답/요청이 플랫폼 기본 charset에 흔들리지 않도록 보정한다.
        // 1. StringHttpMessageConverter
        List<HttpMessageConverter<?>> converters = restTemplate.getMessageConverters();
        for (int i = 0; i < converters.size(); i++) {
            if (converters.get(i) instanceof StringHttpMessageConverter) {
                converters.set(i, new StringHttpMessageConverter(StandardCharsets.UTF_8));
            }
        }

        // 2. MappingJackson2HttpMessageConverter
        for (HttpMessageConverter<?> converter : converters) {
            if (converter instanceof MappingJackson2HttpMessageConverter) {
                MappingJackson2HttpMessageConverter jacksonConverter = (MappingJackson2HttpMessageConverter) converter;
                jacksonConverter.setDefaultCharset(StandardCharsets.UTF_8);
            }
        }
    }

    // ==========================
    // 동기 호출 (RestTemplate)
    // ==========================
    public <T> ResponseEntity<T> getSync(String url, Map<String, String> headers, Class<T> responseType) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(new MediaType("application", "json", StandardCharsets.UTF_8)); // UTF-8 명시
        httpHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        // 호출부에서 전달한 인증/추적 헤더를 공통 헤더 위에 덧씌운다.
        if (headers != null) headers.forEach(httpHeaders::set);
        HttpEntity<String> entity = new HttpEntity<>(httpHeaders);

        return executeWithRetry(() -> {
            log.info("REST GET 호출 URL={}", url);
            return restTemplate.exchange(url, HttpMethod.GET, entity, responseType);
        });
    }

    public <T, B> ResponseEntity<T> postSync(String url, B body, Map<String, String> headers, ParameterizedTypeReference<T> responseTypeRef) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(new MediaType("application", "json", StandardCharsets.UTF_8)); // UTF-8 명시
        httpHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        // POST body는 제네릭으로 받아 호출부의 DTO/Map 형태를 그대로 보존한다.
        if (headers != null) headers.forEach(httpHeaders::set);
        HttpEntity<B> entity = new HttpEntity<>(body, httpHeaders);

        return executeWithRetry(() -> {
            log.info("REST POST 호출 URL={}", url);
            return restTemplate.exchange(url, HttpMethod.POST, entity, responseTypeRef);
        });
    }

    private <T> ResponseEntity<T> executeWithRetry(Retryable<T> action) {
        int maxRetry = 3;
        Duration delay = Duration.ofSeconds(1);
        for (int i = 1; i <= maxRetry; i++) {
            try {
                return action.execute();
            } catch (ResourceAccessException ex) {
                // 네트워크 접근 실패만 재시도한다. HTTP 4xx/5xx 응답은 RestTemplate 예외 정책을 따른다.
                log.warn("요청 실패, 재시도 {}/{} - {}", i, maxRetry, ex.getMessage());
                if (i == maxRetry) throw ex;
                try { Thread.sleep(delay.toMillis()); } catch (InterruptedException ignored) {}
            }
        }
        return null;
    }

    @FunctionalInterface
    private interface Retryable<T> {
        ResponseEntity<T> execute();
    }

    // ==========================
    // 비동기 호출 (WebClient)
    // ==========================
    public <T> Mono<T> getAsync(String url, Map<String, String> headers, Class<T> responseType) {
        WebClient.RequestHeadersSpec<?> request = webClient.get().uri(url);
        // WebClient header mutation은 람다 안에서 수행해야 실제 요청 spec에 반영된다.
        if (headers != null) request.headers(httpHeaders -> headers.forEach(httpHeaders::set));

        return request
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(responseType)
                .doOnSubscribe(sub -> log.info("WebClient GET 호출 URL={}", url))
                .doOnError(err -> log.error("WebClient GET 오류 URL={} - {}", url, err.getMessage()))
                // 응답 오류와 네트워크 오류만 1초 간격으로 최대 3회 재시도한다.
                .retryWhen(Retry.fixedDelay(3, Duration.ofSeconds(1))
                        .filter(throwable -> throwable instanceof WebClientResponseException || throwable instanceof ResourceAccessException)
                        .onRetryExhaustedThrow((spec, signal) -> signal.failure()));
    }

    public <T, B> Mono<T> postAsync(String url, B body, Map<String, String> headers, Class<T> responseType) {
        WebClient.RequestBodySpec request = webClient.post().uri(url);
        // 호출부 헤더는 contentType/accept와 별개로 추가 주입한다.
        if (headers != null) request.headers(httpHeaders -> headers.forEach(httpHeaders::set));

        return request
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(responseType)
                .doOnSubscribe(sub -> log.info("WebClient POST 호출 URL={}", url))
                .doOnError(err -> log.error("WebClient POST 오류 URL={} - {}", url, err.getMessage()))
                // WebClient는 Mono 체인에서 retry 정책을 선언해 구독 시점에 적용한다.
                .retryWhen(Retry.fixedDelay(3, Duration.ofSeconds(1))
                        .filter(throwable -> throwable instanceof WebClientResponseException || throwable instanceof ResourceAccessException)
                        .onRetryExhaustedThrow((spec, signal) -> signal.failure()));
    }
}
