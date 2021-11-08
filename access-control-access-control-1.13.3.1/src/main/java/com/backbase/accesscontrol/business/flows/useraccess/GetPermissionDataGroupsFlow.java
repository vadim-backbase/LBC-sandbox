package com.backbase.accesscontrol.business.flows.useraccess;

import com.backbase.accesscontrol.business.flows.AbstractFlow;
import com.backbase.accesscontrol.client.rest.spec.model.PermissionsDataGroup;
import com.backbase.accesscontrol.dto.UserContextDetailsPermissionRequestDto;
import com.backbase.accesscontrol.service.impl.UserAccessPrivilegeService;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;


@Service
@AllArgsConstructor
public class GetPermissionDataGroupsFlow extends
    AbstractFlow<UserContextDetailsPermissionRequestDto, PermissionsDataGroup> {

    private static final Logger LOGGER = LoggerFactory.getLogger(GetPermissionDataGroupsFlow.class);

    private UserAccessPrivilegeService userAccessPrivilegeService;

    /**
     * {@inheritDoc}
     */
    @Override
    protected PermissionsDataGroup execute(UserContextDetailsPermissionRequestDto parameters) {

        LOGGER.info("Get permissions data groups.");

        return userAccessPrivilegeService
            .getPermissionsDataGroup(parameters.getUserId(), parameters.getServiceAgreementId(),
                parameters.getPermissionsRequest());
    }
}
