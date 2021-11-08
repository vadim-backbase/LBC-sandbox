package com.backbase.legalentity.integration.external.mock.rest;

import com.backbase.buildingblocks.backend.security.accesscontrol.accesscontrol.AccessControlValidator;
import com.backbase.buildingblocks.backend.security.accesscontrol.accesscontrol.AccessResourceType;
import com.backbase.buildingblocks.backend.security.auth.config.FunctionalAccessControl;
import com.backbase.buildingblocks.presentation.errors.Error;
import com.backbase.buildingblocks.presentation.errors.ForbiddenException;
import com.google.common.collect.Lists;
import java.util.Collections;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping({"/client-api/v2/permissions", "/v2/permissions"})
@RestController
public class MockPermissionCheckClientController {

    private static final Logger LOGGER = LoggerFactory.getLogger(MockPermissionCheckClientController.class);

    private AccessControlValidator accessControlValidator;
    private FunctionalAccessControl functionalAccessControl;

    public MockPermissionCheckClientController(AccessControlValidator accessControlValidator,
        FunctionalAccessControl functionalAccessControl) {
        this.functionalAccessControl = functionalAccessControl;
        this.accessControlValidator = accessControlValidator;
    }

    @RequestMapping(
        value = {"/data-item"},
        method = {RequestMethod.GET},
        produces = {"application/json"})
    @ResponseStatus(HttpStatus.OK)
    public void checkAccess(@RequestParam(value = "businessFunction", required = true) String businessFunction,
        @RequestParam(value = "privilege", required = true) String privilege,
        @RequestParam(value = "dataType", required = true) String dataType,
        @RequestParam(value = "dataItemId", required = true) String dataItemId,
        HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {

        LOGGER.info("Permission check in mock for BF {}, Priv {}, data type {} and data item {}", businessFunction,
            privilege, dataType, dataItemId);

        boolean hasNoAccess = accessControlValidator
            .userHasNoAccessToDataItem(businessFunction, privilege, dataType, dataItemId);

        if (hasNoAccess) {
            LOGGER.info("No permission in mock for BF {}, Priv {}, data type {} and data item {}", businessFunction,
                privilege, dataType, dataItemId);
            throw new ForbiddenException()
                .withMessage("Forbidden")
                .withErrors(Collections.singletonList(new Error()
                    .withMessage("User has no access to data item")));
        }
    }

    /**
     * Checks if current user has no access to list of legal entities.
     *
     * @param legalEntityIds comma separated list of internal lega entity ids
     * @param accessResourceType one of the following values:NONE, USER, ACCOUNT, USER_OR_ACCOUNT, USER_AND_ACCOUNT
     */
    @RequestMapping(
        value = {"/access-resource"},
        method = {RequestMethod.GET},
        produces = {"application/json"})
    @ResponseStatus(HttpStatus.OK)
    public void userHasNoAccessToEntitlementResource(
        @RequestParam(value = "legalEntityIds", required = true) String legalEntityIds,
        @RequestParam(value = "accessResourceType", required = true) String accessResourceType,
        HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {

        LOGGER.info("Check access to resources");
        if (accessControlValidator.userHasNoAccessToEntitlementResource(
            Lists.newArrayList(legalEntityIds.split(",")),
            AccessResourceType.valueOf(accessResourceType))) {

            throw new ForbiddenException()
                .withMessage("Forbidden")
                .withErrors(Collections.singletonList(new Error()
                    .withMessage("User has no access to entitlement resource")));
        }
    }

    /**
     * Checks if current user has no access to service agreement.
     *
     * @param serviceAgreementId service agreement internal id
     * @param accessResourceType one of the following values:NONE, USER, ACCOUNT, USER_OR_ACCOUNT, USER_AND_ACCOUNT
     */
    @RequestMapping(
        value = {"/access-service-agreement"},
        method = {RequestMethod.GET},
        produces = {"application/json"})
    @ResponseStatus(HttpStatus.OK)
    public void userHasNoAccessToServiceAgreement(
        @RequestParam(value = "serviceAgreementId", required = true) String serviceAgreementId,
        @RequestParam(value = "accessResourceType", required = true) String accessResourceType,
        HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {

        LOGGER.info("Check access to resources");
        if (accessControlValidator.userHasNoAccessToServiceAgreement(
            serviceAgreementId,
            AccessResourceType.valueOf(accessResourceType))) {

            throw new ForbiddenException()
                .withMessage("Forbidden")
                .withErrors(Collections.singletonList(new Error()
                    .withMessage("User has no access to service agreement")));
        }
    }

    /**
     * Checks if current user has no access to service agreement.
     *
     * @param userName user name that is not in the context
     * @param function business function
     * @param resource resource
     * @param privileges list of comma separated privileges
     */
    @RequestMapping(
        value = {"/check-permissions"},
        method = {RequestMethod.GET},
        produces = {"application/json"})
    @ResponseStatus(HttpStatus.OK)
    public void checkPermissions(
        @RequestParam(value = "userName", required = true) String userName,
        @RequestParam(value = "function", required = true) String function,
        @RequestParam(value = "resource", required = true) String resource,
        @RequestParam(value = "priveges", required = true) String privileges,
        HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {

        LOGGER.info("Check access to resources");
        if (!functionalAccessControl.checkPermissions(userName, resource, function, privileges)) {

            throw new ForbiddenException()
                .withMessage("Forbidden")
                .withErrors(Collections.singletonList(new Error()
                    .withMessage("User has no access")));
        }
    }
}