package com.backbase.accesscontrol.business.serviceagreement;

import static com.backbase.accesscontrol.util.ExceptionUtil.getBadRequestException;
import static com.backbase.accesscontrol.util.InternalRequestUtil.getVoidInternalRequest;
import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_079;

import com.backbase.accesscontrol.business.persistence.serviceagreement.AddUsersInServiceAgreementHandler;
import com.backbase.accesscontrol.business.service.UserManagementService;
import com.backbase.accesscontrol.dto.UsersDto;
import com.backbase.accesscontrol.dto.parameterholder.SingleParameterHolder;
import com.backbase.accesscontrol.service.impl.PersistenceServiceAgreementService;
import com.backbase.accesscontrol.util.constants.EndpointConstants;
import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.dbs.user.api.client.v2.model.GetUser;
import com.backbase.dbs.user.api.client.v2.model.GetUsersList;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.serviceagreements.Participant;
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
 * Business consumer for adding new  User in Service agreement. This class is a business process component of the
 * access-group presentation service, communicating with the persistence services.
 */
@Service
@AllArgsConstructor
public class AddUsersInServiceAgreement {

    private static final Logger LOGGER = LoggerFactory.getLogger(AddUsersInServiceAgreement.class);

    private PersistenceServiceAgreementService persistenceServiceAgreementService;
    private UserManagementService userManagementService;
    private AddUsersInServiceAgreementHandler addUsersInServiceAgreementHandler;

    /**
     * Method that listens on the direct:addUserInServiceAgreementRequestedInternal endpoint and forward the request to
     * the persistence service.
     *
     * @param internalRequest    internal request withi {@link PresentationUsersForServiceAgreementRequestBody}
     * @param serviceAgreementId id of service agreement
     * @return InternalRequest of Void type
     */
    @Consume(value = EndpointConstants.DIRECT_DEFAULT_ADD_USER_IN_SERVICE_AGREEMENT)
    public InternalRequest<Void> addUserInServiceAgreement(
        InternalRequest<PresentationUsersForServiceAgreementRequestBody> internalRequest,
        @Header("serviceAgreementId") String serviceAgreementId) {
        LOGGER.info("Trying to add users {} in service agreement id {}", serviceAgreementId,
            internalRequest.getData().getUsers());

        List<Participant> serviceAgreementParticipants = persistenceServiceAgreementService
            .getServiceAgreementParticipants(serviceAgreementId);

        GetUsersList usersResponse = userManagementService
            .getUsersByIds(internalRequest.getData().getUsers(), null, null, null, null);

        if (userManagementService.usersDoNotBelongInLegalEntityParticipantsOnServiceAgreement(usersResponse,
            serviceAgreementParticipants)) {
            throw getBadRequestException(ERR_AG_079.getErrorMessage(), ERR_AG_079.getErrorCode());
        }

        List<UsersDto> usersAddPostBodies = convertToUsersDto(usersResponse);
        addUsersInServiceAgreementHandler
            .handleRequest(new SingleParameterHolder<>(serviceAgreementId), usersAddPostBodies);

        return getVoidInternalRequest(internalRequest.getInternalRequestContext());
    }


    private List<UsersDto> convertToUsersDto(GetUsersList users) {
        Map<String, List<GetUser>> usersByLegalEntityIdMap = users.getUsers()
            .stream()
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
