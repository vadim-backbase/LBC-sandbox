package com.backbase.accesscontrol.domain.dto;

import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
public class PersistenceUserContextPermissionsApproval {

    private Set<UserContextPermissions> permissions = new HashSet<>();
}
