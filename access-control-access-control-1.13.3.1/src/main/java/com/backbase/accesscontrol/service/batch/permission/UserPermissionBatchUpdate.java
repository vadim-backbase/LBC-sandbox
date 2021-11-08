package com.backbase.accesscontrol.service.batch.permission;

import com.backbase.accesscontrol.domain.dto.ResponseItemExtended;
import com.backbase.accesscontrol.domain.enums.ItemStatusCode;
import com.backbase.accesscontrol.dto.AssignUserPermissionsData;
import com.backbase.accesscontrol.service.LeanGenericBatchProcessor;
import com.backbase.accesscontrol.service.PermissionService;
import com.backbase.buildingblocks.backend.communication.event.proxy.EventBus;
import com.backbase.pandp.accesscontrol.event.spec.v1.UserContextEvent;
import java.util.List;
import javax.validation.Validator;
import org.springframework.stereotype.Service;

@Service
public class UserPermissionBatchUpdate extends
    LeanGenericBatchProcessor<AssignUserPermissionsData, ResponseItemExtended, String> {

    private PermissionService permissionService;

    public UserPermissionBatchUpdate(Validator validator, EventBus eventBus, PermissionService permissionService) {
        super(validator, eventBus);
        this.permissionService = permissionService;
    }

    @Override
    protected String performBatchProcess(
        AssignUserPermissionsData assignUserPermissionsData) {
        return permissionService.updateUserPermission(assignUserPermissionsData);
    }

    @Override
    protected ResponseItemExtended getBatchResponseItem(AssignUserPermissionsData item,
        ItemStatusCode statusCode,
        List<String> errorMessages) {
        return new ResponseItemExtended(
            item.getUsersByExternalId().get(item.getAssignUserPermissions().getExternalUserId()).getId(),
            item.getAssignUserPermissions().getExternalServiceAgreementId(),
            statusCode,
            null,
            errorMessages);
    }

    @Override
    protected UserContextEvent createEvent(AssignUserPermissionsData request, String internalId) {

        return new UserContextEvent()
            .withServiceAgreementId(internalId)
            .withUserId(
                request.getUsersByExternalId().get(request.getAssignUserPermissions().getExternalUserId()).getId());
    }

    @Override
    protected boolean sortResponse() {
        return false;
    }
}

