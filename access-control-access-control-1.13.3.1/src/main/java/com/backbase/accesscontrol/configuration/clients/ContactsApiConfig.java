package com.backbase.accesscontrol.configuration.clients;

import static com.backbase.buildingblocks.communication.http.HttpCommunicationConfiguration.INTERCEPTORS_ENABLED_HEADER;
import static com.backbase.buildingblocks.communication.http.HttpCommunicationConfiguration.INTER_SERVICE_REST_TEMPLATE_BEAN_NAME;

import com.backbase.dbs.contact.api.client.ApiClient;
import com.backbase.dbs.contact.api.client.v2.ContactsApi;
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
import org.springframework.web.client.RestTemplate;

@AutoConfigureAfter(name = {
    "com.backbase.buildingblocks.communication.http.HttpCommunicationConfiguration",
    "com.backbase.buildingblocks.backend.security.auth.config.SecurityContextUtilConfiguration"
})
@Setter
@Configuration
@ComponentScan(basePackages = {"com.backbase.dbs.contact.api.client"},
    excludeFilters = {
        @Filter(type = FilterType.CUSTOM, classes = {TypeExcludeFilter.class}),
        @Filter(type = FilterType.CUSTOM, classes = {AutoConfigurationExcludeFilter.class}),
        @Filter(type = FilterType.REGEX, pattern = "com.backbase.dbs.contact.api.client.*.ApiClient"),
        @Filter(type = FilterType.REGEX, pattern = "com.backbase.dbs.contact.api.client.*.*Api")})
@ConfigurationProperties("backbase.communication.services.contact-manager")
public class ContactsApiConfig {

    private String serviceId = "contact-manager";

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
    public ContactsApi createContactsClientInAccessControl(
        @Qualifier(INTER_SERVICE_REST_TEMPLATE_BEAN_NAME) RestTemplate restTemplate) {
        ApiClient apiClient = new ApiClient(restTemplate);
        apiClient.setBasePath(scheme + "://" + serviceId);
        apiClient.addDefaultHeader(INTERCEPTORS_ENABLED_HEADER, Boolean.TRUE.toString());
        return new ContactsApi(apiClient);
    }
}
