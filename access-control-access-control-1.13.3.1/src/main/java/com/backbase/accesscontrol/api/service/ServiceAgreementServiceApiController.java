package com.backbase.accesscontrol.api.service;

import com.backbase.accesscontrol.audit.AuditObjectType;
import com.backbase.accesscontrol.audit.EventAction;
import com.backbase.accesscontrol.audit.annotation.AuditEvent;
import com.backbase.accesscontrol.dto.RecordsDto;
import com.backbase.accesscontrol.dto.UserAssignedFunctionGroupDto;
import com.backbase.accesscontrol.mappers.model.PayloadConverter;
import com.backbase.accesscontrol.service.ParameterValidationService;
import com.backbase.accesscontrol.service.facades.ServiceAgreementFlowService;
import com.backbase.accesscontrol.service.facades.ServiceAgreementService;
import com.backbase.accesscontrol.service.rest.spec.api.ServiceAgreementApi;
import com.backbase.accesscontrol.service.rest.spec.model.IdItem;
import com.backbase.accesscontrol.service.rest.spec.model.PresentationParticipantBatchUpdate;
import com.backbase.accesscontrol.service.rest.spec.model.PresentationServiceAgreementUsersBatchUpdate;
import com.backbase.accesscontrol.service.rest.spec.model.ServiceAgreementItem;
import com.backbase.accesscontrol.service.rest.spec.model.ServiceAgreementPut;
import com.backbase.accesscontrol.service.rest.spec.model.ServicesAgreementIngest;
import com.backbase.accesscontrol.service.rest.spec.model.UserAssignedFunctionGroupResponse;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreement.PresentationParticipantsPut;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.PresentationServiceAgreementUsersUpdate;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementIngestPostRequestBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementIngestPostResponseBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementPutRequestBody;
import java.util.List;
import javax.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@AllArgsConstructor
public class ServiceAgreementServiceApiController implements ServiceAgreementApi {

    private ServiceAgreementService serviceAgreementService;
    private PayloadConverter payloadConverter;
    private ParameterValidationService parameterValidationService;
    private ServiceAgreementFlowService serviceAgreementFlowService;
    private static final String TOTAL_COUNT_HEADER = "X-Total-Count";

