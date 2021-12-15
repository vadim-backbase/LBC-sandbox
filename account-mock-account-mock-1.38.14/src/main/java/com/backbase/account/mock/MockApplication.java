package com.backbase.account.mock;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties
public class MockApplication{

    public static void main(String[] args) {
        SpringApplication.run(MockApplication.class);
    }
}
