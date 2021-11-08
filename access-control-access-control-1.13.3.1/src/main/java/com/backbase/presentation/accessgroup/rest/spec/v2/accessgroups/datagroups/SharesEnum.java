package com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.datagroups;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum SharesEnum {
    ACCOUNTS("accounts"),

    USERS("users"),

    USERSANDACCOUNTS("usersAndAccounts");

    private String value;

    SharesEnum(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    @JsonCreator
    public static SharesEnum fromValue(String value) {
        for (SharesEnum val : SharesEnum.values()) {
            if (val.value.equals(value)) {
                return val;
            }
        }
        throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
}
