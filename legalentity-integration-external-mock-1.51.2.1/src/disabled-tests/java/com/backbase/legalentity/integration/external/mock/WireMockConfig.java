package com.backbase.legalentity.integration.external.mock;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.common.ConsoleNotifier;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import javax.annotation.PreDestroy;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import reactor.core.publisher.Flux;

@Configuration
public class WireMockConfig {

    private static WireMockServer server;

    @Bean
    @ConditionalOnProperty(name = "wiremock", havingValue = "true")
    public WireMockServer getWireMockServer() {
        server = new WireMockServer(
            wireMockConfig()
                .notifier(new ConsoleNotifier(true))
                .dynamicPort());

        server.start();
        return server;
    }

    @Bean
    @Primary
    ServiceInstanceListSupplier serviceInstanceListSupplier() {
        return new TestServiceInstanceListSupplier("/access-control", server != null ? server.port() : 8080);
    }

    @PreDestroy
    public void finalize() {
        if (Objects.nonNull(server)) {
            server.stop();
        }
    }

    private class TestServiceInstanceListSupplier implements ServiceInstanceListSupplier {

        private String serviceId;
        private int port;

        public TestServiceInstanceListSupplier(
            String serviceId, int port) {
            this.serviceId = serviceId;
            this.port = port;
        }

        @Override
        public String getServiceId() {
            return serviceId;
        }

        @Override
        public Flux<List<ServiceInstance>> get() {
            return Flux.just(Arrays
                .asList(new DefaultServiceInstance(serviceId, serviceId, "localhost", port, false)));
        }
    }
}
