package com.backbase.accesscontrol.service.impl;

import static com.backbase.accesscontrol.domain.GraphConstants.PARTICIPANT_WITH_SERVICE_AGREEMENT_LEGAL_ENTITY_USERS;
import static com.backbase.accesscontrol.domain.GraphConstants.SERVICE_AGREEMENT_EXTENDED;
import static com.backbase.accesscontrol.domain.GraphConstants.SERVICE_AGREEMENT_EXTENDED_WITH_FUNCTION_GROUPS;
import static com.backbase.accesscontrol.domain.GraphConstants.SERVICE_AGREEMENT_WITH_ADDITIONS;
import static com.backbase.accesscontrol.domain.GraphConstants.SERVICE_AGREEMENT_WITH_PARTICIPANTS_AND_ADDITIONS;
import static com.backbase.accesscontrol.domain.GraphConstants.SERVICE_AGREEMENT_WITH_PARTICIPANTS_AND_CREATOR;
import static com.backbase.accesscontrol.domain.GraphConstants.SERVICE_AGREEMENT_WITH_PERMISSION_SETS;
import static com.backbase.accesscontrol.util.ExceptionUtil.getBadRequestException;
import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_094;
import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_105;
import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_106;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_010;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_029;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_038;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_039;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_040;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_042;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_043;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_044;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_045;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_046;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_054;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_056;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_063;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_064;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_065;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_066;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_067;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_068;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_069;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_070;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_075;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_077;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_078;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_086;
import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_006;
import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_036;
import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_039;
import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_056;
import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_057;
import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_062;
import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_063;
import static com.backbase.accesscontrol.util.helpers.ApplicationPropertiesUtils.mockApprovalValidation;
import static com.backbase.accesscontrol.util.helpers.ServiceAgreementUtil.createParticipantWithAdmin;
import static com.backbase.accesscontrol.util.helpers.ServiceAgreementUtil.createServiceAgreement;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.business.persistence.transformer.ServiceAgreementTransformerPersistence;
import com.backbase.accesscontrol.business.serviceagreement.participant.validation.IngestParticipantUpdateRemoveDataValidationProcessor;
import com.backbase.accesscontrol.domain.ApplicableFunctionPrivilege;
import com.backbase.accesscontrol.domain.ApprovalServiceAgreement;
import com.backbase.accesscontrol.domain.ApprovalServiceAgreementParticipant;
import com.backbase.accesscontrol.domain.AssignablePermissionSet;
import com.backbase.accesscontrol.domain.FunctionGroup;
import com.backbase.accesscontrol.domain.GraphConstants;
import com.backbase.accesscontrol.domain.LegalEntity;
import com.backbase.accesscontrol.domain.ParticipantUser;
import com.backbase.accesscontrol.domain.PermissionSetsInServiceAgreements;
import com.backbase.accesscontrol.domain.ServiceAgreement;
import com.backbase.accesscontrol.domain.ServiceAgreementAdmin;
import com.backbase.accesscontrol.domain.ServiceAgreementState;
import com.backbase.accesscontrol.domain.enums.AssignablePermissionType;
import com.backbase.accesscontrol.domain.enums.FunctionGroupType;
import com.backbase.accesscontrol.domain.enums.LegalEntityType;
import com.backbase.accesscontrol.dto.PersistenceExtendedParticipant;
import com.backbase.accesscontrol.dto.SearchAndPaginationParameters;
import com.backbase.accesscontrol.dto.UserParameters;
import com.backbase.accesscontrol.dto.UsersDto;
import com.backbase.accesscontrol.mappers.ApprovalServiceAgreementMapper;
import com.backbase.accesscontrol.mappers.ServiceAgreementByPermissionSetMapper;
import com.backbase.accesscontrol.matchers.BadRequestErrorMatcher;
import com.backbase.accesscontrol.matchers.NotFoundErrorMatcher;
import com.backbase.accesscontrol.repository.ApprovalDataGroupDetailsJpaRepository;
import com.backbase.accesscontrol.repository.ApprovalDataGroupJpaRepository;
import com.backbase.accesscontrol.repository.ApprovalFunctionGroupJpaRepository;
import com.backbase.accesscontrol.repository.ApprovalFunctionGroupRefJpaRepository;
import com.backbase.accesscontrol.repository.ApprovalServiceAgreementJpaRepository;
import com.backbase.accesscontrol.repository.ApprovalServiceAgreementRefJpaRepository;
import com.backbase.accesscontrol.repository.ApprovalUserContextJpaRepository;
import com.backbase.accesscontrol.repository.AssignablePermissionSetJpaRepository;
import com.backbase.accesscontrol.repository.DataGroupJpaRepository;
import com.backbase.accesscontrol.repository.FunctionGroupJpaRepository;
import com.backbase.accesscontrol.repository.IdProjection;
import com.backbase.accesscontrol.repository.LegalEntityJpaRepository;
import com.backbase.accesscontrol.repository.ParticipantJpaRepository;
import com.backbase.accesscontrol.repository.ServiceAgreementJpaRepository;
import com.backbase.accesscontrol.repository.UserAssignedFunctionGroupJpaRepository;
import com.backbase.accesscontrol.service.BusinessFunctionCache;
import com.backbase.accesscontrol.service.DateTimeService;
import com.backbase.accesscontrol.service.PermissionSetService;
import com.backbase.accesscontrol.service.TimeBoundValidatorService;
import com.backbase.accesscontrol.service.ValidateLegalEntityHierarchyService;
import com.backbase.accesscontrol.util.ApplicationProperties;
import com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes;
import com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes;
import com.backbase.accesscontrol.util.helpers.LegalEntityUtil;
import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.buildingblocks.presentation.errors.NotFoundException;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.serviceagreements.ServiceAgreementByPermissionSet;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.serviceagreements.ServiceAgreementItem;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.serviceagreements.ServiceAgreementUsersGetResponseBody;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.serviceagreements.PersistenceServiceAgreementDataGroups;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.PresentationAction;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreement.PresentationParticipantPutBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.CreateStatus;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.Participant;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.PresentationServiceAgreementIdentifier;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementPostRequestBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementPutRequestBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementSave;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.Status;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.NewCustomServiceAgreement;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.NewMasterServiceAgreement;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.ParticipantInfo;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.persistence.Tuple;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@RunWith(MockitoJUnitRunner.class)
public class PersistenceServiceAgreementServiceTest {

    @InjectMocks
    private PersistenceServiceAgreementService persistenceServiceAgreementService;
    @Mock
    private ServiceAgreementJpaRepository serviceAgreementJpaRepository;
    @Spy
    private TimeBoundValidatorService timeBoundValidatorService = new TimeBoundValidatorService(
        "UTC");
    @Mock
    private ParticipantJpaRepository participantJpaRepository;
    @Mock
    private LegalEntityJpaRepository legalEntityJpaRepository;
    @Mock
    private ServiceAgreementAdminService serviceAgreementAdminService;
    @Mock
    private UserAccessFunctionGroupService userAccessFunctionGroupService;
    @Mock
    private ApprovalUserContextJpaRepository approvalUserContextJpaRepository;
    @Mock
    private AssignablePermissionSetJpaRepository assignablePermissionSetJpaRepository;
    @Mock
    private ServiceAgreementSystemFunctionGroupService serviceAgreementSystemFunctionGroupService;
    @Mock
    private PermissionSetService permissionSetService;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ApplicationProperties applicationProperties;
    @Mock
    private ApprovalDataGroupDetailsJpaRepository approvalDataGroupDetailsJpaRepository;
    @Captor
    private ArgumentCaptor<Collection<com.backbase.accesscontrol.domain.Participant>> updatedProviders;
    @Captor
    private ArgumentCaptor<com.backbase.accesscontrol.domain.Participant> participantCaptor;
    @Captor
    private ArgumentCaptor<ServiceAgreement> serviceAgreementCaptor;

    @Mock
    private ServiceAgreementByPermissionSetMapper serviceAgreementByPermissionSetMapper;
    @Mock
    private ApprovalFunctionGroupJpaRepository approvalFunctionGroupJpaRepository;
    @Mock
    private ValidateLegalEntityHierarchyService validateLegalEntityHierarchyService;
    @Mock
    private BusinessFunctionCache businessFunctionCache;
    @Mock
    private DataGroupJpaRepository dataGroupJpaRepository;
    @Mock
    private FunctionGroupJpaRepository functionGroupJpaRepository;
    @Mock
    private ApprovalServiceAgreementJpaRepository approvalServiceAgreementJpaRepository;
    @Mock
    private ApprovalServiceAgreementRefJpaRepository approvalServiceAgreementRefJpaRepository;
    @Mock
    private ApprovalServiceAgreementMapper approvalServiceAgreementMapper;
    @Mock
    private UserAssignedFunctionGroupJpaRepository userAssignedFunctionGroupJpaRepository;
    @Mock
    private ApprovalDataGroupJpaRepository approvalDataGroupJpaRepository;
    @Mock
    private ApprovalFunctionGroupRefJpaRepository approvalFunctionGroupRefJpaRepository;
    @Mock
    private IngestParticipantUpdateRemoveDataValidationProcessor ingestParticipantUpdateRemoveDataValidationProcessor;
    @Spy
    private ServiceAgreementTransformerPersistence serviceAgreementTransformerPersistence;

    private List<ServiceAgreement> serviceAgreements;
    private Page<ServiceAgreement> serviceAgreementsPage;

    @Spy
    private DateTimeService dateTimeService = new DateTimeService("UTC");

    @Before
    public void setUp() {
        serviceAgreements = getServiceAgreements();
        serviceAgreementsPage = new PageImpl<>(serviceAgreements);
    }

    @Test
    public void shouldAddParticipantUsersInServiceAgreement() {
        ServiceAgreement serviceAgreement = getServiceAgreements().get(0);
        String serviceAgreementId = serviceAgreement.getId();

        LegalEntity legalEntity = createLegalEntity("LE-ID1", null);

        com.backbase.accesscontrol.domain.Participant provider =
            new com.backbase.accesscontrol.domain.Participant();
        provider.setShareUsers(true);
        provider.setId("P1");
        provider.setLegalEntity(legalEntity);
        provider.addParticipantUsers(asList("2", "4"));

        when(serviceAgreementJpaRepository.findById(eq(serviceAgreementId), eq(null)))
            .thenReturn(Optional.of(serviceAgreement));

        when(participantJpaRepository
            .findDistinctByServiceAgreementIdAndLegalEntityIdInAndShareUsersTrue(serviceAgreementId,
                new HashSet<>(singletonList("LE-ID1")))).thenReturn(singletonList(provider));

        UsersDto usersAddPostRequestBody = new UsersDto()
            .withLegalEntityId(provider.getLegalEntity().getId())
            .withUsers(asList("1", "3"));

        List<UsersDto> requestBody = singletonList(usersAddPostRequestBody);

        persistenceServiceAgreementService.addUsersInServiceAgreement(serviceAgreementId, requestBody);

        verify(participantJpaRepository).saveAll(updatedProviders.capture());

        Collection<com.backbase.accesscontrol.domain.Participant> valueProviderList = updatedProviders.getValue();
        assertThat(getProviderFromList(valueProviderList, provider).getParticipantUsers(),
            hasItems(hasProperty("userId", equalTo("1")),
                hasProperty("userId", equalTo("2")),
                hasProperty("userId", equalTo("3")),
                hasProperty("userId", equalTo("4"))));

    }

    @Test
    public void shouldThrowBadRequestOnAddUsersInServiceAgreementWhenUsersAlreadyExistInSA() {
        ServiceAgreement serviceAgreement = getServiceAgreements().get(0);
        String serviceAgreementId = serviceAgreement.getId();

        LegalEntity legalEntity = createLegalEntity("le1", null);

        com.backbase.accesscontrol.domain.Participant provider =
            new com.backbase.accesscontrol.domain.Participant();
        provider.setShareUsers(true);
        provider.setId("P1");
        provider.setLegalEntity(legalEntity);
        provider.addParticipantUsers(asList("2", "4"));

        when(serviceAgreementJpaRepository.findById(eq(serviceAgreementId), eq(null)))
            .thenReturn(Optional.of(serviceAgreement));

        when(participantJpaRepository
            .findDistinctByServiceAgreementIdAndLegalEntityIdInAndShareUsersTrue(serviceAgreementId,
                new HashSet<>(singletonList("le1")))).thenReturn(singletonList(provider));

        UsersDto usersAddPostRequestBody = new UsersDto()
            .withLegalEntityId(provider.getLegalEntity().getId())
            .withUsers(asList("1", "4"));

        List<UsersDto> requestBody = singletonList(usersAddPostRequestBody);

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> persistenceServiceAgreementService.addUsersInServiceAgreement(serviceAgreementId, requestBody));

