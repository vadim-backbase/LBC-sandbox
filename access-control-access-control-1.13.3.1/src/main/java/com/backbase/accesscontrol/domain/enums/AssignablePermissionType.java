package com.backbase.accesscontrol.domain.enums;

import java.util.Arrays;

public enum AssignablePermissionType {

    REGULAR_USER_DEFAULT(0),
    ADMIN_USER_DEFAULT(1),
    CUSTOM(2);

    private int value;

    AssignablePermissionType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static AssignablePermissionType from(int idOfEnum) {
        return Arrays.stream(AssignablePermissionType.values()).filter(p -> p.getValue() == idOfEnum).findAny()
            .orElse(AssignablePermissionType.CUSTOM);
    }
}
