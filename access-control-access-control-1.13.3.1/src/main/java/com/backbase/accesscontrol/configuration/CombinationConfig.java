package com.backbase.accesscontrol.configuration;

import java.util.HashSet;
import java.util.Set;
import javax.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "backbase.data-group.combinations")
public class CombinationConfig {

    private Set<String> notOptionalWhenRequested = new HashSet<>();

    @PostConstruct
    private void addArrangementsToSet() {
        notOptionalWhenRequested.add("ARRANGEMENTS");
    }
}
