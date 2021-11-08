package com.backbase.accesscontrol.business.datagroup.strategy;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class WorkerFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(WorkerFactory.class);

    private Map<String, Worker> workerMap;

    public WorkerFactory(List<Worker> workers) {
        workerMap = workers.stream().collect(Collectors.toMap(Worker::getType, worker -> worker));
    }

    /**
     * Worker factory which returns a worker for a specific type.
     *
     * @param type for which we need a worker.
     * @return the worker {@link Worker} or null if there is no worker for the type.
     */
    public Worker getWorker(String type) {
        Worker worker = workerMap.get(type);

        if (Objects.isNull(worker)) {
            LOGGER.warn("Detected custom type: {}", type);
        }

        return worker;
    }
}
