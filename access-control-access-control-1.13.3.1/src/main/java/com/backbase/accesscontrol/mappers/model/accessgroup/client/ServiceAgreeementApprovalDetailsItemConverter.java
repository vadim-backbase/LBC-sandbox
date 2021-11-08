package com.backbase.accesscontrol.mappers.model.accessgroup.client;

import com.backbase.accesscontrol.mappers.model.AbstractPayloadConverter;
import com.backbase.accesscontrol.mappers.model.ConverterKey;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.ServiceAgreementApprovalDetailsItem;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class ServiceAgreeementApprovalDetailsItemConverter
    implements
    AbstractPayloadConverter<ServiceAgreementApprovalDetailsItem,
        com.backbase.accesscontrol.client.rest.spec.model.ServiceAgreeementApprovalDetailsItem> {

    @Override
    public ConverterKey getConverterKey() {
        return new ConverterKey(
            ServiceAgreementApprovalDetailsItem.class
                .getCanonicalName(),
            com.backbase.accesscontrol.client.rest.spec.model.ServiceAgreeementApprovalDetailsItem.class
                .getCanonicalName());
    }
}
