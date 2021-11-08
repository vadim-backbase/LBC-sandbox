package com.backbase.accesscontrol.service.impl;

import static com.backbase.accesscontrol.domain.GraphConstants.SERVICE_AGREEMENT_WITH_PARTICIPANTS_AND_CREATOR;
import static com.backbase.accesscontrol.util.ExceptionUtil.getForbiddenException;
import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_017;
import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_047;
import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_079;
import static com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.users.AccessResourceType.ACCOUNT;
import static com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.users.AccessResourceType.USER;
import static com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.users.AccessResourceType.USER_AND_ACCOUNT;

import com.backbase.accesscontrol.domain.Participant;
import com.backbase.accesscontrol.domain.ServiceAgreement;
import com.backbase.accesscontrol.domain.ServiceAgreementState;
import com.backbase.accesscontrol.domain.UserPermissions;
import com.backbase.accesscontrol.repository.UserAssignedFunctionGroupJpaRepository;
import com.backbase.accesscontrol.service.BusinessFunctionCache;
import com.backbase.accesscontrol.service.ValidateLegalEntityHierarchyService;
import com.backbase.accesscontrol.service.accessresource.AccessResourceTypeFactory;
import com.backbase.accesscontrol.util.properties.MasterServiceAgreementFallbackProperties;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.users.AccessResourceType;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.users.ContextLegalEntities;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.users.EntitlementsResource;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.users.LegalEntityResource;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.users.ServiceAgreementResource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
@Service
@RequiredArgsConstructor
public class UserAccessPermissionCheckService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserAccessPermissionCheckService.class);

    private final UserAssignedFunctionGroupJpaRepository userAssignedFunctionGroupJpaRepository;
    private final PersistenceServiceAgreementService persistenceServiceAgreementService;
    private final PersistenceLegalEntityService persistenceLegalEntityService;
    private final BusinessFunctionCache businessFunctionCache;
    private final ValidateLegalEntityHierarchyService validateLegalEntityHierarchyService;
    private final MasterServiceAgreementFallbackProperties fallbackProperties;


    /**
     * Checks if the user has the required permissions.
     *
     * @param userId                   id of the user
     * @param serviceAgreementId       if of the service agreement
     * @param functionName             name of the business function
     * @param resourceName             name of the resource
     * @param commaSeparatedPrivileges comma separated privileges
     */
    public void checkUserPermission(String userId, String serviceAgreementId, String functionName, String resourceName,
        String commaSeparatedPrivileges) {

        LOGGER.info(
            "Checking permissions for user {}, serviceAgreement {}, "
                + "function {}, resource {}, and privileges {}",
            userId, serviceAgreementId, functionName, resourceName, commaSeparatedPrivileges);

        Set<String> privileges = new LinkedHashSet<>(Arrays.asList(commaSeparatedPrivileges.split(",")));
        Set<String> appFnPrivilegesIds = businessFunctionCache
            .getByFunctionNameOrResourceNameOrPrivilegesOptional(functionName, resourceName, privileges);

        long count = userAssignedFunctionGroupJpaRepository
            .findAfpIdsByUserIdAndServiceAgreementIdAndStateAndAfpIdsIn(
                userId,
                serviceAgreementId,
                ServiceAgreementState.ENABLED,
                appFnPrivilegesIds).stream()
            .map(id -> new UserPermissions(
                businessFunctionCache.getApplicableFunctionPrivilegeById(id).getBusinessFunctionName(),
                businessFunctionCache.getApplicableFunctionPrivilegeById(id).getPrivilegeName()))
            .distinct()
            .count();

        if (count != privileges.size()) {
            LOGGER.warn(
                "No permission for user id {}, service agreement id {} for function {}, resource {} and privileges {}",
                userId, serviceAgreementId, functionName, resourceName, commaSeparatedPrivileges);
            throw getForbiddenException(ERR_ACQ_017.getErrorMessage(), ERR_ACQ_017.getErrorCode());
        }
    }

    /**
     * Checks if certain logged in user that belong to a given legal entity has access to the given legal entity
     * resources and throws exception if it does not.
     *
     * @param entitlementsResource - payload containing user's legal entity, service agreement from context and a list
     *                             of legal entity resources to be checked.
     */
    public ContextLegalEntities checkUserAccessToEntitlementsResources(EntitlementsResource entitlementsResource) {
        LOGGER.info("Checking if user has access to resources: {}", entitlementsResource.getLegalEntityIds());
        List<String> resourcesThatUserHasAccessTo = getResourcesThatUserHasAccessTo(
            entitlementsResource.getContextServiceAgreementId(),
            entitlementsResource.getUserLegalEntityId(),
            entitlementsResource.getAccessResourceType(),
            entitlementsResource.getLegalEntityIds());

        return new ContextLegalEntities()
            .withLegalEntities(resourcesThatUserHasAccessTo);
    }

    /**
     * Checks if certain logged in user that belong to a given legal entity has access to the given service agreement
     * and throws exception if it does not.
     *
     * @param serviceAgreementResource - payload containing user's legal entity, service agreement from context and
     *                                 service agreement id to be checked
     */
    public void checkUserAccessToServiceAgreement(ServiceAgreementResource serviceAgreementResource) {
        LOGGER.info("Checking if user has access to service agreement {}",
            serviceAgreementResource.getServiceAgreementId());
        ServiceAgreement serviceAgreement = persistenceServiceAgreementService
            .getById(serviceAgreementResource.getServiceAgreementId(),
                SERVICE_AGREEMENT_WITH_PARTICIPANTS_AND_CREATOR);

        if (!userHasAccessOnServiceAgreement(serviceAgreementResource, serviceAgreement)) {
            LOGGER.warn("User does not have access to service agreement with id {}",
                serviceAgreementResource.getServiceAgreementId());
            throw getForbiddenException(ERR_ACQ_047.getErrorMessage(), ERR_ACQ_047.getErrorCode());
        }
    }

    /**
     * Retrieves ids of the legal entities that logged in user has access to.
     *
     * @param legalEntityResource - payload containing user's legal entity and service agreement from context
     */
    public ContextLegalEntities getLegalEntitiesThatUserHasAccessTo(LegalEntityResource legalEntityResource) {

        List<String> legalEntityIds = Optional.ofNullable(legalEntityResource.getContextServiceAgreementId())
            .map(serviceAgreementId -> persistenceServiceAgreementService
                .getById(serviceAgreementId,
                    SERVICE_AGREEMENT_WITH_PARTICIPANTS_AND_CREATOR))
            .filter(serviceAgreement -> !serviceAgreement.isMaster())
            .map(serviceAgreement -> getAccessResourceType(legalEntityResource.getAccessResourceType())
                .getValidResources(getServiceAgreementParticipants(serviceAgreement),
                    legalEntityResource.getUserLegalEntityId()))
            .orElseGet(() -> persistenceLegalEntityService
                .getListOfAllSubEntityIds(legalEntityResource.getUserLegalEntityId()));

        return new ContextLegalEntities().withLegalEntities(legalEntityIds);
    }

    private boolean userHasAccessToEntitlementsResource(String contextServiceAgreement, String userLegalEntity,
        AccessResourceType accessResourceType, List<String> resourcesToCheck) {
        List<String> validResources = getValidResourcesForUsersLoggedUnderLegalEntity(contextServiceAgreement,
            userLegalEntity, accessResourceType, resourcesToCheck);
        return validResources.containsAll(resourcesToCheck);
    }

    private List<String> getResourcesThatUserHasAccessTo(String contextServiceAgreement, String userLegalEntity,
        AccessResourceType accessResourceType, List<String> resourcesToCheck) {
        List<String> validResources = getValidResourcesForUsersLoggedUnderLegalEntity(contextServiceAgreement,
            userLegalEntity, accessResourceType, resourcesToCheck);
        validResources.retainAll(resourcesToCheck);
        return validResources;
    }

    private boolean userHasAccessOnServiceAgreement(ServiceAgreementResource data, ServiceAgreement serviceAgreement) {
        boolean hasAccess;
        if (data.getContextServiceAgreementId() != null && !serviceAgreement.isMaster() && data
            .getContextServiceAgreementId().equals(serviceAgreement.getId())) {
            List<String> validResources = getAccessResourceType(data.getAccessResourceType()).getValidResources(
                getServiceAgreementParticipants(serviceAgreement), data.getUserLegalEntityId());

            hasAccess = validResources.contains(data.getUserLegalEntityId());
        } else {
            hasAccess = userHasAccessToEntitlementsResource(data.getContextServiceAgreementId(),
                data.getUserLegalEntityId(), AccessResourceType.NONE,
                Collections.singletonList(serviceAgreement.getCreatorLegalEntity().getId()));
        }
        return hasAccess;
    }


    private AccessResourceTypeFactory getAccessResourceType(AccessResourceType accessResourceType) {
        return AccessResourceTypeFactory.valueOf(accessResourceType.toString());
    }

    private List<Participant> getServiceAgreementParticipants(ServiceAgreement serviceAgreement) {
        return new ArrayList<>(serviceAgreement.getParticipants().values());
    }

    private List<String> getValidResourcesForUsersLoggedUnderLegalEntity(String contextServiceAgreement,
        String userLegalEntity, AccessResourceType accessResourceType, List<String> resourcesToCheck) {

//        we make an assumption that null context service agreement means master service agreement here.
        if (contextServiceAgreement == null) {
            if (!fallbackProperties.isEnabled()) {
                throw getForbiddenException(ERR_ACQ_079.getErrorMessage(), ERR_ACQ_079.getErrorCode());
            }
            return validateLegalEntityHierarchyService.getLegalEntityHierarchy(userLegalEntity, resourcesToCheck);
        }

        ServiceAgreement serviceAgreement = persistenceServiceAgreementService
            .getById(contextServiceAgreement, SERVICE_AGREEMENT_WITH_PARTICIPANTS_AND_CREATOR);

        if (serviceAgreement.isMaster()) {
            return validateLegalEntityHierarchyService.getLegalEntityHierarchy(userLegalEntity, resourcesToCheck);
        }

        AccessResourceType resourceType = accessResourceType;

        if (accessResourceType == ACCOUNT || accessResourceType == USER) {

            Predicate<Participant> participantPredicate = resourceType == ACCOUNT
                ? Participant::isShareAccounts
                : Participant::isShareUsers;

            if (resourcesAreSharedInServiceAgreement(resourcesToCheck, serviceAgreement, participantPredicate)) {
                resourceType = USER_AND_ACCOUNT;
            }
        }

        AccessResourceTypeFactory resourceTypeFactory = getAccessResourceType(resourceType);

        return resourceTypeFactory.getValidResources(getServiceAgreementParticipants(serviceAgreement), userLegalEntity);
    }

    private boolean resourcesAreSharedInServiceAgreement(List<String> resourcesToCheck,
        ServiceAgreement serviceAgreement, Predicate<Participant> participantPredicate) {

        List<String> legalEntityIds = getParticipantsLegalEntityIds(serviceAgreement, participantPredicate);

        return legalEntityIds.containsAll(resourcesToCheck);
    }

    private List<String> getParticipantsLegalEntityIds(ServiceAgreement serviceAgreement,
        Predicate<Participant> participantPredicate) {
        return serviceAgreement.getParticipants().values()
            .stream()
            .filter(participantPredicate)
            .map(p -> p.getLegalEntity().getId())
            .collect(Collectors.toList());
    }
}
