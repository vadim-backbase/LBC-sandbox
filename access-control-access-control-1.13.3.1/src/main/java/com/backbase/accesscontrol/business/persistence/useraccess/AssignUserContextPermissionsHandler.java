package com.backbase.accesscontrol.business.persistence.useraccess;

import com.backbase.accesscontrol.business.persistence.LeanGenericEventEmitter;
import com.backbase.accesscontrol.domain.dto.PersistentUserContextPermissionsPutRequestBody;
import com.backbase.accesscontrol.dto.parameterholder.UserIdServiceAgreementIdParameterHolder;
import com.backbase.accesscontrol.service.PermissionService;
import com.backbase.buildingblocks.backend.communication.event.proxy.EventBus;
import com.backbase.buildingblocks.persistence.model.Event;
import com.backbase.pandp.accesscontrol.event.spec.v1.UserContextEvent;
import org.springframework.stereotype.Component;


@Component
public class AssignUserContextPermissionsHandler extends
    LeanGenericEventEmitter<UserIdServiceAgreementIdParameterHolder,
                PersistentUserContextPermissionsPutRequestBody, Void> {

    private PermissionService permissionService;

    public AssignUserContextPermissionsHandler(
        EventBus eventBus,
        PermissionService permissionService) {
        super(eventBus);
        this.permissionService = permissionService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Void executeRequest(UserIdServiceAgreementIdParameterHolder parameterHolder,
        PersistentUserContextPermissionsPutRequestBody requestData) {
        permissionService.assignUserContextPermissions(parameterHolder.getServiceAgreementId(),
            parameterHolder.getUserId(), requestData.getUserLegalEntityId(),
            requestData.getPermissions());
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected UserContextEvent createSuccessEvent(UserIdServiceAgreementIdParameterHolder parameterHolder,
        PersistentUserContextPermissionsPutRequestBody request, Void response) {
        return new UserContextEvent()
            .withUserId(parameterHolder.getUserId())
            .withServiceAgreementId(parameterHolder.getServiceAgreementId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Event createFailureEvent(UserIdServiceAgreementIdParameterHolder parameterHolder,
        PersistentUserContextPermissionsPutRequestBody request, Exception failure) {
        return null;
    }
}
