package com.backbase.accesscontrol.business.serviceagreement;

import static com.backbase.accesscontrol.util.ExceptionUtil.getBadRequestException;
import static com.backbase.accesscontrol.util.InternalRequestUtil.getVoidInternalRequest;
import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_030;

import com.backbase.accesscontrol.business.persistence.serviceagreement.UpdateServiceAgreementAdminsHandler;
import com.backbase.accesscontrol.business.service.UserManagementService;
import com.backbase.accesscontrol.business.serviceagreement.service.ServiceAgreementClientCommunicationService;
import com.backbase.accesscontrol.business.serviceagreement.service.ServiceAgreementValidator;
import com.backbase.accesscontrol.dto.parameterholder.SingleParameterHolder;
import com.backbase.accesscontrol.util.constants.EndpointConstants;
import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.dbs.user.api.client.v2.model.GetUsersList;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.AdminsPutRequestBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.LegalEntityAdmins;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.apache.camel.Body;
import org.apache.camel.Consume;
import org.apache.camel.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UpdateAdmins {

    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateAdmins.class);

    private UserManagementService userManagementService;
    private ServiceAgreementClientCommunicationService serviceAgreementClientCommunicationService;
    private ServiceAgreementValidator serviceAgreementValidator;
    private UpdateServiceAgreementAdminsHandler updateServiceAgreementAdminsHandler;

    /**
     * Method that listens on the direct:updateAdminsRequestedInternal endpoint
     *
     * @param request            Internal Request of {@link AdminsPutRequestBody} type to be send by the client
     * @param serviceAgreementId id of service agreement
     * @return Void InternalRequest
     */
    @Consume(value = EndpointConstants.DIRECT_DEFAULT_UPDATE_ADMINS)
    public InternalRequest<Void> updateAdmins(@Body InternalRequest<AdminsPutRequestBody> request,
        @Header("id") String serviceAgreementId) {
        LOGGER.info("Trying to update admins of the service agreement with id {}", serviceAgreementId);
        updateServiceAgreementAdmins(request.getData(), serviceAgreementId);
        return getVoidInternalRequest(request.getInternalRequestContext());
    }

    private void updateServiceAgreementAdmins(AdminsPutRequestBody putRequestBody,
        String serviceAgreementId) {

        serviceAgreementValidator.validateDuplicateParticipants(
            putRequestBody.getParticipants().stream().map(LegalEntityAdmins::getId).collect(Collectors.toList()));

        Map<String, Set<String>> participantAdminsRequest = putRequestBody.getParticipants().stream()
            .collect(Collectors.toMap(LegalEntityAdmins::getId, LegalEntityAdmins::getAdmins));

        Set<String> allAdminUsersToGet = participantAdminsRequest.values().stream().flatMap(Collection::stream)
            .collect(Collectors.toSet());

        Optional<GetUsersList> participantAdmins = serviceAgreementClientCommunicationService
            .getUsers(allAdminUsersToGet);

        if (participantAdmins.isPresent()) {
            Map<String, Set<String>> adminsByEntity = userManagementService
                .getUsersByLegalEntityId(participantAdmins.get().getUsers());
            serviceAgreementValidator.validateListOfAdmins(participantAdminsRequest, adminsByEntity);

            updateServiceAgreementAdminsHandler
                .handleRequest(new SingleParameterHolder<>(serviceAgreementId), putRequestBody);
        } else {
            LOGGER.warn("Admins must be valid users.");
            throw getBadRequestException(ERR_AG_030.getErrorMessage(), ERR_AG_030.getErrorCode());
        }
    }
}
