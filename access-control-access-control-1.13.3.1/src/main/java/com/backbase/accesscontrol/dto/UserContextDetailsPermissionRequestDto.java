package com.backbase.accesscontrol.dto;

import com.backbase.accesscontrol.client.rest.spec.model.PermissionsRequest;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@AllArgsConstructor
@Getter
@EqualsAndHashCode
public class UserContextDetailsPermissionRequestDto {

    private String userId;
    private String serviceAgreementId;
    private PermissionsRequest permissionsRequest;

}
