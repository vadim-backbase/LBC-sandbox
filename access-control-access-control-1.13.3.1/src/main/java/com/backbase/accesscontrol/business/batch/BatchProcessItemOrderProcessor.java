package com.backbase.accesscontrol.business.batch;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class BatchProcessItemOrderProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(BatchProcessItemOrderProcessor.class);

    /**
     * Transforms a list of participants into list od ProcessableBatchBody.
     *
     * @param participants list of items
     * @param <T>          type of the item in list to be transformed
     * @return list of {@link ProcessableBatchBody}
     */
    public <T> List<ProcessableBatchBody<T>> transformProcessableBody(List<T> participants) {
        AtomicInteger index = new AtomicInteger();
        LOGGER.info("Converting request to Processable batch body {}", participants);
        List<ProcessableBatchBody<T>> processableBatchBodies = participants
            .stream()
            .map(u -> new ProcessableBatchBody<>(u, index.incrementAndGet()))
            .collect(Collectors.toList());

        LOGGER.info("Converted Processable batch body {}", processableBatchBodies);
        return processableBatchBodies;
    }
}