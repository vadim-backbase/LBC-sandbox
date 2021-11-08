package com.backbase.accesscontrol.api.client;

import static com.backbase.accesscontrol.util.ExceptionUtil.getBadRequestException;
import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_065;
import static org.apache.commons.lang3.StringUtils.isEmpty;

import com.backbase.accesscontrol.auth.AccessResourceType;
import com.backbase.accesscontrol.client.rest.spec.model.ArrangementPrivilegesGetResponseBody;
import com.backbase.accesscontrol.client.rest.spec.model.PresentationUserDataItemPermission;
import com.backbase.accesscontrol.client.rest.spec.model.PrivilegesGetResponseBody;
import com.backbase.accesscontrol.client.rest.spec.model.UsersByPermission;
import com.backbase.accesscontrol.configuration.ValidationConfig;
import com.backbase.accesscontrol.dto.GetUsersByPermissionsParameters;
import com.backbase.accesscontrol.dto.UserContextDetailsDto;
import com.backbase.accesscontrol.dto.parameterholder.DataItemPermissionsSearchParametersHolder;
import com.backbase.accesscontrol.mappers.model.PayloadConverter;
import com.backbase.accesscontrol.service.PermissionValidationService;
import com.backbase.accesscontrol.service.facades.UsersFlowService;
import com.backbase.accesscontrol.service.facades.UsersService;
import com.backbase.accesscontrol.util.UserContextUtil;
import com.backbase.accesscontrol.util.constants.ResourceAndFunctionNameConstants;
import com.google.common.base.Strings;
import java.util.List;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class UsersController implements com.backbase.accesscontrol.client.rest.spec.api.UsersApi {

    public static final Logger LOGGER = LoggerFactory.getLogger(UsersController.class);

    private UsersService usersService;
    private UserContextUtil userContextUtil;
    private ValidationConfig validationConfig;
    private PermissionValidationService permissionValidationService;
    private UsersFlowService usersFlowService;
    private PayloadConverter payloadConverter;


    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Void> getArrangementUserPermissionCheck(String id, String resource, String function,
        String privilege) {
        String serviceAgreementId = userContextUtil.getServiceAgreementId();
        UserContextDetailsDto userContextDetails = userContextUtil.getUserContextDetails();
        String userId = userContextDetails.getInternalUserId();
        String legalEntityId = userContextDetails.getLegalEntityId();

        LOGGER.info(
            "Checking Permission for User {} under Service Agreement {}, with Function Name {} , "
                + "Resource Name {}, Privilege {} and arrangementId {}",
            userId, serviceAgreementId, function, resource, privilege, id);

        usersService
            .getArrangementPermissionCheck(new DataItemPermissionsSearchParametersHolder()
                .withServiceAgreementId(serviceAgreementId)
                .withUserId(userId)
                .withLegalEntityId(legalEntityId)
                .withResourceName(resource)
                .withFunctionName(function)
                .withPrivilege(privilege), id);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<List<ArrangementPrivilegesGetResponseBody>> getArrangementUserPrivileges(String functionName,
        String resourceName, String privilegeName) {
        String serviceAgreementId = userContextUtil.getServiceAgreementId();
        UserContextDetailsDto userContextDetails = userContextUtil.getUserContextDetails();
        String userId = userContextDetails.getInternalUserId();
        String legalEntityId = userContextDetails.getLegalEntityId();

        LOGGER.info(
            "Get all arrangement privileges for user with userId {}, service agreement ID {}, "
                + "function with name: {}, resource with name: {} for privilege(s) {}",
            userId, serviceAgreementId, functionName, resourceName, privilegeName);

        List<com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.users.ArrangementPrivilegesGetResponseBody> response = usersService
            .getArrangementPrivileges(new DataItemPermissionsSearchParametersHolder()
                .withUserId(userId)
                .withServiceAgreementId(serviceAgreementId)
                .withLegalEntityId(legalEntityId)
                .withFunctionName(functionName)
                .withResourceName(resourceName)
                .withPrivilege(privilegeName));

        return new ResponseEntity<>(payloadConverter.convertListPayload(
            response, ArrangementPrivilegesGetResponseBody.class), HttpStatus.OK);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Void> getCheckUserPermission(String functionName, String resourceName, String privileges) {
        String serviceAgreementId = userContextUtil.getServiceAgreementId();
        String userId = userContextUtil.getUserContextDetails().getInternalUserId();

        LOGGER.info(
            "Check User Permission for User with userId {}, service agreement ID {}, "
                + "function with name {}, resource with name {} for privilege(s) {}",
            userId, serviceAgreementId, functionName, resourceName, privileges);

        usersService.getUserPermissionCheck(userId, serviceAgreementId, functionName, resourceName,
            privileges);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<List<PresentationUserDataItemPermission>> getDataItemPermissionsContext(String functionName,
        String resourceName, String privilege, String dataGroupType, String dataItemId) {
        String serviceAgreementId = userContextUtil.getServiceAgreementId();
        String userId = userContextUtil.getUserContextDetails().getInternalUserId();
        if (dataGroupType != null) {
            validationConfig.validateDataGroupType(dataGroupType);
        }

        List<com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.users.PresentationUserDataItemPermission> result = usersService
            .getDataItemPrivileges(new DataItemPermissionsSearchParametersHolder()
                .withServiceAgreementId(serviceAgreementId)
                .withUserId(userId)
                .withResourceName(resourceName)
                .withFunctionName(functionName)
                .withPrivilege(privilege), dataGroupType, dataItemId);

        return new ResponseEntity<>(payloadConverter
            .convertListPayload(result,
                PresentationUserDataItemPermission.class),
            HttpStatus.OK);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<List<com.backbase.accesscontrol.client.rest.spec.model.UserPermissionsSummaryGetResponseBody>> getUserPermissionsSummary() {
        LOGGER.info("Trying to get User Permission Summary");
        return new ResponseEntity<>(payloadConverter
            .convertListPayload(usersService.getUserPermissionsSummary(),
                com.backbase.accesscontrol.client.rest.spec.model.UserPermissionsSummaryGetResponseBody.class),
            HttpStatus.OK);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<List<PrivilegesGetResponseBody>> getUserPrivileges(String functionName, String resourceName) {
        String serviceAgreementId = userContextUtil.getServiceAgreementId();
        String userId = userContextUtil.getUserContextDetails().getInternalUserId();

        LOGGER.info(
            "Get all privileges for user with userId {}, service agreement Id {}, "
                + "function with name {} and resource with name {}",
            userId, serviceAgreementId, functionName, resourceName);

        return new ResponseEntity<>(payloadConverter.convertListPayload(
            usersService.getPrivileges(userId, serviceAgreementId, functionName, resourceName),
            PrivilegesGetResponseBody.class), HttpStatus.OK);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @PreAuthorize("checkPermission('" + ResourceAndFunctionNameConstants.RESOURCE_MANAGE_USERS + "', "
        + "'" + ResourceAndFunctionNameConstants.FUNCTION_MANAGE_USERS + "', "
        + "{'" + ResourceAndFunctionNameConstants.PRIVILEGE_VIEW + "'})")
    public ResponseEntity<UsersByPermission> getUsersByPermissions(String functionName, String serviceAgreementId,
        String privilege, String dataGroupType, String dataItemId) {

        LOGGER.info(
            "Fetching a list of users filtered by permissions with serviceAgreementId {}, functionName {},"
                + " privilege {}, dataGroupType {}, dataItemId {}, ", serviceAgreementId, functionName, privilege,
            dataGroupType, dataItemId);

        String filterByServiceAgreementId =
            Strings.isNullOrEmpty(serviceAgreementId) ? userContextUtil.getServiceAgreementId() : serviceAgreementId;

        permissionValidationService
            .validateAccessToServiceAgreementResource(
                filterByServiceAgreementId,
                AccessResourceType.USER_AND_ACCOUNT);

        if (isEmpty(dataGroupType) ^ isEmpty(dataItemId)) {
            LOGGER.warn("Only one of data group type or data item id was provided.");
            throw getBadRequestException(ERR_ACQ_065.getErrorMessage(), ERR_ACQ_065.getErrorCode());
        }

        GetUsersByPermissionsParameters getUsersByPermissionsParameters = new GetUsersByPermissionsParameters(
            filterByServiceAgreementId, functionName, privilege, dataGroupType, dataItemId);

        return new ResponseEntity<>(
            payloadConverter.convert(usersFlowService.getUsersByPermissions(getUsersByPermissionsParameters),
                UsersByPermission.class),
            HttpStatus.OK);
    }
}
