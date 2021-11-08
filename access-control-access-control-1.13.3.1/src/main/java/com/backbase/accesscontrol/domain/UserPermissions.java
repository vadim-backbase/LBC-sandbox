package com.backbase.accesscontrol.domain;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;

@AllArgsConstructor
@EqualsAndHashCode
public class UserPermissions {

    private String businessFunctionName;
    private String privilegeName;
}