        verify(participantJpaRepository, times(0)).saveAll(updatedProviders.capture());
        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_039.getErrorMessage(), ERR_ACC_039.getErrorCode()));
    }

    @Test
    public void shouldThrowBadRequestOnAddUsersInServiceAgreementWhenInvalidLegalEntityInSA() {
        ServiceAgreement serviceAgreement = getServiceAgreements().get(0);
        String serviceAgreementId = serviceAgreement.getId();

        LegalEntity legalEntity = createLegalEntity("LE-ID1", null);

        com.backbase.accesscontrol.domain.Participant provider =
            new com.backbase.accesscontrol.domain.Participant();
        provider.setShareUsers(true);
        provider.setId("P1");
        provider.setLegalEntity(legalEntity);
        provider.addParticipantUsers(asList("2", "4"));

        when(serviceAgreementJpaRepository.findById(eq(serviceAgreementId), eq(null)))
            .thenReturn(Optional.of(serviceAgreement));

        UsersDto usersAddPostRequestBody = new UsersDto()
            .withLegalEntityId(provider.getLegalEntity().getId())
            .withUsers(asList("1", "4"));

        List<UsersDto> requestBody = singletonList(usersAddPostRequestBody);

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> persistenceServiceAgreementService.addUsersInServiceAgreement(serviceAgreementId, requestBody));

        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_042.getErrorMessage(), ERR_ACC_042.getErrorCode()));
    }

    @Test
    public void shouldReturnNotFoundOnAddUsersInServiceAgreementIfServiceAgreementDoesNotExist() {
        ServiceAgreement serviceAgreement = getServiceAgreements().get(0);
        String serviceAgreementId = serviceAgreement.getId();

        LegalEntity legalEntity = createLegalEntity("LE-ID1", null);

        com.backbase.accesscontrol.domain.Participant provider =
            new com.backbase.accesscontrol.domain.Participant();
        provider.setShareUsers(true);
        provider.setId("P1");
        provider.setLegalEntity(legalEntity);
        provider.addParticipantUsers(asList("2", "4"));

        when(serviceAgreementJpaRepository.findById(eq(serviceAgreementId), eq(null)))
            .thenReturn(Optional.empty());

        UsersDto usersAddPostRequestBody = new UsersDto()
            .withLegalEntityId(provider.getLegalEntity().getId())
            .withUsers(asList("1", "4"));

        List<UsersDto> requestBody = singletonList(usersAddPostRequestBody);

        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> persistenceServiceAgreementService.addUsersInServiceAgreement(serviceAgreementId, requestBody));

        assertThat(exception, new NotFoundErrorMatcher(ERR_ACC_029.getErrorMessage(), ERR_ACC_029.getErrorCode()));
    }

    @Test
    public void shouldReturnBadRequestOnAddUsersInServiceAgreementIfServiceAgreementIsMaster() {
        LegalEntity legalEntityOne = createLegalEntity("le1", null);

        com.backbase.accesscontrol.domain.Participant provider =
            new com.backbase.accesscontrol.domain.Participant();
        provider.setShareUsers(true);
        provider.setId("P1");
        provider.setLegalEntity(legalEntityOne);
        provider.addParticipantUsers(asList("1", "2", "3", "4"));

        UsersDto usersAddPostRequestBody = new UsersDto()
            .withLegalEntityId(provider.getLegalEntity().getId())
            .withUsers(asList("1", "4"));

        List<UsersDto> requestBody = singletonList(usersAddPostRequestBody);

        ServiceAgreement serviceAgreement = getServiceAgreements().get(2);
        String serviceAgreementId = serviceAgreement.getId();

        when(serviceAgreementJpaRepository.findById(eq(serviceAgreementId), eq(null)))
            .thenReturn(Optional.of(serviceAgreement));

        BadRequestException exception = assertThrows(BadRequestException.class, () ->
            persistenceServiceAgreementService.addUsersInServiceAgreement(serviceAgreementId, requestBody));

        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_067.getErrorMessage(), ERR_ACC_067.getErrorCode()));
    }


    @Test
    public void shouldRemoveParticipantUsersFromServiceAgreement() {
        LegalEntity legalEntityOne = createLegalEntity("LE-ID1", null);

        LegalEntity legalEntityTwo = createLegalEntity("LE-ID2", null);

        com.backbase.accesscontrol.domain.Participant provider1 =
            new com.backbase.accesscontrol.domain.Participant();
        provider1.setShareUsers(true);
        provider1.setId("P1");
        provider1.setLegalEntity(legalEntityOne);
        provider1.addParticipantUsers(asList("1", "2", "3", "4"));

        com.backbase.accesscontrol.domain.Participant provider2 =
            new com.backbase.accesscontrol.domain.Participant();
        provider2.setShareUsers(true);
        provider2.setId("P2");
        provider2.setLegalEntity(legalEntityTwo);
        provider2.addParticipantUsers(asList("5", "6", "7", "8"));

        UsersDto userToBeRemovedBodyOne = new UsersDto()
            .withLegalEntityId(provider1.getLegalEntity().getId())
            .withUsers(asList("1", "3"));
        UsersDto userToBeRemovedBodyTwo = new UsersDto()
            .withLegalEntityId(provider2.getLegalEntity().getId())
            .withUsers(asList("5", "7"));

        List<UsersDto> requestBody = new ArrayList<>(
            asList(userToBeRemovedBodyOne, userToBeRemovedBodyTwo));

        ServiceAgreement serviceAgreement = getServiceAgreements().get(0);
        String serviceAgreementId = serviceAgreement.getId();

        when(serviceAgreementJpaRepository.findById(eq(serviceAgreementId), eq(null)))
            .thenReturn(Optional.of(serviceAgreement));

        when(participantJpaRepository
            .findDistinctByServiceAgreementIdAndLegalEntityIdInAndShareUsersTrue(serviceAgreementId,
                new HashSet<>(asList("LE-ID1", "LE-ID2"))))
            .thenReturn(asList(provider1, provider2));

        persistenceServiceAgreementService.removeUsersFromServiceAgreement(serviceAgreementId, requestBody);

        verify(participantJpaRepository).saveAll(updatedProviders.capture());

        Collection<com.backbase.accesscontrol.domain.Participant> valueProviderList = updatedProviders.getValue();
        assertThat(getProviderFromList(valueProviderList, provider1).getParticipantUsers(),
            hasItems(hasProperty("userId", equalTo("4")),
                hasProperty("userId", equalTo("2"))));
        assertThat(getProviderFromList(valueProviderList, provider2).getParticipantUsers(),
            hasItems(hasProperty("userId", equalTo("6")),
                hasProperty("userId", equalTo("8"))));

    }

    @Test
    public void shouldThrowBadRequestExceptionOnRemoveParticipantUsersFromServiceAgreementWhenThereArePendingApprovals() {
        LegalEntity legalEntityOne = createLegalEntity("LE-ID1", null);

        LegalEntity legalEntityTwo = createLegalEntity("LE-ID2", null);

        com.backbase.accesscontrol.domain.Participant provider1 =
            new com.backbase.accesscontrol.domain.Participant();
        provider1.setShareUsers(true);
        provider1.setId("P1");
        provider1.setLegalEntity(legalEntityOne);
        provider1.addParticipantUsers(asList("1", "2", "3", "4"));

        com.backbase.accesscontrol.domain.Participant provider2 =
            new com.backbase.accesscontrol.domain.Participant();
        provider2.setShareUsers(true);
        provider2.setId("P2");
        provider2.setLegalEntity(legalEntityTwo);
        provider2.addParticipantUsers(asList("5", "6", "7", "8"));

        UsersDto userToBeRemovedBodyOne = new UsersDto()
            .withLegalEntityId(provider1.getLegalEntity().getId())
            .withUsers(asList("1", "3"));
        UsersDto userToBeRemovedBodyTwo = new UsersDto()
            .withLegalEntityId(provider2.getLegalEntity().getId())
            .withUsers(asList("5", "7"));

        List<UsersDto> requestBody = new ArrayList<>(
            asList(userToBeRemovedBodyOne, userToBeRemovedBodyTwo));

        Set<String> users = requestBody.stream()
            .flatMap(userBody -> userBody.getUsers().stream())
            .collect(Collectors.toSet());

        ServiceAgreement serviceAgreement = getServiceAgreements().get(0);
        String serviceAgreementId = serviceAgreement.getId();

        when(serviceAgreementJpaRepository.findById(eq(serviceAgreementId), eq(null)))
            .thenReturn(Optional.of(serviceAgreement));

        doThrow(getBadRequestException(CommandErrorCodes.ERR_ACC_075.getErrorMessage(),
            CommandErrorCodes.ERR_ACC_075.getErrorCode()))
            .when(approvalUserContextJpaRepository)
            .countByServiceAgreementIdAndUserIdIn(serviceAgreementId, users);

        BadRequestException exception = assertThrows(BadRequestException.class, () ->
            persistenceServiceAgreementService.removeUsersFromServiceAgreement(serviceAgreementId, requestBody));

        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_075.getErrorMessage(), ERR_ACC_075.getErrorCode()));
    }

    @Test
    public void shouldReturnBadRequestOnRemoveUsersFromServiceAgreementIfUsersDoNotExistInSA() {
        LegalEntity legalEntityOne = createLegalEntity("LE-ID1", null);

        LegalEntity legalEntityTwo = createLegalEntity("LE-ID2", null);

        com.backbase.accesscontrol.domain.Participant provider1 =
            new com.backbase.accesscontrol.domain.Participant();
        provider1.setShareUsers(true);
        provider1.setId("P1");
        provider1.setLegalEntity(legalEntityOne);
        provider1.addParticipantUsers(asList("1", "2", "3", "4"));

        com.backbase.accesscontrol.domain.Participant provider2 =
            new com.backbase.accesscontrol.domain.Participant();
        provider2.setShareUsers(true);
        provider2.setId("P2");
        provider2.setLegalEntity(legalEntityTwo);
        provider2.addParticipantUsers(asList("5", "6", "7", "8"));

        UsersDto userToBeRemovedBodyOne = new UsersDto()
            .withLegalEntityId(provider1.getLegalEntity().getId())
            .withUsers(asList("1", "9"));
        UsersDto userToBeRemovedBodyTwo = new UsersDto()
            .withLegalEntityId(provider2.getLegalEntity().getId())
            .withUsers(asList("5", "7"));

        List<UsersDto> requestBody = new ArrayList<>(
            asList(userToBeRemovedBodyOne, userToBeRemovedBodyTwo));

        ServiceAgreement serviceAgreement = getServiceAgreements().get(0);
        String serviceAgreementId = serviceAgreement.getId();

        when(serviceAgreementJpaRepository.findById(eq(serviceAgreementId), eq(null)))
            .thenReturn(Optional.of(serviceAgreement));

        when(participantJpaRepository
            .findDistinctByServiceAgreementIdAndLegalEntityIdInAndShareUsersTrue(serviceAgreementId,
                new HashSet<>(asList("LE-ID1", "LE-ID2"))))
            .thenReturn(asList(provider1, provider2));

        BadRequestException exception = assertThrows(BadRequestException.class, () ->
            persistenceServiceAgreementService.removeUsersFromServiceAgreement(serviceAgreementId, requestBody));

        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_040.getErrorMessage(), ERR_ACC_040.getErrorCode()));
    }


    @Test
    public void shouldReturnBadRequestOnRemoveUsersFromServiceAgreementIfUserHasInvalidLegalEntity() {
        LegalEntity legalEntityOne = createLegalEntity("LE-ID1", null);

        LegalEntity legalEntityTwo = createLegalEntity("LE-ID2", null);

        com.backbase.accesscontrol.domain.Participant provider1 =
            new com.backbase.accesscontrol.domain.Participant();
        provider1.setShareUsers(true);
        provider1.setId("P1");
        provider1.setLegalEntity(legalEntityOne);
        provider1.addParticipantUsers(asList("1", "2", "3", "4"));

        com.backbase.accesscontrol.domain.Participant provider2 =
            new com.backbase.accesscontrol.domain.Participant();
        provider2.setShareUsers(true);
        provider2.setId("P2");
        provider2.setLegalEntity(legalEntityTwo);
        provider2.addParticipantUsers(asList("5", "6", "7", "8"));

        UsersDto userToBeRemovedBodyOne = new UsersDto()
            .withLegalEntityId(provider1.getLegalEntity().getId())
            .withUsers(asList("1", "3"));
        UsersDto userToBeRemovedBodyTwo = new UsersDto()
            .withLegalEntityId(provider2.getLegalEntity().getId())
            .withUsers(asList("5", "7"));

        List<UsersDto> requestBody = new ArrayList<>(
            asList(userToBeRemovedBodyOne, userToBeRemovedBodyTwo));

        ServiceAgreement serviceAgreement = getServiceAgreements().get(0);
        String serviceAgreementId = serviceAgreement.getId();

        when(serviceAgreementJpaRepository.findById(eq(serviceAgreementId), eq(null)))
            .thenReturn(Optional.of(serviceAgreement));

        BadRequestException exception = assertThrows(BadRequestException.class, () ->
            persistenceServiceAgreementService.removeUsersFromServiceAgreement(serviceAgreementId, requestBody));

        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_042.getErrorMessage(), ERR_ACC_042.getErrorCode()));
    }

    @Test
    public void shouldReturnNotFoundOnRemoveUsersFromServiceAgreementIfServiceAgreementDoesNotExist() {
        LegalEntity legalEntityOne = createLegalEntity("LE-ID1", null);

        com.backbase.accesscontrol.domain.Participant provider1 =
            new com.backbase.accesscontrol.domain.Participant();
        provider1.setShareUsers(true);
        provider1.setId("P1");
        provider1.setLegalEntity(legalEntityOne);
        provider1.addParticipantUsers(asList("1", "2", "3", "4"));

        UsersDto userToBeRemovedBodyOne = new UsersDto()
            .withLegalEntityId(provider1.getLegalEntity().getId())
            .withUsers(asList("1", "3"));

        List<UsersDto> requestBody = new ArrayList<>(
            singletonList(userToBeRemovedBodyOne));

        String serviceAgreementId = "SA ID";

        when(serviceAgreementJpaRepository.findById(eq(serviceAgreementId), eq(null)))
            .thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () ->
            persistenceServiceAgreementService.removeUsersFromServiceAgreement(serviceAgreementId, requestBody));

        assertThat(exception, new NotFoundErrorMatcher(ERR_ACC_029.getErrorMessage(), ERR_ACC_029.getErrorCode()));
    }

    @Test
    public void shouldReturnBadRequestOnRemoveUsersFromServiceAgreementIfServiceAgreementIsMaster() {
        LegalEntity legalEntityOne = createLegalEntity("le1", null);

        com.backbase.accesscontrol.domain.Participant provider1 =
            new com.backbase.accesscontrol.domain.Participant();
        provider1.setShareUsers(true);
        provider1.setId("P1");
        provider1.setLegalEntity(legalEntityOne);
        provider1.addParticipantUsers(asList("1", "2", "3", "4"));

        UsersDto userToBeRemovedBodyOne = new UsersDto()
            .withLegalEntityId(provider1.getLegalEntity().getId())
            .withUsers(asList("1", "3"));

        List<UsersDto> requestBody = new ArrayList<>(
            singletonList(userToBeRemovedBodyOne));

        ServiceAgreement serviceAgreement = getServiceAgreements().get(2);
        String serviceAgreementId = serviceAgreement.getId();

        when(serviceAgreementJpaRepository.findById(eq(serviceAgreementId), eq(null)))
            .thenReturn(Optional.of(serviceAgreement));

        BadRequestException exception = assertThrows(BadRequestException.class, () ->
            persistenceServiceAgreementService.removeUsersFromServiceAgreement(serviceAgreementId, requestBody));

        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_067.getErrorMessage(), ERR_ACC_067.getErrorCode()));
    }

    @Test
    public void testGetById() {
        String id = UUID.randomUUID().toString();
        ServiceAgreement serviceAgreement = new ServiceAgreement();
        serviceAgreement.setState(ServiceAgreementState.DISABLED);
        when(serviceAgreementJpaRepository.findById(id, SERVICE_AGREEMENT_EXTENDED))
            .thenReturn(Optional.of(serviceAgreement));

        ServiceAgreement returnedServiceAgreement = persistenceServiceAgreementService
            .getById(id, SERVICE_AGREEMENT_EXTENDED);

        assertEquals(serviceAgreement, returnedServiceAgreement);
        assertEquals(serviceAgreement.getState(), returnedServiceAgreement.getState());
    }

    @Test
    public void testGetServiceAgreementParticipants() {
        String serviceAgreementId = UUID.randomUUID().toString();

        LegalEntity consumerAndProviderLegalEntity = createLegalEntity("CONSUMER-PROVIDER-LE", null);
        consumerAndProviderLegalEntity.setExternalId("EX-1");
        consumerAndProviderLegalEntity.setName("CONSUMER-PROVIDER-LE");

        com.backbase.accesscontrol.domain.Participant consumer =
            new com.backbase.accesscontrol.domain.Participant();
        consumer.setLegalEntity(consumerAndProviderLegalEntity);
        consumer.setShareAccounts(true);
        consumer.setShareAccounts(true);

        LegalEntity providerLegalEntity = createLegalEntity("PROVIDER-LE", null);
        providerLegalEntity.setExternalId("EX-2");
        providerLegalEntity.setName("PROVIDER-LE");

        ServiceAgreement serviceAgreement = createServiceAgreement("someNameSA", serviceAgreementId, "someDescSA",
            new LegalEntity(), null, null);
        serviceAgreement.setMaster(false);
        Optional<ServiceAgreement> serviceAgreementOptional = Optional.of(serviceAgreement);
        when(serviceAgreementJpaRepository.findById(serviceAgreementId, SERVICE_AGREEMENT_EXTENDED))
            .thenReturn(serviceAgreementOptional);

        com.backbase.accesscontrol.domain.Participant providerParticipant =
            new com.backbase.accesscontrol.domain.Participant();
        providerParticipant.setId("ID");
        providerParticipant.setShareUsers(true);
        providerParticipant.setLegalEntity(providerLegalEntity);

        com.backbase.accesscontrol.domain.Participant proivider2Participant =
            new com.backbase.accesscontrol.domain.Participant();
        proivider2Participant.setId("ID2");
        proivider2Participant.setShareUsers(true);
        proivider2Participant.setShareAccounts(true);
        proivider2Participant.setLegalEntity(consumerAndProviderLegalEntity);

        List<com.backbase.accesscontrol.domain.Participant> ts = asList(
            providerParticipant,
            proivider2Participant
        );
        when(participantJpaRepository
            .findByServiceAgreementId(serviceAgreementId, GraphConstants.PARTICIPANT_WITH_LEGAL_ENTITY)).thenReturn(ts);

        List<com.backbase.pandp.accesscontrol.query.rest.spec.v2
            .accesscontrol.accessgroups.serviceagreements.Participant> serviceAgreementParticipants =
            persistenceServiceAgreementService.getServiceAgreementParticipants(serviceAgreementId);

        assertTrue(containsParticipantWithRoles(serviceAgreementParticipants, providerLegalEntity, true, false));
        assertTrue(
            containsParticipantWithRoles(serviceAgreementParticipants, consumerAndProviderLegalEntity, true, true));
    }

    @Test
    public void shouldThrowNotFoundExceptionWhenGettingServiceAgreementParticipants() {
        String serviceAgreementId = UUID.randomUUID().toString();

        when(serviceAgreementJpaRepository.findById(serviceAgreementId, SERVICE_AGREEMENT_EXTENDED))
            .thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () ->
            persistenceServiceAgreementService.getServiceAgreementParticipants(serviceAgreementId));

        assertThat(exception, new NotFoundErrorMatcher(ERR_ACQ_006.getErrorMessage(), ERR_ACQ_006.getErrorCode()));
    }

    @Test
    public void testGetServiceAgreementParticipantsByExternalServiceAgreementIds() {
        String serviceAgreementId = UUID.randomUUID().toString();

        LegalEntity consumerAndProviderLegalEntity = createLegalEntity("CONSUMER-PROVIDER-LE", null);
        consumerAndProviderLegalEntity.setExternalId("EX-1");
        consumerAndProviderLegalEntity.setName("CONSUMER-PROVIDER-LE");

        com.backbase.accesscontrol.domain.Participant consumer =
            new com.backbase.accesscontrol.domain.Participant();
        consumer.setLegalEntity(consumerAndProviderLegalEntity);
        consumer.setShareAccounts(true);
        consumer.setShareAccounts(true);

        LegalEntity providerLegalEntity = createLegalEntity("PROVIDER-LE", null);
        providerLegalEntity.setExternalId("EX-2");
        providerLegalEntity.setName("PROVIDER-LE");

        ServiceAgreement serviceAgreement = createServiceAgreement("someNameSA", serviceAgreementId, "someDescSA",
            new LegalEntity(), null, null);
        serviceAgreement.setMaster(false);

        com.backbase.accesscontrol.domain.Participant providerParticipant =
            new com.backbase.accesscontrol.domain.Participant();
        providerParticipant.setId("ID");
        providerParticipant.setShareUsers(true);
        providerParticipant.setLegalEntity(providerLegalEntity);
        providerParticipant.setServiceAgreement(serviceAgreement);

        com.backbase.accesscontrol.domain.Participant proivider2Participant =
            new com.backbase.accesscontrol.domain.Participant();
        proivider2Participant.setId("ID2");
        proivider2Participant.setShareUsers(true);
        proivider2Participant.setShareAccounts(true);
        proivider2Participant.setLegalEntity(consumerAndProviderLegalEntity);
        proivider2Participant.setServiceAgreement(serviceAgreement);

        List<com.backbase.accesscontrol.domain.Participant> ts = asList(
            providerParticipant,
            proivider2Participant
        );
        Set<String> ids = new HashSet<>();
        ids.add(serviceAgreementId);
        when(participantJpaRepository.findAllParticipantsWithExternalServiceAgreementIdsIn(ids,
            "graph.Participant.withLegalEntityAndServiceAgreementCreator"))
            .thenReturn(ts);

        List<PersistenceExtendedParticipant> serviceAgreementParticipants = persistenceServiceAgreementService
            .listParticipantsByExternalServiceAgreementIds(ids);

        assertEquals(2, serviceAgreementParticipants.size());
    }

    @Test
    public void testGetMasterServiceAgreementParticipants() {
        String serviceAgreementId = UUID.randomUUID().toString();

        LegalEntity consumerAndProviderLegalEntity = createLegalEntity("CONSUMER-PROVIDER-LE", null);
        consumerAndProviderLegalEntity.setExternalId("EX-1");
        consumerAndProviderLegalEntity.setName("CONSUMER-PROVIDER-LE");

        com.backbase.accesscontrol.domain.Participant consumer =
            new com.backbase.accesscontrol.domain.Participant();
        consumer.setLegalEntity(consumerAndProviderLegalEntity);
        consumer.setShareAccounts(true);
        consumer.setShareAccounts(true);

        LegalEntity providerLegalEntity = createLegalEntity("PROVIDER-LE", null);
        providerLegalEntity.setExternalId("EX-2");
        providerLegalEntity.setName("PROVIDER-LE");

        ServiceAgreement serviceAgreement = createServiceAgreement("someNameSA", serviceAgreementId, "someDescSA",
            providerLegalEntity, null, null);
        serviceAgreement.setMaster(true);
        Optional<ServiceAgreement> serviceAgreementOptional = Optional.of(serviceAgreement);
        when(serviceAgreementJpaRepository.findById(serviceAgreementId, SERVICE_AGREEMENT_EXTENDED))
            .thenReturn(serviceAgreementOptional);

        com.backbase.accesscontrol.domain.Participant providerParticipant =
            new com.backbase.accesscontrol.domain.Participant();
        providerParticipant.setId("ID");
        providerParticipant.setShareUsers(true);
        providerParticipant.setShareAccounts(true);
        providerParticipant.setLegalEntity(providerLegalEntity);

        List<com.backbase.accesscontrol.domain.Participant> ts = singletonList(
            providerParticipant
        );
        when(participantJpaRepository
            .findByServiceAgreementId(serviceAgreementId, GraphConstants.PARTICIPANT_WITH_LEGAL_ENTITY)).thenReturn(ts);

        List<com.backbase.pandp.accesscontrol.query.rest.spec.v2
            .accesscontrol.accessgroups.serviceagreements.Participant> participants = persistenceServiceAgreementService
            .getServiceAgreementParticipants(serviceAgreementId);

        assertTrue(containsParticipantWithRoles(participants, providerLegalEntity, true, true));
    }

    @Test
    public void testGetByIdThrowBadRequest() {
        String id = UUID.randomUUID().toString();
        when(serviceAgreementJpaRepository.findById(id, SERVICE_AGREEMENT_EXTENDED)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> persistenceServiceAgreementService.getById(id, SERVICE_AGREEMENT_EXTENDED));

        assertThat(exception, new NotFoundErrorMatcher(ERR_ACQ_006.getErrorMessage(), ERR_ACQ_006.getErrorCode()));
    }

    @Test
    public void addServiceAgreementTest() {
        String description = "des";
        String name = "name";
        Boolean isMaster = false;
        Map<String, String> additions = new HashMap<>();
        String key = "externalId";
        String value = "123456789";
        additions.put(key, value);

        ServiceAgreement serviceAgreement = createServiceAgreement(name, "id.external", description,
            new LegalEntity().withId("id"),
            null, null);
        serviceAgreement.setAdditions(additions);
        serviceAgreement.setState(ServiceAgreementState.fromString(Status.DISABLED.toString()));
        when(serviceAgreementJpaRepository.save(Mockito.any(ServiceAgreement.class))).thenReturn(serviceAgreement);

        mockDefaultPermissionSets();

        ServiceAgreementPostRequestBody addServiceAgreementRequestBody = createAddServiceAgreementRequestBody(name,
            description);
        addServiceAgreementRequestBody.setParticipants(asList(
            new Participant().withId("leId-01").withSharingAccounts(true).withSharingUsers(true)
                .withAdmins(newHashSet("admin1", "admin2")),
            new Participant().withId("leId-02").withSharingAccounts(true).withSharingUsers(true).withAdmins(null)));
        ServiceAgreement responseServiceAgreement = persistenceServiceAgreementService
            .save(addServiceAgreementRequestBody, "id");

        assertEquals(description, responseServiceAgreement.getDescription());
        assertEquals(name, responseServiceAgreement.getName());
        assertEquals(Status.DISABLED.toString(), responseServiceAgreement.getState().toString());
        assertFalse(responseServiceAgreement.isMaster());
        assertThat(responseServiceAgreement.getAdditions().size(), is(1));
        assertTrue(responseServiceAgreement.getAdditions().containsKey(key));
        assertTrue(responseServiceAgreement.getAdditions().containsValue(value));
        verifyDefaultPermissionSets();
    }

    @Test
    public void saveServiceAgreementApprovalTest() {
        String approvalId = "approvalId";
        String description = "des";
        String name = "name";
        Map<String, String> additions = new HashMap<>();
        String key = "externalId";
        String value = "123456789";
        additions.put(key, value);

        when(approvalServiceAgreementJpaRepository.save(Mockito.any(ApprovalServiceAgreement.class))).thenReturn(null);
        mockDefaultPermissionSets();

        ServiceAgreementPostRequestBody addServiceAgreementRequestBody = createAddServiceAgreementRequestBody(name,
            description);
        addServiceAgreementRequestBody.withParticipants(asList(
            new Participant().withId("LE-01").withSharingAccounts(false).withSharingUsers(true)
                .withAdmins(newHashSet("admin1", "admin2")),
            new Participant().withId("LE-02").withSharingAccounts(true).withSharingUsers(true)
                .withAdmins(newHashSet("admin3", "admin4"))
        ));
        addServiceAgreementRequestBody.setAdditions(additions);

        ApprovalServiceAgreement approvalServiceAgreement = new ApprovalServiceAgreement();
        approvalServiceAgreement.setName(name);
        approvalServiceAgreement.setDescription(description);
        approvalServiceAgreement.setState(ServiceAgreementState.DISABLED);
        approvalServiceAgreement.setAdditions(additions);
        approvalServiceAgreement.setCreatorLegalEntityId("id");
        approvalServiceAgreement.setApprovalId(approvalId);

        ApprovalServiceAgreementParticipant approvalServiceAgreementParticipant = new ApprovalServiceAgreementParticipant();
        approvalServiceAgreementParticipant.setLegalEntityId("LE-01");
        approvalServiceAgreementParticipant.setShareAccounts(false);
        approvalServiceAgreementParticipant.setShareUsers(true);
        approvalServiceAgreementParticipant.setAdmins(newHashSet("admin1", "admin2"));
        ApprovalServiceAgreementParticipant approvalServiceAgreementParticipant1 = new ApprovalServiceAgreementParticipant();
        approvalServiceAgreementParticipant1.setLegalEntityId("LE-02");
        approvalServiceAgreementParticipant1.setShareAccounts(true);
        approvalServiceAgreementParticipant1.setShareUsers(true);
        approvalServiceAgreementParticipant1.setAdmins(newHashSet("admin3", "admin4"));

        approvalServiceAgreement
            .setParticipants(newHashSet(approvalServiceAgreementParticipant, approvalServiceAgreementParticipant1));

        when(approvalServiceAgreementMapper
            .serviceAgreementPostRequestBodyToApprovalServiceAgreement(eq(addServiceAgreementRequestBody), eq("id"),
                eq(approvalId))).thenReturn(approvalServiceAgreement);

        String approvalIdResponse = persistenceServiceAgreementService
            .saveServiceAgreementApproval(addServiceAgreementRequestBody, "id", approvalId);

        ArgumentCaptor<ApprovalServiceAgreement> captor = ArgumentCaptor.forClass(ApprovalServiceAgreement.class);
        verify(approvalServiceAgreementJpaRepository).save(captor.capture());
        ApprovalServiceAgreement savedApprovalServiceAgreement = captor.getValue();

        assertEquals(approvalId, approvalIdResponse);
        assertEquals(description, savedApprovalServiceAgreement.getDescription());
        assertEquals(name, savedApprovalServiceAgreement.getName());
        assertEquals("id", savedApprovalServiceAgreement.getCreatorLegalEntityId());
        assertEquals(Status.DISABLED.toString(), savedApprovalServiceAgreement.getState().toString());
        assertFalse(savedApprovalServiceAgreement.isMaster());

        assertThat(savedApprovalServiceAgreement.getAdditions().size(), is(1));
        assertTrue(savedApprovalServiceAgreement.getAdditions().containsKey(key));
        assertTrue(savedApprovalServiceAgreement.getAdditions().containsValue(value));

        Set<ApprovalServiceAgreementParticipant> participants = savedApprovalServiceAgreement.getParticipants();
        assertEquals(2, participants.size());

        assertThat(participants, containsInAnyOrder(
            allOf(
                hasProperty("legalEntityId", is("LE-01")),
                hasProperty("shareUsers", is(true)),
                hasProperty("shareAccounts", is(false)),
                hasProperty("admins", containsInAnyOrder("admin1", "admin2"))
            ),
            allOf(
                hasProperty("legalEntityId", is("LE-02")),
                hasProperty("shareUsers", is(true)),
                hasProperty("shareAccounts", is(true)),
                hasProperty("admins", containsInAnyOrder("admin3", "admin4"))
            )
        ));

        verifyDefaultPermissionSets();
    }

    private void mockDefaultPermissionSets() {
        when(permissionSetService.getAssignablePermissionSetsById(anySet(), eq(true)))
            .thenReturn(
                Collections.singleton(new AssignablePermissionSet() {{
                    setId(1L);
                    setName("Test Regular");
                    setDescription("Test Regular");
                    setType(AssignablePermissionType.REGULAR_USER_DEFAULT);
                }})
            );

        when(permissionSetService.getAssignablePermissionSetsById(anySet(), eq(false)))
            .thenReturn(
                Collections.singleton(new AssignablePermissionSet() {{
                    setId(2L);
                    setName("Test Admin");
                    setDescription("Test Admin");
                    setType(AssignablePermissionType.ADMIN_USER_DEFAULT);
                }})
            );
    }


    private void verifyDefaultPermissionSets() {
        verify(permissionSetService, times(1)).getAssignablePermissionSetsById(anySet(), eq(true));
        verify(permissionSetService, times(1)).getAssignablePermissionSetsById(anySet(), eq(false));
    }

    @Test
    public void shouldFailOnInvalidPeriod() {
        String description = "des";
        String name = "name";
        Boolean isMaster = false;
        Map<String, String> additions = new HashMap<>();
        String key = "externalId";
        String value = "123456789";
        additions.put(key, value);

        ServiceAgreement serviceAgreement = createServiceAgreement(name, "id.external", description, new LegalEntity()
                .withId("id"),
            null, null);
        serviceAgreement.setAdditions(additions);
        serviceAgreement.setState(ServiceAgreementState.fromString(Status.DISABLED.toString()));

        mockDefaultPermissionSets();

        LocalDateTime localDateTIme = LocalDateTime.now();
        Date startDate = Date.from(localDateTIme.plusDays(3).atZone(ZoneId.systemDefault()).toInstant());
        Date endDate = Date.from(localDateTIme.plusDays(2).atZone(ZoneId.systemDefault()).toInstant());
        ServiceAgreementPostRequestBody addServiceAgreementRequestBody = createAddServiceAgreementRequestBody(name,
            description);
        addServiceAgreementRequestBody.setValidFromDate(dateTimeService.getStringDateFromDate(startDate));
        addServiceAgreementRequestBody.setValidUntilDate(dateTimeService.getStringDateFromDate(endDate));

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> persistenceServiceAgreementService.save(addServiceAgreementRequestBody, "id"));

        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_077.getErrorMessage(), ERR_ACC_077.getErrorCode()));
    }

    @Test
    public void updateServiceAgreementTest() {
        String serviceAgreementId = "SA ID";
        String description = "des";
        String name = "name";
        String newName = "new-name";
        String newDescription = "new-description";
        Map<String, String> additions = new HashMap<>();
        String key = "externalId";
        String oldValue = "123456789";
        String newValue = "987654321";
        String externalId = "ext.1";
        additions.put(key, oldValue);

        ServiceAgreement serviceAgreement = createServiceAgreement(name, "id.external", description, new LegalEntity(),
            null, null);
        serviceAgreement.setId(serviceAgreementId);
        serviceAgreement.setAdditions(additions);
        serviceAgreement.setState(ServiceAgreementState.ENABLED);

        when(serviceAgreementJpaRepository.findById(eq(serviceAgreementId),
            eq(SERVICE_AGREEMENT_EXTENDED_WITH_FUNCTION_GROUPS)))
            .thenReturn(Optional.of(serviceAgreement));
        when(serviceAgreementJpaRepository.save(Mockito.any(ServiceAgreement.class))).thenReturn(serviceAgreement);

        ServiceAgreementPutRequestBody serviceAgreementPutRequestBody =
            (ServiceAgreementPutRequestBody) new ServiceAgreementPutRequestBody()
                .withName(newName)
                .withDescription(newDescription)
                .withExternalId(externalId)
                .withStatus(Status.DISABLED)
                .withAddition(key, newValue);

        persistenceServiceAgreementService.updateServiceAgreement(serviceAgreementId, serviceAgreementPutRequestBody);

        ArgumentCaptor<ServiceAgreement> captor = ArgumentCaptor.forClass(ServiceAgreement.class);
        verify(serviceAgreementJpaRepository).save(captor.capture());

        assertEquals(newDescription, captor.getValue().getDescription());
        assertEquals(newName, captor.getValue().getName());
        assertEquals(externalId, captor.getValue().getExternalId());
        assertEquals(ServiceAgreementState.DISABLED, captor.getValue().getState());
        assertThat(captor.getValue().getAdditions().size(), is(1));
        assertTrue(captor.getValue().getAdditions().containsKey(key));
        assertTrue(captor.getValue().getAdditions().containsValue(newValue));
    }

    @Test
    public void shouldUpdateServiceAgreementAdminsAndSaveServiceAgreement() {
        String serviceAgreementId = "SA ID";
        String description = "des";
        String name = "name";
        String externalId = "ext.1";
        String adminId = "adminId";
        String systemFunctionGroupId = "systemFgId";

        ServiceAgreement approvalSa = createServiceAgreement(name, externalId, description, new LegalEntity(),
            null, null);
        approvalSa.setId(serviceAgreementId);
        approvalSa.setState(ServiceAgreementState.ENABLED);

        HashMap<String, com.backbase.accesscontrol.domain.Participant> approvalParticipants = new HashMap<>();
        HashMap<String, ServiceAgreementAdmin> approvalAdmins = new HashMap<>();
        approvalAdmins.put(adminId, new ServiceAgreementAdmin()
            .withUserId(adminId));

        LegalEntity approvalLegalEntity = new LegalEntity()
            .withId("le1");
        approvalParticipants.put(approvalLegalEntity.getId(), new com.backbase.accesscontrol.domain.Participant()
            .withAdmins(approvalAdmins)
            .withLegalEntity(approvalLegalEntity)
            .withShareAccounts(true)
            .withShareUsers(false)
            .withServiceAgreement(approvalSa));

        HashMap<String, ServiceAgreementAdmin> approvalAdmins3 = new HashMap<>();
        approvalAdmins3.put("adminId3", new ServiceAgreementAdmin()
            .withUserId("adminId3"));

        LegalEntity approvalLegalEntity3 = new LegalEntity()
            .withId("le3");
        approvalParticipants.put(approvalLegalEntity3.getId(), new com.backbase.accesscontrol.domain.Participant()
            .withAdmins(approvalAdmins3)
            .withLegalEntity(approvalLegalEntity3)
            .withShareAccounts(true)
            .withShareUsers(true)
            .withServiceAgreement(approvalSa));

        approvalSa.setParticipants(approvalParticipants);

        ServiceAgreement oldSa = createServiceAgreement(name, externalId, description, new LegalEntity(),
            null, null);
        oldSa.setId(serviceAgreementId);
        oldSa.setState(ServiceAgreementState.ENABLED);

        HashMap<String, com.backbase.accesscontrol.domain.Participant> participants = new HashMap<>();
        HashMap<String, ServiceAgreementAdmin> admins = new HashMap<>();
        admins.put(adminId, new ServiceAgreementAdmin()
            .withUserId(adminId));

        LegalEntity legalEntity = new LegalEntity()
            .withId("le1");
        com.backbase.accesscontrol.domain.Participant participant = new com.backbase.accesscontrol.domain.Participant()
            .withAdmins(emptyMap())
            .withLegalEntity(legalEntity)
            .withShareAccounts(false)
            .withShareUsers(true)
            .withServiceAgreement(oldSa);
        participant.addParticipantUsers(asList("user1", "user2"));
        participants.put(legalEntity.getId(), participant);

        LegalEntity legalEntity2 = new LegalEntity()
            .withId("le2");
        participants.put(legalEntity2.getId(), new com.backbase.accesscontrol.domain.Participant()
            .withAdmins(admins)
            .withLegalEntity(legalEntity2)
            .withServiceAgreement(oldSa));

        oldSa.setParticipants(participants);

        when(serviceAgreementJpaRepository
            .findById(serviceAgreementId,
                SERVICE_AGREEMENT_WITH_PARTICIPANTS_AND_ADDITIONS))
            .thenReturn(Optional.of(oldSa));

        when(serviceAgreementSystemFunctionGroupService.getServiceAgreementFunctionGroups(eq(oldSa)))
            .thenReturn(new ServiceAgreementFunctionGroups(systemFunctionGroupId, oldSa));

        persistenceServiceAgreementService.update(approvalSa);

        verify(serviceAgreementJpaRepository).save(eq(approvalSa));
        verify(userAccessFunctionGroupService)
            .deleteSystemFunctionGroupFromUserAccess(eq(systemFunctionGroupId), eq(adminId), eq(oldSa));
    }

    @Test
    public void updateServiceAgreementStateTest() {
        String serviceAgreementId = "SA ID";
        String description = "des";
        String name = "name";

        ServiceAgreement serviceAgreement = createServiceAgreement(name, "id.external", description, new LegalEntity(),
            null, null);
        serviceAgreement.setId(serviceAgreementId);
        serviceAgreement.setState(ServiceAgreementState.ENABLED);

        when(serviceAgreementJpaRepository.findById(eq(serviceAgreementId),
            eq(null)))
            .thenReturn(Optional.of(serviceAgreement));
        when(serviceAgreementJpaRepository.save(Mockito.any(ServiceAgreement.class))).thenReturn(serviceAgreement);

        persistenceServiceAgreementService.updateServiceAgreementState(serviceAgreementId,
            ServiceAgreementState.DISABLED);

        ArgumentCaptor<ServiceAgreement> captor = ArgumentCaptor.forClass(ServiceAgreement.class);
        verify(serviceAgreementJpaRepository).save(captor.capture());

        assertEquals(ServiceAgreementState.DISABLED, captor.getValue().getState());
    }

    @Test
    public void updateServiceAgreementWithValidFromDateTest() {
        String serviceAgreementId = "SA ID";
        String description = "des";
        String name = "name";
        String newName = "new-name";
        String newDescription = "new-description";
        Map<String, String> additions = new HashMap<>();
        String key = "externalId";
        String oldValue = "123456789";
        String newValue = "987654321";
        String externalId = "ext.1";
        additions.put(key, oldValue);

        ServiceAgreement serviceAgreement = createServiceAgreement(name, "id.external", description, new LegalEntity(),
            null, null);
        serviceAgreement.setId(serviceAgreementId);
        serviceAgreement.setAdditions(additions);
        serviceAgreement.setState(ServiceAgreementState.ENABLED);
        serviceAgreement.setStartDate(new Date(0));
        serviceAgreement.setEndDate(new Date(1));

        when(serviceAgreementJpaRepository.findById(eq(serviceAgreementId),
            eq(SERVICE_AGREEMENT_EXTENDED_WITH_FUNCTION_GROUPS)))
            .thenReturn(Optional.of(serviceAgreement));
        when(serviceAgreementJpaRepository.save(Mockito.any(ServiceAgreement.class))).thenReturn(serviceAgreement);

        ServiceAgreementPutRequestBody serviceAgreementPutRequestBody =
            (ServiceAgreementPutRequestBody) new ServiceAgreementPutRequestBody()
                .withName(newName)
                .withDescription(newDescription)
                .withExternalId(externalId)
                .withStatus(Status.DISABLED)
                .withValidFromDate("2019-02-01")
                .withValidUntilDate("2019-03-01")
                .withAddition(key, newValue);
        when(approvalFunctionGroupJpaRepository
            .findByServiceAgreementId(serviceAgreementId)).thenReturn(Optional.empty());
        persistenceServiceAgreementService.updateServiceAgreement(serviceAgreementId, serviceAgreementPutRequestBody);

        ArgumentCaptor<ServiceAgreement> captor = ArgumentCaptor.forClass(ServiceAgreement.class);
        verify(serviceAgreementJpaRepository).save(captor.capture());

        assertEquals(newDescription, captor.getValue().getDescription());
        assertEquals(newName, captor.getValue().getName());
        assertEquals(externalId, captor.getValue().getExternalId());
        assertEquals(ServiceAgreementState.DISABLED, captor.getValue().getState());
        assertThat(captor.getValue().getAdditions().size(), is(1));
        assertTrue(captor.getValue().getAdditions().containsKey(key));
        assertTrue(captor.getValue().getAdditions().containsValue(newValue));
        assertEquals(captor.getValue().getStartDate(), serviceAgreement.getStartDate());
        assertEquals(captor.getValue().getEndDate(), serviceAgreement.getEndDate());
    }

    @Test
    public void updateServiceAgreementWithOutValidFromDateTest() {
        String serviceAgreementId = "SA ID";
        String description = "des";
        String name = "name";
        String newName = "new-name";
        String newDescription = "new-description";
        Map<String, String> additions = new HashMap<>();
        String key = "externalId";
        String oldValue = "123456789";
        String newValue = "987654321";
        String externalId = "ext.1";
        additions.put(key, oldValue);

        ServiceAgreement serviceAgreement = createServiceAgreement(name, "id.external", description, new LegalEntity(),
            null, null);
        serviceAgreement.setId(serviceAgreementId);
        serviceAgreement.setAdditions(additions);
        serviceAgreement.setState(ServiceAgreementState.ENABLED);
        serviceAgreement.setStartDate(new Date(0));
        serviceAgreement.setEndDate(new Date(1));

        when(serviceAgreementJpaRepository.findById(eq(serviceAgreementId),
            eq(SERVICE_AGREEMENT_EXTENDED_WITH_FUNCTION_GROUPS)))
            .thenReturn(Optional.of(serviceAgreement));
        when(serviceAgreementJpaRepository.save(Mockito.any(ServiceAgreement.class))).thenReturn(serviceAgreement);

        ServiceAgreementPutRequestBody serviceAgreementPutRequestBody =
            (ServiceAgreementPutRequestBody) new ServiceAgreementPutRequestBody()
                .withName(newName)
                .withDescription(newDescription)
                .withExternalId(externalId)
                .withStatus(Status.DISABLED)
                .withAddition(key, newValue);

        persistenceServiceAgreementService.updateServiceAgreement(serviceAgreementId, serviceAgreementPutRequestBody);

        ArgumentCaptor<ServiceAgreement> captor = ArgumentCaptor.forClass(ServiceAgreement.class);
        verify(serviceAgreementJpaRepository).save(captor.capture());

        assertEquals(newDescription, captor.getValue().getDescription());
        assertEquals(newName, captor.getValue().getName());
        assertEquals(externalId, captor.getValue().getExternalId());
        assertEquals(ServiceAgreementState.DISABLED, captor.getValue().getState());
        assertThat(captor.getValue().getAdditions().size(), is(1));
        assertTrue(captor.getValue().getAdditions().containsKey(key));
        assertTrue(captor.getValue().getAdditions().containsValue(newValue));
        assertEquals(captor.getValue().getStartDate(), serviceAgreement.getStartDate());
        assertEquals(captor.getValue().getEndDate(), serviceAgreement.getEndDate());
    }

    @Test
    public void shouldThrowBadRequestOnUpdateServiceAgreementWithStartDateAfterEarliestFunctionGroupStartDateTest() {
        String serviceAgreementId = "SA ID";
        String description = "des";
        String name = "name";
        String newName = "new-name";
        String newDescription = "new-description";
        Map<String, String> additions = new HashMap<>();
        String key = "externalId";
        String oldValue = "123456789";
        String newValue = "987654321";
        String externalId = "ext.1";
        additions.put(key, oldValue);
        FunctionGroup functionGroup = new FunctionGroup();
        functionGroup.setId("FG ID");
        functionGroup.setStartDate(new Date(0));
        functionGroup.setType(FunctionGroupType.DEFAULT);

        ServiceAgreement serviceAgreement = createServiceAgreement(name, "id.external", description, new LegalEntity(),
            null, null);
        serviceAgreement.setId(serviceAgreementId);
        serviceAgreement.setAdditions(additions);
        serviceAgreement.setState(ServiceAgreementState.ENABLED);
        serviceAgreement.setStartDate(new Date(1000));
        serviceAgreement.setFunctionGroups(new HashSet<>(singletonList(functionGroup)));

        when(serviceAgreementJpaRepository.findById(eq(serviceAgreementId),
            eq(SERVICE_AGREEMENT_EXTENDED_WITH_FUNCTION_GROUPS)))
            .thenReturn(Optional.of(serviceAgreement));

        ServiceAgreementPutRequestBody serviceAgreementPutRequestBody =
            (ServiceAgreementPutRequestBody) new ServiceAgreementPutRequestBody()
                .withName(newName)
                .withDescription(newDescription)
                .withExternalId(externalId)
                .withValidFromDate("2019-13-32")
                .withStatus(Status.DISABLED)
                .withAddition(key, newValue);

        BadRequestException exception = assertThrows(BadRequestException.class, () -> persistenceServiceAgreementService
            .updateServiceAgreement(serviceAgreementId, serviceAgreementPutRequestBody));
        assertThat(exception, new BadRequestErrorMatcher(ERR_AG_094.getErrorMessage(), ERR_AG_094.getErrorCode()));
    }

    @Test
    public void shouldThrowBadRequestOnUpdateServiceAgreementWithEndDateBeforeLatestFunctionGroupEndDateTest() {
        String serviceAgreementId = "SA ID";
        String description = "des";
        String name = "name";
        String newName = "new-name";
        String newDescription = "new-description";
        Map<String, String> additions = new HashMap<>();
        String key = "externalId";
        String oldValue = "123456789";
        String newValue = "987654321";
        String externalId = "ext.1";
        additions.put(key, oldValue);
        FunctionGroup functionGroup = new FunctionGroup();
        functionGroup.setId("FG ID");
        functionGroup.setEndDate(new Date(5));
        functionGroup.setType(FunctionGroupType.DEFAULT);

        ServiceAgreement serviceAgreement = createServiceAgreement(name, "id.external", description, new LegalEntity(),
            null, null);
        serviceAgreement.setId(serviceAgreementId);
        serviceAgreement.setAdditions(additions);
        serviceAgreement.setState(ServiceAgreementState.ENABLED);
        serviceAgreement.setEndDate(new Date(4));
        serviceAgreement.setFunctionGroups(new HashSet<>(singletonList(functionGroup)));

        when(serviceAgreementJpaRepository.findById(eq(serviceAgreementId),
            eq(SERVICE_AGREEMENT_EXTENDED_WITH_FUNCTION_GROUPS)))
            .thenReturn(Optional.of(serviceAgreement));

        ServiceAgreementPutRequestBody serviceAgreementPutRequestBody =
            (ServiceAgreementPutRequestBody) new ServiceAgreementPutRequestBody()
                .withName(newName)
                .withDescription(newDescription)
                .withExternalId(externalId)
                .withValidUntilDate("1970-01-01")
                .withValidUntilTime("00:00:00")
                .withStatus(Status.DISABLED)
                .withAddition(key, newValue);

        BadRequestException exception = assertThrows(BadRequestException.class, () -> persistenceServiceAgreementService
            .updateServiceAgreement(serviceAgreementId, serviceAgreementPutRequestBody));
        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_086.getErrorMessage(), ERR_ACC_086.getErrorCode()));
    }

    @Test
    public void disableServiceAgreementTest() {
        String serviceAgreementId = "1";
        ServiceAgreement serviceAgreement = createServiceAgreement("name", "id.external", "description",
            new LegalEntity(), null, null);
        serviceAgreement.setState(ServiceAgreementState.ENABLED);

        when(serviceAgreementJpaRepository.findById(eq(serviceAgreementId),
            eq(SERVICE_AGREEMENT_EXTENDED_WITH_FUNCTION_GROUPS)))
            .thenReturn(Optional.of(serviceAgreement));

        ServiceAgreementPutRequestBody serviceAgreementPutRequestBody = new ServiceAgreementPutRequestBody()
            .withStatus(Status.DISABLED);

        persistenceServiceAgreementService.updateServiceAgreement(serviceAgreementId, serviceAgreementPutRequestBody);

        ArgumentCaptor<ServiceAgreement> captor = ArgumentCaptor.forClass(ServiceAgreement.class);
        verify(serviceAgreementJpaRepository).save(captor.capture());

        assertEquals(serviceAgreement.getDescription(), captor.getValue().getDescription());
        assertEquals(serviceAgreement.getName(), captor.getValue().getName());
        assertEquals(serviceAgreement.getExternalId(), captor.getValue().getExternalId());
        assertNotNull(captor.getValue().getStateChangedAt());
        assertEquals(ServiceAgreementState.DISABLED, captor.getValue().getState());
    }

    @Test
    public void updateServiceAgreementWhenExternalIdIsNullTest() {
        String serviceAgreementId = "SA ID";
        String description = "des";
        String name = "name";
        String newName = "new-name";
        String oldEternalId = "ext.1";
        String newDescription = "new-description";

        ServiceAgreement serviceAgreement = createServiceAgreement(name, oldEternalId, description, new LegalEntity(),
            null, null);
        serviceAgreement.setId(serviceAgreementId);

        when(serviceAgreementJpaRepository.findById(eq(serviceAgreementId),
            eq(SERVICE_AGREEMENT_EXTENDED_WITH_FUNCTION_GROUPS)))
            .thenReturn(Optional.of(serviceAgreement));
        when(serviceAgreementJpaRepository.save(Mockito.any(ServiceAgreement.class))).thenReturn(serviceAgreement);

        ServiceAgreementPutRequestBody serviceAgreementPutRequestBody = new ServiceAgreementPutRequestBody()
            .withName(newName)
            .withDescription(newDescription)
            .withExternalId(null);
        persistenceServiceAgreementService.updateServiceAgreement(serviceAgreementId, serviceAgreementPutRequestBody);

        ArgumentCaptor<ServiceAgreement> captor = ArgumentCaptor.forClass(ServiceAgreement.class);
        verify(serviceAgreementJpaRepository).save(captor.capture());

        assertEquals(newDescription, captor.getValue().getDescription());
        assertEquals(newName, captor.getValue().getName());
        assertEquals(oldEternalId, captor.getValue().getExternalId());
    }

    @Test
    public void updateServiceAgreementWhenNameIsNullTest() {
        String serviceAgreementId = "SA ID";
        String description = "des";
        String name = "name";
        String newName = null;
        String newDescription = "new-description";

        Map<String, String> additions = new HashMap<>();
        String key = "externalId";
        String value = "123456789";
        additions.put(key, value);

        ServiceAgreement serviceAgreement = createServiceAgreement(name, "id.external", description, new LegalEntity(),
            null, null);
        serviceAgreement.setId(serviceAgreementId);
        serviceAgreement.setAdditions(additions);

        ServiceAgreement newServiceAgreement = createServiceAgreement(name, "id.external",
            newDescription, new LegalEntity(),
            null, null);
        newServiceAgreement.setId(serviceAgreementId);
        newServiceAgreement.getAdditions().put(key, value);

        when(serviceAgreementJpaRepository.findById(eq(serviceAgreementId),
            eq(SERVICE_AGREEMENT_EXTENDED_WITH_FUNCTION_GROUPS)))
            .thenReturn(Optional.of(serviceAgreement));
        when(serviceAgreementJpaRepository.save(Mockito.any(ServiceAgreement.class))).thenReturn(serviceAgreement);

        ServiceAgreementPutRequestBody serviceAgreementPutRequestBody =
            (ServiceAgreementPutRequestBody) new ServiceAgreementPutRequestBody()
                .withName(newName)
                .withDescription(newDescription)
                .withAddition(key, value);

        persistenceServiceAgreementService.updateServiceAgreement(serviceAgreementId, serviceAgreementPutRequestBody);

        verify(serviceAgreementJpaRepository).save(refEq(newServiceAgreement));
    }

    @Test
    public void updateServiceAgreementWhenDescriptionIsNullTest() {
        String serviceAgreementId = "SA ID";
        String description = "des";
        String name = "name";
        String newName = "new-name";
        String newDescription = null;

        Map<String, String> additions = new HashMap<>();
        String key = "externalId";
        String value = "123456789";
        additions.put(key, value);

        ServiceAgreement serviceAgreement = createServiceAgreement(name, "id.external", description, new LegalEntity(),
            null, null);
        serviceAgreement.setId(serviceAgreementId);
        serviceAgreement.setAdditions(additions);

        ServiceAgreement newServiceAgreement = createServiceAgreement(newName, "id.external",
            description, new LegalEntity(), null, null);
        newServiceAgreement.setId(serviceAgreementId);

        when(serviceAgreementJpaRepository.findById(eq(serviceAgreementId),
            eq(SERVICE_AGREEMENT_EXTENDED_WITH_FUNCTION_GROUPS)))
            .thenReturn(Optional.of(serviceAgreement));
        when(serviceAgreementJpaRepository.save(Mockito.any(ServiceAgreement.class))).thenReturn(serviceAgreement);

        ServiceAgreementPutRequestBody serviceAgreementPutRequestBody = new ServiceAgreementPutRequestBody()
            .withName(newName)
            .withDescription(newDescription);

        persistenceServiceAgreementService.updateServiceAgreement(serviceAgreementId, serviceAgreementPutRequestBody);

        verify(serviceAgreementJpaRepository).save(refEq(newServiceAgreement));
    }

    @Test
    public void updateServiceAgreementWhenStatusIsChangedFromDisabledToEnabledTest() {
        String serviceAgreementId = "SA ID";
        String description = "des";
        String name = "name";
        String newName = "new-name";
        String newDescription = "new-description";
        Map<String, String> additions = new HashMap<>();
        String oldKey = "externalId";
        String oldValue = "123456789";
        String newKey = "second";
        String newValue = "987654321";
        String externalId = "ext.1";
        additions.put(oldKey, oldValue);

        ServiceAgreement serviceAgreement = createServiceAgreement(name, "id.external", description, new LegalEntity(),
            null, null);
        serviceAgreement.setId(serviceAgreementId);
        serviceAgreement.setAdditions(additions);
        serviceAgreement.setState(ServiceAgreementState.DISABLED);

        when(serviceAgreementJpaRepository.findById(eq(serviceAgreementId),
            eq(SERVICE_AGREEMENT_EXTENDED_WITH_FUNCTION_GROUPS)))
            .thenReturn(Optional.of(serviceAgreement));
        when(serviceAgreementJpaRepository.save(Mockito.any(ServiceAgreement.class))).thenReturn(serviceAgreement);

        ServiceAgreementPutRequestBody serviceAgreementPutRequestBody =
            (ServiceAgreementPutRequestBody) new ServiceAgreementPutRequestBody()
                .withName(newName)
                .withDescription(newDescription)
                .withExternalId(externalId)
                .withStatus(Status.ENABLED)
                .withAddition(newKey, newValue);

        persistenceServiceAgreementService.updateServiceAgreement(serviceAgreementId, serviceAgreementPutRequestBody);

        ArgumentCaptor<ServiceAgreement> captor = ArgumentCaptor.forClass(ServiceAgreement.class);
        verify(serviceAgreementJpaRepository).save(captor.capture());

        assertEquals(newDescription, captor.getValue().getDescription());
        assertEquals(newName, captor.getValue().getName());
        assertEquals(externalId, captor.getValue().getExternalId());
        assertEquals(ServiceAgreementState.ENABLED, captor.getValue().getState());
        assertThat(captor.getValue().getAdditions().size(), is(1));
        assertFalse(captor.getValue().getAdditions().containsKey(oldKey));
        assertFalse(captor.getValue().getAdditions().containsValue(oldValue));
        assertTrue(captor.getValue().getAdditions().containsKey(newKey));
        assertTrue(captor.getValue().getAdditions().containsValue(newValue));
    }

    @Test
    public void updateServiceAgreementWhenStatusIsNotChanged() {
        String serviceAgreementId = "SA ID";
        String description = "des";
        String name = "name";
        String newName = "new-name";
        String newDescription = "new-description";
        Map<String, String> additions = new HashMap<>();
        String key = "externalId";
        String value = "123456789";
        String externalId = "ext.1";
        additions.put(key, value);

        ServiceAgreement serviceAgreement = createServiceAgreement(name, "id.external", description, new LegalEntity(),
            null, null);
        serviceAgreement.setId(serviceAgreementId);
        serviceAgreement.setAdditions(additions);
        serviceAgreement.setState(ServiceAgreementState.DISABLED);

        when(serviceAgreementJpaRepository.findById(eq(serviceAgreementId),
            eq(SERVICE_AGREEMENT_EXTENDED_WITH_FUNCTION_GROUPS))).thenReturn(Optional.of(serviceAgreement));
        when(serviceAgreementJpaRepository.save(Mockito.any(ServiceAgreement.class))).thenReturn(serviceAgreement);

        ServiceAgreementPutRequestBody serviceAgreementPutRequestBody =
            (ServiceAgreementPutRequestBody) new ServiceAgreementPutRequestBody()
                .withName(newName)
                .withDescription(newDescription)
                .withExternalId(externalId)
                .withAddition(key, value);
        persistenceServiceAgreementService.updateServiceAgreement(serviceAgreementId, serviceAgreementPutRequestBody);

        ArgumentCaptor<ServiceAgreement> captor = ArgumentCaptor.forClass(ServiceAgreement.class);
        verify(serviceAgreementJpaRepository).save(captor.capture());

        assertEquals(newDescription, captor.getValue().getDescription());
        assertEquals(newName, captor.getValue().getName());
        assertEquals(externalId, captor.getValue().getExternalId());
        assertEquals(ServiceAgreementState.DISABLED, captor.getValue().getState());
        assertThat(captor.getValue().getAdditions().size(), is(1));
        assertTrue(captor.getValue().getAdditions().containsKey(key));
        assertTrue(captor.getValue().getAdditions().containsValue(value));
    }

    @Test
    public void getAllServiceAgreementsWithCreatorId() {

        when(serviceAgreementJpaRepository
            .findAllServiceAgreementsByParameters(null, "le1", null, SERVICE_AGREEMENT_WITH_ADDITIONS))
            .thenReturn(serviceAgreementsPage);

        Page<ServiceAgreement> res = persistenceServiceAgreementService.getServiceAgreements(null, "le1", null);
        assertEquals(serviceAgreements.size(), res.getContent().size());
        assertEquals(serviceAgreements.get(0).getState(), res.getContent().get(0).getState());
    }

    @Test
    public void getAllServiceAgreementsWithName() {

        List<ServiceAgreement> listToReturn = serviceAgreements.stream().filter(sa -> sa.getName().equals("saName1"))
            .collect(Collectors.toList());
        when(serviceAgreementJpaRepository
            .findAllServiceAgreementsByParameters("saName1", null, null, SERVICE_AGREEMENT_WITH_ADDITIONS))
            .thenReturn(new PageImpl<>(listToReturn));
        Page<ServiceAgreement> res = persistenceServiceAgreementService.getServiceAgreements("saName1", null, null);
        assertEquals(1, res.getContent().size());
        assertEquals(listToReturn.get(0).getState(), res.getContent().get(0).getState());
    }

    @Test
    public void testGetResponseBodyById() {
        String consumerLegalEntityId = "C-LE";
        String serviceAgreementId = "SA-001";

        LegalEntity legalEntity = createLegalEntity("LE-01", null);

        ServiceAgreement serviceAgreement = createServiceAgreement("name", "id.external", "desc", legalEntity, null,
            legalEntity.getId());
        com.backbase.accesscontrol.domain.Participant provider = serviceAgreement.getParticipants()
            .get(legalEntity.getId());
        provider.setLegalEntity(legalEntity);

        List<String> providerUsers = asList("1", "2");
        provider.addParticipantUsers(providerUsers);
        com.backbase.accesscontrol.domain.Participant consumer = createParticipantWithAdmin("A-1", false, true);
        serviceAgreement.addParticipant(consumer, consumerLegalEntityId, false, true);

        when(serviceAgreementJpaRepository.findById(serviceAgreementId, SERVICE_AGREEMENT_WITH_ADDITIONS))
            .thenReturn(Optional.of(serviceAgreement));

        ServiceAgreementItem responseBodyById = persistenceServiceAgreementService
            .getServiceAgreementResponseBodyById(serviceAgreementId);

        assertNotNull(responseBodyById);
        assertEquals(serviceAgreement.getId(), responseBodyById.getId());
        assertEquals(serviceAgreement.getName(), responseBodyById.getName());
        ServiceAgreementState returnedState = ServiceAgreementState.valueOf(responseBodyById.getStatus().toString());
        assertEquals(serviceAgreement.getState(), returnedState);
        assertEquals(serviceAgreement.getCreatorLegalEntity().getId(), responseBodyById.getCreatorLegalEntity());
    }

    @Test
    public void testGetResponseBodyByIdWithStartAndEndDateNotNull() {
        String consumerLegalEntityId = "C-LE";
        String serviceAgreementId = "SA-001";

        LegalEntity legalEntity = createLegalEntity("LE-01", null);

        ServiceAgreement serviceAgreement = createServiceAgreement("name", "id.external", "desc", legalEntity, null,
            legalEntity.getId());
        com.backbase.accesscontrol.domain.Participant provider = serviceAgreement.getParticipants()
            .get(legalEntity.getId());
        provider.setLegalEntity(legalEntity);

        List<String> providerUsers = asList("1", "2");
        provider.addParticipantUsers(providerUsers);
        com.backbase.accesscontrol.domain.Participant consumer = createParticipantWithAdmin("A-1", false, true);
        serviceAgreement.addParticipant(consumer, consumerLegalEntityId, false, true);
        serviceAgreement.setStartDate(new Date(0));
        serviceAgreement.setEndDate(new Date(1));

        when(serviceAgreementJpaRepository.findById(eq(serviceAgreementId), eq(SERVICE_AGREEMENT_WITH_ADDITIONS)))
            .thenReturn(Optional.of(serviceAgreement));

        ServiceAgreementItem responseBodyById = persistenceServiceAgreementService
            .getServiceAgreementResponseBodyById(serviceAgreementId);

        assertNotNull(responseBodyById);
        assertEquals(serviceAgreement.getId(), responseBodyById.getId());
        assertEquals(serviceAgreement.getStartDate(), responseBodyById.getValidFrom());
        assertEquals(serviceAgreement.getEndDate(), responseBodyById.getValidUntil());
        assertEquals(serviceAgreement.getName(), responseBodyById.getName());
        ServiceAgreementState returnedState = ServiceAgreementState.valueOf(responseBodyById.getStatus().toString());
        assertEquals(serviceAgreement.getState(), returnedState);
        assertEquals(serviceAgreement.getCreatorLegalEntity().getId(), responseBodyById.getCreatorLegalEntity());
    }

    @Test
    public void shouldThrowNotFoundExceptionWhenTryingToFindServiceAgreementById() {
        String serviceAgreementId = "SA-001";

        when(serviceAgreementJpaRepository.findById(serviceAgreementId, SERVICE_AGREEMENT_WITH_ADDITIONS))
            .thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> persistenceServiceAgreementService.getServiceAgreementResponseBodyById(serviceAgreementId));
        assertThat(exception, new NotFoundErrorMatcher(ERR_ACQ_006.getErrorMessage(), ERR_ACQ_006.getErrorCode()));
    }

    @Test
    public void testGetResponseBodyByExternalId() {
        String consumerLegalEntityId = "C-LE";
        String serviceAgreementExternalId = "id.external";

        LegalEntity legalEntity = createLegalEntity("LE-01", null);

        ServiceAgreement serviceAgreement = createServiceAgreement("name", serviceAgreementExternalId, "desc",
            legalEntity, null, legalEntity.getId());
        com.backbase.accesscontrol.domain.Participant provider = serviceAgreement.getParticipants()
            .get(legalEntity.getId());
        provider.setLegalEntity(legalEntity);

        List<String> providerUsers = asList("1", "2");
        provider.addParticipantUsers(providerUsers);
        com.backbase.accesscontrol.domain.Participant consumer = createParticipantWithAdmin("A-1", false, true);
        serviceAgreement.addParticipant(consumer, consumerLegalEntityId, false, true);

        mockGetServiceAgreementByExternalId(serviceAgreementExternalId, serviceAgreement);

        ServiceAgreement responseBody = persistenceServiceAgreementService
            .getServiceAgreementResponseBodyByExternalId(serviceAgreementExternalId).get();

        assertNotNull(responseBody);
        assertEquals(serviceAgreement.getId(), responseBody.getId());
        assertEquals(serviceAgreement.getName(), responseBody.getName());
        ServiceAgreementState returnedState = ServiceAgreementState.valueOf(responseBody.getState().toString());
        assertEquals(serviceAgreement.getState(), returnedState);
        assertEquals(serviceAgreement.getCreatorLegalEntity().getId(), responseBody.getCreatorLegalEntity().getId());
    }

    @Test
    public void testGetResponseBodyByExternalIdWithStartAndEndDateNotNull() {
        String consumerLegalEntityId = "C-LE";
        String serviceAgreementExternalId = "id.external";

        LegalEntity legalEntity = createLegalEntity("LE-01", null);

        ServiceAgreement serviceAgreement = createServiceAgreement("name", serviceAgreementExternalId, "desc",
            legalEntity, null, legalEntity.getId());
        com.backbase.accesscontrol.domain.Participant provider = serviceAgreement.getParticipants()
            .get(legalEntity.getId());
        provider.setLegalEntity(legalEntity);

        List<String> providerUsers = asList("1", "2");
        provider.addParticipantUsers(providerUsers);
        com.backbase.accesscontrol.domain.Participant consumer = createParticipantWithAdmin("A-1", false, true);
        serviceAgreement.addParticipant(consumer, consumerLegalEntityId, false, true);
        serviceAgreement.setStartDate(new Date(0));
        serviceAgreement.setEndDate(new Date(1));
        mockGetServiceAgreementByExternalId(serviceAgreementExternalId, serviceAgreement);

        ServiceAgreement responseBody = persistenceServiceAgreementService
            .getServiceAgreementResponseBodyByExternalId(serviceAgreementExternalId).get();

        assertNotNull(responseBody);
        assertEquals(serviceAgreement.getId(), responseBody.getId());
        assertEquals(serviceAgreement.getStartDate(), responseBody.getStartDate());
        assertEquals(serviceAgreement.getEndDate(), responseBody.getEndDate());
        assertEquals(serviceAgreement.getName(), responseBody.getName());
        ServiceAgreementState returnedState = ServiceAgreementState.valueOf(responseBody.getState().toString());
        assertEquals(serviceAgreement.getState(), returnedState);
        assertEquals(serviceAgreement.getCreatorLegalEntity().getId(), responseBody.getCreatorLegalEntity().getId());
    }

    @Test
    public void shouldReturnEmptyOptionalWhenNoServiceAgreementByExternalIdFound() {
        String serviceAgreementExternalId = "id.external";

        when(serviceAgreementJpaRepository.findByExternalId(eq(serviceAgreementExternalId)))
            .thenReturn(Optional.empty());

        Optional<ServiceAgreement> responseSa = persistenceServiceAgreementService
            .getServiceAgreementResponseBodyByExternalId(serviceAgreementExternalId);

        assertFalse(responseSa.isPresent());
    }

    @Test
    public void testGetServiceAgreementsResponseBodies() {
        String adminId1 = "AID-1";
        String adminId2 = "AID-2";

        ServiceAgreement serviceAgreement1 = serviceAgreements.stream()
            .filter(serviceAgreement -> serviceAgreement.getId().equals("1")).collect(Collectors.toList()).get(0);
        ServiceAgreement serviceAgreement2 = serviceAgreements.stream()
            .filter(serviceAgreement -> serviceAgreement.getId().equals("2")).collect(Collectors.toList()).get(0);
        serviceAgreement1.setStartDate(new Date(0));
        serviceAgreement1.setEndDate(new Date(1));

        serviceAgreement2.setStartDate(new Date(2));
        serviceAgreement2.setEndDate(new Date(3));

        List<com.backbase.accesscontrol.domain.Participant> providers = new ArrayList<>();
        List<com.backbase.accesscontrol.domain.Participant> consumers = new ArrayList<>();

        com.backbase.accesscontrol.domain.Participant provider1 = createParticipantWithAdmin(adminId1, true,
            false);
        List<String> providerUsers1 = asList("1", "2");
        provider1.addParticipantUsers(providerUsers1);
        providers.add(provider1);
        provider1.setServiceAgreement(serviceAgreement1);
        serviceAgreement1.addParticipant(provider1, "le1Id", true, false);

        com.backbase.accesscontrol.domain.Participant provider2 = createParticipantWithAdmin(adminId2, true,
            false);
        List<String> providerUsers2 = asList("3", "4", "5");
        provider2.addParticipantUsers(providerUsers2);
        providers.add(provider2);
        provider2.setServiceAgreement(serviceAgreement2);
        serviceAgreement2.addParticipant(provider2, "le2Id", true, false);

        com.backbase.accesscontrol.domain.Participant consumer1 = createParticipantWithAdmin(adminId1, false,
            true);
        consumers.add(consumer1);
        serviceAgreement1.addParticipant(consumer1, "le1Id", false, true);

        com.backbase.accesscontrol.domain.Participant consumer2 = createParticipantWithAdmin(adminId2, false,
            true);
        consumers.add(consumer2);
        serviceAgreement2.addParticipant(consumer2, "le2Id", false, true);

        when(serviceAgreementJpaRepository
            .findAllServiceAgreementsByParameters(null, "le1", null, SERVICE_AGREEMENT_WITH_ADDITIONS))
            .thenReturn(new PageImpl<>(serviceAgreements));

        Page<ServiceAgreement> response = persistenceServiceAgreementService.getServiceAgreements(null, "le1", null);

        List<ServiceAgreement> responseBodies = response.getContent();
        ServiceAgreement responseBody1 = responseBodies.stream()
            .filter(responseBody -> responseBody.getName().equals(serviceAgreement1.getName()))
            .collect(Collectors.toList()).get(0);
        ServiceAgreement responseBody2 = responseBodies.stream()
            .filter(responseBody -> responseBody.getName().equals(serviceAgreement2.getName()))
            .collect(Collectors.toList()).get(0);

        assertEquals(serviceAgreement1.getDescription(), responseBody1.getDescription());
        assertEquals(serviceAgreement2.getDescription(), responseBody2.getDescription());
        assertEquals(serviceAgreement1.getName(), responseBody1.getName());
        assertEquals(serviceAgreement2.getName(), responseBody2.getName());
        assertEquals(serviceAgreement1.getStartDate(), responseBody1.getStartDate());
        assertEquals(serviceAgreement2.getStartDate(), responseBody2.getStartDate());
        assertEquals(serviceAgreement1.getEndDate(), responseBody1.getEndDate());
        assertEquals(serviceAgreement2.getEndDate(), responseBody2.getEndDate());

        ServiceAgreementState returnedState1 = ServiceAgreementState.valueOf(responseBody1.getState().toString());
        assertEquals(serviceAgreement1.getState(), returnedState1);
        ServiceAgreementState returnedState2 = ServiceAgreementState.valueOf(responseBody2.getState().toString());
        assertEquals(serviceAgreement2.getState(), returnedState2);
    }

    @Test
    public void testGetServiceAgreementByExternalId() {
        String serviceAgreementExternalId = "id.sa.external";

        ServiceAgreement serviceAgreementFromRepo = new ServiceAgreement();
        serviceAgreementFromRepo.setExternalId(serviceAgreementExternalId);
        serviceAgreementFromRepo.setId("id.sa");

        mockGetServiceAgreementByExternalId(serviceAgreementExternalId, serviceAgreementFromRepo);

        ServiceAgreement serviceAgreement = persistenceServiceAgreementService
            .getServiceAgreementByExternalId(serviceAgreementExternalId);

        verify(serviceAgreementJpaRepository, times(1)).findByExternalId(eq(serviceAgreementExternalId));
        assertEquals(serviceAgreementFromRepo, serviceAgreement);
    }

    @Test
    public void testShouldThrowBadRequestWhenGetServiceAgreementByExternalId() {
        String serviceAgreementExternalId = "id.sa.external";

        when(serviceAgreementJpaRepository.findByExternalId(eq(serviceAgreementExternalId)))
            .thenReturn(Optional.empty());

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> persistenceServiceAgreementService.getServiceAgreementByExternalId(serviceAgreementExternalId));

        assertThat(exception, new BadRequestErrorMatcher(ERR_ACQ_006.getErrorMessage(), ERR_ACQ_006.getErrorCode()));
    }

    @Test
    public void testAddUserInServiceAgreementProviderNotFound() {
        String externalServiceAgreementId = "s ex id";
        String legalEntityId = "le id";
        String userId = "u id";
        when(participantJpaRepository
            .findDistinctByServiceAgreementExternalIdAndLegalEntityIdAndShareUsersTrue(externalServiceAgreementId,
                legalEntityId))
            .thenReturn(Optional.empty());

        BadRequestException exception = assertThrows(BadRequestException.class, () -> persistenceServiceAgreementService
            .addUserInServiceAgreementBatch(externalServiceAgreementId, userId, legalEntityId));
        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_038.getErrorMessage(), ERR_ACC_038.getErrorCode()));
    }

    @Test
    public void testAddUserInServiceAgreementProviderWithUserAlreadyExisting() {
        String externalServiceAgreementId = "s ex id";
        String legalEntityId = "le id";
        String userId = "u id";
        ServiceAgreement serviceAgreement = new ServiceAgreement();
        serviceAgreement.setMaster(false);
        com.backbase.accesscontrol.domain.Participant provider =
            new com.backbase.accesscontrol.domain.Participant();
        provider.setShareUsers(true);
        provider.addParticipantUser(userId);
        provider.setServiceAgreement(serviceAgreement);
        when(participantJpaRepository
            .findDistinctByServiceAgreementExternalIdAndLegalEntityIdAndShareUsersTrue(externalServiceAgreementId,
                legalEntityId))
            .thenReturn(Optional.of(provider));

        BadRequestException exception = assertThrows(BadRequestException.class, () -> persistenceServiceAgreementService
            .addUserInServiceAgreementBatch(externalServiceAgreementId, userId, legalEntityId));
        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_039.getErrorMessage(), ERR_ACC_039.getErrorCode()));
    }

    @Test
    public void testAddUserInMasterServiceAgreementShouldThrowBadRequest() {
        String externalServiceAgreementId = "s ex id";
        String legalEntityId = "le id";
        String userId = "u id";
        ServiceAgreement serviceAgreement = new ServiceAgreement();
        serviceAgreement.setMaster(true);
        com.backbase.accesscontrol.domain.Participant provider =
            new com.backbase.accesscontrol.domain.Participant();
        provider.setShareUsers(true);
        provider.addParticipantUser(userId);
        provider.setServiceAgreement(serviceAgreement);
        when(participantJpaRepository
            .findDistinctByServiceAgreementExternalIdAndLegalEntityIdAndShareUsersTrue(externalServiceAgreementId,
                legalEntityId))
            .thenReturn(Optional.of(provider));

        BadRequestException exception = assertThrows(BadRequestException.class, () -> persistenceServiceAgreementService
            .addUserInServiceAgreementBatch(externalServiceAgreementId, userId, legalEntityId));
        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_067.getErrorMessage(), ERR_ACC_067.getErrorCode()));
    }

    @Test
    public void testAddUserInServiceAgreementProviderSuccessful() {
        String externalServiceAgreementId = "s ex id";
        String legalEntityId = "le id";
        String userId = "u id";
        ServiceAgreement serviceAgreement = new ServiceAgreement();
        serviceAgreement.setMaster(false);
        com.backbase.accesscontrol.domain.Participant provider =
            new com.backbase.accesscontrol.domain.Participant();
        provider.setShareUsers(true);
        provider.setServiceAgreement(serviceAgreement);
        when(participantJpaRepository
            .findDistinctByServiceAgreementExternalIdAndLegalEntityIdAndShareUsersTrue(externalServiceAgreementId,
                legalEntityId))
            .thenReturn(Optional.of(provider));

        ArgumentCaptor<com.backbase.accesscontrol.domain.Participant> captor = ArgumentCaptor
            .forClass(com.backbase.accesscontrol.domain.Participant.class);

        persistenceServiceAgreementService
            .addUserInServiceAgreementBatch(externalServiceAgreementId, userId, legalEntityId);

        verify(participantJpaRepository).save(captor.capture());
        assertThat(captor.getValue().getParticipantUsers(), hasSize(1));
        assertEquals(userId, captor.getValue().getParticipantUsers().iterator().next().getUserId());
    }

    @Test
    public void testRemoveUserFromServiceAgreementProviderNotFound() {
        String externalServiceAgreementId = "s ex id";
        String legalEntityId = "le id";
        String userId = "u id";

        when(participantJpaRepository
            .findDistinctByServiceAgreementExternalIdAndLegalEntityIdAndShareUsersTrue(externalServiceAgreementId,
                legalEntityId))
            .thenReturn(Optional.empty());

        BadRequestException exception = assertThrows(BadRequestException.class, () -> persistenceServiceAgreementService
            .removeUserFromServiceAgreementBatch(externalServiceAgreementId, userId, legalEntityId));
        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_038.getErrorMessage(), ERR_ACC_038.getErrorCode()));
    }

    @Test
    public void testRemoveUserFromMasterServiceAgreementShouldThrowBadRequest() {
        String externalServiceAgreementId = "s ex id";
        String legalEntityId = "le id";
        String userId = "u id";
        ServiceAgreement serviceAgreement = new ServiceAgreement();
        serviceAgreement.setMaster(true);
        com.backbase.accesscontrol.domain.Participant provider =
            new com.backbase.accesscontrol.domain.Participant();
        provider.setShareUsers(true);
        provider.addParticipantUser(userId);
        provider.setServiceAgreement(serviceAgreement);

        when(participantJpaRepository
            .findDistinctByServiceAgreementExternalIdAndLegalEntityIdAndShareUsersTrue(externalServiceAgreementId,
                legalEntityId))
            .thenReturn(Optional.of(provider));

        BadRequestException exception = assertThrows(BadRequestException.class, () -> persistenceServiceAgreementService
            .removeUserFromServiceAgreementBatch(externalServiceAgreementId, userId, legalEntityId));
        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_067.getErrorMessage(), ERR_ACC_067.getErrorCode()));
    }

    @Test
    public void testRemoveUserFromServiceAgreementProviderUserNotFound() {
        String externalServiceAgreementId = "s ex id";
        String legalEntityId = "le id";
        String userId = "u id";

        ServiceAgreement serviceAgreement = new ServiceAgreement();
        serviceAgreement.setMaster(false);
        com.backbase.accesscontrol.domain.Participant provider =
            new com.backbase.accesscontrol.domain.Participant();
        provider.setShareUsers(true);
        provider.addParticipantUser("ANOTHER USER");
        provider.setServiceAgreement(serviceAgreement);
        when(participantJpaRepository
            .findDistinctByServiceAgreementExternalIdAndLegalEntityIdAndShareUsersTrue(externalServiceAgreementId,
                legalEntityId))
            .thenReturn(Optional.of(provider));

        BadRequestException exception = assertThrows(BadRequestException.class, () -> persistenceServiceAgreementService
            .removeUserFromServiceAgreementBatch(externalServiceAgreementId, userId, legalEntityId));
        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_040.getErrorMessage(), ERR_ACC_040.getErrorCode()));
    }

    @Test
    public void testRemoveUserFromServiceAgreementSuccessful() {
        String externalServiceAgreementId = "s ex id";
        String legalEntityId = "le id";
        String userId = "u id";

        com.backbase.accesscontrol.domain.Participant provider =
            new com.backbase.accesscontrol.domain.Participant();
        provider.setShareUsers(true);
        ServiceAgreement serviceAgreement = new ServiceAgreement();
        serviceAgreement.setState(ServiceAgreementState.ENABLED);
        serviceAgreement.setId("sa id");
        provider.setServiceAgreement(serviceAgreement);

        provider.addParticipantUser("ANOTHER USER");
        provider.addParticipantUser(userId);
        when(participantJpaRepository
            .findDistinctByServiceAgreementExternalIdAndLegalEntityIdAndShareUsersTrue(externalServiceAgreementId,
                legalEntityId))
            .thenReturn(Optional.of(provider));
        when(approvalUserContextJpaRepository.countByServiceAgreementIdAndUserIdIn(eq(serviceAgreement.getId()),
            eq(Sets.newHashSet(userId)))).thenReturn(0L);

        ArgumentCaptor<com.backbase.accesscontrol.domain.Participant> captor = ArgumentCaptor
            .forClass(com.backbase.accesscontrol.domain.Participant.class);

        persistenceServiceAgreementService
            .removeUserFromServiceAgreementBatch(externalServiceAgreementId, userId, legalEntityId);
        verify(userAccessFunctionGroupService)
            .checkIfUsersHaveAssignedPrivilegesForServiceAgreement(eq(serviceAgreement.getId()),
                eq(Sets.newHashSet(userId)));
        verify(approvalUserContextJpaRepository).countByServiceAgreementIdAndUserIdIn(eq(serviceAgreement.getId()),
            eq(Sets.newHashSet(userId)));
        verify(participantJpaRepository).save(eq(provider));
    }

    @Test
    public void testRemoveUserFromServiceAgreementThrowBadRequestExceptionWhenThereArePendingRequestsForUser() {
        String externalServiceAgreementId = "s ex id";
        String legalEntityId = "le id";
        String userId = "u id";

        com.backbase.accesscontrol.domain.Participant provider =
            new com.backbase.accesscontrol.domain.Participant();
        provider.setShareUsers(true);
        ServiceAgreement serviceAgreement = new ServiceAgreement();
        serviceAgreement.setState(ServiceAgreementState.ENABLED);
        serviceAgreement.setId("sa id");
        provider.setServiceAgreement(serviceAgreement);

        provider.addParticipantUser("ANOTHER USER");
        provider.addParticipantUser(userId);
        when(participantJpaRepository
            .findDistinctByServiceAgreementExternalIdAndLegalEntityIdAndShareUsersTrue(externalServiceAgreementId,
                legalEntityId))
            .thenReturn(Optional.of(provider));
        doThrow(getBadRequestException(CommandErrorCodes.ERR_ACC_075.getErrorMessage(),
            CommandErrorCodes.ERR_ACC_075.getErrorCode()))
            .when(approvalUserContextJpaRepository)
            .countByServiceAgreementIdAndUserIdIn(eq(serviceAgreement.getId()), refEq(newHashSet(userId)));

        BadRequestException exception = assertThrows(BadRequestException.class, () -> persistenceServiceAgreementService
            .removeUserFromServiceAgreementBatch(externalServiceAgreementId, userId, legalEntityId));
        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_075.getErrorMessage(), ERR_ACC_075.getErrorCode()));
    }

    @Test
    public void shouldGetUsersInServiceAgreement() {
        String serviceAgreementId = "id.sa";

        when(serviceAgreementJpaRepository.existsById(serviceAgreementId)).thenReturn(true);

        ParticipantUser participantUser1 = getProviderUser("user1");
        ParticipantUser participantUser2 = getProviderUser("user2");
        ParticipantUser participantUser3 = getProviderUser("user3");

        com.backbase.accesscontrol.domain.Participant provider1 = getProvider(participantUser1, participantUser2);
        com.backbase.accesscontrol.domain.Participant provider2 = getProvider(participantUser2);
        com.backbase.accesscontrol.domain.Participant provider3 = getProvider(participantUser1, participantUser2,
            participantUser3);

        List<com.backbase.accesscontrol.domain.Participant> providersFromRepository = new ArrayList<>();
        providersFromRepository.add(provider1);
        providersFromRepository.add(provider2);
        providersFromRepository.add(provider3);

        when(participantJpaRepository
            .findByServiceAgreementIdInAndShareUsersIsTrue(anyList(), eq(
                PARTICIPANT_WITH_SERVICE_AGREEMENT_LEGAL_ENTITY_USERS)))
            .thenReturn(providersFromRepository);

        ServiceAgreementUsersGetResponseBody serviceAgreementUsers = persistenceServiceAgreementService
            .getServiceAgreementUsers(serviceAgreementId);

        assertEquals(3, serviceAgreementUsers.getUserIds().size());
        assertTrue(serviceAgreementUsers.getUserIds().contains(participantUser1.getUserId()));
        assertTrue(serviceAgreementUsers.getUserIds().contains(participantUser2.getUserId()));
        assertTrue(serviceAgreementUsers.getUserIds().contains(participantUser3.getUserId()));
    }

    @Test
    public void shouldThrowBadRequestWhenGettingUsersInServiceAgreement() {
        String serviceAgreementId = "id.sa";

        when(serviceAgreementJpaRepository.existsById(serviceAgreementId)).thenReturn(false);

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> persistenceServiceAgreementService.getServiceAgreementUsers(serviceAgreementId));
        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_029.getErrorMessage(), ERR_ACC_029.getErrorCode()));
    }

    @Test
    public void shouldAddParticipantToServiceAgreement() {
        PresentationParticipantPutBody item = new PresentationParticipantPutBody()
            .withExternalServiceAgreementId("exSAId")
            .withExternalParticipantId("exLEId")
            .withSharingAccounts(true)
            .withSharingUsers(true);

        ServiceAgreement serviceAgreement = getServiceAgreements().get(0);
        com.backbase.accesscontrol.domain.Participant consumer =
            new com.backbase.accesscontrol.domain.Participant();
        consumer.setServiceAgreement(serviceAgreement);
        consumer.setShareAccounts(true);
        consumer.setLegalEntity(serviceAgreement.getCreatorLegalEntity());
        serviceAgreement.addParticipant(consumer, consumer.getLegalEntity().getId(), false, true);

        com.backbase.accesscontrol.domain.Participant provider =
            new com.backbase.accesscontrol.domain.Participant();
        provider.setShareUsers(true);
        provider.setServiceAgreement(serviceAgreement);
        provider.setLegalEntity(serviceAgreement.getCreatorLegalEntity());
        serviceAgreement.addParticipant(provider, provider.getLegalEntity().getId(), true, false);

        when(participantJpaRepository.findByServiceAgreementExternalIdAndServiceAgreementIsMasterFalse(eq(item
            .getExternalServiceAgreementId())))
            .thenReturn(Lists.newArrayList(consumer, provider));
        LegalEntity assignedLegalEntity = LegalEntityUtil.createLegalEntity("assinged", "name-assinged",
            item.getExternalParticipantId(),
            serviceAgreement.getCreatorLegalEntity(), LegalEntityType.BANK);
        assignedLegalEntity.getLegalEntityAncestors().add(serviceAgreement.getCreatorLegalEntity());
        when(legalEntityJpaRepository
            .findByExternalId(eq(item.getExternalParticipantId())))
            .thenReturn(Optional.of(assignedLegalEntity));
        when(serviceAgreementJpaRepository
            .findByExternalId(item.getExternalServiceAgreementId()))
            .thenReturn(Optional.of(serviceAgreement));

        when(legalEntityJpaRepository.findByLegalEntityAncestorsIdAndIdIn(
            Mockito.any(String.class),
            Mockito.any(List.class)))
            .thenReturn(singletonList((IdProjection) assignedLegalEntity::getId));

        assertEquals("1", persistenceServiceAgreementService.addParticipant(item));

        verify(participantJpaRepository).save(participantCaptor.capture());

        com.backbase.accesscontrol.domain.Participant participant = participantCaptor.getValue();

        assertThat(
            participant,
            allOf(
                hasProperty("legalEntity", is(assignedLegalEntity)),
                hasProperty("shareAccounts", is(item.getSharingAccounts())),
                hasProperty("shareUsers", is(item.getSharingUsers()))
            )

        );
    }

    @Test
    public void shouldAddParticipantToServiceAgreementWhenParticipantListIsEmpty() {
        PresentationParticipantPutBody item = new PresentationParticipantPutBody()
            .withExternalServiceAgreementId("exSAId")
            .withExternalParticipantId("exLEId")
            .withSharingAccounts(true)
            .withSharingUsers(true);

        ServiceAgreement serviceAgreement = getServiceAgreements().get(0);

        when(serviceAgreementJpaRepository
            .findByExternalId(item.getExternalServiceAgreementId()))
            .thenReturn(Optional.of(serviceAgreement));
        when(participantJpaRepository.findByServiceAgreementExternalIdAndServiceAgreementIsMasterFalse(eq(item
            .getExternalServiceAgreementId())))
            .thenReturn(new ArrayList<>());
        LegalEntity assignedLegalEntity = LegalEntityUtil.createLegalEntity("assinged", "name-assinged",
            item.getExternalParticipantId(),
            serviceAgreement.getCreatorLegalEntity(), LegalEntityType.BANK);
        assignedLegalEntity.getLegalEntityAncestors().add(serviceAgreement.getCreatorLegalEntity());
        when(legalEntityJpaRepository
            .findByExternalId(eq(item.getExternalParticipantId())))
            .thenReturn(Optional.of(assignedLegalEntity));
        when(legalEntityJpaRepository.findByLegalEntityAncestorsIdAndIdIn(
            Mockito.any(String.class),
            Mockito.any(List.class)))
            .thenReturn(singletonList((IdProjection) assignedLegalEntity::getId));

        assertEquals("1", persistenceServiceAgreementService.addParticipant(item));

        verify(participantJpaRepository).save(participantCaptor.capture());

        com.backbase.accesscontrol.domain.Participant participant = participantCaptor.getValue();

        assertThat(
            participant,
            allOf(
                hasProperty("legalEntity", is(assignedLegalEntity)),
                hasProperty("shareAccounts", is(item.getSharingAccounts())),
                hasProperty("shareUsers", is(item.getSharingUsers()))
            )

        );
    }

    @Test
    public void shouldThrowBadRequestOnAddParticipantToMasterServiceAgreement() {
        PresentationParticipantPutBody item = new PresentationParticipantPutBody()
            .withExternalServiceAgreementId("exSAId")
            .withExternalParticipantId("exLEId")
            .withSharingAccounts(true)
            .withSharingUsers(true);

        ServiceAgreement serviceAgreement = getServiceAgreements().get(2);

        when(serviceAgreementJpaRepository
            .findByExternalId(item.getExternalServiceAgreementId()))
            .thenReturn(Optional.of(serviceAgreement));
        when(participantJpaRepository.findByServiceAgreementExternalIdAndServiceAgreementIsMasterFalse(eq(item
            .getExternalServiceAgreementId())))
            .thenReturn(new ArrayList<>());
        LegalEntity assignedLegalEntity = LegalEntityUtil.createLegalEntity("assinged", "name-assinged",
            item.getExternalParticipantId(),
            serviceAgreement.getCreatorLegalEntity(), LegalEntityType.BANK);
        assignedLegalEntity.getLegalEntityAncestors().add(serviceAgreement.getCreatorLegalEntity());
        when(legalEntityJpaRepository
            .findByExternalId(eq(item.getExternalParticipantId())))
            .thenReturn(Optional.of(assignedLegalEntity));

        when(legalEntityJpaRepository.findByLegalEntityAncestorsIdAndIdIn(
            Mockito.any(String.class),
            Mockito.any(List.class)))
            .thenReturn(singletonList((IdProjection) assignedLegalEntity::getId));

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> persistenceServiceAgreementService.addParticipant(item));
        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_044.getErrorMessage(), ERR_ACC_044.getErrorCode()));
    }

    @Test
    public void shouldThrowBadRequestAndVerifyThatRepoIsCalled1TimeWhenRemovingParticipantFromServiceAgreement() {

        String saId = "sa1";
        Participant participant = new Participant()
            .withId("le1_" + "id")
            .withSharingAccounts(false)
            .withSharingUsers(true)
            .withAdmins(newHashSet("admin1_" + "id"));
        Set<Participant> participants = Sets.newHashSet(
            participant);

        ServiceAgreementSave serviceAgreementSave = getServiceAgreementSaveMock(saId, newHashSet(participants));
        serviceAgreementSave.setExternalId("new Id");
        serviceAgreementSave.setName("new Name");
        serviceAgreementSave.setDescription("new description");
        serviceAgreementSave
            .setStatus(
                com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.Status.DISABLED);
        Map<String, String> additionalProps = new HashMap<>();
        additionalProps.put("second", "12345");
        serviceAgreementSave.setAdditions(additionalProps);

        com.backbase.accesscontrol.domain.Participant participant1 =
            new com.backbase.accesscontrol.domain.Participant();
        participant1.setId("par1_1");
        LegalEntity legalEntity = createLegalEntity("le1_1", null);
        participant1.setLegalEntity(legalEntity);
        participant1.setShareAccounts(false);
        participant1.setShareUsers(true);
        participant1.addAdmin("admin1_1");
        Set<ParticipantUser> users = new HashSet<>();
        for (int i = 0; i < 1050; i++) {
            ParticipantUser user = new ParticipantUser()
                .withUserId("admin1_1" + i);
            users.add(user);
        }
        participant1.setParticipantUsers(users);

        ServiceAgreement serviceAgreementMock = createServiceAgreementMock(saId);
        Map<String, com.backbase.accesscontrol.domain.Participant> pmap = new HashMap<>();
        pmap.put(participant1.getId(), participant1);
        serviceAgreementMock.setParticipants(pmap);
        serviceAgreementMock.setId(saId);
        when(serviceAgreementJpaRepository.findById(saId, SERVICE_AGREEMENT_EXTENDED_WITH_FUNCTION_GROUPS))
            .thenReturn(Optional.of(serviceAgreementMock));
        when(userAssignedFunctionGroupJpaRepository.existsByServiceAgreementIdAndUserIdIn(any(),
            any()))
            .thenReturn(true);

        createLegalEntity("le1_" + saId, null);
        createLegalEntity("le2_" + saId, null);
        mockApprovalValidation(applicationProperties, false);

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> persistenceServiceAgreementService.save(saId, serviceAgreementSave));
        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_054.getErrorMessage(), ERR_ACC_054.getErrorCode()));

        verify((userAssignedFunctionGroupJpaRepository), times(1))
            .existsByServiceAgreementIdAndUserIdIn(eq(saId), any());

    }

    @Test
    public void shouldThrowBadRequestAndVerifyThatRepoIsCalled1TimeWhenRemovingParticipantFromSAApproval() {

        String saId = "sa1";
        Participant participant = new Participant()
            .withId("le1_" + "id")
            .withSharingAccounts(false)
            .withSharingUsers(true)
            .withAdmins(newHashSet("admin1_" + "id"));
        Set<Participant> participants = Sets.newHashSet(
            participant);

        ServiceAgreementSave serviceAgreementSave = getServiceAgreementSaveMock(saId, newHashSet(participants));
        serviceAgreementSave.setExternalId("new Id");
        serviceAgreementSave.setName("new Name");
        serviceAgreementSave.setDescription("new description");
        serviceAgreementSave
            .setStatus(
                com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.Status.DISABLED);
        Map<String, String> additionalProps = new HashMap<>();
        additionalProps.put("second", "12345");
        serviceAgreementSave.setAdditions(additionalProps);

        com.backbase.accesscontrol.domain.Participant participant1 =
            new com.backbase.accesscontrol.domain.Participant();
        participant1.setId("par1_1");
        LegalEntity legalEntity = createLegalEntity("le1_1", null);
        participant1.setLegalEntity(legalEntity);
        participant1.setShareAccounts(false);
        participant1.setShareUsers(true);
        participant1.addAdmin("admin1_1");
        Set<ParticipantUser> users = new HashSet<>();
        for (int i = 0; i < 1050; i++) {
            ParticipantUser user = new ParticipantUser()
                .withUserId("admin1_1" + i);
            users.add(user);
        }
        participant1.setParticipantUsers(users);

        ServiceAgreement serviceAgreementMock = createServiceAgreementMock(saId);
        Map<String, com.backbase.accesscontrol.domain.Participant> pmap = new HashMap<>();
        pmap.put(participant1.getId(), participant1);
        serviceAgreementMock.setParticipants(pmap);
        serviceAgreementMock.setId(saId);
        when(serviceAgreementJpaRepository.findById(saId, SERVICE_AGREEMENT_EXTENDED_WITH_FUNCTION_GROUPS))
            .thenReturn(Optional.of(serviceAgreementMock));
        when(userAssignedFunctionGroupJpaRepository.existsByServiceAgreementIdAndUserIdIn(any(),
            any()))
            .thenReturn(false);
        when(approvalUserContextJpaRepository.existsByServiceAgreementIdAndUserIdIn(any(),
            any()))
            .thenReturn(true);

        createLegalEntity("le1_" + saId, null);
        createLegalEntity("le2_" + saId, null);
        mockApprovalValidation(applicationProperties, true);

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> persistenceServiceAgreementService.save(saId, serviceAgreementSave));
        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_054.getErrorMessage(), ERR_ACC_054.getErrorCode()));

        verify((userAssignedFunctionGroupJpaRepository), times(1))
            .existsByServiceAgreementIdAndUserIdIn(eq(saId), any());

    }

    @Test
    public void shouldRemoveParticipantFromServiceAgreement() {
        PresentationParticipantPutBody item = new PresentationParticipantPutBody()
            .withExternalServiceAgreementId("exSAId")
            .withExternalParticipantId("exLEId")
            .withAction(PresentationAction.REMOVE);
        LegalEntity creatorLegalEntity = new LegalEntity();
        creatorLegalEntity.setId("le-id");
        creatorLegalEntity.setName("le");
        creatorLegalEntity.setExternalId("exLEId");
        creatorLegalEntity.setType(LegalEntityType.BANK);
        creatorLegalEntity.setParent(null);

        LegalEntity otherLegalEntity = new LegalEntity();
        otherLegalEntity.setId("le2");
        otherLegalEntity.setExternalId("ex-le-2");
        otherLegalEntity.setName("name");

        ServiceAgreement serviceAgreement = new ServiceAgreement();
        serviceAgreement.setState(ServiceAgreementState.ENABLED);
        serviceAgreement.setId("id");
        serviceAgreement.setMaster(false);
        serviceAgreement.setCreatorLegalEntity(creatorLegalEntity);
        serviceAgreement.setExternalId("ex-sa-id");

        ServiceAgreementAdmin admin1 = new ServiceAgreementAdmin();
        admin1.setId("a1");
        ServiceAgreementAdmin admin2 = new ServiceAgreementAdmin();
        admin2.setId("a2");
        Map<String, ServiceAgreementAdmin> admins = new HashMap<>();
        admins.put("a1", admin1);
        admins.put("a2", admin2);

        com.backbase.accesscontrol.domain.Participant participantToRemove =
            new com.backbase.accesscontrol.domain.Participant();
        participantToRemove.setServiceAgreement(serviceAgreement);
        participantToRemove.setId("p-id");
        participantToRemove.setLegalEntity(creatorLegalEntity);
        participantToRemove.setAdmins(admins);

        com.backbase.accesscontrol.domain.Participant otherParticipant =
            new com.backbase.accesscontrol.domain.Participant();
        otherParticipant.setServiceAgreement(serviceAgreement);
        otherParticipant.setLegalEntity(otherLegalEntity);

        Map<String, com.backbase.accesscontrol.domain.Participant> participants = new HashMap<>();
        participants.put("le-id", participantToRemove);
        participants.put("le2", otherParticipant);

        serviceAgreement.setParticipants(participants);

        when(participantJpaRepository.findByServiceAgreementExternalIdAndLegalEntityExternalId(eq(item
            .getExternalServiceAgreementId()), eq(item.getExternalParticipantId())))
            .thenReturn(Optional.ofNullable(participantToRemove));

        ServiceAgreementFunctionGroups agreementFunctionGroups = mock(ServiceAgreementFunctionGroups.class);
        when(agreementFunctionGroups.getSystemFunctionGroup())
            .thenReturn("1");
        when(serviceAgreementSystemFunctionGroupService.getServiceAgreementFunctionGroups(eq(serviceAgreement)))
            .thenReturn(agreementFunctionGroups);

        assertEquals("id", persistenceServiceAgreementService.removeParticipant(item));

        ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);
        verify(userAccessFunctionGroupService, times(2))
            .deleteSystemFunctionGroupFromUserAccess(eq("1"), argumentCaptor.capture(),
                eq(participantToRemove.getServiceAgreement()));
        verify(participantJpaRepository).delete(participantCaptor.capture());

        assertThat(argumentCaptor.getAllValues(), containsInAnyOrder("a1", "a2"));
        com.backbase.accesscontrol.domain.Participant participant = participantCaptor.getValue();
        assertEquals(creatorLegalEntity.getExternalId(), participant.getLegalEntity().getExternalId());
        assertEquals(serviceAgreement.getExternalId(), participant.getServiceAgreement().getExternalId());

    }

    @Test
    public void shouldThrowNotFoundExceptionRemoveParticipantFromServiceAgreement() {
        PresentationParticipantPutBody item = new PresentationParticipantPutBody()
            .withExternalServiceAgreementId("exSAId")
            .withExternalParticipantId("exLEId")
            .withAction(PresentationAction.REMOVE);

        when(participantJpaRepository.findByServiceAgreementExternalIdAndLegalEntityExternalId(eq(item
            .getExternalServiceAgreementId()), eq(item.getExternalParticipantId())))
            .thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> persistenceServiceAgreementService.removeParticipant(item));
        assertThat(exception, new NotFoundErrorMatcher(ERR_ACC_044.getErrorMessage(), ERR_ACC_044.getErrorCode()));
    }

    @Test
    public void shouldThrowBadRequestOnRemovingParticipantWhenThereArePendingPermissionsForUserInServiceAgreement() {
        PresentationParticipantPutBody item = new PresentationParticipantPutBody()
            .withExternalServiceAgreementId("exSAId")
            .withExternalParticipantId("exLEId")
            .withAction(PresentationAction.REMOVE);
        LegalEntity creatorLegalEntity = new LegalEntity();
        creatorLegalEntity.setId("le-id");
        creatorLegalEntity.setName("le");
        creatorLegalEntity.setExternalId("exLEId");
        creatorLegalEntity.setType(LegalEntityType.BANK);
        creatorLegalEntity.setParent(null);

        LegalEntity otherLegalEntity = new LegalEntity();
        otherLegalEntity.setId("le2");
        otherLegalEntity.setExternalId("ex-le-2");
        otherLegalEntity.setName("name");

        ServiceAgreement serviceAgreement = new ServiceAgreement();
        serviceAgreement.setState(ServiceAgreementState.ENABLED);
        serviceAgreement.setId("id");
        serviceAgreement.setMaster(false);
        serviceAgreement.setCreatorLegalEntity(creatorLegalEntity);
        serviceAgreement.setExternalId("ex-sa-id");

        ServiceAgreementAdmin admin1 = new ServiceAgreementAdmin();
        admin1.setId("a1");
        ServiceAgreementAdmin admin2 = new ServiceAgreementAdmin();
        admin2.setId("a2");
        Map<String, ServiceAgreementAdmin> admins = new HashMap<>();
        admins.put("a1", admin1);
        admins.put("a2", admin2);

        com.backbase.accesscontrol.domain.Participant participantToRemove =
            new com.backbase.accesscontrol.domain.Participant();
        participantToRemove.setServiceAgreement(serviceAgreement);
        participantToRemove.setId("p-id");
        participantToRemove.setLegalEntity(creatorLegalEntity);
        participantToRemove.setAdmins(admins);

        com.backbase.accesscontrol.domain.Participant otherParticipant =
            new com.backbase.accesscontrol.domain.Participant();
        otherParticipant.setServiceAgreement(serviceAgreement);
        otherParticipant.setLegalEntity(otherLegalEntity);

        Map<String, com.backbase.accesscontrol.domain.Participant> participants = new HashMap<>();
        participants.put("le-id", participantToRemove);
        participants.put("le2", otherParticipant);

        serviceAgreement.setParticipants(participants);

        when(participantJpaRepository.findByServiceAgreementExternalIdAndLegalEntityExternalId(eq(item
            .getExternalServiceAgreementId()), eq(item.getExternalParticipantId())))
            .thenReturn(Optional.ofNullable(participantToRemove));

        when(approvalUserContextJpaRepository
            .countByServiceAgreementIdAndLegalEntityId(eq(serviceAgreement.getId()), eq(creatorLegalEntity.getId())))
            .thenReturn(1L);

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> persistenceServiceAgreementService.removeParticipant(item));
        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_078.getErrorMessage(), ERR_ACC_078.getErrorCode()));
    }

    @Test
    public void throwBadRequestWhenUserHasPermissionsUnderSpecificServiceAgreement() {
        PresentationParticipantPutBody item = new PresentationParticipantPutBody()
            .withExternalServiceAgreementId("exSAId")
            .withExternalParticipantId("exLEId")
            .withAction(PresentationAction.REMOVE);

        LegalEntity creatorLegalEntity = new LegalEntity();
        creatorLegalEntity.setId("le-id");
        creatorLegalEntity.setName("le");
        creatorLegalEntity.setExternalId("exLEId");
        creatorLegalEntity.setType(LegalEntityType.BANK);
        creatorLegalEntity.setParent(null);

        ServiceAgreement serviceAgreement = new ServiceAgreement();
        serviceAgreement.setState(ServiceAgreementState.ENABLED);
        serviceAgreement.setId("id");
        serviceAgreement.setMaster(false);
        serviceAgreement.setCreatorLegalEntity(creatorLegalEntity);
        serviceAgreement.setExternalId("ex-sa-id");

        String userId = "User_ID";

        ParticipantUser participantUser = new ParticipantUser();
        participantUser.setUserId(userId);

        Set<ParticipantUser> participantUsers = new HashSet<>();
        participantUsers.add(participantUser);

        com.backbase.accesscontrol.domain.Participant participantToRemove =
            new com.backbase.accesscontrol.domain.Participant();
        participantToRemove.setServiceAgreement(serviceAgreement);
        participantToRemove.setId("p-id");
        participantToRemove.setLegalEntity(creatorLegalEntity);
        participantToRemove.setShareUsers(true);
        participantToRemove.setParticipantUsers(participantUsers);

        when(userAccessFunctionGroupService
            .getAllUserIdsByServiceAgreementExternalIdAndFunctionGroupType("exSAId", FunctionGroupType.DEFAULT))
            .thenReturn(singletonList(userId));

        when(participantJpaRepository.findByServiceAgreementExternalIdAndLegalEntityExternalId(eq(item
            .getExternalServiceAgreementId()), eq(item.getExternalParticipantId())))
            .thenReturn(Optional.of(participantToRemove));

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> persistenceServiceAgreementService.removeParticipant(item));
        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_054.getErrorMessage(), ERR_ACC_054.getErrorCode()));

    }

    @Test
    public void shouldUpdateParticipantToServiceAgreementWhenRootAddedAndCreatedFromRoot() {
        PresentationParticipantPutBody item = new PresentationParticipantPutBody()
            .withExternalServiceAgreementId("exSAId")
            .withExternalParticipantId("exLEId")
            .withSharingAccounts(true)
            .withSharingUsers(true);

        LegalEntity assignedLegalEntity = LegalEntityUtil.createLegalEntity("assinged", "name-assinged",
            item.getExternalParticipantId(),
            null, LegalEntityType.BANK);

        ServiceAgreement serviceAgreement = getServiceAgreements().get(0);
        LegalEntity creatorLegalEntity = serviceAgreement.getCreatorLegalEntity();
        serviceAgreement.setCreatorLegalEntity(assignedLegalEntity);

        com.backbase.accesscontrol.domain.Participant consumer =
            new com.backbase.accesscontrol.domain.Participant();
        consumer.setShareAccounts(true);
        consumer.setServiceAgreement(serviceAgreement);
        consumer.setLegalEntity(creatorLegalEntity);
        serviceAgreement.addParticipant(consumer, creatorLegalEntity.getId(), false, true);

        com.backbase.accesscontrol.domain.Participant provider =
            new com.backbase.accesscontrol.domain.Participant();
        provider.setShareUsers(true);
        provider.setServiceAgreement(serviceAgreement);
        provider.setLegalEntity(creatorLegalEntity);
        serviceAgreement.addParticipant(provider, creatorLegalEntity.getId(), true, false);

        when(participantJpaRepository.findByServiceAgreementExternalIdAndServiceAgreementIsMasterFalse(eq(item
            .getExternalServiceAgreementId())))
            .thenReturn(Lists.newArrayList(consumer, provider));

        when(legalEntityJpaRepository
            .findByExternalId(eq(item.getExternalParticipantId())))
            .thenReturn(Optional.of(assignedLegalEntity));
        when(serviceAgreementJpaRepository
            .findByExternalId(item.getExternalServiceAgreementId()))
            .thenReturn(Optional.of(serviceAgreement));
        assertEquals("1", persistenceServiceAgreementService.addParticipant(item));

        verify(participantJpaRepository).save(participantCaptor.capture());
        com.backbase.accesscontrol.domain.Participant agreement = participantCaptor.getValue();
        assertThat(
            agreement.getLegalEntity(),
            is(assignedLegalEntity)
        );
    }

    @Test
    public void shouldThrowBadRequestWhenRemovingParticipantsOnMasterServiceAgreement() {
        ServiceAgreement serviceAgreement = getServiceAgreements().get(2);
        LegalEntity creatorLegalEntity = serviceAgreement.getCreatorLegalEntity();
        serviceAgreement.setCreatorLegalEntity(creatorLegalEntity);

        com.backbase.accesscontrol.domain.Participant participant = new
            com.backbase.accesscontrol.domain.Participant();
        participant.setShareAccounts(true);
        participant.setShareUsers(true);
        participant.setId("externalParticipantId");
        participant.setServiceAgreement(serviceAgreement);
        participant.setLegalEntity(creatorLegalEntity);

        when(participantJpaRepository.findByServiceAgreementExternalIdAndLegalEntityExternalId(
            eq(participant.getServiceAgreement()
                .getExternalId()), eq("ex-id")))
            .thenReturn(Optional.of(participant));

        PresentationParticipantPutBody participantPutBody = new PresentationParticipantPutBody()
            .withExternalParticipantId(creatorLegalEntity.getExternalId())
            .withAction(PresentationAction.REMOVE)
            .withExternalServiceAgreementId(serviceAgreement.getExternalId());

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> persistenceServiceAgreementService.removeParticipant(participantPutBody));
        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_056.getErrorMessage(), ERR_ACC_056.getErrorCode()));
    }

    @Test
    public void shouldThrowExceptionWhenInvalidHierarchy() {
        PresentationParticipantPutBody item = new PresentationParticipantPutBody()
            .withExternalServiceAgreementId("exSAId")
            .withExternalParticipantId("exLEId")
            .withSharingAccounts(true)
            .withSharingUsers(true);

        LegalEntity assignedLegalEntity = LegalEntityUtil.createLegalEntity("assinged", "name-assinged",
            item.getExternalParticipantId(),
            null, LegalEntityType.BANK);

        ServiceAgreement serviceAgreement = getServiceAgreements().get(0);
        LegalEntity creatorLegalEntity = serviceAgreement.getCreatorLegalEntity();

        com.backbase.accesscontrol.domain.Participant consumer =
            new com.backbase.accesscontrol.domain.Participant();
        consumer.setShareAccounts(true);
        consumer.setServiceAgreement(serviceAgreement);
        consumer.setLegalEntity(creatorLegalEntity);

        com.backbase.accesscontrol.domain.Participant provider =
            new com.backbase.accesscontrol.domain.Participant();
        provider.setShareUsers(true);
        provider.setServiceAgreement(serviceAgreement);
        provider.setLegalEntity(creatorLegalEntity);

        when(serviceAgreementJpaRepository
            .findByExternalId(item.getExternalServiceAgreementId()))
            .thenReturn(Optional.of(serviceAgreement));
        when(participantJpaRepository.findByServiceAgreementExternalIdAndServiceAgreementIsMasterFalse(eq(item
            .getExternalServiceAgreementId())))
            .thenReturn(Lists.newArrayList(consumer, provider));

        when(legalEntityJpaRepository
            .findByExternalId(eq(item.getExternalParticipantId())))
            .thenReturn(Optional.of(assignedLegalEntity));

        when(legalEntityJpaRepository.findByLegalEntityAncestorsIdAndIdIn(
            Mockito.any(String.class),
            Mockito.any(List.class)))
            .thenReturn(new ArrayList());

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> persistenceServiceAgreementService.addParticipant(item));
        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_046.getErrorMessage(), ERR_ACC_046.getErrorCode()));
    }

    @Test
    public void shouldThrowExceptionWhenInvalidLegalEntity() {
        PresentationParticipantPutBody item = new PresentationParticipantPutBody()
            .withExternalServiceAgreementId("exSAId")
            .withExternalParticipantId("exLEId")
            .withSharingAccounts(true)
            .withSharingUsers(true);

        ServiceAgreement serviceAgreement = getServiceAgreements().get(0);

        com.backbase.accesscontrol.domain.Participant consumer =
            new com.backbase.accesscontrol.domain.Participant();
        consumer.setShareAccounts(true);
        consumer.setServiceAgreement(serviceAgreement);
        consumer.setLegalEntity(serviceAgreement.getCreatorLegalEntity());

        com.backbase.accesscontrol.domain.Participant provider =
            new com.backbase.accesscontrol.domain.Participant();
        provider.setShareUsers(true);
        provider.setServiceAgreement(serviceAgreement);
        provider.setLegalEntity(serviceAgreement.getCreatorLegalEntity());

        when(participantJpaRepository.findByServiceAgreementExternalIdAndServiceAgreementIsMasterFalse(eq(item
            .getExternalServiceAgreementId())))
            .thenReturn(Lists.newArrayList(consumer, provider));

        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> persistenceServiceAgreementService.addParticipant(item));
        assertThat(exception, new NotFoundErrorMatcher(ERR_ACC_010.getErrorMessage(), ERR_ACC_010.getErrorCode()));
    }

    @Test
    public void shouldThrowExceptionWhenAlreadyExposed() {
        PresentationParticipantPutBody item = new PresentationParticipantPutBody()
            .withExternalServiceAgreementId("exSAId")
            .withExternalParticipantId("exLEId")
            .withSharingAccounts(true)
            .withSharingUsers(true);

        ServiceAgreement serviceAgreement = getServiceAgreements().get(0);

        LegalEntity creatorLegalEntity = serviceAgreement.getCreatorLegalEntity();
        creatorLegalEntity.setExternalId(item.getExternalParticipantId());
        com.backbase.accesscontrol.domain.Participant consumer =
            new com.backbase.accesscontrol.domain.Participant();
        consumer.setShareAccounts(true);
        consumer.setServiceAgreement(serviceAgreement);
        consumer.setLegalEntity(creatorLegalEntity);

        com.backbase.accesscontrol.domain.Participant provider =
            new com.backbase.accesscontrol.domain.Participant();
        provider.setShareUsers(true);
        provider.setServiceAgreement(serviceAgreement);
        provider.setLegalEntity(creatorLegalEntity);

        when(participantJpaRepository.findByServiceAgreementExternalIdAndServiceAgreementIsMasterFalse(eq(item
            .getExternalServiceAgreementId())))
            .thenReturn(Lists.newArrayList(consumer, provider));

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> persistenceServiceAgreementService.addParticipant(item));
        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_045.getErrorMessage(), ERR_ACC_045.getErrorCode()));
    }

    @Test
    public void shouldThrowExceptionWhenSharingNothing() {
        PresentationParticipantPutBody item = new PresentationParticipantPutBody()
            .withExternalServiceAgreementId("exSAId")
            .withExternalParticipantId("exLEId")
            .withSharingAccounts(false)
            .withSharingUsers(false);

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> persistenceServiceAgreementService.addParticipant(item));
        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_043.getErrorMessage(), ERR_ACC_043.getErrorCode()));
    }

    @Test
    public void shouldThrowNotFoundForNotExistingServiceAgreement() {
        String saId = "sa1";
        List<Participant> participants = getParticipants(saId);
        ServiceAgreementSave serviceAgreementSave = getServiceAgreementSaveMock(saId, newHashSet(participants));
        when(serviceAgreementJpaRepository.findById(saId, SERVICE_AGREEMENT_EXTENDED_WITH_FUNCTION_GROUPS))
            .thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> persistenceServiceAgreementService.save(saId, serviceAgreementSave));
        assertThat(exception, new NotFoundErrorMatcher(ERR_ACC_029.getErrorMessage(), ERR_ACC_029.getErrorCode()));
    }

    @Test
    public void shouldThrowBadRequestOnDifferentIsMasterValue() {
        String saId = "sa1";
        List<Participant> participants = getParticipants(saId);
        ServiceAgreementSave serviceAgreementSave = getServiceAgreementSaveMock(saId, newHashSet(participants));
        ServiceAgreement serviceAgreementMock = createServiceAgreementMock(saId);
        serviceAgreementMock.setMaster(true);
        when(serviceAgreementJpaRepository.findById(saId, SERVICE_AGREEMENT_EXTENDED_WITH_FUNCTION_GROUPS))
            .thenReturn(Optional.of(serviceAgreementMock));

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> persistenceServiceAgreementService.save(saId, serviceAgreementSave));
        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_068.getErrorMessage(), ERR_ACC_068.getErrorCode()));
    }


    @Test
    public void shouldFailOnAddedParticipantForMasterServiceAgreement() {
        String saId = "sa1";
        List<Participant> participants = getMasterServiceAgreementParticipants(saId, true, true);
        participants.addAll(getMasterServiceAgreementParticipants("otherParticipant", true, true));

        ServiceAgreementSave serviceAgreementSave = getServiceAgreementSaveMock(
            saId, newHashSet(participants))
            .withIsMaster(true);

        ServiceAgreement serviceAgreementMock = createMasterServiceAgreementMock(saId);
        when(serviceAgreementJpaRepository.findById(saId, SERVICE_AGREEMENT_EXTENDED_WITH_FUNCTION_GROUPS))
            .thenReturn(Optional.of(serviceAgreementMock));

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> persistenceServiceAgreementService.save(saId, serviceAgreementSave));
        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_069.getErrorMessage(), ERR_ACC_069.getErrorCode()));
    }


    @Test
    public void shouldFailOnDisablingMasterServiceAgreementForRoot() {
        String saId = "sa1";
        List<Participant> participants = getMasterServiceAgreementParticipants(saId, true, true);

        ServiceAgreementSave serviceAgreementSave = getServiceAgreementSaveMock(
            saId, newHashSet(participants))
            .withIsMaster(true)
            .withStatus(
                com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.Status.DISABLED);

        ServiceAgreement serviceAgreementMock = createMasterServiceAgreementMock(saId);
        when(serviceAgreementJpaRepository.findById(saId,
            GraphConstants.SERVICE_AGREEMENT_EXTENDED_WITH_FUNCTION_GROUPS))
            .thenReturn(Optional.of(serviceAgreementMock));

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> persistenceServiceAgreementService.save(saId, serviceAgreementSave));
        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_070.getErrorMessage(), ERR_ACC_070.getErrorCode()));
    }

    @Test
    public void shouldThrowBadRequestForExistingServiceAgreementWithExternalId() {
        String saId = "sa1";
        String extId = "extId2";
        List<Participant> participants = getParticipants(saId);
        ServiceAgreementSave serviceAgreementSave = getServiceAgreementSaveMock(saId, newHashSet(participants));
        serviceAgreementSave.setExternalId(extId);
        ServiceAgreement serviceAgreementMock = createServiceAgreementMock(saId);
        when(serviceAgreementJpaRepository.findById(saId, SERVICE_AGREEMENT_EXTENDED_WITH_FUNCTION_GROUPS))
            .thenReturn(Optional.of(serviceAgreementMock));
        when(serviceAgreementJpaRepository.existsByExternalId(extId)).thenReturn(true);

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> persistenceServiceAgreementService.save(saId, serviceAgreementSave));
        assertThat(exception, new BadRequestErrorMatcher(ERR_ACQ_036.getErrorMessage(), ERR_ACQ_036.getErrorCode()));
    }

    @Test
    public void shouldPassForForRoleChangeParticipantShareUsers() {
        String saId = "sa1";
        List<Participant> participants = getParticipants(saId);
        participants.get(1).setSharingUsers(false);
        ServiceAgreementSave serviceAgreementSave = getServiceAgreementSaveMock(saId, newHashSet(participants));
        ServiceAgreement serviceAgreementMock = createServiceAgreementMock(saId);

        when(serviceAgreementJpaRepository.findById(anyString(), eq(SERVICE_AGREEMENT_EXTENDED_WITH_FUNCTION_GROUPS)))
            .thenReturn(Optional.of(serviceAgreementMock));
        when(applicationProperties.getApproval().getValidation().isEnabled()).thenReturn(false);

        persistenceServiceAgreementService.save(saId, serviceAgreementSave);

        verify(serviceAgreementJpaRepository, times(1))
            .findById(eq(saId), eq(SERVICE_AGREEMENT_EXTENDED_WITH_FUNCTION_GROUPS));
        verify(approvalDataGroupDetailsJpaRepository, times(0))
            .findAllByServiceAgreementIdAndType(anyString(), anyString());
    }

    @Test
    public void shouldPassForUnblockingParticipantShareAccounts() {
        String saId = "sa1";
        List<Participant> participants = getParticipants(saId);
        participants.get(0).setSharingAccounts(false);

        ServiceAgreementSave serviceAgreementSave = getServiceAgreementSaveMock(saId, newHashSet(participants));
        ServiceAgreement serviceAgreementMock = createServiceAgreementMock(saId);

        when(serviceAgreementJpaRepository.findById(anyString(), eq(SERVICE_AGREEMENT_EXTENDED_WITH_FUNCTION_GROUPS)))
            .thenReturn(Optional.of(serviceAgreementMock));
        when(applicationProperties.getApproval().getValidation().isEnabled()).thenReturn(false);

        persistenceServiceAgreementService.save(saId, serviceAgreementSave);

        verify(serviceAgreementJpaRepository, times(1))
            .findById(eq(saId), eq(SERVICE_AGREEMENT_EXTENDED_WITH_FUNCTION_GROUPS));
        verify(approvalDataGroupDetailsJpaRepository, times(0))
            .findAllByServiceAgreementIdAndType(anyString(), anyString());
    }

    @Test
    public void shouldPassForCheckingParticipantShareUsers() {
        String saId = "sa1";
        List<Participant> participants = getParticipants(saId);
        participants.get(1).setSharingAccounts(true);

        ServiceAgreementSave serviceAgreementSave = getServiceAgreementSaveMock(saId, newHashSet(participants));
        ServiceAgreement serviceAgreementMock = createServiceAgreementMock(saId);

        when(serviceAgreementJpaRepository.findById(anyString(), eq(SERVICE_AGREEMENT_EXTENDED_WITH_FUNCTION_GROUPS)))
            .thenReturn(Optional.of(serviceAgreementMock));
        when(applicationProperties.getApproval().getValidation().isEnabled()).thenReturn(false);
        verify(approvalDataGroupDetailsJpaRepository, times(0))
            .findAllByServiceAgreementIdAndType(anyString(), anyString());

        persistenceServiceAgreementService.save(saId, serviceAgreementSave);

        verify(serviceAgreementJpaRepository, times(1))
            .findById(eq(saId), eq(SERVICE_AGREEMENT_EXTENDED_WITH_FUNCTION_GROUPS));
    }

    @Test
    public void shouldPassWhenChangingParticipantToShareAccountsWhenNoAccountsInItsDataGroups() {
        String saId = "sa1";
        List<Participant> participants = getParticipants(saId);
        participants.get(0).setSharingUsers(true);

        ServiceAgreementSave serviceAgreementSave = getServiceAgreementSaveMock(saId, newHashSet(participants));
        ServiceAgreement serviceAgreementMock = createServiceAgreementMock(saId);

        when(serviceAgreementJpaRepository.findById(anyString(), eq(SERVICE_AGREEMENT_EXTENDED_WITH_FUNCTION_GROUPS)))
            .thenReturn(Optional.of(serviceAgreementMock));
        when(applicationProperties.getApproval().getValidation().isEnabled()).thenReturn(false);

        persistenceServiceAgreementService.save(saId, serviceAgreementSave);
        verify(serviceAgreementJpaRepository, times(1))
            .findById(eq(saId), eq(SERVICE_AGREEMENT_EXTENDED_WITH_FUNCTION_GROUPS));
        verify(approvalDataGroupDetailsJpaRepository, times(0))
            .findAllByServiceAgreementIdAndType(anyString(), anyString());
    }

    @Test
    public void shouldThrowBadRequestForIncorrectHierarchy() {
        String saId = "sa1";
        List<Participant> participants = getParticipants(saId);
        ServiceAgreementSave serviceAgreementSave = getServiceAgreementSaveMock(saId, newHashSet(participants));
        ServiceAgreement serviceAgreementMock = createServiceAgreementMock(saId);
        when(serviceAgreementJpaRepository.findById(saId, SERVICE_AGREEMENT_EXTENDED_WITH_FUNCTION_GROUPS))
            .thenReturn(Optional.of(serviceAgreementMock));
        when(applicationProperties.getApproval().getValidation().isEnabled()).thenReturn(false);
        Set<String> participantsId = participants.stream().map(
            Participant::getId)
            .collect(Collectors.toSet());
        BadRequestException badRequestException = getBadRequestException(ERR_ACQ_039.getErrorMessage(),
            ERR_ACQ_039.getErrorCode());
        doThrow(badRequestException).when(validateLegalEntityHierarchyService)
            .validateLegalEntityHierarchy(eq(serviceAgreementMock.getCreatorLegalEntity()), eq(participantsId));

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> persistenceServiceAgreementService.save(saId, serviceAgreementSave));
        assertThat(exception, new BadRequestErrorMatcher(ERR_ACQ_039.getErrorMessage(), ERR_ACQ_039.getErrorCode()));
        verify(approvalDataGroupDetailsJpaRepository, times(0))
            .findAllByServiceAgreementIdAndType(anyString(), anyString());
    }

    @Test
    public void shouldNotThrowAnyExceptionAndSaveServiceAgreement() {
        String saId = "sa1";
        List<Participant> participants = getParticipants(saId);
        ServiceAgreementSave serviceAgreementSave = getServiceAgreementSaveMock(saId, newHashSet(participants));
        serviceAgreementSave.setExternalId("new Id");
        serviceAgreementSave.setName("new Name");
        serviceAgreementSave.setDescription("new description");
        serviceAgreementSave
            .setStatus(
                com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.Status.DISABLED);
        Map<String, String> additionalProps = new HashMap<>();
        additionalProps.put("second", "12345");
        serviceAgreementSave.setAdditions(additionalProps);

        ServiceAgreement serviceAgreementMock = createServiceAgreementMock(saId);
        when(serviceAgreementJpaRepository.findById(saId, SERVICE_AGREEMENT_EXTENDED_WITH_FUNCTION_GROUPS))
            .thenReturn(Optional.of(serviceAgreementMock));
        when(serviceAgreementJpaRepository.existsByExternalId(serviceAgreementSave.getExternalId())).thenReturn(false);
        when(applicationProperties.getApproval().getValidation().isEnabled()).thenReturn(false);

        createLegalEntity("le1_" + saId, null);
        createLegalEntity("le2_" + saId, null);

        persistenceServiceAgreementService.save(saId, serviceAgreementSave);

        verify(approvalDataGroupDetailsJpaRepository, times(0))
            .findAllByServiceAgreementIdAndType(anyString(), anyString());
        verify(serviceAgreementAdminService, times(1))
            .updateAdmins(serviceAgreementMock, serviceAgreementSave.getParticipants());
        verify(serviceAgreementJpaRepository, times(1)).save(serviceAgreementMock);
        verify(serviceAgreementJpaRepository).save(serviceAgreementCaptor.capture());

        ServiceAgreement serviceAgreementNew = serviceAgreementCaptor.getValue();
        assertEquals(serviceAgreementNew.getExternalId(), serviceAgreementSave.getExternalId());
        assertEquals(serviceAgreementNew.getName(), serviceAgreementSave.getName());
        assertEquals(serviceAgreementNew.getDescription(), serviceAgreementSave.getDescription());
        assertEquals(serviceAgreementNew.getState().name(), serviceAgreementSave.getStatus().name());

        assertEquals(serviceAgreementNew.getParticipants().size(),
            serviceAgreementSave.getParticipants().size());
        assertEquals(serviceAgreementNew.getParticipants().get("le1_" + saId).isShareAccounts(),
            participants.get(0).getSharingAccounts());
        assertEquals(serviceAgreementNew.getParticipants().get("le1_" + saId).isShareUsers(),
            participants.get(0).getSharingUsers());

        assertEquals(serviceAgreementNew.getParticipants().get("le2_" + saId).isShareAccounts(),
            participants.get(1).getSharingAccounts());
        assertEquals(serviceAgreementNew.getParticipants().get("le2_" + saId).isShareUsers(),
            participants.get(1).getSharingUsers());

        assertEquals(serviceAgreementNew.getAdditions().size(), additionalProps.size());
        assertEquals(serviceAgreementNew.getAdditions().keySet(), additionalProps.keySet());
        assertEquals(serviceAgreementNew.getAdditions().get("second"), additionalProps.get("second"));
    }


    @Test
    public void shouldNotThrowAnyExceptionAndSaveServiceAgreementWhenOldExternalIdIsNull() {
        String saId = "sa1";
        List<Participant> participants = getParticipants(saId);
        ServiceAgreementSave serviceAgreementSave = getServiceAgreementSaveMock(saId, newHashSet(participants));
        serviceAgreementSave.setExternalId("new Id");
        serviceAgreementSave.setName("new Name");
        serviceAgreementSave.setDescription("new description");
        serviceAgreementSave
            .setStatus(
                com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.Status.DISABLED);

        ServiceAgreement serviceAgreementMock = createServiceAgreementMock(saId);
        serviceAgreementMock.setExternalId("old id");
        serviceAgreementMock.setId(saId);
        when(serviceAgreementJpaRepository.findById(saId,
            GraphConstants.SERVICE_AGREEMENT_EXTENDED_WITH_FUNCTION_GROUPS))
            .thenReturn(Optional.of(serviceAgreementMock));
        when(serviceAgreementJpaRepository.existsByExternalId(serviceAgreementSave.getExternalId())).thenReturn(false);
        when(applicationProperties.getApproval().getValidation().isEnabled()).thenReturn(true);
        when(approvalDataGroupDetailsJpaRepository.findAllByServiceAgreementIdAndType(anyString(), anyString()))
            .thenReturn(new ArrayList<>());

        createLegalEntity("le1_" + saId, null);
        createLegalEntity("le2_" + saId, null);

        persistenceServiceAgreementService.save(saId, serviceAgreementSave);

        verify(serviceAgreementAdminService, times(1))
            .updateAdmins(serviceAgreementMock, serviceAgreementSave.getParticipants());
        verify(serviceAgreementJpaRepository, times(1)).save(serviceAgreementMock);
        verify(serviceAgreementJpaRepository).save(serviceAgreementCaptor.capture());
        verify(approvalDataGroupDetailsJpaRepository)
            .findAllByServiceAgreementIdAndType(eq(saId), eq("ARRANGEMENTS"));

        ServiceAgreement serviceAgreementNew = serviceAgreementCaptor.getValue();
        assertEquals(serviceAgreementNew.getExternalId(), serviceAgreementSave.getExternalId());
        assertEquals(serviceAgreementNew.getName(), serviceAgreementSave.getName());
        assertEquals(serviceAgreementNew.getDescription(), serviceAgreementSave.getDescription());
        assertEquals(serviceAgreementNew.getState().name(), serviceAgreementSave.getStatus().name());

        assertEquals(serviceAgreementNew.getParticipants().size(),
            serviceAgreementSave.getParticipants().size());
        assertEquals(serviceAgreementNew.getParticipants().get("le1_" + saId).isShareAccounts(),
            participants.get(0).getSharingAccounts());
        assertEquals(serviceAgreementNew.getParticipants().get("le1_" + saId).isShareUsers(),
            participants.get(0).getSharingUsers());

        assertEquals(serviceAgreementNew.getParticipants().get("le2_" + saId).isShareAccounts(),
            participants.get(1).getSharingAccounts());
        assertEquals(serviceAgreementNew.getParticipants().get("le2_" + saId).isShareUsers(),
            participants.get(1).getSharingUsers());
    }

    @Test
    public void shouldListServiceAgreements() {
        LegalEntity creatorLegalEntity = createLegalEntity(UUID.randomUUID().toString(), null);
        LegalEntity firstChild = createLegalEntity(UUID.randomUUID().toString(), null);
        LegalEntity secondChild = createLegalEntity(UUID.randomUUID().toString(), null);
        ServiceAgreement serviceAgreement1 = getServiceAgreement(creatorLegalEntity, true);
        ServiceAgreement serviceAgreement2 = getServiceAgreement(firstChild, false);
        List<ServiceAgreement> serviceAgreements = asList(serviceAgreement1, serviceAgreement2);
        serviceAgreementsPage = new PageImpl<>(serviceAgreements);
        SearchAndPaginationParameters searchAndPaginationParameters = new SearchAndPaginationParameters(null, null,
            null, null);

        serviceAgreement1.setStartDate(new Date(0));
        serviceAgreement1.setEndDate(new Date(1));
        serviceAgreement2.setStartDate(new Date(2));
        serviceAgreement2.setEndDate(new Date(3));

        Set<LegalEntity> children = new HashSet<>();
        children.add(firstChild);
        children.add(secondChild);
        creatorLegalEntity.setChildren(children);

        when(serviceAgreementJpaRepository
            .findByCreatorIdInHierarchyAndParameters(eq(creatorLegalEntity.getId()), eq(new UserParameters(null, null)),
                eq(searchAndPaginationParameters), eq(SERVICE_AGREEMENT_WITH_PARTICIPANTS_AND_ADDITIONS)))
            .thenReturn(serviceAgreementsPage);

        Page<ServiceAgreement> persistenceServiceAgreements = persistenceServiceAgreementService
            .listServiceAgreements(creatorLegalEntity.getId(), new UserParameters(null, null),
                searchAndPaginationParameters);

        assertEquals(persistenceServiceAgreements.get().findFirst().get().getStartDate(),
            serviceAgreement1.getStartDate());
        assertEquals(persistenceServiceAgreements.get().findFirst().get()
            .getEndDate(), serviceAgreement1.getEndDate());
    }

    @Test
    public void shouldThrowNotFoundForNotExistingServiceAgreementOnUpdateServiceAgreementApproval() {
        String saId = "sa1";
        List<Participant> participants = getParticipants(saId);
        ServiceAgreementSave serviceAgreementSave = getServiceAgreementSaveMock(saId, newHashSet(participants));
        when(serviceAgreementJpaRepository.findById(saId, SERVICE_AGREEMENT_EXTENDED_WITH_FUNCTION_GROUPS))
            .thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> persistenceServiceAgreementService
            .updateServiceAgreementApproval(serviceAgreementSave, saId, "approvalId"));
        assertThat(exception, new NotFoundErrorMatcher(ERR_ACC_029.getErrorMessage(), ERR_ACC_029.getErrorCode()));
    }

    @Test
    public void shouldThrowBadRequestOnDifferentIsMasterValueOnUpdateServiceAgreementApproval() {
        String saId = "sa1";
        List<Participant> participants = getParticipants(saId);
        ServiceAgreementSave serviceAgreementSave = getServiceAgreementSaveMock(saId, newHashSet(participants));
        ServiceAgreement serviceAgreementMock = createServiceAgreementMock(saId);
        serviceAgreementMock.setMaster(true);
        when(serviceAgreementJpaRepository.findById(saId, SERVICE_AGREEMENT_EXTENDED_WITH_FUNCTION_GROUPS))
            .thenReturn(Optional.of(serviceAgreementMock));

        BadRequestException exception = assertThrows(BadRequestException.class, () -> persistenceServiceAgreementService
            .updateServiceAgreementApproval(serviceAgreementSave, saId, "approvalId"));
        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_068.getErrorMessage(), ERR_ACC_068.getErrorCode()));
    }

    @Test
    public void shouldThrowBadRequestIfServiceAgreementAlreadyHavePendingRecordOnUpdateServiceAgreementApproval() {
        String saId = "sa1";
        List<Participant> participants = getParticipants(saId);
        ServiceAgreementSave serviceAgreementSave = getServiceAgreementSaveMock(saId, newHashSet(participants));
        ServiceAgreement serviceAgreementMock = createServiceAgreementMock(saId);

        when(serviceAgreementJpaRepository.findById(saId, SERVICE_AGREEMENT_EXTENDED_WITH_FUNCTION_GROUPS))
            .thenReturn(Optional.of(serviceAgreementMock));
        when(applicationProperties.getApproval().getValidation().isEnabled()).thenReturn(false);

        when(approvalServiceAgreementRefJpaRepository.existsByServiceAgreementId(eq(saId))).thenReturn(true);

        BadRequestException exception = assertThrows(BadRequestException.class, () -> persistenceServiceAgreementService
            .updateServiceAgreementApproval(serviceAgreementSave, saId, "approvalId"));
        assertThat(exception, new BadRequestErrorMatcher(ERR_AG_105.getErrorMessage(), ERR_AG_105.getErrorCode()));
        verify(approvalDataGroupDetailsJpaRepository, times(0))
            .findAllByServiceAgreementIdAndType(anyString(), anyString());
    }

    @Test
    public void shouldThrowBadRequestWhenValidateExternalIdIsUniqueInPendingOnUpdateServiceAgreementApproval() {
        String saId = "sa1";
        List<Participant> participants = getParticipants(saId);
        ServiceAgreementSave serviceAgreementSave = getServiceAgreementSaveMock(saId, newHashSet(participants));
        ServiceAgreement serviceAgreementMock = createServiceAgreementMock(saId);
        when(serviceAgreementJpaRepository.findById(saId, SERVICE_AGREEMENT_EXTENDED_WITH_FUNCTION_GROUPS))
            .thenReturn(Optional.of(serviceAgreementMock));
        when(applicationProperties.getApproval().getValidation().isEnabled()).thenReturn(false);

        when(approvalServiceAgreementJpaRepository.existsByExternalId(eq("ext_" + saId)))
            .thenReturn(true);

        BadRequestException exception = assertThrows(BadRequestException.class, () -> persistenceServiceAgreementService
            .updateServiceAgreementApproval(serviceAgreementSave, saId, "approvalId"));
        assertThat(exception, new BadRequestErrorMatcher(ERR_AG_106.getErrorMessage(), ERR_AG_106.getErrorCode()));
    }

    @Test
    public void shouldFailOnAddedParticipantForMasterServiceAgreementOnUpdateServiceAgreementApproval() {
        String saId = "sa1";
        List<Participant> participants = getMasterServiceAgreementParticipants(saId, true, true);
        participants.addAll(getMasterServiceAgreementParticipants("otherParticipant", true, true));

        ServiceAgreementSave serviceAgreementSave = getServiceAgreementSaveMock(
            saId, newHashSet(participants))
            .withIsMaster(true);

        ServiceAgreement serviceAgreementMock = createMasterServiceAgreementMock(saId);
        when(serviceAgreementJpaRepository.findById(saId, SERVICE_AGREEMENT_EXTENDED_WITH_FUNCTION_GROUPS))
            .thenReturn(Optional.of(serviceAgreementMock));

        BadRequestException exception = assertThrows(BadRequestException.class, () -> persistenceServiceAgreementService
            .updateServiceAgreementApproval(serviceAgreementSave, saId, "approvalId"));
        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_069.getErrorMessage(), ERR_ACC_069.getErrorCode()));
        verify(approvalDataGroupDetailsJpaRepository, times(0))
            .findAllByServiceAgreementIdAndType(anyString(), anyString());
    }


    @Test
    public void shouldFailOnDisablingMasterServiceAgreementForRootOnUpdateServiceAgreementApproval() {
        String saId = "sa1";
        List<Participant> participants = getMasterServiceAgreementParticipants(saId, true, true);

        ServiceAgreementSave serviceAgreementSave = getServiceAgreementSaveMock(
            saId, newHashSet(participants))
            .withIsMaster(true)
            .withStatus(
                com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.Status.DISABLED);

        ServiceAgreement serviceAgreementMock = createMasterServiceAgreementMock(saId);
        when(serviceAgreementJpaRepository.findById(saId,
            GraphConstants.SERVICE_AGREEMENT_EXTENDED_WITH_FUNCTION_GROUPS))
            .thenReturn(Optional.of(serviceAgreementMock));

        BadRequestException exception = assertThrows(BadRequestException.class, () -> persistenceServiceAgreementService
            .updateServiceAgreementApproval(serviceAgreementSave, saId, "approvalId"));
        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_070.getErrorMessage(), ERR_ACC_070.getErrorCode()));
    }

    @Test
    public void shouldThrowBadRequestForExistingServiceAgreementWithExternalIdOnUpdateServiceAgreementApproval() {
        String saId = "sa1";
        String extId = "extId2";
        List<Participant> participants = getParticipants(saId);
        ServiceAgreementSave serviceAgreementSave = getServiceAgreementSaveMock(saId, newHashSet(participants));
        serviceAgreementSave.setExternalId(extId);
        ServiceAgreement serviceAgreementMock = createServiceAgreementMock(saId);
        when(serviceAgreementJpaRepository.findById(saId, SERVICE_AGREEMENT_EXTENDED_WITH_FUNCTION_GROUPS))
            .thenReturn(Optional.of(serviceAgreementMock));
        when(serviceAgreementJpaRepository.existsByExternalId(extId)).thenReturn(true);

        BadRequestException exception = assertThrows(BadRequestException.class, () -> persistenceServiceAgreementService
            .updateServiceAgreementApproval(serviceAgreementSave, saId, "approvalId"));
        assertThat(exception, new BadRequestErrorMatcher(ERR_ACQ_036.getErrorMessage(), ERR_ACQ_036.getErrorCode()));
    }

    @Test
    public void shouldSuccessfullySaveApprovalServiceAgreementOnUpdateServiceAgreementApproval() {
        String saId = "sa1";
        List<Participant> participants = getParticipants(saId);
        ServiceAgreementSave serviceAgreementSave = getServiceAgreementSaveMock(saId, newHashSet(participants));
        serviceAgreementSave.setExternalId("new Id");
        serviceAgreementSave.setName("new Name");
        serviceAgreementSave.setDescription("new description");
        serviceAgreementSave
            .setStatus(
                com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.Status.DISABLED);
        Map<String, String> additionalProps = new HashMap<>();
        additionalProps.put("second", "12345");
        serviceAgreementSave.setAdditions(additionalProps);

        ServiceAgreement serviceAgreementMock = createServiceAgreementMock(saId);
        serviceAgreementMock.setId(saId);

        when(serviceAgreementJpaRepository.findById(saId, SERVICE_AGREEMENT_WITH_PERMISSION_SETS))
            .thenReturn(Optional.of(serviceAgreementMock));

        when(serviceAgreementJpaRepository.findById(saId, SERVICE_AGREEMENT_EXTENDED_WITH_FUNCTION_GROUPS))
            .thenReturn(Optional.of(serviceAgreementMock));
        when(serviceAgreementJpaRepository.existsByExternalId(serviceAgreementSave.getExternalId())).thenReturn(false);
        when(applicationProperties.getApproval().getValidation().isEnabled()).thenReturn(false);

        createLegalEntity("le1_" + saId, null);
        createLegalEntity("le2_" + saId, null);

        persistenceServiceAgreementService.updateServiceAgreementApproval(serviceAgreementSave, saId, "approvalId");

        verify(approvalDataGroupDetailsJpaRepository, times(0))
            .findAllByServiceAgreementIdAndType(anyString(), anyString());

        ArgumentCaptor<ApprovalServiceAgreement> captor = ArgumentCaptor.forClass(ApprovalServiceAgreement.class);
        verify(approvalServiceAgreementJpaRepository).save(captor.capture());

        ApprovalServiceAgreement approvalServiceAgreement = captor.getValue();
        assertEquals(approvalServiceAgreement.getExternalId(), serviceAgreementSave.getExternalId());
        assertEquals(approvalServiceAgreement.getName(), serviceAgreementSave.getName());
        assertEquals(approvalServiceAgreement.getDescription(), serviceAgreementSave.getDescription());
        assertEquals(approvalServiceAgreement.getState().name(), serviceAgreementSave.getStatus().name());

        assertEquals(approvalServiceAgreement.getParticipants().size(), serviceAgreementSave.getParticipants().size());

        List<ApprovalServiceAgreementParticipant> approvalParticipants = new ArrayList<>(
            approvalServiceAgreement.getParticipants());
        assertEquals(approvalParticipants.get(0).isShareAccounts(), participants.get(0).getSharingAccounts());
        assertEquals(approvalParticipants.get(0).isShareUsers(), participants.get(0).getSharingUsers());

        assertEquals(approvalParticipants.get(1).isShareAccounts(), participants.get(1).getSharingAccounts());
        assertEquals(approvalParticipants.get(1).isShareUsers(), participants.get(1).getSharingUsers());

        assertEquals(approvalServiceAgreement.getAdditions().size(), additionalProps.size());
        assertEquals(approvalServiceAgreement.getAdditions().keySet(), additionalProps.keySet());
        assertEquals(approvalServiceAgreement.getAdditions().get("second"), additionalProps.get("second"));
    }

    @Test
    public void shouldCreateServiceAgreement() {
        com.backbase.accesscontrol.domain.Participant participantWithAdmin
            = createParticipantWithAdmin("a1", true, true);
        LegalEntity legalEntity = createLegalEntity("1", "e1");
        participantWithAdmin.setLegalEntity(legalEntity);

        ServiceAgreement serviceAgreement = new ServiceAgreement();
        serviceAgreement.setDescription("desc");
        serviceAgreement.setCreatorLegalEntity(legalEntity);
        serviceAgreement.setExternalId("new-ex-id");
        serviceAgreement.setMaster(true);
        serviceAgreement.setName("new name");
        serviceAgreement.addParticipant(Lists.newArrayList(participantWithAdmin));

        ServiceAgreement saved = new ServiceAgreement();
        saved.setId("saved");
        when(serviceAgreementJpaRepository.save(serviceAgreement))
            .thenReturn(saved);
        ServiceAgreement created = persistenceServiceAgreementService.create(serviceAgreement);
        assertEquals(saved, created);
        verify(serviceAgreementAdminService).addAdminPermissions(saved);
    }

    @Test
    public void shouldFailOnInvalidDateRange() {
        com.backbase.accesscontrol.domain.Participant participantWithAdmin
            = createParticipantWithAdmin("a1", true, true);
        LegalEntity legalEntity = createLegalEntity("1", "e1");
        participantWithAdmin.setLegalEntity(legalEntity);

        ServiceAgreement serviceAgreement = new ServiceAgreement();
        serviceAgreement.setDescription("desc");
        serviceAgreement.setCreatorLegalEntity(legalEntity);
        serviceAgreement.setExternalId("new-ex-id");
        serviceAgreement.setMaster(true);
        serviceAgreement.setName("new name");
        serviceAgreement.setStartDate(new Date(2));
        serviceAgreement.setEndDate(new Date(1));
        serviceAgreement.addParticipant(Lists.newArrayList(participantWithAdmin));

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> persistenceServiceAgreementService.create(serviceAgreement));
        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_077.getErrorMessage(), ERR_ACC_077.getErrorCode()));
    }

    @Test
    public void shouldFailOnDuplicatedExternalId() {
        com.backbase.accesscontrol.domain.Participant participantWithAdmin
            = createParticipantWithAdmin("a1", true, true);
        LegalEntity legalEntity = createLegalEntity("1", "e1");
        participantWithAdmin.setLegalEntity(legalEntity);

        ServiceAgreement serviceAgreement = new ServiceAgreement();
        serviceAgreement.setDescription("desc");
        serviceAgreement.setCreatorLegalEntity(legalEntity);
        serviceAgreement.setExternalId("new-ex-id");
        serviceAgreement.setMaster(true);
        serviceAgreement.setName("new name");
        serviceAgreement.addParticipant(Lists.newArrayList(participantWithAdmin));

        when(serviceAgreementJpaRepository.existsByExternalId(serviceAgreement.getExternalId()))
            .thenReturn(true);

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> persistenceServiceAgreementService.create(serviceAgreement));
        assertThat(exception, new BadRequestErrorMatcher(ERR_ACQ_036.getErrorMessage(), ERR_ACQ_036.getErrorCode()));
    }

    @Test
    public void shouldFailOnExistingMasterServiceAgreement() {
        com.backbase.accesscontrol.domain.Participant participantWithAdmin
            = createParticipantWithAdmin("a1", true, true);
        LegalEntity legalEntity = createLegalEntity("1", "e1");
        participantWithAdmin.setLegalEntity(legalEntity);

        ServiceAgreement serviceAgreement = new ServiceAgreement();
        serviceAgreement.setDescription("desc");
        serviceAgreement.setCreatorLegalEntity(legalEntity);
        serviceAgreement.setExternalId("new-ex-id");
        serviceAgreement.setMaster(true);
        serviceAgreement.setName("new name");
        serviceAgreement.addParticipant(Lists.newArrayList(participantWithAdmin));

        when(serviceAgreementJpaRepository.existsByCreatorLegalEntityIdAndIsMasterTrue(legalEntity.getId()))
            .thenReturn(true);

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> persistenceServiceAgreementService.create(serviceAgreement));
        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_066.getErrorMessage(), ERR_ACC_066.getErrorCode()));
    }

    @Test
    public void shouldFailOnMasterWithUsers() {
        com.backbase.accesscontrol.domain.Participant participantWithAdmin
            = createParticipantWithAdmin("a1", true, true);
        LegalEntity legalEntity = createLegalEntity("1", "e1");
        participantWithAdmin.setLegalEntity(legalEntity);
        participantWithAdmin.setParticipantUsers(newHashSet(new ParticipantUser()));
        ServiceAgreement serviceAgreement = new ServiceAgreement();
        serviceAgreement.setDescription("desc");
        serviceAgreement.setCreatorLegalEntity(legalEntity);
        serviceAgreement.setExternalId("new-ex-id");
        serviceAgreement.setMaster(true);
        serviceAgreement.setName("new name");
        serviceAgreement.addParticipant(Lists.newArrayList(participantWithAdmin));

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> persistenceServiceAgreementService.create(serviceAgreement));
        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_063.getErrorMessage(), ERR_ACC_063.getErrorCode()));
    }

    @Test
    public void shouldFailWhenMasterDontShareUsers() {
        com.backbase.accesscontrol.domain.Participant participantWithAdmin
            = createParticipantWithAdmin("a1", false, true);
        LegalEntity legalEntity = createLegalEntity("1", "e1");
        participantWithAdmin.setLegalEntity(legalEntity);
        ServiceAgreement serviceAgreement = new ServiceAgreement();
        serviceAgreement.setDescription("desc");
        serviceAgreement.setCreatorLegalEntity(legalEntity);
        serviceAgreement.setExternalId("new-ex-id");
        serviceAgreement.setMaster(true);
        serviceAgreement.setName("new name");
        serviceAgreement.addParticipant(
            Lists.newArrayList(
                participantWithAdmin
            )
        );

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> persistenceServiceAgreementService.create(serviceAgreement));
        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_065.getErrorMessage(), ERR_ACC_065.getErrorCode()));
    }

    @Test
    public void shouldFailWhenMasterDontShareAccounts() {
        com.backbase.accesscontrol.domain.Participant participantWithAdmin
            = createParticipantWithAdmin("a1", true, false);
        LegalEntity legalEntity = createLegalEntity("1", "e1");
        participantWithAdmin.setLegalEntity(legalEntity);
        participantWithAdmin.setParticipantUsers(newHashSet(
            new ParticipantUser(participantWithAdmin, "u1")
        ));
        ServiceAgreement serviceAgreement = new ServiceAgreement();
        serviceAgreement.setDescription("desc");
        serviceAgreement.setCreatorLegalEntity(legalEntity);
        serviceAgreement.setExternalId("new-ex-id");
        serviceAgreement.setMaster(true);
        serviceAgreement.setName("new name");
        serviceAgreement.addParticipant(
            Lists.newArrayList(
                participantWithAdmin
            )
        );

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> persistenceServiceAgreementService.create(serviceAgreement));
        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_065.getErrorMessage(), ERR_ACC_065.getErrorCode()));
    }

    @Test
    public void shouldFailWhenMasterHaveMoreThanOneParticipant() {
        LegalEntity legalEntity = createLegalEntity("1", "e1");
        com.backbase.accesscontrol.domain.Participant participantWithAdmin
            = createParticipantWithAdmin("a1", true, false);
        participantWithAdmin.setLegalEntity(legalEntity);

        LegalEntity legalEntity2 = createLegalEntity("2", "e2");
        com.backbase.accesscontrol.domain.Participant participantWithAdmin2
            = createParticipantWithAdmin("a2", true, false);
        participantWithAdmin2.setLegalEntity(legalEntity2);

        ServiceAgreement serviceAgreement = new ServiceAgreement();
        serviceAgreement.setDescription("desc");
        serviceAgreement.setCreatorLegalEntity(legalEntity);
        serviceAgreement.setExternalId("new-ex-id");
        serviceAgreement.setMaster(true);
        serviceAgreement.setName("new name");
        serviceAgreement.addParticipant(
            Lists.newArrayList(
                participantWithAdmin,
                participantWithAdmin2
            )
        );

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> persistenceServiceAgreementService.create(serviceAgreement));
        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_064.getErrorMessage(), ERR_ACC_064.getErrorCode()));
    }

    @Test
    public void shouldFailWhenMasterHaveLessThanOneParticipant() {
        LegalEntity legalEntity = createLegalEntity("1", "e1");

        ServiceAgreement serviceAgreement = new ServiceAgreement();
        serviceAgreement.setDescription("desc");
        serviceAgreement.setCreatorLegalEntity(legalEntity);
        serviceAgreement.setExternalId("new-ex-id");
        serviceAgreement.setMaster(true);
        serviceAgreement.setName("new name");

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> persistenceServiceAgreementService.create(serviceAgreement));
        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_064.getErrorMessage(), ERR_ACC_064.getErrorCode()));
    }

    @Test
    public void shouldFailWhenHaveParticipantThatDontShareAnyting() {
        LegalEntity legalEntity = createLegalEntity("1", "e1");
        com.backbase.accesscontrol.domain.Participant participantWithAdmin
            = createParticipantWithAdmin("a1", false, false);
        participantWithAdmin.setLegalEntity(legalEntity);

        ServiceAgreement serviceAgreement = new ServiceAgreement();
        serviceAgreement.setDescription("desc");
        serviceAgreement.setCreatorLegalEntity(legalEntity);
        serviceAgreement.setExternalId("new-ex-id");
        serviceAgreement.setMaster(true);
        serviceAgreement.setName("new name");
        serviceAgreement.addParticipant(
            Lists.newArrayList(
                participantWithAdmin
            )
        );

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> persistenceServiceAgreementService.create(serviceAgreement));
        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_043.getErrorMessage(), ERR_ACC_043.getErrorCode()));
    }

    @Test
    public void shouldFailWhenParticipantDontShareUsersButHaveThem() {
        LegalEntity legalEntity = createLegalEntity("1", "e1");
        com.backbase.accesscontrol.domain.Participant participantWithAdmin
            = createParticipantWithAdmin("a1", false, true);
        participantWithAdmin.setLegalEntity(legalEntity);
        participantWithAdmin.setParticipantUsers(newHashSet(new ParticipantUser()));
        ServiceAgreement serviceAgreement = new ServiceAgreement();
        serviceAgreement.setDescription("desc");
        serviceAgreement.setCreatorLegalEntity(legalEntity);
        serviceAgreement.setExternalId("new-ex-id");
        serviceAgreement.setMaster(true);
        serviceAgreement.setName("new name");
        serviceAgreement.addParticipant(
            Lists.newArrayList(
                participantWithAdmin
            )
        );

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> persistenceServiceAgreementService.create(serviceAgreement));
        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_063.getErrorMessage(), ERR_ACC_063.getErrorCode()));
    }

    @Test
    public void shouldDoNothingWhenNoPendingApprovalsForGivenUsersAndServiceAgreement() {
        String serviceAgreementId = "SA-01";
        HashSet<String> userIds = newHashSet("U-01", "U-02");
        when(approvalUserContextJpaRepository.countByServiceAgreementIdAndUserIdIn(serviceAgreementId,
            userIds))
            .thenReturn(0L);

        approvalUserContextJpaRepository.countByServiceAgreementIdAndUserIdIn(serviceAgreementId, userIds);

        verify(approvalUserContextJpaRepository).countByServiceAgreementIdAndUserIdIn(serviceAgreementId, userIds);
    }

    @Test
    public void shouldDeleteServiceAgreementByExternalId() {
        LegalEntity legalEntity = createLegalEntity("1", "e1");
        LegalEntity parentLe = new LegalEntity();
        legalEntity.setParent(parentLe);
        com.backbase.accesscontrol.domain.Participant participantWithAdmin
            = createParticipantWithAdmin("a1", false, true);
        participantWithAdmin.setLegalEntity(legalEntity);
        participantWithAdmin.setParticipantUsers(newHashSet(new ParticipantUser()));
        ServiceAgreement serviceAgreement = new ServiceAgreement();
        serviceAgreement.setId("serviceAgreementId");
        serviceAgreement.setDescription("desc");
        serviceAgreement.setCreatorLegalEntity(legalEntity);
        serviceAgreement.setExternalId("new-ex-id");
        serviceAgreement.setMaster(true);
        serviceAgreement.setName("new name");
        serviceAgreement.addParticipant(
            Lists.newArrayList(
                participantWithAdmin
            )
        );
        when(serviceAgreementJpaRepository.findByExternalId(eq(serviceAgreement.getExternalId()),
            eq(SERVICE_AGREEMENT_WITH_PARTICIPANTS_AND_CREATOR))).thenReturn(Optional.of(serviceAgreement));

        PresentationServiceAgreementIdentifier serviceAgreementIdentifier =
            new PresentationServiceAgreementIdentifier()
                .withExternalIdIdentifier(serviceAgreement.getExternalId());
        when(dataGroupJpaRepository.findByServiceAgreementId("serviceAgreementId"))
            .thenReturn(new ArrayList<>());
        when(approvalDataGroupJpaRepository
            .existsByDataGroupIdIn(new HashSet<>())).thenReturn(false);
        when(functionGroupJpaRepository.findByServiceAgreementId("serviceAgreementId"))
            .thenReturn(new ArrayList<>());
        when(approvalFunctionGroupRefJpaRepository.existsByFunctionGroupIdIn(new HashSet<>())).thenReturn(false);
        persistenceServiceAgreementService.deleteServiceAgreementByIdentifier(serviceAgreementIdentifier);
        verify(serviceAgreementJpaRepository)
            .findByExternalId(serviceAgreement.getExternalId(), SERVICE_AGREEMENT_WITH_PARTICIPANTS_AND_CREATOR);
    }

    @Test
    public void shouldThrowNotFoundWhenDeleteServiceAgreementByExternalId() {
        LegalEntity legalEntity = createLegalEntity("1", "e1");
        LegalEntity parentLe = new LegalEntity();
        legalEntity.setParent(parentLe);
        com.backbase.accesscontrol.domain.Participant participantWithAdmin
            = createParticipantWithAdmin("a1", false, true);
        participantWithAdmin.setLegalEntity(legalEntity);
        participantWithAdmin.setParticipantUsers(newHashSet(new ParticipantUser()));
        ServiceAgreement serviceAgreement = new ServiceAgreement();
        serviceAgreement.setDescription("desc");
        serviceAgreement.setCreatorLegalEntity(legalEntity);
        serviceAgreement.setExternalId("new-ex-id");
        serviceAgreement.setMaster(true);
        serviceAgreement.setName("new name");
        serviceAgreement.addParticipant(
            Lists.newArrayList(
                participantWithAdmin
            )
        );
        PresentationServiceAgreementIdentifier serviceAgreementIdentifier =
            new PresentationServiceAgreementIdentifier()
                .withExternalIdIdentifier(serviceAgreement.getExternalId());
        when(serviceAgreementJpaRepository.findByExternalId(eq(serviceAgreement.getExternalId()),
            eq(SERVICE_AGREEMENT_WITH_PARTICIPANTS_AND_CREATOR))).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> persistenceServiceAgreementService.deleteServiceAgreementByIdentifier(serviceAgreementIdentifier));

        verify(serviceAgreementJpaRepository)
            .findByExternalId(serviceAgreement.getExternalId(), SERVICE_AGREEMENT_WITH_PARTICIPANTS_AND_CREATOR);
        assertThat(exception, new NotFoundErrorMatcher(ERR_ACQ_006.getErrorMessage(), ERR_ACQ_006.getErrorCode()));
    }

    @Test
    public void shouldDeleteServiceAgreementByInternalId() {
        LegalEntity legalEntity = createLegalEntity("1", "e1");
        LegalEntity parentLe = new LegalEntity();
        legalEntity.setParent(parentLe);
        com.backbase.accesscontrol.domain.Participant participantWithAdmin
            = createParticipantWithAdmin("a1", false, true);
        participantWithAdmin.setLegalEntity(legalEntity);
        participantWithAdmin.setParticipantUsers(newHashSet(new ParticipantUser()));
        ServiceAgreement serviceAgreement = new ServiceAgreement();
        serviceAgreement.setId("540949a03a7846abb69e7c667bc73688");
        serviceAgreement.setDescription("desc");
        serviceAgreement.setCreatorLegalEntity(legalEntity);
        serviceAgreement.setExternalId("new-ex-id");
        serviceAgreement.setMaster(true);
        serviceAgreement.setName("new name");
        serviceAgreement.addParticipant(
            Lists.newArrayList(
                participantWithAdmin
            )
        );
        when(serviceAgreementJpaRepository.findById(eq(serviceAgreement.getId()),
            eq(SERVICE_AGREEMENT_WITH_PARTICIPANTS_AND_CREATOR))).thenReturn(Optional.of(serviceAgreement));

        PresentationServiceAgreementIdentifier serviceAgreementIdentifier =
            new PresentationServiceAgreementIdentifier()
                .withIdIdentifier(serviceAgreement.getId());
        persistenceServiceAgreementService.deleteServiceAgreementByIdentifier(serviceAgreementIdentifier);
        verify(serviceAgreementJpaRepository)
            .findById(serviceAgreement.getId(), SERVICE_AGREEMENT_WITH_PARTICIPANTS_AND_CREATOR);
    }

    @Test
    public void shouldThrowNotFoundWhenDeleteServiceAgreementByInternalId() {
        LegalEntity legalEntity = createLegalEntity("1", "e1");
        LegalEntity parentLe = new LegalEntity();
        legalEntity.setParent(parentLe);
        com.backbase.accesscontrol.domain.Participant participantWithAdmin
            = createParticipantWithAdmin("a1", false, true);
        participantWithAdmin.setLegalEntity(legalEntity);
        participantWithAdmin.setParticipantUsers(newHashSet(new ParticipantUser()));
        ServiceAgreement serviceAgreement = new ServiceAgreement();
        serviceAgreement.setId("540949a03a7846abb69e7c667bc73688");
        serviceAgreement.setDescription("desc");
        serviceAgreement.setCreatorLegalEntity(legalEntity);
        serviceAgreement.setExternalId("new-ex-id");
        serviceAgreement.setMaster(true);
        serviceAgreement.setName("new name");
        serviceAgreement.addParticipant(
            Lists.newArrayList(
                participantWithAdmin
            )
        );
        when(serviceAgreementJpaRepository.findById(eq(serviceAgreement.getId()),
            eq(SERVICE_AGREEMENT_WITH_PARTICIPANTS_AND_CREATOR))).thenReturn(Optional.empty());

        PresentationServiceAgreementIdentifier serviceAgreementIdentifier =
            new PresentationServiceAgreementIdentifier()
                .withIdIdentifier(serviceAgreement.getId());

        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> persistenceServiceAgreementService.deleteServiceAgreementByIdentifier(serviceAgreementIdentifier));

        verify(serviceAgreementJpaRepository)
            .findById(serviceAgreement.getId(), SERVICE_AGREEMENT_WITH_PARTICIPANTS_AND_CREATOR);
        assertThat(exception, new NotFoundErrorMatcher(ERR_ACQ_006.getErrorMessage(), ERR_ACQ_006.getErrorCode()));
    }

    @Test
    public void shouldDeleteServiceAgreementByName() {
        LegalEntity legalEntity = createLegalEntity("1", "e1");
        LegalEntity parentLe = new LegalEntity();
        legalEntity.setParent(parentLe);
        com.backbase.accesscontrol.domain.Participant participantWithAdmin
            = createParticipantWithAdmin("a1", false, true);
        participantWithAdmin.setLegalEntity(legalEntity);
        participantWithAdmin.setParticipantUsers(newHashSet(new ParticipantUser()));
        ServiceAgreement serviceAgreement = new ServiceAgreement();
        serviceAgreement.setId("540949a03a7846abb69e7c667bc73688");
        serviceAgreement.setDescription("desc");
        serviceAgreement.setCreatorLegalEntity(legalEntity);
        serviceAgreement.setExternalId("new-ex-id");
        serviceAgreement.setMaster(true);
        serviceAgreement.setName("Sa Name");
        serviceAgreement.addParticipant(
            Lists.newArrayList(
                participantWithAdmin
            )
        );
        when(serviceAgreementJpaRepository.findServiceAgreementsByName(eq(serviceAgreement.getName())))
            .thenReturn(singletonList(serviceAgreement));

        PresentationServiceAgreementIdentifier serviceAgreementIdentifier =
            new PresentationServiceAgreementIdentifier()
                .withNameIdentifier(serviceAgreement.getName());
        persistenceServiceAgreementService.deleteServiceAgreementByIdentifier(serviceAgreementIdentifier);
        verify(serviceAgreementJpaRepository)
            .findServiceAgreementsByName(serviceAgreement.getName());
    }

    @Test
    public void shouldThrowNotFoundWhenDeleteServiceAgreementByName() {
        LegalEntity legalEntity = createLegalEntity("1", "e1");
        LegalEntity parentLe = new LegalEntity();
        legalEntity.setParent(parentLe);
        com.backbase.accesscontrol.domain.Participant participantWithAdmin
            = createParticipantWithAdmin("a1", false, true);
        participantWithAdmin.setLegalEntity(legalEntity);
        participantWithAdmin.setParticipantUsers(newHashSet(new ParticipantUser()));
        ServiceAgreement serviceAgreement = new ServiceAgreement();
        serviceAgreement.setId("540949a03a7846abb69e7c667bc73688");
        serviceAgreement.setDescription("desc");
        serviceAgreement.setCreatorLegalEntity(legalEntity);
        serviceAgreement.setExternalId("new-ex-id");
        serviceAgreement.setMaster(true);
        serviceAgreement.setName("Sa Name");
        serviceAgreement.addParticipant(
            Lists.newArrayList(
                participantWithAdmin
            )
        );
        when(serviceAgreementJpaRepository.findServiceAgreementsByName(eq(serviceAgreement.getName())))
            .thenReturn(new ArrayList<>());

        PresentationServiceAgreementIdentifier serviceAgreementIdentifier =
            new PresentationServiceAgreementIdentifier()
                .withNameIdentifier(serviceAgreement.getName());

        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> persistenceServiceAgreementService.deleteServiceAgreementByIdentifier(serviceAgreementIdentifier));

        verify(serviceAgreementJpaRepository)
            .findServiceAgreementsByName(serviceAgreement.getName());
        assertThat(exception, new NotFoundErrorMatcher(ERR_ACQ_006.getErrorMessage(), ERR_ACQ_006.getErrorCode()));
    }

    @Test
    public void shouldThrowBadRequestWhenThereAreMoreSericeAgreementsWithSameName() {
        LegalEntity legalEntity = createLegalEntity("1", "e1");
        LegalEntity parentLe = new LegalEntity();
        legalEntity.setParent(parentLe);
        com.backbase.accesscontrol.domain.Participant participantWithAdmin
            = createParticipantWithAdmin("a1", false, true);
        participantWithAdmin.setLegalEntity(legalEntity);
        participantWithAdmin.setParticipantUsers(newHashSet(new ParticipantUser()));
        ServiceAgreement serviceAgreement = new ServiceAgreement();
        serviceAgreement.setId("540949a03a7846abb69e7c667bc73688");
        serviceAgreement.setDescription("desc");
        serviceAgreement.setCreatorLegalEntity(legalEntity);
        serviceAgreement.setExternalId("new-ex-id");
        serviceAgreement.setMaster(true);
        serviceAgreement.setName("Sa Name");
        serviceAgreement.addParticipant(
            Lists.newArrayList(
                participantWithAdmin
            )
        );
        ServiceAgreement serviceAgreementSameName = new ServiceAgreement();
        serviceAgreement.setDescription("desc2");
        serviceAgreement.setCreatorLegalEntity(legalEntity);
        serviceAgreement.setExternalId("new-ex-id2");
        serviceAgreement.setMaster(true);
        serviceAgreement.setName("Sa Name");
        serviceAgreement.addParticipant(
            Lists.newArrayList(
                participantWithAdmin
            )
        );
        when(serviceAgreementJpaRepository.findServiceAgreementsByName(eq(serviceAgreement.getName())))
            .thenReturn(asList(serviceAgreement, serviceAgreementSameName));

        PresentationServiceAgreementIdentifier serviceAgreementIdentifier =
            new PresentationServiceAgreementIdentifier()
                .withNameIdentifier(serviceAgreement.getName());

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> persistenceServiceAgreementService.deleteServiceAgreementByIdentifier(serviceAgreementIdentifier));

        verify(serviceAgreementJpaRepository)
            .findServiceAgreementsByName(serviceAgreement.getName());
        assertThat(exception, new BadRequestErrorMatcher(ERR_ACQ_057.getErrorMessage(), ERR_ACQ_057.getErrorCode()));
    }

    @Test
    public void shouldThrowBadRequestWhenTryingToDeleteMSAOfRootLe() {
        LegalEntity legalEntity = createLegalEntity("1", "e1");
        com.backbase.accesscontrol.domain.Participant participantWithAdmin
            = createParticipantWithAdmin("a1", false, true);
        participantWithAdmin.setLegalEntity(legalEntity);
        participantWithAdmin.setParticipantUsers(newHashSet(new ParticipantUser()));
        ServiceAgreement serviceAgreement = new ServiceAgreement();
        serviceAgreement.setDescription("desc");
        serviceAgreement.setCreatorLegalEntity(legalEntity);
        serviceAgreement.setExternalId("new-ex-id");
        serviceAgreement.setMaster(true);
        serviceAgreement.setName("new name");
        serviceAgreement.addParticipant(
            Lists.newArrayList(
                participantWithAdmin
            )
        );
        PresentationServiceAgreementIdentifier serviceAgreementIdentifier =
            new PresentationServiceAgreementIdentifier()
                .withExternalIdIdentifier(serviceAgreement.getExternalId());

        when(serviceAgreementJpaRepository.findByExternalId(eq(serviceAgreement.getExternalId()),
            eq(SERVICE_AGREEMENT_WITH_PARTICIPANTS_AND_CREATOR))).thenReturn(Optional.of(serviceAgreement));

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> persistenceServiceAgreementService.deleteServiceAgreementByIdentifier(serviceAgreementIdentifier));

        assertThat(exception, new BadRequestErrorMatcher(ERR_ACQ_056.getErrorMessage(), ERR_ACQ_056.getErrorCode()));
    }

    @Test
    public void shouldReturnServiceAgreementByPermissionSetById() {

        List<String> saIds = new ArrayList<>();
        List<ServiceAgreement> serviceAgreements = new ArrayList<>();
        List<ServiceAgreementByPermissionSet> serviceAgreementsByPermissionSet = new ArrayList<>();
        List<PermissionSetsInServiceAgreements> permissionSetList = new ArrayList<>();

        prepareDataForGetServiceAgreementByPermissionSet(saIds, serviceAgreements, serviceAgreementsByPermissionSet,
            permissionSetList);

        SearchAndPaginationParameters searchAndPaginationParameters = new SearchAndPaginationParameters(
            0, 10, null, null
        );
        Page<ServiceAgreement> serviceAgreementsPage = new PageImpl<>(serviceAgreements, Pageable.unpaged(), 2);
        Page<ServiceAgreementByPermissionSet> serviceAgreementsByPsPage = new PageImpl<>(
            serviceAgreementsByPermissionSet, Pageable.unpaged(), 2);

        AssignablePermissionSet assignablePermissionSet = new AssignablePermissionSet() {{
            setId(10L);
            setName("test");
        }};

        when(assignablePermissionSetJpaRepository.findById(anyLong())).thenReturn(Optional.of(
            assignablePermissionSet
        ));

        when(serviceAgreementJpaRepository
            .getServiceAgreementByPermissionSetId(
                eq(assignablePermissionSet),
                eq(searchAndPaginationParameters)))
            .thenReturn(serviceAgreementsPage);

        when(serviceAgreementByPermissionSetMapper.sourceToDestination(eq(serviceAgreements)))
            .thenReturn(serviceAgreementsByPermissionSet);

        Page<ServiceAgreementByPermissionSet> list = persistenceServiceAgreementService
            .getByPermissionSetById("10", searchAndPaginationParameters);

        verify(serviceAgreementJpaRepository).getServiceAgreementByPermissionSetId(eq(assignablePermissionSet),
            eq(searchAndPaginationParameters));
        verify(assignablePermissionSetJpaRepository).findById(eq(10L));
        verify(serviceAgreementByPermissionSetMapper).sourceToDestination(eq(serviceAgreements));
    }

    @Test
    public void shouldReturnServiceAgreementByPermissionSetByName() {
        List<String> saIds = new ArrayList<>();
        List<ServiceAgreement> serviceAgreements = new ArrayList<>();
        List<ServiceAgreementByPermissionSet> serviceAgreementsByPermissionSet = new ArrayList<>();
        List<PermissionSetsInServiceAgreements> permissionSetList = new ArrayList<>();

        prepareDataForGetServiceAgreementByPermissionSet(saIds, serviceAgreements, serviceAgreementsByPermissionSet,
            permissionSetList);

        SearchAndPaginationParameters searchAndPaginationParameters = new SearchAndPaginationParameters(
            0, 10, null, null
        );

        Page<ServiceAgreement> serviceAgreementsPage = new PageImpl<>(serviceAgreements, Pageable.unpaged(), 2);
        Page<ServiceAgreementByPermissionSet> serviceAgreementsByPsPage = new PageImpl<>(
            serviceAgreementsByPermissionSet, Pageable.unpaged(), 2);

        AssignablePermissionSet assignablePermissionSet = new AssignablePermissionSet() {{
            setId(10L);
            setName("test");
        }};

        when(assignablePermissionSetJpaRepository.findByName(anyString())).thenReturn(Optional.of(
            assignablePermissionSet
        ));

        when(serviceAgreementJpaRepository
            .getServiceAgreementByPermissionSetId(
                eq(assignablePermissionSet),
                eq(searchAndPaginationParameters)))
            .thenReturn(serviceAgreementsPage);

        when(serviceAgreementByPermissionSetMapper.sourceToDestination(eq(serviceAgreements)))
            .thenReturn(serviceAgreementsByPermissionSet);

        Page<ServiceAgreementByPermissionSet> list = persistenceServiceAgreementService
            .getByPermissionSetByName("aps", searchAndPaginationParameters);

        verify(serviceAgreementJpaRepository).getServiceAgreementByPermissionSetId(eq(assignablePermissionSet),
            eq(searchAndPaginationParameters));
        verify(assignablePermissionSetJpaRepository).findByName(eq("aps"));
        verify(serviceAgreementByPermissionSetMapper).sourceToDestination(eq(serviceAgreements));

    }

    private void prepareDataForGetServiceAgreementByPermissionSet(List<String> saIds,
        List<ServiceAgreement> serviceAgreements,
        List<ServiceAgreementByPermissionSet> serviceAgreementsByPermissionSet,
        List<PermissionSetsInServiceAgreements> permissionSetList) {
        String saId_1 = "1";
        String saId_2 = "2";
        String saId_3 = "3";
        String saName_1 = "sa-01";
        String saName_2 = "sa-02";
        String saName_3 = "sa-03";
        String saExId_1 = "sa_ext_id_01";
        String saExId_2 = "sa_ext_id_02";
        String saExId_3 = "sa_ext_id_03";

        serviceAgreements.addAll(asList(
            new ServiceAgreement().withId(saId_1).withName(saName_1).withExternalId(saExId_1).withMaster(false),
            new ServiceAgreement().withId(saId_2).withName(saName_2).withExternalId(saExId_2).withMaster(false),
            new ServiceAgreement().withId(saId_3).withName(saName_3).withExternalId(saExId_3).withMaster(true)
        ));
        serviceAgreementsByPermissionSet.addAll(asList(
            new ServiceAgreementByPermissionSet().withId(saId_1).withName(saName_1).withExternalId(saExId_1)
                .withIsMaster(false),
            new ServiceAgreementByPermissionSet().withId(saId_2).withName(saName_2).withExternalId(saExId_2)
                .withIsMaster(false),
            new ServiceAgreementByPermissionSet().withId(saId_3).withName(saName_3).withExternalId(saExId_3)
                .withIsMaster(true)
        ));
        permissionSetList.addAll(asList(
            new PermissionSetsInServiceAgreements(saId_1, 10L, 0),
            new PermissionSetsInServiceAgreements(saId_2, 10L, 1),
            new PermissionSetsInServiceAgreements(saId_3, 10L, 0),
            new PermissionSetsInServiceAgreements(saId_3, 10L, 1)));
        saIds.addAll(asList(saId_1, saId_2, saId_3));
    }

    @Test
    public void shouldFailServiceAgreementByPermissionSetByIdForInvalidId() {

        SearchAndPaginationParameters searchAndPaginationParameters = new SearchAndPaginationParameters(
            0, 10, null, null
        );

        BadRequestException exception = assertThrows(BadRequestException.class, () -> persistenceServiceAgreementService
            .getByPermissionSetById("10a", searchAndPaginationParameters));

        assertThat(exception, new BadRequestErrorMatcher(ERR_ACQ_063.getErrorMessage(), ERR_ACQ_063.getErrorCode()));
    }

    @Test
    public void shouldFailServiceAgreementByPermissionSetByIdForNonExistingId() {

        SearchAndPaginationParameters searchAndPaginationParameters = new SearchAndPaginationParameters(
            0, 10, null, null
        );

        when(assignablePermissionSetJpaRepository.findById(anyLong())).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> persistenceServiceAgreementService
            .getByPermissionSetById("10", searchAndPaginationParameters));

        assertThat(exception, new NotFoundErrorMatcher(ERR_ACQ_062.getErrorMessage(), ERR_ACQ_062.getErrorCode()));
    }

    @Test
    public void shouldFailServiceAgreementByPermissionSetByNameForNonExistingName() {

        SearchAndPaginationParameters searchAndPaginationParameters = new SearchAndPaginationParameters(
            0, 10, null, null
        );

        when(assignablePermissionSetJpaRepository.findByName(eq("aps"))).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> persistenceServiceAgreementService
            .getByPermissionSetByName("aps", searchAndPaginationParameters));
        assertThat(exception, new NotFoundErrorMatcher(ERR_ACQ_062.getErrorMessage(), ERR_ACQ_062.getErrorCode()));
    }

    @Test
    public void shouldReturnEmptyListWhenSomeOfThePrivilegesProvidedIsInvalid() {
        String userId = "userId";
        String dataGroupType = "ARRANGEMENTS";
        String functionName = "Payments";
        String resourceName = "Transactions";
        String privileges = "invalidPrivilege,view,create";

        List<PersistenceServiceAgreementDataGroups> saDgResponse = persistenceServiceAgreementService
            .getServiceAgreementsDataGroups(userId, dataGroupType, functionName, resourceName, privileges);

        verify(businessFunctionCache, times(0))
            .getByFunctionNameOrResourceNameOrPrivilegesOptional(eq(functionName), eq(resourceName), eq(emptyList()));

        verify(serviceAgreementJpaRepository, times(0))
            .findByUserIdAndDataGroupTypeAndAfpIdsIn(eq(userId), eq(dataGroupType), eq(emptySet()));

        assertTrue(saDgResponse.isEmpty());
    }

    @Test
    public void shouldContinueExecutionNormallyWhenWePassEmptyPrivilege() {
        String userId = "userId";
        String dataGroupType = "ARRANGEMENTS";
        String functionName = "Payments";
        String resourceName = "Transactions";
        String privileges = "";
        Set<String> afpIds = newHashSet("afpId-01", "afpId-02");

        when(businessFunctionCache.getByFunctionNameOrResourceNameOrPrivilegesOptional(eq(functionName),
            eq(resourceName), eq(emptyList()))).thenReturn(afpIds);

        persistenceServiceAgreementService
            .getServiceAgreementsDataGroups(userId, dataGroupType, functionName, resourceName, privileges);

        verify(businessFunctionCache)
            .getByFunctionNameOrResourceNameOrPrivilegesOptional(eq(functionName), eq(resourceName), eq(emptyList()));

        verify(serviceAgreementJpaRepository)
            .findByUserIdAndDataGroupTypeAndAfpIdsIn(eq(userId), eq(dataGroupType), eq(afpIds));
    }

    @Test
    public void shouldReturnEmptyListWhenAfpIdsReturnedFromCacheIsEmpty() {
        String userId = "userId";
        String dataGroupType = "ARRANGEMENTS";
        String functionName = "invalidFgName";
        String resourceName = "invalidResource";

        when(businessFunctionCache.getByFunctionNameOrResourceNameOrPrivilegesOptional(eq(functionName),
            eq(resourceName), eq(emptyList()))).thenReturn(emptySet());

        List<PersistenceServiceAgreementDataGroups> saDgResponse = persistenceServiceAgreementService
            .getServiceAgreementsDataGroups(userId, dataGroupType, functionName, resourceName, null);

        assertTrue(saDgResponse.isEmpty());
    }

    @Test
    public void shouldCallRepositoryWithEmptyAfpIdsAndShouldNotCallCacheWhenFunctionNameAndResourceNameAreNull() {
        String userId = "userId";
        String dataGroupType = "ARRANGEMENTS";
        String functionName = null;
        String resourceName = null;

        persistenceServiceAgreementService
            .getServiceAgreementsDataGroups(userId, dataGroupType, functionName, resourceName, null);

        verify(businessFunctionCache, times(0))
            .getByFunctionNameOrResourceNameOrPrivilegesOptional(isNull(), isNull(), eq(emptyList()));

        verify(serviceAgreementJpaRepository)
            .findByUserIdAndDataGroupTypeAndAfpIdsIn(eq(userId), eq(dataGroupType), eq(emptySet()));
    }

    @Test
    public void shouldGetServiceAgreementsDataGroupsWhenPrivilegesAreNotProvided() {
        String userId = "userId";
        String dataGroupType = "ARRANGEMENTS";
        String functionName = "SEPA CT";
        String resourceName = "Payments";

        Set<String> afpIds = newHashSet("afpId-01", "afpId-02");
        when(businessFunctionCache.getByFunctionNameOrResourceNameOrPrivilegesOptional(eq(functionName),
            eq(resourceName), eq(emptyList()))).thenReturn(afpIds);

        List<Tuple> mockData = asList(
            mockTuple("saId-01", "dgId-01", "item-01", "afpId-01"),
            mockTuple("saId-01", "dgId-01", "item-02", "afpId-01"),
            mockTuple("saId-01", "dgId-02", "item-03", "afpId-01"),
            mockTuple("saId-01", "dgId-02", "item-04", "afpId-01"),

            mockTuple("saId-02", "dgId-03", "item-05", "afpId-01"),
            mockTuple("saId-02", "dgId-03", "item-06", "afpId-01"),
            mockTuple("saId-02", "dgId-04", "item-07", "afpId-01"),
            mockTuple("saId-02", "dgId-04", "item-08", "afpId-01")
        );

        when(serviceAgreementJpaRepository
            .findByUserIdAndDataGroupTypeAndAfpIdsIn(eq(userId), eq(dataGroupType), eq(afpIds))).thenReturn(mockData);

        List<PersistenceServiceAgreementDataGroups> saDgResponse = persistenceServiceAgreementService
            .getServiceAgreementsDataGroups(userId, dataGroupType, functionName, resourceName, null);

        assertThat(saDgResponse, containsInAnyOrder(
            allOf(
                hasProperty("serviceAgreementId", is("saId-01")),
                hasProperty("dataGroups", containsInAnyOrder(
                    allOf(
                        hasProperty("id", is("dgId-01")),
                        hasProperty("items", containsInAnyOrder("item-01", "item-02"))
                    ),
                    allOf(
                        hasProperty("id", is("dgId-02")),
                        hasProperty("items", containsInAnyOrder("item-03", "item-04")))
                    )
                )
            ),
            allOf(
                hasProperty("serviceAgreementId", is("saId-02")),
                hasProperty("dataGroups", containsInAnyOrder(
                    allOf(
                        hasProperty("id", is("dgId-03")),
                        hasProperty("items", containsInAnyOrder("item-05", "item-06"))
                    ),
                    allOf(
                        hasProperty("id", is("dgId-04")),
                        hasProperty("items", containsInAnyOrder("item-07", "item-08")))
                    )
                )
            )
        ));
    }

    @Test
    public void shouldGetServiceAgreementsDataGroupsWhenPrivilegesProvided() {
        String userId = "userId";
        String dataGroupType = "ARRANGEMENTS";
        String functionName = "SEPA CT";
        String resourceName = "Payments";
        String privileges = "view,create";

        Set<String> afpIds = newHashSet("afpId-01", "afpId-02");
        when(businessFunctionCache.getByFunctionNameOrResourceNameOrPrivilegesOptional(eq(functionName),
            eq(resourceName), eq(asList("view", "create")))).thenReturn(afpIds);

        ApplicableFunctionPrivilege afp01 = new ApplicableFunctionPrivilege();
        afp01.setId("afpId-01");
        afp01.setBusinessFunctionName(functionName);
        afp01.setBusinessFunctionResourceName(resourceName);
        afp01.setPrivilegeName("view");

        ApplicableFunctionPrivilege afp02 = new ApplicableFunctionPrivilege();
        afp02.setId("afpId-02");
        afp02.setBusinessFunctionName(functionName);
        afp02.setBusinessFunctionResourceName(resourceName);
        afp02.setPrivilegeName("create");

        ApplicableFunctionPrivilege afp03 = new ApplicableFunctionPrivilege();
        afp03.setId("afpId-03");
        afp03.setBusinessFunctionName("Product Summary");
        afp03.setBusinessFunctionResourceName("Product Summary");
        afp03.setPrivilegeName("create");

        when(businessFunctionCache.getApplicableFunctionPrivileges(eq(afpIds)))
            .thenReturn(newHashSet(afp01, afp02));

        when(businessFunctionCache.getApplicableFunctionPrivileges(eq(newHashSet("afpId-01"))))
            .thenReturn(newHashSet(afp01));

        when(businessFunctionCache.getApplicableFunctionPrivileges(eq(newHashSet("afpId-03"))))
            .thenReturn(newHashSet(afp03));

        List<Tuple> mockData = asList(
            mockTuple("saId-01", "dgId-01", "item-01", "afpId-01"),
            mockTuple("saId-01", "dgId-01", "item-01", "afpId-02"),

            mockTuple("saId-01", "dgId-01", "item-02", "afpId-01"),
            mockTuple("saId-01", "dgId-01", "item-02", "afpId-02"),

            mockTuple("saId-01", "dgId-02", "item-03", "afpId-01"),
            mockTuple("saId-01", "dgId-02", "item-03", "afpId-02"),

            mockTuple("saId-01", "dgId-02", "item-04", "afpId-01"),
            mockTuple("saId-01", "dgId-02", "item-04", "afpId-02"),

            mockTuple("saId-02", "dgId-03", "item-05", "afpId-01"),

            mockTuple("saId-02", "dgId-03", "item-06", "afpId-01"),

            mockTuple("saId-02", "dgId-04", "item-07", "afpId-01"),
            mockTuple("saId-02", "dgId-04", "item-07", "afpId-02"),

            mockTuple("saId-02", "dgId-04", "item-08", "afpId-01"),
            mockTuple("saId-02", "dgId-04", "item-08", "afpId-02"),

            mockTuple("saId-03", "dgId-05", "item-09", "afpId-03")
        );

        when(serviceAgreementJpaRepository
            .findByUserIdAndDataGroupTypeAndAfpIdsIn(eq(userId), eq(dataGroupType), eq(emptyList())))
            .thenReturn(mockData);

        List<PersistenceServiceAgreementDataGroups> saDgResponse = persistenceServiceAgreementService
            .getServiceAgreementsDataGroups(userId, dataGroupType, functionName, resourceName, privileges);

        assertThat(saDgResponse, containsInAnyOrder(
            allOf(
                hasProperty("serviceAgreementId", is("saId-01")),
                hasProperty("dataGroups", containsInAnyOrder(
                    allOf(
                        hasProperty("id", is("dgId-01")),
                        hasProperty("items", containsInAnyOrder("item-01", "item-02"))
                    ),
                    allOf(
                        hasProperty("id", is("dgId-02")),
                        hasProperty("items", containsInAnyOrder("item-03", "item-04")))
                    )
                )
            ),
            allOf(
                hasProperty("serviceAgreementId", is("saId-02")),
                hasProperty("dataGroups", containsInAnyOrder(
                    allOf(
                        hasProperty("id", is("dgId-04")),
                        hasProperty("items", containsInAnyOrder("item-07", "item-08")))
                    )
                )
            )
        ));
    }

    @Test
    public void shouldSaveNewCustomServiceAgreement() throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        timeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        dateFormat.setLenient(false);
        timeFormat.setLenient(false);

        Date start = DateUtils.truncate(dateFormat.parse("2020-01-01"), Calendar.SECOND);
        String startDate = dateFormat.format(start);
        String startTime = timeFormat.format(start);

        Date end = DateUtils.truncate(new Date(), Calendar.SECOND);
        String endDate = dateFormat.format(end);
        String endTime = timeFormat.format(end);

        LegalEntity legalEntity = new LegalEntity()
            .withId("le-id");
        NewCustomServiceAgreement newCsa = new NewCustomServiceAgreement()
            .withServiceAgreementName("name")
            .withServiceAgreementDescription("desc")
            .withServiceAgreementExternalId("ex-id")
            .withServiceAgreementValidFromDate(startDate)
            .withServiceAgreementValidFromTime(startTime)
            .withServiceAgreementValidUntilDate(endDate)
            .withServiceAgreementValidUntilTime(endTime)
            .withServiceAgreementState(com.backbase.presentation.legalentity.rest.spec.v2.legalentities.Status.DISABLED)
            .withParticipantInfo(new ParticipantInfo()
                .withShareAccounts(false)
                .withShareUsers(false));
        newCsa.setAddition("additionName", "additionValue");
        String creatorLegalEntityId = "creator-le-id";
        
        when(serviceAgreementJpaRepository.save(any(ServiceAgreement.class))).thenAnswer(i -> i.getArguments()[0]);
        
        
        ServiceAgreement createdServiceAgreement = persistenceServiceAgreementService.save(legalEntity, newCsa, creatorLegalEntityId);
        
        assertNotNull(createdServiceAgreement);
        assertEquals(newCsa.getServiceAgreementName(), createdServiceAgreement.getName());
        assertEquals(newCsa.getServiceAgreementDescription(), createdServiceAgreement.getDescription());
        assertEquals(newCsa.getServiceAgreementExternalId(), createdServiceAgreement.getExternalId());
        assertEquals(creatorLegalEntityId, createdServiceAgreement.getCreatorLegalEntity().getId());
        assertEquals(start, createdServiceAgreement.getStartDate());
        assertEquals(end, createdServiceAgreement.getEndDate());
        assertEquals(ServiceAgreementState.DISABLED, createdServiceAgreement.getState());
        assertEquals(newCsa.getAdditions(), createdServiceAgreement.getAdditions());
        assertEquals(1, createdServiceAgreement.getParticipants().size());
        com.backbase.accesscontrol.domain.Participant participant = createdServiceAgreement.getParticipants()
            .get("le-id");
        assertEquals(legalEntity, participant.getLegalEntity());
        assertEquals(newCsa.getParticipantInfo().getShareAccounts(), participant.isShareAccounts());
        assertEquals(newCsa.getParticipantInfo().getShareUsers(), participant.isShareUsers());
    }

    @Test
    public void shouldSaveNewCustomServiceAgreementWithEnabledStatusDefault() throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");

        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        timeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        dateFormat.setLenient(false);
        timeFormat.setLenient(false);

        Date start = DateUtils.truncate(dateFormat.parse("2020-01-01"), Calendar.SECOND);
        String startDate = dateFormat.format(start);
        String startTime = timeFormat.format(start);

        Date end = DateUtils.truncate(new Date(), Calendar.SECOND);
        String endDate = dateFormat.format(end);
        String endTime = timeFormat.format(end);

        LegalEntity legalEntity = new LegalEntity()
            .withId("le-id");
        NewCustomServiceAgreement newCsa = new NewCustomServiceAgreement()
            .withServiceAgreementName("name")
            .withServiceAgreementDescription("desc")
            .withServiceAgreementExternalId("ex-id")
            .withServiceAgreementValidFromDate(startDate)
            .withServiceAgreementValidFromTime(startTime)
            .withServiceAgreementValidUntilDate(endDate)
            .withServiceAgreementValidUntilTime(endTime)
            .withParticipantInfo(new ParticipantInfo()
                .withShareAccounts(false)
                .withShareUsers(false));
        String creatorLegalEntityId = "creator-le-id";

        when(serviceAgreementJpaRepository.save(any(ServiceAgreement.class))).thenAnswer(i -> i.getArguments()[0]);

        ServiceAgreement createdServiceAgreement = persistenceServiceAgreementService
            .save(legalEntity, newCsa, creatorLegalEntityId);

        assertNotNull(createdServiceAgreement);
        assertEquals(newCsa.getServiceAgreementName(), createdServiceAgreement.getName());
        assertEquals(newCsa.getServiceAgreementDescription(), createdServiceAgreement.getDescription());
        assertEquals(newCsa.getServiceAgreementExternalId(), createdServiceAgreement.getExternalId());
        assertEquals(creatorLegalEntityId, createdServiceAgreement.getCreatorLegalEntity().getId());
        assertEquals(start, createdServiceAgreement.getStartDate());
        assertEquals(end, createdServiceAgreement.getEndDate());
        assertEquals(ServiceAgreementState.ENABLED, createdServiceAgreement.getState());
        assertEquals(1, createdServiceAgreement.getParticipants().size());
        com.backbase.accesscontrol.domain.Participant participant = createdServiceAgreement.getParticipants()
            .get("le-id");
        assertEquals(legalEntity, participant.getLegalEntity());
        assertEquals(newCsa.getParticipantInfo().getShareAccounts(), participant.isShareAccounts());
        assertEquals(newCsa.getParticipantInfo().getShareUsers(), participant.isShareUsers());
    }

    @Test
    public void shouldThrowExceptionWhenExistingServiceAgreementExternalIdInNewCustomServiceAgreement()
        throws ParseException {
        LegalEntity legalEntity = new LegalEntity()
            .withId("le-id");
        NewCustomServiceAgreement newCsa = new NewCustomServiceAgreement()
            .withServiceAgreementName("name")
            .withServiceAgreementDescription("desc")
            .withServiceAgreementExternalId("ex-id")
            .withParticipantInfo(new ParticipantInfo()
                .withShareAccounts(false)
                .withShareUsers(false));
        String creatorLegalEntityId = "creator-le-id";

        when(serviceAgreementJpaRepository.findByExternalId(eq("ex-id")))
            .thenReturn(Optional.of(new ServiceAgreement()));

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> persistenceServiceAgreementService.save(legalEntity, newCsa, creatorLegalEntityId));
        assertThat(exception, new BadRequestErrorMatcher(AccessGroupErrorCodes.ERR_AG_069.getErrorMessage(),
            AccessGroupErrorCodes.ERR_AG_069.getErrorCode()));
    }

    @Test
    public void shouldSaveNewCustomSaWhenNullServiceAgreementExternalIdInNewCustomServiceAgreement() {
        LegalEntity legalEntity = new LegalEntity()
            .withId("le-id");
        NewCustomServiceAgreement newCsa = new NewCustomServiceAgreement()
            .withServiceAgreementName("name")
            .withServiceAgreementDescription("desc")
            .withServiceAgreementExternalId(null)
            .withParticipantInfo(new ParticipantInfo()
                .withShareAccounts(false)
                .withShareUsers(false));
        String creatorLegalEntityId = "creator-le-id";

        when(serviceAgreementJpaRepository.save(any(ServiceAgreement.class))).thenAnswer(i -> i.getArguments()[0]);

        ServiceAgreement createdServiceAgreement = persistenceServiceAgreementService
            .save(legalEntity, newCsa, creatorLegalEntityId);

        verify(serviceAgreementJpaRepository, times(0)).findByExternalId(anyString());

        assertNotNull(createdServiceAgreement);
        assertEquals(newCsa.getServiceAgreementName(), createdServiceAgreement.getName());
        assertEquals(newCsa.getServiceAgreementDescription(), createdServiceAgreement.getDescription());
        assertEquals(newCsa.getServiceAgreementExternalId(), createdServiceAgreement.getExternalId());
        assertEquals(creatorLegalEntityId, createdServiceAgreement.getCreatorLegalEntity().getId());
        assertEquals(ServiceAgreementState.ENABLED, createdServiceAgreement.getState());
        assertEquals(1, createdServiceAgreement.getParticipants().size());
        com.backbase.accesscontrol.domain.Participant participant = createdServiceAgreement.getParticipants()
            .get("le-id");
        assertEquals(legalEntity, participant.getLegalEntity());
        assertEquals(newCsa.getParticipantInfo().getShareAccounts(), participant.isShareAccounts());
        assertEquals(newCsa.getParticipantInfo().getShareUsers(), participant.isShareUsers());
    }

    @Test
    public void shouldSaveNewMasterServiceAgreement() throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");

        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        timeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        dateFormat.setLenient(false);
        timeFormat.setLenient(false);

        Date start = DateUtils.truncate(dateFormat.parse("2020-01-01"), Calendar.SECOND);
        String startDate = dateFormat.format(start);
        String startTime = timeFormat.format(start);

        Date end = DateUtils.truncate(new Date(), Calendar.SECOND);
        String endDate = dateFormat.format(end);
        String endTime = timeFormat.format(end);

        LegalEntity legalEntity = new LegalEntity()
            .withId("le-id");
        NewMasterServiceAgreement newMsa = new NewMasterServiceAgreement()
            .withServiceAgreementName("name")
            .withServiceAgreementDescription("desc")
            .withServiceAgreementValidFromDate(startDate)
            .withServiceAgreementValidFromTime(startTime)
            .withServiceAgreementValidUntilDate(endDate)
            .withServiceAgreementValidUntilTime(endTime)
            .withServiceAgreementState(com.backbase.presentation.legalentity.rest.spec.v2.legalentities.Status.ENABLED);
        newMsa.setAddition("additionName", "additionValue");

        when(serviceAgreementJpaRepository.save(any(ServiceAgreement.class))).thenAnswer(i -> i.getArguments()[0]);

        ServiceAgreement createdServiceAgreement = persistenceServiceAgreementService.save(legalEntity, newMsa);

        assertNotNull(createdServiceAgreement);
        assertEquals(newMsa.getServiceAgreementName(), createdServiceAgreement.getName());
        assertEquals(newMsa.getServiceAgreementDescription(), createdServiceAgreement.getDescription());
        assertEquals(start, createdServiceAgreement.getStartDate());
        assertEquals(end, createdServiceAgreement.getEndDate());
        assertEquals(newMsa.getAdditions(), createdServiceAgreement.getAdditions());
        assertEquals(1, createdServiceAgreement.getParticipants().size());
        com.backbase.accesscontrol.domain.Participant participant = createdServiceAgreement.getParticipants()
            .get("le-id");
        assertEquals(legalEntity, participant.getLegalEntity());
    }

    @Test
    public void shouldThrowExceptionWhenInvalidDateInNewMasterServiceAgreement() throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");

        Date end = DateUtils.truncate(new Date(), Calendar.SECOND);
        String endDate = dateFormat.format(end);
        String endTime = timeFormat.format(end);

        Date start = DateUtils.truncate(new Date(), Calendar.SECOND);
        String startDate = dateFormat.format(start);
        String startTime = timeFormat.format(start);

        LegalEntity legalEntity = new LegalEntity()
            .withId("le-id");
        NewMasterServiceAgreement newMsa = new NewMasterServiceAgreement()
            .withServiceAgreementName("name")
            .withServiceAgreementDescription("desc")
            .withServiceAgreementValidFromDate(startDate)
            .withServiceAgreementValidFromTime(startTime)
            .withServiceAgreementValidUntilDate(endDate)
            .withServiceAgreementValidUntilTime(endTime)
            .withServiceAgreementState(com.backbase.presentation.legalentity.rest.spec.v2.legalentities.Status.ENABLED);

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> persistenceServiceAgreementService.save(legalEntity, newMsa));
        assertThat(exception, new BadRequestErrorMatcher(CommandErrorCodes.ERR_ACC_077.getErrorMessage(),
            CommandErrorCodes.ERR_ACC_077.getErrorCode()));
    }

    private Tuple mockTuple(String saId, String dgId, String itemId, String afpId) {
        Tuple tuple = mock(Tuple.class);
        when(tuple.get(0, String.class)).thenReturn(saId);
        when(tuple.get(1, String.class)).thenReturn(dgId);
        when(tuple.get(2, String.class)).thenReturn(itemId);
        when(tuple.get(3, String.class)).thenReturn(afpId);
        return tuple;
    }

    private ServiceAgreementPostRequestBody createAddServiceAgreementRequestBody(String name, String description) {
        return new ServiceAgreementPostRequestBody()
            .withName(name)
            .withDescription(description)
            .withStatus(CreateStatus.DISABLED);
    }

    private void mockGetServiceAgreementByExternalId(String serviceAgreementExternalId,
        ServiceAgreement serviceAgreement) {
        when(serviceAgreementJpaRepository.findByExternalId(eq(serviceAgreementExternalId)))
            .thenReturn(Optional.of(serviceAgreement));
    }

    private ServiceAgreement getServiceAgreement(LegalEntity creatorLegalEntity, boolean master) {
        ServiceAgreement serviceAgreement1 = new ServiceAgreement();
        serviceAgreement1.setMaster(master);
        serviceAgreement1.setCreatorLegalEntity(creatorLegalEntity);
        return serviceAgreement1;
    }

    private LegalEntity createLegalEntity(String creatorId, String externalId) {
        LegalEntity legalEntity = new LegalEntity();
        legalEntity.setId(creatorId);
        legalEntity.setExternalId(externalId);
        return legalEntity;
    }

    private ServiceAgreementSave getServiceAgreementSaveMock(
        String id,
        Set<Participant> participants) {
        return new ServiceAgreementSave()
            .withDescription("Description")
            .withExternalId("ext_" + id)
            .withName("name_" + id)
            .withParticipants(participants)
            .withStatus(
                com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.Status.ENABLED);
    }

    private List<Participant> getParticipants(
        String id) {
        return Lists.newArrayList(
            new Participant()
                .withId("le1_" + id)
                .withSharingAccounts(true)
                .withSharingUsers(false)
                .withAdmins(newHashSet("admin1_" + id)),
            new Participant()
                .withId("le2_" + id)
                .withSharingAccounts(false)
                .withSharingUsers(true)
                .withAdmins(newHashSet("admin2_" + id)));
    }

    private List<Participant> getMasterServiceAgreementParticipants(
        String id, boolean sharingUsers, boolean sharingAccounts) {
        return Lists.newArrayList(
            new Participant()
                .withId("le1_" + id)
                .withSharingAccounts(sharingAccounts)
                .withSharingUsers(sharingUsers)
                .withAdmins(newHashSet("admin1_" + id)));
    }

    private ServiceAgreement createServiceAgreementMock(String id) {
        ServiceAgreement serviceAgreementMock = new ServiceAgreement();
        serviceAgreementMock.setExternalId(id);
        serviceAgreementMock.setDescription("Description");
        serviceAgreementMock.setExternalId("ext_" + id);
        serviceAgreementMock.setMaster(false);
        serviceAgreementMock.setName("name_" + id);
        serviceAgreementMock.setState(ServiceAgreementState.ENABLED);
        com.backbase.accesscontrol.domain.Participant participant1 =
            new com.backbase.accesscontrol.domain.Participant();
        participant1.setId("par1_" + id);
        LegalEntity legalEntity = createLegalEntity("le1_" + id, null);
        participant1.setLegalEntity(legalEntity);
        participant1.setShareAccounts(true);
        participant1.setShareUsers(false);
        participant1.addAdmin("admin1_" + id);
        com.backbase.accesscontrol.domain.Participant participant2 =
            new com.backbase.accesscontrol.domain.Participant();
        participant2.setId("par2_" + id);
        legalEntity = new LegalEntity();
        legalEntity.setId("le2_" + id);
        participant2.setLegalEntity(legalEntity);
        participant2.setShareAccounts(false);
        participant2.setShareUsers(true);
        participant2.addAdmin("admin2_" + id);
        serviceAgreementMock.addParticipant(Lists.newArrayList(participant1, participant2));
        legalEntity = new LegalEntity();
        legalEntity.setId("master");
        serviceAgreementMock.setCreatorLegalEntity(legalEntity);
        return serviceAgreementMock;
    }

    private ServiceAgreement createMasterServiceAgreementMock(String id) {
        ServiceAgreement serviceAgreementMock = new ServiceAgreement();
        serviceAgreementMock.setExternalId(id);
        serviceAgreementMock.setDescription("Description");
        serviceAgreementMock.setExternalId("ext_" + id);
        serviceAgreementMock.setMaster(true);
        serviceAgreementMock.setName("name_" + id);
        serviceAgreementMock.setState(ServiceAgreementState.ENABLED);
        com.backbase.accesscontrol.domain.Participant participant1 =
            new com.backbase.accesscontrol.domain.Participant();
        participant1.setId("par1_" + id);
        LegalEntity legalEntity = createLegalEntity("le1_" + id, null);
        participant1.setLegalEntity(legalEntity);
        participant1.setShareAccounts(true);
        participant1.setShareUsers(true);
        participant1.addAdmin("admin1_" + id);
        legalEntity = new LegalEntity();
        legalEntity.setId("le2_" + id);
        serviceAgreementMock.addParticipant(Lists.newArrayList(participant1));
        legalEntity = new LegalEntity();
        serviceAgreementMock.setCreatorLegalEntity(legalEntity);
        return serviceAgreementMock;
    }

    private com.backbase.accesscontrol.domain.Participant getProvider(ParticipantUser... participantUsers) {
        com.backbase.accesscontrol.domain.Participant provider =
            new com.backbase.accesscontrol.domain.Participant();
        provider.setParticipantUsers(new LinkedHashSet<>(asList(participantUsers)));
        provider.setShareUsers(true);
        return provider;
    }

    private ParticipantUser getProviderUser(String userId) {
        ParticipantUser participantUser = new ParticipantUser();
        participantUser.setUserId(userId);
        return participantUser;
    }

    private List<ServiceAgreement> getServiceAgreements() {
        List<ServiceAgreement> serviceAgreements = new ArrayList<>();
        LegalEntity creatorEntity = createLegalEntity("le1", "ex-id");
        ServiceAgreement serviceAgreement1 = new ServiceAgreement();
        serviceAgreement1.setId("1");
        serviceAgreement1.setName("saName1");
        serviceAgreement1.setCreatorLegalEntity(creatorEntity);
        serviceAgreement1.setState(ServiceAgreementState.DISABLED);
        serviceAgreements.add(serviceAgreement1);

        ServiceAgreement serviceAgreement2 = new ServiceAgreement();
        serviceAgreement2.setName("saName2");
        serviceAgreement2.setId("2");
        serviceAgreement2.setCreatorLegalEntity(creatorEntity);
        serviceAgreement2.setState(ServiceAgreementState.DISABLED);
        serviceAgreements.add(serviceAgreement2);

        ServiceAgreement serviceAgreement3 = new ServiceAgreement();
        serviceAgreement3.setName("saName3");
        serviceAgreement3.setId("3");
        serviceAgreement3.setCreatorLegalEntity(creatorEntity);
        serviceAgreement3.setState(ServiceAgreementState.ENABLED);
        serviceAgreement3.setMaster(true);
        serviceAgreement3.setExternalId("ex-sa-3");
        serviceAgreements.add(serviceAgreement3);

        return serviceAgreements;
    }


    private boolean containsParticipantWithRoles(
        List<com.backbase.pandp.accesscontrol.query.rest.spec.v2
            .accesscontrol.accessgroups.serviceagreements.Participant> participants,
        LegalEntity legalEntity, boolean sharingUsers, boolean sharingAccounts) {
        return participants
            .stream()
            .anyMatch(participant ->
                participant.getId().equals(legalEntity.getId())
                    && participant.getName().equals(legalEntity.getName())
                    && participant.getExternalId().equals(legalEntity.getExternalId())
                    && participant.getSharingUsers() == sharingUsers
                    && participant.getSharingAccounts() == sharingAccounts
            );
    }

    private com.backbase.accesscontrol.domain.Participant getProviderFromList(
        Collection<com.backbase.accesscontrol.domain.Participant> valueProviderList,
        com.backbase.accesscontrol.domain.Participant provider11) {
        return valueProviderList.stream().filter(provider -> provider.getId().equals(provider11.getId())).findFirst()
            .get();
    }

}
