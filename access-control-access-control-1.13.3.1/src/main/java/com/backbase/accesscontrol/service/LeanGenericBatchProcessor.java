package com.backbase.accesscontrol.service;

import static com.backbase.accesscontrol.domain.enums.ItemStatusCode.HTTP_STATUS_BAD_REQUEST;
import static com.backbase.accesscontrol.domain.enums.ItemStatusCode.HTTP_STATUS_INTERNAL_SERVER_ERROR;
import static com.backbase.accesscontrol.domain.enums.ItemStatusCode.HTTP_STATUS_NOT_FOUND;
import static com.backbase.accesscontrol.domain.enums.ItemStatusCode.HTTP_STATUS_OK;

import com.backbase.accesscontrol.domain.dto.ResponseItem;
import com.backbase.accesscontrol.domain.enums.ItemStatusCode;
import com.backbase.buildingblocks.backend.communication.event.EnvelopedEvent;
import com.backbase.buildingblocks.backend.communication.event.proxy.EventBus;
import com.backbase.buildingblocks.persistence.model.Event;
import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.buildingblocks.presentation.errors.NotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Generic batch service which performs validation of each request body and invokes a method for single item processing.
 * Catches common P&P runtime exception and creates suitable {@link ResponseItem}.
 */
public abstract class LeanGenericBatchProcessor<T, V extends ResponseItem, K> {

    private static final Logger LOGGER = LoggerFactory.getLogger(LeanGenericBatchProcessor.class);

    protected Validator validator;
    private EventBus eventBus;

    public LeanGenericBatchProcessor(Validator validator,
        EventBus eventBus) {
        this.validator = validator;
        this.eventBus = eventBus;
    }

    /**
     * Implement the logic for a single batch item.
     *
     * @param item - single request body.
     */
    protected abstract K performBatchProcess(T item);

    protected boolean sortResponse() {
        return true;
    }

    /**
     * Performs validation of each request body and invokes a method for single item processing. Catches common P&P
     * runtime exception and creates suitable {@link ResponseItem}.
     *
     * @param batchRequestBodies batch request.
     * @return list of multi-status responses.
     */
    public List<V> processBatchItems(List<T> batchRequestBodies) {
        List<V> batchResponseItems = new ArrayList<>();
        for (T item : batchRequestBodies) {
            LOGGER.info("Processing batch item");
            batchResponseItems.add(validateAndProcessSingleItem(item));
        }
        if (sortResponse()) {
            return reverseOrderByBatchResponseStatus(batchResponseItems);
        }
        return batchResponseItems;
    }

    private V validateAndProcessSingleItem(T item) {
        List<String> errorMessages = validateConstraintsForRequestBody(item);
        errorMessages.addAll(customValidateConstraintsForRequestBody(item));
        if (!errorMessages.isEmpty()) {
            return getBatchResponseItem(item, HTTP_STATUS_BAD_REQUEST, errorMessages);
        }
        return processItem(item);
    }

    @SuppressWarnings("Duplicates")
    private V processItem(T item) {
        K id = null;
        try {
            LOGGER.info("Processing single batch item and catching errors");
            id = performBatchProcess(item);
        } catch (NotFoundException e) {
            V batchResponseItem = getBatchResponseItem(item, HTTP_STATUS_NOT_FOUND,
                Collections.singletonList(e.getErrors().get(0).getMessage()));
            LOGGER.warn("Resource not found. Message: {}",  e.getMessage());
            return batchResponseItem;
        } catch (BadRequestException e) {
            V batchResponseItem = getBatchResponseItem(item, HTTP_STATUS_BAD_REQUEST,
                Collections.singletonList(e.getErrors().get(0).getMessage()));
            LOGGER.warn("Bad request for resource. Message: {}", e.getMessage());
            return batchResponseItem;

        } catch (Exception e) {
            Throwable root = ExceptionUtils.getRootCause(e);
            if (root instanceof BadRequestException) {
                V batchResponseItem = getBatchResponseItem(item, HTTP_STATUS_BAD_REQUEST,
                    Collections.singletonList(((BadRequestException) root).getErrors().get(0).getMessage()));
                LOGGER.warn("Bad request for resource. Message: {}", root.getMessage());
                return batchResponseItem;
            }
            if (root instanceof NotFoundException) {
                V batchResponseItem = getBatchResponseItem(item, HTTP_STATUS_NOT_FOUND,
                    Collections.singletonList(((NotFoundException)root).getErrors().get(0).getMessage()));
                LOGGER.warn("Resource not found. Message: {}",  e.getMessage());
                return batchResponseItem;
            }
            V batchResponseItem = getBatchResponseItem(item, HTTP_STATUS_INTERNAL_SERVER_ERROR,
                Collections.singletonList(e.getMessage()));
            LOGGER.warn("Internal server error occurred for resource. Message: {}", e.getMessage());
            return batchResponseItem;
        }
        processEvent(createEvent(item, id));
        return getBatchResponseItem(item, HTTP_STATUS_OK, new ArrayList<>());
    }

    /**
     * Perform constraint validation. Method protected to be overridden.
     *
     * @param requestBody - single request body with validation annotations.
     * @return list of errors.
     */
    private List<String> validateConstraintsForRequestBody(T requestBody) {
        return validator.validate(requestBody).stream()
            .map(this::createErrorMessageFromViolation)
            .collect(Collectors.toList());
    }

    private String createErrorMessageFromViolation(ConstraintViolation<T> violation) {
        return violation.getPropertyPath() + " " + violation.getMessage();
    }


    /**
     * Create single response item.
     *
     * @return single batch response.
     */
    protected abstract V getBatchResponseItem(T item, ItemStatusCode statusCode, List<String> errorMessages);

    /**
     * Perform constraint validation , which can not be covered with spec. Method protected to be overridden.
     *
     * @param requestItem - single request body with validation annotations.
     * @return list of errors.
     */
    protected List<String> customValidateConstraintsForRequestBody(T requestItem) {
        return new ArrayList<>();
    }

    /**
     * Create the event indicating the successful execution of the request.
     *
     * @param request The request.
     * @param internalId the internal id of the item.
     * @return The event to fire.
     */
    protected abstract Event createEvent(T request, K internalId);

    private void processEvent(Event event) {
        if(event == null ) return;
        EnvelopedEvent<Event> envelopedEvent = new EnvelopedEvent<>();
        envelopedEvent.setEvent(event);
        envelopedEvent.setOriginatorContext(null);

        eventBus.emitEvent(envelopedEvent);
    }

    private List<V> reverseOrderByBatchResponseStatus(List<V> batchResponseItems) {
        return batchResponseItems.stream()
            .sorted(Comparator.comparing(V::getStatus).reversed())
            .collect(Collectors.toList());
    }

}
