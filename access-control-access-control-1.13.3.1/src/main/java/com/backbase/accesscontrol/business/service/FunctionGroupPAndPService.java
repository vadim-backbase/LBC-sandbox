package com.backbase.accesscontrol.business.service;

import static com.backbase.accesscontrol.util.ExceptionUtil.getBadRequestException;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_105;

import com.backbase.accesscontrol.audit.AuditObjectType;
import com.backbase.accesscontrol.audit.EventAction;
import com.backbase.accesscontrol.audit.annotation.AuditEvent;
import com.backbase.accesscontrol.business.persistence.functiongroup.AddFunctionGroupApprovalHandler;
import com.backbase.accesscontrol.business.persistence.functiongroup.CreateFunctionGroupHandler;
import com.backbase.accesscontrol.business.persistence.functiongroup.DeleteFunctionGroupApprovalHandler;
import com.backbase.accesscontrol.business.persistence.functiongroup.DeleteFunctionGroupHandler;
import com.backbase.accesscontrol.business.persistence.functiongroup.UpdateFunctionGroupApprovalHandler;
import com.backbase.accesscontrol.business.persistence.functiongroup.UpdateFunctionGroupHandler;
import com.backbase.accesscontrol.domain.dto.FunctionGroupApprovalBase;
import com.backbase.accesscontrol.dto.ApprovalDto;
import com.backbase.accesscontrol.dto.parameterholder.EmptyParameterHolder;
import com.backbase.accesscontrol.dto.parameterholder.FunctionGroupIdApprovalIdParameterHolder;
import com.backbase.accesscontrol.dto.parameterholder.SingleParameterHolder;
import com.backbase.accesscontrol.mappers.BatchResponseItemExtendedMapper;
import com.backbase.accesscontrol.mappers.FunctionGroupMapper;
import com.backbase.accesscontrol.service.FunctionGroupService;
import com.backbase.accesscontrol.service.batch.functiongroup.DeleteFunctionGroupPersistence;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.functiongroups.FunctionGroupBase.Type;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.functiongroups.FunctionGroupByIdGetResponseBody;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.functiongroups.FunctionGroupsGetResponseBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.BatchResponseItemExtended;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.PresentationIdentifier;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.functiongroups.FunctionGroupBase;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.functiongroups.FunctionGroupByIdPutRequestBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.functiongroups.FunctionGroupsPostResponseBody;
import java.util.List;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * A service class that communicates with the P&P services via the clients and returns responses.
 */
