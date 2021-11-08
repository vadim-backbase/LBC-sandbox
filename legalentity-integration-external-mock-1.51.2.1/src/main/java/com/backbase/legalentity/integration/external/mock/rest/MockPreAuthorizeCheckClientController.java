package com.backbase.legalentity.integration.external.mock.rest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping({"/client-api/v2/authorize", "/v2/authorize"})
@RestController
public class MockPreAuthorizeCheckClientController {

    private static final Logger LOGGER = LoggerFactory.getLogger(MockPreAuthorizeCheckClientController.class);
    private static final String RESOURCE_NAME = "Service Agreement";
    private static final String FUNCTION_ASSIGN_PERMISSIONS = "Manage Service Agreements";
    private static final String PRIVILEGE_NAME = "view,edit";

    /**
     * Permission check for resource: Service Agreement, business function: Manage Service Agreements and privileges: view,edit
     */
    @GetMapping(
        value = {"/permission-check"},
        produces = {"application/json"})
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("checkPermission('" + RESOURCE_NAME + "', "
        + "'" + FUNCTION_ASSIGN_PERMISSIONS + "', "
        + "{'" + PRIVILEGE_NAME + "'})")
    public void permissionCheck(
        HttpServletRequest httpServletRequest,
        HttpServletResponse httpServletResponse) {
        LOGGER.info("Successful check permissions");
    }

}
