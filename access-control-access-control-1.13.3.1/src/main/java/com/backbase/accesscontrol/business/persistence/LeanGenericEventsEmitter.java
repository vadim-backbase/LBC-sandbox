package com.backbase.accesscontrol.business.persistence;

import com.backbase.accesscontrol.dto.parameterholder.GenericParameterHolder;
import com.backbase.buildingblocks.backend.communication.event.EnvelopedEvent;
import com.backbase.buildingblocks.backend.communication.event.proxy.EventBus;
import com.backbase.buildingblocks.persistence.model.Event;
import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.buildingblocks.presentation.errors.InternalServerErrorException;
import com.backbase.buildingblocks.presentation.errors.NotFoundException;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generic events emitter template.
 *
 * @param <T> The parameter holder. Should contain all path and query parameters.
 * @param <U> The request body.
 * @param <V> The output type of the request handling.
 */
public abstract class LeanGenericEventsEmitter<T extends GenericParameterHolder,U,V> extends LeanGenericEventEmitter<T,U,V> {

    public LeanGenericEventsEmitter(EventBus eventBus) {
        super(eventBus);
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(LeanGenericEventsEmitter.class);

    /**
     * Create the list of events indicating the successful execution of the request.
     *
     * @param request  The request.
     * @param response result from the executed request.
     * @return The events to fire.
     */
    protected abstract List<Event> createSuccessEvents(T parameterHolder, U request, V response);

    /**
     * Template method to handle the request.
     *
     * @param requestDto  an object that extends {@link GenericParameterHolder} which contains path variables and query
     *                    params
     * @param requestData the requested data
     * @return a result of the processing the request.
     */
    @SuppressWarnings("squid:S2139")
    @Override
    public V handleRequest(T requestDto, U requestData) {
        try {
            V response = executeRequest(requestDto, requestData);
            processEvent(createSuccessEvents(requestDto, requestData, response));
            return response;
        } catch (BadRequestException | NotFoundException e) {
            LOGGER.warn("Exception executing the process.", e);
            processEvent(Collections.singletonList(createFailureEvent(requestDto, requestData, e)));
            throw e;
        } catch (InternalServerErrorException e) {
            LOGGER.warn("Runtime exception while executing the process.", e);
            processEvent(Collections.singletonList(createFailureEvent(requestDto, requestData, e)));
            throw e;
        }
    }

    private void processEvent(List<Event> events) {
        if (events.isEmpty()) {
            return;
        }
        
        for (Event event : events) {
            if (event == null) {
                continue;
            }
            EnvelopedEvent<Event> envelopedEvent = new EnvelopedEvent<>();
            envelopedEvent.setEvent(event);
            envelopedEvent.setOriginatorContext(null);

            eventBus.emitEvent(envelopedEvent);
        }        
    }
}

