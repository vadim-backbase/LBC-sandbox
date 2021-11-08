package com.backbase.accesscontrol.api.service.it.datagroup;

import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_001;
import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_098;
import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_077;
import static com.backbase.accesscontrol.util.helpers.DataGroupUtil.createDataGroup;
import static com.backbase.accesscontrol.util.helpers.LegalEntityUtil.createLegalEntity;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import com.backbase.accesscontrol.api.TestDbWireMock;
import com.backbase.accesscontrol.domain.DataGroup;
import com.backbase.accesscontrol.domain.LegalEntity;
import com.backbase.accesscontrol.domain.Participant;
import com.backbase.accesscontrol.domain.ServiceAgreement;
import com.backbase.accesscontrol.domain.enums.LegalEntityType;
import com.backbase.accesscontrol.matchers.BadRequestErrorMatcher;
import com.backbase.accesscontrol.util.helpers.DataGroupUtil;
import com.backbase.accesscontrol.util.helpers.UrlBuilder;
import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.dbs.arrangement.api.client.v2.model.AccountInternalIdGetResponseBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.LegalEntityIdentifier;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.PresentationItemIdentifier;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.datagroups.PresentationGetDataGroupsRequest;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.datagroups.PresentationServiceAgreementWithDataGroups;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.datagroups.SharesEnum;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.PresentationServiceAgreementIdentifier;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Sets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

public class SearchDataGroupIT extends TestDbWireMock {

    private static final String url = "/accessgroups/data-groups/type/{type}/search";

    private static final String getArrangementInternalIdUrl = baseServiceUrl
        + "/arrangements/internal/%s";

    private LegalEntity legalEntity;
    private ServiceAgreement serviceAgreement;
    private ServiceAgreement serviceAgreementWithParticipant;
    private ServiceAgreement serviceAgreementWithParticipant2;
    private ServiceAgreement serviceAgreementWithParticipant3;
    private DataGroup otherDataGroupServiceAgreement;
    private DataGroup otherDataGroupServiceAgreement2;
    private DataGroup otherDataGroupServiceAgreement3;
    private DataGroup dataGroup1;
    private DataGroup dataGroup2;
    private String dataItem1 = getUuid();
    private String dataItem2 = getUuid();
    private String dataItem3 = getUuid();

    @Before
    public void setUp() throws Exception {
        legalEntity = rootLegalEntity;
        legalEntity = legalEntityJpaRepository.save(legalEntity);
        serviceAgreement = rootMsa;

        dataGroup1 = DataGroupUtil.createDataGroup("dg-name", "ARRANGEMENTS", "desc", serviceAgreement);
        dataGroup1.setDataItemIds(Collections.singleton(dataItem1));
        dataGroup1 = dataGroupJpaRepository.save(dataGroup1);
        dataGroup2 = DataGroupUtil.createDataGroup("dg-name-2", "ARRANGEMENTS", "desc", serviceAgreement);
        dataGroup2.setDataItemIds(Sets.newHashSet(dataItem2, dataItem3));
        dataGroupJpaRepository.save(dataGroup2);

        LegalEntity legalEntity = createLegalEntity(null, "EX1", "Backbase", null, LegalEntityType.BANK);
        legalEntityJpaRepository.save(legalEntity);

        LegalEntity otherLegalEntity = createLegalEntity(null, "EX2", "Backbase1", null, LegalEntityType.BANK);
        legalEntityJpaRepository.save(otherLegalEntity);
        serviceAgreementWithParticipant = createSAWithParticipants("name3", "description", "ext-id", otherLegalEntity,
            legalEntity, SharesEnum.ACCOUNTS);

        serviceAgreementJpaRepository.save(serviceAgreementWithParticipant);

        otherDataGroupServiceAgreement = createDataGroup("otherDataGroupServiceAgreement",
                "ARRANGEMENTS",
                "arrangment10",
                serviceAgreementWithParticipant);
        dataGroupJpaRepository.save(otherDataGroupServiceAgreement);

        LegalEntity otherLegalEntity2 = createLegalEntity(null, "EX3", "Backbase2", null, LegalEntityType.BANK);
        legalEntityJpaRepository.save(otherLegalEntity2);
        serviceAgreementWithParticipant2 = createSAWithParticipants("name4", "description", "ext-id2",
            otherLegalEntity2, legalEntity, SharesEnum.USERS);
        serviceAgreementJpaRepository.save(serviceAgreementWithParticipant2);

        otherDataGroupServiceAgreement2 = createDataGroup("otherDataGroupServiceAgreement2",
            "ARRANGEMENTS",
            "arrangment10",
            serviceAgreementWithParticipant2);
        dataGroupJpaRepository.save(otherDataGroupServiceAgreement2);

        LegalEntity otherLegalEntity3 = createLegalEntity(null, "EX4", "Backbase3", null, LegalEntityType.BANK);
        legalEntityJpaRepository.save(otherLegalEntity3);
        serviceAgreementWithParticipant3 = createSAWithParticipants("name5", "description", "ext-id3",
            otherLegalEntity3, legalEntity, SharesEnum.USERSANDACCOUNTS);
        serviceAgreementJpaRepository.save(serviceAgreementWithParticipant3);

        otherDataGroupServiceAgreement3 = createDataGroup("otherDataGroupServiceAgreement3",
            "ARRANGEMENTS",
            "arrangment10",
            serviceAgreementWithParticipant3);
        dataGroupJpaRepository.save(otherDataGroupServiceAgreement3);
    }

