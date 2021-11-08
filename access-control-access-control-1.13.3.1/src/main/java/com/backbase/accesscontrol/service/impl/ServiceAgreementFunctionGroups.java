package com.backbase.accesscontrol.service.impl;

import com.backbase.accesscontrol.domain.ServiceAgreement;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
class ServiceAgreementFunctionGroups {

    private String systemFunctionGroup;
    private ServiceAgreement serviceAgreement;
}
