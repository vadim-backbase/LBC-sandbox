package com.backbase.accesscontrol.mappers;

import com.backbase.accesscontrol.domain.dto.ResponseItemExtended;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.BatchResponseItemExtended;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BatchResponseItemExtendedMapper {

    List<BatchResponseItemExtended> mapList(List<ResponseItemExtended> responseItemExtendedList);
}
