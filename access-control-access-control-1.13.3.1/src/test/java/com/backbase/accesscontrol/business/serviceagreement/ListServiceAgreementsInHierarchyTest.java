package com.backbase.accesscontrol.business.serviceagreement;

import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.business.service.UserManagementService;
import com.backbase.accesscontrol.domain.ServiceAgreement;
import com.backbase.accesscontrol.dto.SearchAndPaginationParameters;
import com.backbase.accesscontrol.dto.UserParameters;
import com.backbase.accesscontrol.mappers.ServiceAgreementsListInHierarchyMapper;
import com.backbase.accesscontrol.service.impl.PersistenceServiceAgreementService;
import com.backbase.accesscontrol.util.ServiceAgreementsUtils;
import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.serviceagreements.PersistenceServiceAgreement;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

@RunWith(MockitoJUnitRunner.class)
public class ListServiceAgreementsInHierarchyTest {

    @Mock
    private PersistenceServiceAgreementService persistenceServiceAgreementService;
    @Mock
    private UserManagementService userManagementService;
    @Mock
    private ServiceAgreementsUtils serviceAgreementsUtils;
    @Mock
    private ServiceAgreementsListInHierarchyMapper serviceAgreementsMapper;

    @InjectMocks
    private ListServiceAgreementsHierarchy listServiceAgreements;

    @Test
    public void shouldPassIfGetServiceAgreementInHierarchyIsInvoked() {
        String query = "";
        String cursor = "";
        Integer from = 0;
        Integer size = 10;
        String creatorId = "LE-01";
        String userId = "userId";
        String legalEntityId = "LeId";
        String serviceAgreementId = "SA-01";
        List<PersistenceServiceAgreement> serviceAgreements = Arrays.asList(
            new PersistenceServiceAgreement()
                .withId(serviceAgreementId),
            new PersistenceServiceAgreement()
                .withId("SA-02"));

        com.backbase.dbs.user.api.client.v2.model.GetUser userGetResponseBody = new com.backbase.dbs.user.api.client.v2.model.GetUser();
        userGetResponseBody.setId(userId);
        userGetResponseBody.setLegalEntityId(legalEntityId);

        when(userManagementService.getUserByInternalId(eq(userId)))
            .thenReturn(userGetResponseBody);
        UserParameters userParameters = new UserParameters(userId, legalEntityId);
        SearchAndPaginationParameters searchAndPaginationParameters = new SearchAndPaginationParameters(from, size,
            query, cursor);
        Page<ServiceAgreement> serviceAgreementsResponse = new PageImpl<>(
            Collections.singletonList(new ServiceAgreement().withId(serviceAgreementId)));
        when(persistenceServiceAgreementService
            .listServiceAgreements(eq(creatorId), refEq(userParameters), refEq(searchAndPaginationParameters)))
            .thenReturn(serviceAgreementsResponse);
        when(serviceAgreementsUtils
            .transformToPersistenceServiceAgreements(serviceAgreementsResponse.getContent()))
            .thenReturn(serviceAgreements);
        listServiceAgreements
            .getServiceAgreements(new InternalRequest<>(), creatorId, userId, query, from, size, cursor);

        verify(userManagementService, times(1))
            .getUserByInternalId(eq(userId));
        verify(persistenceServiceAgreementService, times(1))
            .listServiceAgreements(eq(creatorId), refEq(userParameters), refEq(searchAndPaginationParameters));

        verify(serviceAgreementsMapper, times(1)).mapList(refEq(serviceAgreements));
    }
}
