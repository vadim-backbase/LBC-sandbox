package com.backbase.accesscontrol.api.service;

import com.backbase.accesscontrol.audit.AuditObjectType;
import com.backbase.accesscontrol.audit.EventAction;
import com.backbase.accesscontrol.audit.annotation.AuditEvent;
import com.backbase.accesscontrol.business.service.UserManagementService;
import com.backbase.accesscontrol.configuration.SkipValidation;
import com.backbase.accesscontrol.dto.UserContextDetailsDto;
import com.backbase.accesscontrol.dto.parameterholder.DataItemPermissionsSearchParametersHolder;
import com.backbase.accesscontrol.mappers.model.PayloadConverter;
import com.backbase.accesscontrol.service.facades.UsersService;
import com.backbase.accesscontrol.service.rest.spec.api.UsersApi;
import com.backbase.accesscontrol.util.UserContextUtil;
import com.backbase.dbs.user.api.client.v2.model.GetUser;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.BatchResponseItemExtended;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.users.PresentationAssignUserPermissions;
import java.util.List;
import javax.validation.Valid;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class UsersServiceApiController implements UsersApi {

    private static final Logger LOGGER = LoggerFactory.getLogger(UsersServiceApiController.class);

    private UsersService usersService;
    private UserManagementService userManagementService;
    private UserContextUtil userContextUtil;
    private PayloadConverter payloadConverter;

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<List<com.backbase.accesscontrol.service.rest.spec.model.PrivilegesGetResponseBody>>
    getPrivileges(String userId, String functionName, String resourceName, String serviceAgreementId) {

        return ResponseEntity.ok(
            payloadConverter.convertListPayload(
                usersService.getPrivileges(userId, serviceAgreementId, functionName, resourceName),
                com.backbase.accesscontrol.service.rest.spec.model.PrivilegesGetResponseBody.class));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<List<com.backbase.accesscontrol.service.rest.spec.model.ArrangementPrivilegesGetResponseBody>>
    getArrangementPrivileges(String userId, String functionName, String resourceName, String serviceAgreementId,
        String privilegeName) {
        LOGGER.info(
            "Trying to get Privileges for User {} under Service Agreement {}, "
                + "with Function Name {}, Resource Name {} and Privileges {}",
            userId, serviceAgreementId, functionName, resourceName, privilegeName);

        String legalEntityId = null;

        if (serviceAgreementId == null) {
            GetUser userByInternalId = userManagementService
                .getUserByInternalId(userId);
            legalEntityId = userByInternalId.getLegalEntityId();
        }

        return ResponseEntity.ok(
            payloadConverter.convertListPayload(usersService
                    .getArrangementPrivileges(new DataItemPermissionsSearchParametersHolder()
                        .withUserId(userId)
                        .withServiceAgreementId(serviceAgreementId)
                        .withLegalEntityId(legalEntityId)
                        .withFunctionName(functionName)
                        .withResourceName(resourceName)
                        .withPrivilege(privilegeName)),
                com.backbase.accesscontrol.service.rest.spec.model.ArrangementPrivilegesGetResponseBody.class));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Void> getArrangementPermissionCheck(String arrangementId, String userId,
        String resourceName, String functionName, String privilege, String serviceAgreementId) {

        LOGGER.info(
            "Checking Permission for User with id {} under Service Agreement {}, "
                + "with Function Name {} , Resource Name {}, Privilege {} and arrangementId {}",
            userId, serviceAgreementId, functionName, resourceName, privilege, arrangementId);

        String legalEntityId = null;

        if (serviceAgreementId == null) {
            GetUser userByInternalId = userManagementService
                .getUserByInternalId(userId);
            legalEntityId = userByInternalId.getLegalEntityId();
        }

        usersService
            .getArrangementPermissionCheck(new DataItemPermissionsSearchParametersHolder()
                .withUserId(userId)
                .withServiceAgreementId(serviceAgreementId)
                .withLegalEntityId(legalEntityId)
                .withFunctionName(functionName)
                .withResourceName(resourceName)
                .withPrivilege(privilege), arrangementId);
        return ResponseEntity.ok().build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<List<com.backbase.accesscontrol.service.rest.spec.model.PrivilegesGetResponseBody>>
    getUserPrivileges(String functionName, String resourceName) {

        String serviceAgreementId = userContextUtil.getServiceAgreementId();
        String userId = userContextUtil.getUserContextDetails().getInternalUserId();

        LOGGER.info(
            "Trying to get permissions for User {} under Service Agreement {}, "
                + "with Function Name {}, Resource Name {}",
            userId, serviceAgreementId, functionName, resourceName);

        return ResponseEntity.ok(payloadConverter.convertListPayload(usersService
                .getPrivileges(userId, serviceAgreementId, functionName, resourceName),
            com.backbase.accesscontrol.service.rest.spec.model.PrivilegesGetResponseBody.class));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<List<com.backbase.accesscontrol.service.rest.spec.model.ArrangementPrivilegesGetResponseBody>>
    getArrangementUserPrivileges(String functionName,
        String resourceName, String privilegeName) {

        String serviceAgreementId = userContextUtil.getServiceAgreementId();
        UserContextDetailsDto userContextDetails = userContextUtil.getUserContextDetails();
        String userId = userContextDetails.getInternalUserId();
        String legalEntityId = userContextDetails.getLegalEntityId();

        LOGGER.info(
            "Trying to get Privileges for User {} under Service Agreement {}, "
                + "with Function Name: {} , Resource Name: {} and Privileges: {}",
            userId, serviceAgreementId, functionName, resourceName, privilegeName);

        return ResponseEntity.ok(
            payloadConverter.convertListPayload(usersService
                    .getArrangementPrivileges(new DataItemPermissionsSearchParametersHolder()
                        .withUserId(userId)
                        .withServiceAgreementId(serviceAgreementId)
                        .withLegalEntityId(legalEntityId)
                        .withFunctionName(functionName)
                        .withResourceName(resourceName)
                        .withPrivilege(privilegeName)),
                com.backbase.accesscontrol.service.rest.spec.model.ArrangementPrivilegesGetResponseBody.class));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Void> getArrangementUserPermissionCheck(String arrangementId, String resourceName,
        String functionName,
        String privilege) {

        String serviceAgreementId = userContextUtil.getServiceAgreementId();

        UserContextDetailsDto userContextDetails = userContextUtil.getUserContextDetails();
        String userId = userContextDetails.getInternalUserId();
        String legalEntityId = userContextDetails.getLegalEntityId();

        LOGGER.info(
            "Checking Permission for User: {} under Service Agreement: {}, "
                + "with Function Name {} , Resource Name {}, Privilege {} and arrangementId {}",
            userId, serviceAgreementId, functionName, resourceName, privilege, arrangementId);

        usersService
            .getArrangementPermissionCheck(new DataItemPermissionsSearchParametersHolder()
                .withUserId(userId)
                .withServiceAgreementId(serviceAgreementId)
                .withLegalEntityId(legalEntityId)
                .withFunctionName(functionName)
                .withResourceName(resourceName)
                .withPrivilege(privilege), arrangementId);
        return ResponseEntity.ok().build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Void> getUserPermissionCheck(String userId, String functionName,
        String resourceName, String privileges, String serviceAgreementId) {

        LOGGER.info(
            "Checking Permission for User {} under Service Agreement {}, "
                + "with Function Name {} , Resource Name {} and Privileges {}",
            userId, serviceAgreementId, functionName, resourceName, privileges);
        usersService
            .getUserPermissionCheck(userId, serviceAgreementId, functionName, resourceName, privileges);
        return ResponseEntity.ok().build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Validated(SkipValidation.class)
    @AuditEvent(eventAction = EventAction.UPDATE_PERMISSIONS, objectType = AuditObjectType.ASSIGN_USER_PERMISSIONS)
    public ResponseEntity<List<com.backbase.accesscontrol.service.rest.spec.model.BatchResponseItemExtended>>
    putAssignUserPermissions(
        @Valid List<com.backbase.accesscontrol.service.rest.spec.model.PresentationAssignUserPermissions>
            presentationAssignUserPermissions) {

        LOGGER.info("Bulk update user permissions.");
        List<BatchResponseItemExtended> response = usersService.saveBulkUserPermissions(
            payloadConverter.convertListPayload(presentationAssignUserPermissions,
                PresentationAssignUserPermissions.class));
        return new ResponseEntity<>(payloadConverter.convertListPayload(response,
            com.backbase.accesscontrol.service.rest.spec.model.BatchResponseItemExtended.class),
            HttpStatus.MULTI_STATUS);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<List<com.backbase.accesscontrol.service.rest.spec.model
        .UserPermissionsSummaryGetResponseBody>> getUserPermissionsSummary() {
        LOGGER.info("Getting permission summary for user  and service agreement");

        return ResponseEntity.ok(payloadConverter.convertListPayload(usersService.getUserPermissionsSummary(),
            com.backbase.accesscontrol.service.rest.spec.model
                .UserPermissionsSummaryGetResponseBody.class));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Void> getCheckUserPermission(String functionName, String resourceName, String privileges) {

        String serviceAgreementId = userContextUtil.getServiceAgreementId();
        String userId = userContextUtil.getUserContextDetails().getInternalUserId();

        LOGGER.info(
            "Checking Permission for User {} under Service Agreement {}, "
                + "with Function Name {} , Resource Name {} and Privileges {}",
            userId, serviceAgreementId, functionName, resourceName, privileges);

        usersService
            .getUserPermissionCheck(userId, serviceAgreementId, functionName, resourceName, privileges);
        return ResponseEntity.ok().build();
    }
}
