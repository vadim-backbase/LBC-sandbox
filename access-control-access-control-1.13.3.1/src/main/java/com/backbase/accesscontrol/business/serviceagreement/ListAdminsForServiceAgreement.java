package com.backbase.accesscontrol.business.serviceagreement;

import static com.backbase.accesscontrol.util.InternalRequestUtil.getInternalRequest;

import com.backbase.accesscontrol.business.service.UserManagementService;
import com.backbase.accesscontrol.dto.ListElementsWrapper;
import com.backbase.accesscontrol.service.ObjectConverter;
import com.backbase.accesscontrol.service.impl.ServiceAgreementAdminService;
import com.backbase.accesscontrol.util.constants.EndpointConstants;
import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementUsersGetResponseBody;
import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import org.apache.camel.Consume;
import org.apache.camel.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Business consumer retrieving a List of Admins for service Agreement. This class is the business process component of
 * the access-group presentation service, communicating with the p&p service, retrieving all Admins for Service
 * Agreement.
 */
@Service
@AllArgsConstructor
public class ListAdminsForServiceAgreement {

    private static final Logger LOGGER = LoggerFactory.getLogger(ListAdminsForServiceAgreement.class);

    private UserManagementService userManagementService;
    private ObjectConverter objectConverter;
    private ServiceAgreementAdminService serviceAgreementAdminService;

    /**
     * Sends request to pandp service for retrieveing admins for given service agreement.
     *
     * @param internalRequest internal request
     * @param serviceAgreementId id of the service agreement
     * @return internal request of list of {@link ServiceAgreementUsersGetResponseBody}
     */
    @Consume(value = EndpointConstants.DIRECT_DEFAULT_LIST_ADMIN_USERS_FOR_SERVICE_AGREEMENT)
    public InternalRequest<List<ServiceAgreementUsersGetResponseBody>> getAdminsForServiceAgreement(
        InternalRequest<Void> internalRequest,
        @Header("serviceAgreementId") String serviceAgreementId) {
        LOGGER.info("Trying to list all admins for service agreement id {}", serviceAgreementId);
        return getInternalRequest(getAdminsForServiceAgreement(serviceAgreementId),
            internalRequest.getInternalRequestContext());
    }

    private List<ServiceAgreementUsersGetResponseBody> getAdminsForServiceAgreement(
        String serviceAgreementId) {
        Set<String> admins = serviceAgreementAdminService.getServiceAgreementAdmins(serviceAgreementId).getAdmins();
        ListElementsWrapper<ServiceAgreementUsersGetResponseBody> usersOfServiceAgreementDto = userManagementService
            .getUsersForServiceAgreement(admins, null, null, null, null);

        List<ServiceAgreementUsersGetResponseBody> responseBodies = objectConverter
            .convertList(usersOfServiceAgreementDto.getRecords(), ServiceAgreementUsersGetResponseBody.class);
        return responseBodies;
    }
}
