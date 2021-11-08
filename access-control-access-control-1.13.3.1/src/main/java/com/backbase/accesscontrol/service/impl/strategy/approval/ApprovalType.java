package com.backbase.accesscontrol.service.impl.strategy.approval;

import com.backbase.accesscontrol.domain.enums.ApprovalAction;
import com.backbase.accesscontrol.domain.enums.ApprovalCategory;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class ApprovalType {

    ApprovalAction approvalAction;
    ApprovalCategory approvalCategory;

}
