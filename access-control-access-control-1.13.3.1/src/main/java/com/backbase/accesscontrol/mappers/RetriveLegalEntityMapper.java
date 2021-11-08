package com.backbase.accesscontrol.mappers;

import com.backbase.accesscontrol.domain.LegalEntity;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.LegalEntityByExternalIdGetResponseBody;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RetriveLegalEntityMapper {

    LegalEntityByExternalIdGetResponseBody toLegalEntityByExternalIdGetResponseBody(LegalEntity legalEntity);
}
