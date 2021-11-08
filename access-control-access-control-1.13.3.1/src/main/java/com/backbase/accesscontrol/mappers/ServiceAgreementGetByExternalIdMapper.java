package com.backbase.accesscontrol.mappers;

import com.backbase.accesscontrol.domain.LegalEntity;
import com.backbase.accesscontrol.domain.ServiceAgreement;
import com.backbase.accesscontrol.domain.ServiceAgreementState;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementExternalIdGetResponseBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.Status;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public abstract class ServiceAgreementGetByExternalIdMapper extends
    ServiceAgreementMapper<ServiceAgreementExternalIdGetResponseBody, ServiceAgreement> {

    @Mapping(target = "validFromDate",
        expression = "java(dateTimeService.getStringDateFromDate(item.getStartDate()))")
    @Mapping(target = "validFromTime",
        expression = "java(dateTimeService.getStringTimeFromDate(item.getStartDate()))")
    @Mapping(target = "validUntilDate",
        expression = "java(dateTimeService.getStringDateFromDate(item.getEndDate()))")
    @Mapping(target = "validUntilTime",
        expression = "java(dateTimeService.getStringTimeFromDate(item.getEndDate()))")
    @Mapping(target = "status", expression = "java(convertStatus(item.getState()))")
    @Mapping(target = "isMaster", expression = "java(item.isMaster())")
    public abstract ServiceAgreementExternalIdGetResponseBody mapSingle(ServiceAgreement item);

    public String convertLegalEntityId(LegalEntity legalEntity) {
        return legalEntity.getId();
    }

    public Status convertStatus(ServiceAgreementState state) {
        return Status.fromValue(state.toString());
    }
}
