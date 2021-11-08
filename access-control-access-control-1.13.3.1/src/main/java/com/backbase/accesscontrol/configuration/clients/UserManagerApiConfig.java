package com.backbase.accesscontrol.configuration.clients;

import static com.backbase.buildingblocks.communication.http.HttpCommunicationConfiguration.INTERCEPTORS_ENABLED_HEADER;

import com.backbase.buildingblocks.communication.http.HttpCommunicationConfiguration;
import javax.validation.constraints.Pattern;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigurationExcludeFilter;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.context.TypeExcludeFilter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.client.RestTemplate;
@AutoConfigureAfter(name = {
    "com.backbase.buildingblocks.communication.http.HttpCommunicationConfiguration",
    "com.backbase.buildingblocks.backend.security.auth.config.SecurityContextUtilConfiguration"
})
@Setter
@Validated
@Configuration
@ComponentScan(basePackages = {"com.backbase.dbs.user.api.client.v2"},
    excludeFilters = {
        @Filter(type = FilterType.CUSTOM, classes = {TypeExcludeFilter.class}),
        @Filter(type = FilterType.CUSTOM, classes = {AutoConfigurationExcludeFilter.class}),
        @Filter(type = FilterType.REGEX, pattern = "com.backbase.dbs.user.api.client.*.ApiClient"),
        @Filter(type = FilterType.REGEX, pattern = "com.backbase.dbs.user.api.client.*.*Api")})
@ConfigurationProperties("backbase.communication.services.pandp.user.query")
public class UserManagerApiConfig {

    private String serviceId = "user-manager";

    @Value("${backbase.communication.http.default-scheme:http}")
    @Pattern(regexp = "https?")
    private String scheme;

    /**
     * Creates a REST client.
     *
     * @param restTemplate the RestTemplate for the client.
     * @return the client.
     */
    @Bean
    public com.backbase.dbs.user.api.client.v2.UserManagementApi createUserManagementApiClientInAccessControl(
        @Qualifier(HttpCommunicationConfiguration.INTER_SERVICE_REST_TEMPLATE_BEAN_NAME) RestTemplate restTemplate) {
        com.backbase.dbs.user.api.client.ApiClient apiClient = new com.backbase.dbs.user.api.client.ApiClient(
            restTemplate);
        apiClient.setBasePath(scheme + "://" + serviceId);
        apiClient.addDefaultHeader(INTERCEPTORS_ENABLED_HEADER, Boolean.TRUE.toString());
        return new com.backbase.dbs.user.api.client.v2.UserManagementApi(apiClient);
    }
}