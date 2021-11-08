package com.backbase.accesscontrol.mappers;

import com.backbase.accesscontrol.service.DateTimeService;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.Status;
import java.util.List;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class ServiceAgreementMapper<T, U> {

    @Autowired
    protected DateTimeService dateTimeService;

    @Mapping(target = "validFromDate",
        expression = "java(dateTimeService.getStringDateFromDate(item.getValidFrom()))")
    @Mapping(target = "validFromTime",
        expression = "java(dateTimeService.getStringTimeFromDate(item.getValidFrom()))")
    @Mapping(target = "validUntilDate",
        expression = "java(dateTimeService.getStringDateFromDate(item.getValidUntil()))")
    @Mapping(target = "validUntilTime",
        expression = "java(dateTimeService.getStringTimeFromDate(item.getValidUntil()))")
    public abstract T mapSingle(U item);

    public abstract List<T> mapList(List<U> serviceAgreementItems);

    public Status convertStatus(
        com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.serviceagreements
            .Status status) {
        return Status.fromValue(status.toString());
    }


}
