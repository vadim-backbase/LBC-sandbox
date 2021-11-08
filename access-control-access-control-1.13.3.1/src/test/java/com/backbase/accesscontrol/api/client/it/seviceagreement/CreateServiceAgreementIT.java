package com.backbase.accesscontrol.api.client.it.seviceagreement;

import static com.backbase.accesscontrol.domain.GraphConstants.SERVICE_AGREEMENT_EXTENDED_WITH_FUNCTION_GROUPS;
import static com.backbase.accesscontrol.util.constants.ResourceAndFunctionNameConstants.MANAGE_SERVICE_AGREEMENTS_FUNCTION_NAME;
import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_005;
import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_095;
import static com.backbase.accesscontrol.util.errorcodes.LegalEntityErrorCodes.ERR_AG_013;
import static com.backbase.accesscontrol.util.helpers.ResourceAndFunctionNameConstants.PRIVILEGE_CREATE;
import static com.backbase.pandp.accesscontrol.event.spec.v1.Action.ADD;
import static com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.CreateStatus.ENABLED;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import com.backbase.accesscontrol.api.TestDbWireMock;
import com.backbase.accesscontrol.api.client.ServiceAgreementController;
import com.backbase.accesscontrol.domain.LegalEntity;
import com.backbase.accesscontrol.domain.ServiceAgreement;
import com.backbase.accesscontrol.domain.enums.LegalEntityType;
import com.backbase.accesscontrol.matchers.BadRequestErrorMatcher;
import com.backbase.accesscontrol.matchers.ForbiddenErrorMatcher;
import com.backbase.accesscontrol.service.DateTimeService;
import com.backbase.accesscontrol.util.helpers.DateFormatterUtil;
import com.backbase.accesscontrol.util.helpers.UrlBuilder;
import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.buildingblocks.presentation.errors.ForbiddenException;
import com.backbase.pandp.accesscontrol.event.spec.v1.ServiceAgreementEvent;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.CreateStatus;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.Participant;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementPostRequestBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.Status;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;

/**
 * Test for {@link ServiceAgreementController#postServiceAgreement}
 */
public class CreateServiceAgreementIT extends TestDbWireMock {

    private static final String url = "/accessgroups/serviceagreements";
    private static final String GET_USERS_URL = "/service-api/v2/users/bulk";
    private LegalEntity childLe;

    @Autowired
    private DateTimeService dateTimeService;


    @Before
    public void setUp() {
        childLe = legalEntityJpaRepository.save(new LegalEntity()
            .withExternalId("child")
            .withName("child")
            .withParent(rootLegalEntity)
            .withType(LegalEntityType.BANK));
    }

    @Test
    public void testSuccessfulServiceAgreementCreated() throws Exception {
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

        executeClientRequest(
            new UrlBuilder(url).build(), HttpMethod.POST, serviceAgreementPostRequestBody, contextUserId,
            MANAGE_SERVICE_AGREEMENTS_FUNCTION_NAME,
            PRIVILEGE_CREATE);

        ServiceAgreement sa = serviceAgreementJpaRepository
            .findServiceAgreementsByName(serviceAgreementPostRequestBody.getName()).get(0);

        ServiceAgreement serviceAgreement = serviceAgreementJpaRepository
            .findById(sa.getId(), SERVICE_AGREEMENT_EXTENDED_WITH_FUNCTION_GROUPS).get();

        assertEquals(name, serviceAgreement.getName());
        assertEquals(DateFormatterUtil.utcFormatDateOnly(from),
            dateTimeService.getStringDateFromDate(serviceAgreement.getStartDate()));
        assertEquals(DateFormatterUtil.utcFormatDateOnly(until),
            dateTimeService.getStringDateFromDate(serviceAgreement.getEndDate()));
        assertEquals(DateFormatterUtil.utcFormatTimeOnly(from),
            dateTimeService.getStringTimeFromDate(serviceAgreement.getStartDate()));
        assertEquals(DateFormatterUtil.utcFormatTimeOnly(until),
            dateTimeService.getStringTimeFromDate(serviceAgreement.getEndDate()));

        assertEquals(description, serviceAgreement.getDescription());
        assertEquals(rootLegalEntity, serviceAgreement.getCreatorLegalEntity());
        assertEquals(Status.DISABLED.toString(), serviceAgreement.getState().toString());

        assertTrue(serviceAgreement.getParticipants().get(rootLegalEntity.getId()).getAdmins().keySet()
            .containsAll(rootAdmins));
        assertTrue(
            serviceAgreement.getParticipants().get(childLe.getId()).getAdmins().keySet().containsAll(childAdmins));
        verifyServiceAgreementEvents(Sets.newHashSet(new ServiceAgreementEvent()
            .withAction(ADD)
            .withId(serviceAgreement.getId())));
    }

