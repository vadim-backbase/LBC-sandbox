package com.backbase.accesscontrol.api.client;

import com.backbase.accesscontrol.auth.AccessResourceType;
import com.backbase.accesscontrol.client.rest.spec.model.DataGroupItem;
import com.backbase.accesscontrol.client.rest.spec.model.DataGroupItemBase;
import com.backbase.accesscontrol.client.rest.spec.model.IdItem;
import com.backbase.accesscontrol.configuration.ValidationConfig;
import com.backbase.accesscontrol.dto.DataGroupOperationResponse;
import com.backbase.accesscontrol.mappers.DataGroupItemMapper;
import com.backbase.accesscontrol.mappers.model.PayloadConverter;
import com.backbase.accesscontrol.service.PermissionValidationService;
import com.backbase.accesscontrol.service.facades.DataGroupService;
import com.backbase.accesscontrol.util.constants.ResourceAndFunctionNameConstants;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.datagroups.DataGroupByIdGetResponseBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.datagroups.DataGroupByIdPutRequestBody;
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
public class DataGroupClientController implements com.backbase.accesscontrol.client.rest.spec.api.DataGroupsApi {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataGroupClientController.class);

    private DataGroupService dataGroupService;
    private PermissionValidationService permissionValidationService;
    private ValidationConfig validationConfig;
    private DataGroupItemMapper dataGroupItemMapper;
    private PayloadConverter payloadConverter;


    /**
     * {@inheritDoc}
     */
    @Override
    @PreAuthorize("checkPermission('" + ResourceAndFunctionNameConstants.ENTITLEMENTS_RESOURCE_NAME + "', "
        + "'" + ResourceAndFunctionNameConstants.ENTITLEMENTS_MANAGE_DATA_GROUPS + "', "
        + "{'" + ResourceAndFunctionNameConstants.PRIVILEGE_DELETE + "'})")
    public ResponseEntity<Void> deleteDataGroupById(String id) {
        LOGGER.info("Deleting data group with id {}", id);

        DataGroupOperationResponse data = dataGroupService.deleteDataGroup(id);

        HttpStatus status = HttpStatus.OK;

        if (data.isApprovalOn()) {
            status = HttpStatus.ACCEPTED;
        }
        return new ResponseEntity<>(status);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @PreAuthorize("checkPermission('" + ResourceAndFunctionNameConstants.ENTITLEMENTS_RESOURCE_NAME + "', "
        + "'" + ResourceAndFunctionNameConstants.ENTITLEMENTS_MANAGE_DATA_GROUPS + "', "
        + "{'" + ResourceAndFunctionNameConstants.PRIVILEGE_VIEW + "'})")
    public ResponseEntity<DataGroupItem> getDataGroupById(String id) {
        LOGGER.info("Getting data group with id {}", id);
        DataGroupByIdGetResponseBody data = dataGroupService.getDataGroupById(id);
        permissionValidationService
            .validateAccessToServiceAgreementResource(data.getServiceAgreementId(), AccessResourceType.USER_OR_ACCOUNT);
        return new ResponseEntity<>(payloadConverter.convert(data, DataGroupItem.class), HttpStatus.OK);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @PreAuthorize("checkPermission('" + ResourceAndFunctionNameConstants.ENTITLEMENTS_RESOURCE_NAME + "', "
        + "'" + ResourceAndFunctionNameConstants.ENTITLEMENTS_MANAGE_DATA_GROUPS + "', "
        + "{'" + ResourceAndFunctionNameConstants.PRIVILEGE_VIEW + "'})")
    public ResponseEntity<List<DataGroupItem>> getDataGroups(String serviceAgreementId, String type,
        Boolean includeItems) {
        LOGGER.info("Retrieving data groups by service agreement {} and type {}", serviceAgreementId, type);

        permissionValidationService
            .validateAccessToServiceAgreementResource(serviceAgreementId, AccessResourceType.USER_OR_ACCOUNT);
        validationConfig.validateDataGroupTypeWhenProvided(type);
        return new ResponseEntity<>(payloadConverter
            .convertListPayload(dataGroupService.getDataGroups(serviceAgreementId, type, includeItems),
                DataGroupItem.class), HttpStatus.OK);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @PreAuthorize("checkPermission('" + ResourceAndFunctionNameConstants.ENTITLEMENTS_RESOURCE_NAME + "', "
        + "'" + ResourceAndFunctionNameConstants.ENTITLEMENTS_MANAGE_DATA_GROUPS + "', "
        + "{'" + ResourceAndFunctionNameConstants.PRIVILEGE_CREATE + "'})")
    public ResponseEntity<IdItem> postDataGroups(DataGroupItemBase dataGroupItemBase) {
        LOGGER.info("Creating new data group {}", dataGroupItemBase.getName());
        permissionValidationService
            .validateAccessToServiceAgreementResource(dataGroupItemBase.getServiceAgreementId(),
                AccessResourceType.USER_AND_ACCOUNT);
        validationConfig.validateDataGroupType(dataGroupItemBase.getType());
        validationConfig.validateIfDataGroupTypeIsAllowed(dataGroupItemBase.getType());
        validationConfig.validateDataGroupItems(dataGroupItemBase.getItems());

        DataGroupOperationResponse data = dataGroupService
            .addDataGroup(dataGroupItemMapper.convertFromBase(dataGroupItemBase));

        if (data.isApprovalOn()) {
            return new ResponseEntity<>(new IdItem().id(data.getId()), HttpStatus.ACCEPTED);
        }
        return new ResponseEntity<>(new IdItem().id(data.getId()), HttpStatus.CREATED);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @PreAuthorize("checkPermission('" + ResourceAndFunctionNameConstants.ENTITLEMENTS_RESOURCE_NAME + "', "
        + "'" + ResourceAndFunctionNameConstants.ENTITLEMENTS_MANAGE_DATA_GROUPS + "', "
        + "{'" + ResourceAndFunctionNameConstants.PRIVILEGE_EDIT + "'})")
    public ResponseEntity<Void> putDataGroupById(String id, DataGroupItem dataGroupItem) {
        permissionValidationService
            .validateAccessToServiceAgreementResource(dataGroupItem.getServiceAgreementId(),
                AccessResourceType.USER_AND_ACCOUNT);
        validationConfig.validateDataGroupType(dataGroupItem.getType());
        validationConfig.validateIfDataGroupTypeIsAllowed(dataGroupItem.getType());
        validationConfig.validateDataGroupItems(dataGroupItem.getItems());

        DataGroupOperationResponse data = dataGroupService
            .updateDataGroup(payloadConverter.convertAndValidate(dataGroupItem, DataGroupByIdPutRequestBody.class), id);

        HttpStatus status = HttpStatus.OK;

        if (data.isApprovalOn()) {
            status = HttpStatus.ACCEPTED;
        }
        return new ResponseEntity<>(status);
    }
}
