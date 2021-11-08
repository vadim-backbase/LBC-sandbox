package com.backbase.accesscontrol.domain.dto;

import lombok.Data;

@Data
public class PrivilegeDto {

    private String privilege;

    public PrivilegeDto withPrivilege(String privilege) {
        this.privilege = privilege;
        return this;
    }
}
