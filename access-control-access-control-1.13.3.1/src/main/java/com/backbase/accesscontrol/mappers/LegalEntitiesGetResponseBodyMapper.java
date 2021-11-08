package com.backbase.accesscontrol.mappers;

import com.backbase.accesscontrol.domain.LegalEntity;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.LegalEntitiesGetResponseBody;
import java.util.List;
import java.util.Objects;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class LegalEntitiesGetResponseBodyMapper {

    public abstract List<LegalEntitiesGetResponseBody> toPresentation(
        List<LegalEntity> legalEntities);

    @Mapping(target = "parentId", expression = "java(checkIfParentNull(legalEntity))")
    @Mapping(target = "isParent", expression = "java(java.util.Objects.isNull(legalEntity.getParent()))")
    public abstract LegalEntitiesGetResponseBody sourceToDestination(LegalEntity legalEntity);

    protected String checkIfParentNull(LegalEntity legalEntity) {
        if (Objects.nonNull(legalEntity.getParent())) {
            return legalEntity.getParent().getId();
        }
        return null;
    }
}
