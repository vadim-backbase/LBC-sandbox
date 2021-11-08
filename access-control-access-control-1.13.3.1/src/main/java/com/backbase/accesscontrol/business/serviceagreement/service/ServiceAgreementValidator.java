package com.backbase.accesscontrol.business.serviceagreement.service;

import static com.backbase.accesscontrol.util.ExceptionUtil.getBadRequestException;
import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_005;
import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_006;
import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_008;
import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_028;
import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_030;
import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_080;
import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_095;

import com.backbase.accesscontrol.business.service.UserManagementService;
import com.backbase.accesscontrol.dto.ServiceAgreementDto;
import com.backbase.accesscontrol.dto.ServiceAgreementParticipantDto;
import com.backbase.accesscontrol.service.DateTimeService;
import com.backbase.dbs.user.api.client.v2.model.GetUser;
import com.backbase.dbs.user.api.client.v2.model.GetUsersList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ServiceAgreementValidator {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceAgreementValidator.class);

    private ServiceAgreementClientCommunicationService serviceAgreementClientCommunicationService;
    private UserManagementService userManagementService;
    private ServiceAgreementBusinessRulesService serviceAgreementBusinessRulesService;
    private DateTimeService dateTimeService;

    /**
     * Validates validity of the payload.
     *
     * @param serviceAgreementDto payload to validate
     */
    public void validatePayload(ServiceAgreementDto serviceAgreementDto) {

        Date fromDate = dateTimeService.getStartDateFromDateAndTime(serviceAgreementDto.getValidFromDate(),
            serviceAgreementDto.getValidFromTime());
        Date untilDate = dateTimeService.getEndDateFromDateAndTime(serviceAgreementDto.getValidUntilDate(),
            serviceAgreementDto.getValidUntilTime());
        if (!serviceAgreementBusinessRulesService.isPeriodValid(fromDate, untilDate)) {
            LOGGER.warn("Invalid validity period: from date {} and end date {}.", fromDate, untilDate);
            throw getBadRequestException(ERR_AG_095.getErrorMessage(), ERR_AG_095.getErrorCode());
        }
        if (serviceAgreementBusinessRulesService.isInvalidParticipant(serviceAgreementDto.getParticipants())) {
            LOGGER.warn("Every participant should share at least users or accounts on service agreement.");
            throw getBadRequestException(ERR_AG_005.getErrorMessage(), ERR_AG_005.getErrorCode());
        }
        if (serviceAgreementBusinessRulesService
            .participantSharingUsersNotExists(serviceAgreementDto.getParticipants())) {
            LOGGER.warn("At least one participant should share users on service agreement.");
            throw getBadRequestException(ERR_AG_006.getErrorMessage(), ERR_AG_006.getErrorCode());
        }
        if (serviceAgreementBusinessRulesService
            .participantSharingAccountsNotExists(serviceAgreementDto.getParticipants())) {
            LOGGER.warn("At least one participant should share accounts on service agreement.");
            throw getBadRequestException(ERR_AG_008.getErrorMessage(), ERR_AG_008.getErrorCode());
        }

        validateDuplicateParticipants(getParticipantIds(serviceAgreementDto));
        validateAdminsByLegalEntity(serviceAgreementDto);
    }

    /**
     * Validates list of admins per legal entity. Checks whether the admins belong to the legal entity or are valid
     * participants.
     *
     * @param adminsToAssign list of admins to check for assignment
     * @param adminsByEntity admins by corresponding legal entity
     */
    public void validateListOfAdmins(Map<String, Set<String>> adminsToAssign, Map<String, Set<String>> adminsByEntity) {
        if (serviceAgreementBusinessRulesService
            .adminUsersNotBelongToTheGivenLegalEntities(adminsByEntity, adminsToAssign)) {
            LOGGER.warn("Admins must belong to the participants of the service agreement");
            throw getBadRequestException(ERR_AG_028.getErrorMessage(), ERR_AG_028.getErrorCode());
        }
    }

    /**
     * Validates duplicate participant ids in the list.
     *
     * @param participants list of participants ids to be validated
     */
    public void validateDuplicateParticipants(List<String> participants) {
        if (serviceAgreementBusinessRulesService.isDuplicateParticipant(participants)) {
            LOGGER.warn("Duplicated legal entity in participants");
            throw getBadRequestException(ERR_AG_080.getErrorMessage(), ERR_AG_080.getErrorCode());
        }
    }


    private void validateAdminsByLegalEntity(ServiceAgreementDto serviceAgreementDto) {
        Map<String, Set<String>> participantAdminsRequest = serviceAgreementDto.getParticipants().stream()
            .collect(Collectors.toMap(ServiceAgreementParticipantDto::getId,
                participant -> Optional.ofNullable(participant.getAdmins()).orElse(new HashSet<>())));

        Set<String> allAdminUsersToGet = participantAdminsRequest.values().stream().flatMap(Collection::stream)
            .collect(Collectors.toSet());

        Optional<GetUsersList> participantAdmins = serviceAgreementClientCommunicationService
            .getUsers(allAdminUsersToGet);

        if (participantAdmins.isPresent()) {
            List<GetUser> users = participantAdmins.get().getUsers();
            validateListOfAdmins(participantAdminsRequest, userManagementService.getUsersByLegalEntityId(users));
        } else {
            LOGGER.warn("Admins must be valid users.");
            throw getBadRequestException(ERR_AG_030.getErrorMessage(), ERR_AG_030.getErrorCode());
        }
    }

    private List<String> getParticipantIds(ServiceAgreementDto serviceAgreementDto) {
        return serviceAgreementDto.getParticipants()
            .stream()
            .map(ServiceAgreementParticipantDto::getId)
            .collect(Collectors.toList());
    }

}
