package com.backbase.accesscontrol.business.datagroup.dataitems;

import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_089;
import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_098;
import static com.backbase.accesscontrol.util.helpers.RequestUtils.getInternalRequest;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.business.service.ArrangementsService;
import com.backbase.accesscontrol.business.serviceagreement.GetServiceAgreementParticipants;
import com.backbase.accesscontrol.matchers.BadRequestErrorMatcher;
import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.buildingblocks.presentation.errors.NotFoundException;
import com.backbase.dbs.arrangement.api.client.v2.model.AccountArrangementItem;
import com.backbase.dbs.arrangement.api.client.v2.model.AccountArrangementItems;
import com.backbase.dbs.arrangement.api.client.v2.model.AccountArrangementsFilter;
import com.backbase.dbs.arrangement.api.client.v2.model.AccountArrangementsLegalEntities;
import com.backbase.dbs.arrangement.api.client.v2.model.AccountPresentationArrangementLegalEntityIds;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementParticipantsGetResponseBody;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ArrangementItemServiceTest {

    protected ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private ArrangementsService arrangementsService;
    @Mock
    private GetServiceAgreementParticipants getServiceAgreementParticipants;

    @InjectMocks
    private ArrangementItemService arrangementItemService;

    @Test
    public void shouldReturnArrangementsType() {
        assertEquals("ARRANGEMENTS", arrangementItemService.getType());
    }

    @Test
    public void shouldCallPersistenceAndReturnInternalId() {

        when(arrangementsService.getInternalId(eq("extArr1"))).thenReturn("arr1");
        assertEquals(singletonList("arr1"), arrangementItemService.getInternalId("extArr1", null));
    }

    @Test
    public void shouldGetExternalToInternalIds() {
        String externalId = "externalId";
        String internalId = "internalId";
        List<String> externalIds = singletonList(externalId);

        AccountArrangementItem accountArrangementItem = new AccountArrangementItem();
        accountArrangementItem.setId(internalId);
        accountArrangementItem.setExternalArrangementId(externalId);

        AccountArrangementItems arrangementsResponse = new AccountArrangementItems()
            .arrangementElements(singletonList(accountArrangementItem));

        AccountArrangementsFilter arrangementFilterParams = new AccountArrangementsFilter()
            .externalArrangementIds(externalIds)
            .size(externalIds.size());

        when(arrangementsService.postFilter(refEq((arrangementFilterParams))))
            .thenReturn(arrangementsResponse);

        Map<String, List<String>> externalToInternalIds = arrangementItemService
            .mapExternalToInternalIds(new HashSet<>(externalIds), null);

        assertNotNull(externalToInternalIds);
        assertThat(externalToInternalIds.get(externalId), contains(internalId));
    }

    @Test
    public void shouldGetEmptyInternalIdsWhenEmptyExternalIdsArePassed() {
        Set<String> externalIds = emptySet();
        Map<String, List<String>> externalToInternalIds = arrangementItemService
            .mapExternalToInternalIds(externalIds, null);

        assertNotNull(externalToInternalIds);
        assertEquals(0, externalToInternalIds.keySet().size());
        verify(arrangementsService, times(0)).postFilter(any(AccountArrangementsFilter.class));
    }

    @Test
    public void shouldValidateArrangementItemByServiceAgreementId() {
        String internalId = "internalId";
        String serviceAgreementId = "saId";
        List<String> internalIds = singletonList(internalId);

        ServiceAgreementParticipantsGetResponseBody serviceAgreementParticipantsGetResponseBody =
            new ServiceAgreementParticipantsGetResponseBody()
                .withId("id")
                .withName("Name")
                .withExternalId("externalId")
                .withSharingAccounts(true)
                .withSharingUsers(true);

        InternalRequest<List<ServiceAgreementParticipantsGetResponseBody>> participants =
            getInternalRequest(singletonList(serviceAgreementParticipantsGetResponseBody));

        when(getServiceAgreementParticipants
            .getServiceAgreementParticipants(
                any(InternalRequest.class),
                eq(serviceAgreementId))).thenReturn(participants);

        List<String> participantIds = participants.getData().stream()
            .filter(ServiceAgreementParticipantsGetResponseBody::getSharingAccounts)
            .map(ServiceAgreementParticipantsGetResponseBody::getId)
            .collect(Collectors.toList());

        AccountPresentationArrangementLegalEntityIds persistenceArrangementsLegalEntityIds = new AccountPresentationArrangementLegalEntityIds()
            .legalEntityIds(participantIds);

        AccountArrangementsLegalEntities persistenceArrangementsLegalEntitiesBody =
            new AccountArrangementsLegalEntities()
                .arrangementsLegalEntities(singletonList(persistenceArrangementsLegalEntityIds));

        when(arrangementsService.getArrangementsLegalEntities(eq(internalIds), eq(participantIds)))
            .thenReturn(persistenceArrangementsLegalEntitiesBody);

        arrangementItemService.validate(internalIds, serviceAgreementId);

        verify(arrangementsService).getArrangementsLegalEntities(anyList(), anyList());
        verify(getServiceAgreementParticipants)
            .getServiceAgreementParticipants(any(InternalRequest.class), eq(serviceAgreementId));
    }

    @Test
    public void shouldValidateArrangementItemByServiceAgreementIdWithEmptyDataItems() {
        String serviceAgreementId = "saId";
        List<String> dataItems = emptyList();
        arrangementItemService.validate(dataItems, serviceAgreementId);

        verify(arrangementsService, times(0))
            .getArrangementsLegalEntities(anyList(), anyList());
        verify(getServiceAgreementParticipants, times(0))
            .getServiceAgreementParticipants(any(InternalRequest.class), eq(serviceAgreementId));
    }

    @Test
    public void shouldValidateArrangementItemsByParticipantIds() {
        String internalId = "internalId";
        List<String> internalIds = singletonList(internalId);

        ServiceAgreementParticipantsGetResponseBody serviceAgreementParticipantsGetResponseBody =
            new ServiceAgreementParticipantsGetResponseBody()
                .withId("id")
                .withName("Name")
                .withExternalId("externalId")
                .withSharingAccounts(true)
                .withSharingUsers(true);

        InternalRequest<List<ServiceAgreementParticipantsGetResponseBody>> participants =
            getInternalRequest(singletonList(serviceAgreementParticipantsGetResponseBody));

        List<String> participantIds = participants.getData().stream()
            .filter(ServiceAgreementParticipantsGetResponseBody::getSharingAccounts)
            .map(ServiceAgreementParticipantsGetResponseBody::getId)
            .collect(Collectors.toList());

        AccountPresentationArrangementLegalEntityIds persistenceArrangementsLegalEntityIds = new AccountPresentationArrangementLegalEntityIds()
            .legalEntityIds(participantIds);
        AccountArrangementsLegalEntities persistenceArrangementsLegalEntitiesBody = new AccountArrangementsLegalEntities()
                .arrangementsLegalEntities(singletonList(persistenceArrangementsLegalEntityIds));
        when(arrangementsService.getArrangementsLegalEntities(eq(internalIds), eq(participantIds)))
            .thenReturn(persistenceArrangementsLegalEntitiesBody);

        arrangementItemService.validate(internalIds, participantIds);

        verify(arrangementsService, times(1)).getArrangementsLegalEntities(anyList(), anyList());
    }

    @Test
    public void shouldValidateArrangementItemsByParticipantIdsWithEmptyDataItems() {
        ServiceAgreementParticipantsGetResponseBody serviceAgreementParticipantsGetResponseBody =
            new ServiceAgreementParticipantsGetResponseBody()
                .withId("id")
                .withName("Name")
                .withExternalId("externalId")
                .withSharingAccounts(true)
                .withSharingUsers(true);

        InternalRequest<List<ServiceAgreementParticipantsGetResponseBody>> participants =
            getInternalRequest(singletonList(serviceAgreementParticipantsGetResponseBody));

        String[] participantIds = participants.getData().stream()
            .filter(ServiceAgreementParticipantsGetResponseBody::getSharingAccounts)
            .map(ServiceAgreementParticipantsGetResponseBody::getId)
            .toArray(String[]::new);

        List<String> dataItems = emptyList();
        arrangementItemService.validate(dataItems, asList(participantIds));

        verify(arrangementsService, times(0))
            .getArrangementsLegalEntities(anyList(), anyList());
    }

    @Test
    public void shouldThrowBadRequestWhenArrangementValidationHasFailed() {
        String internalId = "internalId";
        String internalId2 = "internalId2";
        String serviceAgreementId = "saId";
        List<String> internalIds = asList(internalId, internalId2);

        ServiceAgreementParticipantsGetResponseBody serviceAgreementParticipantsGetResponseBody =
            new ServiceAgreementParticipantsGetResponseBody()
                .withId("id")
                .withName("Name")
                .withExternalId("externalId")
                .withSharingAccounts(true)
                .withSharingUsers(true);

        InternalRequest<List<ServiceAgreementParticipantsGetResponseBody>> participants =
            getInternalRequest(singletonList(serviceAgreementParticipantsGetResponseBody));

        when(getServiceAgreementParticipants
            .getServiceAgreementParticipants(
                any(InternalRequest.class),
                eq(serviceAgreementId))).thenReturn(participants);

        List<String> participantIds = participants.getData().stream()
            .filter(ServiceAgreementParticipantsGetResponseBody::getSharingAccounts)
            .map(ServiceAgreementParticipantsGetResponseBody::getId)
            .collect(Collectors.toList());

        AccountPresentationArrangementLegalEntityIds persistenceArrangementsLegalEntityIds = new AccountPresentationArrangementLegalEntityIds()
            .legalEntityIds(participantIds);
        AccountArrangementsLegalEntities persistenceArrangementsLegalEntitiesBody = new AccountArrangementsLegalEntities()
            .arrangementsLegalEntities(singletonList(persistenceArrangementsLegalEntityIds));

        when(arrangementsService.getArrangementsLegalEntities(eq(internalIds), eq(participantIds)))
            .thenReturn(persistenceArrangementsLegalEntitiesBody);

        BadRequestException badRequestException = assertThrows(BadRequestException.class, () -> arrangementItemService
            .validate(internalIds, serviceAgreementId));

        assertThat(badRequestException,
            is(new BadRequestErrorMatcher(ERR_AG_089.getErrorMessage(), ERR_AG_089.getErrorCode())));
    }

    @Test
    public void shouldConvertNotFoundToBadRequestException() {

        when(arrangementsService.getInternalId(anyString())).thenThrow(new NotFoundException());

        BadRequestException badRequestException = assertThrows(BadRequestException.class,
            () -> arrangementItemService.getInternalId("arr1", null));

        assertThat(badRequestException,
            is(new BadRequestErrorMatcher(ERR_AG_098.getErrorMessage(), ERR_AG_098.getErrorCode())));
    }
}