    @Test
    public void shouldReturnBadRequestForInvalidType() {
        PresentationGetDataGroupsRequest request = new PresentationGetDataGroupsRequest();

        BadRequestException exception = assertThrows(BadRequestException.class, () -> executeRequest(
            new UrlBuilder(url)
                .addPathParameter("INVALID")
                .build(),
            request,
            HttpMethod.POST
        ));

        assertThat(exception, new BadRequestErrorMatcher(ERR_AG_001.getErrorMessage(), ERR_AG_001.getErrorCode()));
    }

    @Test
    public void shouldReturnResultWhenLegalEntityIdentifierNotNullAndShareAccountsByDefault() throws Exception {

        String type = "ARRANGEMENTS";

        PresentationGetDataGroupsRequest request = new PresentationGetDataGroupsRequest()
                .withLegalEntityIdentifier(new LegalEntityIdentifier()
                .externalIdIdentifier("Backbase"));


        String responseAsString = executeRequest(
            new UrlBuilder(url)
                .addPathParameter(type)
                .build(),
            request,
            HttpMethod.POST
        );

        List<PresentationServiceAgreementWithDataGroups> response = readValue(
            responseAsString,
            new TypeReference<List<PresentationServiceAgreementWithDataGroups>>() {
            });

        assertEquals(2, response.size());
        assertThat(Arrays.asList("name3", "name5"), containsInAnyOrder(response.get(0).getServiceAgreement().getName(),
            response.get(1).getServiceAgreement().getName()));
        assertThat(Arrays.asList("ext-id", "ext-id3"),
            containsInAnyOrder(response.get(0).getServiceAgreement().getExternalId(),
                response.get(1).getServiceAgreement().getExternalId()));
    }

    @Test
    public void shouldReturnResultWhenLegalEntityIdentifierNotNullAndShareAccounts() throws Exception {

        String type = "ARRANGEMENTS";

        PresentationGetDataGroupsRequest request = new PresentationGetDataGroupsRequest()
            .withLegalEntityIdentifier(new LegalEntityIdentifier()
                .externalIdIdentifier("Backbase").withShares(SharesEnum.ACCOUNTS));

        String responseAsString = executeRequest(
            new UrlBuilder(url)
                .addPathParameter(type)
                .build(),
            request,
            HttpMethod.POST
        );
        List<PresentationServiceAgreementWithDataGroups> response = readValue(
            responseAsString,
            new TypeReference<List<PresentationServiceAgreementWithDataGroups>>() {
            });

        assertEquals(2, response.size());
        assertThat(Arrays.asList("name3", "name5"), containsInAnyOrder(response.get(0).getServiceAgreement().getName(),
            response.get(1).getServiceAgreement().getName()));
        assertThat(Arrays.asList("ext-id", "ext-id3"),
            containsInAnyOrder(response.get(0).getServiceAgreement().getExternalId(),
                response.get(1).getServiceAgreement().getExternalId()));
    }

