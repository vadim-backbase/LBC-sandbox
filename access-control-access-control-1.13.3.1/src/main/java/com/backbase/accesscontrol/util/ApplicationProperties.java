package com.backbase.accesscontrol.util;

import com.backbase.accesscontrol.util.properties.ApprovalProperty;
import com.backbase.buildingblocks.context.ContextScoped;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@ConfigurationProperties("backbase")
@ContextScoped
@Validated
@Data
public class ApplicationProperties {

    private ApprovalProperty approval = new ApprovalProperty();
}

