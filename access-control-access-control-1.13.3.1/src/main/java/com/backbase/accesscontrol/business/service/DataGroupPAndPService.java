package com.backbase.accesscontrol.business.service;

import com.backbase.accesscontrol.audit.AuditObjectType;
import com.backbase.accesscontrol.audit.EventAction;
import com.backbase.accesscontrol.audit.annotation.AuditEvent;
import com.backbase.accesscontrol.auth.AccessResourceType;
import com.backbase.accesscontrol.business.persistence.datagroup.AddDataGroupApprovalHandler;
import com.backbase.accesscontrol.business.persistence.datagroup.AddDataGroupHandler;
import com.backbase.accesscontrol.business.persistence.datagroup.DeleteDataGroupApprovalHandler;
import com.backbase.accesscontrol.business.persistence.datagroup.DeleteDataGroupHandler;
import com.backbase.accesscontrol.business.persistence.datagroup.UpdateDataGroupApprovalHandler;
import com.backbase.accesscontrol.business.persistence.datagroup.UpdateDataGroupByIdentifierHandler;
import com.backbase.accesscontrol.business.persistence.datagroup.UpdateDataGroupHandler;
import com.backbase.accesscontrol.configuration.ValidationConfig;
import com.backbase.accesscontrol.dto.ApprovalDto;
import com.backbase.accesscontrol.dto.parameterholder.EmptyParameterHolder;
import com.backbase.accesscontrol.dto.parameterholder.SingleParameterHolder;
import com.backbase.accesscontrol.mappers.BatchResponseItemExtendedMapper;
import com.backbase.accesscontrol.service.DataGroupService;
import com.backbase.accesscontrol.service.PermissionValidationService;
import com.backbase.accesscontrol.service.batch.datagroup.DeleteDataGroupPersistence;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.datagroups.DataGroupItemBase;
import com.backbase.presentation.accessgroup.event.spec.v1.DataGroupBase;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.BatchResponseItemExtended;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.PresentationIdentifier;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.datagroups.DataGroupByIdPutRequestBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.datagroups.DataGroupsPostResponseBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.datagroups.PresentationSingleDataGroupPutRequestBody;
import java.util.List;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

/**
 * A service class that communicates with the P&P services via the clients and returns responses.
 */
