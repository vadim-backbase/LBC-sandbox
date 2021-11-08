package com.backbase.accesscontrol.mappers;

import com.backbase.accesscontrol.domain.LegalEntity;
import com.backbase.accesscontrol.dto.RecordsDto;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.SubEntitiesPostResponseBody;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.enumeration.LegalEntityType;
import java.util.stream.Collectors;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.springframework.data.domain.Page;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SubEntitiesPersistenceToRecordsDtoMapper {

    default RecordsDto<SubEntitiesPostResponseBody> toPresentation(Page<LegalEntity> parameters) {
        RecordsDto<SubEntitiesPostResponseBody> result = new RecordsDto<>();
        result.setTotalNumberOfRecords(parameters.getTotalElements());
        result.setRecords(parameters.getContent().stream()
            .map(this::toPresentation)
            .collect(Collectors.toList()));
        return result;
    }

    SubEntitiesPostResponseBody toPresentation(LegalEntity legalEntity);

    default LegalEntityType convertType(com.backbase.accesscontrol.domain.enums.LegalEntityType type) {
        return LegalEntityType.fromValue(type.toString());
    }
}
