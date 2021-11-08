package com.backbase.accesscontrol.business.serviceagreement.participant;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

import com.backbase.accesscontrol.business.batch.BatchProcessItemOrderProcessor;
import com.backbase.accesscontrol.business.batch.InvalidParticipantItem;
import com.backbase.accesscontrol.business.batch.ProcessableBatchBody;
import com.backbase.accesscontrol.mappers.BatchResponseItemExtendedMapper;
import com.backbase.accesscontrol.service.batch.serviceagreement.UpdateServiceAgreementParticipant;
import com.backbase.buildingblocks.presentation.errors.InternalServerErrorException;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.BatchResponseItemExtended;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.BatchResponseStatusCode;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreement.PresentationParticipantPutBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreement.PresentationParticipantsPut;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import lombok.AllArgsConstructor;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class IngestParticipantUpdateProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(IngestParticipantUpdateProcessor.class);

    private BatchProcessItemOrderProcessor batchProcessItemOrderProcessor;
    private IngestParticipantUpdateRemoveProcessor ingestParticipantRemoveProcessor;
    private Validator validator;
    private UpdateServiceAgreementParticipant updateServiceAgreementParticipant;
    private BatchResponseItemExtendedMapper batchResponseItemMapperExtended;

    /**
     * Process request for updating participants on service agreement in batch.
     *
     * @param data contains participants to be updated
     * @return BusinessProcessResult of list of {@link BatchResponseItemExtended}
     */
    public List<BatchResponseItemExtended> processParticipantUpdate(
        PresentationParticipantsPut data) {

        List<ProcessableBatchBody<PresentationParticipantPutBody>> processableBatchBodies =
            batchProcessItemOrderProcessor.transformProcessableBody(data.getParticipants());
        validateRequestBodies(processableBatchBodies);
        List<InvalidParticipantItem> invalidParticipantItems = ingestParticipantRemoveProcessor
            .processItems(getUnprocessedElements(processableBatchBodies));

        Map<Integer, List<String>> processedRemoveItems = getProcessedItems(invalidParticipantItems);
        processUnprocessedParticipants(processableBatchBodies, processedRemoveItems);
        processableBatchBodies
            .stream()
            .filter(item -> processedRemoveItems.containsKey(item.getOrder()))
            .forEach(item -> {
                BatchResponseItemExtended response = createResponse(processedRemoveItems, item);
                item.setResponse(response);
            });
        return processableBatchBodies.stream()
            .sorted(Comparator.comparing(ProcessableBatchBody::getOrder))
            .map(ProcessableBatchBody::getResponse)
            .collect(toList());
    }

    private BatchResponseItemExtended createResponse(Map<Integer, List<String>> processedRemoveItems,
        ProcessableBatchBody<PresentationParticipantPutBody> item) {
        return new BatchResponseItemExtended()
            .withAction(item.getItem().getAction())
            .withExternalServiceAgreementId(item.getItem().getExternalServiceAgreementId())
            .withResourceId(item.getItem().getExternalParticipantId())
            .withErrors(processedRemoveItems.get(item.getOrder()))
            .withStatus(BatchResponseStatusCode.HTTP_STATUS_BAD_REQUEST);
    }

    private void validateRequestBodies(
        List<ProcessableBatchBody<PresentationParticipantPutBody>> procesableBatchBodies) {
        LOGGER.info("Validating request bodies, {}", procesableBatchBodies);
        for (ProcessableBatchBody<PresentationParticipantPutBody> processableBatchBody : procesableBatchBodies) {
            Set<ConstraintViolation<PresentationParticipantPutBody>> validate = validator
                .validate(processableBatchBody.getItem());
            if (CollectionUtils.isNotEmpty(validate)) {
                LOGGER.warn("Invalid body, {} with validations {}", processableBatchBody, validate);
                processableBatchBody.setResponse(
                    createBadResponse(validate, processableBatchBody.getItem())
                );
            }
        }
    }

    private void processUnprocessedParticipants(
        List<ProcessableBatchBody<PresentationParticipantPutBody>> procesableBatchBodies,
        Map<Integer, List<String>> processedRemoveItems) {
        List<ProcessableBatchBody<PresentationParticipantPutBody>> persistenceProcessableItems =
            getPersistenceProcessableItems(
                procesableBatchBodies, processedRemoveItems);
        List<BatchResponseItemExtended> responseItemExtended;
        if (persistenceProcessableItems.isEmpty()) {
            responseItemExtended = new ArrayList<>();
        } else {
            responseItemExtended = batchResponseItemMapperExtended.mapList(updateServiceAgreementParticipant
                .processBatchItems(persistenceProcessableItems.stream()
                    .map(ProcessableBatchBody::getItem)
                    .collect(toList())));
        }
        if (persistenceProcessableItems.size() != responseItemExtended.size()) {
            LOGGER.warn("The items sent to be process and returned are not with the same size");
            throw new InternalServerErrorException("Unexpected process objects size");
        }
        mergerRequests(persistenceProcessableItems, responseItemExtended);
    }

    private void mergerRequests(List<ProcessableBatchBody<PresentationParticipantPutBody>> persistenceProcessableItems,
        List<BatchResponseItemExtended> batchResponseItemExtendeds) {
        int i = 0;
        for (ProcessableBatchBody<PresentationParticipantPutBody> itemExtendedProcessableBatchBody :
            persistenceProcessableItems) {
            itemExtendedProcessableBatchBody.setResponse(batchResponseItemExtendeds.get(i));
            i++;
        }
    }

    private List<ProcessableBatchBody<PresentationParticipantPutBody>> getPersistenceProcessableItems(
        List<ProcessableBatchBody<PresentationParticipantPutBody>> procesableBatchBodies,
        Map<Integer, List<String>> processedRemoveItems) {
        return procesableBatchBodies
            .stream()
            .filter(item -> isUnprocessed(processedRemoveItems, item))
            .filter(item -> Objects.isNull(item.getResponse()))
            .collect(Collectors.toList());
    }

    private List<ProcessableBatchBody<PresentationParticipantPutBody>> getUnprocessedElements(
        List<ProcessableBatchBody<PresentationParticipantPutBody>> procesableBatchBodies) {
        LOGGER.info("Retrieving list of unprocessed bodies {}", procesableBatchBodies);
        List<ProcessableBatchBody<PresentationParticipantPutBody>> unprocessedBodies = procesableBatchBodies
            .stream()
            .filter(batchBody -> Objects.isNull(batchBody.getResponse()))
            .collect(Collectors.toList());
        LOGGER.info("Retrieved unprocessed bodies {}", unprocessedBodies);
        return unprocessedBodies;
    }

    private BatchResponseItemExtended createBadResponse(
        Set<ConstraintViolation<PresentationParticipantPutBody>> validate, PresentationParticipantPutBody item) {
        return new BatchResponseItemExtended()
            .withStatus(BatchResponseStatusCode.HTTP_STATUS_BAD_REQUEST)
            .withErrors(getViolationMessages(validate))
            .withAction(item.getAction())
            .withResourceId(item.getExternalParticipantId())
            .withExternalServiceAgreementId(item.getExternalServiceAgreementId());
    }

    private List<String> getViolationMessages(Set<ConstraintViolation<PresentationParticipantPutBody>> validate) {
        return validate
            .stream()
            .map(violation -> violation.getPropertyPath() + " " + violation.getMessage())
            .collect(Collectors.toList());
    }

    private Map<Integer, List<String>> getProcessedItems(List<InvalidParticipantItem> invalidParticipantItems) {
        return invalidParticipantItems.stream()
            .collect(groupingBy(InvalidParticipantItem::getOrder, toUniqueList()));
    }

    private boolean isUnprocessed(Map<Integer, List<String>> processedItems,
        ProcessableBatchBody<PresentationParticipantPutBody> item) {
        return !processedItems.containsKey(item.getOrder());
    }

    private Collector<InvalidParticipantItem, List<String>, List<String>> toUniqueList() {
        return Collector.of(
            ArrayList::new,
            (errors, participantItem) -> errors.addAll(participantItem.getErrors()),
            (errors, additionalErrors) -> Stream.of(errors, additionalErrors)
                .flatMap(Collection::stream)
                .distinct()
                .collect(Collectors.toList())
        );
    }


}