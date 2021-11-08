package com.backbase.accesscontrol.service.impl;

import static com.backbase.accesscontrol.domain.GraphConstants.SERVICE_AGREEMENT_WITH_PARTICIPANTS_AND_ADMINS_AND_FUNCTION_GROUPS;
import static com.backbase.accesscontrol.matchers.ServiceAgreementMatcher.getServiceAgreementMatcher;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_047;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_048;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_049;
import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_006;
import static com.backbase.accesscontrol.util.helpers.LegalEntityUtil.createLegalEntity;
import static com.backbase.accesscontrol.util.helpers.ServiceAgreementUtil.createParticipantWithAdmin;
import static com.backbase.accesscontrol.util.helpers.ServiceAgreementUtil.createServiceAgreement;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.domain.GraphConstants;
import com.backbase.accesscontrol.domain.LegalEntity;
import com.backbase.accesscontrol.domain.Participant;
import com.backbase.accesscontrol.domain.ServiceAgreement;
import com.backbase.accesscontrol.domain.UserContext;
import com.backbase.accesscontrol.matchers.BadRequestErrorMatcher;
import com.backbase.accesscontrol.matchers.NotFoundErrorMatcher;
import com.backbase.accesscontrol.repository.FunctionGroupJpaRepository;
import com.backbase.accesscontrol.repository.ParticipantJpaRepository;
import com.backbase.accesscontrol.repository.ServiceAgreementAdminJpaRepository;
import com.backbase.accesscontrol.repository.ServiceAgreementJpaRepository;
import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.buildingblocks.presentation.errors.NotFoundException;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.serviceagreements.ServiceAgreementAdminsGetResponseBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.AdminsPutRequestBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.LegalEntityAdmins;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ServiceAgreementAdminServiceTest {

    @Mock
    private ServiceAgreementJpaRepository serviceAgreementJpaRepository;
    @Mock
    private ParticipantJpaRepository participantJpaRepository;
    @Mock
    private UserContextService userContextService;
    @Mock
    private UserAccessFunctionGroupService userAccessFunctionGroupService;
    @Mock
    private ServiceAgreementSystemFunctionGroupService serviceAgreementSystemFunctionGroupService;
    @Mock
    private FunctionGroupJpaRepository functionGroupJpaRepository;
    @Mock
    private ServiceAgreementAdminJpaRepository serviceAgreementAdminJpaRepository;
    @Captor
    private ArgumentCaptor<Participant> participantCaptor;
    @InjectMocks
    private ServiceAgreementAdminService serviceAgreementService;

    @Test
    public void testGetServiceAgreementAdmin() {
        String serviceAgreementId = "sa ID";
        String userId1 = "user 1";
        String userId2 = "user 2";
        String userId3 = "user 3";

        Participant participant1 = new Participant();
        Participant participant2 = new Participant();
        ServiceAgreement serviceAgreement = new ServiceAgreement();
        serviceAgreement.setId(serviceAgreementId);

        participant1.addAdmin(userId1);
        participant1.addAdmin(userId2);
        participant1.setServiceAgreement(serviceAgreement);

        participant2.addAdmin(userId1);
        participant2.addAdmin(userId3);
        participant2.setServiceAgreement(serviceAgreement);

        when(participantJpaRepository
            .findByServiceAgreementId(serviceAgreementId, GraphConstants.PARTICIPANT_WITH_LEGAL_ENTITY)).thenReturn(
            asList(participant1, participant2)
        );
        when(serviceAgreementJpaRepository.existsById(serviceAgreementId)).thenReturn(true);
        ServiceAgreementAdminsGetResponseBody serviceAgreementAdmins = serviceAgreementService
            .getServiceAgreementAdmins(serviceAgreementId);
        assertThat(serviceAgreementAdmins.getAdmins(), hasSize(3));
        assertThat(serviceAgreementAdmins.getAdmins(), hasItems(userId1, userId2, userId3));
    }

    @Test
    public void testGetServiceAgreementAdminThrowExceptionWhenServiceAgreementNotExists() {
        String serviceAgreementId = "sa ID";
        when(serviceAgreementJpaRepository.existsById(serviceAgreementId)).thenReturn(false);

        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> serviceAgreementService.getServiceAgreementAdmins(serviceAgreementId));
        assertThat(exception, new NotFoundErrorMatcher(ERR_ACQ_006.getErrorMessage(), ERR_ACQ_006.getErrorCode()));
    }

    @Test
    public void testRemoveAdminInServiceAgreementProviderSuccessful() {
        String externalServiceAgreementId = "SA-001-external";
        String participantId = "LE-01";
        String userId = "U-01";
        String serviceAgreementId = "SA-01";
        String adminProvider = "adminProvider";

        UserContext userContext = new UserContext();
        userContext.setId(1L);
        ServiceAgreement serviceAgreement = createServiceAgreement("name", externalServiceAgreementId, "desc",
            new LegalEntity(), null, participantId);
        serviceAgreement.setId(serviceAgreementId);
        serviceAgreement.getParticipants().get(participantId).addAdmin(userId);
        serviceAgreement.getParticipants().get(participantId).addAdmin(adminProvider);

        Participant provider = new Participant();
        provider.setShareUsers(true);
        provider.addAdmin(userId);
        provider.addAdmin(adminProvider);

        mockGetServiceAgreementByExternalId(externalServiceAgreementId, serviceAgreement);
        mockGetParticipantsBySaIdAndLegalEntityId(externalServiceAgreementId, participantId, Optional.of(provider));
        mockFindUserContextByUserIdAndServiceAgreementId(serviceAgreementId, userContext);
        mockGetServiceAgreementFunctionGroups(serviceAgreement);
        mockCheckExistsAdminInServiceAgreement(false);

        serviceAgreementService.removeAdminFromServiceAgreementBatch(externalServiceAgreementId, userId, participantId);

        verify(participantJpaRepository, times(1)).save(participantCaptor.capture());
        verify(functionGroupJpaRepository).deleteById("sys-fg-id");
        assertFalse(participantCaptor.getValue().getAdmins().containsKey(userId));
        assertTrue(participantCaptor.getValue().getAdmins().containsKey(adminProvider));
    }

    @Test
    public void testRemoveAdminThrowExceptionWhenLegalEntityIsNotParticipantInServiceAgreement() {
        String externalServiceAgreementId = "SA-001-external";
        String legalEntityId = "LE-01";
        String userId = "U-01";
        String serviceAgreementId = "SA-01";

        UserContext userContext = new UserContext();
        userContext.setId(1L);

        ServiceAgreement serviceAgreement = createServiceAgreement("name", externalServiceAgreementId, "desc",
            new LegalEntity(), null, null);
        serviceAgreement.setId(serviceAgreementId);

        mockGetServiceAgreementByExternalId(externalServiceAgreementId, serviceAgreement);
        mockGetParticipantsBySaIdAndLegalEntityId(externalServiceAgreementId, legalEntityId, Optional.empty());
        mockFindUserContextByUserIdAndServiceAgreementId(serviceAgreementId, userContext);

        BadRequestException exception = assertThrows(BadRequestException.class, () -> serviceAgreementService
            .removeAdminFromServiceAgreementBatch(externalServiceAgreementId, userId, legalEntityId));
        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_047.getErrorMessage(), ERR_ACC_047.getErrorCode()));
    }

    @Test
    public void shouldThrowExceptionOnAddAdminInServiceAgreementProviderWhenUserIsNotAdmin() {
        String externalServiceAgreementId = "SA-001-external";
        String legalEntityId = "LE-01";
        String userId = "U-01";
        String serviceAgreementId = "SA-01";

        UserContext userContext = new UserContext();
        userContext.setId(1L);

        ServiceAgreement serviceAgreement = createServiceAgreement("name", externalServiceAgreementId, "desc",
            new LegalEntity(), "C1", "P1");
        serviceAgreement.setId(serviceAgreementId);

        Participant provider =
            new Participant();
        provider.setShareUsers(true);

        mockGetServiceAgreementByExternalId(externalServiceAgreementId, serviceAgreement);
        mockGetParticipantsBySaIdAndLegalEntityId(externalServiceAgreementId, legalEntityId, Optional.of(provider));
        mockFindUserContextByUserIdAndServiceAgreementId(serviceAgreementId, userContext);

        BadRequestException exception = assertThrows(BadRequestException.class, () -> serviceAgreementService
            .removeAdminFromServiceAgreementBatch(externalServiceAgreementId, userId, legalEntityId));
        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_049.getErrorMessage(), ERR_ACC_049.getErrorCode()));
    }

    @Test
    public void shouldThrowExceptionOnAddAdminInServiceAgreementConsumerWhenUserIsNotAdmin() {
        String externalServiceAgreementId = "SA-001-external";
        String legalEntityId = "LE-01";
        String userId = "U-01";
        String serviceAgreementId = "SA-01";

        UserContext userContext = new UserContext();
        userContext.setId(1L);

        ServiceAgreement serviceAgreement = createServiceAgreement("name", externalServiceAgreementId, "desc",
            new LegalEntity(), legalEntityId, legalEntityId);
        serviceAgreement.setId(serviceAgreementId);

        Participant consumer = new Participant();
        consumer.setShareAccounts(true);

        mockGetServiceAgreementByExternalId(externalServiceAgreementId, serviceAgreement);
        mockGetParticipantsBySaIdAndLegalEntityId(externalServiceAgreementId, legalEntityId, Optional.of(consumer));
        mockFindUserContextByUserIdAndServiceAgreementId(serviceAgreementId, userContext);

        BadRequestException exception = assertThrows(BadRequestException.class, () -> serviceAgreementService
            .removeAdminFromServiceAgreementBatch(externalServiceAgreementId, userId, legalEntityId));
        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_049.getErrorMessage(), ERR_ACC_049.getErrorCode()));
    }

    @Test
    public void testRemoveAdminInServiceAgreementConsumerSuccessful() {
        String externalServiceAgreementId = "SA-001-external";
        String participantId = "LE-01";
        String userId = "U-01";
        String serviceAgreementId = "SA-01";
        String adminConsumer = "adminConsumer";

        UserContext userContext = new UserContext();
        userContext.setId(1L);

        ServiceAgreement serviceAgreement = createServiceAgreement("name", externalServiceAgreementId, "desc",
            new LegalEntity(), participantId, null);
        serviceAgreement.setId(serviceAgreementId);
        serviceAgreement.getParticipants().get(participantId).addAdmin(userId);
        serviceAgreement.getParticipants().get(participantId).addAdmin(adminConsumer);

        Participant consumer = new Participant();
        consumer.setShareAccounts(true);
        consumer.addAdmin(userId);
        consumer.addAdmin(adminConsumer);

        mockGetServiceAgreementByExternalId(externalServiceAgreementId, serviceAgreement);
        mockGetParticipantsBySaIdAndLegalEntityId(externalServiceAgreementId, participantId, Optional.of(consumer));
        mockFindUserContextByUserIdAndServiceAgreementId(serviceAgreementId, userContext);
        mockGetServiceAgreementFunctionGroups(serviceAgreement);
        mockCheckExistsAdminInServiceAgreement(false);

        serviceAgreementService.removeAdminFromServiceAgreementBatch(externalServiceAgreementId, userId, participantId);

        verify(participantJpaRepository, times(1)).save(participantCaptor.capture());
        verify(functionGroupJpaRepository).deleteById(eq("sys-fg-id"));
        assertFalse(participantCaptor.getValue().getAdmins().containsKey(userId));
        assertTrue(participantCaptor.getValue().getAdmins().containsKey(adminConsumer));
    }

    @Test
    public void testRemoveAdminInServiceAgreementProviderAndConsumerSuccessful() {
        String externalServiceAgreementId = "SA-001-external";
        String participantId = "P1";
        String userId = "U-01";
        String serviceAgreementId = "SA-01";
        String adminProvider = "adminProvider";
        String adminConsumer = "adminConsumer";

        UserContext userContext = new UserContext();
        userContext.setId(1L);

        ServiceAgreement serviceAgreement = createServiceAgreement("name", externalServiceAgreementId, "desc",
            new LegalEntity(), participantId, participantId);
        serviceAgreement.setId(serviceAgreementId);
        serviceAgreement.getParticipants().get(participantId).addAdmin(userId);
        serviceAgreement.getParticipants().get(participantId).addAdmin(adminConsumer);
        serviceAgreement.getParticipants().get(participantId).addAdmin(userId);
        serviceAgreement.getParticipants().get(participantId).addAdmin(adminProvider);

        Participant provider = new Participant();
        provider.setShareUsers(true);
        provider.addAdmin(userId);
        provider.addAdmin(adminProvider);
        provider.setShareAccounts(true);
        provider.addAdmin(adminConsumer);

        mockGetServiceAgreementByExternalId(externalServiceAgreementId, serviceAgreement);
        mockGetParticipantsBySaIdAndLegalEntityId(externalServiceAgreementId, participantId, Optional.of(provider));
        mockFindUserContextByUserIdAndServiceAgreementId(serviceAgreementId, userContext);
        mockGetServiceAgreementFunctionGroups(serviceAgreement);
        mockCheckExistsAdminInServiceAgreement(true);

        serviceAgreementService.removeAdminFromServiceAgreementBatch(externalServiceAgreementId, userId, participantId);

        verify(participantJpaRepository).save(participantCaptor.capture());
        List<Participant> participants = participantCaptor.getAllValues();
        verify(functionGroupJpaRepository, times(0)).deleteById(anyString());
        assertFalse(participants.get(0).getAdmins().containsKey(userId));
        assertTrue(participants.get(0).getAdmins().containsKey(adminConsumer));
        assertTrue(participants.get(0).getAdmins().containsKey(adminProvider));
    }

    @Test
    public void testUpdateAdmin() {
        String serviceAgreementId = "SA ID";
        Long userContextId = 1L;

        UserContext userContext = new UserContext();
        userContext.setId(userContextId);

        ServiceAgreement serviceAgreement = createServiceAgreement("name", "id.external", "desc", new LegalEntity(),
            "C1", "P1");
        serviceAgreement.setId(serviceAgreementId);
        serviceAgreement.getParticipants().get("C1").addAdmin("2");
        serviceAgreement.getParticipants().get("C1").addAdmin("1000");
        serviceAgreement.getParticipants().get("P1").addAdmin("7");

        when(serviceAgreementJpaRepository.findById(serviceAgreementId,
            SERVICE_AGREEMENT_WITH_PARTICIPANTS_AND_ADMINS_AND_FUNCTION_GROUPS))
            .thenReturn(Optional.of(serviceAgreement));
        mockFindUserContextByUserIdAndServiceAgreementId(serviceAgreementId, userContext);
        mockGetServiceAgreementFunctionGroups(serviceAgreement);
        mockCheckExistsAdminInServiceAgreement(true);

        AdminsPutRequestBody requestBody = new AdminsPutRequestBody()
            .withParticipants(
                asList(new LegalEntityAdmins().withId("C1").withAdmins(Sets.newHashSet("1", "2", "3")),
                    new LegalEntityAdmins().withId("P1").withAdmins(Sets.newHashSet("4", "5", "6"))));
        serviceAgreementService.updateAdmins(serviceAgreementId, requestBody);
        verify(functionGroupJpaRepository, times(0)).deleteById(anyString());
        assertThat(serviceAgreement,
            getServiceAgreementMatcher(equalTo(serviceAgreementId), equalTo("name"), equalTo("desc"), notNullValue()));
        assertThat(serviceAgreement.getParticipants().get("C1").getAdmins().keySet(),
            containsInAnyOrder(requestBody.getParticipants().get(0).getAdmins().toArray()));
        assertThat(serviceAgreement.getParticipants().get("P1").getAdmins().keySet(),
            containsInAnyOrder(requestBody.getParticipants().get(1).getAdmins().toArray()));
    }

    @Test
    public void shouldThrowBadRequestOnUpdateAdminsWhenParticipantsAreNotValid() {
        String serviceAgreementId = "SA ID";
        Long userContextId = 1L;

        UserContext userContext = new UserContext();
        userContext.setId(userContextId);

        ServiceAgreement serviceAgreement = createServiceAgreement("name", "id.external", "desc", new LegalEntity(),
            "C1", "P1");
        serviceAgreement.setId(serviceAgreementId);
        serviceAgreement.getParticipants().get("C1").addAdmin("2");
        serviceAgreement.getParticipants().get("C1").addAdmin("1000");
        serviceAgreement.getParticipants().get("P1").addAdmin("7");

        when(serviceAgreementJpaRepository.findById(serviceAgreementId,
            SERVICE_AGREEMENT_WITH_PARTICIPANTS_AND_ADMINS_AND_FUNCTION_GROUPS))
            .thenReturn(Optional.of(serviceAgreement));
        mockFindUserContextByUserIdAndServiceAgreementId(serviceAgreementId, userContext);
        mockGetServiceAgreementFunctionGroups(serviceAgreement);

        AdminsPutRequestBody requestBody = new AdminsPutRequestBody()
            .withParticipants(asList(new LegalEntityAdmins().withId("C1").withAdmins(Sets.newHashSet("1", "2", "3")),
                new LegalEntityAdmins().withId("P2").withAdmins(Sets.newHashSet("4", "5", "6"))));

        BadRequestException exception = assertThrows(BadRequestException.class, () -> serviceAgreementService.updateAdmins(serviceAgreementId, requestBody));
        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_047.getErrorMessage(), ERR_ACC_047.getErrorCode()));
    }

    @Test
    public void testUpdateAdminForCallFromServiceAgreementService() {
        String serviceAgreementId = "SA ID";
        Long userContextId = 1L;

        UserContext userContext = new UserContext();
        userContext.setId(userContextId);

        ServiceAgreement serviceAgreement = createServiceAgreement("name", "id.external", "desc", new LegalEntity(),
            "C1", "P1");
        serviceAgreement.setId(serviceAgreementId);
        serviceAgreement.getParticipants().get("C1").addAdmin("2");
        serviceAgreement.getParticipants().get("C1").addAdmin("1000");
        serviceAgreement.getParticipants().get("P1").addAdmin("7");

        mockFindUserContextByUserIdAndServiceAgreementId(serviceAgreementId, userContext);
        mockGetServiceAgreementFunctionGroups(serviceAgreement);
        mockCheckExistsAdminInServiceAgreement(true);

        Set<com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.Participant> requestBody =
            Sets.newHashSet(
                new com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.Participant()
                    .withId("C1").withAdmins(Sets.newHashSet("1", "2", "3")),
                new com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.Participant()
                    .withId("P1").withAdmins(Sets.newHashSet("4", "5", "6")));
        serviceAgreementService.updateAdmins(serviceAgreement, requestBody);
        verify(functionGroupJpaRepository, times(0)).deleteById(anyString());
        assertThat(serviceAgreement,
            getServiceAgreementMatcher(equalTo(serviceAgreementId), equalTo("name"), equalTo("desc"), notNullValue()));
        assertThat(serviceAgreement.getParticipants().get("C1").getAdmins().keySet(),
            containsInAnyOrder("1", "2", "3"));
        assertThat(serviceAgreement.getParticipants().get("P1").getAdmins().keySet(),
            containsInAnyOrder("4", "5", "6"));
    }

    @Test
    public void testAddAdminInServiceAgreementProviderSuccessful() {
        String externalServiceAgreementId = "SA-001-external";
        String legalEntityId = "LE-01";
        String userId = "U-01";
        String serviceAgreementId = "SA-01";

        UserContext userContext = new UserContext();
        userContext.setId(1L);
        ServiceAgreement serviceAgreement = createServiceAgreement("name", externalServiceAgreementId, "desc",
            new LegalEntity(), null, null);
        serviceAgreement.setId(serviceAgreementId);
        Participant provider = new Participant();
        provider.setShareUsers(true);

        mockGetServiceAgreementByExternalId(externalServiceAgreementId, serviceAgreement);
        mockGetParticipantsBySaIdAndLegalEntityId(externalServiceAgreementId, legalEntityId, Optional.of(provider));
        mockFindUserContextByUserIdAndServiceAgreementId(serviceAgreementId, userContext);
        mockGetServiceAgreementFunctionGroups(serviceAgreement);

        serviceAgreementService.addAdminInServiceAgreementBatch(externalServiceAgreementId, userId, legalEntityId);

        verify(participantJpaRepository, times(1)).saveAndFlush(participantCaptor.capture());
        assertTrue(participantCaptor.getValue().getAdmins().containsKey(userId));
    }

    @Test
    public void testAddAdminInServiceAgreementConsumerSuccessful() {
        String externalServiceAgreementId = "SA-001-external";
        String legalEntityId = "LE-01";
        String userId = "U-01";
        String serviceAgreementId = "SA-01";

        UserContext userContext = new UserContext();
        userContext.setId(1L);

        ServiceAgreement serviceAgreement = createServiceAgreement("name", externalServiceAgreementId, "desc",
            new LegalEntity(), null, null);
        serviceAgreement.setId(serviceAgreementId);

        Participant consumer = new Participant();
        consumer.setShareAccounts(true);

        mockGetServiceAgreementByExternalId(externalServiceAgreementId, serviceAgreement);
        mockGetParticipantsBySaIdAndLegalEntityId(externalServiceAgreementId, legalEntityId, Optional.of(consumer));
        mockFindUserContextByUserIdAndServiceAgreementId(serviceAgreementId, userContext);
        mockGetServiceAgreementFunctionGroups(serviceAgreement);

        serviceAgreementService.addAdminInServiceAgreementBatch(externalServiceAgreementId, userId, legalEntityId);

        verify(participantJpaRepository, times(1)).saveAndFlush(participantCaptor.capture());
        assertTrue(participantCaptor.getValue().getAdmins().containsKey(userId));
    }

    @Test
    public void testAddAdminThrowExceptionWhenLegalEntityIsNotParticipantInServiceAgreement() {
        String externalServiceAgreementId = "SA-001-external";
        String legalEntityId = "LE-01";
        String userId = "U-01";
        String serviceAgreementId = "SA-01";

        UserContext userContext = new UserContext();
        userContext.setId(1L);

        ServiceAgreement serviceAgreement = createServiceAgreement("name", externalServiceAgreementId, "desc",
            new LegalEntity(), null, null);
        serviceAgreement.setId(serviceAgreementId);

        mockGetServiceAgreementByExternalId(externalServiceAgreementId, serviceAgreement);
        mockGetParticipantsBySaIdAndLegalEntityId(externalServiceAgreementId, legalEntityId, Optional.empty());
        mockFindUserContextByUserIdAndServiceAgreementId(serviceAgreementId, userContext);

        BadRequestException exception = assertThrows(BadRequestException.class, () -> serviceAgreementService
            .addAdminInServiceAgreementBatch(externalServiceAgreementId, userId, legalEntityId));
        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_047.getErrorMessage(), ERR_ACC_047.getErrorCode()));
    }

    @Test
    public void testAddAdminInServiceAgreementProviderAndConsumerSuccessful() {
        String externalServiceAgreementId = "SA-001-external";
        String legalEntityId = "LE-01";
        String userId = "U-01";
        String serviceAgreementId = "SA-01";

        UserContext userContext = new UserContext();
        userContext.setId(1L);

        ServiceAgreement serviceAgreement = createServiceAgreement("name", externalServiceAgreementId, "desc",
            new LegalEntity(), null, null);
        serviceAgreement.setId(serviceAgreementId);
        Participant provider = new Participant();
        provider.setShareUsers(true);
        provider.setShareAccounts(true);

        mockGetServiceAgreementByExternalId(externalServiceAgreementId, serviceAgreement);
        mockGetParticipantsBySaIdAndLegalEntityId(externalServiceAgreementId, legalEntityId, Optional.of(provider));
        mockFindUserContextByUserIdAndServiceAgreementId(serviceAgreementId, userContext);
        mockGetServiceAgreementFunctionGroups(serviceAgreement);

        serviceAgreementService.addAdminInServiceAgreementBatch(externalServiceAgreementId, userId, legalEntityId);

        verify(participantJpaRepository, times(1)).saveAndFlush(participantCaptor.capture());

        List<Participant> participantsSaved = participantCaptor.getAllValues();

        assertTrue(participantsSaved.get(0).getAdmins().containsKey(userId));
    }

    @Test
    public void shouldThrowExceptionOnAddAdminInServiceAgreementConsumerWhenUserIsAlreadyAdmin() {
        String externalServiceAgreementId = "SA-001-external";
        String legalEntityId = "LE-01";
        String userId = "U-01";
        String serviceAgreementId = "SA-01";

        UserContext userContext = new UserContext();
        userContext.setId(1L);

        ServiceAgreement serviceAgreement = createServiceAgreement("name", externalServiceAgreementId, "desc",
            new LegalEntity(), "C1", "P1");
        serviceAgreement.setId(serviceAgreementId);
        serviceAgreement.getParticipants().get("C1").addAdmin(userId);

        Participant consumer = new Participant();
        consumer.setShareAccounts(true);
        consumer.addAdmin(userId);

        mockGetServiceAgreementByExternalId(externalServiceAgreementId, serviceAgreement);
        mockGetParticipantsBySaIdAndLegalEntityId(externalServiceAgreementId, legalEntityId, Optional.of(consumer));
        mockFindUserContextByUserIdAndServiceAgreementId(serviceAgreementId, userContext);

        BadRequestException exception = assertThrows(BadRequestException.class, () -> serviceAgreementService
            .addAdminInServiceAgreementBatch(externalServiceAgreementId, userId, legalEntityId));
        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_048.getErrorMessage(), ERR_ACC_048.getErrorCode()));
    }

    @Test
    public void shouldAssingPermissionToAdmin() {
        String adminId = "a1";
        Participant participantWithAdmin = createParticipantWithAdmin(adminId, true, true);
        LegalEntity legalEntity = createLegalEntity("l1", null, null, null, null);
        participantWithAdmin.setLegalEntity(legalEntity);

        ServiceAgreement serviceAgreement = new ServiceAgreement();
        serviceAgreement.setDescription("desc");
        serviceAgreement.setCreatorLegalEntity(legalEntity);
        serviceAgreement.setExternalId("new-ex-id");
        serviceAgreement.setMaster(true);
        serviceAgreement.setName("new name");
        serviceAgreement.addParticipant(Lists.newArrayList(participantWithAdmin));

        ServiceAgreementFunctionGroups serviceAgreementFunctionGroups = mock(ServiceAgreementFunctionGroups.class);
        UserContext userContext = new UserContext(adminId, serviceAgreement.getId());
        when(serviceAgreementSystemFunctionGroupService.getServiceAgreementFunctionGroups(serviceAgreement))
            .thenReturn(serviceAgreementFunctionGroups);
        String functionGroupId = "1";
        when(serviceAgreementFunctionGroups.getSystemFunctionGroup())
            .thenReturn(functionGroupId);
        when(userContextService.getOrCreateUserContext(adminId, serviceAgreement.getId()))
            .thenReturn(userContext);
        when(serviceAgreementFunctionGroups.getServiceAgreement())
            .thenReturn(serviceAgreement);
        serviceAgreementService.addAdminPermissions(serviceAgreement);
        verify(userAccessFunctionGroupService)
            .addSystemFunctionGroupToUserAccess(functionGroupId, serviceAgreement, userContext);
    }

    @Test
    public void shouldReturnWhenNoParticipantsArePresent() {
        String adminId = "a1";
        Participant participantWithAdmin = createParticipantWithAdmin(adminId, true, true);
        LegalEntity legalEntity = createLegalEntity("l1", null, null, null, null);
        participantWithAdmin.setLegalEntity(legalEntity);

        ServiceAgreement serviceAgreement = new ServiceAgreement();
        serviceAgreement.setDescription("desc");
        serviceAgreement.setCreatorLegalEntity(legalEntity);
        serviceAgreement.setExternalId("new-ex-id");
        serviceAgreement.setMaster(true);
        serviceAgreement.setName("new name");
        serviceAgreement.addParticipant(
            Lists.newArrayList()
        );
        UserContext userContext = new UserContext(adminId, serviceAgreement.getId());

        String functionGroupId = "1";

        serviceAgreementService.addAdminPermissions(serviceAgreement);
        verify(userAccessFunctionGroupService, never())
            .addSystemFunctionGroupToUserAccess(functionGroupId, serviceAgreement, userContext);
    }

    @Test
    public void shouldAssignPermissionToParticipantAdmins() {
        String adminId = "a1";
        String functionGroupId = "1";

        Participant participantWithAdmin = createParticipantWithAdmin(adminId, true, true);
        LegalEntity legalEntity = createLegalEntity("l1", null, null, null, null);
        participantWithAdmin.setLegalEntity(legalEntity);

        List<Participant> participants = Lists.newArrayList(participantWithAdmin);

        ServiceAgreement serviceAgreement = new ServiceAgreement();
        serviceAgreement.setId("saId");
        serviceAgreement.setDescription("desc");
        serviceAgreement.setCreatorLegalEntity(legalEntity);
        serviceAgreement.setExternalId("new-ex-id");
        serviceAgreement.setMaster(true);
        serviceAgreement.setName("new name");
        serviceAgreement.addParticipant(participants);

        ServiceAgreementFunctionGroups serviceAgreementFunctionGroups = new ServiceAgreementFunctionGroups(
            functionGroupId, serviceAgreement);

        UserContext userContext = new UserContext(adminId, serviceAgreement.getId());

        when(serviceAgreementSystemFunctionGroupService.getServiceAgreementFunctionGroups(eq(serviceAgreement)))
            .thenReturn(serviceAgreementFunctionGroups);
        when(userContextService.getOrCreateUserContext(eq(adminId), eq(serviceAgreement.getId())))
            .thenReturn(userContext);

        serviceAgreementService.addParticipantAdminsPermissions(participants);

        verify(userAccessFunctionGroupService)
            .addSystemFunctionGroupToUserAccess(eq(functionGroupId), eq(serviceAgreement), eq(userContext));
    }

    @Test
    public void shouldNotAssignPermissionToEmptyListParticipants() {
        List<Participant> participants = emptyList();

        serviceAgreementService.addParticipantAdminsPermissions(participants);

        verify(userAccessFunctionGroupService,times(0))
            .addSystemFunctionGroupToUserAccess(any(), any(), any());
    }

    @Test
    public void shouldThrowExceptionOnAddAdminInServiceAgreementProviderWhenUserIsAlreadyAdmin() {
        String externalServiceAgreementId = "SA-001-external";
        String legalEntityId = "LE-01";
        String userId = "U-01";
        String serviceAgreementId = "SA-01";

        UserContext userContext = new UserContext();
        userContext.setId(1L);

        ServiceAgreement serviceAgreement = createServiceAgreement("name", externalServiceAgreementId, "desc",
            new LegalEntity(), "C1", "P1");
        serviceAgreement.setId(serviceAgreementId);
        serviceAgreement.getParticipants().get("P1").addAdmin(userId);

        Participant provider = new Participant();
        provider.setShareUsers(true);
        provider.addAdmin(userId);

        mockGetServiceAgreementByExternalId(externalServiceAgreementId, serviceAgreement);
        mockGetParticipantsBySaIdAndLegalEntityId(externalServiceAgreementId, legalEntityId, Optional.of(provider));
        mockFindUserContextByUserIdAndServiceAgreementId(serviceAgreementId, userContext);

        BadRequestException exception = assertThrows(BadRequestException.class, () -> serviceAgreementService
            .addAdminInServiceAgreementBatch(externalServiceAgreementId, userId, legalEntityId));
        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_048.getErrorMessage(), ERR_ACC_048.getErrorCode()));
    }

    private void mockGetServiceAgreementByExternalId(String serviceAgreementExternalId,
        ServiceAgreement serviceAgreement) {
        when(serviceAgreementJpaRepository.findByExternalId(eq(serviceAgreementExternalId)))
            .thenReturn(Optional.of(serviceAgreement));
    }

    private void mockGetParticipantsBySaIdAndLegalEntityId(String externalServiceAgreementId, String legalEntityId,
        Optional<Participant> participants) {
        when(participantJpaRepository
            .findByServiceAgreementExternalIdAndLegalEntityId(externalServiceAgreementId, legalEntityId))
            .thenReturn(participants);
    }


    private void mockFindUserContextByUserIdAndServiceAgreementId(String serviceAgreementId, UserContext userContext) {
        when(userContextService.getOrCreateUserContext(anyString(), eq(serviceAgreementId))).thenReturn(userContext);
    }

    private void mockGetServiceAgreementFunctionGroups(ServiceAgreement serviceAgreement) {
        when(serviceAgreementSystemFunctionGroupService.getServiceAgreementFunctionGroups(eq(serviceAgreement)))
            .thenReturn(new ServiceAgreementFunctionGroups("sys-fg-id", serviceAgreement));
    }

    private void mockCheckExistsAdminInServiceAgreement(boolean res) {
        when(serviceAgreementAdminJpaRepository.existsByParticipantServiceAgreement(any(ServiceAgreement.class)))
            .thenReturn(res);
    }

}