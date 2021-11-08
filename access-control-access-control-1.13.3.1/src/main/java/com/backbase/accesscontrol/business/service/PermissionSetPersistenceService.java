package com.backbase.accesscontrol.business.service;

import com.backbase.accesscontrol.business.persistence.aps.CreatePermissionSetHandler;
import com.backbase.accesscontrol.business.persistence.aps.DeletePermissionSetHandler;
import com.backbase.accesscontrol.business.persistence.aps.UpdatePermissionSetHandler;
import com.backbase.accesscontrol.dto.IdentifierPair;
import com.backbase.accesscontrol.dto.parameterholder.EmptyParameterHolder;
import com.backbase.accesscontrol.util.PermissionSetValidationUtil;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.aps.PresentationPermissionSet;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.aps.PresentationPermissionSetItemPut;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.response.PresentationInternalIdResponse;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * A service class that communicates with the P&P services via the clients and returns responses.
 */
@Service
@AllArgsConstructor
public class PermissionSetPersistenceService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PermissionSetPersistenceService.class);

    private PermissionSetValidationUtil permissionSetValidationUtil;
    private CreatePermissionSetHandler createPermissionSetHandler;
    private DeletePermissionSetHandler deletePermissionSetHandler;
    private UpdatePermissionSetHandler updatePermissionSetHandler;


    /**
     * Calls persistence for creating permission set.
     *
     * @param presentationPermissionSet permission set to be created
     * @return {@link PresentationInternalIdResponse} which contains the id of the created permission set
     */
    public PresentationInternalIdResponse createPermissionSet(PresentationPermissionSet presentationPermissionSet) {

        LOGGER.info("Call persistence with data {}", createPermissionSetHandler);
        BigDecimal id = createPermissionSetHandler.handleRequest(new EmptyParameterHolder(), presentationPermissionSet);

        return new PresentationInternalIdResponse().withId(id);
    }

    /**
     * Calls persistence client.
     *
     * @param identifierType type of the identifier, name or id
     * @param identifier     identifier value
     */
    public void deletePermissionSet(String identifierType, String identifier) {
        LOGGER.info("Calls delete assignable permission set on persistence client with {} by {}",
            identifier, identifierType);

        deletePermissionSetHandler.handleRequest(new IdentifierPair(identifierType, identifier), null);

    }

    /**
     * Calls persistence for updating permission set.
     *
     * @param presentationPermissionSet permission set to be updated
     */
    public void updatePermissionSet(PresentationPermissionSetItemPut presentationPermissionSet) {
        LOGGER.info("Calls update assignable permission set on persistence client with data {} ",
            presentationPermissionSet);

        permissionSetValidationUtil.validateUserApsIdentifiers(presentationPermissionSet.getAdminUserAps(),
            presentationPermissionSet.getRegularUserAps());

        updatePermissionSetHandler.handleRequest(new EmptyParameterHolder(), presentationPermissionSet);

    }

}
