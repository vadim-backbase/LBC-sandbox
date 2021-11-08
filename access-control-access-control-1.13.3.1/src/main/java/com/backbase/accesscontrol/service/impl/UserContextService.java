package com.backbase.accesscontrol.service.impl;

import static com.backbase.accesscontrol.util.ExceptionUtil.getBadRequestException;
import static com.backbase.accesscontrol.util.ExceptionUtil.getForbiddenException;
import static com.backbase.accesscontrol.util.ExceptionUtil.getNotFoundException;
import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_112;
import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_114;
import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_003;

import com.backbase.accesscontrol.configuration.CombinationConfig;
import com.backbase.accesscontrol.domain.ServiceAgreement;
import com.backbase.accesscontrol.domain.UserContext;
import com.backbase.accesscontrol.domain.dto.CheckDataItemsPermissions;
import com.backbase.accesscontrol.domain.dto.UserContextProjection;
import com.backbase.accesscontrol.repository.ServiceAgreementJpaRepository;
import com.backbase.accesscontrol.repository.UserContextJpaRepository;
import com.backbase.accesscontrol.service.BusinessFunctionCache;
import com.backbase.accesscontrol.service.FunctionGroupService;
import com.backbase.accesscontrol.service.rest.spec.model.DataItemsPermissions;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.usercontext.Element;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.usercontext.UserContextsGetResponseBody;
import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@Service
@AllArgsConstructor
public class UserContextService {

    private UserContextJpaRepository userContextJpaRepository;
    private ServiceAgreementJpaRepository serviceAgreementJpaRepository;
    private BusinessFunctionCache businessFunctionCache;
    private CombinationConfig combinationConfig;
    private FunctionGroupService functionGroupService;

    /**
     * Creates a new user with id userId and saves it to the database if it does not exist.
     *
     * @param userId             id of the user;
     * @param serviceAgreementId id of the service agreement
     * @return {@link UserContext} containing the id, userId and serviceAgreementId.
     */
    @Transactional
    public UserContext getOrCreateUserContext(String userId, String serviceAgreementId) {
        Optional<UserContext> userIdAndServiceAgreementId = userContextJpaRepository
            .findByUserIdAndServiceAgreementId(userId, serviceAgreementId);
        return userIdAndServiceAgreementId
            .orElseGet(() -> {
                log.debug("Adding user access for user {} and service agreement {}", userId, serviceAgreementId);
                UserContext userContext = new UserContext(userId, serviceAgreementId);
                return userContextJpaRepository.save(userContext);
            });
    }

    /**
     * Returns {@link UserContext} for user under specified service agreement.
     *  @param userId Id of the user for checking permission.
     * @param serviceAgreementId Under which service agreement permissions are checked.
     */
    public Optional<UserContext> getUserContextByUserIdAndServiceAgreementIdWithFunctionAndDataGroupIds(String userId,
        String serviceAgreementId) {

        log.debug("Getting UserAccess for user {} in service agreement {}.", userId, serviceAgreementId);
        return userContextJpaRepository
            .findByUserIdAndServiceAgreementIdWithFunctionDataGroupIdsAndSelfApprovalPolicies(userId, serviceAgreementId);
    }

    /**
     * Delete User Context record by Id.
     *
     * @param userContextId
     */
    public void delete(Long userContextId) {
        userContextJpaRepository
            .deleteById(userContextId);
    }

    /**
     * Retrieves User Context by id and transforms it to {@link  UserContextsGetResponseBody}.
     *
     * @param userId - id of the user provider
     * @param query  The search term used to search
     * @param from   - Page Number.
     * @param size   - Limit the number of elements on the response
     * @return {@link UserContextsGetResponseBody}
     */
    @Transactional(readOnly = true)
    public UserContextsGetResponseBody getUserContextsByUserId(String userId, String query, Integer from,
        Integer size) {
        log.debug(
            "Retrieving user contexts by provider user id {}, query {} and pagination parameters "
                + "from {} and size {}", userId, query, from, size);
        Page<ServiceAgreement> serviceAgreements = serviceAgreementJpaRepository
            .findServiceAgreementsWhereUserHasPermissions(userId, query == null ? "" : query,
                PageRequest.of(from, size));
        List<Element> elementList = serviceAgreements
            .map(this::getElementResponseBody).getContent();
        UserContextsGetResponseBody userContextsGetResponseBody = new UserContextsGetResponseBody();
        userContextsGetResponseBody.setElements(elementList);
        userContextsGetResponseBody.setTotalElements(serviceAgreements.getTotalElements());
        return userContextsGetResponseBody;
    }

    /**
     * Validates if user context is valid or invalid.
     *
     * @param userId             - User id
     * @param serviceAgreementId - Service agreement id
     */
    public void validateUserContext(String userId, String serviceAgreementId) {
        log.debug("Validating user contexts by user id {}, serviceAgreementId {} parameters ", userId,
            serviceAgreementId);

        if (!serviceAgreementJpaRepository.existContextForUserIdAndServiceAgreementId(userId, serviceAgreementId)) {
            log.warn("Invalid user context for user id {}.", userId);
            throw getForbiddenException("Invalid user context for user id " + userId, null);
        }
    }


