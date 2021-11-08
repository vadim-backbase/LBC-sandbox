package com.backbase.accesscontrol.business.serviceagreement;

import static com.backbase.accesscontrol.util.InternalRequestUtil.getVoidInternalRequest;

import com.backbase.accesscontrol.business.persistence.serviceagreement.RemoveUsersFromServiceAgreementHandler;
import com.backbase.accesscontrol.business.service.UserManagementService;
import com.backbase.accesscontrol.dto.UsersDto;
import com.backbase.accesscontrol.dto.parameterholder.SingleParameterHolder;
import com.backbase.accesscontrol.util.constants.EndpointConstants;
import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.dbs.user.api.client.v2.model.GetUser;
import com.backbase.dbs.user.api.client.v2.model.GetUsersList;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.PresentationUsersForServiceAgreementRequestBody;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.apache.camel.Consume;
import org.apache.camel.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Business consumer for removing user Service agreement. This class is a business process component of the access-group
 * presentation service, communicating with the P&P services.
 */
@Service
@AllArgsConstructor
public class RemoveUsersFromServiceAgreement {

    private static final Logger LOGGER = LoggerFactory.getLogger(RemoveUsersFromServiceAgreement.class);

    private UserManagementService userManagementService;
    private RemoveUsersFromServiceAgreementHandler removeUsersFromServiceAgreementHandler;

    /**
     * Method that listens on the direct:removeUsersFromServiceAgreementRequestedInternal endpoint
     *
     * @param internalRequest    internal request
     * @param serviceAgreementId id of service agreement
     * @return Void InternalRequest
     */
    @Consume(value = EndpointConstants.DIRECT_DEFAULT_REMOVE_USER_FROM_SERVICE_AGREEMENT)
    public InternalRequest<Void> removeUserInServiceAgreement(
        InternalRequest<PresentationUsersForServiceAgreementRequestBody> internalRequest,
        @Header("serviceAgreementId") String serviceAgreementId) {
        LOGGER.info("Trying to remove users {} in service agreement from {}", internalRequest.getData().getUsers(),
            serviceAgreementId);
        removeUserInServiceAgreement(internalRequest.getData(), serviceAgreementId);
        return getVoidInternalRequest(internalRequest.getInternalRequestContext());
    }

    private void removeUserInServiceAgreement(
        PresentationUsersForServiceAgreementRequestBody serviceAgreementRequestBody, String serviceAgreementId) {

        GetUsersList usersResponse = userManagementService
            .getUsersByIds(serviceAgreementRequestBody.getUsers(), null, null, null, null);
        List<UsersDto> usersRemoveRequest = convertToUsersDto(usersResponse);

        removeUsersFromServiceAgreementHandler
            .handleRequest(new SingleParameterHolder<>(serviceAgreementId), usersRemoveRequest);
    }

    private List<UsersDto> convertToUsersDto(GetUsersList users) {
        Map<String, List<GetUser>> usersByLegalEntityIdMap = users.getUsers().stream()
            .collect(Collectors.groupingBy(GetUser::getLegalEntityId));

        return usersByLegalEntityIdMap.entrySet()
            .stream()
            .map(usersByLE ->
                new UsersDto()
                    .withUsers(usersByLE.getValue().stream()
                        .map(GetUser::getId)
                        .collect(Collectors.toList()))
                    .withLegalEntityId(usersByLE.getKey())
            ).collect(Collectors.toList());
    }
}
