package com.backbase.accesscontrol.business.serviceagreement;

import static com.backbase.accesscontrol.util.InternalRequestUtil.getInternalRequest;

import com.backbase.accesscontrol.mappers.PersistenceApprovalPermissionsPresentationApprovalPermissionMapper;
import com.backbase.accesscontrol.service.ApprovalService;
import com.backbase.accesscontrol.util.constants.EndpointConstants;
import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.serviceagreements.PersistenceApprovalPermissions;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.PresentationApprovalPermissions;
import lombok.AllArgsConstructor;
import org.apache.camel.Body;
import org.apache.camel.Consume;
import org.apache.camel.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class GetPersistenceApprovalPermissions {

    private static final Logger LOGGER = LoggerFactory.getLogger(GetPersistenceApprovalPermissions.class);

    private ApprovalService approvalService;
    private PersistenceApprovalPermissionsPresentationApprovalPermissionMapper mapper;

    /**
     * Listens for requests for user permissions for given service agreement. It returns "approvalId" as well if
     * approval functionality is ON.
     *
     * @param internalRequest    Internal request wrapper.
     * @param serviceAgreementId Id of the service agreement.
     * @param userId             Id of the user.
     * @return internal request of {@link PresentationApprovalPermissions}
     */
    @Consume(value = EndpointConstants.DIRECT_GET_ASSIGNED_USERS_PERMISSIONS_INTERNAL)
    public InternalRequest<PresentationApprovalPermissions> getAssignedUsersPermissions(
        @Body InternalRequest<Void> internalRequest,
        @Header("serviceAgreementId") String serviceAgreementId,
        @Header("userId") String userId) {

        LOGGER.info("Trying to get permissions for user {} in service agreement {}", userId, serviceAgreementId);

        PersistenceApprovalPermissions persistenceApprovalPermissions = approvalService
            .getPersistenceApprovalPermissions(userId, serviceAgreementId);

        PresentationApprovalPermissions presentationApprovalPermissions = mapper
            .sourceToDestination(persistenceApprovalPermissions);

        return getInternalRequest(presentationApprovalPermissions, internalRequest.getInternalRequestContext());
    }
}
