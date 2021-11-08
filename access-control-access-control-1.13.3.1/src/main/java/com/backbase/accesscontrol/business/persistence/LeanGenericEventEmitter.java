package com.backbase.accesscontrol.business.persistence;

import com.backbase.accesscontrol.dto.parameterholder.GenericParameterHolder;
import com.backbase.buildingblocks.backend.communication.event.EnvelopedEvent;
import com.backbase.buildingblocks.backend.communication.event.proxy.EventBus;
import com.backbase.buildingblocks.persistence.model.Event;
import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.buildingblocks.presentation.errors.InternalServerErrorException;
import com.backbase.buildingblocks.presentation.errors.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generic event emitter template.
 *
 * @param <T> The parameter holder. Should contain all path and query parameters.
 * @param <U> The request body.
 * @param <V> The output type of the request handling.
 */
public abstract class LeanGenericEventEmitter<T extends GenericParameterHolder, U, V> {

    private static final Logger LOGGER = LoggerFactory.getLogger(LeanGenericEventEmitter.class);

    protected EventBus eventBus;

    public LeanGenericEventEmitter(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    /**
     * Execute the actual request.
     *
     * @param requestData the request
     * @return The result of the request.
     */
    protected abstract V executeRequest(T parameterHolder, U requestData);

    /**
     * Create the event indicating the successful execution of the request.
     *
     * @param request  The request.
     * @param response result from the executed request.
     * @return The event to fire.
     */
    protected abstract Event createSuccessEvent(T parameterHolder, U request, V response);

    /**
     * Create the event indicating the successful execution of the request.
     *
     * @param request The request.
     * @param failure The exception that was thrown during request execution.
     * @return The event to fire.
     */
    protected abstract Event createFailureEvent(T parameterHolder, U request, Exception failure);

    /**
     * Template method to handle the request.
     *
     * @param requestDto  an object that extends {@link GenericParameterHolder} which contains path variables and query
     *                    params
     * @param requestData the requested data
     * @return a result of the processing the request.
     */
    @SuppressWarnings("squid:S2139")
    public V handleRequest(T requestDto, U requestData) {
        try {
            V response = executeRequest(requestDto, requestData);
            processEvent(createSuccessEvent(requestDto, requestData, response));
            return response;
        } catch (BadRequestException | NotFoundException e) {
            LOGGER.warn("Exception executing the process.", e);
            processEvent(createFailureEvent(requestDto, requestData, e));
            throw e;
        } catch (InternalServerErrorException e) {
            LOGGER.warn("Runtime exception while executing the process.", e);
            processEvent(createFailureEvent(requestDto, requestData, e));
            throw e;
        }
    }

    private void processEvent(Event event) {
        if (event == null) {
            return;
        }
        EnvelopedEvent<Event> envelopedEvent = new EnvelopedEvent<>();
        envelopedEvent.setEvent(event);
        envelopedEvent.setOriginatorContext(null);

        eventBus.emitEvent(envelopedEvent);
    }
}

