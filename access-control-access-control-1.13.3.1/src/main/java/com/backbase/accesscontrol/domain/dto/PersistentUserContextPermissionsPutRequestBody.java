package com.backbase.accesscontrol.domain.dto;

import java.util.Set;

import lombok.Data;

@Data
public class PersistentUserContextPermissionsPutRequestBody {

    private String userLegalEntityId;
    private Set<UserContextPermissions> permissions;
}
