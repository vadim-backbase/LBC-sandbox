package com.backbase.accesscontrol.business.datagroup.strategy;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import com.backbase.accesscontrol.business.datagroup.dataitems.ArrangementItemService;
import com.backbase.accesscontrol.business.datagroup.dataitems.CustomerItemService;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class WorkerFactoryTest {

    private static final String ARRANGEMENTS_DATA_ITEM_TYPE = "ARRANGEMENTS";
    private static final String CUSTOMERS_DATA_ITEM_TYPE = "CUSTOMERS";

    @Mock
    private ArrangementItemService arrangementItemService;

    @Mock
    private CustomerItemService customerItemService;

    private WorkerFactory workerFactory;
    private Worker arrangementWorker;
    private Worker customerWorker;

    @Before
    public void setUp() {
        arrangementWorker = new ArrangementWorker(arrangementItemService, true);
        customerWorker = new CustomerWorker(customerItemService);
        List<Worker> workers = asList(arrangementWorker, customerWorker);
        workerFactory = new WorkerFactory(workers);
    }

    @Test
    public void shouldReturnNullIfThereIsNotWorkerForTheSpecifiedType() {
        Worker worker = workerFactory.getWorker("UNKNOWN_WORKER");
        assertNull(worker);
    }

    @Test
    public void shouldReturnTheArrangementWorkerSuccessfully() {
        Worker worker = workerFactory.getWorker(ARRANGEMENTS_DATA_ITEM_TYPE);
        assertNotNull(worker);
        assertEquals(arrangementWorker, worker);
    }

    @Test
    public void shouldReturnTheCustomerWorkerSuccessfully() {
        Worker worker = workerFactory.getWorker(CUSTOMERS_DATA_ITEM_TYPE);
        assertNotNull(worker);
        assertEquals(customerWorker, worker);
    }
}
