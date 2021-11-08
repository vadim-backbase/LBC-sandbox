package com.backbase.accesscontrol.business.useraccess;

import static com.backbase.accesscontrol.util.InternalRequestUtil.getInternalRequest;

import com.backbase.accesscontrol.business.serviceagreement.service.ServiceAgreementClientCommunicationService;
import com.backbase.accesscontrol.service.impl.UserAccessPrivilegeService;
import com.backbase.accesscontrol.util.constants.EndpointConstants;
import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.users.PrivilegesGetResponseBody;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.apache.camel.Consume;
import org.apache.camel.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Business consumer retrieving a List of Privileges. This class is the business process component of the access-group
 * presentation service, communicating with the p&p service and retrieving all privileges.
 */
@Service
@AllArgsConstructor
public class ListPrivileges {

    private static final Logger LOGGER = LoggerFactory.getLogger(ListPrivileges.class);

    private UserAccessPrivilegeService userAccessPrivilegeService;
    private ServiceAgreementClientCommunicationService serviceAgreementClientCommunicationService;

    /**
     * Sends request to pandp service for retrieving all privileges.
     *
     * @param internalRequest    the internal request
     * @param userId             user id
     * @param serviceAgreementId service agreement id
     * @param functionName       function name
     * @param resourceName       resource name
     * @return Business Process Result of List{@link PrivilegesGetResponseBody}
     */
    @Consume(value = EndpointConstants.DIRECT_DEFAULT_LIST_PRIVILEGES)
    public InternalRequest<List<PrivilegesGetResponseBody>> getAllPrivileges(
        InternalRequest<Void> internalRequest,
        @Header("userId") String userId,
        @Header("serviceAgreementId") String serviceAgreementId,
        @Header("functionName") String functionName,
        @Header("resourceName") String resourceName) {
        LOGGER.info(
            "Trying to fetch all privileges for user with id {}, service Agreement Id {},"
                + " function with name {} and resource with name {}",
            userId, serviceAgreementId, functionName, resourceName);

        String serviceAgreement = serviceAgreementClientCommunicationService
            .getServiceAgreementIdForUserWithUserId(userId, serviceAgreementId);

        List<String> privileges = userAccessPrivilegeService
            .getPrivileges(userId, serviceAgreement, resourceName, functionName);

        List<PrivilegesGetResponseBody> privilegesGetResponseBodies = privileges.stream()
            .map(privilegeName -> new PrivilegesGetResponseBody()
                .withPrivilege(privilegeName))
            .collect(Collectors.toList());
        return getInternalRequest(privilegesGetResponseBodies, internalRequest.getInternalRequestContext());
    }

}