    /**
     * {@inheritDoc}
     */
    @Override
    @AuditEvent(eventAction = EventAction.UPDATE, objectType = AuditObjectType.SERVICE_AGREEMENT_SERVICE)
    public ResponseEntity<Void> putServiceAgreementItem(@PathVariable("serviceAgreementId") String serviceAgreementId,
        @RequestBody @Valid ServiceAgreementPut serviceAgreementPut
    ) {

        log.debug("Updating service agreement with id {}", serviceAgreementId);
        serviceAgreementService
            .updateServiceAgreement(payloadConverter.convertAndValidate(serviceAgreementPut,
                ServiceAgreementPutRequestBody.class), serviceAgreementId);
        return ResponseEntity.ok().build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @AuditEvent(eventAction = EventAction.UPDATE_PARTICIPANTS,
        objectType = AuditObjectType.SERVICE_AGREEMENT_PARTICIPANTS_UPDATE)
    public ResponseEntity<List<com.backbase.accesscontrol.service.rest.spec.model.BatchResponseItemExtended>>
    putPresentationIngestServiceAgreementParticipants(
        @RequestBody @Valid PresentationParticipantBatchUpdate presentationParticipantBatchUpdate) {

        log.debug("Updating participants in service agreement of service agreement in batch.");
        return new ResponseEntity<>(payloadConverter.convertListPayload(
            serviceAgreementService
                .updateParticipants(payloadConverter.convertAndValidate(presentationParticipantBatchUpdate,
                    PresentationParticipantsPut.class)),
            com.backbase.accesscontrol.service.rest.spec.model.BatchResponseItemExtended.class),
            HttpStatus.MULTI_STATUS);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @AuditEvent(eventAction = EventAction.UPDATE_ADMINS,
        objectType = AuditObjectType.SERVICE_AGREEMENT_ADMINS_UPDATE_BATCH)
    public ResponseEntity<List<com.backbase.accesscontrol.service.rest.spec.model.BatchResponseItemExtended>>
    putPresentationServiceAgreementAdminsBatchUpdate(@RequestBody @Valid PresentationServiceAgreementUsersBatchUpdate
        presentationServiceAgreementUsersBatchUpdate) {
        log.debug("Updating admins in service agreement in batch.");
        return new ResponseEntity<>(payloadConverter.convertListPayload(
            serviceAgreementService
                .updateServiceAgreementAdminsBatch(
                    payloadConverter.convertAndValidate(presentationServiceAgreementUsersBatchUpdate,
                        PresentationServiceAgreementUsersUpdate.class)),
            com.backbase.accesscontrol.service.rest.spec.model.BatchResponseItemExtended.class),
            HttpStatus.MULTI_STATUS);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @AuditEvent(eventAction = EventAction.UPDATE_USERS,
        objectType = AuditObjectType.SERVICE_AGREEMENT_USERS_UPDATE_BATCH)
    public ResponseEntity<List<com.backbase.accesscontrol.service.rest.spec.model.BatchResponseItemExtended>>
    putPresentationServiceAgreementUsersBatchUpdate(
        @RequestBody @Valid PresentationServiceAgreementUsersBatchUpdate
            presentationServiceAgreementUsersBatchUpdate) {
        log.debug("Updating users in service agreement in batch.");
        return new ResponseEntity<>(payloadConverter.convertListPayload(
            serviceAgreementService
                .updateUsersInServiceAgreement(
                    payloadConverter.convertAndValidate(presentationServiceAgreementUsersBatchUpdate,
                        PresentationServiceAgreementUsersUpdate.class)),
            com.backbase.accesscontrol.service.rest.spec.model.BatchResponseItemExtended.class),
            HttpStatus.MULTI_STATUS);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @AuditEvent(eventAction = EventAction.CREATE, objectType = AuditObjectType.SERVICE_AGREEMENT_SERVICE)
    public ResponseEntity<IdItem> postServiceAgreementIngest(
        @RequestBody @Valid ServicesAgreementIngest servicesAgreementIngest) {
        log.debug("Ingesting service agreement.");

        ServiceAgreementIngestPostResponseBody serviceAgreementIngestPostResponseBody = serviceAgreementService
            .ingestServiceAgreement(payloadConverter
                .convertAndValidate(servicesAgreementIngest, ServiceAgreementIngestPostRequestBody.class));

        log.debug("Service agreement ingested.");
        return new ResponseEntity<>(
            payloadConverter.convertAndValidate(serviceAgreementIngestPostResponseBody, IdItem.class),
            HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<List<UserAssignedFunctionGroupResponse>> getUsers(String serviceAgreementId,
        String functionGroupId, @Valid Integer from, @Valid Integer size) {
        log.debug("Fetching assigned users in serviceAgreement {} by functionGroupId {}",
            serviceAgreementId, functionGroupId);

        parameterValidationService.validateFromAndSizeParameter(from, size);

        RecordsDto<com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.UserAssignedFunctionGroupResponse> assignedUsers = serviceAgreementFlowService
            .getUsersWithAssignedFunctionGroup(
                new UserAssignedFunctionGroupDto(serviceAgreementId, functionGroupId, from, size));

        return ResponseEntity.ok()
            .header(TOTAL_COUNT_HEADER, String.valueOf(assignedUsers.getTotalNumberOfRecords()))
            .body(payloadConverter
                .convertListPayload(assignedUsers.getRecords(), UserAssignedFunctionGroupResponse.class));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<ServiceAgreementItem> getServiceAgreementExternalId(
        @PathVariable("externalId") String externalId) {

        log.debug("Get Service Agreement by external id {}", externalId);
        return ResponseEntity
            .ok(payloadConverter.convert(serviceAgreementService.getServiceAgreementByExternalId(externalId),
                ServiceAgreementItem.class));
    }
}
