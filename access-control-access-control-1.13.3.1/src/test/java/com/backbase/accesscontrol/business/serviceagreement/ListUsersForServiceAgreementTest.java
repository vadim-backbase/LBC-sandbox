package com.backbase.accesscontrol.business.serviceagreement;

import static com.backbase.accesscontrol.util.helpers.RequestUtils.getInternalRequest;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.business.service.UserManagementService;
import com.backbase.accesscontrol.domain.LegalEntity;
import com.backbase.accesscontrol.dto.ListElementsWrapper;
import com.backbase.accesscontrol.service.impl.PersistenceLegalEntityService;
import com.backbase.accesscontrol.service.impl.PersistenceServiceAgreementService;
import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.serviceagreements.Participant;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.serviceagreements.ServiceAgreementItem;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.legalentities.LegalEntityGetResponseBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementUsersGetResponseBody;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ListUsersForServiceAgreementTest {

    private static final String SERVICE_AGREEMENT_ID = "SA01";
    private static final String LEGAL_ENTITY_ID = "CLE";
    private static final String LEGAL_ENTITY_NAME = "CLEName";

    @Mock
    private UserManagementService userManagementService;
    @Mock
    private PersistenceLegalEntityService persistenceLegalEntityService;
    @Mock
    private PersistenceServiceAgreementService persistenceServiceAgreementService;

    private ListUsersForServiceAgreement listUsersForServiceAgreement;

    @Before
    public void setUp() throws Exception {
        listUsersForServiceAgreement = new ListUsersForServiceAgreement(
            userManagementService,
            persistenceLegalEntityService,
            persistenceServiceAgreementService
        );
    }

    @Test
    public void shouldPassIfGetUsersForServiceAgreementUnderCustomServiceAgreementIsInvoked() {
        Integer from = 1;
        Integer size = 2;
        Long totalNumberOfRecords = 100L;
        List<String> userIds = asList("U1", "U2");

        ListElementsWrapper<ServiceAgreementUsersGetResponseBody> usersOfServiceAgreementDto = getRequestData(
            totalNumberOfRecords);

        Set<String> usersIds = new LinkedHashSet<>(userIds);
        com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.serviceagreements.ServiceAgreementUsersGetResponseBody saUsersGetResponseBody =
            new com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.serviceagreements.ServiceAgreementUsersGetResponseBody()
                .withUserIds(usersIds);

        ServiceAgreementItem serviceAgreementItem = getServiceAgreementItem(false);
        when(persistenceServiceAgreementService
            .getServiceAgreementUsers(eq(SERVICE_AGREEMENT_ID)))
            .thenReturn(saUsersGetResponseBody);

        List<Participant> participants = Collections.singletonList(new Participant()
            .withId(LEGAL_ENTITY_ID)
            .withName(LEGAL_ENTITY_NAME));
        when(persistenceServiceAgreementService
            .getServiceAgreementParticipants(eq(SERVICE_AGREEMENT_ID)))
            .thenReturn(participants);

        when(persistenceServiceAgreementService.getServiceAgreementResponseBodyById(eq(SERVICE_AGREEMENT_ID)))
            .thenReturn(serviceAgreementItem);

        when(userManagementService
            .getUsersForServiceAgreement(eq(new HashSet<>(userIds)), eq(null), eq(from),
                eq(size), eq(null)))
            .thenReturn(usersOfServiceAgreementDto);

        InternalRequest<Void> voidInternalRequest = getInternalRequest(null);
        InternalRequest<ListElementsWrapper<ServiceAgreementUsersGetResponseBody>> businessProcessResult = listUsersForServiceAgreement
            .getUsersForServiceAgreement(voidInternalRequest, SERVICE_AGREEMENT_ID, null, from, size, null);

        verify(persistenceServiceAgreementService, times(1))
            .getServiceAgreementUsers(eq(SERVICE_AGREEMENT_ID));
        verify(userManagementService, times(1))
            .getUsersForServiceAgreement(eq(new HashSet<>(userIds)), eq(null), eq(from),
                eq(size), eq(null));
        int expectedSize = usersOfServiceAgreementDto.getRecords().size();
        assertEquals(expectedSize, businessProcessResult.getData().getRecords().size());
        assertEquals(participants.get(0).getName(),
            businessProcessResult.getData().getRecords().get(0).getLegalEntityName());
        assertEquals(totalNumberOfRecords, businessProcessResult.getData().getTotalNumberOfRecords());
    }


    @Test
    public void shouldPassIfGetUsersForServiceAgreementUnderMasterServiceAgreementIsInvoked() {
        Integer from = 1;
        Integer size = 2;
        Long totalNumberOfRecords = 100L;
        ServiceAgreementItem serviceAgreementItem = getServiceAgreementItem(true);

        LegalEntityGetResponseBody legalEntityGetResponseBody = new LegalEntityGetResponseBody();
        legalEntityGetResponseBody.setId(LEGAL_ENTITY_ID);
        legalEntityGetResponseBody.setName(LEGAL_ENTITY_NAME);

        ListElementsWrapper<ServiceAgreementUsersGetResponseBody> usersOfServiceAgreementDto = getRequestData(
            totalNumberOfRecords);

        when(persistenceServiceAgreementService.getServiceAgreementResponseBodyById(eq(SERVICE_AGREEMENT_ID)))
            .thenReturn(serviceAgreementItem);

        when(persistenceLegalEntityService
            .getLegalEntityById(eq(LEGAL_ENTITY_ID)))
            .thenReturn(new LegalEntity().withName(LEGAL_ENTITY_NAME));

        com.backbase.dbs.user.api.client.v2.model.GetUsersByLegalEntityIdsRequest item =
            new com.backbase.dbs.user.api.client.v2.model.GetUsersByLegalEntityIdsRequest();
        item.setSize(size);
        item.setFrom(from);
        item.setLegalEntityIds(Lists.newArrayList(LEGAL_ENTITY_ID));
        when(userManagementService
            .getUsersForCreatorLegalEntity(refEq(item)))
            .thenReturn(usersOfServiceAgreementDto);

        InternalRequest<Void> voidInternalRequest = getInternalRequest(null);
        InternalRequest<ListElementsWrapper<ServiceAgreementUsersGetResponseBody>> businessProcessResult = listUsersForServiceAgreement
            .getUsersForServiceAgreement(voidInternalRequest, SERVICE_AGREEMENT_ID, null, from, size, null);
        com.backbase.dbs.user.api.client.v2.model.GetUsersByLegalEntityIdsRequest item1 =
            new com.backbase.dbs.user.api.client.v2.model.GetUsersByLegalEntityIdsRequest();
        item1.setSize(size);
        item1.setFrom(from);
        item1.setLegalEntityIds(Lists.newArrayList(LEGAL_ENTITY_ID));
        verify(userManagementService, times(1))
            .getUsersForCreatorLegalEntity(refEq(item1));
        int expectedSize = usersOfServiceAgreementDto.getRecords().size();
        assertEquals(expectedSize, businessProcessResult.getData().getRecords().size());
        assertEquals(legalEntityGetResponseBody.getName(),
            businessProcessResult.getData().getRecords().get(0).getLegalEntityName());
        assertEquals(totalNumberOfRecords, businessProcessResult.getData().getTotalNumberOfRecords());
    }

    @Test
    public void shouldFailWhenUserIdsIsEmpty() {
        Integer from = 1;
        Integer size = 2;

        com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.serviceagreements.ServiceAgreementUsersGetResponseBody saUsersGetResponseBody =
            new com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.serviceagreements.ServiceAgreementUsersGetResponseBody()
                .withUserIds(new HashSet<>());
        ServiceAgreementItem serviceAgreementItem = getServiceAgreementItem(false);

        when(persistenceServiceAgreementService.getServiceAgreementResponseBodyById(eq(SERVICE_AGREEMENT_ID)))
            .thenReturn(serviceAgreementItem);

        when(persistenceServiceAgreementService
            .getServiceAgreementUsers(eq(SERVICE_AGREEMENT_ID)))
            .thenReturn(saUsersGetResponseBody);

        InternalRequest<Void> voidInternalRequest = getInternalRequest(null);

        InternalRequest<ListElementsWrapper<ServiceAgreementUsersGetResponseBody>> usersForServiceAgreement = listUsersForServiceAgreement
            .getUsersForServiceAgreement(voidInternalRequest, SERVICE_AGREEMENT_ID, null, from, size, null);

        assertEquals(0L, usersForServiceAgreement.getData().getTotalNumberOfRecords().longValue());
    }

    private ListElementsWrapper<ServiceAgreementUsersGetResponseBody> getRequestData(Long totalNumberOfRecords) {
        List<ServiceAgreementUsersGetResponseBody> serviceAgreementUsersGetResponseBodies = new ArrayList<>();

        ServiceAgreementUsersGetResponseBody serviceAgreementUsersGetResponseBody = new ServiceAgreementUsersGetResponseBody();
        serviceAgreementUsersGetResponseBody.setId("U1");
        serviceAgreementUsersGetResponseBody.setFullName("User1");
        serviceAgreementUsersGetResponseBody.setExternalId("exid1");
        serviceAgreementUsersGetResponseBody.setLegalEntityId(LEGAL_ENTITY_ID);

        ServiceAgreementUsersGetResponseBody serviceAgreementUsersGetResponseBody2 = new ServiceAgreementUsersGetResponseBody();
        serviceAgreementUsersGetResponseBody2.setId("U2");
        serviceAgreementUsersGetResponseBody2.setFullName("User2");
        serviceAgreementUsersGetResponseBody2.setExternalId("exid2");
        serviceAgreementUsersGetResponseBody2.setLegalEntityId(LEGAL_ENTITY_ID);

        serviceAgreementUsersGetResponseBodies.add(serviceAgreementUsersGetResponseBody);
        serviceAgreementUsersGetResponseBodies.add(serviceAgreementUsersGetResponseBody2);

        return new ListElementsWrapper<>(serviceAgreementUsersGetResponseBodies, totalNumberOfRecords);
    }

    private ServiceAgreementItem getServiceAgreementItem(Boolean isMaster) {
        ServiceAgreementItem serviceAgreementItem = new ServiceAgreementItem();
        serviceAgreementItem.setIsMaster(isMaster);
        serviceAgreementItem.setId(SERVICE_AGREEMENT_ID);
        serviceAgreementItem.setCreatorLegalEntity(LEGAL_ENTITY_ID);
        return serviceAgreementItem;
    }

}
