package com.backbase.accesscontrol.mappers.model.legalenitty.service;

import com.backbase.accesscontrol.mappers.model.AbstractPayloadConverter;
import com.backbase.accesscontrol.mappers.model.ConverterKey;
import com.backbase.accesscontrol.service.rest.spec.model.GetServiceAgreement;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.MasterServiceAgreementGetResponseBody;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class MasterServiceAgreementGetResponseBodyToGetServiceAgreementMapper
    implements AbstractPayloadConverter<MasterServiceAgreementGetResponseBody, GetServiceAgreement> {

    @Override
    public ConverterKey getConverterKey() {
        return new ConverterKey(MasterServiceAgreementGetResponseBody.class.getCanonicalName(),
            GetServiceAgreement.class.getCanonicalName());
    }
}
