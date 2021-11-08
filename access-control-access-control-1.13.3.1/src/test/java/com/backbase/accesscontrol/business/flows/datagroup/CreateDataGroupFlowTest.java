package com.backbase.accesscontrol.business.flows.datagroup;

import static com.backbase.accesscontrol.util.ExceptionUtil.getBadRequestException;
import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_001;
import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_089;
import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_100;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.business.datagroup.strategy.Worker;
import com.backbase.accesscontrol.business.datagroup.strategy.WorkerFactory;
import com.backbase.accesscontrol.business.persistence.datagroup.AddDataGroupHandler;
import com.backbase.accesscontrol.business.service.AgreementsPersistenceService;
import com.backbase.accesscontrol.configuration.ValidationConfig;
import com.backbase.accesscontrol.domain.ServiceAgreement;
import com.backbase.accesscontrol.dto.parameterholder.EmptyParameterHolder;
import com.backbase.accesscontrol.matchers.BadRequestErrorMatcher;
import com.backbase.accesscontrol.service.impl.PersistenceServiceAgreementService;
import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.presentation.accessgroup.event.spec.v1.DataGroupBase;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.datagroups.DataGroupsPostResponseBody;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CreateDataGroupFlowTest {

    private static final String ID = "id";

    @Mock
    private WorkerFactory workerFactory;
    @Mock
    private ValidationConfig validationConfig;
    @Mock
    private Worker worker;
    @Mock
    private PersistenceServiceAgreementService persistenceServiceAgreementService;
    @Mock
    private AgreementsPersistenceService agreementsPersistenceService;
    @Mock
    private AddDataGroupHandler addDataGroupHandler;
    private CreateDataGroupFlow createDataGroupFlow;

    private DataGroupBase dataGroupBase;
    private List<String> externalItems;
    private List<String> internalItems;

    @Before
    public void setUp() {
        externalItems = new ArrayList<String>() {{
            add("externalItem1");
            add("externalItem2");
        }};

        internalItems = new ArrayList() {{
            add("internalItem1");
            add("internalItem2");
        }};

        dataGroupBase = new DataGroupBase()
            .withName("Name")
            .withDescription("Description")
            .withType("ARRANGEMENTS")
            .withAreItemsInternalIds(false)
            .withServiceAgreementId("serviceAgreementId")
            .withItems(externalItems);

        createDataGroupFlow = new CreateDataGroupFlow(workerFactory, validationConfig, agreementsPersistenceService,
            persistenceServiceAgreementService, addDataGroupHandler);
    }

    @Test
    public void shouldCreateDataGroupWhenItemsAreInternal() {
        dataGroupBase.setAreItemsInternalIds(true);
        dataGroupBase.setItems(internalItems);
        DataGroupsPostResponseBody dataGroupsPostResponseBody = new DataGroupsPostResponseBody().withId(ID);

        doNothing().when(validationConfig).validateDataGroupType(eq(dataGroupBase.getType()));
        when(workerFactory.getWorker(eq(dataGroupBase.getType()))).thenReturn(worker);
        doNothing().when(worker).validateInternalIds(any(Set.class), anySet());
        when(worker.isValidatingAgainstParticipants()).thenReturn(true);

        when(addDataGroupHandler.handleRequest(any(EmptyParameterHolder.class), any(DataGroupBase.class)))
            .thenReturn(dataGroupsPostResponseBody);

        DataGroupsPostResponseBody result = createDataGroupFlow.execute(dataGroupBase);

        assertNotNull(result);
        assertEquals(ID, result.getId());
    }

    @Test
    public void shouldCreateDataGroupWhenItemsAreInternalAndTypeCustomers() {
        DataGroupBase dataGroupBase = new DataGroupBase()
            .withName("Name")
            .withDescription("Description")
            .withType("CUSTOMERS")
            .withAreItemsInternalIds(true)
            .withServiceAgreementId("serviceAgreementId")
            .withItems(internalItems);
        DataGroupsPostResponseBody dataGroupsPostResponseBody = new DataGroupsPostResponseBody().withId(ID);

        doNothing().when(validationConfig).validateDataGroupType(eq(dataGroupBase.getType()));
        when(workerFactory.getWorker(eq(dataGroupBase.getType()))).thenReturn(worker);
        doNothing().when(worker).validateInternalIds(any(Set.class), anySet());
        when(worker.isValidatingAgainstParticipants()).thenReturn(false);

        when(addDataGroupHandler.handleRequest(any(EmptyParameterHolder.class), any(DataGroupBase.class)))
            .thenReturn(dataGroupsPostResponseBody);

        DataGroupsPostResponseBody result = createDataGroupFlow.execute(dataGroupBase);

        assertNotNull(result);
        assertEquals(ID, result.getId());
    }

    @Test
    public void shouldCreateDataGroupWhenItemsAreInternalAndExternalIdIsNull() {
        String externalServiceAgreementId = "exId";
        dataGroupBase.setAreItemsInternalIds(true);
        dataGroupBase.setItems(internalItems);
        dataGroupBase.setExternalServiceAgreementId(externalServiceAgreementId);
        DataGroupsPostResponseBody dataGroupsPostResponseBody = new DataGroupsPostResponseBody().withId(ID);

        doNothing().when(validationConfig).validateDataGroupType(eq(dataGroupBase.getType()));
        when(workerFactory.getWorker(eq(dataGroupBase.getType()))).thenReturn(worker);
        doNothing().when(worker).validateInternalIds(any(Set.class), anySet());
        when(worker.isValidatingAgainstParticipants()).thenReturn(true);

        when(agreementsPersistenceService
            .getSharingAccountsParticipantIdsForServiceAgreement(externalServiceAgreementId))
            .thenReturn(Sets.newHashSet("1", "2"));
        when(addDataGroupHandler.handleRequest(any(EmptyParameterHolder.class), any(DataGroupBase.class)))
            .thenReturn(dataGroupsPostResponseBody);

        DataGroupsPostResponseBody result = createDataGroupFlow.execute(dataGroupBase);

        assertNotNull(result);
        assertEquals(ID, result.getId());
    }

    @Test
    public void shouldThrowExceptionWhenItemsAreInternalAndValidationFails() {
        dataGroupBase.setAreItemsInternalIds(true);
        dataGroupBase.setItems(internalItems);

        doNothing().when(validationConfig).validateDataGroupType(eq(dataGroupBase.getType()));
        when(workerFactory.getWorker(eq(dataGroupBase.getType()))).thenReturn(worker);
        when(worker.isValidatingAgainstParticipants()).thenReturn(true);

        doThrow(getBadRequestException(ERR_AG_089.getErrorMessage(),
            ERR_AG_089.getErrorCode()))
            .when(worker).validateInternalIds(any(Set.class), anySet());

        BadRequestException badRequestException = assertThrows(BadRequestException.class,
            () -> createDataGroupFlow.execute(dataGroupBase));

        assertThat(badRequestException,
            is(new BadRequestErrorMatcher(ERR_AG_089.getErrorMessage(), ERR_AG_089.getErrorCode())));
    }

    @Test
    public void shouldCreateDataGroupWhenItemsAreExternal() {
        dataGroupBase.setItems(externalItems);
        DataGroupsPostResponseBody dataGroupsPostResponseBody = new DataGroupsPostResponseBody().withId(ID);

        doNothing().when(validationConfig).validateDataGroupType(eq(dataGroupBase.getType()));
        when(workerFactory.getWorker(eq(dataGroupBase.getType()))).thenReturn(worker);
        when(worker.isValidatingAgainstParticipants()).thenReturn(true);

        when(addDataGroupHandler.handleRequest(any(EmptyParameterHolder.class), any(DataGroupBase.class)))
            .thenReturn(dataGroupsPostResponseBody);

        DataGroupsPostResponseBody result = createDataGroupFlow.execute(dataGroupBase);

        assertNotNull(result);
        assertEquals(ID, result.getId());
    }

    @Test
    public void shouldCreateDataGroupWhenItemsAreExternalAndTypeCustomer() {
        DataGroupBase dataGroupBase = new DataGroupBase()
            .withName("Name")
            .withDescription("Description")
            .withType("CUSTOMERS")
            .withAreItemsInternalIds(false)
            .withServiceAgreementId("serviceAgreementId")
            .withItems(externalItems);
        DataGroupsPostResponseBody dataGroupsPostResponseBody = new DataGroupsPostResponseBody().withId(ID);

        doNothing().when(validationConfig).validateDataGroupType(eq(dataGroupBase.getType()));
        when(workerFactory.getWorker(eq(dataGroupBase.getType()))).thenReturn(worker);
        when(worker.isValidatingAgainstParticipants()).thenReturn(false);

        when(addDataGroupHandler.handleRequest(any(EmptyParameterHolder.class), any(DataGroupBase.class)))
            .thenReturn(dataGroupsPostResponseBody);

        DataGroupsPostResponseBody result = createDataGroupFlow.execute(dataGroupBase);

        assertNotNull(result);
        assertEquals(ID, result.getId());
    }

    @Test
    public void shouldCreateDataGroupWithExternalSaIdWhenItemsAreExternalAndTypeCustomer() {
        DataGroupBase dataGroupBase = new DataGroupBase()
            .withName("Name")
            .withDescription("Description")
            .withType("CUSTOMERS")
            .withAreItemsInternalIds(false)
            .withExternalServiceAgreementId("exId")
            .withItems(externalItems);
        DataGroupsPostResponseBody dataGroupsPostResponseBody = new DataGroupsPostResponseBody().withId(ID);

        doNothing().when(validationConfig).validateDataGroupType(eq(dataGroupBase.getType()));
        when(workerFactory.getWorker(eq(dataGroupBase.getType()))).thenReturn(worker);
        when(worker.isValidatingAgainstParticipants()).thenReturn(false);
        when(persistenceServiceAgreementService.getServiceAgreementByExternalId(eq("exId")))
            .thenReturn(new ServiceAgreement()
                .withId("serviceAgreementId")
                .withExternalId("exId"));

        when(addDataGroupHandler.handleRequest(any(EmptyParameterHolder.class), any(DataGroupBase.class)))
            .thenReturn(dataGroupsPostResponseBody);

        DataGroupsPostResponseBody result = createDataGroupFlow.execute(dataGroupBase);

        assertNotNull(result);
        assertEquals(ID, result.getId());
    }

    @Test
    public void shouldThrowExceptionWhenItemsAreExternalAndValidateFails() {
        dataGroupBase.setItems(externalItems);

        doNothing().when(validationConfig).validateDataGroupType(eq(dataGroupBase.getType()));
        when(workerFactory.getWorker(eq(dataGroupBase.getType()))).thenReturn(worker);
        when(worker.isValidatingAgainstParticipants()).thenReturn(true);

        doThrow(getBadRequestException(ERR_AG_089.getErrorMessage(),
            ERR_AG_089.getErrorCode()))
            .when(worker).convertToInternalIdsAndValidate(any(Set.class), anySet(), anyString());

        BadRequestException badRequestException = assertThrows(BadRequestException.class,
            () -> createDataGroupFlow.execute(dataGroupBase));

        assertThat(badRequestException,
            is(new BadRequestErrorMatcher(ERR_AG_089.getErrorMessage(), ERR_AG_089.getErrorCode())));
    }

    @Test
    public void shouldCreateDataWhenItemsAreInternalAndDoExternalValidation() {
        dataGroupBase.setItems(internalItems);
        DataGroupsPostResponseBody dataGroupsPostResponseBody = new DataGroupsPostResponseBody().withId(ID);

        doNothing().when(validationConfig).validateDataGroupType(eq(dataGroupBase.getType()));
        when(workerFactory.getWorker(eq(dataGroupBase.getType()))).thenReturn(null);

        when(addDataGroupHandler.handleRequest(any(EmptyParameterHolder.class), any(DataGroupBase.class)))
            .thenReturn(dataGroupsPostResponseBody);

        DataGroupsPostResponseBody result = createDataGroupFlow.execute(dataGroupBase);

        assertNotNull(result);
        assertEquals(ID, result.getId());
    }

    @Test
    public void shouldThrowExceptionWhenTypeIsInvalid() {
        doThrow(getBadRequestException(ERR_AG_001.getErrorMessage(),
            ERR_AG_001.getErrorCode()))
            .when(validationConfig).validateDataGroupType(anyString());

        BadRequestException badRequestException = assertThrows(BadRequestException.class,
            () -> createDataGroupFlow.execute(dataGroupBase));

        assertThat(badRequestException,
            is(new BadRequestErrorMatcher(ERR_AG_001.getErrorMessage(), ERR_AG_001.getErrorCode())));
    }

    @Test
    public void shouldThrowBadExceptionWhenSavingWithoutSAs() {
        DataGroupBase dataGroupBase = new DataGroupBase();
        BadRequestException badRequestException = assertThrows(BadRequestException.class,
            () -> createDataGroupFlow.execute(dataGroupBase));

        assertThat(badRequestException,
            is(new BadRequestErrorMatcher(ERR_AG_100.getErrorMessage(), ERR_AG_100.getErrorCode())));
    }

    @Test
    public void shouldThrowBadExceptionWhenSavingWithEmptySA() {
        DataGroupBase dataGroupBase = new DataGroupBase();
        dataGroupBase.setServiceAgreementId("");

        BadRequestException badRequestException = assertThrows(BadRequestException.class,
            () -> createDataGroupFlow.execute(dataGroupBase));

        assertThat(badRequestException,
            is(new BadRequestErrorMatcher(ERR_AG_100.getErrorMessage(), ERR_AG_100.getErrorCode())));
    }

    @Test
    public void shouldThrowBadExceptionWhenSavingWithEmptyExternalSA() {
        DataGroupBase dataGroupBase = new DataGroupBase();
        dataGroupBase.setExternalServiceAgreementId("");

        BadRequestException badRequestException = assertThrows(BadRequestException.class,
            () -> createDataGroupFlow.execute(dataGroupBase));

        assertThat(badRequestException,
            is(new BadRequestErrorMatcher(ERR_AG_100.getErrorMessage(), ERR_AG_100.getErrorCode())));
    }
}
