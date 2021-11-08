package com.backbase.accesscontrol.util;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum PrivilegesEnum {
    VIEW("view"),
    EDIT("edit"),
    DELETE("delete"),
    APPROVE("approve"),
    CANCEL("cancel"),
    EXECUTE("execute"),
    CREATE("create");

    private final String name;

    public String getPrivilegeName() {
        return name;
    }
}
