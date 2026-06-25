package com.bwg.channel.backend.gateway.cmm.configuration;

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
 * swagger-initializer.js 요청을 가로채어 다크/라이트 테마 토글 코드가 포함된 응답을 직접 반환한다.
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
        + "  \"configUrl\" : \"/v3/api-docs/swagger-config\",\n"
        + "  \"persistAuthorization\" : true,\n"
        + "  \"validatorUrl\" : \"\"\n"
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
