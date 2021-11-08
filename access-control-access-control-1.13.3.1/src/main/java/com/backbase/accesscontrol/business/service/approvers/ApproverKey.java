package com.backbase.accesscontrol.business.service.approvers;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;

@AllArgsConstructor
@EqualsAndHashCode
public class ApproverKey {

    private String function;
    private String action;
}
