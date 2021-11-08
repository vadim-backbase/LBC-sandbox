package com.backbase.accesscontrol.auth;

import com.backbase.accesscontrol.business.service.UserManagementService;
import com.backbase.accesscontrol.dto.UserContextDetailsDto;
import com.backbase.accesscontrol.service.impl.UserAccessPermissionCheckService;
import com.backbase.accesscontrol.service.impl.UserAccessPrivilegeService;
import com.backbase.buildingblocks.backend.security.auth.config.FunctionalAccessControl;
import com.backbase.buildingblocks.backend.security.auth.config.SecurityContextUtil;
import com.backbase.buildingblocks.jwt.internal.token.InternalJwtClaimsSet;
import com.backbase.buildingblocks.presentation.errors.ForbiddenException;
import com.backbase.buildingblocks.presentation.errors.NotFoundException;
import com.backbase.dbs.user.api.client.v2.model.GetUser;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.users.ContextLegalEntities;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.users.EntitlementsResource;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.users.PersistenceUserDataItemPermission;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.users.ServiceAgreementResource;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/**
 * Implementation class which holds the Access Control PnP client and is sending the request for checking permissions.
 */
@Primary
@Component
@AllArgsConstructor
public class AccessControlValidatorImpl implements FunctionalAccessControl, AccessControlValidator {

    private static final Logger LOGGER = LoggerFactory.getLogger(AccessControlValidatorImpl.class);
    private static final String USER_IS_NOT_AUTHENTICATED = "User is not authenticated.";

    private UserManagementService userManagementService;
    private ServiceAgreementIdProvider serviceAgreementIdProvider;
    private SecurityContextUtil securityContextUtil;
    private UserAccessPrivilegeService userAccessPrivilegeService;
    private UserAccessPermissionCheckService userAccessPermissionCheckService;

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean checkPermissions(String username, String resource, String function, String privileges) {
        LOGGER.info(
            "Trying to check user permission for username {}, resource {}, function {} and privileges {}",
            username, resource, function, privileges);
        Optional<String> serviceAgreementFromContext = serviceAgreementIdProvider.getServiceAgreementId();
        return serviceAgreementFromContext
            .map(serviceAgreementId -> doPermissionCheck(username, resource, function, privileges, serviceAgreementId))
            .orElseGet(() -> {
                String masterServiceAgreementId =
                    serviceAgreementIdProvider
                        .getMasterServiceAgreementIdIfServiceAgreementNotPresentInContext(username);
                LOGGER.info("No service agreement ID found in context. Resuming with Master Service Agreement {}",
                    masterServiceAgreementId);
                return doPermissionCheck(username, resource, function, privileges, masterServiceAgreementId);
            });
    }