    @Test
    public void shouldReturnResultWhenLegalEntityIdentifierNotNullAndShareUsers() throws Exception {

        String type = "ARRANGEMENTS";

        PresentationGetDataGroupsRequest request = new PresentationGetDataGroupsRequest()
            .withLegalEntityIdentifier(new LegalEntityIdentifier()
                .externalIdIdentifier("Backbase").withShares(SharesEnum.USERS));

        String responseAsString = executeRequest(
            new UrlBuilder(url)
                .addPathParameter(type)
                .build(),
            request,
            HttpMethod.POST
        );

        List<PresentationServiceAgreementWithDataGroups> response = readValue(
            responseAsString,
            new TypeReference<List<PresentationServiceAgreementWithDataGroups>>() {
            });

        assertEquals(2, response.size());
        assertThat(Arrays.asList("name4", "name5"), containsInAnyOrder(response.get(0).getServiceAgreement().getName(),
            response.get(1).getServiceAgreement().getName()));
        assertThat(Arrays.asList("ext-id2", "ext-id3"),
            containsInAnyOrder(response.get(0).getServiceAgreement().getExternalId(),
                response.get(1).getServiceAgreement().getExternalId()));
    }

    @Test
    public void shouldReturnResultWhenLegalEntityIdentifierNotNullAndShareUsersAndAccounts() throws Exception {

        String type = "ARRANGEMENTS";

        PresentationGetDataGroupsRequest request = new PresentationGetDataGroupsRequest()
            .withLegalEntityIdentifier(new LegalEntityIdentifier()
                .externalIdIdentifier("Backbase").withShares(SharesEnum.USERSANDACCOUNTS));

        String responseAsString = executeRequest(
            new UrlBuilder(url)
                .addPathParameter(type)
                .build(),
            request,
            HttpMethod.POST
        );

        List<PresentationServiceAgreementWithDataGroups> response = readValue(
            responseAsString,
            new TypeReference<List<PresentationServiceAgreementWithDataGroups>>() {
            });
        assertThat(Arrays.asList("name5"), containsInAnyOrder(response.get(0).getServiceAgreement().getName()));
        assertThat(Arrays.asList("ext-id3"), containsInAnyOrder(response.get(0).getServiceAgreement().getExternalId()));
    }

    @Test
    public void shouldReturnResultWhenBothIdentifiersArProvidedForArrangements() throws Exception {

        String type = "ARRANGEMENTS";

        PresentationGetDataGroupsRequest request = new PresentationGetDataGroupsRequest()
            .withServiceAgreementIdentifier(new PresentationServiceAgreementIdentifier()
                .withExternalIdIdentifier(serviceAgreement.getExternalId()))
            .withDataItemIdentifier(new PresentationItemIdentifier().withExternalIdIdentifier("arr1"));

        AccountInternalIdGetResponseBody arrangementResponse = new AccountInternalIdGetResponseBody()
            .internalId(dataItem1);

        addStubGet(String.format(getArrangementInternalIdUrl, "arr1"), arrangementResponse, 200);

        String responseAsString = executeRequest(
            new UrlBuilder(url)
                .addPathParameter(type)
                .build(),
            request,
            HttpMethod.POST
        );

        List<PresentationServiceAgreementWithDataGroups> response = readValue(
            responseAsString,
            new TypeReference<List<PresentationServiceAgreementWithDataGroups>>() {
            });

        assertThat(response, contains(
            allOf(
                hasProperty("serviceAgreement", allOf(
                    hasProperty("name", equalTo(serviceAgreement.getName())),
                    hasProperty("externalId", equalTo(serviceAgreement.getExternalId())),
                    hasProperty("id", equalTo(serviceAgreement.getId())))),
                hasProperty("dataGroups", contains(
                    allOf(
                        hasProperty("id", equalTo(dataGroup1.getId())),
                        hasProperty("name", equalTo(dataGroup1.getName())),
                        hasProperty("description", equalTo(dataGroup1.getDescription()))))))));
    }

