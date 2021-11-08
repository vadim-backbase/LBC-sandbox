package com.backbase.accesscontrol.service.facades;

import com.backbase.accesscontrol.business.flows.useraccess.GetPermissionDataGroupsFlow;
import com.backbase.accesscontrol.client.rest.spec.model.PermissionsDataGroup;
import com.backbase.accesscontrol.client.rest.spec.model.PermissionsRequest;
import com.backbase.accesscontrol.dto.UserContextDetailsPermissionRequestDto;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserContextFlowService {

    private GetPermissionDataGroupsFlow getPermissionDataGroupsFlow;

    /**
     * Get user context permission data groups by parameters.
     *
     * @param userId             user id from context
     * @param serviceAgreementId service agreement id from context
     * @param parameters         {@link PermissionsRequest}
     * @return {@link PermissionsDataGroup}
     */
    public PermissionsDataGroup getUserContextPermissions(String userId, String serviceAgreementId,
        PermissionsRequest parameters) {
        return getPermissionDataGroupsFlow
            .start(new UserContextDetailsPermissionRequestDto(userId, serviceAgreementId, parameters));
    }
}
