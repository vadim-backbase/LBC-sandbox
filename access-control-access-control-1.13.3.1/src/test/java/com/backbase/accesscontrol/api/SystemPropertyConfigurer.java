package com.backbase.accesscontrol.api;

import javax.annotation.PostConstruct;
import org.springframework.stereotype.Component;

@Component
public class SystemPropertyConfigurer {

    @PostConstruct
    public void setProperty() {
        System.setProperty("SIG_SECRET_KEY", "JWTSecretKeyDontUseInProduction!");
        System.setProperty("EXTERNAL_SIG_SECRET_KEY", "JWTSecretKeyDontUseInProduction!");
    }
}