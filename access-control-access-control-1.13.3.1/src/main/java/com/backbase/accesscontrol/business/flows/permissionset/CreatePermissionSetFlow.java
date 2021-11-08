package com.backbase.accesscontrol.business.flows.permissionset;

import com.backbase.accesscontrol.business.flows.AbstractFlow;
import com.backbase.accesscontrol.business.service.PermissionSetPersistenceService;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.aps.PresentationPermissionSet;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.response.PresentationInternalIdResponse;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@Component
public class CreatePermissionSetFlow extends AbstractFlow<PresentationPermissionSet, PresentationInternalIdResponse> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CreatePermissionSetFlow.class);

    private PermissionSetPersistenceService permissionSetPersistenceService;

    /**
     * {@inheritDoc}
     */
    @Override
    protected PresentationInternalIdResponse execute(PresentationPermissionSet presentationPermissionSet) {
        LOGGER.info("Execute create permission set flow service with data {}", presentationPermissionSet);
        return permissionSetPersistenceService.createPermissionSet(presentationPermissionSet);
    }
}