    @Test
    public void shouldThrowBadRequestWhenTimePeriodOfServiceAgreementNotValid() {

        LocalDateTime localDateTIme = LocalDateTime.now();
        Date from = Date.from(localDateTIme.plusDays(3).atZone(ZoneId.systemDefault()).toInstant());
        Date until = Date.from(localDateTIme.plusDays(2).atZone(ZoneId.systemDefault()).toInstant());

        ServiceAgreementPostRequestBody postData = new ServiceAgreementPostRequestBody()
            .withDescription("desc")
            .withName("MSA")
            .withParticipants(singletonList(new Participant()
                .withId(rootLegalEntity.getId())
                .withSharingAccounts(true)
                .withSharingUsers(true)))
            .withValidFromDate(DateFormatterUtil.utcFormatDateOnly(from))
            .withValidUntilDate(DateFormatterUtil.utcFormatDateOnly(until))
            .withValidFromTime(DateFormatterUtil.utcFormatTimeOnly(from))
            .withValidUntilTime(DateFormatterUtil.utcFormatTimeOnly(until))
            .withStatus(ENABLED);

        BadRequestException exception = assertThrows(BadRequestException.class, () -> executeClientRequest(
            new UrlBuilder(url)
                .build(), HttpMethod.POST, postData, contextUserId,
            MANAGE_SERVICE_AGREEMENTS_FUNCTION_NAME,
            PRIVILEGE_CREATE));

        assertThat(exception, new BadRequestErrorMatcher(ERR_AG_095.getErrorMessage(), ERR_AG_095.getErrorCode()));
    }

    @Test
    public void testCreateServiceAgreementsWithParticipantsThatAreNotValidSubEntities() {
        Participant validParticipant1 = new Participant()
            .withId(rootLegalEntity.getId())
            .withSharingAccounts(true).withSharingUsers(false);
        Participant notValidParticipant = new Participant()
            .withId("f9400fb7a3b3ca4cafb8136b06bae123")
            .withSharingAccounts(false).withSharingUsers(false);

        String name = "name";
        ServiceAgreementPostRequestBody serviceAgreementPostRequestBody = createServiceAgreementPostRequestBody(
            name,
            "Service Agreement",
            asList(validParticipant1, notValidParticipant),
            CreateStatus.DISABLED, null, null);

        ForbiddenException exception = assertThrows(ForbiddenException.class, () -> executeClientRequest(
            new UrlBuilder(url)
                .build(), HttpMethod.POST, serviceAgreementPostRequestBody, contextUserId,
            MANAGE_SERVICE_AGREEMENTS_FUNCTION_NAME,
            PRIVILEGE_CREATE));

        assertThat(exception, new ForbiddenErrorMatcher(ERR_AG_013.getErrorMessage(), ERR_AG_013.getErrorCode()));
    }

    @Test
    public void testCreateServiceAgreementsWhenParticipantNotSharingUsersAndAccounts() throws Exception {
        Participant participant1 = new Participant()
            .withId(rootLegalEntity.getId())
            .withSharingAccounts(false).withSharingUsers(true);
        Participant participant2 = new Participant()
            .withId(childLe.getId())
            .withSharingAccounts(false).withSharingUsers(false);
        String name = "name";

        ServiceAgreementPostRequestBody serviceAgreementPostRequestBody = createServiceAgreementPostRequestBody(
            name,
            "Service Agreement",
            asList(participant1, participant2),
            CreateStatus.DISABLED, null, null);

        BadRequestException exception = assertThrows(BadRequestException.class, () -> executeClientRequest(
            new UrlBuilder(url)
                .build(), HttpMethod.POST, serviceAgreementPostRequestBody, contextUserId,
            MANAGE_SERVICE_AGREEMENTS_FUNCTION_NAME,
            PRIVILEGE_CREATE));
        assertThat(exception, new BadRequestErrorMatcher(ERR_AG_005.getErrorMessage(), ERR_AG_005.getErrorCode()));
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
