package com.backbase.accesscontrol.business.flows.permissionset;

import com.backbase.accesscontrol.business.flows.AbstractFlow;
import com.backbase.accesscontrol.business.service.PermissionSetPersistenceService;
import com.backbase.accesscontrol.dto.DeletePermissionSetParameters;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class DeletePermissionSetFlow extends AbstractFlow<DeletePermissionSetParameters, Void> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeletePermissionSetFlow.class);

    private PermissionSetPersistenceService persistenceService;

    /**
     * {@inheritDoc}
     */
    @Override
    protected Void execute(DeletePermissionSetParameters data) {
        LOGGER.info("Trying to delete assignable permission set {} by {}",
            data.getIdentifier(), data.getIdentifierType());

        persistenceService.deletePermissionSet(data.getIdentifierType(), data.getIdentifier());
        return null;
    }
}
