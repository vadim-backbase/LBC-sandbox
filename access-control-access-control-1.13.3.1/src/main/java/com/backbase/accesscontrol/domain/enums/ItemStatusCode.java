package com.backbase.accesscontrol.domain.enums;

public enum ItemStatusCode
{

    HTTP_STATUS_OK("200"),
    HTTP_STATUS_BAD_REQUEST("400"),
    HTTP_STATUS_NOT_FOUND("404"),
    HTTP_STATUS_INTERNAL_SERVER_ERROR("500");
    private final String value;

    ItemStatusCode(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }
}
