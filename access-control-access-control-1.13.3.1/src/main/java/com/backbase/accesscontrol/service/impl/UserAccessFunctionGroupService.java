package com.backbase.accesscontrol.service.impl;

import static com.backbase.accesscontrol.domain.GraphConstants.SERVICE_AGREEMENT_EXTENDED;
import static com.backbase.accesscontrol.util.ExceptionUtil.getBadRequestException;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_041;
import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_006;
import static org.apache.commons.lang.StringUtils.isEmpty;

import com.backbase.accesscontrol.domain.FunctionGroup;
import com.backbase.accesscontrol.domain.Participant;
import com.backbase.accesscontrol.domain.ServiceAgreement;
import com.backbase.accesscontrol.domain.UserAssignedFunctionGroup;
import com.backbase.accesscontrol.domain.UserContext;
import com.backbase.accesscontrol.domain.enums.FunctionGroupType;
import com.backbase.accesscontrol.repository.FunctionGroupJpaRepository;
import com.backbase.accesscontrol.repository.ServiceAgreementJpaRepository;
import com.backbase.accesscontrol.repository.UserAssignedFunctionGroupJpaRepository;
import com.backbase.accesscontrol.service.BusinessFunctionCache;
import com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class UserAccessFunctionGroupService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserAccessFunctionGroupService.class);

    private FunctionGroupJpaRepository functionGroupJpaRepository;
    private UserAssignedFunctionGroupJpaRepository userAssignedFunctionGroupJpaRepository;
    private ServiceAgreementJpaRepository serviceAgreementJpaRepository;
    private UserContextService userContextService;
    private BusinessFunctionCache businessFunctionCache;

    /**
     * Deletes function group from user access.
     *
     * @param functionGroupId            - id of the function group
     * @param userId                     - id of the user on which the function groups is assigned
     * @param serviceAgreementIdFromBody - id of the service agreement
     */
    @Transactional
    public void deleteFunctionGroupFromUserAccess(String functionGroupId, String userId,
        String serviceAgreementIdFromBody) {
        LOGGER.info("Trying to remove Function group {} from user {} under service agreement {}",
            functionGroupId, userId, serviceAgreementIdFromBody);

        FunctionGroup functionGroup = getFunctionGroupById(functionGroupId, FunctionGroupType.DEFAULT);
        validateFunctionGroupBelongsToServiceAgreement(serviceAgreementIdFromBody, functionGroup);

        ServiceAgreement serviceAgreement = getServiceAgreementById(serviceAgreementIdFromBody);
        validateIfUserIsExposedOnCustomServiceAgreement(userId, serviceAgreement);

        Optional<UserAssignedFunctionGroup> userAssignedFunctionGroups = getUserAssignedFunctionGroups(functionGroupId,
            userId, serviceAgreementIdFromBody);
        checkFunctionGroupAssignmentStatus(!userAssignedFunctionGroups.isPresent(), QueryErrorCodes.ERR_ACQ_028);

        userAssignedFunctionGroups.ifPresent(uaFg -> userAssignedFunctionGroupJpaRepository.delete(uaFg));
    }

    /**
     * Deletes System function group from user that is admin.
     *
     * @param functionGroupId  - id of the function group
     * @param userId           - id of the user on which the function groups is assigned
     * @param serviceAgreement - service agreement
     */
    @Transactional
    public void deleteSystemFunctionGroupFromUserAccess(String functionGroupId, String userId,
        ServiceAgreement serviceAgreement) {
        LOGGER.info("Trying to remove Function group {} from user {} under service agreement {}",
            functionGroupId, userId, serviceAgreement.getId());

        Optional<UserAssignedFunctionGroup> userAssignedFunctionGroup = getUserAssignedFunctionGroups(functionGroupId,
            userId, serviceAgreement.getId());

        userAssignedFunctionGroup.ifPresent(uaFg -> {
            userAssignedFunctionGroupJpaRepository.delete(uaFg);
            if (!userAssignedFunctionGroupJpaRepository.existsByUserContextId(uaFg.getUserContextId())) {
                userContextService.delete(uaFg.getUserContextId());
            }
        });

    }

    /**
     * Assigns function group to user that is admin on service agreement.
     *
     * @param functionGroupId  id of function group;
     * @param serviceAgreement service agreement
     * @param userContext      user context
     * @return {@link UserAssignedFunctionGroup}
     */
    @Transactional
    public UserAssignedFunctionGroup addSystemFunctionGroupToUserAccess(String functionGroupId,
        ServiceAgreement serviceAgreement, UserContext userContext) {
        LOGGER.info("Trying to assign System Function group {} to user {} under service agreement {}",
            functionGroupId, userContext.getUserId(), serviceAgreement.getId());

        FunctionGroup functionGroup = getFunctionGroupFromServiceAgreementById(serviceAgreement, functionGroupId);

        UserAssignedFunctionGroup userAssignedFunctionGroup = new UserAssignedFunctionGroup(functionGroup, userContext);

        return userAssignedFunctionGroupJpaRepository.save(userAssignedFunctionGroup);
    }

    /**
     * Method which return a map of user internal ids together with assigned function group internal ids filtered by the
     * parameters below.
     *
     * @param serviceAgreementId service agreement id
     * @param functionName       function name
     * @param privilege          privilege name, not required
     * @param dataItemType       data item type, not required, if provided should provide data item id
     * @param dataItemId         data item id, not required, if provided should provide data item type
     * @return map of user internal ids together with assigned function group internal ids
     */
    @Transactional(readOnly = true)
    public Map<String, Set<String>> getUsersFunctionGroups(String serviceAgreementId,
        String functionName, String privilege, String dataItemType, String dataItemId) {
        LOGGER.info("Trying to get user internal ids together with assigned function group internal ids filtered by "
                + "service agreement id {}, function name {}, privilege {} data group type {} data item id {}",
            serviceAgreementId, functionName, privilege, dataItemType, dataItemId);

        Set<String> afpIds = businessFunctionCache
            .getByFunctionNameOrResourceNameOrPrivilegesOptional(functionName, null,
                isEmpty(privilege) ? null : Collections.singleton(privilege));

        if (isEmpty(dataItemType) && isEmpty(dataItemId)) {
            return userAssignedFunctionGroupJpaRepository.findByServiceAgreementIdAndAfpIds(serviceAgreementId, afpIds);
        } else {
            return userAssignedFunctionGroupJpaRepository
                .findByServiceAgreementIdAndDataItemIdAndDataGroupTypeAndAfpIds(serviceAgreementId, dataItemId,
                    dataItemType, afpIds);
        }
    }

    private ServiceAgreement getServiceAgreementById(String serviceAgreementId) {
        return serviceAgreementJpaRepository.findById(serviceAgreementId, SERVICE_AGREEMENT_EXTENDED)
            .orElseThrow(() -> {
                LOGGER.warn("Service agreement with id {} does not exist", serviceAgreementId);
                return getBadRequestException(ERR_ACQ_006.getErrorMessage(), ERR_ACQ_006.getErrorCode());
            });
    }

    private FunctionGroup getFunctionGroupById(String functionGroupId, FunctionGroupType type) {
        return functionGroupJpaRepository.findByIdAndType(functionGroupId, type)
            .orElseThrow(() -> {
                LOGGER.warn("Function Group with id {} does not exist in repository.", functionGroupId);
                return getBadRequestException(QueryErrorCodes.ERR_ACQ_003.getErrorMessage(),
                    QueryErrorCodes.ERR_ACQ_003.getErrorCode());
            });
    }

    private FunctionGroup getFunctionGroupFromServiceAgreementById(ServiceAgreement serviceAgreement,
        String functionGroupId) {
        return serviceAgreement.getFunctionGroups().stream()
            .filter(functionGroup -> functionGroup.getId().equals(functionGroupId)).findFirst()
            .orElseThrow(() -> {
                LOGGER.warn("Function Group with id {} does not exist in repository.", functionGroupId);
                return getBadRequestException(QueryErrorCodes.ERR_ACQ_003.getErrorMessage(),
                    QueryErrorCodes.ERR_ACQ_003.getErrorCode());
            });
    }

    private void validateIfUserIsExposedOnCustomServiceAgreement(String userId, ServiceAgreement serviceAgreement) {
        boolean userIsExposed = serviceAgreement.getParticipants().values()
            .stream()
            .filter(Participant::isShareUsers)
            .flatMap(provider -> provider.getParticipantUsers().stream())
            .anyMatch(providerUser -> providerUser.getUserId().equals(userId));
        if (!serviceAgreement.isMaster() && !userIsExposed) {
            LOGGER.warn("User with id {} is not exposed in service agreement", userId);
            throw getBadRequestException(QueryErrorCodes.ERR_ACQ_027.getErrorMessage(),
                QueryErrorCodes.ERR_ACQ_027.getErrorCode());
        }
    }

    private void validateFunctionGroupBelongsToServiceAgreement(String serviceAgreementId,
        FunctionGroup functionGroup) {
        if (!serviceAgreementId.equals(functionGroup.getServiceAgreement().getId())) {
            LOGGER.warn("Function group does not belong to service agreement with id {}", serviceAgreementId);
            throw getBadRequestException(QueryErrorCodes.ERR_ACQ_026.getErrorMessage(),
                QueryErrorCodes.ERR_ACQ_026.getErrorCode());
        }
    }

    private Optional<UserAssignedFunctionGroup> getUserAssignedFunctionGroups(String functionGroupId, String userId,
        String serviceAgreementIdFromBody) {
        return userAssignedFunctionGroupJpaRepository
            .findByUserIdAndServiceAgreementIdAndFunctionGroupId(userId, serviceAgreementIdFromBody, functionGroupId);
    }

    private void checkFunctionGroupAssignmentStatus(boolean assignmentStatus, QueryErrorCodes errorCode) {
        if (assignmentStatus) {
            LOGGER.warn("Function group is not assigned to user and can not be revoked");
            throw getBadRequestException(errorCode.getErrorMessage(), errorCode.getErrorCode());
        }
    }

    /**
     * Checks if there are any FG/DG assigned to any user provided under the service agreement. If so, BadRequest is
     * thrown.
     *
     * @param serviceAgreementId    service agreement id
     * @param allUserIdsToBeRemoved users that needs to be removed
     */
    public void checkIfUsersHaveAssignedPrivilegesForServiceAgreement(String serviceAgreementId,
        Set<String> allUserIdsToBeRemoved) {
        long numberOfAssignedFgDgsForUsersUnderServiceAgreement = userAssignedFunctionGroupJpaRepository
            .countAllByServiceAgreementIdAndUserIdInAndFunctionGroupType(serviceAgreementId, allUserIdsToBeRemoved,
                FunctionGroupType.DEFAULT);
        if (numberOfAssignedFgDgsForUsersUnderServiceAgreement > 0) {
            LOGGER.warn("Some users have assigned fg/dg and cannot be removed from service agreement with id {}",
                serviceAgreementId);
            throw getBadRequestException(ERR_ACC_041.getErrorMessage(), ERR_ACC_041.getErrorCode());
        }
    }

    /**
     * Retrieve all user ids by service agreement external id and function group type.
     *
     * @param externalServiceAgreementId service agreement external id
     * @param functionGroupType          function group type
     * @return list of {@link UserAssignedFunctionGroup}
     */
    public List<String> getAllUserIdsByServiceAgreementExternalIdAndFunctionGroupType(
        String externalServiceAgreementId, FunctionGroupType functionGroupType) {
        return userAssignedFunctionGroupJpaRepository
            .findAllUserIdsByServiceAgreementExternalIdAndFunctionGroupType(externalServiceAgreementId,
                functionGroupType);
    }

}
