package com.backbase.accesscontrol.api.client.it.seviceagreement;

import static com.backbase.accesscontrol.util.constants.ResourceAndFunctionNameConstants.MANAGE_SERVICE_AGREEMENTS_FUNCTION_NAME;
import static com.backbase.accesscontrol.util.helpers.ResourceAndFunctionNameConstants.PRIVILEGE_CREATE;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

import com.backbase.accesscontrol.api.TestDbWireMock;
import com.backbase.accesscontrol.api.client.ServiceAgreementController;
import com.backbase.accesscontrol.domain.LegalEntity;
import com.backbase.accesscontrol.domain.enums.LegalEntityType;
import com.backbase.accesscontrol.util.helpers.DateFormatterUtil;
import com.backbase.accesscontrol.util.helpers.UrlBuilder;
import com.backbase.dbs.approval.api.client.v2.model.ApprovalDto;
import com.backbase.dbs.approval.api.client.v2.model.ApprovalStatus;
import com.backbase.dbs.approval.api.client.v2.model.PresentationPostApprovalResponse;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.CreateStatus;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.Participant;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementPostRequestBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementPostResponseBody;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

/**
 * Test for {@link ServiceAgreementController#postServiceAgreement}
 */
@TestPropertySource(properties = {
    "backbase.approval.validation.enabled=true",
    "backbase.approval.level.enabled=true"
})
public class CreateServiceAgreementWithApprovalIT extends TestDbWireMock {

    private static final String URL = "/accessgroups/serviceagreements";
    private static final String GET_USERS_URL = "/service-api/v2/users/bulk";
    private static final String POST_APPROVALS_URL = baseServiceUrl + "/approvals";

    private LegalEntity childLe;

    @Before
    public void setUp() {
        childLe = legalEntityJpaRepository.save(new LegalEntity()
            .withExternalId("child")
            .withName("child")
            .withParent(rootLegalEntity)
            .withType(LegalEntityType.BANK));
    }

    @Test
    public void testSuccessfulApprovalServiceAgreementPending() throws Exception {
        String approvalId = getUuid();
        HashSet<String> rootAdmins = newHashSet("admin1", "admin2");
        HashSet<String> childAdmins = newHashSet("admin3", "admin4");

        Participant validParticipant1 = new Participant()
            .withId(rootLegalEntity.getId())
            .withSharingAccounts(true).withSharingUsers(false)
            .withAdmins(rootAdmins);
        Participant validParticipant2 = new Participant()
            .withId(childLe.getId())
            .withSharingAccounts(false).withSharingUsers(true)
            .withAdmins(childAdmins);
        String name = "name";
        String description = "description";
        String externalIdKey = "externalId";
        String externalIdValue = "ex 123";

        LocalDateTime localDateTIme = LocalDateTime.now();
        Date from = Date.from(localDateTIme.minusDays(2).atZone(ZoneId.systemDefault()).toInstant());
        Date until = Date.from(localDateTIme.plusDays(2).atZone(ZoneId.systemDefault()).toInstant());

        ServiceAgreementPostRequestBody serviceAgreementPostRequestBody = createServiceAgreementPostRequestBody(
            name,
            description,
            asList(validParticipant1, validParticipant2),
            CreateStatus.DISABLED, from, until);
        serviceAgreementPostRequestBody.withAddition(externalIdKey, externalIdValue);

        ApprovalDto approval = new ApprovalDto()
            .id(approvalId)
            .userId("user")
            .serviceAgreementId(rootMsa.getId())
            .status(ApprovalStatus.PENDING)
            .itemId(UUID.randomUUID().toString())
            .resource("Entitlements")
            .function("Manage Service Agreements")
            .action("CREATE");
        PresentationPostApprovalResponse presentationPostApprovalResponse =
            new PresentationPostApprovalResponse().approval(approval);
        addStubPost(POST_APPROVALS_URL, presentationPostApprovalResponse, 201);

        com.backbase.dbs.user.api.client.v2.model.GetUsersList list = new com.backbase.dbs.user.api.client.v2.model.GetUsersList();

        com.backbase.dbs.user.api.client.v2.model.GetUser user = new com.backbase.dbs.user.api.client.v2.model.GetUser();
        user.setId("admin1");
        user.setLegalEntityId(rootLegalEntity.getId());

        com.backbase.dbs.user.api.client.v2.model.GetUser user2 = new com.backbase.dbs.user.api.client.v2.model.GetUser();
        user2.setId("admin2");
        user2.setLegalEntityId(rootLegalEntity.getId());

        com.backbase.dbs.user.api.client.v2.model.GetUser user3 = new com.backbase.dbs.user.api.client.v2.model.GetUser();
        user3.setId("admin3");
        user3.setLegalEntityId(childLe.getId());

        com.backbase.dbs.user.api.client.v2.model.GetUser user4 = new com.backbase.dbs.user.api.client.v2.model.GetUser();
        user4.setId("admin4");
        user4.setLegalEntityId(childLe.getId());

        list.addUsersItem(user);
        list.addUsersItem(user2);
        list.addUsersItem(user3);
        list.addUsersItem(user4);
        addStubGet(new UrlBuilder(GET_USERS_URL)
                .addQueryParameter("id", asList("admin1", "admin2", "admin3", "admin4")).build(),
            list, 200);

        ResponseEntity<String> responseEntity = executeClientRequestEntity(
            new UrlBuilder(URL).build(), HttpMethod.POST, serviceAgreementPostRequestBody, contextUserId,
            MANAGE_SERVICE_AGREEMENTS_FUNCTION_NAME,
            PRIVILEGE_CREATE);

        assertEquals(HttpStatus.ACCEPTED, responseEntity.getStatusCode());
        ServiceAgreementPostResponseBody serviceAgreementPostResponseBody = readValue(responseEntity.getBody(),
            ServiceAgreementPostResponseBody.class);

        assertEquals(approvalId, serviceAgreementPostResponseBody.getId());
    }

