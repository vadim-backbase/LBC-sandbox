package com.backbase.accesscontrol.service.config;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static java.util.Collections.singletonList;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.common.ConsoleNotifier;
import java.util.List;
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

    private static class TestServiceInstanceListSupplier implements ServiceInstanceListSupplier {

        private final String serviceId;
        private final int port;

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
            return Flux.just(singletonList(
                new DefaultServiceInstance(serviceId, serviceId, "localhost", port, false)));
        }
    }

}
