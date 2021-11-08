package com.backbase.accesscontrol.business.serviceagreement;

import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_028;
import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_030;
import static com.backbase.accesscontrol.util.helpers.RequestUtils.getInternalRequest;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.business.persistence.serviceagreement.UpdateServiceAgreementAdminsHandler;
import com.backbase.accesscontrol.business.service.UserManagementService;
import com.backbase.accesscontrol.business.serviceagreement.service.ServiceAgreementBusinessRulesService;
import com.backbase.accesscontrol.business.serviceagreement.service.ServiceAgreementClientCommunicationService;
import com.backbase.accesscontrol.business.serviceagreement.service.ServiceAgreementValidator;
import com.backbase.accesscontrol.matchers.BadRequestErrorMatcher;
import com.backbase.accesscontrol.service.DateTimeService;
import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.serviceagreements.ServiceAgreementItem;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.AdminsPutRequestBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.LegalEntityAdmins;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import  com.backbase.dbs.user.api.client.v2.model.GetUsersList;

@RunWith(MockitoJUnitRunner.class)
public class UpdateAdminsTest {

    @Mock
    private ServiceAgreementBusinessRulesService serviceAgreementBusinessRulesService;
    @Mock
    private ServiceAgreementClientCommunicationService serviceAgreementClientCommunicationService;
    @Mock
    private UpdateServiceAgreementAdminsHandler updateServiceAgreementAdminsHandler;
    @Mock
    private UserManagementService userManagementService;
    private UpdateAdmins updateAdmins;

    @Before
    public void setUp() {
        ServiceAgreementValidator serviceAgreementValidator = spy(
            new ServiceAgreementValidator(serviceAgreementClientCommunicationService, userManagementService,
                serviceAgreementBusinessRulesService,
                new DateTimeService("UTC")));
        updateAdmins =
            new UpdateAdmins(userManagementService, serviceAgreementClientCommunicationService,
                serviceAgreementValidator,
                updateServiceAgreementAdminsHandler
            );
    }

    @Test
    public void shouldReturnResultWithFailedEventCreatorWhenAdminsNotBelongToTheParticipants() {
        String serviceAgreementId = "SA-01";
        String participantId1 = "LE_004";
        String participantId2 = "LE_001";
        LegalEntityAdmins participant1 = createLegalEntityAdmin(participantId1,
            new HashSet<>(Collections.singletonList("004")));
        LegalEntityAdmins participant2 = createLegalEntityAdmin(participantId2,
            new HashSet<>(Collections.singletonList("001")));

        HashMap<String, Set<String>> adminsToAssign = new HashMap<>();
        adminsToAssign.put(participantId1, newHashSet("004"));
        adminsToAssign.put(participantId2, newHashSet("001"));

        ServiceAgreementItem serviceAgreement = createServiceAgreement(serviceAgreementId);
        serviceAgreement.setCreatorLegalEntity("LE-01");

        AdminsPutRequestBody adminPutRequestBody = createAdminPutRequestBody(asList(participant1, participant2));

        mockGetUsers(newHashSet("004", "001"), Optional.of(new GetUsersList()));
        mockGetUsersByLegalEntity(new ArrayList<>(), new HashMap<>());

        mockAdminUsersNotBelongToTheGivenLegalEntities(new HashMap<>(), adminsToAssign, true);

        InternalRequest<AdminsPutRequestBody> putRequest = getInternalRequest(adminPutRequestBody);

        BadRequestException badRequestException = assertThrows(BadRequestException.class,
            () -> updateAdmins.updateAdmins(putRequest, serviceAgreementId));

        assertThat(badRequestException,
            is(new BadRequestErrorMatcher(ERR_AG_028.getErrorMessage(), ERR_AG_028.getErrorCode())));
    }

