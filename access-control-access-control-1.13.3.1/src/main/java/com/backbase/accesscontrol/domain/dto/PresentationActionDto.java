
package com.backbase.accesscontrol.domain.dto;

import java.util.HashMap;
import java.util.Map;

public enum PresentationActionDto
{

    ADD("add"),
    REMOVE("remove");
    private final String value;
    private static final Map<String, PresentationActionDto> CONSTANTS = new HashMap<>();

    static {
        for (PresentationActionDto c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    PresentationActionDto(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }

    public static PresentationActionDto fromValue(String value) {
        PresentationActionDto constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }
}
