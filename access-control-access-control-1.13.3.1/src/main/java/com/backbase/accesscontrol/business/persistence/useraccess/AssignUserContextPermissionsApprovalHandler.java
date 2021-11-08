package com.backbase.accesscontrol.business.persistence.useraccess;

import com.backbase.accesscontrol.business.persistence.LeanGenericEventEmitter;
import com.backbase.accesscontrol.domain.dto.PersistenceUserContextPermissionsApproval;
import com.backbase.accesscontrol.dto.parameterholder.UserPermissionsApprovalParameterHolder;
import com.backbase.accesscontrol.service.PermissionService;
import com.backbase.buildingblocks.backend.communication.event.proxy.EventBus;
import com.backbase.buildingblocks.persistence.model.Event;
import org.springframework.stereotype.Component;


@Component
public class AssignUserContextPermissionsApprovalHandler extends
    LeanGenericEventEmitter<UserPermissionsApprovalParameterHolder,
                PersistenceUserContextPermissionsApproval, Void> {

    private PermissionService permissionService;

    public AssignUserContextPermissionsApprovalHandler(
        EventBus eventBus, PermissionService permissionService) {
        super(eventBus);
        this.permissionService = permissionService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Void executeRequest(UserPermissionsApprovalParameterHolder parameterHolder,
        PersistenceUserContextPermissionsApproval requestData) {
        permissionService.assignUserContextPermissionsApproval(parameterHolder.getServiceAgreementId(),
            parameterHolder.getUserId(), parameterHolder.getLegalEntityId(),
            parameterHolder.getApprovalId(),
            requestData.getPermissions());

        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Event createSuccessEvent(UserPermissionsApprovalParameterHolder parameterHolder,
        PersistenceUserContextPermissionsApproval request, Void response) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Event createFailureEvent(UserPermissionsApprovalParameterHolder parameterHolder,
        PersistenceUserContextPermissionsApproval request, Exception failure) {
        return null;
    }
}
