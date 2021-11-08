package com.backbase.accesscontrol.api.service;

import com.backbase.accesscontrol.audit.AuditObjectType;
import com.backbase.accesscontrol.audit.EventAction;
import com.backbase.accesscontrol.audit.annotation.AuditEvent;
import com.backbase.accesscontrol.mappers.model.PayloadConverter;
import com.backbase.accesscontrol.service.facades.ServiceAgreementService;
import com.backbase.accesscontrol.service.rest.spec.api.ServiceAgreementsApi;
import com.backbase.accesscontrol.service.rest.spec.model.ListOfFunctionGroupsWithDataGroups;
import com.backbase.accesscontrol.service.rest.spec.model.ServiceAgreementBatchDelete;
import com.backbase.accesscontrol.service.rest.spec.model.ServiceAgreementParticipantsGetResponseBody;
import com.backbase.accesscontrol.util.UserContextUtil;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.PresentationApprovalStatus;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.PresentationDeleteServiceAgreements;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.PresentationFunctionDataGroupItems;
import java.util.List;
import javax.validation.Valid;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class ServiceAgreementsServiceApiController implements ServiceAgreementsApi {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceAgreementsServiceApiController.class);

    private ServiceAgreementService serviceAgreementService;
    private UserContextUtil userContextUtil;
    private PayloadConverter payloadConverter;

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<com.backbase.accesscontrol.service.rest.spec.model.PresentationApprovalStatus>
    putAssignUsersPermissions(@PathVariable("id") String serviceAgreementId, @PathVariable("userId") String userId,
        @RequestBody @Valid ListOfFunctionGroupsWithDataGroups listOfFunctionGroupsWithDataGroups) {

        LOGGER.info("Assigning permissions for user {}, under sa id {}, permissions {}", userId, serviceAgreementId,
            listOfFunctionGroupsWithDataGroups);
        PresentationApprovalStatus presentationApprovalStatus = serviceAgreementService
            .putAssignUsersPermissions(payloadConverter.convertAndValidate(listOfFunctionGroupsWithDataGroups,
                PresentationFunctionDataGroupItems.class), serviceAgreementId, userId);
        LOGGER.info("Permissions updated {}", presentationApprovalStatus);

        return ResponseEntity.ok(payloadConverter.convert(presentationApprovalStatus,
            com.backbase.accesscontrol.service.rest.spec.model.PresentationApprovalStatus.class));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<List<ServiceAgreementParticipantsGetResponseBody>>
    getServiceAgreementParticipants(@PathVariable("id") String serviceAgreementId) {
        LOGGER.info("Getting participants on Service Agreement {}", serviceAgreementId);

        return ResponseEntity.ok(payloadConverter
            .convertListPayload(serviceAgreementService.getServiceAgreementParticipants(serviceAgreementId),
                ServiceAgreementParticipantsGetResponseBody.class));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<List<ServiceAgreementParticipantsGetResponseBody>> getContextServiceAgreementParticipants() {
        String serviceAgreementId = userContextUtil.getServiceAgreementId();

        return ResponseEntity.ok(payloadConverter
            .convertListPayload(serviceAgreementService.getServiceAgreementParticipants(serviceAgreementId),
                ServiceAgreementParticipantsGetResponseBody.class));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @AuditEvent(eventAction = EventAction.DELETE, objectType = AuditObjectType.SERVICE_AGREEMENT_BATCH_SERVICE)
    public ResponseEntity<List<com.backbase.accesscontrol.service.rest.spec.model.BatchResponseItem>> postBatchdelete(
        @RequestBody @Valid ServiceAgreementBatchDelete serviceAgreementBatchDelete) {
        LOGGER.info("Deleting a list of Service Agreements {}", serviceAgreementBatchDelete);

        return new ResponseEntity(payloadConverter
            .convertListPayload(
                serviceAgreementService.batchDeleteServiceAgreement(
                    payloadConverter
                        .convertAndValidate(serviceAgreementBatchDelete, PresentationDeleteServiceAgreements.class)),
                com.backbase.accesscontrol.service.rest.spec.model.BatchResponseItem.class), HttpStatus.MULTI_STATUS);
    }
}