    @Test
    public void shouldSuccessfullyUpdateAdmins() {
        String serviceAgreementId = "SA-01";
        String participantId1 = "LE_004";
        String participantId2 = "LE_001";
        LegalEntityAdmins participant1 = createLegalEntityAdmin(participantId1,
            new HashSet<>(Collections.singletonList("004")));
        LegalEntityAdmins participant2 = createLegalEntityAdmin(participantId2,
            new HashSet<>(Collections.singletonList("001")));

        HashMap<String, Set<String>> adminsToAssign = new HashMap<>();
        adminsToAssign.put(participantId1, newHashSet("004"));
        adminsToAssign.put(participantId2, newHashSet("001"));

        ServiceAgreementItem serviceAgreement = createServiceAgreement(serviceAgreementId);
        serviceAgreement.setCreatorLegalEntity("LE-01");

        AdminsPutRequestBody adminPutRequestBody = createAdminPutRequestBody(asList(participant1, participant2));

        mockGetUsers(newHashSet("004", "001"),
            Optional.of(new com.backbase.dbs.user.api.client.v2.model.GetUsersList()));
        mockGetUsersByLegalEntity(new ArrayList<>(), new HashMap<>());

        mockAdminUsersNotBelongToTheGivenLegalEntities(new HashMap<>(), adminsToAssign, false);

        InternalRequest<AdminsPutRequestBody> request = getInternalRequest(adminPutRequestBody);
        when(serviceAgreementBusinessRulesService.isDuplicateParticipant(asList(participantId1, participantId2)))
            .thenReturn(false);

        updateAdmins.updateAdmins(request, serviceAgreementId);
        verify(updateServiceAgreementAdminsHandler, times(1))
            .handleRequest(any(), eq(adminPutRequestBody));
    }

    @Test
    public void testUpdateAdminsWhenNotAllAdminsAreValidUsers() {
        String serviceAgreementId = "SA-01";
        String participantId1 = "LE_004";
        String participantId2 = "LE_001";
        LegalEntityAdmins participant1 = createLegalEntityAdmin(participantId1,
            new HashSet<>(Collections.singletonList("004")));
        LegalEntityAdmins participant2 = createLegalEntityAdmin(participantId2,
            new HashSet<>(Collections.singletonList("001")));

        ServiceAgreementItem serviceAgreement = createServiceAgreement(serviceAgreementId);
        serviceAgreement.setCreatorLegalEntity("LE-01");

        AdminsPutRequestBody adminPutRequestBody = createAdminPutRequestBody(asList(participant1, participant2));

        mockGetUsers(newHashSet("004", "001"), Optional.empty());

        InternalRequest<AdminsPutRequestBody> putRequest = getInternalRequest(adminPutRequestBody);
        BadRequestException badRequestException = assertThrows(BadRequestException.class,
            () -> updateAdmins.updateAdmins(putRequest, serviceAgreementId));

        assertThat(badRequestException,
            is(new BadRequestErrorMatcher(ERR_AG_030.getErrorMessage(), ERR_AG_030.getErrorCode())));
    }

    private LegalEntityAdmins createLegalEntityAdmin(String entityId, Set<String> admins) {
        return new LegalEntityAdmins().withId(entityId).withAdmins(admins);
    }

    private AdminsPutRequestBody createAdminPutRequestBody(List<LegalEntityAdmins> participants) {
        return new AdminsPutRequestBody()
            .withParticipants(participants);
    }

    private void mockAdminUsersNotBelongToTheGivenLegalEntities(HashMap<String, Set<String>> usersByLegalEntity,
        Map<String, Set<String>> adminsToAssign, boolean value) {
        when(serviceAgreementBusinessRulesService
            .adminUsersNotBelongToTheGivenLegalEntities(usersByLegalEntity, adminsToAssign))
            .thenReturn(value);
    }

    private void mockGetUsersByLegalEntity(ArrayList<com.backbase.dbs.user.api.client.v2.model.GetUser> providerUsers,
        HashMap<String, Set<String>> providersByLegalEntity) {
        when(userManagementService.getUsersByLegalEntityId(providerUsers))
            .thenReturn(providersByLegalEntity);
    }

    private void mockGetUsers(Set<String> admins,
        Optional<com.backbase.dbs.user.api.client.v2.model.GetUsersList> providerUsers) {
        when(serviceAgreementClientCommunicationService.getUsers(eq(admins)))
            .thenReturn(providerUsers);
    }

    private ServiceAgreementItem createServiceAgreement(String serviceAgreementId) {
        return new ServiceAgreementItem()
            .withId(serviceAgreementId);
    }
}
