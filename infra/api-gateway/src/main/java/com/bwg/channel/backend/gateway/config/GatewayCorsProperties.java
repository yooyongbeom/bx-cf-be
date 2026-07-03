package com.bwg.channel.backend.gateway.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.cors")
public class GatewayCorsProperties {
    private List<String> allowedOriginPatterns = new ArrayList<>();
    private List<String> allowedMethods = new ArrayList<>(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
    private List<String> allowedHeaders = new ArrayList<>(List.of("*"));
    private List<String> exposedHeaders = new ArrayList<>();
    private Boolean allowCredentials = true;
    private Long maxAge = 3600L;
}
