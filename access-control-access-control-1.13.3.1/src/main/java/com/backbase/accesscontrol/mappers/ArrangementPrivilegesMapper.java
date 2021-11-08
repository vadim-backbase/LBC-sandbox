package com.backbase.accesscontrol.mappers;

import com.backbase.accesscontrol.dto.ArrangementPrivilegesDto;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.users.ArrangementPrivilegesGetResponseBody;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ArrangementPrivilegesMapper {

    ArrangementPrivilegesGetResponseBody sourceToDestination(
        ArrangementPrivilegesDto arrangementPrivilegesDto);

    List<com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.users.ArrangementPrivilegesGetResponseBody> toListArrangementPrivilegesGetResponseBody(
        List<ArrangementPrivilegesDto> arrangementPrivileges);

    List<ArrangementPrivilegesGetResponseBody> toListArrangementPrivilegesGetResponseBodyPresentation(
        List<ArrangementPrivilegesDto> arrangementPrivileges);
}
