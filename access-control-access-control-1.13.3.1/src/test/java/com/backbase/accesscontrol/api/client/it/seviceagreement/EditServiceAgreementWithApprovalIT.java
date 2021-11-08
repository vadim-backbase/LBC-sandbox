package com.backbase.accesscontrol.api.client.it.seviceagreement;

import static com.backbase.accesscontrol.domain.GraphConstants.SERVICE_AGREEMENT_WITH_PARTICIPANTS_AND_CREATOR;
import static com.backbase.accesscontrol.util.constants.ResourceAndFunctionNameConstants.MANAGE_SERVICE_AGREEMENTS_FUNCTION_NAME;
import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_005;
import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_008;
import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_105;
import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_106;
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
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;

import com.backbase.accesscontrol.api.TestDbWireMock;
import com.backbase.accesscontrol.api.client.ServiceAgreementsController;
import com.backbase.accesscontrol.domain.ApprovalServiceAgreement;
import com.backbase.accesscontrol.domain.FunctionGroup;
import com.backbase.accesscontrol.domain.LegalEntity;
import com.backbase.accesscontrol.domain.ServiceAgreement;
import com.backbase.accesscontrol.domain.enums.FunctionGroupType;
import com.backbase.accesscontrol.domain.enums.LegalEntityType;
import com.backbase.accesscontrol.matchers.BadRequestErrorMatcher;
import com.backbase.accesscontrol.service.DateTimeService;
import com.backbase.accesscontrol.util.helpers.DateFormatterUtil;
import com.backbase.accesscontrol.util.helpers.FunctionGroupUtil;
import com.backbase.accesscontrol.util.helpers.LegalEntityUtil;
import com.backbase.accesscontrol.util.helpers.ServiceAgreementUtil;
import com.backbase.accesscontrol.util.helpers.UrlBuilder;
import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.dbs.approval.api.client.v2.model.ApprovalDto;
import com.backbase.dbs.approval.api.client.v2.model.ApprovalStatus;
import com.backbase.dbs.approval.api.client.v2.model.PresentationPostApprovalResponse;
import com.backbase.dbs.approval.api.client.v2.model.PutUpdateStatusRequest;
import com.backbase.dbs.user.api.client.v2.model.GetUser;
import com.backbase.dbs.user.api.client.v2.model.GetUsersList;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.Participant;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementSave;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.Status;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Test for {@link ServiceAgreementsController#putServiceAgreementSave}
 */
@TestPropertySource(properties = {
    "backbase.approval.validation.enabled=true",
    "backbase.approval.level.enabled=true"
})
public class EditServiceAgreementWithApprovalIT extends TestDbWireMock {

    private static final String URL = "/accessgroups/service-agreements/";
    private static final String GET_USERS_URL = "/service-api/v2/users/bulk";
    private static final String POST_APPROVALS_URL = baseServiceUrl + "/approvals";
    private static final String PUT_CANCEL_APPROVALS_URL = baseServiceUrl + "/approvals/{approvalId}/status";

    @Autowired
    private DateTimeService dateTimeService;

    private ServiceAgreement serviceAgreementCustom;

    String approvalId = "approvalId";
    LegalEntity legalEntity;

    @Before
    public void setUp() {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);

        transactionTemplate.execute(transactionStatus -> {
            legalEntity = legalEntityJpaRepository
                .save(LegalEntityUtil.createLegalEntity("ex-id", "le-name", rootLegalEntity));
            legalEntityJpaRepository.flush();
            serviceAgreementCustom = createServiceAgreement("name", "external", "desc", rootLegalEntity, null,
                null);
            serviceAgreementCustom.setMaster(false);

            addParticipantToServiceAgreement(serviceAgreementCustom, rootLegalEntity, asList("admin1", "admin2"),
                asList("user1", "user2"), true, true);

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
            functionGroupJpaRepository.save(functionGroup);
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

        mockApprovalService();
        mockCancelApprovalService();

        BadRequestException exception = assertThrows(BadRequestException.class, () -> executeClientRequest(
            new UrlBuilder(URL)
                .addPathParameter(serviceAgreementCustom.getId())
                .build(), HttpMethod.PUT, postData, contextUserId,
            MANAGE_SERVICE_AGREEMENTS_FUNCTION_NAME,
            PRIVILEGE_EDIT));

        assertThat(exception,
            is(new BadRequestErrorMatcher(ERR_ACQ_036.getErrorMessage(), ERR_ACQ_036.getErrorCode())));
    }

    @Test
    public void shouldThrowBadRequestIfIsMasterIsChanged() {
        mockApprovalService();
        mockCancelApprovalService();

        ServiceAgreementSave postData = getValidUpdateBody(rootMsa)
            .withIsMaster(false);

        BadRequestException exception = assertThrows(BadRequestException.class, () -> executeClientRequest(
            new UrlBuilder(URL)
                .addPathParameter(rootMsa.getId())
                .build(), HttpMethod.PUT, postData, contextUserId,
            MANAGE_SERVICE_AGREEMENTS_FUNCTION_NAME,
            PRIVILEGE_EDIT));
        assertThat(exception,
            is(new BadRequestErrorMatcher(ERR_ACC_068.getErrorMessage(), ERR_ACC_068.getErrorCode())));
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

        mockApprovalService();
        mockCancelApprovalService();

        BadRequestException exception = assertThrows(BadRequestException.class, () -> executeClientRequest(
            new UrlBuilder(URL)
                .addPathParameter(serviceAgreementCustom.getId())
                .build(), HttpMethod.PUT, postData, contextUserId,
            MANAGE_SERVICE_AGREEMENTS_FUNCTION_NAME,
            PRIVILEGE_EDIT));

        assertThat(exception,
            is(new BadRequestErrorMatcher(ERR_ACC_086.getErrorMessage(), ERR_ACC_086.getErrorCode())));
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

        mockApprovalService();
        mockCancelApprovalService();

        BadRequestException exception = assertThrows(BadRequestException.class, () -> executeClientRequest(
            new UrlBuilder(URL)
                .addPathParameter(serviceAgreementCustom.getId())
                .build(), HttpMethod.PUT, postData, contextUserId,
            MANAGE_SERVICE_AGREEMENTS_FUNCTION_NAME,
            PRIVILEGE_EDIT));

        assertThat(exception,
            is(new BadRequestErrorMatcher(ERR_ACC_086.getErrorMessage(), ERR_ACC_086.getErrorCode())));
    }

    @Test
    public void testSuccessfulEditCustomServiceAgreement() throws Exception {
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
            .withSharingUsers(true)
            .withAdmins(Sets.newHashSet("admin1"));

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
            .withParticipants(Sets.newHashSet(participant1));
        serviceAgreementSaveBody.setAdditions(additions);

        GetUser user = new GetUser();
        user.setId("admin1");
        user.setLegalEntityId(rootLegalEntity.getId());
        GetUsersList list = new GetUsersList();
        list.setUsers(Lists.newArrayList(user));

        addStubGet(new UrlBuilder(GET_USERS_URL)
                .addQueryParameter("id", "admin1")
                .build(),
            list, 200);

        mockApprovalService();

        ResponseEntity<String> responseEntity = executeClientRequestEntity(
            new UrlBuilder(URL)
                .addPathParameter(serviceAgreementCustom.getId())
                .build(), HttpMethod.PUT, serviceAgreementSaveBody, contextUserId,
            MANAGE_SERVICE_AGREEMENTS_FUNCTION_NAME,
            PRIVILEGE_EDIT);

        assertEquals(HttpStatus.ACCEPTED, responseEntity.getStatusCode());

        List<ApprovalServiceAgreement> approvalServiceAgreements = approvalServiceAgreementJpaRepository.findAll();

        assertEquals(1, approvalServiceAgreements.size());
        ApprovalServiceAgreement approvalServiceAgreement = approvalServiceAgreements.get(0);

        assertEquals(name, approvalServiceAgreement.getName());
        assertEquals(description, approvalServiceAgreement.getDescription());
        assertEquals(updatedStatus.toString(), approvalServiceAgreement.getState().toString());
        assertEquals(externalId, approvalServiceAgreement.getExternalId());
        assertEquals(rootLegalEntity.getId(), approvalServiceAgreement.getCreatorLegalEntityId());
        assertFalse(approvalServiceAgreement.isMaster());
        assertEquals(approvalId, approvalServiceAgreement.getApprovalId());
        assertEquals(serviceAgreementCustom.getId(), approvalServiceAgreement.getServiceAgreementId());
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

        mockApprovalService();
        mockCancelApprovalService();

        BadRequestException exception = assertThrows(BadRequestException.class, () -> executeClientRequest(
            new UrlBuilder(URL)
                .addPathParameter(rootMsa.getId())
                .build(), HttpMethod.PUT, postData, contextUserId,
            MANAGE_SERVICE_AGREEMENTS_FUNCTION_NAME,
            PRIVILEGE_EDIT));

        assertThat(exception, new BadRequestErrorMatcher(ERR_ACQ_037.getErrorMessage(), ERR_ACQ_037.getErrorCode()));
    }

    @Test
    public void shouldThrowBadRequestIfTryingToDisableMasterServiceAgreementOfRootBank() {
        ServiceAgreementSave postData = getValidUpdateBody(rootMsa)
            .withIsMaster(true)
            .withStatus(Status.DISABLED);

        mockApprovalService();
        mockCancelApprovalService();

        BadRequestException exception = assertThrows(BadRequestException.class, () -> executeClientRequest(
            new UrlBuilder(URL)
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

        mockApprovalService();
        mockCancelApprovalService();

        BadRequestException exception = assertThrows(BadRequestException.class, () -> executeClientRequest(
            new UrlBuilder(URL)
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

        mockApprovalService();
        mockCancelApprovalService();

        BadRequestException exception = assertThrows(BadRequestException.class, () -> executeClientRequest(
            new UrlBuilder(URL)
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

        mockApprovalService();
        mockCancelApprovalService();

        BadRequestException exception = assertThrows(BadRequestException.class, () -> executeClientRequest(
            new UrlBuilder(URL)
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

        mockApprovalService();
        mockCancelApprovalService();

        BadRequestException exception = assertThrows(BadRequestException.class, () -> executeClientRequest(
            new UrlBuilder(URL)
                .addPathParameter(serviceAgreementCustom.getId())
                .build(), HttpMethod.PUT, postData, contextUserId,
            MANAGE_SERVICE_AGREEMENTS_FUNCTION_NAME,
            PRIVILEGE_EDIT));
        assertThat(exception, new BadRequestErrorMatcher(ERR_AG_005.getErrorMessage(), ERR_AG_005.getErrorCode()));

    }

    @Test
    public void shouldThrowBadRequestIfThereIsAlreadyPendingUpdate() {
        ApprovalServiceAgreement approvalServiceAgreement = new ApprovalServiceAgreement();
        approvalServiceAgreement.setServiceAgreementId(serviceAgreementCustom.getId());
        approvalServiceAgreement.withApprovalId(approvalId);
        approvalServiceAgreement.setName("New name");
        approvalServiceAgreement.setDescription("New description");
        approvalServiceAgreement.setCreatorLegalEntityId(rootLegalEntity.getId());

        approvalServiceAgreementJpaRepository.save(approvalServiceAgreement);

        ServiceAgreementSave postData = getValidUpdateBody(serviceAgreementCustom);

        mockApprovalService();
        mockCancelApprovalService();

        BadRequestException exception = assertThrows(BadRequestException.class, () -> executeClientRequest(
            new UrlBuilder(URL)
                .addPathParameter(serviceAgreementCustom.getId())
                .build(), HttpMethod.PUT, postData, contextUserId,
            MANAGE_SERVICE_AGREEMENTS_FUNCTION_NAME,
            PRIVILEGE_EDIT));
        assertThat(exception, new BadRequestErrorMatcher(ERR_AG_105.getErrorMessage(), ERR_AG_105.getErrorCode()));
    }

    @Test
    public void shouldThrowBadRequestIfThereIsAlreadyPendingUpdateWithSpecifiedExternalId() {
        ApprovalServiceAgreement approvalServiceAgreement = new ApprovalServiceAgreement();
        approvalServiceAgreement.setServiceAgreementId(rootMsa.getId());
        approvalServiceAgreement.withApprovalId(approvalId);
        approvalServiceAgreement.setName("New name");
        approvalServiceAgreement.setDescription("New description");
        approvalServiceAgreement.setExternalId("externalId");
        approvalServiceAgreement.setCreatorLegalEntityId(rootLegalEntity.getId());

        approvalServiceAgreementJpaRepository.save(approvalServiceAgreement);

        ServiceAgreementSave postData = getValidUpdateBody(serviceAgreementCustom)
            .withExternalId("externalId");

        mockApprovalService();
        mockCancelApprovalService();

        BadRequestException exception = assertThrows(BadRequestException.class, () -> executeClientRequest(
            new UrlBuilder(URL)
                .addPathParameter(serviceAgreementCustom.getId())
                .build(), HttpMethod.PUT, postData, contextUserId,
            MANAGE_SERVICE_AGREEMENTS_FUNCTION_NAME,
            PRIVILEGE_EDIT));
        assertThat(exception, new BadRequestErrorMatcher(ERR_AG_106.getErrorMessage(), ERR_AG_106.getErrorCode()));
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
        participants.add(
            new Participant()
                .withId(legalEntityUnderUnderBank.getId())
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
        assertEquals(1, beforeUpdate.get().getParticipants().size());

        mockApprovalService();

        ResponseEntity<String> responseEntity = executeClientRequestEntity(
            new UrlBuilder(URL)
                .addPathParameter(customServiceAgreement.getId())
                .build(), HttpMethod.PUT, postData, contextUserId,
            MANAGE_SERVICE_AGREEMENTS_FUNCTION_NAME,
            PRIVILEGE_EDIT);

        assertEquals(HttpStatus.ACCEPTED, responseEntity.getStatusCode());
    }

    @Test
    public void testSuccessfulEditCustomServiceAgreementWithZeroApprovalPolicy() throws Exception {

        Map<String, String> additions = new HashMap<>();
        String key = "externalId";
        String value = "123456789";
        additions.put(key, value);

        String name = "new name";
        String description = "new description";
        String externalId = "new external";
        Status updatedStatus = Status.ENABLED;

        Participant participant1 = new Participant()
            .withId(legalEntity.getId())
            .withSharingAccounts(true)
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
            .withParticipants(Sets.newHashSet(participant1));
        serviceAgreementSaveBody.setAdditions(additions);

        ApprovalDto approval = new ApprovalDto()
            .id(approvalId)
            .userId("user")
            .serviceAgreementId(rootMsa.getId())
            .status(ApprovalStatus.APPROVED)
            .itemId(UUID.randomUUID().toString())
            .resource("Entitlements")
            .function("Manage Service Agreements")
            .action("EDIT");
        PresentationPostApprovalResponse presentationPostApprovalResponse =
            new PresentationPostApprovalResponse().approval(approval);
        addStubPost(POST_APPROVALS_URL, presentationPostApprovalResponse, 201);

        ResponseEntity<String> responseEntity = executeClientRequestEntity(
            new UrlBuilder(URL)
                .addPathParameter(serviceAgreementCustom.getId())
                .build(), HttpMethod.PUT, serviceAgreementSaveBody, contextUserId,
            MANAGE_SERVICE_AGREEMENTS_FUNCTION_NAME,
            PRIVILEGE_EDIT);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        ServiceAgreement serviceAgreement =
            serviceAgreementJpaRepository.findById(serviceAgreementCustom.getId()).get();

        assertNotNull(serviceAgreement);
        assertEquals(name, serviceAgreement.getName());
        assertEquals(description, serviceAgreement.getDescription());
        assertEquals(updatedStatus.toString(), serviceAgreement.getState().toString());
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

    private void mockApprovalService() {
        ApprovalDto approval = new ApprovalDto()
            .id(approvalId)
            .userId("user")
            .serviceAgreementId(rootMsa.getId())
            .status(ApprovalStatus.PENDING)
            .itemId(UUID.randomUUID().toString())
            .resource("Entitlements")
            .function("Manage Service Agreements")
            .action("EDIT");
        PresentationPostApprovalResponse presentationPostApprovalResponse =
            new PresentationPostApprovalResponse().approval(approval);
        addStubPost(POST_APPROVALS_URL, presentationPostApprovalResponse, 201);
    }

    private void mockCancelApprovalService() {
        addStubPutEqualToJson(new UrlBuilder(PUT_CANCEL_APPROVALS_URL).addPathParameter(approvalId).build(), null, 200,
            new PutUpdateStatusRequest().status(ApprovalStatus.CANCELLED));
    }
}