@Service
@AllArgsConstructor
public class DataGroupPAndPService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataGroupPAndPService.class);

    private DataGroupService dataGroupService;
    private PermissionValidationService permissionValidationService;
    private ValidationConfig validationConfig;
    private AddDataGroupHandler addDataGroupHandler;
    private AddDataGroupApprovalHandler addDataGroupApprovalHandler;
    private UpdateDataGroupByIdentifierHandler updateDataGroupByIdentifierHandler;
    private DeleteDataGroupHandler deleteDataGroupHandler;
    private UpdateDataGroupHandler updateDataGroupHandler;
    private UpdateDataGroupApprovalHandler updateDataGroupApprovalHandler;
    private DeleteDataGroupApprovalHandler deleteDataGroupApprovalHandler;
    private DeleteDataGroupPersistence deleteDataGroupPersistence;
    private BatchResponseItemExtendedMapper batchResponseItemExtendedMapper;


    /**
     * Creates new Data Group.
     *
     * @param request data {@link DataGroupBase} to be created
     * @return - created {@link DataGroupsPostResponseBody}.
     */
    @AuditEvent(eventAction = EventAction.CREATE, objectType = AuditObjectType.DATA_GROUP)
    public DataGroupsPostResponseBody createDataGroupWithAudit(DataGroupBase request) {

        return addDataGroupHandler.handleRequest(new EmptyParameterHolder(), request);
    }

    /**
     * Creates new Data Group.
     *
     * @param dataGroupBase the object {@link DataGroupBase} to be created
     * @return - the created {@link DataGroupsPostResponseBody} object.
     */
    public DataGroupsPostResponseBody createDataGroup(DataGroupBase dataGroupBase) {

        return addDataGroupHandler.handleRequest(new EmptyParameterHolder(), dataGroupBase);
    }

    /**
     * Deletes the data group by it's id.
     *
     * @param id - id of the data group
     */
    @AuditEvent(eventAction = EventAction.DELETE, objectType = AuditObjectType.DATA_GROUP)
    public void deleteDataGroup(String id) {
        validationConfig.validateIfDataGroupTypeIsAllowed(dataGroupService.getById(id).getDataItemType());

        DataGroupItemBase dataGroup = permissionValidationService.getDataGroupById(id);

        permissionValidationService.validateAccessToServiceAgreementResource(dataGroup.getServiceAgreementId(),
            AccessResourceType.USER_AND_ACCOUNT);

        deleteDataGroupHandler.handleRequest(new SingleParameterHolder<>(id), null);

    }

    /**
     * Deletes the data group by it's id with approval ON.
     *
     * @param dataGroupId id of the data group
     * @param approvalId  approval id
     */
    @AuditEvent(eventAction = EventAction.DELETE_PENDING, objectType = AuditObjectType.DATA_GROUP_APPROVAL)
    public void deleteDataGroupWithApproval(String dataGroupId, String approvalId) {
        validationConfig.validateIfDataGroupTypeIsAllowed(dataGroupService.getById(dataGroupId).getDataItemType());

        DataGroupItemBase dataGroup = permissionValidationService.getDataGroupById(dataGroupId);

        permissionValidationService.validateAccessToServiceAgreementResource(dataGroup.getServiceAgreementId(),
            AccessResourceType.USER_AND_ACCOUNT);

        deleteDataGroupApprovalHandler
            .handleRequest(new SingleParameterHolder<>(dataGroupId), new ApprovalDto(approvalId, null));

    }

    /**
     * Updates the data group by it's id.
     *
     * @param request data {@link DataGroupByIdPutRequestBody} to be created
     * @param id      id of the data group
     */
    @AuditEvent(eventAction = EventAction.UPDATE, objectType = AuditObjectType.DATA_GROUP)
    public void updateDataGroup(DataGroupByIdPutRequestBody request, String id) {

        LOGGER.info("Updating data group with body {}", request);
        updateDataGroupHandler.handleRequest(new SingleParameterHolder<>(id), request);
    }

    /**
     * Updates the data group by it's id with approval ON.
     *
     * @param request    data {@link DataGroupByIdPutRequestBody} to be created
     * @param approvalId id of the data group
     */
    @AuditEvent(eventAction = EventAction.UPDATE_PENDING, objectType = AuditObjectType.DATA_GROUP_APPROVAL)
    public ResponseEntity<Void> updateDataGroupWithApproval(DataGroupByIdPutRequestBody request, String approvalId) {
        updateDataGroupApprovalHandler
            .handleRequest(new SingleParameterHolder<>(new ApprovalDto(approvalId, null)), request);
        return null;
    }

    /**
     * Delete Data Groups by Identifiers.
     *
     * @param request list of PresentationIdentifier to be deleted
     * @return - BatchResponseItemExtended for every data group deleted
     */
    public List<BatchResponseItemExtended> deleteDataGroupsByIdentifiers(List<PresentationIdentifier> request) {
        return batchResponseItemExtendedMapper.mapList(deleteDataGroupPersistence.processBatchItems(request));
    }

    /**
     * Create Data Group with approval.
     */
    @AuditEvent(eventAction = EventAction.CREATE_PENDING, objectType = AuditObjectType.DATA_GROUP_APPROVAL)
    public DataGroupsPostResponseBody createDataGroupWithApproval(
        DataGroupBase request, String approvalId) {

        return addDataGroupApprovalHandler.handleRequest(new SingleParameterHolder<>(approvalId), request);
    }


    /**
     * Update data group by identifier.
     *
     * @param requestBody {@link PresentationSingleDataGroupPutRequestBody}
     */
    public void updateDataGroupPersistence(PresentationSingleDataGroupPutRequestBody requestBody) {

        updateDataGroupByIdentifierHandler.handleRequest(new EmptyParameterHolder(), requestBody);
    }

}
