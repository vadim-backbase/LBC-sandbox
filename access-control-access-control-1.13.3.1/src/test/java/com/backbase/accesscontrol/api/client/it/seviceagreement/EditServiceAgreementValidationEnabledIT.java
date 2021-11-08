package com.backbase.accesscontrol.api.client.it.seviceagreement;

import static com.backbase.accesscontrol.service.impl.UserAccessPrivilegeService.ARRANGEMENTS;
import static com.backbase.accesscontrol.util.constants.ResourceAndFunctionNameConstants.MANAGE_SERVICE_AGREEMENTS_FUNCTION_NAME;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_056;
import static com.backbase.accesscontrol.util.helpers.ResourceAndFunctionNameConstants.PRIVILEGE_EDIT;
import static com.backbase.accesscontrol.util.helpers.ServiceAgreementUtil.createServiceAgreement;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertThrows;

import com.backbase.accesscontrol.api.TestDbWireMock;
import com.backbase.accesscontrol.domain.DataGroup;
import com.backbase.accesscontrol.domain.FunctionGroup;
import com.backbase.accesscontrol.domain.LegalEntity;
import com.backbase.accesscontrol.domain.ServiceAgreement;
import com.backbase.accesscontrol.domain.enums.FunctionGroupType;
import com.backbase.accesscontrol.matchers.BadRequestErrorMatcher;
import com.backbase.accesscontrol.util.helpers.DataGroupUtil;
import com.backbase.accesscontrol.util.helpers.FunctionGroupUtil;
import com.backbase.accesscontrol.util.helpers.LegalEntityUtil;
import com.backbase.accesscontrol.util.helpers.UrlBuilder;
import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.dbs.arrangement.api.client.v2.model.AccountArrangementsLegalEntities;
import com.backbase.dbs.arrangement.api.client.v2.model.AccountPresentationArrangementLegalEntityIds;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.Participant;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementSave;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.Status;
import com.google.common.collect.Sets;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.support.TransactionTemplate;

@TestPropertySource(properties = {
    "backbase.data-group.validation.enabled=true"
})
public class EditServiceAgreementValidationEnabledIT extends TestDbWireMock {

    private static final String GET_LEGAL_ENTITIES_URL = baseServiceUrl + "/arrangements/legalentities";

    private static final String url = "/accessgroups/service-agreements/";

    private ServiceAgreement serviceAgreementCustom;
    private LegalEntity legalEntity;

    @Before
    public void setUp() {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);

        transactionTemplate.execute(transactionStatus -> {
            legalEntity = legalEntityJpaRepository
                .save(LegalEntityUtil.createLegalEntity("ex-id", "le-name", rootLegalEntity));
            serviceAgreementCustom = createServiceAgreement("name", "external", "desc", rootLegalEntity, null,
                null);
            serviceAgreementCustom.setMaster(false);

            addParticipantToServiceAgreement(serviceAgreementCustom, rootLegalEntity, asList("user1", "user2"),
                asList("user1", "user2"), true, true);

            addParticipantToServiceAgreement(serviceAgreementCustom, legalEntity, asList("user3", "user2"),
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
    @SuppressWarnings("squid:S2699")
    public void shouldSuccessfullyRemoveParticipant() throws IOException, JSONException {

        DataGroup dataGroup = new DataGroup()
            .withServiceAgreement(serviceAgreementCustom)
            .withDescription("desc")
            .withName("name")
            .withDataItemIds(Sets.newHashSet("arrangement1"))
            .withDataItemType(ARRANGEMENTS);

        dataGroupJpaRepository.saveAndFlush(dataGroup);

        HashSet<Participant> participants = new HashSet<>();
        Participant participant = new Participant()
            .withId(legalEntity.getId())
            .withSharingAccounts(true)
            .withSharingUsers(true);
        participants.add(
            participant
        );

        AccountPresentationArrangementLegalEntityIds item = new AccountPresentationArrangementLegalEntityIds()
            .arrangementId("arrangement1")
            .legalEntityIds(singletonList(legalEntity.getId()));
        AccountArrangementsLegalEntities persistenceArrangementsLegalEntitiesBody = new AccountArrangementsLegalEntities()
            .arrangementsLegalEntities(singletonList(item));

        addStubGet(new UrlBuilder(GET_LEGAL_ENTITIES_URL)
                .addQueryParameter("arrangementIds", "arrangement1")
                .addQueryParameter("legalEntityIds", legalEntity.getId())
                .build(),
            persistenceArrangementsLegalEntitiesBody, 200);

        ServiceAgreementSave postData = getValidUpdateBody(serviceAgreementCustom)
            .withParticipants(participants);

        executeClientRequest(
            new UrlBuilder(url)
                .addPathParameter(serviceAgreementCustom.getId())
                .build(), HttpMethod.PUT, postData, contextUserId,
            MANAGE_SERVICE_AGREEMENTS_FUNCTION_NAME,
            PRIVILEGE_EDIT);
    }

    @Test
    public void shouldThrowBadRequestWhenRemovingParticipantWithDataItems() {
        HashSet<Participant> participants = new HashSet<>();
        Participant participant = new Participant()
            .withId(legalEntity.getId())
            .withSharingAccounts(true)
            .withSharingUsers(true);
        participants.add(
            participant
        );

        DataGroup dataGroup = DataGroupUtil
            .createDataGroup("dg name", "ARRANGEMENTS", "description", serviceAgreementCustom);
        dataGroup.setDataItemIds(Sets.newHashSet("item1"));
        dataGroupJpaRepository.save(dataGroup);

        AccountPresentationArrangementLegalEntityIds arrangement = new AccountPresentationArrangementLegalEntityIds()
            .legalEntityIds(singletonList(rootLegalEntity.getId()));

        AccountArrangementsLegalEntities persistenceArrangementsLegalEntitiesBody = new AccountArrangementsLegalEntities()
            .arrangementsLegalEntities(singletonList(arrangement));

        addStubGet(new UrlBuilder(GET_LEGAL_ENTITIES_URL)
                .addQueryParameter("arrangementIds", "item1")
                .addQueryParameter("legalEntityIds", legalEntity.getId())
                .build(),
            persistenceArrangementsLegalEntitiesBody, 200);

        ServiceAgreementSave postData = getValidUpdateBody(serviceAgreementCustom)
            .withParticipants(participants);
        BadRequestException exception = assertThrows(BadRequestException.class, () ->
            executeClientRequest(
                new UrlBuilder(url)
                    .addPathParameter(serviceAgreementCustom.getId())
                    .build(), HttpMethod.PUT, postData, contextUserId,
                MANAGE_SERVICE_AGREEMENTS_FUNCTION_NAME,
                PRIVILEGE_EDIT));
        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_056.getErrorMessage(), ERR_ACC_056.getErrorCode()));

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
}
