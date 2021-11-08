package com.backbase.accesscontrol.business.flows.permissionset;

import com.backbase.accesscontrol.business.flows.AbstractFlow;
import com.backbase.accesscontrol.business.service.PermissionSetPersistenceService;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.aps.PresentationPermissionSetItemPut;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@Component
public class UpdatePermissionSetFlow extends AbstractFlow<PresentationPermissionSetItemPut, Void> {

    private static final Logger LOGGER = LoggerFactory.getLogger(UpdatePermissionSetFlow.class);

    private PermissionSetPersistenceService permissionSetPersistenceService;

    /**
     * {@inheritDoc}
     */
    @Override
    protected Void execute(PresentationPermissionSetItemPut presentationPermissionSetPut) {
        LOGGER.info("Execute update permission set flow service with data {}", presentationPermissionSetPut);
        permissionSetPersistenceService.updatePermissionSet(presentationPermissionSetPut);
        return null;
    }
}