    @Test
    public void shouldReturnResultWhenServiceAgreementIdentifierIsProvidedForArrangements() throws Exception {

        String type = "ARRANGEMENTS";

        PresentationGetDataGroupsRequest request = new PresentationGetDataGroupsRequest()
            .withServiceAgreementIdentifier(new PresentationServiceAgreementIdentifier()
                .withIdIdentifier(serviceAgreement.getId()))
            .withDataItemIdentifier(null);

        String responseAsString = executeRequest(
            new UrlBuilder(url)
                .addPathParameter(type)
                .build(),
            request,
            HttpMethod.POST
        );

        List<PresentationServiceAgreementWithDataGroups> response = readValue(
            responseAsString,
            new TypeReference<List<PresentationServiceAgreementWithDataGroups>>() {
            });

        assertThat(response, contains(
            allOf(
                hasProperty("serviceAgreement", allOf(
                    hasProperty("name", equalTo(serviceAgreement.getName())),
                    hasProperty("externalId", equalTo(serviceAgreement.getExternalId())),
                    hasProperty("id", equalTo(serviceAgreement.getId())))),
                hasProperty("dataGroups", contains(
                    allOf(
                        hasProperty("id", equalTo(dataGroup1.getId())),
                        hasProperty("name", equalTo(dataGroup1.getName())),
                        hasProperty("description", equalTo(dataGroup1.getDescription()))),
                    allOf(
                        hasProperty("id", equalTo(dataGroup2.getId())),
                        hasProperty("name", equalTo(dataGroup2.getName())),
                        hasProperty("description", equalTo(dataGroup2.getDescription())))
                )))));
    }

    @Test
    public void shouldReturnResultWhenServiceAgreementIdentifierIsProvidedForArrangementsAndDataIsNull()
        throws Exception {

        String type = "ARRANGEMENTS";

        PresentationGetDataGroupsRequest request = new PresentationGetDataGroupsRequest()
            .withServiceAgreementIdentifier(new PresentationServiceAgreementIdentifier()
                .withIdIdentifier(serviceAgreement.getId()))
            .withDataItemIdentifier(new PresentationItemIdentifier());

        String responseAsString = executeRequest(
            new UrlBuilder(url)
                .addPathParameter(type)
                .build(),
            request,
            HttpMethod.POST
        );

        List<PresentationServiceAgreementWithDataGroups> response = readValue(
            responseAsString,
            new TypeReference<List<PresentationServiceAgreementWithDataGroups>>() {
            });

        assertThat(response, contains(
            allOf(
                hasProperty("serviceAgreement", allOf(
                    hasProperty("name", equalTo(serviceAgreement.getName())),
                    hasProperty("externalId", equalTo(serviceAgreement.getExternalId())),
                    hasProperty("id", equalTo(serviceAgreement.getId())))),
                hasProperty("dataGroups", contains(
                    allOf(
                        hasProperty("id", equalTo(dataGroup1.getId())),
                        hasProperty("name", equalTo(dataGroup1.getName())),
                        hasProperty("description", equalTo(dataGroup1.getDescription()))),
                    allOf(
                        hasProperty("id", equalTo(dataGroup2.getId())),
                        hasProperty("name", equalTo(dataGroup2.getName())),
                        hasProperty("description", equalTo(dataGroup2.getDescription())))
                )))));
    }

