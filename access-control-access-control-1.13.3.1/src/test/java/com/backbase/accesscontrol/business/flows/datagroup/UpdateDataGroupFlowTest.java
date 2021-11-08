package com.backbase.accesscontrol.business.flows.datagroup;

import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_061;
import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_098;
import static com.backbase.accesscontrol.util.helpers.TestDataUtils.getUuid;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.business.datagroup.strategy.Worker;
import com.backbase.accesscontrol.business.datagroup.strategy.WorkerFactory;
import com.backbase.accesscontrol.business.service.AgreementsPersistenceService;
import com.backbase.accesscontrol.business.service.DataGroupPAndPService;
import com.backbase.accesscontrol.domain.DataGroup;
import com.backbase.accesscontrol.domain.ServiceAgreement;
import com.backbase.accesscontrol.matchers.BadRequestErrorMatcher;
import com.backbase.accesscontrol.service.DataGroupService;
import com.backbase.accesscontrol.service.impl.PersistenceServiceAgreementService;
import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.serviceagreements.Participant;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.NameIdentifier;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.PresentationIdentifier;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.PresentationItemIdentifier;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.datagroups.PresentationSingleDataGroupPutRequestBody;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.validation.Validator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class UpdateDataGroupFlowTest {

    @Mock
    private DataGroupService dataGroupService;
    @Mock
    private DataGroupPAndPService dataGroupPAndPService;
    @Mock
    private WorkerFactory workerFactory;
    @Mock
    private Worker worker;
    @Mock
    private PersistenceServiceAgreementService persistenceServiceAgreementService;
    @Mock
    private AgreementsPersistenceService agreementsPersistenceService;
    @Mock
    private Validator validator;
    private UpdateDataGroupFlow updateDataGroupFlow;


    @Before
    public void setUp() {
        updateDataGroupFlow = new UpdateDataGroupFlow(dataGroupService, dataGroupPAndPService,
            workerFactory, validator, agreementsPersistenceService,
            persistenceServiceAgreementService, true);
    }

    @Test
    public void shouldUpdateDataGroupByInternalIdAndInternalDataItemIdentifiers() {
        String dgId = getUuid();
        String arrId1 = getUuid();
        String arrId2 = getUuid();
        String serviceAgreementId = "sa";
        PresentationSingleDataGroupPutRequestBody requestBody = new PresentationSingleDataGroupPutRequestBody()
            .withDataGroupIdentifier(new PresentationIdentifier()
                .withIdIdentifier(dgId))
            .withDescription("desc")
            .withName("name")
            .withType("ARRANGEMENTS")
            .withDataItems(asList(new PresentationItemIdentifier()
                    .withInternalIdIdentifier(arrId1),
                new PresentationItemIdentifier()
                    .withInternalIdIdentifier(arrId2)));

        when(workerFactory.getWorker(anyString())).thenReturn(worker);
        doNothing().when(worker).validateInternalIds(any(Set.class), anySet());
        when(worker.isValidatingAgainstParticipants()).thenReturn(true);

        DataGroup dataGroup = new DataGroup()
            .withServiceAgreementId(serviceAgreementId)
            .withId(dgId)
            .withName("name")
            .withDataItemType("ARRANGEMENTS")
            .withDescription("desc")
            .withDataItemIds(newHashSet(arrId1, arrId2));

        when(dataGroupService.getById(eq(dgId))).thenReturn(dataGroup);

        List<Participant> participantList = new ArrayList<>();
        participantList.add(new Participant()
            .withId("id1")
            .withSharingAccounts(true)
            .withExternalId("le-ex-id1"));
        participantList.add(new Participant()
            .withId("id2")
            .withSharingAccounts(true)
            .withExternalId("le-ex-id2"));
        when(persistenceServiceAgreementService
            .getServiceAgreementParticipants(eq(serviceAgreementId)))
            .thenReturn(participantList);

        updateDataGroupFlow.execute(requestBody);

        verify(dataGroupPAndPService, times(1)).updateDataGroupPersistence(requestBody);
    }

    @Test
    public void shouldUpdateDataGroupByInternalIdAndInternalDataItemIdentifiersAndTypeCustomer() {
        String dgId = getUuid();
        String internalId1 = getUuid();
        String internalId2 = getUuid();

        PresentationSingleDataGroupPutRequestBody requestBody = new PresentationSingleDataGroupPutRequestBody()
            .withDataGroupIdentifier(new PresentationIdentifier()
                .withIdIdentifier(dgId))
            .withDescription("desc")
            .withName("name")
            .withType("CUSTOMERS")
            .withDataItems(asList(new PresentationItemIdentifier()
                    .withInternalIdIdentifier(internalId1),
                new PresentationItemIdentifier()
                    .withInternalIdIdentifier(internalId2)));

        when(workerFactory.getWorker(anyString())).thenReturn(worker);
        doNothing().when(worker).validateInternalIds(any(Set.class), anySet());
        when(worker.isValidatingAgainstParticipants()).thenReturn(false);

        updateDataGroupFlow.execute(requestBody);

        verify(dataGroupPAndPService, times(1)).updateDataGroupPersistence(requestBody);
    }

    @Test
    public void shouldUpdateDataGroupByInternalIdAndNoDataItems() {
        String dgId = getUuid();
        String serviceAgreementId = "sa";
        PresentationSingleDataGroupPutRequestBody requestBody = new PresentationSingleDataGroupPutRequestBody()
            .withDataGroupIdentifier(new PresentationIdentifier()
                .withIdIdentifier(dgId))
            .withDescription("desc")
            .withName("name")
            .withType("ARRANGEMENTS");

        when(workerFactory.getWorker(anyString())).thenReturn(worker);
        doNothing().when(worker).validateInternalIds(any(Set.class), anySet());
        when(worker.isValidatingAgainstParticipants()).thenReturn(true);

        DataGroup dataGroup = new DataGroup()
            .withServiceAgreementId(serviceAgreementId)
            .withId(dgId)
            .withName("name")
            .withDataItemType("ARRANGEMENTS")
            .withDescription("desc");
        when(dataGroupService.getById(eq(dgId))).thenReturn(dataGroup);

        List<Participant> participantList = new ArrayList<>();
        participantList.add(new Participant()
            .withId("id1")
            .withSharingAccounts(true)
            .withExternalId("le-ex-id1"));
        participantList.add(new Participant()
            .withId("id2")
            .withSharingAccounts(true)
            .withExternalId("le-ex-id2"));
        when(persistenceServiceAgreementService
            .getServiceAgreementParticipants(eq(serviceAgreementId)))
            .thenReturn(participantList);

        updateDataGroupFlow.execute(requestBody);

        verify(dataGroupPAndPService, times(1)).updateDataGroupPersistence(requestBody);
    }

    @Test
    public void shouldUpdateDataGroupByInternalIdAndNoDataItemsAndTypeCustomer() {
        String dgId = getUuid();
        PresentationSingleDataGroupPutRequestBody requestBody = new PresentationSingleDataGroupPutRequestBody()
            .withDataGroupIdentifier(new PresentationIdentifier()
                .withIdIdentifier(dgId))
            .withDescription("desc")
            .withName("name")
            .withType("CUSTOMERS");

        when(workerFactory.getWorker(anyString())).thenReturn(worker);
        doNothing().when(worker).validateInternalIds(any(Set.class), anySet());
        when(worker.isValidatingAgainstParticipants()).thenReturn(false);

        updateDataGroupFlow.execute(requestBody);

        verify(dataGroupPAndPService, times(1)).updateDataGroupPersistence(requestBody);
    }

    @Test
    public void shouldThrowBadRequestWhenInternalAndExternalDataItemIdentifierProvided() {
        String dgId = getUuid();
        String arrId1 = getUuid();
        String arrId2 = getUuid();
        PresentationSingleDataGroupPutRequestBody requestBody = new PresentationSingleDataGroupPutRequestBody()
            .withDataGroupIdentifier(new PresentationIdentifier()
                .withIdIdentifier(dgId))
            .withDescription("desc")
            .withName("name")
            .withType("ARRANGEMENTS")
            .withDataItems(asList(new PresentationItemIdentifier()
                    .withInternalIdIdentifier(arrId1),
                new PresentationItemIdentifier()
                    .withExternalIdIdentifier(arrId2)));

        BadRequestException badRequestException = assertThrows(BadRequestException.class,
            () -> updateDataGroupFlow.execute(requestBody));

        assertThat(badRequestException,
            is(new BadRequestErrorMatcher(ERR_AG_098.getErrorMessage(), ERR_AG_098.getErrorCode())));
    }

    @Test
    public void shouldThrowBadRequestWhenDataGroupHasTwoIdentifiers() {
        String dgId = getUuid();
        String arrId1 = getUuid();
        String arrId2 = getUuid();
        PresentationSingleDataGroupPutRequestBody requestBody = new PresentationSingleDataGroupPutRequestBody()
            .withDataGroupIdentifier(new PresentationIdentifier()
                .withIdIdentifier(dgId)
                .withNameIdentifier(new NameIdentifier()))
            .withDescription("desc")
            .withName("name")
            .withType("ARRANGEMENTS")
            .withDataItems(asList(new PresentationItemIdentifier()
                    .withInternalIdIdentifier(arrId1),
                new PresentationItemIdentifier()
                    .withInternalIdIdentifier(arrId2)));

        BadRequestException badRequestException = assertThrows(BadRequestException.class,
            () -> updateDataGroupFlow.execute(requestBody));

        assertThat(badRequestException,
            is(new BadRequestErrorMatcher(ERR_AG_098.getErrorMessage(), ERR_AG_098.getErrorCode())));
    }

    @Test
    public void shouldCallExternalValidationWhenThereIsNoWorkerAndUpdateDataGroup() {
        String dgId = getUuid();
        String arrId1 = getUuid();
        String arrId2 = getUuid();
        PresentationSingleDataGroupPutRequestBody requestBody = new PresentationSingleDataGroupPutRequestBody()
            .withDataGroupIdentifier(new PresentationIdentifier()
                .withIdIdentifier(dgId))
            .withDescription("desc")
            .withName("name")
            .withType("ARRANGEMENTS")
            .withDataItems(asList(new PresentationItemIdentifier()
                    .withInternalIdIdentifier(arrId1),
                new PresentationItemIdentifier()
                    .withInternalIdIdentifier(arrId2)));

        when(workerFactory.getWorker(anyString())).thenReturn(null);
        updateDataGroupFlow.execute(requestBody);

        verify(dataGroupPAndPService, times(1)).updateDataGroupPersistence(requestBody);
    }

    @Test
    public void shouldUpdateDataGroupByInternalIdAndExternalDataItemIdentifiers() {
        String dgId = getUuid();
        String arrId1 = getUuid();
        String arrId2 = getUuid();
        String serviceAgreementId = "sa";
        PresentationSingleDataGroupPutRequestBody requestBody = new PresentationSingleDataGroupPutRequestBody()
            .withDataGroupIdentifier(new PresentationIdentifier()
                .withIdIdentifier(dgId))
            .withDescription("desc")
            .withName("name")
            .withType("ARRANGEMENTS")
            .withDataItems(asList(new PresentationItemIdentifier()
                    .withExternalIdIdentifier("1"),
                new PresentationItemIdentifier()
                    .withExternalIdIdentifier("2")));

        when(workerFactory.getWorker(anyString())).thenReturn(worker);
        when(worker.isValidatingAgainstParticipants()).thenReturn(true);
        when(worker.convertToInternalIdsAndValidate(any(Set.class), anySet(), anyString()))
            .thenReturn(asList(arrId1, arrId2));

        DataGroup dataGroup = new DataGroup()
            .withServiceAgreementId(serviceAgreementId)
            .withId(dgId)
            .withName("name")
            .withDataItemType("ARRANGEMENTS")
            .withDescription("desc")
            .withDataItemIds(newHashSet(arrId1, arrId2));
        when(dataGroupService.getById(eq(dgId))).thenReturn(dataGroup);

        List<Participant> participantList = new ArrayList<>();
        participantList.add(new Participant()
            .withId("id1")
            .withSharingAccounts(true)
            .withExternalId("le-ex-id1"));
        participantList.add(new Participant()
            .withId("id2")
            .withSharingAccounts(true)
            .withExternalId("le-ex-id2"));
        when(persistenceServiceAgreementService
            .getServiceAgreementParticipants(eq(serviceAgreementId)))
            .thenReturn(participantList);

        updateDataGroupFlow.execute(requestBody);

        PresentationSingleDataGroupPutRequestBody requestBodyForPersist = new PresentationSingleDataGroupPutRequestBody()
            .withDataGroupIdentifier(new PresentationIdentifier()
                .withIdIdentifier(dgId))
            .withDescription("desc")
            .withName("name")
            .withType("ARRANGEMENTS")
            .withDataItems(asList(new PresentationItemIdentifier()
                    .withInternalIdIdentifier(arrId1),
                new PresentationItemIdentifier()
                    .withInternalIdIdentifier(arrId2)));

        verify(dataGroupPAndPService, times(1)).updateDataGroupPersistence(requestBodyForPersist);
    }

    @Test
    public void shouldUpdateDataGroupByNameIdentifier() {
        String dgId = getUuid();
        String arrId1 = getUuid();
        String arrId2 = getUuid();
        String serviceAgreementId = "sa";
        String exSaId = "exId";
        PresentationSingleDataGroupPutRequestBody requestBody = new PresentationSingleDataGroupPutRequestBody()
            .withDataGroupIdentifier(new PresentationIdentifier()
                .withNameIdentifier(new NameIdentifier()
                    .withName("sa")
                    .withExternalServiceAgreementId(exSaId)))
            .withDescription("desc")
            .withName("name")
            .withType("ARRANGEMENTS")
            .withDataItems(asList(new PresentationItemIdentifier()
                    .withExternalIdIdentifier("1"),
                new PresentationItemIdentifier()
                    .withExternalIdIdentifier("2")));

        when(workerFactory.getWorker(anyString())).thenReturn(worker);
        when(worker.isValidatingAgainstParticipants()).thenReturn(true);
        when(worker.convertToInternalIdsAndValidate(any(Set.class), anySet(), anyString()))
            .thenReturn(asList(arrId1, arrId2));

        when(persistenceServiceAgreementService.getServiceAgreementByExternalId(eq(exSaId)))
            .thenReturn(new ServiceAgreement()
                .withId(serviceAgreementId)
                .withExternalId(exSaId));

        when(agreementsPersistenceService.getSharingAccountsParticipantIdsForServiceAgreement(eq(exSaId)))
            .thenReturn(Sets.newHashSet("id1", "id2"));

        updateDataGroupFlow.execute(requestBody);

        PresentationSingleDataGroupPutRequestBody requestBodyForPersist = new PresentationSingleDataGroupPutRequestBody()
            .withDataGroupIdentifier(new PresentationIdentifier()
                .withNameIdentifier(new NameIdentifier()
                    .withExternalServiceAgreementId(exSaId)
                    .withName("sa")))
            .withDescription("desc")
            .withName("name")
            .withType("ARRANGEMENTS")
            .withDataItems(asList(new PresentationItemIdentifier()
                    .withInternalIdIdentifier(arrId1),
                new PresentationItemIdentifier()
                    .withInternalIdIdentifier(arrId2)));

        verify(dataGroupPAndPService, times(1)).updateDataGroupPersistence(requestBodyForPersist);
    }


    @Test
    public void shouldUpdateDataGroupByInternalIdAndExternalDataItemIdentifiersAndTypeCustomer() {
        String dgId = getUuid();
        String internalId1 = getUuid();
        String internalId2 = getUuid();
        PresentationSingleDataGroupPutRequestBody requestBody = new PresentationSingleDataGroupPutRequestBody()
            .withDataGroupIdentifier(new PresentationIdentifier()
                .withIdIdentifier(dgId))
            .withDescription("desc")
            .withName("name")
            .withType("CUSTOMERS")
            .withDataItems(asList(new PresentationItemIdentifier()
                    .withExternalIdIdentifier("1"),
                new PresentationItemIdentifier()
                    .withExternalIdIdentifier("2")));

        when(workerFactory.getWorker(anyString())).thenReturn(worker);
        when(worker.isValidatingAgainstParticipants()).thenReturn(false);
        when(worker.convertToInternalIdsAndValidate(any(Set.class), anySet(), anyString()))
            .thenReturn(asList(internalId1, internalId2));
        when(dataGroupService.getById(eq(dgId)))
            .thenReturn(new DataGroup()
                .withId(dgId)
                .withServiceAgreementId("said"));

        updateDataGroupFlow.execute(requestBody);

        PresentationSingleDataGroupPutRequestBody requestBodyForPersist = new PresentationSingleDataGroupPutRequestBody()
            .withDataGroupIdentifier(new PresentationIdentifier()
                .withIdIdentifier(dgId))
            .withDescription("desc")
            .withName("name")
            .withType("CUSTOMERS")
            .withDataItems(asList(new PresentationItemIdentifier()
                    .withInternalIdIdentifier(internalId1),
                new PresentationItemIdentifier()
                    .withInternalIdIdentifier(internalId2)));

        verify(dataGroupPAndPService, times(1)).updateDataGroupPersistence(requestBodyForPersist);
    }

    @Test
    public void shouldUpdateDataGroupByExternalIdAndInternalDataItemIdentifiers() {
        String arrId1 = getUuid();
        String arrId2 = getUuid();
        String saId = getUuid();
        Set<String> participantIds = newHashSet(getUuid(), getUuid());
        PresentationSingleDataGroupPutRequestBody requestBody = new PresentationSingleDataGroupPutRequestBody()
            .withDataGroupIdentifier(new PresentationIdentifier()
                .withNameIdentifier(new NameIdentifier()
                    .withName("dg name")
                    .withExternalServiceAgreementId(saId)))
            .withDescription("desc")
            .withName("name")
            .withType("ARRANGEMENTS")
            .withDataItems(asList(new PresentationItemIdentifier()
                    .withInternalIdIdentifier(arrId1),
                new PresentationItemIdentifier()
                    .withInternalIdIdentifier(arrId2)));

        when(workerFactory.getWorker(anyString())).thenReturn(worker);
        when(worker.isValidatingAgainstParticipants()).thenReturn(true);
        doNothing().when(worker).validateInternalIds(any(Set.class), anySet());
        when(agreementsPersistenceService
            .getSharingAccountsParticipantIdsForServiceAgreement(eq(saId)))
            .thenReturn(participantIds);

        updateDataGroupFlow.execute(requestBody);

        verify(dataGroupPAndPService, times(1)).updateDataGroupPersistence(requestBody);
    }

    @Test
    public void shouldUpdateDataGroupByExternalIdAndInternalDataItemIdentifiersAndTypeCustomer() {
        String internalId1 = getUuid();
        String internalId2 = getUuid();
        String saId = getUuid();
        Set<String> participantIds = newHashSet(getUuid(), getUuid());
        PresentationSingleDataGroupPutRequestBody requestBody = new PresentationSingleDataGroupPutRequestBody()
            .withDataGroupIdentifier(new PresentationIdentifier()
                .withNameIdentifier(new NameIdentifier()
                    .withName("dg name")
                    .withExternalServiceAgreementId(saId)))
            .withDescription("desc")
            .withName("name")
            .withType("CUSTOMERS")
            .withDataItems(asList(new PresentationItemIdentifier()
                    .withInternalIdIdentifier(internalId1),
                new PresentationItemIdentifier()
                    .withInternalIdIdentifier(internalId2)));

        when(workerFactory.getWorker(anyString())).thenReturn(worker);
        when(worker.isValidatingAgainstParticipants()).thenReturn(true);
        doNothing().when(worker).validateInternalIds(any(Set.class), anySet());
        when(agreementsPersistenceService
            .getSharingAccountsParticipantIdsForServiceAgreement(eq(saId)))
            .thenReturn(participantIds);

        updateDataGroupFlow.execute(requestBody);

        verify(dataGroupPAndPService, times(1)).updateDataGroupPersistence(requestBody);
    }

    @Test
    public void shouldThrowBadRequestWhenInvalidServiceAgreementIdProvided() {
        String saId = getUuid();
        String internalId1 = getUuid();
        String internalId2 = getUuid();
        PresentationSingleDataGroupPutRequestBody requestBody = new PresentationSingleDataGroupPutRequestBody()
            .withDataGroupIdentifier(new PresentationIdentifier()
                .withNameIdentifier(new NameIdentifier()
                    .withName("dg name")
                    .withExternalServiceAgreementId(saId)))
            .withDescription("desc")
            .withName("name")
            .withType("CUSTOMERS")
            .withDataItems(asList(new PresentationItemIdentifier()
                    .withInternalIdIdentifier(internalId1),
                new PresentationItemIdentifier()
                    .withInternalIdIdentifier(internalId2)));

        when(workerFactory.getWorker(anyString())).thenReturn(worker);
        when(worker.isValidatingAgainstParticipants()).thenReturn(true);
        when(agreementsPersistenceService
            .getSharingAccountsParticipantIdsForServiceAgreement(eq(saId)))
            .thenReturn(Collections.emptySet());

        BadRequestException badRequestException = assertThrows(BadRequestException.class,
            () -> updateDataGroupFlow.execute(requestBody));

        assertThat(badRequestException,
            is(new BadRequestErrorMatcher(ERR_AG_061.getErrorMessage(), ERR_AG_061.getErrorCode())));
    }

}