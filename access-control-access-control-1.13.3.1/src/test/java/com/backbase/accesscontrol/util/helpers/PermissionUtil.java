package com.backbase.accesscontrol.util.helpers;

import com.backbase.accesscontrol.domain.dto.Permission;
import com.backbase.accesscontrol.domain.dto.PrivilegeDto;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PermissionUtil {

    public static List<Permission> getPermissions(Permission... permissionsArray) {
        List<Permission> permissions = new ArrayList<>();
        Collections.addAll(permissions, permissionsArray);
        return permissions;
    }

    public static Permission getPermission(String buisnessFunctionId, PrivilegeDto... privileges) {
        List<PrivilegeDto> assignedPrivileges = new ArrayList<>();
        Collections.addAll(assignedPrivileges, privileges);
        return new Permission()
            .withFunctionId(buisnessFunctionId)
            .withAssignedPrivileges(assignedPrivileges);
    }
}