    @Transactional
    public Page<String> findUserIdsByServiceAgreementIdAndFunctionGroupId(String serviceAgreementId,
        String functionGroupId, Integer from, Integer size) {

        checkIfFunctionGroupExists(serviceAgreementId, functionGroupId);

        return userContextJpaRepository.findUserIdsByServiceAgreementIdAndFunctionGroupId(serviceAgreementId,
            functionGroupId,
            PageRequest.of(from, size)
        );
    }

    private void checkIfFunctionGroupExists(String serviceAgreementId, String functionGroupId) {
        boolean functionGroupExists = functionGroupService.getFunctionGroupsByServiceAgreementId(serviceAgreementId)
            .stream()
            .anyMatch(functionGroup -> functionGroup.getId().equals(functionGroupId));

        if (!functionGroupExists) {
            log.warn("FunctionGroup {} does not exist in ServiceAgreement {}", functionGroupId, serviceAgreementId);
            throw getNotFoundException(ERR_ACQ_003.getErrorMessage(), ERR_ACQ_003.getErrorCode());
        }
    }

    private Element getElementResponseBody(ServiceAgreement serviceAgreement) {
        Element element = new Element();
        element.setServiceAgreementName(serviceAgreement.getName());
        element.setServiceAgreementId(serviceAgreement.getId());
        element.setServiceAgreementMaster(serviceAgreement.isMaster());
        element.setExternalId(serviceAgreement.getExternalId());
        element.setDescription(serviceAgreement.getDescription());
        return element;
    }

    @Transactional(readOnly = true)
    public void checkDataItemsPermissions(Set<String> uniqueTypes,
        DataItemsPermissions dataItemsPermissions, String internalUserId, String serviceAgreementId) {
        Set<String> appFnPrivilegeIds = businessFunctionCache
            .getByFunctionNameOrResourceNameOrPrivilegesOptional(
                dataItemsPermissions.getFunctionName(), null,
                Collections.singleton(dataItemsPermissions.getPrivilege()));
        if (appFnPrivilegeIds.isEmpty()) {
            throw getBadRequestException((ERR_AG_114.getErrorMessage()), ERR_AG_114.getErrorCode());
        }
        Set<String> notOptionalWhenRequested = combinationConfig.getNotOptionalWhenRequested();
        log.debug("Checking data items permissions");
        List<CheckDataItemsPermissions> resultList =
            userContextJpaRepository.findDataItemsPermissions(dataItemsPermissions, appFnPrivilegeIds, internalUserId,
                serviceAgreementId);
        if (resultList.isEmpty()) {
            if (!Sets.intersection(uniqueTypes, notOptionalWhenRequested).isEmpty() || !userContextJpaRepository
                .checkIfPermissionIsAssignedWithoutDataGroups(appFnPrivilegeIds, internalUserId,
                    serviceAgreementId)) {
                log.warn("User has no permissions, empty list.");
                throw getForbiddenException(ERR_AG_112.getErrorMessage(), ERR_AG_112.getErrorCode());
            }
        } else {
            resultList.forEach(i -> {
                if (!i.getCountTypesInCombination().equals(i.getCheckSumTypesAndItems())) {
                    log.warn("User has no permissions, not all items are in combination.");
                    throw getForbiddenException(ERR_AG_112.getErrorMessage(), ERR_AG_112.getErrorCode());
                }
                if (!Collections.disjoint(uniqueTypes, notOptionalWhenRequested)
                    && i.getCountTypesInCombination() < uniqueTypes.size() && !userContextJpaRepository
                    .checkIfPredefinedTypesAreInCombination(i.getCombinationId(), notOptionalWhenRequested)) {
                    log.warn("Not optional when requested types are not in a combination.");
                    throw getForbiddenException(ERR_AG_112.getErrorMessage(), ERR_AG_112.getErrorCode());
                }
            });
        }
    }

    /**
     * Get user context records list which assign to data group with group id.
     *
     * @param dataGroupId             - DataGroup id
     * @return list of user context projection (only userId and serviceAgreementId).
     */
    @Transactional
    public List<UserContextProjection> getUserContextListByDataGroupId(String dataGroupId) {
        log.debug("Retrieving user context list by group id {} ", dataGroupId);
        return userContextJpaRepository.findAllUserContextsByAssignDataGroupId(dataGroupId);
    }

    /**
     * Get user context records list which assign to function group with function group id.
     *
     * @param functionGroupId             - FunctionGroup id
     * @return list of user context projection (only userId and serviceAgreementId).
     */
    @Transactional
    public List<UserContextProjection> getUserContextListByFunctionGroupId(String functionGroupId) {
        log.debug("Retrieving user context list by group id {} ", functionGroupId);
        return userContextJpaRepository.findAllUserContextsByAssignFunctionGroupId(functionGroupId);
    }
}
