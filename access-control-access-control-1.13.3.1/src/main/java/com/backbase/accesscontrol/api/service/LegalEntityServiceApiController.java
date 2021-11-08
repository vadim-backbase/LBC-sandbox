package com.backbase.accesscontrol.api.service;

import com.backbase.accesscontrol.audit.AuditObjectType;
import com.backbase.accesscontrol.audit.EventAction;
import com.backbase.accesscontrol.audit.annotation.AuditEvent;
import com.backbase.accesscontrol.configuration.SkipValidation;
import com.backbase.accesscontrol.dto.GetLegalEntitiesRequestDto;
import com.backbase.accesscontrol.dto.RecordsDto;
import com.backbase.accesscontrol.mappers.model.PayloadConverter;
import com.backbase.accesscontrol.service.ParameterValidationService;
import com.backbase.accesscontrol.service.facades.LegalEntityFlowService;
import com.backbase.accesscontrol.service.facades.LegalEntityService;
import com.backbase.accesscontrol.service.rest.spec.api.LegalEntitiesApi;
import com.backbase.accesscontrol.service.rest.spec.model.GetServiceAgreement;
import com.backbase.accesscontrol.service.rest.spec.model.LegalEntitiesBatchDelete;
import com.backbase.accesscontrol.service.rest.spec.model.LegalEntityCreateItem;
import com.backbase.accesscontrol.service.rest.spec.model.LegalEntityItem;
import com.backbase.accesscontrol.service.rest.spec.model.LegalEntityItemBase;
import com.backbase.accesscontrol.service.rest.spec.model.LegalEntityItemId;
import com.backbase.accesscontrol.service.rest.spec.model.LegalEntityUpdateItem;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.BatchResponseItem;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.CreateLegalEntitiesPostRequestBody;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.CreateLegalEntitiesPostResponseBody;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.LegalEntitiesPostRequestBody;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.LegalEntitiesPostResponseBody;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.LegalEntityByExternalIdGetResponseBody;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.LegalEntityByExternalIdPutRequestBody;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.LegalEntityByIdGetResponseBody;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.LegalEntityPut;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.MasterServiceAgreementGetResponseBody;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.PresentationBatchDeleteLegalEntities;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.SubEntitiesGetResponseBody;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.SubEntitiesPostResponseBody;
import java.util.Collections;
import java.util.List;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class LegalEntityServiceApiController implements LegalEntitiesApi {

    public static final Logger LOGGER = LoggerFactory.getLogger(LegalEntityServiceApiController.class);

    private LegalEntityService legalEntityService;
    private ParameterValidationService parameterValidationService;
    private LegalEntityFlowService legalEntityFlowService;
    private PayloadConverter payloadConverter;

    /**
     * Controller method for creating legal entity.
     *
     * @param legalEntitiesPostRequestBody requestBody of type {@link LegalEntitiesPostRequestBody}
     * @return responseBody of type {@link LegalEntitiesPostResponseBody}
     */
    @Override
    @AuditEvent(eventAction = EventAction.CREATE, objectType = AuditObjectType.LEGAL_ENTITY)
    public ResponseEntity<LegalEntityItemId> postLegalEntities(
        @RequestBody LegalEntityCreateItem legalEntitiesPostRequestBody) {
        LOGGER.info("Trying to create legal entity");

        LegalEntitiesPostRequestBody requestBody = payloadConverter
            .convertAndValidate(legalEntitiesPostRequestBody, LegalEntitiesPostRequestBody.class);
        return new ResponseEntity<>(payloadConverter.convert(legalEntityService.createLegalEntity(requestBody),
            LegalEntityItemId.class), HttpStatus.CREATED);
    }

    /**
     * Controller method for updating batch legal entities.
     *
     * @param list of request body type {@link LegalEntityPut}
     * @return list of responseBody type {@link BatchResponseItem}
     */
    @Override
    @AuditEvent(eventAction = EventAction.UPDATE, objectType = AuditObjectType.LEGAL_ENTITY_BATCH)
    @Validated(SkipValidation.class)
    public ResponseEntity<List<com.backbase.accesscontrol.service.rest.spec.model.BatchResponseItem>> putLegalEntities(
        @RequestBody List<com.backbase.accesscontrol.service.rest.spec.model.LegalEntityPut> list) {
        LOGGER.info("Trying to update batch legal entities");

        List<LegalEntityPut> payload = payloadConverter.convertListPayload(list, LegalEntityPut.class);
        return new ResponseEntity<>(
            payloadConverter.convertListPayload(legalEntityService.updateBatchLegalEntities(payload),
                com.backbase.accesscontrol.service.rest.spec.model.BatchResponseItem.class), HttpStatus.MULTI_STATUS);
    }

    /**
     * Controller method for creating legal entity.
     *
     * @param createLegalEntitiesPostRequestBody request body of type {@link CreateLegalEntitiesPostRequestBody}
     * @return responseBody of type {@link CreateLegalEntitiesPostResponseBody}
     */
    @Override
    @AuditEvent(eventAction = EventAction.CREATE, objectType = AuditObjectType.LEGAL_ENTITY_ADD)
    public ResponseEntity<LegalEntityItemId> postCreateLegalEntities(
        @RequestBody LegalEntityCreateItem createLegalEntitiesPostRequestBody) {
        LOGGER.info("Trying to create legal entity");

        CreateLegalEntitiesPostRequestBody requestBody = payloadConverter
            .convertAndValidate(createLegalEntitiesPostRequestBody, CreateLegalEntitiesPostRequestBody.class);
        return new ResponseEntity<>(
            payloadConverter.convert(legalEntityService.addLegalEntity(requestBody), LegalEntityItemId.class),
            HttpStatus.CREATED);
    }

    /**
     * Controller method for getting legal entity by id.
     *
     * @param legalEntityId legal entity id.
     * @return responseBody of type {@link LegalEntityByIdGetResponseBody}
     */
    @Override
    public ResponseEntity<LegalEntityItem> getLegalEntityById(@PathVariable("legalEntityId") String legalEntityId) {
        LOGGER.info("Fetching a Legal Entity that the logged in user belongs to.");

        return new ResponseEntity<>(
            payloadConverter.convert(legalEntityService.getLegalEntityById(legalEntityId),
                LegalEntityItem.class), HttpStatus.OK);
    }

    /**
     * Controller method for fetching a {@link List {@link SubEntitiesGetResponseBody}} LegalEntities can be queried, if
     * present by Legal Entity ID.
     *
     * @param parentEntityId - id of the parent legal entity
     * @param cursor - cursor used for pagination
     * @param from - from parameter used for pagination
     * @param size - size used for pagination
     * @param query - query string used to filter data
     * @return {@link List {@link SubEntitiesGetResponseBody}}
     */
    @Override
    public ResponseEntity<List<LegalEntityItemBase>> getSubEntities(
        @RequestParam(value = "parentEntityId", required = false) String parentEntityId,
        @RequestParam(value = "cursor", required = false) String cursor,
        @RequestParam(value = "from", required = false) Integer from,
        @RequestParam(value = "size", required = false) Integer size,
        @RequestParam(value = "query", required = false) String query) {
        LOGGER.info("Fetching a list of all Legal Entity's children of the logged User");
        query = parameterValidationService.validateQueryParameter(query);
        parameterValidationService.validateFromAndSizeParameter(from, size);
        if ((from == null && size == null)) {
            from = 0;
            size = 10;
        }
        RecordsDto<SubEntitiesPostResponseBody> subLegalEntitiesDto = legalEntityFlowService
            .getSubLegalEntities(new GetLegalEntitiesRequestDto(parentEntityId, Collections.emptySet(),
                cursor, from, size, query));

        return new ResponseEntity<>(payloadConverter.convertListPayload(subLegalEntitiesDto.getRecords(),
            LegalEntityItemBase.class),
            HttpStatus.OK);
    }

    /**
     * Controller method for fetching a {@link LegalEntityByExternalIdGetResponseBody}.
     *
     * @param externalId - external id of wanted legal entity
     * @return {@link LegalEntityByExternalIdGetResponseBody}
     */
    @Override
    public ResponseEntity<LegalEntityItemBase> getLegalEntityByExternalId(
        @PathVariable("externalId") String externalId) {
        LOGGER.info("Fetching the Legal Entity with externalId {}", externalId);

        return new ResponseEntity<>(
            payloadConverter.convert(legalEntityService.getLegalEntityByExternalId(externalId),
                LegalEntityItemBase.class), HttpStatus.OK);
    }

    /**
     * Controller method for updating legal entity by Id.
     *
     * @param legalEntityByExternalIdPutRequestBody requestBody
     * @param externalId legal entity external id
     */
    @Override
    @AuditEvent(eventAction = EventAction.UPDATE, objectType = AuditObjectType.LEGAL_ENTITY)
    public ResponseEntity<Void> putLegalEntityByExternalId(@PathVariable("externalId") String externalId,
        @RequestBody LegalEntityUpdateItem legalEntityByExternalIdPutRequestBody) {
        LOGGER.info("Updating the Legal Entity with externalId {}", externalId);

        LegalEntityByExternalIdPutRequestBody requestBody = payloadConverter
            .convertAndValidate(legalEntityByExternalIdPutRequestBody, LegalEntityByExternalIdPutRequestBody.class);
        legalEntityService.updateLegalEntityByExternalId(requestBody, externalId);
        return new ResponseEntity<>(HttpStatus.OK);
    }


    /**
     * Controller method for fetching {@link MasterServiceAgreementGetResponseBody}.
     *
     * @param externalId external id of the legal entity
     * @return responseBody of type {@link MasterServiceAgreementGetResponseBody}
     */
    @Override
    public ResponseEntity<GetServiceAgreement> getMasterServiceAgreementByExternalLegalEntity(
        @PathVariable("externalId") String externalId) {
        LOGGER.info("Fetching the master service agreement for external legalEntityId {}", externalId);

        return new ResponseEntity<>(
            payloadConverter.convert(legalEntityService.getMasterServiceAgreementByExternalId(externalId),
                GetServiceAgreement.class),
            HttpStatus.OK);
    }

    /**
     * Controller method for batch delete of legal entities.
     *
     * @param presentationBatchDeleteLegalEntities requestBody
     * @return list of {@link BatchResponseItem}
     */
    @Override
    @AuditEvent(eventAction = EventAction.DELETE, objectType = AuditObjectType.LEGAL_ENTITY_BATCH)
    public ResponseEntity<List<com.backbase.accesscontrol.service.rest.spec.model.BatchResponseItem>> postLegalEntitiesBatchDelete(
        @RequestBody LegalEntitiesBatchDelete presentationBatchDeleteLegalEntities) {
        LOGGER.info("Deleting a list of Legal Entities by external ids {}",
            presentationBatchDeleteLegalEntities.getExternalIds());
        PresentationBatchDeleteLegalEntities payload = payloadConverter
            .convertAndValidate(presentationBatchDeleteLegalEntities, PresentationBatchDeleteLegalEntities.class);

        return new ResponseEntity<>(
            payloadConverter.convertListPayload(legalEntityService.batchDeleteLegalEntities(payload),
                com.backbase.accesscontrol.service.rest.spec.model.BatchResponseItem.class),
            HttpStatus.MULTI_STATUS);
    }
}