    /**
     * Does permission check and handles exceptions.
     */
    private boolean doPermissionCheck(String username, String resource, String function, String privileges,
        String serviceAgreementId) {
        LOGGER.info(
            "Checking user permission for username {}, serviceAgreementId {}, resource {}, "
                + "function {} and privileges {}",
            username, serviceAgreementId, resource, function, privileges);
        boolean isEntitled;

        try {
            String userId = getUserContextDetails().getInternalUserId();
            userAccessPermissionCheckService.checkUserPermission(
                userId,
                serviceAgreementId,
                function,
                resource,
                privileges);
            isEntitled = true;
        } catch (ForbiddenException | NotFoundException e) {
            LOGGER.warn("User doesn't exists or is forbidden to perform function.", e);
            isEntitled = false;
        }
        return isEntitled;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean userHasNoAccessToEntitlementResource(String resourceLegalEntityId,
        AccessResourceType accessResourceType) {
        return userHasNoAccessToEntitlementResource(Collections.singletonList(resourceLegalEntityId),
            accessResourceType);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean userHasNoAccessToEntitlementResource(List<String> legalEntities,
        AccessResourceType accessResourceType) {
        return getAuthenticatedUserName()
            .map(
                userName -> checkUserAccessToEntitlementsResource(userName,
                    legalEntities, accessResourceType))
            .orElse(false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean userHasNoAccessToDataItem(String businessFunction, String privilege, String dataType,
        String itemId) {

        String authenticatedUserName = getAuthenticatedUserName().orElseThrow(() ->
            new ForbiddenException()
                .withMessage(USER_IS_NOT_AUTHENTICATED));

        String userId = getUserContextDetails().getInternalUserId();
        String serviceAgreementFromContext = serviceAgreementIdProvider.getServiceAgreementId()
            .orElse(serviceAgreementIdProvider
                .getMasterServiceAgreementIdIfServiceAgreementNotPresentInContext(authenticatedUserName));

        List<PersistenceUserDataItemPermission> userDataItemsPrivileges = userAccessPrivilegeService
            .getUserDataItemsPrivileges(userId, serviceAgreementFromContext, null, businessFunction, privilege,
                dataType, itemId);

        return !(userDataItemsPrivileges.size() == 1 && userDataItemsPrivileges.get(0).getDataItem().getId()
            .equals(itemId));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean userHasNoAccessToServiceAgreement(String serviceAgreementId, AccessResourceType accessResourceType) {
        try {
            return getAuthenticatedUserName()
                .map(username -> {
                    validateUserAccessToServiceAgreement(username, serviceAgreementId, accessResourceType);
                    return false;
                })
                .orElse(false);
        } catch (ForbiddenException | NotFoundException ex) {
            return true;
        }
    }

    private void validateUserAccessToServiceAgreement(String username, String serviceAgreementId,
        AccessResourceType accessResourceType) {
        String serviceAgreementFromContext = serviceAgreementIdProvider.getServiceAgreementId()
            .orElseGet(()->
                serviceAgreementIdProvider.getMasterServiceAgreementIdIfServiceAgreementNotPresentInContext(username));
        String userLegalEntity = getUserContextDetails().getLegalEntityId();

        userAccessPermissionCheckService.checkUserAccessToServiceAgreement(new ServiceAgreementResource()
            .withServiceAgreementId(serviceAgreementId)
            .withAccessResourceType(accessResourceType.getType())
            .withContextServiceAgreementId(serviceAgreementFromContext)
            .withUserLegalEntityId(userLegalEntity));

    }

    /**
     * Validates if user has access to legal entities.
     *
     * @param legalEntities      legal entity ids to be checked
     * @param accessResourceType type of the resource that needs to be accessed. example {@link
     *                           AccessResourceType#USER}
     */
    private boolean checkUserAccessToEntitlementsResource(String userName, List<String> legalEntities,
        AccessResourceType accessResourceType) {
        String userLegalEntityId = getUserContextDetails().getLegalEntityId();
        String contextServiceAgreementId = serviceAgreementIdProvider.getServiceAgreementId()
            .orElseGet(() -> serviceAgreementIdProvider
                .getMasterServiceAgreementIdIfServiceAgreementNotPresentInContext(userName));

        ContextLegalEntities contextLegalEntities = userAccessPermissionCheckService
            .checkUserAccessToEntitlementsResources(new EntitlementsResource()
                .withUserLegalEntityId(userLegalEntityId)
                .withContextServiceAgreementId(contextServiceAgreementId)
                .withLegalEntityIds(legalEntities)
                .withAccessResourceType(accessResourceType.getType()));

        return !Objects.requireNonNull(contextLegalEntities).getLegalEntities().containsAll(legalEntities);
    }

    private UserContextDetailsDto getUserContextDetails() {
        Optional<String> internalId = securityContextUtil.getInternalId();
        Optional<String> leid = securityContextUtil.getUserTokenClaim("leid", String.class);
        if (internalId.isPresent() && leid.isPresent()) {
            return new UserContextDetailsDto(internalId.get(), leid.get());
        } else {
            return getUserDetailsFromPersistence();
        }
    }

    private UserContextDetailsDto getUserDetailsFromPersistence() {
        String authenticatedUserName = getAuthenticatedUserName().orElseThrow(() ->
            new ForbiddenException()
                .withMessage(USER_IS_NOT_AUTHENTICATED));
        UserContextDetailsDto userContextDetailsDto;
        try {
            GetUser responseBody = userManagementService
                .getUserByExternalId(authenticatedUserName);
            userContextDetailsDto = new UserContextDetailsDto(responseBody.getId(),
                responseBody.getLegalEntityId());
        } catch (NotFoundException e) {
            throw new ForbiddenException()
                .withMessage(USER_IS_NOT_AUTHENTICATED);
        }
        return userContextDetailsDto;

    }

    /**
     * Returns the name of the currently logged in user.
     *
     * @return name of the currently logged in user
     */
    private Optional<String> getAuthenticatedUserName() {
        return securityContextUtil.getUserTokenClaim(InternalJwtClaimsSet.SUBJECT_CLAIM, String.class);
    }

}
