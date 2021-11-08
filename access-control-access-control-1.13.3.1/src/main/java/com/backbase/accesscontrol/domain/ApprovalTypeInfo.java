package com.backbase.accesscontrol.domain;

import com.backbase.accesscontrol.domain.enums.ApprovalAction;
import com.backbase.accesscontrol.domain.enums.ApprovalCategory;

public interface ApprovalTypeInfo {


    ApprovalAction getApprovalAction();

    ApprovalCategory getApprovalCategory();

}