    @Test
    public void testSuccessfulServiceAgreementCreatedWithZeroPolicy() throws Exception {
        String approvalId = getUuid();
        HashSet<String> rootAdmins = newHashSet("admin1", "admin2");
        HashSet<String> childAdmins = newHashSet("admin3", "admin4");

        Participant validParticipant1 = new Participant()
            .withId(rootLegalEntity.getId())
            .withSharingAccounts(true).withSharingUsers(false)
            .withAdmins(rootAdmins);
        Participant validParticipant2 = new Participant()
            .withId(childLe.getId())
            .withSharingAccounts(false).withSharingUsers(true)
            .withAdmins(childAdmins);
        String name = "name";
        String description = "description";
        String externalIdKey = "externalId";
        String externalIdValue = "ex 123";

        LocalDateTime localDateTIme = LocalDateTime.now();
        Date from = Date.from(localDateTIme.minusDays(2).atZone(ZoneId.systemDefault()).toInstant());
        Date until = Date.from(localDateTIme.plusDays(2).atZone(ZoneId.systemDefault()).toInstant());

        ServiceAgreementPostRequestBody serviceAgreementPostRequestBody = createServiceAgreementPostRequestBody(
            name,
            description,
            asList(validParticipant1, validParticipant2),
            CreateStatus.DISABLED, from, until);
        serviceAgreementPostRequestBody.withAddition(externalIdKey, externalIdValue);

        ApprovalDto approval = new ApprovalDto()
            .id(approvalId)
            .userId("user")
            .serviceAgreementId(rootMsa.getId())
            .status(ApprovalStatus.APPROVED)
            .itemId(UUID.randomUUID().toString())
            .resource("Entitlements")
            .function("Manage Service Agreements")
            .action("CREATE");
        PresentationPostApprovalResponse presentationPostApprovalResponse =
            new PresentationPostApprovalResponse().approval(approval);
        addStubPost(POST_APPROVALS_URL, presentationPostApprovalResponse, 201);
        com.backbase.dbs.user.api.client.v2.model.GetUsersList list = new com.backbase.dbs.user.api.client.v2.model.GetUsersList();

        com.backbase.dbs.user.api.client.v2.model.GetUser user = new com.backbase.dbs.user.api.client.v2.model.GetUser();
        user.setId("admin1");
        user.setLegalEntityId(rootLegalEntity.getId());

        com.backbase.dbs.user.api.client.v2.model.GetUser user2 = new com.backbase.dbs.user.api.client.v2.model.GetUser();
        user2.setId("admin2");
        user2.setLegalEntityId(rootLegalEntity.getId());

        com.backbase.dbs.user.api.client.v2.model.GetUser user3 = new com.backbase.dbs.user.api.client.v2.model.GetUser();
        user3.setId("admin3");
        user3.setLegalEntityId(childLe.getId());

        com.backbase.dbs.user.api.client.v2.model.GetUser user4 = new com.backbase.dbs.user.api.client.v2.model.GetUser();
        user4.setId("admin4");
        user4.setLegalEntityId(childLe.getId());

        list.addUsersItem(user);
        list.addUsersItem(user2);
        list.addUsersItem(user3);
        list.addUsersItem(user4);
        addStubGet(new UrlBuilder(GET_USERS_URL)
                .addQueryParameter("id", asList("admin1", "admin2", "admin3", "admin4")).build(),
            list, 200);

        ResponseEntity<String> responseEntity = executeClientRequestEntity(
            new UrlBuilder(URL).build(), HttpMethod.POST, serviceAgreementPostRequestBody, contextUserId,
            MANAGE_SERVICE_AGREEMENTS_FUNCTION_NAME,
            PRIVILEGE_CREATE);

        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
    }

    private ServiceAgreementPostRequestBody createServiceAgreementPostRequestBody(String name, String description,
        List<Participant> participants, CreateStatus status, Date from, Date until) {
        return new ServiceAgreementPostRequestBody()
            .withName(name)
            .withDescription(description)
            .withStatus(status)
            .withValidFromDate(DateFormatterUtil.utcFormatDateOnly(from))
            .withValidFromTime(DateFormatterUtil.utcFormatTimeOnly(from))
            .withValidUntilDate(DateFormatterUtil.utcFormatDateOnly(until))
            .withValidUntilTime(DateFormatterUtil.utcFormatTimeOnly(until))
            .withParticipants(participants);
    }
}
