package com.backbase.accesscontrol.service.impl;

import static com.backbase.accesscontrol.matchers.ServiceAgreementMatcher.getServiceAgreementMatcher;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_057;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_058;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_060;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_061;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_062;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_095;
import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_005;
import static com.backbase.accesscontrol.util.helpers.LegalEntityUtil.createLegalEntity;
import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;

import com.backbase.accesscontrol.domain.LegalEntity;
import com.backbase.accesscontrol.domain.Participant;
import com.backbase.accesscontrol.domain.ServiceAgreement;
import com.backbase.accesscontrol.domain.ServiceAgreementState;
import com.backbase.accesscontrol.dto.ServiceAgreementData;
import com.backbase.accesscontrol.matchers.BadRequestErrorMatcher;
import com.backbase.accesscontrol.matchers.NotFoundErrorMatcher;
import com.backbase.accesscontrol.repository.LegalEntityJpaRepository;
import com.backbase.accesscontrol.service.DateTimeService;
import com.backbase.accesscontrol.service.PermissionSetService;
import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.buildingblocks.presentation.errors.InternalServerErrorException;
import com.backbase.buildingblocks.presentation.errors.NotFoundException;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.aps.PresentationUserApsIdentifiers;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.CreateStatus;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ParticipantIngest;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementIngestPostRequestBody;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ServiceAgreementIngestServiceImplTest {

    @InjectMocks
    private ServiceAgreementIngestServiceImpl serviceAgreementIngestService;
    @Mock
    private LegalEntityJpaRepository legalEntityJpaRepository;
    @Mock
    private PersistenceServiceAgreementService persistenceServiceAgreementService;
    @Mock
    private PermissionSetService permissionSetService;
    @Spy
    private DateTimeService dateTimeService = new DateTimeService("UTC");
    @Captor
    private ArgumentCaptor<ServiceAgreement> serviceAgreementArgumentCaptor;

    @Test
    public void shouldTransformAndInvokeService() {
        Map<String, String> additions = new HashMap<>();
        additions.put("key", "value");
        Date from = dateTimeService.getStartDateFromDateAndTime("2000-01-01", "00:00:00");
        Date until = dateTimeService.getStartDateFromDateAndTime("2100-01-01", "00:00:00");
        ServiceAgreementIngestPostRequestBody persistenceServiceAgreementIngest = new ServiceAgreementIngestPostRequestBody()
            .withName("name")
            .withExternalId("external-id")
            .withStatus(CreateStatus.ENABLED)
            .withIsMaster(true)
            .withDescription("description")
            .withValidFromDate("2000-01-01")
            .withValidFromTime("00:00:00")
            .withValidUntilDate("2100-01-01")
            .withValidUntilTime("00:00:00")
            .withAdminUserAps(new PresentationUserApsIdentifiers()
                .withIdIdentifiers(Sets.newHashSet(new BigDecimal(1))))
            .withRegularUserAps(new PresentationUserApsIdentifiers()
                .withNameIdentifiers(Sets.newHashSet("name")))
            .withParticipantsToIngest(
                Sets.newHashSet(new ParticipantIngest()
                    .withExternalId("exId")
                    .withAdmins(
                        Sets.newHashSet("u1"))
                    .withUsers(
                        Sets.newHashSet("u1"))
                    .withSharingAccounts(true)
                    .withSharingUsers(true)
                )
            );
        persistenceServiceAgreementIngest
            .setAdditions(additions);

        ServiceAgreement saved = new ServiceAgreement();
        saved.setId("save-id");

        LegalEntity exId = createLegalEntity("1", null, "exId", null, null);
        LegalEntity root = createLegalEntity("2", null, "exId2", null, null);
        when(legalEntityJpaRepository.findDistinctByExternalIdIn(anyList(), isNull()))
            .thenReturn(newArrayList(exId));
        when(legalEntityJpaRepository.findDistinctByParentIsNull(eq(null)))
            .thenReturn(newArrayList(root));
        Participant value = new Participant();
        value.setLegalEntity(exId);
        value.setServiceAgreement(null);

        com.backbase.dbs.user.api.client.v2.model.GetUser user1 = new com.backbase.dbs.user.api.client.v2.model.GetUser();
        user1.setId("id1");
        user1.setLegalEntityId("1");
        user1.setExternalId("u1");

        when(permissionSetService.getAssignablePermissionSetsById(anySet(), anyBoolean())).thenReturn(new HashSet<>());
        when(persistenceServiceAgreementService.create(any(ServiceAgreement.class))).thenReturn(saved);
        String serviceAgreement = serviceAgreementIngestService
            .ingestServiceAgreement(new ServiceAgreementData<>(persistenceServiceAgreementIngest,
                Maps.toMap(Sets.newHashSet("u1"),
                    key -> user1)));

        assertThat(serviceAgreement, is(saved.getId()));
        verify(permissionSetService)
            .getAssignablePermissionSetsByName(eq(Sets.newHashSet("name")), eq(true));
        verify(permissionSetService)
            .getAssignablePermissionSetsById(eq(Sets.newHashSet(1L)), eq(false));
        verify(persistenceServiceAgreementService).create(serviceAgreementArgumentCaptor.capture());
        ServiceAgreement captorValue = serviceAgreementArgumentCaptor.getValue();
        assertEquals(persistenceServiceAgreementIngest.getIsMaster(), captorValue.isMaster());
        assertEquals(Sets.newHashSet(exId.getId()), captorValue.getParticipants().keySet());
        assertEquals(from, captorValue.getStartDate());
        assertEquals(until, captorValue.getEndDate());
        Participant participant = captorValue.getParticipants().get(exId.getId());
        assertTrue(participant.isShareAccounts());
        assertTrue(participant.isShareUsers());
        assertThat(participant,
            allOf(
                hasProperty("id", nullValue()),
                hasProperty("legalEntity", is(exId)),
                hasProperty("serviceAgreement", is(captorValue))
            ));
    }

    @Test
    public void shouldTransformAndInvokeServiceWithOutParticipants() {
        Map<String, String> additions = new HashMap<>();
        additions.put("key", "value");
        ServiceAgreementIngestPostRequestBody persistenceServiceAgreementIngest = new ServiceAgreementIngestPostRequestBody()
            .withName("name")
            .withExternalId("external-id")
            .withStatus(CreateStatus.ENABLED)
            .withIsMaster(true)
            .withDescription("description")
            .withParticipantsToIngest(null);
        persistenceServiceAgreementIngest
            .setAdditions(additions);

        ServiceAgreement saved = new ServiceAgreement();
        saved.setId("save-id");

        LegalEntity root = createLegalEntity("2", null, "exId2", null, null);

        when(legalEntityJpaRepository.findDistinctByParentIsNull(eq(null)))
            .thenReturn(newArrayList(root));

        when(persistenceServiceAgreementService.create(argThat(
            getServiceAgreementMatcher(
                nullValue(),
                is(persistenceServiceAgreementIngest.getName()),
                is(persistenceServiceAgreementIngest.getDescription()),
                is(root),
                is(persistenceServiceAgreementIngest.getExternalId()),
                is(ServiceAgreementState.ENABLED),
                notNullValue(),
                is(additions),
                nullValue(),
                nullValue()
            )
        )))
            .thenReturn(saved);

        String serviceAgreement = serviceAgreementIngestService
            .ingestServiceAgreement(new ServiceAgreementData<>(persistenceServiceAgreementIngest, new HashMap<>()));

        assertThat(serviceAgreement, is(saved.getId()));
        verify(persistenceServiceAgreementService).create(serviceAgreementArgumentCaptor.capture());
        ServiceAgreement captorValue = serviceAgreementArgumentCaptor.getValue();
        assertEquals(persistenceServiceAgreementIngest.getIsMaster(), captorValue.isMaster());
    }

    @Test
    public void shouldThrowInternalServerErrorExceptionOnInvalidHierarchy() {
        Map<String, String> additions = new HashMap<>();
        additions.put("key", "value");
        ServiceAgreementIngestPostRequestBody persistenceServiceAgreementIngest = new ServiceAgreementIngestPostRequestBody()
            .withName("name")
            .withExternalId("external-id")
            .withStatus(CreateStatus.ENABLED)
            .withIsMaster(true)
            .withDescription("description")
            .withParticipantsToIngest(
                Sets.newHashSet(new ParticipantIngest()
                    .withExternalId("exId")
                    .withAdmins(
                        Sets.newHashSet("u1"))
                    .withUsers(
                        Sets.newHashSet("u1"))
                    .withSharingAccounts(true)
                    .withSharingUsers(true)
                )
            );
        persistenceServiceAgreementIngest
            .setAdditions(additions);

        LegalEntity exId = createLegalEntity("1", null, "exId", null, null);
        when(legalEntityJpaRepository.findDistinctByExternalIdIn(anyList(), isNull()))
            .thenReturn(newArrayList(exId));

        com.backbase.dbs.user.api.client.v2.model.GetUser user1 = new com.backbase.dbs.user.api.client.v2.model.GetUser();
        user1.setId("id1");
        user1.setLegalEntityId("1");
        user1.setExternalId("u1");

        InternalServerErrorException exception = assertThrows(InternalServerErrorException.class, () -> serviceAgreementIngestService
            .ingestServiceAgreement(new ServiceAgreementData<>(persistenceServiceAgreementIngest,
                Maps.toMap(Sets.newHashSet("u1"),
                    key -> user1))));

        assertEquals(ERR_ACC_062.getErrorMessage(), exception.getMessage());
    }

    @Test
    public void shouldThrowBadRequestOnInvalidUserLegalEntity() {
        Map<String, String> additions = new HashMap<>();
        additions.put("key", "value");
        ServiceAgreementIngestPostRequestBody persistenceServiceAgreementIngest = new ServiceAgreementIngestPostRequestBody()
            .withName("name")
            .withExternalId("external-id")
            .withStatus(CreateStatus.ENABLED)
            .withIsMaster(true)
            .withDescription("description")
            .withParticipantsToIngest(
                Sets.newHashSet(new ParticipantIngest()
                    .withExternalId("exId")
                    .withUsers(
                        Sets.newHashSet("u1")
                    )
                    .withSharingAccounts(true)
                    .withSharingUsers(true)
                )
            );
        persistenceServiceAgreementIngest
            .setAdditions(additions);

        LegalEntity exId = createLegalEntity("2", null, "exId", null, null);
        when(legalEntityJpaRepository.findDistinctByExternalIdIn(anyList(), isNull()))
            .thenReturn(newArrayList(exId));

        com.backbase.dbs.user.api.client.v2.model.GetUser user1 = new com.backbase.dbs.user.api.client.v2.model.GetUser();
        user1.setId("id1");
        user1.setLegalEntityId("1");
        user1.setExternalId("u1");

        BadRequestException exception = assertThrows(BadRequestException.class, () ->serviceAgreementIngestService
            .ingestServiceAgreement(new ServiceAgreementData<>(persistenceServiceAgreementIngest,
                Maps.toMap(Sets.newHashSet("u1"),
                    key ->user1))));

        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_057.getErrorMessage(), ERR_ACC_057.getErrorCode()));
    }

    @Test
    public void shouldThrowBadRequestOnInvalidAdminLegalEntity() {
        Map<String, String> additions = new HashMap<>();
        additions.put("key", "value");
        ServiceAgreementIngestPostRequestBody persistenceServiceAgreementIngest = new ServiceAgreementIngestPostRequestBody()
            .withName("name")
            .withExternalId("external-id")
            .withStatus(CreateStatus.ENABLED)
            .withIsMaster(true)
            .withDescription("description")
            .withParticipantsToIngest(
                Sets.newHashSet(new ParticipantIngest()
                    .withExternalId("exId")
                    .withAdmins(
                        Sets.newHashSet("u1")
                    )
                    .withSharingAccounts(true)
                    .withSharingUsers(true)
                )
            );
        persistenceServiceAgreementIngest
            .setAdditions(additions);

        LegalEntity exId = createLegalEntity("2", null, "exId", null, null);
        when(legalEntityJpaRepository.findDistinctByExternalIdIn(anyList(), isNull()))
            .thenReturn(newArrayList(exId));

        com.backbase.dbs.user.api.client.v2.model.GetUser user1 = new com.backbase.dbs.user.api.client.v2.model.GetUser();
        user1.setId("id1");
        user1.setLegalEntityId("1");
        user1.setExternalId("u1");

        BadRequestException exception = assertThrows(BadRequestException.class, () -> serviceAgreementIngestService
            .ingestServiceAgreement(new ServiceAgreementData<>(persistenceServiceAgreementIngest,
                Maps.toMap(Sets.newHashSet("u1"),
                    key -> user1))));

        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_058.getErrorMessage(), ERR_ACC_058.getErrorCode()));
    }

    @Test
    public void shouldThrowBadRequestOnMissingLegalEntities() {
        Map<String, String> additions = new HashMap<>();
        additions.put("key", "value");
        ServiceAgreementIngestPostRequestBody persistenceServiceAgreementIngest = new ServiceAgreementIngestPostRequestBody()
            .withName("name")
            .withExternalId("external-id")
            .withStatus(CreateStatus.ENABLED)
            .withIsMaster(true)
            .withDescription("description")
            .withParticipantsToIngest(
                Sets.newHashSet(new ParticipantIngest()
                    .withExternalId("exId")
                    .withSharingAccounts(true)
                    .withSharingUsers(true)
                )
            );
        persistenceServiceAgreementIngest
            .setAdditions(additions);

        BadRequestException exception = assertThrows(BadRequestException.class, () -> serviceAgreementIngestService
            .ingestServiceAgreement(new ServiceAgreementData<>(persistenceServiceAgreementIngest,
                new HashMap<>())));

        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_060.getErrorMessage(), ERR_ACC_060.getErrorCode()));
    }

    @Test
    public void shouldThrowBadRequestOnDuplicatedLegalEntities() {
        Map<String, String> additions = new HashMap<>();
        additions.put("key", "value");
        ServiceAgreementIngestPostRequestBody persistenceServiceAgreementIngest = new ServiceAgreementIngestPostRequestBody()
            .withName("name")
            .withExternalId("external-id")
            .withStatus(CreateStatus.ENABLED)
            .withIsMaster(true)
            .withDescription("description")
            .withParticipantsToIngest(
                Sets.newHashSet(new ParticipantIngest()
                        .withExternalId("exId")
                        .withSharingAccounts(true)
                        .withSharingUsers(false),
                    new ParticipantIngest()
                        .withExternalId("exId")
                        .withSharingAccounts(false)
                        .withSharingUsers(true)

                )
            );
        persistenceServiceAgreementIngest
            .setAdditions(additions);
        LegalEntity exId = createLegalEntity("2", null, "exId", null, null);
        when(legalEntityJpaRepository.findDistinctByExternalIdIn(anyList(), isNull()))
            .thenReturn(newArrayList(exId));

        BadRequestException exception = assertThrows(BadRequestException.class, () -> serviceAgreementIngestService
            .ingestServiceAgreement(new ServiceAgreementData<>(persistenceServiceAgreementIngest,
                new HashMap<>())));

        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_061.getErrorMessage(), ERR_ACC_061.getErrorCode()));
    }

    @Test
    public void shouldThrowErrorWhenBothIdentifierSetsAreProvided() {
        Map<String, String> additions = new HashMap<>();
        additions.put("key", "value");
        ServiceAgreementIngestPostRequestBody persistenceServiceAgreementIngest = new ServiceAgreementIngestPostRequestBody()
            .withName("name")
            .withExternalId("external-id")
            .withStatus(CreateStatus.ENABLED)
            .withIsMaster(true)
            .withDescription("description")
            .withAdminUserAps(new PresentationUserApsIdentifiers()
                .withIdIdentifiers(Sets.newHashSet(new BigDecimal(1))))
            .withRegularUserAps(new PresentationUserApsIdentifiers()
                .withIdIdentifiers(Sets.newHashSet(new BigDecimal(1)))
                .withNameIdentifiers(Sets.newHashSet("name")))
            .withParticipantsToIngest(
                Sets.newHashSet(new ParticipantIngest()
                    .withExternalId("exId")
                    .withSharingAccounts(true)
                    .withSharingUsers(true)
                )
            );
        persistenceServiceAgreementIngest
            .setAdditions(additions);

        ServiceAgreement saved = new ServiceAgreement();
        saved.setId("save-id");

        LegalEntity exId = createLegalEntity("1", null, "exId", null, null);
        LegalEntity root = createLegalEntity("2", null, "exId2", null, null);
        when(legalEntityJpaRepository.findDistinctByExternalIdIn(anyList(), isNull()))
            .thenReturn(newArrayList(exId));
        when(legalEntityJpaRepository.findDistinctByParentIsNull(eq(null)))
            .thenReturn(newArrayList(root));
        Participant value = new Participant();
        value.setLegalEntity(exId);
        value.setServiceAgreement(null);

        BadRequestException exception = assertThrows(BadRequestException.class, () -> serviceAgreementIngestService
            .ingestServiceAgreement(new ServiceAgreementData<>(persistenceServiceAgreementIngest,
                new HashMap<>())));

        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_095.getErrorMessage(), ERR_ACC_095.getErrorCode()));
    }

    @Test
    public void shouldThrowErrorWhenBothIdentifierSetsAreEmpty() {
        Map<String, String> additions = new HashMap<>();
        additions.put("key", "value");
        ServiceAgreementIngestPostRequestBody persistenceServiceAgreementIngest = new ServiceAgreementIngestPostRequestBody()
            .withName("name")
            .withExternalId("external-id")
            .withStatus(CreateStatus.ENABLED)
            .withIsMaster(true)
            .withDescription("description")
            .withAdminUserAps(new PresentationUserApsIdentifiers()
                .withIdIdentifiers(Sets.newHashSet(new BigDecimal(1))))
            .withRegularUserAps(new PresentationUserApsIdentifiers()
                .withIdIdentifiers(new HashSet<>())
                .withNameIdentifiers(new HashSet<>()))
            .withParticipantsToIngest(
                Sets.newHashSet(new ParticipantIngest()
                    .withExternalId("exId")
                    .withSharingAccounts(true)
                    .withSharingUsers(true)
                )
            );
        persistenceServiceAgreementIngest
            .setAdditions(additions);

        ServiceAgreement saved = new ServiceAgreement();
        saved.setId("save-id");

        LegalEntity exId = createLegalEntity("1", null, "exId", null, null);
        LegalEntity root = createLegalEntity("2", null, "exId2", null, null);
        when(legalEntityJpaRepository.findDistinctByExternalIdIn(anyList(), isNull()))
            .thenReturn(newArrayList(exId));
        when(legalEntityJpaRepository.findDistinctByParentIsNull(eq(null)))
            .thenReturn(newArrayList(root));
        Participant value = new Participant();
        value.setLegalEntity(exId);
        value.setServiceAgreement(null);

        BadRequestException exception = assertThrows(BadRequestException.class, () -> serviceAgreementIngestService
            .ingestServiceAgreement(new ServiceAgreementData<>(persistenceServiceAgreementIngest,
                new HashMap<>())));

        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_095.getErrorMessage(), ERR_ACC_095.getErrorCode()));
    }

    @Test
    public void shouldIngestCustomServiceAgreementWithCreatorLegalEntityField() {
        ServiceAgreementIngestPostRequestBody persistenceServiceAgreementIngest = new ServiceAgreementIngestPostRequestBody()
            .withName("name")
            .withExternalId("external-id")
            .withIsMaster(false)
            .withDescription("description")
            .withCreatorLegalEntity("creatorLegalEntityId");

        LegalEntity creatorLegalEntity = createLegalEntity("creatorLegalEntityId", null, "exId", null, null);
        when(legalEntityJpaRepository.findById(persistenceServiceAgreementIngest.getCreatorLegalEntity()))
            .thenReturn(Optional.of(creatorLegalEntity));

        ServiceAgreementData<ServiceAgreementIngestPostRequestBody> serviceAgreementData = new ServiceAgreementData(
            persistenceServiceAgreementIngest,
            Collections.emptyMap());

        when(permissionSetService.getAssignablePermissionSetsById(anySet(), anyBoolean())).thenReturn(new HashSet<>());
        when(persistenceServiceAgreementService.create(any(ServiceAgreement.class)))
            .thenReturn(mock(ServiceAgreement.class));
        serviceAgreementIngestService.ingestServiceAgreement(serviceAgreementData);

        verify(persistenceServiceAgreementService).create(serviceAgreementArgumentCaptor.capture());
        ServiceAgreement createdServiceAgreement = serviceAgreementArgumentCaptor.getValue();
        assertThat(createdServiceAgreement.isMaster(), is(false));
        assertThat(createdServiceAgreement.getCreatorLegalEntity().getId(), equalTo("creatorLegalEntityId"));
    }

    @Test
    public void shouldThrowNotFoundWhenCreatorLegalEntityDoesNotExist() {
        ServiceAgreementIngestPostRequestBody persistenceServiceAgreementIngest = new ServiceAgreementIngestPostRequestBody()
            .withExternalId("external-id")
            .withIsMaster(false)
            .withDescription("description")
            .withCreatorLegalEntity("creatorLegalEntityId");

        when(legalEntityJpaRepository.findById(persistenceServiceAgreementIngest.getCreatorLegalEntity()))
            .thenReturn(Optional.empty());

        ServiceAgreementData<ServiceAgreementIngestPostRequestBody> serviceAgreementData = new ServiceAgreementData(
            persistenceServiceAgreementIngest,
            Collections.emptyMap());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> serviceAgreementIngestService
            .ingestServiceAgreement(serviceAgreementData));
        assertThat(exception, new NotFoundErrorMatcher(ERR_ACQ_005.getErrorMessage(), ERR_ACQ_005.getErrorCode()));
    }
}