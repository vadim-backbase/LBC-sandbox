package com.backbase.accesscontrol.api.client;

import com.backbase.accesscontrol.auth.AccessResourceType;
import com.backbase.accesscontrol.business.approval.scope.ApprovalOnRequestScope;
import com.backbase.accesscontrol.client.rest.spec.model.FunctionGroupItem;
import com.backbase.accesscontrol.client.rest.spec.model.FunctionGroupItemBase;
import com.backbase.accesscontrol.client.rest.spec.model.FunctionGroupItemPut;
import com.backbase.accesscontrol.client.rest.spec.model.IdItem;
import com.backbase.accesscontrol.mappers.model.PayloadConverter;
import com.backbase.accesscontrol.service.PermissionValidationService;
import com.backbase.accesscontrol.service.facades.FunctionGroupsService;
import com.backbase.accesscontrol.util.constants.ResourceAndFunctionNameConstants;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.functiongroups.FunctionGroupBase;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.functiongroups.FunctionGroupBase.Type;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.functiongroups.FunctionGroupByIdGetResponseBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.functiongroups.FunctionGroupByIdPutRequestBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.functiongroups.FunctionGroupsPostResponseBody;
import java.util.List;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

/**
 * Rest controller for FunctionGroups.
 */
@RestController
@AllArgsConstructor
public class FunctionGroupsController implements com.backbase.accesscontrol.client.rest.spec.api.FunctionGroupsApi {

    public static final Logger LOGGER = LoggerFactory.getLogger(FunctionGroupsController.class);

    private FunctionGroupsService functionGroupsService;
    private PermissionValidationService permissionValidationService;
    private ApprovalOnRequestScope approvalOnRequestScope;
    private PayloadConverter payloadConverter;
    /**
     * {@inheritDoc}
     */
    @Override
    @PreAuthorize(
        "checkPermission('" + ResourceAndFunctionNameConstants.ENTITLEMENTS_RESOURCE_NAME_FUNCTION_NAME + "', "
            + "'" + ResourceAndFunctionNameConstants.ENTITLEMENTS_MANAGE_FUNCTION_GROUPS + "', "
            + "{'" + ResourceAndFunctionNameConstants.PRIVILEGE_DELETE + "'})")
    public ResponseEntity<Void> deleteFunctionGroupById(String id) {
        LOGGER.info("Delete function group by id");
        com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.functiongroups
            .FunctionGroupByIdGetResponseBody functionGroupById =
            permissionValidationService.getFunctionGroupById(id);

        permissionValidationService.validateAccessToServiceAgreementResource(functionGroupById.getServiceAgreementId(),
            AccessResourceType.USER_AND_ACCOUNT);
        functionGroupsService.deleteFunctionGroup(id);

        HttpStatus status = HttpStatus.OK;

        if (approvalOnRequestScope.isApproval()) {
            status = HttpStatus.ACCEPTED;
        }

        return new ResponseEntity<>(status);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @PreAuthorize(
        "checkPermission('" + ResourceAndFunctionNameConstants.ENTITLEMENTS_RESOURCE_NAME_FUNCTION_NAME + "', "
            + "'" + ResourceAndFunctionNameConstants.ENTITLEMENTS_MANAGE_FUNCTION_GROUPS + "', "
            + "{'" + ResourceAndFunctionNameConstants.PRIVILEGE_VIEW + "'})")
    public ResponseEntity<FunctionGroupItem> getFunctionGroupById(String id) {
        LOGGER.info("Get function group by id");

        FunctionGroupByIdGetResponseBody response = functionGroupsService.getFunctionGroupById(id);
        
        if (response.getType() != Type.TEMPLATE) {
            permissionValidationService.validateAccessToServiceAgreementResource(response.getServiceAgreementId(),
                AccessResourceType.USER_OR_ACCOUNT);
        }

        return new ResponseEntity<>(
            payloadConverter.convert(response,
                FunctionGroupItem.class),
            HttpStatus.OK);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @PreAuthorize(
        "checkPermission('" + ResourceAndFunctionNameConstants.ENTITLEMENTS_RESOURCE_NAME_FUNCTION_NAME + "', "
            + "'" + ResourceAndFunctionNameConstants.ENTITLEMENTS_MANAGE_FUNCTION_GROUPS + "', "
            + "{'" + ResourceAndFunctionNameConstants.PRIVILEGE_VIEW + "'})")
    public ResponseEntity<List<FunctionGroupItem>> getFunctionGroups(String serviceAgreementId) {
        LOGGER.info("Getting a list of all function access groups for a given service agreement id");

        permissionValidationService
            .validateAccessToServiceAgreementResource(serviceAgreementId, AccessResourceType.USER_OR_ACCOUNT);
        return new ResponseEntity<>(payloadConverter
            .convertListPayload(functionGroupsService.getAllFunctionGroup(serviceAgreementId),
                FunctionGroupItem.class),
            HttpStatus.OK);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @PreAuthorize(
        "checkPermission('" + ResourceAndFunctionNameConstants.ENTITLEMENTS_RESOURCE_NAME_FUNCTION_NAME + "', "
            + "'" + ResourceAndFunctionNameConstants.ENTITLEMENTS_MANAGE_FUNCTION_GROUPS + "', "
            + "{'" + ResourceAndFunctionNameConstants.PRIVILEGE_CREATE + "'})")
    public ResponseEntity<IdItem> postFunctionGroups(FunctionGroupItemBase functionGroupItemBase) {
        LOGGER.info("Saving a new function group");

        permissionValidationService
            .validateAccessToServiceAgreementResource(functionGroupItemBase.getServiceAgreementId(),
                AccessResourceType.USER_AND_ACCOUNT);

        FunctionGroupsPostResponseBody data = functionGroupsService
            .addFunctionGroup(payloadConverter.convertAndValidate(functionGroupItemBase, FunctionGroupBase.class));
        if (approvalOnRequestScope.isApproval()) {
            return new ResponseEntity<>(new IdItem().id(data.getId()), HttpStatus.ACCEPTED);
        }
        return new ResponseEntity<>(new IdItem().id(data.getId()), HttpStatus.CREATED);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @PreAuthorize(
        "checkPermission('" + ResourceAndFunctionNameConstants.ENTITLEMENTS_RESOURCE_NAME_FUNCTION_NAME + "', "
            + "'" + ResourceAndFunctionNameConstants.ENTITLEMENTS_MANAGE_FUNCTION_GROUPS + "', "
            + "{'" + ResourceAndFunctionNameConstants.PRIVILEGE_EDIT + "'})")
    public ResponseEntity<Void> putFunctionGroupById(String id, FunctionGroupItemPut functionGroupItemPut) {
        LOGGER.info("Updating Function Group with id {}: ", id);
        permissionValidationService
            .validateAccessToServiceAgreementResource(functionGroupItemPut.getServiceAgreementId(),
                AccessResourceType.USER_AND_ACCOUNT);

        functionGroupsService.updateFunctionGroup(id,
            payloadConverter.convertAndValidate(functionGroupItemPut, FunctionGroupByIdPutRequestBody.class));

        HttpStatus status = HttpStatus.OK;

        if (approvalOnRequestScope.isApproval()) {
            status = HttpStatus.ACCEPTED;
        }
        return new ResponseEntity<>(status);
    }
}
