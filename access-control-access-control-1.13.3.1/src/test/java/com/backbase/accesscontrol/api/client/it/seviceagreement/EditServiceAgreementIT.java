package com.backbase.accesscontrol.api.client.it.seviceagreement;

import static com.backbase.accesscontrol.domain.GraphConstants.SERVICE_AGREEMENT_WITH_PARTICIPANTS_AND_CREATOR;
import static com.backbase.accesscontrol.service.impl.UserAccessPrivilegeService.ARRANGEMENTS;
import static com.backbase.accesscontrol.util.constants.ResourceAndFunctionNameConstants.MANAGE_SERVICE_AGREEMENTS_FUNCTION_NAME;
import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_005;
import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_008;
import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_032;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_054;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_068;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_069;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_070;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_086;
import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_036;
import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_037;
import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_039;
import static com.backbase.accesscontrol.util.helpers.ResourceAndFunctionNameConstants.PRIVILEGE_EDIT;
import static com.backbase.accesscontrol.util.helpers.ServiceAgreementUtil.createServiceAgreement;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import com.backbase.accesscontrol.api.TestDbWireMock;
import com.backbase.accesscontrol.api.client.ServiceAgreementsController;
import com.backbase.accesscontrol.domain.ApprovalDataGroupDetails;
import com.backbase.accesscontrol.domain.FunctionGroup;
import com.backbase.accesscontrol.domain.LegalEntity;
import com.backbase.accesscontrol.domain.ServiceAgreement;
import com.backbase.accesscontrol.domain.UserAssignedFunctionGroup;
import com.backbase.accesscontrol.domain.UserContext;
import com.backbase.accesscontrol.domain.enums.FunctionGroupType;
import com.backbase.accesscontrol.domain.enums.LegalEntityType;
import com.backbase.accesscontrol.matchers.BadRequestErrorMatcher;
import com.backbase.accesscontrol.matchers.ForbiddenErrorMatcher;
import com.backbase.accesscontrol.service.DateTimeService;
import com.backbase.accesscontrol.service.impl.ServiceAgreementAdminService;
import com.backbase.accesscontrol.util.helpers.DateFormatterUtil;
import com.backbase.accesscontrol.util.helpers.FunctionGroupUtil;
import com.backbase.accesscontrol.util.helpers.LegalEntityUtil;
import com.backbase.accesscontrol.util.helpers.ServiceAgreementUtil;
import com.backbase.accesscontrol.util.helpers.UrlBuilder;
import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.buildingblocks.presentation.errors.ForbiddenException;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.Participant;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementSave;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.Status;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Test for {@link ServiceAgreementsController#putServiceAgreementSave}
 */
public class EditServiceAgreementIT extends TestDbWireMock {

    private static final String url = "/accessgroups/service-agreements/";

    @Autowired
    private ServiceAgreementAdminService serviceAgreementAdminService;

    @Autowired
    private DateTimeService dateTimeService;

    private ServiceAgreement serviceAgreementCustom;
    private LegalEntity legalEntity;
    private LegalEntity legalEntity1;
    private LegalEntity legalEntity2;
    private FunctionGroup saveFg;
    private FunctionGroup saveFgSys;

    @Before
    public void setUp() {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);

        transactionTemplate.execute(transactionStatus -> {
            legalEntity = legalEntityJpaRepository
                .save(LegalEntityUtil.createLegalEntity("ex-id", "CORPCUST_1_1", rootLegalEntity));
            legalEntity1 = legalEntityJpaRepository
                .save(LegalEntityUtil.createLegalEntity("ex-id1", "CORPCUST_1_1_1", rootLegalEntity));
            legalEntity2 = legalEntityJpaRepository
                .save(LegalEntityUtil.createLegalEntity("ex-id2", "CORPCUST_2_1", rootLegalEntity));
            serviceAgreementCustom = createServiceAgreement("name", "external", "desc", rootLegalEntity, null,
                null);
            serviceAgreementCustom.setMaster(false);

            addParticipantToServiceAgreement(serviceAgreementCustom, legalEntity,
                Collections.singletonList("User1CORPCUST_1_1"),
                Lists.newArrayList("User1CORPCUST_1_1", "User2CORPCUST_1_1"), true, true);

            addParticipantToServiceAgreement(serviceAgreementCustom, rootLegalEntity,
                Collections.singletonList("admin"),
                Lists.newArrayList("admin", "User1BANK0001"), true, false);

            addParticipantToServiceAgreement(serviceAgreementCustom, legalEntity1,
                Collections.singletonList("User1CORPCUST_1_1_1"),
                Lists.newArrayList("User1CORPCUST_1_1_1", "User2CORPCUST_1_1_1"), true, true);

            addParticipantToServiceAgreement(serviceAgreementCustom, legalEntity2,
                Collections.emptyList(),
                Lists.newArrayList("User1CORPCUST_2_1", "User2CORPCUST_2_2"), true, false);

            serviceAgreementCustom = serviceAgreementJpaRepository.saveAndFlush(serviceAgreementCustom);

            FunctionGroup functionGroup = FunctionGroupUtil
                .getFunctionGroup(null, "fg-name", "fg-description", new HashSet<>(),
                    FunctionGroupType.DEFAULT,
                    serviceAgreementCustom);

            LocalDateTime localDateTIme = LocalDateTime.now();
            Date startDate = Date.from(localDateTIme.minusDays(2).atZone(ZoneId.systemDefault()).toInstant());
            Date endDate = Date.from(localDateTIme.plusDays(2).atZone(ZoneId.systemDefault()).toInstant());
            functionGroup.setStartDate(startDate);
            functionGroup.setEndDate(endDate);
            saveFg = functionGroupJpaRepository.save(functionGroup);

            return true;
        });
    }

    @Test
    public void shouldSuccessfullyUpdateParticipant() throws IOException, JSONException {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);

        transactionTemplate.execute(transactionStatus -> {
            FunctionGroup functionGroup = new FunctionGroup();
            functionGroup.setName("SYSTEM_FUNCTION_GROUP");
            functionGroup.setDescription("description");
            functionGroup.setType(FunctionGroupType.SYSTEM);
            functionGroup.setServiceAgreement(serviceAgreementCustom);
            functionGroupJpaRepository.save(functionGroup);
            return true;
        });

        Optional<FunctionGroup> system_function_group = functionGroupJpaRepository
            .findByNameAndServiceAgreementId("SYSTEM_FUNCTION_GROUP",
                serviceAgreementCustom.getId());
        UserContext userContext = new UserContext().withUserId("User1CORPCUST_1_1")
            .withServiceAgreementId(serviceAgreementCustom.getId());
        UserContext saveUserContext = userContextJpaRepository.save(userContext);
        UserContext userContext1 = new UserContext().withUserId("admin")
            .withServiceAgreementId(serviceAgreementCustom.getId());
        UserContext saveUserContext1 = userContextJpaRepository.save(userContext1);
        UserContext userContext2 = new UserContext().withUserId("User1CORPCUST_1_1_1")
            .withServiceAgreementId(serviceAgreementCustom.getId());
        UserContext saveUserContext2 = userContextJpaRepository.save(userContext2);
        UserAssignedFunctionGroup permission = new UserAssignedFunctionGroup()
            .withFunctionGroup(system_function_group.get())
            .withUserContext(saveUserContext);
        userAssignedFunctionGroupJpaRepository.saveAndFlush(permission);
        UserAssignedFunctionGroup permission1 = new UserAssignedFunctionGroup()
            .withFunctionGroup(system_function_group.get())
            .withUserContext(saveUserContext1);
        userAssignedFunctionGroupJpaRepository.saveAndFlush(permission1);
        UserAssignedFunctionGroup permission2 = new UserAssignedFunctionGroup()
            .withFunctionGroup(system_function_group.get())
            .withUserContext(saveUserContext2);
        userAssignedFunctionGroupJpaRepository.saveAndFlush(permission2);

        Participant participant = new Participant()
            .withId(legalEntity.getId())
            .withSharingAccounts(true)
            .withSharingUsers(true);

        Participant participant2 = new Participant()
            .withId(legalEntity2.getId())
            .withSharingAccounts(true)
            .withSharingUsers(false);

        HashSet<Participant> participants = new HashSet<>(asList(
            participant,
            participant2
        ));

        ServiceAgreementSave postData = getValidUpdateBody(serviceAgreementCustom)
            .withParticipants(participants);

        executeClientRequest(
            new UrlBuilder(url)
                .addPathParameter(serviceAgreementCustom.getId())
                .build(), HttpMethod.PUT, postData, contextUserId,
            MANAGE_SERVICE_AGREEMENTS_FUNCTION_NAME,
            PRIVILEGE_EDIT);

        transactionTemplate.execute(transactionStatus -> {
            ServiceAgreement sa = serviceAgreementJpaRepository.findById(serviceAgreementCustom.getId()).get();
            com.backbase.accesscontrol.domain.Participant changedRoleParticipant = sa.getParticipants()
                .get(legalEntity2.getId());
            assertFalse(changedRoleParticipant.isShareUsers());
            assertTrue(changedRoleParticipant.isShareAccounts());
            assertThat(changedRoleParticipant.getParticipantUsers(), hasSize(0));

            return true;
        });

    }

    @Test
    public void shouldThrowBadRequestIfExternalIdIsUpdatedAndIsNotUnique() {
        serviceAgreementJpaRepository.save(
            ServiceAgreementUtil
                .createServiceAgreement("new-sa", "new-sa-ext", "new-sa-desc", rootLegalEntity, null, null)
        );

        ServiceAgreementSave postData = getValidUpdateBody(serviceAgreementCustom)
            .withIsMaster(false)
            .withExternalId("new-sa-ext");

        BadRequestException exception = assertThrows(BadRequestException.class, () -> executeClientRequest(
            new UrlBuilder(url)
                .addPathParameter(serviceAgreementCustom.getId())
                .build(), HttpMethod.PUT, postData, contextUserId,
            MANAGE_SERVICE_AGREEMENTS_FUNCTION_NAME,
            PRIVILEGE_EDIT));
        assertThat(exception, new BadRequestErrorMatcher(ERR_ACQ_036.getErrorMessage(), ERR_ACQ_036.getErrorCode()));
    }

    @Test
    public void shouldThrowBadRequestIfIsMasterIsChanged() {
        ServiceAgreementSave postData = getValidUpdateBody(rootMsa)
            .withIsMaster(false);

        BadRequestException exception = assertThrows(BadRequestException.class, () -> executeClientRequest(
            new UrlBuilder(url)
                .addPathParameter(rootMsa.getId())
                .build(), HttpMethod.PUT, postData, contextUserId,
            MANAGE_SERVICE_AGREEMENTS_FUNCTION_NAME,
            PRIVILEGE_EDIT));
        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_068.getErrorMessage(), ERR_ACC_068.getErrorCode()));
    }

    @Test
    public void shouldThrowBadRequestExceptionWhenBelongingFunctionGroupsEndDateIsAfterEndDate() {
        LocalDateTime localDateTIme = LocalDateTime.now();
        Date startDate = Date.from(localDateTIme.minusDays(2).atZone(ZoneId.systemDefault()).toInstant());
        Date endDate = Date.from(localDateTIme.plusDays(1).atZone(ZoneId.systemDefault()).toInstant());

        ServiceAgreementSave postData = getValidUpdateBody(serviceAgreementCustom)
            .withValidFromDate(dateTimeService.getStringDateFromDate(startDate))
            .withValidUntilDate(dateTimeService.getStringDateFromDate(endDate))
            .withIsMaster(false);

        BadRequestException exception = assertThrows(BadRequestException.class, () -> executeClientRequest(
            new UrlBuilder(url)
                .addPathParameter(serviceAgreementCustom.getId())
                .build(), HttpMethod.PUT, postData, contextUserId,
            MANAGE_SERVICE_AGREEMENTS_FUNCTION_NAME,
            PRIVILEGE_EDIT));
        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_086.getErrorMessage(), ERR_ACC_086.getErrorCode()));
    }

    @Test
    public void shouldThrowBadRequestExceptionWhenBelongingFunctionGroupStartDateIsBeforeStartDate() {
        LocalDateTime localDateTIme = LocalDateTime.now();
        Date startDate = Date.from(localDateTIme.minusDays(1).atZone(ZoneId.systemDefault()).toInstant());
        Date endDate = Date.from(localDateTIme.plusDays(2).atZone(ZoneId.systemDefault()).toInstant());

        ServiceAgreementSave postData = getValidUpdateBody(serviceAgreementCustom)
            .withValidFromDate(dateTimeService.getStringDateFromDate(startDate))
            .withValidUntilDate(dateTimeService.getStringDateFromDate(endDate))
            .withIsMaster(false);

        BadRequestException exception = assertThrows(BadRequestException.class, () -> executeClientRequest(
            new UrlBuilder(url)
                .addPathParameter(serviceAgreementCustom.getId())
                .build(), HttpMethod.PUT, postData, contextUserId,
            MANAGE_SERVICE_AGREEMENTS_FUNCTION_NAME,
            PRIVILEGE_EDIT));
        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_086.getErrorMessage(), ERR_ACC_086.getErrorCode()));

    }

    @Test
    @SuppressWarnings("squid:S2699")
    public void testSuccessfulEditCustomServiceAgreement() throws Exception {

        ApprovalDataGroupDetails dg = new ApprovalDataGroupDetails();
        dg.setServiceAgreementId(serviceAgreementCustom.getId());
        dg.setType(ARRANGEMENTS);
        dg.setDescription("dg");
        dg.setName("khki");
        dg.setApprovalId("012345678901234567890123456789012345");
        approvalDataGroupJpaRepository.save(dg);
        Map<String, String> additions = new HashMap<>();
        String key = "externalId";
        String value = "123456789";
        additions.put(key, value);

        String name = "new name";
        String description = "new description";
        String externalId = "new external";
        Status updatedStatus = Status.ENABLED;

        Participant participant1 = new Participant()
            .withId(rootLegalEntity.getId())
            .withSharingAccounts(true)
            .withSharingUsers(false);
        Participant participant2 = new Participant()
            .withId(legalEntity.getId())
            .withSharingAccounts(false)
            .withSharingUsers(true);

        LocalDateTime localDateTIme = LocalDateTime.now();
        Date startDate = Date.from(localDateTIme.minusDays(3).atZone(ZoneId.systemDefault()).toInstant());
        Date endDate = Date.from(localDateTIme.plusDays(3).atZone(ZoneId.systemDefault()).toInstant());

        ServiceAgreementSave serviceAgreementSaveBody = new ServiceAgreementSave()
            .withName(name)
            .withDescription(description)
            .withExternalId(externalId)
            .withStatus(updatedStatus)
            .withValidFromDate(DateFormatterUtil.utcFormatDateOnly(startDate))
            .withValidFromTime(DateFormatterUtil.utcFormatTimeOnly(startDate))
            .withValidUntilDate(DateFormatterUtil.utcFormatDateOnly(endDate))
            .withValidUntilTime(DateFormatterUtil.utcFormatTimeOnly(endDate))
            .withParticipants(Sets.newHashSet(participant1, participant2));
        serviceAgreementSaveBody.setAdditions(additions);

        executeClientRequest(
            new UrlBuilder(url)
                .addPathParameter(serviceAgreementCustom.getId())
                .build(), HttpMethod.PUT, serviceAgreementSaveBody, contextUserId,
            MANAGE_SERVICE_AGREEMENTS_FUNCTION_NAME,
            PRIVILEGE_EDIT);

    }

    @Test
    public void shouldThrowForbiddenIfServiceAgreementDoesNotExist() {
        ServiceAgreementSave postData = getValidUpdateBody(serviceAgreementCustom);

        ForbiddenException exception = assertThrows(ForbiddenException.class, () -> executeClientRequest(
            new UrlBuilder(url)
                .addPathParameter("random")
                .build(), HttpMethod.PUT, postData, contextUserId,
            MANAGE_SERVICE_AGREEMENTS_FUNCTION_NAME,
            PRIVILEGE_EDIT));
        assertThat(exception, new ForbiddenErrorMatcher(ERR_AG_032.getErrorMessage(), ERR_AG_032.getErrorCode()));
    }

    @Test
    public void shouldThrowBadRequestIfParticipantIsChangedInMasterServiceAgreement() {
        HashSet<Participant> participants = new HashSet<>();
        participants.add(
            new Participant()
                .withId(
                    legalEntityJpaRepository.save(
                        LegalEntityUtil
                            .createLegalEntity(null, "le-name-2", "ex-id3-2", rootLegalEntity, LegalEntityType.BANK)
                    ).getId()
                )
                .withSharingAccounts(true)
                .withSharingUsers(true)
        );
        ServiceAgreementSave postData = getValidUpdateBody(rootMsa)
            .withIsMaster(true)
            .withParticipants(participants);

        BadRequestException exception = assertThrows(BadRequestException.class, () -> executeClientRequest(
            new UrlBuilder(url)
                .addPathParameter(rootMsa.getId())
                .build(), HttpMethod.PUT, postData, contextUserId,
            MANAGE_SERVICE_AGREEMENTS_FUNCTION_NAME,
            PRIVILEGE_EDIT));
        assertThat(exception, new BadRequestErrorMatcher(ERR_ACQ_037.getErrorMessage(), ERR_ACQ_037.getErrorCode()));
    }

    @Test
    public void shouldThrowBadRequestWhenRemovingParticipant() {
        UserContext userContext = new UserContext().withUserId("User1CORPCUST_1_1")
            .withServiceAgreementId(serviceAgreementCustom.getId());
        UserContext saveUserContext = userContextJpaRepository.save(userContext);
        UserAssignedFunctionGroup permission = new UserAssignedFunctionGroup()
            .withFunctionGroup(saveFg)
            .withUserContext(saveUserContext);
        userAssignedFunctionGroupJpaRepository.saveAndFlush(permission);

        HashSet<Participant> participants = new HashSet<>();
        Participant participant = new Participant()
            .withId(rootLegalEntity.getId())
            .withSharingAccounts(true)
            .withSharingUsers(true);
        participants.add(
            participant
        );

        ServiceAgreementSave postData = getValidUpdateBody(serviceAgreementCustom)
            .withParticipants(participants);

        BadRequestException exception = assertThrows(BadRequestException.class, () -> executeClientRequest(
            new UrlBuilder(url)
                .addPathParameter(serviceAgreementCustom.getId())
                .build(), HttpMethod.PUT, postData, contextUserId,
            MANAGE_SERVICE_AGREEMENTS_FUNCTION_NAME,
            PRIVILEGE_EDIT));
        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_054.getErrorMessage(), ERR_ACC_054.getErrorCode()));
    }

    @Test
    public void shouldThrowBadRequestIfTryingToDisableMasterServiceAgreementOfRootBank() {
        ServiceAgreementSave postData = getValidUpdateBody(rootMsa)
            .withIsMaster(true)
            .withStatus(Status.DISABLED);

        BadRequestException exception = assertThrows(BadRequestException.class, () -> executeClientRequest(
            new UrlBuilder(url)
                .addPathParameter(rootMsa.getId())
                .build(), HttpMethod.PUT, postData, contextUserId,
            MANAGE_SERVICE_AGREEMENTS_FUNCTION_NAME,
            PRIVILEGE_EDIT));
        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_070.getErrorMessage(), ERR_ACC_070.getErrorCode()));
    }

    @Test
    public void shouldThrowBadRequestExceptionIfNewParticipantIsNotInHierarchy() {
        LegalEntity legalEntityUnderBank = legalEntityJpaRepository.save(
            LegalEntityUtil
                .createLegalEntity(null, "le-name-under-bank", "le-under-bank", rootLegalEntity, LegalEntityType.BANK)
        );
        LegalEntity legalEntityUnderUnderBank = legalEntityJpaRepository.save(
            LegalEntityUtil
                .createLegalEntity(null, "le-name-under-under-bank", "le-under-under-bank", legalEntityUnderBank,
                    LegalEntityType.BANK)
        );

        ServiceAgreement customServiceAgreement = serviceAgreementJpaRepository.save(
            ServiceAgreementUtil
                .createServiceAgreement("custom-sa", "custom-sa", "desc.sa", legalEntityUnderBank,
                    legalEntityUnderUnderBank.getId(), legalEntityUnderUnderBank.getId())
        );

        HashSet<Participant> participants = new HashSet<>();
        participants.add(
            new Participant()
                .withId(legalEntityUnderUnderBank.getId())
                .withSharingAccounts(true)
                .withSharingUsers(true)
        );
        participants.add(
            new Participant()
                .withId(rootLegalEntity.getId())
                .withSharingAccounts(true)
                .withSharingUsers(true)
        );
        ServiceAgreementSave postData = new ServiceAgreementSave()
            .withName(customServiceAgreement.getName() + "-updated")
            .withDescription(customServiceAgreement.getDescription() + "-updated")
            .withExternalId(customServiceAgreement.getExternalId())
            .withIsMaster(false)
            .withStatus(Status.ENABLED)
            .withParticipants(participants);

        BadRequestException exception = assertThrows(BadRequestException.class, () -> executeClientRequest(
            new UrlBuilder(url)
                .addPathParameter(customServiceAgreement.getId())
                .build(), HttpMethod.PUT, postData, contextUserId,
            MANAGE_SERVICE_AGREEMENTS_FUNCTION_NAME,
            PRIVILEGE_EDIT));
        assertThat(exception, new BadRequestErrorMatcher(ERR_ACQ_039.getErrorMessage(), ERR_ACQ_039.getErrorCode()));
    }

    @Test
    public void shouldThrowBadRequestIfParticipantIsAddedInMasterServiceAgreement() {
        HashSet<Participant> participants = new HashSet<>();
        participants.add(
            new Participant()
                .withId(rootLegalEntity.getId())
                .withSharingAccounts(true)
                .withSharingUsers(true)
        );
        participants.add(
            new Participant()
                .withId(UUID.randomUUID().toString().replace("-", ""))
                .withSharingAccounts(true)
                .withSharingUsers(true)
        );
        ServiceAgreementSave postData = getValidUpdateBody(rootMsa)
            .withIsMaster(true)
            .withParticipants(participants);

        BadRequestException exception = assertThrows(BadRequestException.class, () -> executeClientRequest(
            new UrlBuilder(url)
                .addPathParameter(rootMsa.getId())
                .build(), HttpMethod.PUT, postData, contextUserId,
            MANAGE_SERVICE_AGREEMENTS_FUNCTION_NAME,
            PRIVILEGE_EDIT));
        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_069.getErrorMessage(), ERR_ACC_069.getErrorCode()));
    }

    @Test
    public void shouldThrowBadRequestIfOnlyParticipantThatSharesSomethingIsRemoved() {
        HashSet<Participant> participants = new HashSet<>();
        participants.add(
            new Participant()
                .withId(rootLegalEntity.getId())
                .withSharingAccounts(false)
                .withSharingUsers(true)
        );
        ServiceAgreementSave postData = getValidUpdateBody(serviceAgreementCustom)
            .withParticipants(participants);

        BadRequestException exception = assertThrows(BadRequestException.class, () -> executeClientRequest(
            new UrlBuilder(url)
                .addPathParameter(serviceAgreementCustom.getId())
                .build(), HttpMethod.PUT, postData, contextUserId,
            MANAGE_SERVICE_AGREEMENTS_FUNCTION_NAME,
            PRIVILEGE_EDIT));
        assertThat(exception, new BadRequestErrorMatcher(ERR_AG_008.getErrorMessage(), ERR_AG_008.getErrorCode()));
    }

    @Test
    public void shouldThrowBadRequestIfParticipantIsNotSharingAnything() {
        HashSet<Participant> participants = new HashSet<>();
        participants.add(
            new Participant()
                .withId(rootLegalEntity.getId())
                .withSharingAccounts(false)
                .withSharingUsers(false)
        );
        ServiceAgreementSave postData = getValidUpdateBody(serviceAgreementCustom)
            .withParticipants(participants);

        BadRequestException exception = assertThrows(BadRequestException.class, () -> executeClientRequest(
            new UrlBuilder(url)
                .addPathParameter(serviceAgreementCustom.getId())
                .build(), HttpMethod.PUT, postData, contextUserId,
            MANAGE_SERVICE_AGREEMENTS_FUNCTION_NAME,
            PRIVILEGE_EDIT));
        assertThat(exception, new BadRequestErrorMatcher(ERR_AG_005.getErrorMessage(), ERR_AG_005.getErrorCode()));
    }

    @Test
    public void shouldSuccessfullyAddParticipantInCustomServiceAgreement() throws Exception {
        LegalEntity legalEntityUnderBank = legalEntityJpaRepository.save(
            LegalEntityUtil
                .createLegalEntity(null, "le-name-under-bank", "le-under-bank", rootLegalEntity, LegalEntityType.BANK)
        );
        LegalEntity legalEntityUnderUnderBank = legalEntityJpaRepository.save(
            LegalEntityUtil
                .createLegalEntity(null, "le-name-under-under-bank", "le-under-under-bank", legalEntityUnderBank,
                    LegalEntityType.BANK)
        );

        ServiceAgreement customServiceAgreement = serviceAgreementJpaRepository.save(
            ServiceAgreementUtil
                .createServiceAgreement("custom-sa", "custom-sa", "desc.sa", legalEntityUnderBank,
                    legalEntityUnderUnderBank.getId(), legalEntityUnderUnderBank.getId())
        );

        HashSet<Participant> participants = new HashSet<>();
        participants.add(
            new Participant()
                .withId(legalEntityUnderBank.getId())
                .withSharingAccounts(true)
                .withSharingUsers(true)
        );
        ServiceAgreementSave postData = new ServiceAgreementSave()
            .withName(customServiceAgreement.getName() + "-updated")
            .withDescription(customServiceAgreement.getDescription() + "-updated")
            .withExternalId(customServiceAgreement.getExternalId())
            .withIsMaster(false)
            .withStatus(Status.ENABLED)
            .withParticipants(participants);

        Optional<ServiceAgreement> beforeUpdate = serviceAgreementJpaRepository
            .findById(serviceAgreementCustom.getId(), SERVICE_AGREEMENT_WITH_PARTICIPANTS_AND_CREATOR);
        assertEquals(4, beforeUpdate.get().getParticipants().size());
        executeClientRequest(
            new UrlBuilder(url)
                .addPathParameter(customServiceAgreement.getId())
                .build(), HttpMethod.PUT, postData, contextUserId,
            MANAGE_SERVICE_AGREEMENTS_FUNCTION_NAME,
            PRIVILEGE_EDIT);

        Optional<ServiceAgreement> afterUpdate = serviceAgreementJpaRepository
            .findById(customServiceAgreement.getId(), SERVICE_AGREEMENT_WITH_PARTICIPANTS_AND_CREATOR);
        assertEquals(1, afterUpdate.get().getParticipants().size());
    }

    @Test
    public void shouldRemoveAdmins() throws Exception {

        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);

        transactionTemplate.execute(transactionStatus -> {
            ServiceAgreement sa = serviceAgreementJpaRepository.findById(rootMsa.getId()).get();

            Participant participant = new Participant()
                .withId(rootLegalEntity.getId())
                .withAdmins(Sets.newHashSet("user-id"));

            serviceAgreementAdminService.updateAdmins(sa, Sets.newHashSet(participant));
            return true;
        });

        assertTrue(serviceAgreementAdminJpaRepository.existsByParticipantServiceAgreement(rootMsa));
        assertTrue(functionGroupJpaRepository.findByNameAndServiceAgreementId("SYSTEM_FUNCTION_GROUP",
            rootMsa.getId()).isPresent());

        ServiceAgreementSave postData = getValidUpdateBody(rootMsa)
            .withIsMaster(true);

        executeClientRequest(
            new UrlBuilder(url)
                .addPathParameter(rootMsa.getId())
                .build(), HttpMethod.PUT, postData, contextUserId,
            MANAGE_SERVICE_AGREEMENTS_FUNCTION_NAME,
            PRIVILEGE_EDIT);

        assertFalse(serviceAgreementAdminJpaRepository.existsByParticipantServiceAgreement(rootMsa));
        assertFalse(functionGroupJpaRepository.findByNameAndServiceAgreementId("SYSTEM_FUNCTION_GROUP",
            rootMsa.getId()).isPresent());

    }

    @Test
    public void shouldNotCreateSystemFunctionGroupWhenUpdatingAdminOnServiceAgreementWithoutAdmins() throws Exception {

        ServiceAgreement custom = ServiceAgreementUtil
            .createServiceAgreement("custom.sa", "exid.custom", "desc.sa", rootLegalEntity, null, null);
        custom.setMaster(false);
        custom = serviceAgreementJpaRepository.save(custom);
        assertFalse(serviceAgreementAdminJpaRepository.existsByParticipantServiceAgreement(custom));
        assertFalse(functionGroupJpaRepository.findByNameAndServiceAgreementId("SYSTEM_FUNCTION_GROUP",
            custom.getId()).isPresent());

        ServiceAgreementSave postData = getValidUpdateBody(custom);

        executeClientRequest(
            new UrlBuilder(url)
                .addPathParameter(custom.getId())
                .build(), HttpMethod.PUT, postData, contextUserId,
            MANAGE_SERVICE_AGREEMENTS_FUNCTION_NAME,
            PRIVILEGE_EDIT);

        assertFalse(serviceAgreementAdminJpaRepository.existsByParticipantServiceAgreement(custom));
        assertFalse(functionGroupJpaRepository.findByNameAndServiceAgreementId("SYSTEM_FUNCTION_GROUP",
            custom.getId()).isPresent());

    }

    private void addParticipantToServiceAgreement(ServiceAgreement serviceAgreement, LegalEntity providerLe,
        List<String> admins, List<String> users, boolean shareUsers, boolean shareAccounts) {
        com.backbase.accesscontrol.domain.Participant provider = new com.backbase.accesscontrol.domain.Participant();
        provider.setShareUsers(shareUsers);
        provider.setShareAccounts(shareAccounts);
        for (String adminId : admins) {
            provider.addAdmin(adminId);
        }
        if (users != null) {
            for (String userId : users) {
                provider.addParticipantUser(userId);
            }
        }
        serviceAgreement.addParticipant(provider, providerLe.getId(), shareUsers, shareAccounts);
    }

    private ServiceAgreementSave getValidUpdateBody(ServiceAgreement serviceAgreement) {
        HashSet<Participant> participants = new HashSet<>();
        participants.add(
            new Participant()
                .withId(rootLegalEntity.getId())
                .withSharingAccounts(true)
                .withSharingUsers(true)
        );
        return new ServiceAgreementSave()
            .withName(serviceAgreement.getName() + "-updated")
            .withDescription(serviceAgreement.getDescription() + "-updated")
            .withExternalId(serviceAgreement.getExternalId())
            .withIsMaster(false)
            .withStatus(Status.ENABLED)
            .withParticipants(participants);
    }

    private com.backbase.dbs.user.api.client.v2.model.GetUser createUser(String legalEntityId,
        String user1Participant1) {

        com.backbase.dbs.user.api.client.v2.model.GetUser user = new com.backbase.dbs.user.api.client.v2.model.GetUser();
        user.setId(user1Participant1);
        user.setLegalEntityId(legalEntityId);
        return user;
    }
}
