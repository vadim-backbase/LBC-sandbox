package com.backbase.accesscontrol.mappers;

import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.SubEntitiesGetResponseBody;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.SubEntitiesPostResponseBody;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SubEntitiesGetResponseBodyMapper {

    List<SubEntitiesGetResponseBody> toGetResponses(List<SubEntitiesPostResponseBody> parameters);

}
