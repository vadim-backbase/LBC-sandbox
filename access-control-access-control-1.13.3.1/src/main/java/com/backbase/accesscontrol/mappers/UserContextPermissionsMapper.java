package com.backbase.accesscontrol.mappers;

import com.backbase.accesscontrol.client.rest.spec.model.PermissionsDataGroup;
import com.backbase.accesscontrol.client.rest.spec.model.PermissionsRequest;
import com.backbase.accesscontrol.service.rest.spec.model.PermissionDataGroupData;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserContextPermissionsMapper {

    PermissionsRequest permissionsRequestMap(
        com.backbase.accesscontrol.service.rest.spec.model.PermissionsRequest permissionsRequest);

    com.backbase.accesscontrol.service.rest.spec.model.PermissionsDataGroup permissionsDataGroupMap(
        PermissionsDataGroup permissionsDataGroup);

    List<PermissionDataGroupData> permissionDataGroupDataMap(
        List<com.backbase.accesscontrol.client.rest.spec.model.PermissionDataGroupData> permissionDataGroupData);
}
