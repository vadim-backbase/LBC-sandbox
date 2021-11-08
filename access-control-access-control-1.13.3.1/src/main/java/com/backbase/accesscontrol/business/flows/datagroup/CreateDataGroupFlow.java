package com.backbase.accesscontrol.business.flows.datagroup;

import static com.backbase.accesscontrol.util.ExceptionUtil.getBadRequestException;
import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_100;
import static java.util.Collections.emptySet;

import com.backbase.accesscontrol.business.datagroup.strategy.Worker;
import com.backbase.accesscontrol.business.datagroup.strategy.WorkerFactory;
import com.backbase.accesscontrol.business.flows.AbstractFlow;
import com.backbase.accesscontrol.business.persistence.datagroup.AddDataGroupHandler;
import com.backbase.accesscontrol.business.service.AgreementsPersistenceService;
import com.backbase.accesscontrol.configuration.ValidationConfig;
import com.backbase.accesscontrol.dto.parameterholder.EmptyParameterHolder;
import com.backbase.accesscontrol.service.impl.PersistenceServiceAgreementService;
import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.serviceagreements.Participant;
import com.backbase.presentation.accessgroup.event.spec.v1.DataGroupBase;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.datagroups.DataGroupsPostResponseBody;
import com.google.common.base.Strings;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class CreateDataGroupFlow extends AbstractFlow<DataGroupBase, DataGroupsPostResponseBody> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CreateDataGroupFlow.class);

    private WorkerFactory workerFactory;
    private ValidationConfig validationConfig;
    private AgreementsPersistenceService agreementsPersistenceService;
    private PersistenceServiceAgreementService persistenceServiceAgreementService;
    private AddDataGroupHandler addDataGroupHandler;

    /**
     * {@inheritDoc}
     */
    @Override
    protected DataGroupsPostResponseBody execute(DataGroupBase dataGroupBase) {
        LOGGER.info("Trying to save data group {}", dataGroupBase);

        return businessFlow(dataGroupBase);
    }

    /**
     * This method should be overwritten when it is required the data items to be custom validated. Should throw {@link
     * BadRequestException} if some of the items are not valid.
     *
     * @param dataGroupBase - Data group request data.
     */
    protected void externalValidation(DataGroupBase dataGroupBase) {
        /** This method is empty */
    }

    private DataGroupsPostResponseBody businessFlow(DataGroupBase dataGroupBase) {

        validateServiceAgreement(dataGroupBase);

        validationConfig.validateDataGroupType(dataGroupBase.getType());

        Worker worker = workerFactory.getWorker(dataGroupBase.getType());

        if (Objects.isNull(worker)) {
            LOGGER.info("There is no worker for this type {}.", dataGroupBase.getType());
            externalValidation(dataGroupBase);
        } else {

            Set<String> items = new HashSet<>(dataGroupBase.getItems());
            Set<String> participantIds = emptySet();
            String externalServiceAgreementId = dataGroupBase.getExternalServiceAgreementId();

            if (worker.isValidatingAgainstParticipants()) {
                if (Objects.nonNull(externalServiceAgreementId)) {
                    participantIds = agreementsPersistenceService
                        .getSharingAccountsParticipantIdsForServiceAgreement(externalServiceAgreementId);

                } else {
                    participantIds = persistenceServiceAgreementService
                        .getServiceAgreementParticipants(dataGroupBase.getServiceAgreementId())
                        .stream()
                        .filter(Participant::getSharingAccounts)
                        .map(Participant::getId)
                        .collect(Collectors.toSet());
                }
            }

            if (dataGroupBase.getAreItemsInternalIds()) {
                worker.validateInternalIds(items, participantIds);
            } else {
                String internalSaId = dataGroupBase.getServiceAgreementId();
                if (Objects.nonNull(externalServiceAgreementId)) {
                    internalSaId = persistenceServiceAgreementService
                        .getServiceAgreementByExternalId(externalServiceAgreementId).getId();
                }
                List<String> validatedInternalIds = worker
                    .convertToInternalIdsAndValidate(items, participantIds, internalSaId);
                dataGroupBase.getItems().clear();
                dataGroupBase.getItems().addAll(validatedInternalIds);
            }
        }

        return addDataGroupHandler.handleRequest(new EmptyParameterHolder(), dataGroupBase);
    }


    private void validateServiceAgreement(DataGroupBase dataGroupBase) {
        if (Strings.isNullOrEmpty(dataGroupBase.getServiceAgreementId())
            && Strings.isNullOrEmpty(dataGroupBase.getExternalServiceAgreementId())) {
            LOGGER.warn("Data group doesn't have service agreement id or external service agreement id");
            throw getBadRequestException(ERR_AG_100.getErrorMessage(), ERR_AG_100.getErrorCode());
        }
    }
}