    @Test
    public void shouldThrowInternalErrorWhenNotImplementedTypeIsProvided() {

        String type = "CONTACTS";

        PresentationGetDataGroupsRequest request = new PresentationGetDataGroupsRequest()
            .withServiceAgreementIdentifier(new PresentationServiceAgreementIdentifier()
                .withExternalIdIdentifier(rootMsa.getExternalId()))
            .withDataItemIdentifier(new PresentationItemIdentifier().withExternalIdIdentifier("arr1"));

        ResponseEntity<String> response = executeRequestEntity(
            new UrlBuilder(url)
                .addPathParameter(type)
                .build(),
            request,
            HttpMethod.POST
        );

        assertEquals(500, response.getStatusCode().value());
        assertEquals("{\"message\":\"Internal Server Error\"}", response.getBody());

    }

    @Test
    public void shouldThrowBadRequestWhenPayeesNoServiceAgreement() {

        String type = "PAYEES";
        PresentationGetDataGroupsRequest request = new PresentationGetDataGroupsRequest()
            .withServiceAgreementIdentifier(null)
            .withDataItemIdentifier(new PresentationItemIdentifier()
                .withInternalIdIdentifier(null)
                .withExternalIdIdentifier("id"));

        assertThat(assertThrows(BadRequestException.class, () -> executeRequest(
            new UrlBuilder(url)
                .addPathParameter(type)
                .build(),
            request,
            HttpMethod.POST
        )), new BadRequestErrorMatcher(ERR_ACQ_077.getErrorMessage(), ERR_ACQ_077.getErrorCode()));
    }

    @Test
    public void shouldThrowBadRequestWhenInvalidRequestForArrangementsInvalidExternalId() {

        String type = "ARRANGEMENTS";
        AccountInternalIdGetResponseBody arrangementResponse = new AccountInternalIdGetResponseBody();
        addStubGet(String.format(getArrangementInternalIdUrl, "invalidId"), arrangementResponse, 404);
        PresentationGetDataGroupsRequest request = new PresentationGetDataGroupsRequest()
            .withServiceAgreementIdentifier(null)
            .withDataItemIdentifier(new PresentationItemIdentifier()
                .withInternalIdIdentifier(null)
                .withExternalIdIdentifier("invalidId"));
        shouldThrowBadRequestWhenInvalidRequest(type, request);
    }

    @Test
    public void shouldThrowBadRequestWhenInvalidRequestForArrangements() {

        String type = "ARRANGEMENTS";

        PresentationGetDataGroupsRequest request = new PresentationGetDataGroupsRequest();
        shouldThrowBadRequestWhenInvalidRequest(type, request);
    }

    private void shouldThrowBadRequestWhenInvalidRequest(String type, PresentationGetDataGroupsRequest request) {

        assertThat(assertThrows(BadRequestException.class, () -> executeRequest(
            new UrlBuilder(url)
                .addPathParameter(type)
                .build(),
            request,
            HttpMethod.POST
        )), new BadRequestErrorMatcher(ERR_AG_098.getErrorMessage(), ERR_AG_098.getErrorCode()));
    }

    private ServiceAgreement createSAWithParticipants(String name, String description, String externalId,
        LegalEntity creatorLE,
        LegalEntity participantLE, SharesEnum shares) {
        Participant participant = null;
        if (shares == SharesEnum.ACCOUNTS) {
            participant = new Participant()
                .withShareUsers(false)
                .withShareAccounts(true)
                .withLegalEntity(participantLE);
        } else if (shares == SharesEnum.USERSANDACCOUNTS) {
            participant = new Participant()
                .withShareUsers(true)
                .withShareAccounts(true)
                .withLegalEntity(participantLE);
        } else if (shares == SharesEnum.USERS) {
            participant = new Participant()
                .withShareUsers(true)
                .withShareAccounts(false)
                .withLegalEntity(participantLE);
        } else {
            throw new IllegalArgumentException("Unexpected value for shares parameter: '" + shares + "'");
        }
        participant.addAdmin("user-id");
        ServiceAgreement serviceAgreementWithParticipant = new ServiceAgreement()
            .withName(name)
            .withDescription(description)
            .withCreatorLegalEntity(creatorLE)
            .withExternalId(externalId)
            .withMaster(true);
        serviceAgreementWithParticipant.addParticipant(participant);
        return serviceAgreementWithParticipant;
    }

}
