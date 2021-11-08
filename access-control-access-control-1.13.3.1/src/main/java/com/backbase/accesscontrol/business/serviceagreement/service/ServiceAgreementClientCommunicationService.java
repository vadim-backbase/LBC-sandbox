package com.backbase.accesscontrol.business.serviceagreement.service;

import static com.backbase.accesscontrol.util.ExceptionUtil.getForbiddenException;
import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_079;

import com.backbase.accesscontrol.api.service.ServiceAgreementQueryController;
import com.backbase.accesscontrol.business.service.UserManagementService;
import com.backbase.accesscontrol.service.facades.ServiceAgreementServiceFacade;
import com.backbase.accesscontrol.service.impl.PersistenceLegalEntityService;
import com.backbase.accesscontrol.util.properties.MasterServiceAgreementFallbackProperties;
import com.backbase.dbs.user.api.client.v2.model.GetUsersList;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.serviceagreements.ServiceAgreementAdminsGetResponseBody;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.serviceagreements.ServiceAgreementUsersGetResponseBody;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Service component making calls to Access control Service Agreement query side, using {@link
 * ServiceAgreementQueryController}.
 */
@Service
@RequiredArgsConstructor
public class ServiceAgreementClientCommunicationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceAgreementClientCommunicationService.class);

    private final ServiceAgreementServiceFacade serviceAgreementServiceFacade;

    private final UserManagementService userManagementService;
    private final PersistenceLegalEntityService persistenceLegalEntityService;
    private final MasterServiceAgreementFallbackProperties fallbackProperties;

    /**
     * Return the MasterServiceAgreementID if serviceAgreementId is null.
     *
     * @param userId             the name of the user
     * @param serviceAgreementId the ID of the service agreement
     * @return the MasterServiceAgreementID if serviceAgreementId is null.
     */
    public String getServiceAgreementIdForUserWithUserId(String userId, String serviceAgreementId) {

        if (serviceAgreementId == null) {
            if (!fallbackProperties.isEnabled()) {
                throw getForbiddenException(ERR_ACQ_079.getErrorMessage(), ERR_ACQ_079.getErrorCode());
            }
            serviceAgreementId = getMasterServiceAgreementForUserWithUserId(userId);
        }
        return serviceAgreementId;
    }

    /**
     * Retrieves user to be assigned as admins from p&p service.
     *
     * @param admins - admins to be retrieved from p&p
     * @return list of {@link GetUsersList}
     */
    public Optional<GetUsersList> getUsers(Set<String> admins) {
        LOGGER.info("Getting Users {} to be assigned as admins by Legal Entity", admins);
        GetUsersList users;

        if (CollectionUtils.isEmpty(admins)) {
            users = new GetUsersList();
        } else {
            String adminsString = String.join(",", admins);
            users = userManagementService.getUsers(adminsString);

        }
        return Optional.ofNullable(users);
    }

    private String getMasterServiceAgreementForUserWithUserId(String userId) {
        String userLegalEntityId = userManagementService.getUserByInternalId(userId).getLegalEntityId();

        return persistenceLegalEntityService.getMasterServiceAgreement(userLegalEntityId).getId();
    }

    /**
     * Gets an object of {@link ServiceAgreementAdminsGetResponseBody} in the Service Agreement.
     *
     * @param serviceAgreementId Id of the service agreement
     * @return list of  {@link ServiceAgreementAdminsGetResponseBody}
     */
    public ServiceAgreementAdminsGetResponseBody getServiceAgreementAdmins(String serviceAgreementId) {
        LOGGER.info("Trying to get participants on service agreement with id: {}", serviceAgreementId);

        return serviceAgreementServiceFacade.getServiceAgreementAdmins(serviceAgreementId);
    }

    /**
     * Gets an object of {@link ServiceAgreementUsersGetResponseBody}, in the Service Agreement.
     *
     * @param serviceAgreementId Id of the service agreement
     * @return list of  {@link ServiceAgreementUsersGetResponseBody}
     */
    public ServiceAgreementUsersGetResponseBody getServiceAgreementUsers(String serviceAgreementId) {
        LOGGER.info("Trying to get participants on service agreement with id {}", serviceAgreementId);

        return serviceAgreementServiceFacade.getServiceAgreementUsers(serviceAgreementId);
    }

    /**
     * Retrieves admins to be updated defined in participants for the service agreement from user pandp.
     *
     * @param participants list of Participant
     * @return list of {@link GetUsersList}
     */
    public GetUsersList getAdminsToBeUpdated(
        List<com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements
            .Participant> participants) {
        GetUsersList admins;
        List<String> adminsIds = participants.stream()
            .flatMap(participant -> Optional.ofNullable(participant.getAdmins())
                .orElse(new HashSet<>())
                .stream())
            .collect(Collectors.toList());

        if (adminsIds.isEmpty()) {
            admins = new GetUsersList();
        } else {
            String adminsString = String.join(",", adminsIds);
            admins = userManagementService.getUsers(adminsString);
        }
        return admins;
    }
}
