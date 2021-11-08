package com.backbase.accesscontrol.mappers.model.legalenitty.service;

import com.backbase.accesscontrol.mappers.model.AbstractPayloadConverter;
import com.backbase.accesscontrol.mappers.model.ConverterKey;
import com.backbase.accesscontrol.service.rest.spec.model.LegalEntitiesBatchDelete;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.PresentationBatchDeleteLegalEntities;
import java.util.LinkedHashSet;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;


@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class LegalEntitiesBatchDeleteToPresentationBatchDeleteLegalEntitiesMapper
    implements AbstractPayloadConverter<LegalEntitiesBatchDelete, PresentationBatchDeleteLegalEntities> {

    abstract LinkedHashSet<String> convertListToLinkedHashSet(List<String> externalIds);

    @Override
    public ConverterKey getConverterKey() {
        return new ConverterKey(LegalEntitiesBatchDelete.class.getCanonicalName(),
            PresentationBatchDeleteLegalEntities.class.getCanonicalName());
    }
}