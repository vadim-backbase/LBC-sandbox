package com.backbase.accesscontrol.util.helpers;

import com.backbase.accesscontrol.domain.Privilege;
import com.backbase.accesscontrol.domain.dto.PrivilegeDto;

public class PrivilegeUtil {

    public static Privilege getPrivilege(String id, String name, String code) {
        Privilege privilege = new Privilege();
        privilege.setId(id);
        privilege.setName(name);
        privilege.setCode(code);
        return privilege;
    }

    public static PrivilegeDto getPrivilege(
        String privilegeName) {
        return new PrivilegeDto()
            .withPrivilege(privilegeName);
    }
}
