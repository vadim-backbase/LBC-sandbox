package com.backbase.accesscontrol.business.useraccess;

import static com.backbase.accesscontrol.util.InternalRequestUtil.getVoidInternalRequest;

import com.backbase.accesscontrol.business.serviceagreement.service.ServiceAgreementClientCommunicationService;
import com.backbase.accesscontrol.service.impl.UserAccessPermissionCheckService;
import com.backbase.accesscontrol.util.constants.EndpointConstants;
import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import lombok.AllArgsConstructor;
import org.apache.camel.Consume;
import org.apache.camel.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * This class is the business process component of the access-group presentation service, communicating with the p&p
 * service and validating whether user has permissions for the given resource or not.
 */
@Service
@AllArgsConstructor
public class ValidatePermissions {

    private static final Logger LOGGER = LoggerFactory.getLogger(ValidatePermissions.class);

    private UserAccessPermissionCheckService userAccessPermissionCheckService;
    private ServiceAgreementClientCommunicationService serviceAgreementClientCommunicationService;

    /**
     * Sends request to pandp for permission check.
     *
     * @param internalRequest    void internal request
     * @param userId             user id
     * @param serviceAgreementId service agreement id
     * @param resourceName       resource name
     * @param functionName       function name
     * @param privileges         privileges
     * @return Business Process Result of {@link Void}
     */
    @Consume(value = EndpointConstants.DIRECT_DEFAULT_CHECK_PERMISSIONS)
    public InternalRequest<Void> getUserPermissionCheck(
        InternalRequest<Void> internalRequest,
        @Header("userId") String userId,
        @Header("serviceAgreementId") String serviceAgreementId,
        @Header("resourceName") String resourceName,
        @Header("functionName") String functionName,
        @Header("privileges") String privileges) {
        LOGGER.info(
            "Trying to check permissions for user with userId {}, service Agreement Id {}, "
                + "function with name {}, resource with name {} for permissions {}",
            userId, serviceAgreementId, functionName, resourceName, privileges);

        String serviceAgreement = serviceAgreementClientCommunicationService
            .getServiceAgreementIdForUserWithUserId(userId,
                serviceAgreementId);
        userAccessPermissionCheckService
            .checkUserPermission(
                userId,
                serviceAgreement,
                functionName,
                resourceName,
                privileges);

        return getVoidInternalRequest(internalRequest.getInternalRequestContext());
    }
}
