package com.bwg.channel.backend.gateway.filter;

import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

/**
 * swagger-initializer.js 요청을 가로채어 다크/라이트 테마 토글 코드가 포함된 응답을 직접 반환
 * chain.filter()를 호출하지 않으므로 springdoc 핸들러와 충돌하지 않는다.
 */
@Component
public class SwaggerThemeFilter implements WebFilter, Ordered {

    private static final String INITIALIZER_JS =
        "window.onload = function() {\n"
        + "  //<editor-fold desc=\"Changeable Configuration Block\">\n"
        + "  window.ui = SwaggerUIBundle({\n"
        + "    url: \"https://petstore.swagger.io/v2/swagger.json\",\n"
        + "    dom_id: '#swagger-ui',\n"
        + "    deepLinking: true,\n"
        + "    presets: [\n"
        + "      SwaggerUIBundle.presets.apis,\n"
        + "      SwaggerUIStandalonePreset\n"
        + "    ],\n"
        + "    plugins: [\n"
        + "      SwaggerUIBundle.plugins.DownloadUrl\n"
        + "    ],\n"
        + "    layout: \"StandaloneLayout\" ,\n"
        + "\n"
        + "  \"urls\" : [\n"
        + "    { \"name\" : \"auth-svc\", \"url\" : \"/auth-svc/v3/api-docs\" },\n"
        + "    { \"name\" : \"product-svc\", \"url\" : \"/product-svc/v3/api-docs\" },\n"
        + "    { \"name\" : \"system-svc\", \"url\" : \"/system-svc/v3/api-docs\" },\n"
        + "    { \"name\" : \"integration-svc\", \"url\" : \"/integration-svc/v3/api-docs\" },\n"
        + "    { \"name\" : \"mci-svc\", \"url\" : \"/mci-svc/v3/api-docs\" }\n"
        + "  ],\n"
        + "  \"urls.primaryName\" : \"auth-svc\",\n"
        + "  \"persistAuthorization\" : true,\n"
        + "  \"validatorUrl\" : \"\",\n"
        // Bruno/Postman의 post-request 스크립트 대응: 2xx 응답 본문에 accessToken이 있으면
        // 자동으로 뽑아 bearerAuth 스킴에 Authorize 처리한다. (login/erp-login/refresh-token 공통)
        + "  responseInterceptor : function(res){\n"
        + "    try{\n"
        + "      if(res.status>=200&&res.status<300&&res.text){\n"
        + "        var m=/\"accessToken\"\\s*:\\s*\"([^\"]+)\"/.exec(res.text);\n"
        + "        if(m&&window.ui){window.ui.preauthorizeApiKey('bearerAuth',m[1]);console.log('[swagger] accessToken \\uC790\\uB3D9 Authorize \\uC644\\uB8CC');}\n"
        + "      }\n"
        + "    }catch(e){console.warn('[swagger] token capture \\uC2E4\\uD328',e);}\n"
        + "    return res;\n"
        + "  }\n"
        + "\n"
        + "  });\n"
        + "  //</editor-fold>\n"
        + "};\n"
        + "\n"
        + "(function(){\n"
        + "  var l=document.createElement('link');\n"
        + "  l.rel='stylesheet';l.href='/swagger-theme.css';\n"
        + "  (document.head||document.documentElement).appendChild(l);\n"
        + "\n"
        + "  var K='bwg-swagger-theme';\n"
        + "  function dark(){return document.body&&document.body.classList.contains('bwg-dark');}\n"
        + "  function apply(d){\n"
        + "    if(document.body)document.body.classList.toggle('bwg-dark',d);\n"
        + "    var b=document.getElementById('bwg-theme-btn');\n"
        + "    if(b)b.textContent=d?'\\u2600 Light':'\\u263e Dark';\n"
        + "  }\n"
        + "  function tryAddBtn(){\n"
        + "    if(document.getElementById('bwg-theme-btn'))return;\n"
        + "    if(!document.querySelector('.swagger-ui')){setTimeout(tryAddBtn,300);return;}\n"
        + "    var b=document.createElement('button');\n"
        + "    b.id='bwg-theme-btn';\n"
        + "    b.textContent=dark()?'\\u2600 Light':'\\u263e Dark';\n"
        + "    b.onclick=function(){var n=!dark();localStorage.setItem(K,n?'dark':'light');apply(n);};\n"
        + "    document.body.appendChild(b);\n"
        + "  }\n"
        + "  window.addEventListener('load',function(){\n"
        + "    if(localStorage.getItem(K)==='dark')apply(true);\n"
        + "    setTimeout(tryAddBtn,300);\n"
        + "  });\n"
        + "})();\n";

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE - 10;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        if (!path.endsWith("swagger-initializer.js")) {
            return chain.filter(exchange);
        }

        ServerHttpResponse response = exchange.getResponse();
        byte[] bytes = INITIALIZER_JS.getBytes(StandardCharsets.UTF_8);
        response.getHeaders().setContentType(
            MediaType.parseMediaType("application/javascript;charset=UTF-8")
        );
        response.getHeaders().setContentLength(bytes.length);
        DataBuffer buffer = response.bufferFactory().wrap(bytes);
        return response.writeWith(Mono.just(buffer));
    }
}
