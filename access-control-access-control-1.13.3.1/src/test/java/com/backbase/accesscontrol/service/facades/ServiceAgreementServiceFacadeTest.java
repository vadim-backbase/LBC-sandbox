package com.backbase.accesscontrol.service.facades;


import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.domain.LegalEntity;
import com.backbase.accesscontrol.domain.ServiceAgreement;
import com.backbase.accesscontrol.dto.SearchAndPaginationParameters;
import com.backbase.accesscontrol.dto.UserParameters;
import com.backbase.accesscontrol.service.impl.PersistenceServiceAgreementService;
import com.backbase.accesscontrol.service.impl.ServiceAgreementAdminService;
import com.backbase.accesscontrol.util.ServiceAgreementsUtils;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.serviceagreements.Participant;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.serviceagreements.PersistenceServiceAgreement;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.serviceagreements.ServiceAgreementAdminsGetResponseBody;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.serviceagreements.ServiceAgreementItem;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.serviceagreements.ServiceAgreementUsersGetResponseBody;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.serviceagreements.Status;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.serviceagreements.PersistenceServiceAgreements;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

@RunWith(MockitoJUnitRunner.class)
public class ServiceAgreementServiceFacadeTest {

    @InjectMocks
    private ServiceAgreementServiceFacade serviceAgreementServiceFacade;
    @Mock
    private PersistenceServiceAgreementService persistenceServiceAgreementService;
    @Mock
    private ServiceAgreementAdminService serviceAgreementAdminService;
    @Mock
    private ServiceAgreementsUtils serviceAgreementsUtils;

    @Test
    public void testShouldGetServiceAgreementById() {
        String serviceAgreementId = "SA-001";

        ServiceAgreementItem serviceAgreement = new ServiceAgreementItem();
        serviceAgreement.setStatus(Status.DISABLED);
        serviceAgreement.setId(serviceAgreementId);
        serviceAgreement.setName("Service Agreement 1");
        LegalEntity legalEntity = new LegalEntity();
        legalEntity.setId(UUID.randomUUID().toString());
        serviceAgreement.setCreatorLegalEntity("LE-01");
        when(persistenceServiceAgreementService.getServiceAgreementResponseBodyById(serviceAgreementId))
            .thenReturn(serviceAgreement);

        ServiceAgreementItem responseBody =
            serviceAgreementServiceFacade.getServiceAgreementResponseBodyById(serviceAgreementId);

        assertEquals(serviceAgreementId, responseBody.getId());
        assertEquals(serviceAgreement.getName(), responseBody.getName());
        assertEquals(serviceAgreement.getDescription(), responseBody.getDescription());
        assertEquals(serviceAgreement.getStatus(), responseBody.getStatus());
    }

    @Test
    public void testShouldGetServiceAgreementAdmin() {
        String serviceAgreementId = "SA ID";
        ServiceAgreementAdminsGetResponseBody serviceAgreementAdmins = new ServiceAgreementAdminsGetResponseBody();
        when(serviceAgreementAdminService.getServiceAgreementAdmins(serviceAgreementId))
            .thenReturn(serviceAgreementAdmins);
        ServiceAgreementAdminsGetResponseBody returnedResponseBody = serviceAgreementServiceFacade
            .getServiceAgreementAdmins(serviceAgreementId);
        assertEquals(serviceAgreementAdmins, returnedResponseBody);
    }

    @Test
    public void testShouldGetServiceAgreementUser() {
        String serviceAgreementId = "SA-ID";
        ServiceAgreementUsersGetResponseBody agreementUsersGetResponseBody = new ServiceAgreementUsersGetResponseBody();
        when(persistenceServiceAgreementService.getServiceAgreementUsers(serviceAgreementId))
            .thenReturn(agreementUsersGetResponseBody);

        ServiceAgreementUsersGetResponseBody serviceAgreementUsersGetResponseBody = serviceAgreementServiceFacade
            .getServiceAgreementUsers(serviceAgreementId);

        assertEquals(agreementUsersGetResponseBody, serviceAgreementUsersGetResponseBody);
    }

    @Test
    public void testShouldGetServiceAgreementParticipants() {
        String serviceAgreementId = "SA-ID";
        List<Participant> response = singletonList(
            new Participant()
                .withId("id")
                .withExternalId("ex-id")
                .withName("name")
                .withSharingAccounts(false)
                .withSharingUsers(true)
        );
        when(persistenceServiceAgreementService.getServiceAgreementParticipants(serviceAgreementId))
            .thenReturn(response);

        List<Participant> participants = serviceAgreementServiceFacade.getServiceAgreementParticipants
            (serviceAgreementId);

        assertEquals(response, participants);
    }

    @Test
    public void testListServiceAgreements() {

        String creatorId = "le";
        UserParameters userParameters = new UserParameters("userId", "userLegalEntityId");
        SearchAndPaginationParameters searchAndPaginationParameters = new SearchAndPaginationParameters(0, 10, "", "");
        ServiceAgreement sa = new ServiceAgreement().withId("sa");
        List<ServiceAgreement> serviceAgreement = singletonList(sa);
        Page<ServiceAgreement> response = new PageImpl<>(serviceAgreement);
        when(persistenceServiceAgreementService
            .listServiceAgreements(creatorId,
                userParameters,
                searchAndPaginationParameters)).thenReturn(response);
        PersistenceServiceAgreement psa = new PersistenceServiceAgreement()
            .withId("sa");
        List<PersistenceServiceAgreement> responsePersistence = new ArrayList<>(singletonList(psa));
        when(serviceAgreementsUtils
            .transformToPersistenceServiceAgreements(
                response.getContent())).thenReturn(responsePersistence);
        PersistenceServiceAgreements persistenceServiceAgreements = serviceAgreementServiceFacade
            .listServiceAgreements(creatorId, userParameters, searchAndPaginationParameters);
        assertEquals(persistenceServiceAgreements.getServiceAgreements(), responsePersistence);
    }
}
