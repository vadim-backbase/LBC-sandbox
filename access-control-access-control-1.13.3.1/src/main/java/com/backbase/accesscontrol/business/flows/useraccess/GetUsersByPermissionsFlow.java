package com.backbase.accesscontrol.business.flows.useraccess;

import com.backbase.accesscontrol.business.flows.AbstractFlow;
import com.backbase.accesscontrol.business.service.UserAccessPAndPService;
import com.backbase.accesscontrol.dto.GetUsersByPermissionsParameters;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.users.UserFunctionGroupsGetResponseBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.users.UsersByPermissionsResponseBody;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;


@Service
@AllArgsConstructor
public class GetUsersByPermissionsFlow extends
    AbstractFlow<GetUsersByPermissionsParameters, UsersByPermissionsResponseBody> {

    private static final Logger LOGGER = LoggerFactory.getLogger(GetUsersByPermissionsFlow.class);

    private UserAccessPAndPService persistenceService;


    @Override
    protected UsersByPermissionsResponseBody execute(GetUsersByPermissionsParameters searchParameters) {

        LOGGER.info("Get users filtered by permissions with search {}.", searchParameters);

        return new UsersByPermissionsResponseBody()
            .withUserIds(persistenceService.getUsersByPermissions(searchParameters).stream()
                .map(UserFunctionGroupsGetResponseBody::getUserId).collect(
                    Collectors.toList()));
    }
}
