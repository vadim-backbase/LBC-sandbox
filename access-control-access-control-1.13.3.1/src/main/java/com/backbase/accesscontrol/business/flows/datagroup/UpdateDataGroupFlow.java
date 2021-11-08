package com.backbase.accesscontrol.business.flows.datagroup;

import static com.backbase.accesscontrol.util.ExceptionUtil.getBadRequestException;
import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_061;
import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_098;
import static java.util.Collections.emptySet;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import com.backbase.accesscontrol.business.datagroup.strategy.Worker;
import com.backbase.accesscontrol.business.datagroup.strategy.WorkerFactory;
import com.backbase.accesscontrol.business.flows.AbstractFlow;
import com.backbase.accesscontrol.business.service.AgreementsPersistenceService;
import com.backbase.accesscontrol.business.service.DataGroupPAndPService;
import com.backbase.accesscontrol.service.DataGroupService;
import com.backbase.accesscontrol.service.impl.PersistenceServiceAgreementService;
import com.backbase.accesscontrol.util.ExceptionUtil;
import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.serviceagreements.Participant;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.PresentationIdentifier;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.PresentationItemIdentifier;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.datagroups.PresentationSingleDataGroupPutRequestBody;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.validation.Validator;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class UpdateDataGroupFlow extends AbstractFlow<PresentationSingleDataGroupPutRequestBody, Void> {

    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateDataGroupFlow.class);

    private DataGroupService dataGroupService;
    private DataGroupPAndPService dataGroupPAndPService;
    private WorkerFactory workerFactory;
    private Validator validator;
    private AgreementsPersistenceService agreementsPersistenceService;
    private PersistenceServiceAgreementService persistenceServiceAgreementService;
    private boolean isValidationEnabled;

    /**
     * Constructor.
     *
     * @param dataGroupPAndPService              data group pandp service
     * @param workerFactory                      worker factory
     * @param validator                          validator
     * @param agreementsPersistenceService       agreement persistence service
     * @param persistenceServiceAgreementService service agreement business service
     * @param isValidationEnabled                validation flag
     */
    public UpdateDataGroupFlow(
        DataGroupService dataGroupService,
        DataGroupPAndPService dataGroupPAndPService,
        WorkerFactory workerFactory, Validator validator,
        AgreementsPersistenceService agreementsPersistenceService,
        PersistenceServiceAgreementService persistenceServiceAgreementService,
        @Value("${backbase.data-group.validation.enabled:false}") boolean isValidationEnabled) {
        this.dataGroupService = dataGroupService;
        this.dataGroupPAndPService = dataGroupPAndPService;
        this.workerFactory = workerFactory;
        this.validator = validator;
        this.agreementsPersistenceService = agreementsPersistenceService;
        this.persistenceServiceAgreementService = persistenceServiceAgreementService;
        this.isValidationEnabled = isValidationEnabled;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected final Void execute(PresentationSingleDataGroupPutRequestBody requestBody) {
        LOGGER.info("Trying to update data group {}", requestBody);

        return businessFlow(requestBody);
    }

    private Void businessFlow(PresentationSingleDataGroupPutRequestBody requestBody) {
        Worker worker = workerFactory.getWorker(requestBody.getType());

        if (!areIdentifiersAndExternalIdsCorrect(requestBody)) {
            LOGGER.warn("Invalid identifier(s): {}", requestBody);
            throw ExceptionUtil.getBadRequestException(ERR_AG_098.getErrorMessage(), ERR_AG_098.getErrorCode());
        }
        if (isNull(worker)) {
            LOGGER.info("There is no worker for this type {}.", requestBody.getType());
            externalValidation(requestBody);
        } else {
            Set<String> participantIds = emptySet();
            if (worker.isValidatingAgainstParticipants()) {
                participantIds = getParticipantIds(requestBody);
            }

            if (!requestBody.getDataItems().isEmpty()
                && nonNull(requestBody.getDataItems().get(0).getExternalIdIdentifier())) {

                String internalSaId = getServiceAgreementIdFromIdentifier(requestBody.getDataGroupIdentifier());
                List<String> internalItemIds = worker.convertToInternalIdsAndValidate(requestBody.getDataItems()
                        .stream()
                        .map(PresentationItemIdentifier::getExternalIdIdentifier).collect(Collectors.toSet()),
                    participantIds, internalSaId);
                requestBody.setDataItems(internalItemIds
                    .stream()
                    .map(item -> new PresentationItemIdentifier().withInternalIdIdentifier(item))
                    .collect(Collectors.toList()));
            } else {
                worker.validateInternalIds(
                    requestBody.getDataItems().stream().map(PresentationItemIdentifier::getInternalIdIdentifier)
                        .collect(Collectors.toSet()), participantIds);
            }
        }
        return updateDataGroupPersistence(requestBody);
    }

    private Set<String> getParticipantIds(PresentationSingleDataGroupPutRequestBody requestBody) {
        if (!isValidationEnabled) {
            return new HashSet<>();
        }
        Set<String> participantIds;
        if (nonNull(requestBody.getDataGroupIdentifier().getNameIdentifier())) {

            String externalServiceAgreementId = requestBody.getDataGroupIdentifier().getNameIdentifier()
                .getExternalServiceAgreementId();
            participantIds = agreementsPersistenceService
                .getSharingAccountsParticipantIdsForServiceAgreement(externalServiceAgreementId);
            if (CollectionUtils.isEmpty(participantIds)) {
                LOGGER.warn("No participants found for external service agreement {}", externalServiceAgreementId);
                throw getBadRequestException(ERR_AG_061.getErrorMessage(), ERR_AG_061.getErrorCode());
            }
        } else {

            String serviceAgreementId = dataGroupService.getById(requestBody.getDataGroupIdentifier().getIdIdentifier())
                .getServiceAgreementId();

            participantIds = persistenceServiceAgreementService
                .getServiceAgreementParticipants(serviceAgreementId)
                .stream()
                .filter(Participant::getSharingAccounts)
                .map(Participant::getId)
                .collect(Collectors.toSet());

        }
        return participantIds;
    }

    private String getServiceAgreementIdFromIdentifier(PresentationIdentifier identifier) {
        if (nonNull(identifier.getIdIdentifier())) {
            return dataGroupService.getById(identifier.getIdIdentifier()).getServiceAgreementId();
        } else {
            return persistenceServiceAgreementService
                .getServiceAgreementByExternalId(identifier.getNameIdentifier().getExternalServiceAgreementId())
                .getId();
        }
    }

    /**
     * This method should be overwritten when it is required the data items to be custom validated. Should throw {@link
     * BadRequestException} if some of the items are not valid.
     *
     * @param requestBody - Data group request data.
     */
    protected void externalValidation(PresentationSingleDataGroupPutRequestBody requestBody) {
        /** This method is empty */
    }

    private Void updateDataGroupPersistence(
        PresentationSingleDataGroupPutRequestBody requestBody) {

        dataGroupPAndPService.updateDataGroupPersistence(requestBody);

        return null;
    }

    private boolean areIdentifiersAndExternalIdsCorrect(PresentationSingleDataGroupPutRequestBody request) {
        return nonNull(request.getDataGroupIdentifier())
            && hasSingleIdentifier(request)
            && validator.validate(request.getDataGroupIdentifier()).isEmpty()
            && onlyInternalOrExternalIdentifiersAreSet(request);
    }

    private boolean hasSingleIdentifier(PresentationSingleDataGroupPutRequestBody request) {
        return (nonNull(request.getDataGroupIdentifier().getNameIdentifier())
            ^ nonNull(request.getDataGroupIdentifier().getIdIdentifier()))
            && (isNull(request.getDataGroupIdentifier().getNameIdentifier())
            || (nonNull(request.getDataGroupIdentifier().getNameIdentifier().getExternalServiceAgreementId())
            && nonNull(request.getDataGroupIdentifier().getNameIdentifier().getName())));
    }

    private boolean onlyInternalOrExternalIdentifiersAreSet(PresentationSingleDataGroupPutRequestBody request) {
        return request.getDataItems().isEmpty()
            || (request.getDataItems()
            .stream().allMatch(item -> nonNull(item) && nonNull(item.getInternalIdIdentifier()))
            ^ request.getDataItems().stream()
            .allMatch(item -> nonNull(item) && nonNull(item.getExternalIdIdentifier())));

    }

}
