package com.backbase.accesscontrol.api.service;

import com.backbase.accesscontrol.audit.AuditObjectType;
import com.backbase.accesscontrol.audit.EventAction;
import com.backbase.accesscontrol.audit.annotation.AuditEvent;
import com.backbase.accesscontrol.configuration.SkipValidation;
import com.backbase.accesscontrol.mappers.model.PayloadConverter;
import com.backbase.accesscontrol.service.facades.FunctionGroupsService;
import com.backbase.accesscontrol.service.rest.spec.api.FunctionGroupsApi;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.BatchResponseItemExtended;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.PresentationIdentifier;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.functiongroups.PresentationFunctionGroup;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.functiongroups.PresentationFunctionGroupPutRequestBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.functiongroups.PresentationIngestFunctionGroupPostResponseBody;
import java.util.List;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Service
@RestController
@AllArgsConstructor
public class FunctionGroupServiceApiController implements FunctionGroupsApi {

    private static final Logger LOGGER = LoggerFactory.getLogger(FunctionGroupServiceApiController.class);
    private FunctionGroupsService functionGroupsService;

    private PayloadConverter payloadConverter;

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Void> deleteFunctionGroupById(@PathVariable("id") String id) {
        LOGGER.info("Delete function group by id");
        functionGroupsService.deleteFunctionGroup(id);
        return ResponseEntity.ok().build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @AuditEvent(eventAction = EventAction.CREATE, objectType = AuditObjectType.FUNCTION_GROUP_INGEST)
    public ResponseEntity<com.backbase.accesscontrol.service.rest.spec.model.IdItem> postPresentationIngestFunctionGroup(
        @RequestBody com.backbase.accesscontrol.service.rest.spec.model.PresentationIngestFunctionGroup presentationFunctionGroup) {
        LOGGER.info("Creating new function group");
        PresentationIngestFunctionGroupPostResponseBody response = functionGroupsService
            .ingestFunctionGroup(
                payloadConverter.convertAndValidate(presentationFunctionGroup, PresentationFunctionGroup.class));
        return new ResponseEntity<>(
            payloadConverter.convert(response, com.backbase.accesscontrol.service.rest.spec.model.IdItem.class),
            HttpStatus.CREATED);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @AuditEvent(eventAction = EventAction.UPDATE, objectType = AuditObjectType.FUNCTION_GROUP_BATCH)
    @Validated(SkipValidation.class)
    public ResponseEntity<List<com.backbase.accesscontrol.service.rest.spec.model.BatchResponseItemExtended>> putFunctionGroupsUpdate(
        @RequestBody List<com.backbase.accesscontrol.service.rest.spec.model.PresentationFunctionGroupPutRequestBody> list) {
        LOGGER.info("Updating batch functions groups by identifier");
        List<BatchResponseItemExtended> response = functionGroupsService.updateFunctionGroupsBatch(
            payloadConverter.convertListPayload(list, PresentationFunctionGroupPutRequestBody.class));
        return new ResponseEntity<>(payloadConverter.convertListPayload(response,
            com.backbase.accesscontrol.service.rest.spec.model.BatchResponseItemExtended.class),
            HttpStatus.MULTI_STATUS);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @AuditEvent(eventAction = EventAction.DELETE, objectType = AuditObjectType.FUNCTION_GROUP_BATCH)
    @Validated(SkipValidation.class)
    public ResponseEntity<List<com.backbase.accesscontrol.service.rest.spec.model.BatchResponseItemExtended>> postFunctionGroupsDelete(
        @RequestBody List<com.backbase.accesscontrol.service.rest.spec.model.PresentationIdentifier> list) {
        LOGGER.info("Deleting batch functions groups");
        List<BatchResponseItemExtended> response = functionGroupsService
            .deleteFunctionGroup(payloadConverter.convertListPayload(list, PresentationIdentifier.class));

        return new ResponseEntity<>(payloadConverter.convertListPayload(response,
            com.backbase.accesscontrol.service.rest.spec.model.BatchResponseItemExtended.class),
            HttpStatus.MULTI_STATUS);
    }
}