@Service
@AllArgsConstructor
public class FunctionGroupPAndPService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FunctionGroupPAndPService.class);
    private FunctionGroupService functionGroupService;
    private FunctionGroupMapper functionGroupMapper;
    private DeleteFunctionGroupHandler deleteFunctionGroupHandler;
    private DeleteFunctionGroupApprovalHandler deleteFunctionGroupApprovalHandler;
    private AddFunctionGroupApprovalHandler addFunctionGroupApprovalHandler;
    private CreateFunctionGroupHandler createFunctionGroupHandler;
    private DeleteFunctionGroupPersistence deleteFunctionGroupPersistence;
    private UpdateFunctionGroupHandler updateFunctionGroupHandler;
    private UpdateFunctionGroupApprovalHandler updateFunctionGroupApprovalHandler;
    private BatchResponseItemExtendedMapper batchResponseItemExtendedMapper;


    /**
     * Retrieves Function Groups by service agreement id.
     *
     * @param serviceAgreementId service agreement ID.
     * @return returns all function access groups from the P&P service, belonging to the specified service agreement.
     */
    public List<FunctionGroupsGetResponseBody> getFunctionGroups(String serviceAgreementId) {
    	return functionGroupService.getFunctionGroupsByServiceAgreementId(serviceAgreementId);
    }

    /**
     * Updates Function Group.
     *
     * @param request - FunctionGroupByIdPutRequestBody to be updated
     * @param id      - function group id
     */
    @AuditEvent(eventAction = EventAction.UPDATE, objectType = AuditObjectType.FUNCTION_GROUP)
    public Void updateFunctionGroup(FunctionGroupByIdPutRequestBody request, String id) {
        return updateFunctionGroupHandler.handleRequest(new SingleParameterHolder<>(id), request);
    }

    /**
     * Creates new Function Group.
     *
     * @param functionGroupBase - FunctionGroupsPostResponseBody to be created
     * @return - {@link FunctionGroupsPostResponseBody}
     */
    @AuditEvent(eventAction = EventAction.CREATE, objectType = AuditObjectType.FUNCTION_GROUP)
    public FunctionGroupsPostResponseBody createFunctionGroup(FunctionGroupBase functionGroupBase) {

        com.backbase.accesscontrol.domain.dto.FunctionGroupBase functionGroupBaseDto =
            functionGroupMapper.functionGroupBasePresentationToFunctionGroupBaseDto(functionGroupBase);

        return createFunctionGroupHandler.handleRequest(new EmptyParameterHolder(), functionGroupBaseDto);
    }

    /**
     * Delete Function Group by id.
     *
     * @param id - Function Group id
     */
    @AuditEvent(eventAction = EventAction.DELETE, objectType = AuditObjectType.FUNCTION_GROUP)
    public void deleteFunctionGroup(String id) {
        FunctionGroupByIdGetResponseBody functionGroup = functionGroupService.getFunctionGroupById(id);
        if (functionGroup.getType().equals(Type.TEMPLATE)) {
            LOGGER.warn("Function group template delete operation is not supported. Function group template id {}", id);
            throw getBadRequestException(ERR_ACC_105.getErrorMessage(), ERR_ACC_105.getErrorCode());
        }
        deleteFunctionGroupHandler.handleRequest(new SingleParameterHolder<>(id), null);
    }


    /**
     * Create delete pending record for function group.
     *
     * @param id         - Function Group id
     * @param approvalID - Approval id
     */
    @AuditEvent(eventAction = EventAction.DELETE_PENDING, objectType = AuditObjectType.FUNCTION_GROUP_APPROVAL)
    public void deleteFunctionGroup(String id, String approvalID) {

        deleteFunctionGroupApprovalHandler
            .handleRequest(new SingleParameterHolder<>(id), new ApprovalDto(approvalID, null));
    }


    /**
     * Deletes Function Groups.
     *
     * @param request - list of {@link PresentationIdentifier} of Function Groups to be deleted
     * @return - list of {@link BatchResponseItemExtended} for every function group deleted
     */
    public List<BatchResponseItemExtended> deleteFunctionGroup(List<PresentationIdentifier> request) {

        return batchResponseItemExtendedMapper.mapList(deleteFunctionGroupPersistence.processBatchItems(request));
    }

    /**
     * Retrieves Function Group by id from persistence service.
     *
     * @param id - Function Group id
     * @return {@link FunctionGroupByIdGetResponseBody}
     */
    public FunctionGroupByIdGetResponseBody getFunctionGroupById(String id) {
        return functionGroupService.getFunctionGroupById(id);
    }

    /**
     * Create approval function group record in database. Temporary record that will update function group with given
     * functionGroupId.
     *
     * @param request         FunctionGroupByIdPutRequestBody object
     * @param functionGroupId id of function group
     */
    @AuditEvent(eventAction = EventAction.UPDATE_PENDING, objectType = AuditObjectType.FUNCTION_GROUP_APPROVAL)
    public void updateFunctionGroupWithApproval(FunctionGroupByIdPutRequestBody request, String functionGroupId,
        String approvalId) {

        updateFunctionGroupApprovalHandler
            .handleRequest(new FunctionGroupIdApprovalIdParameterHolder(functionGroupId, approvalId), request);
    }

    /**
     * Create approval function group record in database. Temporary record for function group.
     *
     * @param request FunctionGroupBase object
     * @return {@link FunctionGroupsPostResponseBody}
     */
    @AuditEvent(eventAction = EventAction.CREATE_PENDING, objectType = AuditObjectType.FUNCTION_GROUP_APPROVAL)
    public FunctionGroupsPostResponseBody createFunctionGroupWithApproval(FunctionGroupBase request,
        String approvalId) {
        FunctionGroupApprovalBase functionGroupApprovalCreate = functionGroupMapper
            .toFunctionGroupCreate(request, approvalId);
        return addFunctionGroupApprovalHandler.handleRequest(new EmptyParameterHolder(), functionGroupApprovalCreate);
    }
}
