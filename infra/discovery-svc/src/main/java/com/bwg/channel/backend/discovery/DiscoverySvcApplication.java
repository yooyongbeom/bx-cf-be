package com.bwg.channel.backend.discovery;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer
public class DiscoverySvcApplication {

    public static void main(String[] args) {
        SpringApplication.run(DiscoverySvcApplication.class, args);
    }

}
