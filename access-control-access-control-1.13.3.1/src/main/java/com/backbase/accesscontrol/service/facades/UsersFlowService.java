package com.backbase.accesscontrol.service.facades;

import com.backbase.accesscontrol.business.flows.useraccess.GetUsersByPermissionsFlow;
import com.backbase.accesscontrol.dto.GetUsersByPermissionsParameters;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.users.UsersByPermissionsResponseBody;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UsersFlowService {

    private GetUsersByPermissionsFlow getUsersByPermissionsFlow;

    /**
     * Get users from service agreement filtered by permissions.
     *
     * @param parameters - {@link GetUsersByPermissionsParameters}
     * @return {@link UsersByPermissionsResponseBody}
     */
    public UsersByPermissionsResponseBody getUsersByPermissions(
        GetUsersByPermissionsParameters parameters) {
        return  getUsersByPermissionsFlow.start(parameters);
    }
}
