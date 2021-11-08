package com.backbase.accesscontrol.mappers;


import com.backbase.dbs.accesscontrol.api.client.v2.model.LegalEntityItem;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.LegalEntityExternalData;
import java.util.List;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ExternalSearchLegalEntityMapper {

    List<LegalEntityExternalData> toPresentation(List<LegalEntityItem> legalEntityItems);
}
