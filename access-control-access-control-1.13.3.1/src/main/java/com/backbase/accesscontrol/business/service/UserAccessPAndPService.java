package com.backbase.accesscontrol.business.service;

import com.backbase.accesscontrol.dto.GetUsersByPermissionsParameters;
import com.backbase.accesscontrol.service.impl.UserAccessFunctionGroupService;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.users.UserFunctionGroupsGetResponseBody;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * A service class that communicates with the P&P services via the clients and returns responses.
 */
@Service
@AllArgsConstructor
public class UserAccessPAndPService {

    private UserAccessFunctionGroupService userAccessFunctionGroupService;

    /**
     * Gets all user internal ids together with assigned function group internal ids from AC P&P service filtered by
     * service agreement, function name, privilege, data group type, data item id.
     *
     * @param getUsersFunctionGroupsQueryParameters {@link GetUsersByPermissionsParameters}.
     * @return List of {@link UserFunctionGroupsGetResponseBody}.
     */
    public List<UserFunctionGroupsGetResponseBody> getUsersByPermissions(
        GetUsersByPermissionsParameters getUsersFunctionGroupsQueryParameters) {

        return userAccessFunctionGroupService
            .getUsersFunctionGroups(getUsersFunctionGroupsQueryParameters.getServiceAgreementId(),
                getUsersFunctionGroupsQueryParameters.getFunctionName(),
                getUsersFunctionGroupsQueryParameters.getPrivilege(),
                getUsersFunctionGroupsQueryParameters.getDataGroupType(),
                getUsersFunctionGroupsQueryParameters.getDataItemId()).entrySet()
            .stream().map(entry -> new UserFunctionGroupsGetResponseBody().withUserId(entry.getKey())
                .withFunctionGroupIds(entry.getValue())).collect(Collectors.toList());
    }

}