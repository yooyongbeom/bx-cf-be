package com.bwg.channel.backend.gateway.cmm.configuration;

import org.reactivestreams.Publisher;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Component
public class SwaggerThemeFilter implements WebFilter, Ordered {

    private static final String INJECT =
            "<link rel=\"stylesheet\" href=\"/swagger-theme.css\">\n" +
            "<script src=\"/swagger-theme.js\"></script>\n";

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE - 10;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        // swagger-ui로 끝나는 경로 또는 index.html 포함 경로 모두 처리
        boolean isSwaggerHtml = path.contains("swagger-ui") && path.endsWith("index.html")
                || path.endsWith("/swagger-ui.html");
        if (!isSwaggerHtml) {
            return chain.filter(exchange);
        }

        ServerHttpResponse original = exchange.getResponse();
        DataBufferFactory factory = original.bufferFactory();

        ServerHttpResponseDecorator decorated = new ServerHttpResponseDecorator(original) {

            @Override
            public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                return super.writeWith(inject(body, factory));
            }

            @Override
            public Mono<Void> writeAndFlushWith(Publisher<? extends Publisher<? extends DataBuffer>> body) {
                return writeWith(Flux.from(body).flatMapSequential(p -> p));
            }
        };

        // Content-Length 제거 (주입으로 길이가 달라짐)
        decorated.getHeaders().remove(HttpHeaders.CONTENT_LENGTH);

        return chain.filter(exchange.mutate().response(decorated).build());
    }

    private Flux<DataBuffer> inject(Publisher<? extends DataBuffer> body, DataBufferFactory factory) {
        return Flux.from(body)
                .buffer()
                .map(buffers -> {
                    DataBuffer joined = factory.join(buffers);
                    byte[] bytes = new byte[joined.readableByteCount()];
                    joined.read(bytes);
                    DataBufferUtils.release(joined);

                    String html = new String(bytes, StandardCharsets.UTF_8);
                    if (html.contains("</body>")) {
                        html = html.replace("</body>", INJECT + "</body>");
                    } else {
                        html = html + INJECT;
                    }
                    return factory.wrap(html.getBytes(StandardCharsets.UTF_8));
                });
    }
}
