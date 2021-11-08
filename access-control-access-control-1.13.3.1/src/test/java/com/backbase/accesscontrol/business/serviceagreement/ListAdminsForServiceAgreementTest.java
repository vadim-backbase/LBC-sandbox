package com.backbase.accesscontrol.business.serviceagreement;

import static com.backbase.accesscontrol.util.helpers.RequestUtils.getInternalRequest;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.business.service.UserManagementService;
import com.backbase.accesscontrol.dto.ListElementsWrapper;
import com.backbase.accesscontrol.service.ObjectConverter;
import com.backbase.accesscontrol.service.impl.ServiceAgreementAdminService;
import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.serviceagreements.ServiceAgreementAdminsGetResponseBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementUsersGetResponseBody;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ListAdminsForServiceAgreementTest {

    @Mock
    private UserManagementService userManagementService;
    @Mock
    private ServiceAgreementAdminService serviceAgreementAdminService;
    @Spy
    private ObjectConverter objectConverter = new ObjectConverter(spy(ObjectMapper.class));

    @InjectMocks
    private ListAdminsForServiceAgreement listAdminsForServiceAgreement;

    @Before
    public void setUp() throws Exception {
        listAdminsForServiceAgreement = new ListAdminsForServiceAgreement(
            userManagementService,
            objectConverter,
            serviceAgreementAdminService
        );
    }

    @Test
    public void shouldPassIfGetAdminsForServiceAgreementIsInvoked() {
        String serviceAgreementId = "SA01";
        String consumerAdmin = "adminC1";
        String providerAdmin = "adminP1";
        Long totalNumberOfRecords = 10L;
        String externalId = "exid1";
        String externalId1 = "exid2";
        String legalEntityId = "leid1";
        String legalEntityId1 = "leid2";

        Set<String> userIds = new HashSet<>(asList(providerAdmin, consumerAdmin));

        ServiceAgreementUsersGetResponseBody providerAdminBody = getServiceAgreementAdminsGetResponseBody(providerAdmin,
            externalId, legalEntityId);
        ServiceAgreementUsersGetResponseBody consumerAdminBody = getServiceAgreementAdminsGetResponseBody(consumerAdmin,
            externalId1, legalEntityId1);
        InternalRequest<List<ServiceAgreementUsersGetResponseBody>> internalRequestServiceAgreementUsersGetResponseBody =
            getInternalRequest(getServiceAgreementAdminsData(providerAdminBody, consumerAdminBody));

        ServiceAgreementUsersGetResponseBody serviceAgreementUsersGetResponseBody = getServiceAgreementUsersGetResponseBody(
            providerAdmin, externalId, legalEntityId);
        ServiceAgreementUsersGetResponseBody serviceAgreementUsersGetResponseBody1 = getServiceAgreementUsersGetResponseBody(
            consumerAdmin, externalId1, legalEntityId1);
        ListElementsWrapper<ServiceAgreementUsersGetResponseBody> usersOfServiceAgreementDto = getUsersData(
            totalNumberOfRecords,
            serviceAgreementUsersGetResponseBody,
            serviceAgreementUsersGetResponseBody1);

        HashSet<String> adminIds = new HashSet<>(asList(consumerAdmin, providerAdmin));
        ServiceAgreementAdminsGetResponseBody adminsResponseBody = new ServiceAgreementAdminsGetResponseBody()
            .withAdmins(adminIds);

        when(serviceAgreementAdminService
            .getServiceAgreementAdmins(eq(serviceAgreementId)))
            .thenReturn(adminsResponseBody);
        when(userManagementService
            .getUsersForServiceAgreement(eq(userIds), eq(null), eq(null), eq(null),
                eq(null)))
            .thenReturn(usersOfServiceAgreementDto);

        InternalRequest<Void> voidInternalRequest = getInternalRequest(null);
        InternalRequest<List<ServiceAgreementUsersGetResponseBody>> businessProcessResult = listAdminsForServiceAgreement
            .getAdminsForServiceAgreement(voidInternalRequest, serviceAgreementId);

        verify(userManagementService, times(1))
            .getUsersForServiceAgreement(eq(userIds), eq(null), eq(null), eq(null), eq(null));
        assertEquals(internalRequestServiceAgreementUsersGetResponseBody.getData(), businessProcessResult.getData());

    }

    private List<ServiceAgreementUsersGetResponseBody> getServiceAgreementAdminsData(
        ServiceAgreementUsersGetResponseBody providerAdmin,
        ServiceAgreementUsersGetResponseBody consumerAdmin) {
        List<ServiceAgreementUsersGetResponseBody> listOfAdmins = new ArrayList<>();
        listOfAdmins.add(providerAdmin);
        listOfAdmins.add(consumerAdmin);
        return listOfAdmins;
    }

    private ServiceAgreementUsersGetResponseBody getServiceAgreementAdminsGetResponseBody(String id, String externalId,
        String legalEntityId) {
        return new ServiceAgreementUsersGetResponseBody()
            .withId(id)
            .withExternalId(externalId)
            .withLegalEntityId(legalEntityId);
    }

    private ListElementsWrapper<ServiceAgreementUsersGetResponseBody> getUsersData(Long totalNumberOfRecords,
        ServiceAgreementUsersGetResponseBody serviceAgreementUsersGetResponseBody,
        ServiceAgreementUsersGetResponseBody serviceAgreementUsersGetResponseBody2) {
        List<ServiceAgreementUsersGetResponseBody> serviceAgreementUsersGetResponseBodies = new ArrayList<>();
        serviceAgreementUsersGetResponseBodies.add(serviceAgreementUsersGetResponseBody);
        serviceAgreementUsersGetResponseBodies.add(serviceAgreementUsersGetResponseBody2);

        return new ListElementsWrapper<>(serviceAgreementUsersGetResponseBodies, totalNumberOfRecords);
    }

    private ServiceAgreementUsersGetResponseBody getServiceAgreementUsersGetResponseBody(String id, String externalId,
        String legalEntityId) {
        return new ServiceAgreementUsersGetResponseBody()
            .withId(id)
            .withExternalId(externalId)
            .withLegalEntityId(legalEntityId);
    }
}
