package com.backbase.account.mock;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "account-mock")
@Setter
@Getter
public class MockConfiguration {

    private int tenantId;
    private boolean returnNullDetails;

}


