package com.backbase.accesscontrol.util.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties("backbase.accesscontrol.masterserviceagreement.fallback")
public class MasterServiceAgreementFallbackProperties {
    boolean enabled = false;
}
