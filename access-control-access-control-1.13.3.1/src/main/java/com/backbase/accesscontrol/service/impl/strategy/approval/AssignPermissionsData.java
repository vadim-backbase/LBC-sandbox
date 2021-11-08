package com.backbase.accesscontrol.service.impl.strategy.approval;

import com.backbase.accesscontrol.domain.dto.UserContextPermissions;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class AssignPermissionsData {

    private String serviceAgreementId;
    private String userId;
    private String legalEntityId;
    private Set<UserContextPermissions> permissions;

}
