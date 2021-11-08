package com.backbase.accesscontrol.mappers;

import com.backbase.accesscontrol.domain.ServiceAgreement;
import com.backbase.accesscontrol.domain.ServiceAgreementState;
import com.backbase.accesscontrol.service.DateTimeService;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.MasterServiceAgreementGetResponseBody;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.Status;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring")
public abstract class MasterServiceAgreementMapper {

    @Autowired
    protected DateTimeService dateTimeService;

    @Mapping(target = "validFromDate",
        expression = "java(dateTimeService.getStringDateFromDate(item.getStartDate()))")
    @Mapping(target = "validFromTime",
        expression = "java(dateTimeService.getStringTimeFromDate(item.getStartDate()))")
    @Mapping(target = "validUntilDate",
        expression = "java(dateTimeService.getStringDateFromDate(item.getEndDate()))")
    @Mapping(target = "validUntilTime",
        expression = "java(dateTimeService.getStringTimeFromDate(item.getEndDate()))")
    @Mapping(target = "creatorLegalEntity", expression = "java(item.getCreatorLegalEntity().getId())")
    @Mapping(target = "status", source = "state")
    @Mapping(target = "isMaster", expression = "java(item.isMaster())")
    public abstract MasterServiceAgreementGetResponseBody convertToResponse(ServiceAgreement item);

    public Status getStatus(ServiceAgreementState state) {
        return Status.fromValue(state.toString());
    }
}
