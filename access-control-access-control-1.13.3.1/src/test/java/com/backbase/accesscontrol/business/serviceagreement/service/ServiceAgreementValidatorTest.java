package com.backbase.accesscontrol.business.serviceagreement.service;

import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_005;
import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_006;
import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_008;
import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_028;
import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_030;
import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_080;
import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_095;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.business.service.UserManagementService;
import com.backbase.accesscontrol.dto.ServiceAgreementDto;
import com.backbase.accesscontrol.dto.ServiceAgreementParticipantDto;
import com.backbase.accesscontrol.matchers.BadRequestErrorMatcher;
import com.backbase.accesscontrol.service.DateTimeService;
import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.dbs.user.api.client.v2.model.GetUser;
import com.backbase.dbs.user.api.client.v2.model.GetUsersList;
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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ServiceAgreementValidatorTest {

    @Mock
    private ServiceAgreementClientCommunicationService serviceAgreementClientCommunicationService;
    @Mock
    private UserManagementService userManagementService;
    @Mock
    private ServiceAgreementBusinessRulesService serviceAgreementBusinessRulesService;
    @Spy
    private DateTimeService dateTimeService = new DateTimeService("UTC");
    @InjectMocks
    private ServiceAgreementValidator testy;

    @Before
    public void setup() {
        when(serviceAgreementBusinessRulesService.isPeriodValid(isNull(), isNull())).thenReturn(true);
    }

    @Test
    public void shouldThrowBadRequestWhenParticipantNotSharingUsersOrAccounts() {
        ServiceAgreementParticipantDto participant = new ServiceAgreementParticipantDto().withId("LE-01")
            .withSharingAccounts(false).withSharingUsers(false);
        ServiceAgreementDto serviceAgreementDto = new ServiceAgreementDto()
            .withParticipants(Collections.singletonList(participant));

        when(serviceAgreementBusinessRulesService
            .isInvalidParticipant(serviceAgreementDto.getParticipants()))
            .thenReturn(true);

        BadRequestException badRequestException = assertThrows(BadRequestException.class,
            () -> testy.validatePayload(serviceAgreementDto));

        assertThat(badRequestException,
            is(new BadRequestErrorMatcher(ERR_AG_005.getErrorMessage(), ERR_AG_005.getErrorCode())));
    }

    @Test
    public void shouldThrowBadRequestWhenThereIsDuplicateParticipant() {
        String id = "LE-01";
        ServiceAgreementParticipantDto participantSharingUsers = new ServiceAgreementParticipantDto()
            .withId(id)
            .withSharingAccounts(false)
            .withSharingUsers(true);
        ServiceAgreementParticipantDto participantSharingAccounts = new ServiceAgreementParticipantDto()
            .withId(id)
            .withSharingAccounts(true)
            .withSharingUsers(false);
        ServiceAgreementDto serviceAgreementDto = new ServiceAgreementDto()
            .withParticipants(asList(participantSharingUsers, participantSharingAccounts));

        when(serviceAgreementBusinessRulesService
            .isInvalidParticipant(serviceAgreementDto.getParticipants()))
            .thenReturn(false);
        when(serviceAgreementBusinessRulesService
            .isDuplicateParticipant(asList(participantSharingAccounts.getId(), participantSharingUsers.getId())))
            .thenReturn(true);

        BadRequestException badRequestException = assertThrows(BadRequestException.class,
            () -> testy.validatePayload(serviceAgreementDto));

        assertThat(badRequestException,
            is(new BadRequestErrorMatcher(ERR_AG_080.getErrorMessage(), ERR_AG_080.getErrorCode())));
    }

    @Test
    public void shouldReturnErrorForInvalidServiceAgreementPeriod() {
        ServiceAgreementDto serviceAgreementDto = new ServiceAgreementDto();

        when(serviceAgreementBusinessRulesService.isPeriodValid(isNull(), isNull())).thenReturn(false);

        BadRequestException badRequestException = assertThrows(BadRequestException.class,
            () -> testy.validatePayload(serviceAgreementDto));

        assertThat(badRequestException,
            is(new BadRequestErrorMatcher(ERR_AG_095.getErrorMessage(), ERR_AG_095.getErrorCode())));
    }

    @Test
    public void shouldThrowBadRequestWhenParticipantSharingUsersNotExists() {
        ServiceAgreementParticipantDto participant = new ServiceAgreementParticipantDto().withId("LE-01")
            .withSharingAccounts(true);
        ServiceAgreementDto serviceAgreementDto = new ServiceAgreementDto()
            .withParticipants(Collections.singletonList(participant));

        when(serviceAgreementBusinessRulesService
            .isInvalidParticipant(serviceAgreementDto.getParticipants()))
            .thenReturn(false);
        when(serviceAgreementBusinessRulesService
            .participantSharingUsersNotExists(serviceAgreementDto.getParticipants()))
            .thenReturn(true);

        BadRequestException badRequestException = assertThrows(BadRequestException.class,
            () -> testy.validatePayload(serviceAgreementDto));

        assertThat(badRequestException,
            is(new BadRequestErrorMatcher(ERR_AG_006.getErrorMessage(), ERR_AG_006.getErrorCode())));
    }

    @Test
    public void shouldThrowBadRequestWhenParticipantSharingAccountsNotExists() {
        ServiceAgreementParticipantDto participant = new ServiceAgreementParticipantDto().withId("LE-01")
            .withSharingUsers(true);
        ServiceAgreementDto serviceAgreementDto = new ServiceAgreementDto()
            .withParticipants(Collections.singletonList(participant));

        when(serviceAgreementBusinessRulesService
            .isInvalidParticipant(serviceAgreementDto.getParticipants()))
            .thenReturn(false);
        when(serviceAgreementBusinessRulesService
            .participantSharingUsersNotExists(serviceAgreementDto.getParticipants()))
            .thenReturn(false);
        when(serviceAgreementBusinessRulesService
            .participantSharingAccountsNotExists(serviceAgreementDto.getParticipants()))
            .thenReturn(true);

        BadRequestException badRequestException = assertThrows(BadRequestException.class,
            () -> testy.validatePayload(serviceAgreementDto));

        assertThat(badRequestException,
            is(new BadRequestErrorMatcher(ERR_AG_008.getErrorMessage(), ERR_AG_008.getErrorCode())));
    }

    @Test
    public void shouldThrowBadRequestWhenEmptyUsersReturned() {
        ServiceAgreementParticipantDto validParticipant1 = new ServiceAgreementParticipantDto().withId("LE-01")
            .withSharingAccounts(true)
            .withSharingUsers(false).withAdmins(newHashSet("admin1"));
        ServiceAgreementParticipantDto validParticipant2 = new ServiceAgreementParticipantDto().withId("LE-02")
            .withSharingAccounts(false)
            .withSharingUsers(true).withAdmins(newHashSet("admin2"));
        ServiceAgreementDto serviceAgreementDto = new ServiceAgreementDto()
            .withParticipants(asList(validParticipant1, validParticipant2));

        when(serviceAgreementClientCommunicationService.getUsers(eq(newHashSet("admin1", "admin2"))))
            .thenReturn(Optional.of(new com.backbase.dbs.user.api.client.v2.model.GetUsersList()));
        when(userManagementService.getUsersByLegalEntityId(eq(emptyList()))).thenReturn(emptyMap());

        Map<String, Set<String>> adminsToAssign = new HashMap<>();
        adminsToAssign.put("LE-01", newHashSet("admin1"));
        adminsToAssign.put("LE-02", newHashSet("admin2"));
        when(serviceAgreementBusinessRulesService
            .adminUsersNotBelongToTheGivenLegalEntities(eq(emptyMap()), eq(adminsToAssign)))
            .thenReturn(true);

        BadRequestException badRequestException = assertThrows(BadRequestException.class,
            () -> testy.validatePayload(serviceAgreementDto));

        assertThat(badRequestException,
            is(new BadRequestErrorMatcher(ERR_AG_028.getErrorMessage(), ERR_AG_028.getErrorCode())));
    }

    @Test
    public void shouldThrowBadRequestWhenUsersNotPresent() {
        ServiceAgreementParticipantDto validParticipant1 = new ServiceAgreementParticipantDto().withId("LE-01")
            .withSharingAccounts(true)
            .withSharingUsers(false).withAdmins(newHashSet("admin1"));
        ServiceAgreementParticipantDto validParticipant2 = new ServiceAgreementParticipantDto().withId("LE-02")
            .withSharingAccounts(false)
            .withSharingUsers(true).withAdmins(newHashSet("admin2"));
        ServiceAgreementDto serviceAgreementDto = new ServiceAgreementDto()
            .withParticipants(asList(validParticipant1, validParticipant2));

        when(serviceAgreementClientCommunicationService.getUsers(eq(newHashSet("admin1", "admin2"))))
            .thenReturn(Optional.empty());

        BadRequestException badRequestException = assertThrows(BadRequestException.class,
            () -> testy.validatePayload(serviceAgreementDto));

        assertThat(badRequestException,
            is(new BadRequestErrorMatcher(ERR_AG_030.getErrorMessage(), ERR_AG_030.getErrorCode())));
    }

    @Test
    public void shouldReturnResultValidWhenNullAdmins() {
        ServiceAgreementParticipantDto validParticipant1 = new ServiceAgreementParticipantDto().withId("LE-01")
            .withSharingAccounts(true)
            .withSharingUsers(false).withAdmins(null);

        HashSet<String> admins = new HashSet<>();
        admins.add(null);
        ServiceAgreementParticipantDto validParticipant2 = new ServiceAgreementParticipantDto().withId("LE-02")
            .withSharingAccounts(false)
            .withSharingUsers(true).withAdmins(admins);

        ServiceAgreementDto serviceAgreementDto = new ServiceAgreementDto()
            .withParticipants(asList(validParticipant1, validParticipant2));

        when(serviceAgreementClientCommunicationService.getUsers(eq(admins)))
            .thenReturn(Optional.of(new GetUsersList()));

        testy.validatePayload(serviceAgreementDto);
    }

    @Test
    public void shouldReturnResultValid() {
        ServiceAgreementParticipantDto validParticipant1 = new ServiceAgreementParticipantDto().withId("LE-01")
            .withSharingAccounts(true)
            .withSharingUsers(false).withAdmins(newHashSet("admin1"));
        ServiceAgreementParticipantDto validParticipant2 = new ServiceAgreementParticipantDto().withId("LE-02")
            .withSharingAccounts(false)
            .withSharingUsers(true).withAdmins(newHashSet("admin2"));
        ServiceAgreementDto serviceAgreementDto = new ServiceAgreementDto()
            .withParticipants(asList(validParticipant1, validParticipant2));

        com.backbase.dbs.user.api.client.v2.model.GetUser user1 = new GetUser();
        user1.setLegalEntityId("LE-01");
        user1.setId("admin1");
        com.backbase.dbs.user.api.client.v2.model.GetUser user2 = new GetUser();
        user2.setLegalEntityId("LE-02");
        user2.setId("admin2");

        GetUsersList users = new GetUsersList();
            users.setUsers(asList(user1, user2));

        when(serviceAgreementClientCommunicationService.getUsers(eq(newHashSet("admin1", "admin2"))))
            .thenReturn(Optional.of(users));

        testy.validatePayload(serviceAgreementDto);
    }

    @Test
    public void shouldThrowNoException() {
        testy.validateListOfAdmins(new HashMap<>(), new HashMap<>());

        verify(serviceAgreementBusinessRulesService)
            .adminUsersNotBelongToTheGivenLegalEntities(eq(emptyMap()), eq(emptyMap()));
    }

    @Test
    public void shouldThrowBadRequestWhenValidationFails() {
        HashMap<String, Set<String>> adminsToAssign = new HashMap<>();
        adminsToAssign.put("LE-1", newHashSet("user1"));

        when(serviceAgreementBusinessRulesService
            .adminUsersNotBelongToTheGivenLegalEntities(eq(emptyMap()), eq(adminsToAssign)))
            .thenReturn(true);

        BadRequestException badRequestException = assertThrows(BadRequestException.class,
            () -> testy.validateListOfAdmins(adminsToAssign, new HashMap<>()));

        assertThat(badRequestException,
            is(new BadRequestErrorMatcher(ERR_AG_028.getErrorMessage(), ERR_AG_028.getErrorCode())));
    }

    @Test
    public void shouldReturnBadRequestWhenNotExpectedParticipantNumber() {
        String participant1 = "LE-1";
        String participant2 = "LE-2";

        List<String> participantIds = asList(participant1, participant2);
        when(serviceAgreementBusinessRulesService.isDuplicateParticipant(participantIds))
            .thenReturn(true);

        BadRequestException badRequestException = assertThrows(BadRequestException.class,
            () -> testy.validateDuplicateParticipants(participantIds));

        assertThat(badRequestException,
            is(new BadRequestErrorMatcher(ERR_AG_080.getErrorMessage(), ERR_AG_080.getErrorCode())));
    }
}