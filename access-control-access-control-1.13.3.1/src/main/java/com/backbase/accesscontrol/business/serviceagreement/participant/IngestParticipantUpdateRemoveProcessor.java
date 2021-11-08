package com.backbase.accesscontrol.business.serviceagreement.participant;

import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_087;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toSet;

import com.backbase.accesscontrol.business.batch.DataItemValidatableItem;
import com.backbase.accesscontrol.business.batch.InvalidParticipantItem;
import com.backbase.accesscontrol.business.batch.ProcessableBatchBody;
import com.backbase.accesscontrol.business.service.AgreementsPersistenceService;
import com.backbase.accesscontrol.business.serviceagreement.participant.validation.IngestParticipantUpdateRemoveDataValidationProcessor;
import com.backbase.accesscontrol.dto.PersistenceDataGroupExtendedItemDto;
import com.backbase.accesscontrol.dto.PersistenceExtendedParticipant;
import com.backbase.accesscontrol.service.DataGroupService;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.PresentationAction;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreement.PresentationParticipantPutBody;
import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class IngestParticipantUpdateRemoveProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(IngestParticipantUpdateRemoveProcessor.class);

    private AgreementsPersistenceService agreementsPersistenceService;
    private DataGroupService dataGroupService;
    private IngestParticipantUpdateRemoveDataValidationProcessor ingestParticipantUpdateRemoveDataValidationProcessor;
    @Value("${backbase.approval.validation.enabled:false}")
    private boolean includePendingDataGroups;

    public IngestParticipantUpdateRemoveProcessor(
        AgreementsPersistenceService agreementsPersistenceService,
        DataGroupService dataGroupService,
        IngestParticipantUpdateRemoveDataValidationProcessor ingestParticipantUpdateRemoveDataValidationProcessor) {
        this.agreementsPersistenceService = agreementsPersistenceService;
        this.dataGroupService = dataGroupService;
        this.ingestParticipantUpdateRemoveDataValidationProcessor = ingestParticipantUpdateRemoveDataValidationProcessor;
    }

    /**
     * Process participants to be updated.
     *
     * @param processableBatchBodies contains list of participants to be processed
     * @return list of {@link InvalidParticipantItem}
     */
    public List<InvalidParticipantItem> processItems(
        List<ProcessableBatchBody<PresentationParticipantPutBody>> processableBatchBodies) {
        List<ProcessableBatchBody<PresentationParticipantPutBody>> removeItems = getItemsToRemove(
            processableBatchBodies);
        Map<String, Set<PersistenceExtendedParticipant>> participantsPerExternalId = agreementsPersistenceService
            .getParticipantsPerExternalId(getServiceAgreementExternalIds(removeItems));

        List<InvalidParticipantItem> invalidParticipantItems = ingestParticipantUpdateRemoveDataValidationProcessor
            .processValidateParticipants(getDataItemValidatableItems(removeItems, participantsPerExternalId));

        return Stream.of(getNotExistingParticipants(removeItems, participantsPerExternalId), invalidParticipantItems)
            .flatMap(Collection::stream)
            .collect(Collectors.toList());
    }

    private List<DataItemValidatableItem> getDataItemValidatableItems(
        List<ProcessableBatchBody<PresentationParticipantPutBody>> removeItems,
        Map<String, Set<PersistenceExtendedParticipant>> participantsPerExternalId) {
        Map<String, Set<PersistenceDataGroupExtendedItemDto>> externalServiceAgreementDataGroupMap =
            getDataGroupsPerServiceAgreement(
                getServicesAgreementsOfParticipantsThatShareAccounts(participantsPerExternalId, removeItems),
                includePendingDataGroups);
        return removeItems
            .stream()
            .filter(participant -> hasDataGroups(externalServiceAgreementDataGroupMap,
                participant.getItem().getExternalServiceAgreementId()))
            .map(participant -> createDataItemValidatableItem(participantsPerExternalId,
                externalServiceAgreementDataGroupMap, participant))
            .filter(this::isValidDataItem)
            .collect(Collectors.toList());
    }

    private Map<String, Set<PersistenceDataGroupExtendedItemDto>> getDataGroupsPerServiceAgreement(
        Set<String> serviceAgreementExternalIds, boolean includePendingDataGroups) {
        if (CollectionUtils.isEmpty(serviceAgreementExternalIds)) {
            return new HashMap<>();
        }

        List<PersistenceDataGroupExtendedItemDto> persistenceDataGroupExtendedItems =
            dataGroupService
                .getDataGroupsByExternalServiceAgreementIds(serviceAgreementExternalIds, includePendingDataGroups);

        return persistenceDataGroupExtendedItems
            .stream()
            .collect(getPersistenceDataGroupExtendedItemMapCollector());
    }

    private Collector<PersistenceDataGroupExtendedItemDto,
        ?, Map<String, Set<PersistenceDataGroupExtendedItemDto>>> getPersistenceDataGroupExtendedItemMapCollector() {
        return groupingBy(PersistenceDataGroupExtendedItemDto::getExternalServiceAgreementId,
            mapping(item -> item, toSet()));
    }

    private boolean isValidDataItem(DataItemValidatableItem dataItemValidatableItem) {
        return Objects.nonNull(dataItemValidatableItem.getPersistenceExtendedParticipant());
    }

    private DataItemValidatableItem createDataItemValidatableItem(
        Map<String, Set<PersistenceExtendedParticipant>> participantsPerExternalId,
        Map<String, Set<PersistenceDataGroupExtendedItemDto>> externalServiceaAgreementDataGroupMap,
        ProcessableBatchBody<PresentationParticipantPutBody> participant) {
        return new DataItemValidatableItem(
            participant,
            getParticipantForGivenServiceAgreement(participantsPerExternalId, participant).orElse(null),
            externalServiceaAgreementDataGroupMap.get(participant.getItem().getExternalServiceAgreementId())
        );
    }

    private Set<String> getServicesAgreementsOfParticipantsThatShareAccounts(
        Map<String, Set<PersistenceExtendedParticipant>> participantsPerExternalId,
        List<ProcessableBatchBody<PresentationParticipantPutBody>> removeItems) {
        return participantsPerExternalId
            .entrySet()
            .stream()
            .flatMap(pair -> pair.getValue().stream())
            .filter(p -> isServiceAgreementOfParticipantSharingAccounts(removeItems, p))
            .map(PersistenceExtendedParticipant::getExternalServiceAgreementId)
            .collect(Collectors.toSet());
    }

    private boolean isServiceAgreementOfParticipantSharingAccounts(
        List<ProcessableBatchBody<PresentationParticipantPutBody>> removeItems,
        PersistenceExtendedParticipant participant) {
        return removeItems.stream()
            .map(ProcessableBatchBody::getItem)
            .anyMatch(re -> participant.getExternalServiceAgreementId().equals(re.getExternalServiceAgreementId())
                && participant.getExternalId().equals(re.getExternalParticipantId())
            )
            && participant.isSharingAccounts();
    }

    private List<InvalidParticipantItem> getNotExistingParticipants(
        List<ProcessableBatchBody<PresentationParticipantPutBody>> removeItems,
        Map<String, Set<PersistenceExtendedParticipant>> participantsPerExternalId) {
        return removeItems
            .stream()
            .filter(removeItem -> isNotPresent(participantsPerExternalId, removeItem))
            .map(this::createInvalidParticipantItem)
            .collect(Collectors.toList());
    }

    private InvalidParticipantItem createInvalidParticipantItem(
        ProcessableBatchBody<PresentationParticipantPutBody> invalidParticipantItem) {
        return new InvalidParticipantItem(invalidParticipantItem.getOrder(),
            Lists.newArrayList(ERR_AG_087.getErrorMessage()));
    }

    private boolean hasDataGroups(
        Map<String, Set<PersistenceDataGroupExtendedItemDto>> externalServiceAgreementDataGroup,
        String externalServiceAgreementId) {
        return !getDataGroupItems(externalServiceAgreementDataGroup, externalServiceAgreementId).isEmpty();

    }

    private Set<PersistenceDataGroupExtendedItemDto> getDataGroupItems(
        Map<String, Set<PersistenceDataGroupExtendedItemDto>> externalServiceAgreementDataGroupMap,
        String externalServiceAgreementId) {
        return Optional.ofNullable(externalServiceAgreementDataGroupMap.get(externalServiceAgreementId))
            .orElse(Collections.emptySet());
    }

    private boolean isNotPresent(Map<String, Set<PersistenceExtendedParticipant>> participantsPerExternalId,
        ProcessableBatchBody<PresentationParticipantPutBody> removeItem) {
        return !isPresent(participantsPerExternalId, removeItem);
    }

    private boolean isPresent(Map<String, Set<PersistenceExtendedParticipant>> participantsPerExternalId,
        ProcessableBatchBody<PresentationParticipantPutBody> removeItem) {
        return getParticipantForGivenServiceAgreement(participantsPerExternalId, removeItem).isPresent();
    }


    private Optional<PersistenceExtendedParticipant> getParticipantForGivenServiceAgreement(
        Map<String, Set<PersistenceExtendedParticipant>> participantsPerExternalId,
        ProcessableBatchBody<PresentationParticipantPutBody> batchBody) {
        return Optional.ofNullable(participantsPerExternalId.get(batchBody.getItem().getExternalParticipantId()))
            .orElseGet(HashSet::new)
            .stream()
            .filter(p -> p.getExternalServiceAgreementId().equals(batchBody.getItem().getExternalServiceAgreementId()))
            .findFirst();
    }


    private List<ProcessableBatchBody<PresentationParticipantPutBody>> getItemsToRemove(
        List<ProcessableBatchBody<PresentationParticipantPutBody>> procesableBatchBodies) {
        LOGGER.info("Filtering remove bodies {}", procesableBatchBodies);
        List<ProcessableBatchBody<PresentationParticipantPutBody>> removeBodies = procesableBatchBodies.stream()
            .filter(batchBody -> PresentationAction.REMOVE.equals(batchBody.getItem().getAction()))
            .collect(Collectors.toList());
        LOGGER.info("Filtered remove bodies {}", removeBodies);
        return removeBodies;
    }

    private Set<String> getServiceAgreementExternalIds(
        List<ProcessableBatchBody<PresentationParticipantPutBody>> removeItems) {
        return removeItems.stream()
            .map(ProcessableBatchBody::getItem)
            .map(PresentationParticipantPutBody::getExternalServiceAgreementId)
            .collect(toSet());
    }
}