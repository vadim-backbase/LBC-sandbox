package com.backbase.accesscontrol;

import com.backbase.audit.client.EnableAuditClient;
import com.backbase.buildingblocks.jwt.internal.config.EnableInternalJwtConsumer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableInternalJwtConsumer
@EnableDiscoveryClient
@EnableAuditClient
@Configuration
@EnableJpaRepositories(basePackages = {
    "com.backbase.accesscontrol.repository"
})
@EntityScan(basePackages = {
    "com.backbase.accesscontrol.domain"
})
public class Application extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(Application.class);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(Application.class);
    }
}
