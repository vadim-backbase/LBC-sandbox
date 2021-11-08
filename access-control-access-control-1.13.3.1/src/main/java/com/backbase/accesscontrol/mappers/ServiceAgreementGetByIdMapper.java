package com.backbase.accesscontrol.mappers;

import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.serviceagreements.ServiceAgreementItem;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementItemGetResponseBody;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public abstract class ServiceAgreementGetByIdMapper extends
    ServiceAgreementMapper<ServiceAgreementItemGetResponseBody, ServiceAgreementItem> {

}
