package com.backbase.legalentity.integration.external.mock;

import com.backbase.buildingblocks.jwt.internal.config.EnableInternalJwtConsumer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableInternalJwtConsumer
@EnableDiscoveryClient
public class Application{

    public static void main(String[] args) {
        SpringApplication.run(Application.class);
    }
}