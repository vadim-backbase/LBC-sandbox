package com.backbase.accesscontrol.util.helpers;

import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.util.ApplicationProperties;

public class ApplicationPropertiesUtils {

    public static void mockApprovalValidation(ApplicationProperties applicationProperties, boolean enabled) {

        when(applicationProperties.getApproval().getValidation().isEnabled()).thenReturn(enabled);
    }

    public static void mockApprovalLevel(ApplicationProperties applicationProperties, boolean enabled) {
        when(applicationProperties.getApproval().getLevel().isEnabled()).thenReturn(enabled);
    }
}
