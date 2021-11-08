package com.backbase.accesscontrol.api.service;

import com.backbase.accesscontrol.audit.AuditObjectType;
import com.backbase.accesscontrol.audit.EventAction;
import com.backbase.accesscontrol.audit.annotation.AuditEvent;
import com.backbase.accesscontrol.configuration.SkipValidation;
import com.backbase.accesscontrol.configuration.ValidationConfig;
import com.backbase.accesscontrol.mappers.model.PayloadConverter;
import com.backbase.accesscontrol.service.facades.DataGroupFlowService;
import com.backbase.accesscontrol.service.facades.DataGroupService;
import com.backbase.accesscontrol.service.rest.spec.api.DataGroupsApi;
import com.backbase.accesscontrol.service.rest.spec.model.DataGroupItemSystemBase;
import com.backbase.accesscontrol.service.rest.spec.model.IdItem;
import com.backbase.accesscontrol.service.rest.spec.model.PresentationDataGroupUpdate;
import com.backbase.accesscontrol.service.rest.spec.model.PresentationSearchDataGroupsRequest;
import com.backbase.accesscontrol.service.rest.spec.model.PresentationServiceAgreementWithDataGroupsItem;
import com.backbase.presentation.accessgroup.event.spec.v1.DataGroupBase;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.BatchResponseItemExtended;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.PresentationIdentifier;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.datagroups.DataGroupsPostResponseBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.datagroups.PresentationDataGroupItemPutRequestBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.datagroups.PresentationGetDataGroupsRequest;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.datagroups.PresentationServiceAgreementWithDataGroups;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.datagroups.PresentationSingleDataGroupPutRequestBody;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class DataGroupsServiceApiController implements DataGroupsApi {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataGroupsServiceApiController.class);

    private DataGroupService dataGroupService;
    private ValidationConfig validationConfig;
    private DataGroupFlowService dataGroupFlowService;

    private PayloadConverter payloadConverter;

    /**
     * {@inheritDoc}
     */
    @Override
    @AuditEvent(eventAction = EventAction.CREATE, objectType = AuditObjectType.DATA_GROUP_SERVICE)
    public ResponseEntity<IdItem> postDataGroups(
        @RequestBody DataGroupItemSystemBase dataGroupBase) {
        LOGGER.info("Creating data group {}", dataGroupBase);

        DataGroupsPostResponseBody response = dataGroupFlowService
            .createDataGroup(payloadConverter.convertAndValidate(dataGroupBase, DataGroupBase.class));

        return new ResponseEntity<>(payloadConverter.convert(response, IdItem.class), HttpStatus.CREATED);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @AuditEvent(eventAction = EventAction.UPDATE, objectType = AuditObjectType.DATA_GROUP_SERVICE)
    public ResponseEntity<Void> putDataGroups(
        @RequestBody PresentationDataGroupUpdate presentationSingleDataGroupPutRequestBody) {
        LOGGER.info("Updating data group {}", presentationSingleDataGroupPutRequestBody);
        validationConfig.validateDataGroupType(presentationSingleDataGroupPutRequestBody.getType());

        dataGroupFlowService.updateDataGroup(payloadConverter.convertAndValidate(presentationSingleDataGroupPutRequestBody,
            PresentationSingleDataGroupPutRequestBody.class));

        return ResponseEntity.ok().build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Void> deleteDataGroupById(@PathVariable("id") String dataGroupId) {
        LOGGER.info("Deleting data group with dataGroupId {}", dataGroupId);

        dataGroupService.deleteDataGroup(dataGroupId);

        return ResponseEntity.ok().build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @AuditEvent(eventAction = EventAction.UPDATE, objectType = AuditObjectType.DATA_GROUP_ITEMS)
    @Validated(SkipValidation.class)
    public ResponseEntity<List<com.backbase.accesscontrol.service.rest.spec.model.BatchResponseItemExtended>> putDataGroupItemsUpdate(
        @RequestBody List<com.backbase.accesscontrol.service.rest.spec.model.PresentationDataGroupItemPutRequestBody> dataGroupItemPutRequestBodies) {
        LOGGER.info("Updating batch data group items by identifier");
        List<BatchResponseItemExtended> response = dataGroupService.updateDataGroupItemsBatchByIdentifier(
            payloadConverter
                .convertListPayload(dataGroupItemPutRequestBodies, PresentationDataGroupItemPutRequestBody.class),
            new ArrayList<>(), new HashMap<>(), new HashMap<>());

        return new ResponseEntity<>(payloadConverter.convertListPayload(response,
            com.backbase.accesscontrol.service.rest.spec.model.BatchResponseItemExtended.class),
            HttpStatus.MULTI_STATUS);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @AuditEvent(eventAction = EventAction.DELETE, objectType = AuditObjectType.DATA_GROUP_BATCH_SERVICE)
    @Validated(SkipValidation.class)
    public ResponseEntity<List<com.backbase.accesscontrol.service.rest.spec.model.BatchResponseItemExtended>> postDataGroupsDelete(
        @RequestBody List<com.backbase.accesscontrol.service.rest.spec.model.PresentationIdentifier> presentationIdentifierList) {
        LOGGER.info("Deleting batch data groups");
        List<BatchResponseItemExtended> response = dataGroupService
            .deleteDataGroupsByIdentifiers(
                payloadConverter.convertListPayload(presentationIdentifierList, PresentationIdentifier.class));

        return new ResponseEntity<>(payloadConverter.convertListPayload(response,
            com.backbase.accesscontrol.service.rest.spec.model.BatchResponseItemExtended.class),
            HttpStatus.MULTI_STATUS);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<List<PresentationServiceAgreementWithDataGroupsItem>> postSearch(
        @PathVariable("type") String type,
        @RequestBody PresentationSearchDataGroupsRequest presentationGetDataGroupsRequest) {
        LOGGER.info("Getting data groups defined by type {} and search criteria {}", type,
            presentationGetDataGroupsRequest);
        List<PresentationServiceAgreementWithDataGroups> response = dataGroupService
            .searchDataGroups(payloadConverter
                .convertAndValidate(presentationGetDataGroupsRequest, PresentationGetDataGroupsRequest.class), type);
        return new ResponseEntity<>(payloadConverter.convertListPayload(response,
            PresentationServiceAgreementWithDataGroupsItem.class),
            HttpStatus.OK);
    }
}
