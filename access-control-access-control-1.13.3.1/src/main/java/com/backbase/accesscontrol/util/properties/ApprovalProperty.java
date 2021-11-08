package com.backbase.accesscontrol.util.properties;

import lombok.Data;

@Data
public class ApprovalProperty {

    private ApprovalValidation validation = new ApprovalValidation();
    private ApprovalLevel level = new ApprovalLevel();
}


