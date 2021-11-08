package com.backbase.accesscontrol.business.useraccess;

import static com.backbase.accesscontrol.util.InternalRequestUtil.getInternalRequest;
import static java.util.Collections.emptyList;
import static java.util.Objects.isNull;

import com.backbase.accesscontrol.dto.UserPrivilegesSummaryGetResponseBodyDto;
import com.backbase.accesscontrol.service.impl.UserAccessPrivilegeService;
import com.backbase.accesscontrol.util.UserContextUtil;
import com.backbase.accesscontrol.util.constants.EndpointConstants;
import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.users.UserPermissionsSummaryGetResponseBody;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.apache.camel.Body;
import org.apache.camel.Consume;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ListUserPrivilegesSummary {

    private static final Logger LOGGER = LoggerFactory.getLogger(ListUserPrivilegesSummary.class);

    private UserAccessPrivilegeService privilegeService;
    private UserContextUtil userContextUtil;

    /**
     * Method that listens on the direct:getUserPrivilegesSummaryRequestedInternal endpoint and uses the {@link
     * UserAccessPrivilegeService} to forward the request to the P&P service.
     *
     * @param request internal request to be send by the client
     * @return Internal Request of List{@link UserPermissionsSummaryGetResponseBody}
     */
    @Consume(value = EndpointConstants.DIRECT_DEFAULT_LIST_USER_PRIVILEGES_SUMMARY)
    public InternalRequest<List<UserPermissionsSummaryGetResponseBody>> getUserPrivilegesSummary(
        @Body InternalRequest<Void> request) {

        LOGGER.info("Getting User Privileges Summary");

        String serviceAgreementId = userContextUtil.getServiceAgreementId();
        String internalUserId = userContextUtil.getUserContextDetails().getInternalUserId();

        LOGGER.info("Getting Privileges Summary for user with id {} and service agreement id {}",
            internalUserId, serviceAgreementId);

        List<UserPrivilegesSummaryGetResponseBodyDto> response = privilegeService
            .getPrivilegesSummary(internalUserId,
                serviceAgreementId);

        return getInternalRequest(convertToPresentationResponse(response), request.getInternalRequestContext());
    }

    private List<UserPermissionsSummaryGetResponseBody> convertToPresentationResponse(
        List<UserPrivilegesSummaryGetResponseBodyDto> responseBodies) {
        if (isNull(responseBodies)) {
            return emptyList();
        }

        return responseBodies.stream()
            .map(responseBody -> new UserPermissionsSummaryGetResponseBody()
                .withResource(responseBody.getResource())
                .withFunction(responseBody.getFunction())
                .withPermissions(responseBody.getPrivileges()))
            .collect(Collectors.toList());
    }
}
