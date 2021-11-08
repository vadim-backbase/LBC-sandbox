package com.backbase.accesscontrol.domain;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@AllArgsConstructor
@Getter
@EqualsAndHashCode
public class PersistenceUserPermissionKey {

    private String resource;
    private String businessFunction;
}
