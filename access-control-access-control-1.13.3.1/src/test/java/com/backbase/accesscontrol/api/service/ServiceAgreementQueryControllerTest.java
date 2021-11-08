package com.backbase.accesscontrol.api.service;

import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_071;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.mappers.model.PayloadConverter;
import com.backbase.accesscontrol.mappers.model.query.service.ParticipantToParticipantMapper;
import com.backbase.accesscontrol.mappers.model.query.service.PersistenceServiceAgreementDataGroupsToPersistenceServiceAgreementDataGroupsMapper;
import com.backbase.accesscontrol.mappers.model.query.service.ServiceAgreementAdminsGetResponseBodyToServiceAgreementAdminsMapper;
import com.backbase.accesscontrol.mappers.model.query.service.ServiceAgreementItemToServiceAgreementItemMapper;
import com.backbase.accesscontrol.mappers.model.query.service.ServiceAgreementItemToServiceAgreementItemQueryMapper;
import com.backbase.accesscontrol.mappers.model.query.service.ServiceAgreementUsersGetResponseBodyToServiceAgreementUsersMapper;
import com.backbase.accesscontrol.matchers.BadRequestErrorMatcher;
import com.backbase.accesscontrol.service.facades.ServiceAgreementServiceFacade;
import com.backbase.accesscontrol.service.impl.PersistenceServiceAgreementService;
import com.backbase.accesscontrol.service.rest.spec.model.ServiceAgreementAdmins;
import com.backbase.accesscontrol.service.rest.spec.model.ServiceAgreementParticipantsGetResponseBody;
import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.serviceagreements.Participant;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.serviceagreements.ServiceAgreementAdminsGetResponseBody;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.serviceagreements.ServiceAgreementItem;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.serviceagreements.ServiceAgreementUsersGetResponseBody;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.serviceagreements.PersistenceDataGroupDataItems;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.serviceagreements.PersistenceServiceAgreementDataGroups;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ServiceAgreementQueryControllerTest {

    @InjectMocks
    private ServiceAgreementQueryController serviceAgreementsQueryController;

    @Mock
    private ServiceAgreementServiceFacade serviceAgreementServiceFacade;

    @Mock
    private PersistenceServiceAgreementService persistenceServiceAgreementService;
    @Spy
    private PayloadConverter payloadConverter =
        new PayloadConverter(asList(
            spy(Mappers.getMapper(ServiceAgreementItemToServiceAgreementItemMapper.class)),
            spy(Mappers.getMapper(ServiceAgreementItemToServiceAgreementItemQueryMapper.class)),
            spy(Mappers.getMapper(ServiceAgreementAdminsGetResponseBodyToServiceAgreementAdminsMapper.class)),
            spy(Mappers.getMapper(ServiceAgreementUsersGetResponseBodyToServiceAgreementUsersMapper.class)),
            spy(Mappers
                .getMapper(PersistenceServiceAgreementDataGroupsToPersistenceServiceAgreementDataGroupsMapper.class)),
            spy(Mappers.getMapper(ParticipantToParticipantMapper.class))
        ));

    @Test
    public void shouldReturnServiceAgreementById() {
        String id = "SA-01";
        ServiceAgreementItem response = new ServiceAgreementItem()
            .withId(id);

        when(serviceAgreementServiceFacade.getServiceAgreementResponseBodyById(anyString()))
            .thenReturn(response);

        com.backbase.accesscontrol.service.rest.spec.model.ServiceAgreementItemQuery serviceAgreement =
            serviceAgreementsQueryController
                .getServiceAgreement(id).getBody();

        verify(serviceAgreementServiceFacade).getServiceAgreementResponseBodyById(eq(id));

        assertEquals(response.getId(), serviceAgreement.getId());
    }

    @Test
    public void shouldReturnParticipantsByServiceAgreementId() {
        String serviceAgreementId = "id.SA-01";

        List<Participant> response = new ArrayList<>();
        response.add(new Participant()
            .withId(serviceAgreementId));
        when(serviceAgreementServiceFacade.getServiceAgreementParticipants(anyString()))
            .thenReturn(response);

        List<ServiceAgreementParticipantsGetResponseBody> serviceAgreementParticipants =
            serviceAgreementsQueryController.getServiceAgreementParticipantsQuery(serviceAgreementId).getBody();

        verify(serviceAgreementServiceFacade)
            .getServiceAgreementParticipants(serviceAgreementId);

        assertEquals(response.get(0).getId(), serviceAgreementParticipants.get(0).getId());
        assertEquals(response.get(0).getExternalId(), serviceAgreementParticipants.get(0).getExternalId());
    }

    @Test
    public void shouldGetServiceAgreementAdmins() {
        String serviceAgreementId = "Some ID";

        ServiceAgreementAdminsGetResponseBody response = new ServiceAgreementAdminsGetResponseBody()
            .withAdmins(Sets.newHashSet("id1", "id2"));
        when(serviceAgreementServiceFacade.getServiceAgreementAdmins(anyString()))
            .thenReturn(response);
        ServiceAgreementAdmins returnedResponse =
            serviceAgreementsQueryController.getServiceAgreementAdmins(serviceAgreementId).getBody();
        verify(serviceAgreementServiceFacade).getServiceAgreementAdmins(serviceAgreementId);

        assertArrayEquals(response.getAdmins().toArray(), returnedResponse.getAdmins().toArray());
    }

    @Test
    public void shouldGetServiceAgreementUsers() {
        String serviceAgreementId = "id.SA-01";

        ServiceAgreementUsersGetResponseBody response = new ServiceAgreementUsersGetResponseBody()
            .withUserIds(Sets.newHashSet("u1", "u2"));

        when(serviceAgreementServiceFacade.getServiceAgreementUsers(anyString()))
            .thenReturn(response);

        com.backbase.accesscontrol.service.rest.spec.model.ServiceAgreementUsersQuery serviceAgreementUsers =
            serviceAgreementsQueryController
                .getServiceAgreementUsers(serviceAgreementId).getBody();

        verify(serviceAgreementServiceFacade, times(1)).
            getServiceAgreementUsers(serviceAgreementId);

        assertArrayEquals(response.getUserIds().toArray(), serviceAgreementUsers.getUserIds().toArray());
    }

    @Test
    public void shouldThrowBadRequestWhenPrivilegesProvidedWithoutFunctionNameOrResourceName() {

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> serviceAgreementsQueryController
                .getServiceAgreementsDataGroups("userId", "ARRANGEMENTS",
                    null, null, "view,create"
                ));

        assertThat(exception, new BadRequestErrorMatcher(ERR_ACQ_071.getErrorMessage(), ERR_ACQ_071.getErrorCode()));
    }

    @Test
    public void shouldGetServiceAgreementsDataGroups() {
        String userId = "userId";
        String dataGroupType = "ARRANGEMENTS";
        String resourceName = "Transactions";
        String functionName = "Payments";
        String privileges = "view,create";

        List<PersistenceServiceAgreementDataGroups> mockedResponse = asList(
            new PersistenceServiceAgreementDataGroups()
                .withServiceAgreementId("saId-01")
                .withDataGroups(
                    asList(
                        new PersistenceDataGroupDataItems().withId("dgId-01").withItems(asList("item-01", "item-02")),
                        new PersistenceDataGroupDataItems().withId("dgId-02").withItems(asList("item-03", "item-04"))
                    )
                ),
            new PersistenceServiceAgreementDataGroups()
                .withServiceAgreementId("saId-02")
                .withDataGroups(
                    asList(
                        new PersistenceDataGroupDataItems().withId("dgId-03").withItems(asList("item-05", "item-06")),
                        new PersistenceDataGroupDataItems().withId("dgId-04").withItems(asList("item-07", "item-08"))
                    )
                )
        );

        when(persistenceServiceAgreementService
            .getServiceAgreementsDataGroups(eq(userId), eq(dataGroupType), eq(functionName), eq(resourceName),
                eq(privileges))).thenReturn(mockedResponse);

        List<com.backbase.accesscontrol.service.rest.spec.model.PersistenceServiceAgreementDataGroups> response =
            serviceAgreementsQueryController
                .getServiceAgreementsDataGroups(userId, dataGroupType, resourceName, functionName, privileges)
                .getBody();

        assertEquals(payloadConverter.convertListPayload(mockedResponse,
            com.backbase.accesscontrol.service.rest.spec.model.PersistenceServiceAgreementDataGroups.class)
            , response);
    }
}
