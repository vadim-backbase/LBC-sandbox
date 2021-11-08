package com.backbase.accesscontrol.api.client.it.on;

import static com.backbase.accesscontrol.domain.GraphConstants.DATA_GROUP_SERVICE_AGREEMENT;
import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_089;
import static com.backbase.accesscontrol.util.helpers.ResourceAndFunctionNameConstants.ENTITLEMENTS_MANAGE_DATA_GROUPS;
import static com.backbase.accesscontrol.util.helpers.ResourceAndFunctionNameConstants.PRIVILEGE_CREATE;
import static com.backbase.pandp.accesscontrol.event.spec.v1.Action.ADD;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import com.backbase.accesscontrol.api.TestDbWireMock;
import com.backbase.accesscontrol.api.client.DataGroupClientController;
import com.backbase.accesscontrol.domain.DataGroup;
import com.backbase.accesscontrol.matchers.BadRequestErrorMatcher;
import com.backbase.accesscontrol.util.helpers.UrlBuilder;
import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.dbs.arrangement.api.client.v2.model.AccountArrangementsLegalEntities;
import com.backbase.dbs.arrangement.api.client.v2.model.AccountPresentationArrangementLegalEntityIds;
import com.backbase.pandp.accesscontrol.event.spec.v1.DataGroupEvent;
import com.backbase.presentation.accessgroup.event.spec.v1.DataGroupBase;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.datagroups.DataGroupsPostResponseBody;
import com.google.common.collect.Sets;
import java.util.List;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.TestPropertySource;

/**
 * Test for {@link DataGroupClientController#postDataGroups}
 */
@TestPropertySource(properties = {
    "backbase.data-group.validation.enabled=true"}
)
public class CreateDataGroupIT extends TestDbWireMock {

    private static final String url = "/accessgroups/data-groups";
    private static final String PERSISTENCE_ARRANGEMENTS = "/service-api/v2/arrangements/legalentities";

    @Test
    public void shouldCreateDataGroupUnderSA() throws Exception {
        String dgName = "Name";
        String description = "Test Description";

        String serviceAgreementId = rootMsa.getId();
        String leCreatorId = rootLegalEntity.getId();
        String item01 = getUuid();
        String item02 = getUuid();

        AccountArrangementsLegalEntities arrangementIdsBody = new AccountArrangementsLegalEntities()
            .arrangementsLegalEntities(asList(new AccountPresentationArrangementLegalEntityIds().arrangementId(item01),
                new AccountPresentationArrangementLegalEntityIds().arrangementId(item02)));

        addStubGet(
            new UrlBuilder(PERSISTENCE_ARRANGEMENTS)
                .addQueryParameter("arrangementIds", item01)
                .addQueryParameter("arrangementIds", item02)
                .addQueryParameter("legalEntityIds", leCreatorId)
                .build(),
            arrangementIdsBody,
            200
        );

        DataGroupBase postData = new DataGroupBase()
            .withName(dgName)
            .withDescription(description)
            .withServiceAgreementId(serviceAgreementId)
            .withItems(asList(item01, item02))
            .withType("ARRANGEMENTS");

        String contentAsString = executeClientRequest(url, HttpMethod.POST, postData, "user",
            ENTITLEMENTS_MANAGE_DATA_GROUPS, PRIVILEGE_CREATE);

        String responseId = readValue(contentAsString, DataGroupsPostResponseBody.class)
            .getId();

        List<DataGroup> dataGroups = dataGroupJpaRepository
            .findByServiceAgreementId(serviceAgreementId, DATA_GROUP_SERVICE_AGREEMENT);
        assertEquals(dataGroups.get(0).getId(), responseId);
        verifyDataGroupEvents(Sets.newHashSet(new DataGroupEvent()
            .withAction(ADD)
            .withId(responseId)));
    }

    @Test
    public void shouldFailValidationRoute() {
        String dgName = "Name";
        String description = "Test Description";
        String serviceAgreementId = rootMsa.getId();
        String item01 = getUuid();
        String item02 = getUuid();

        DataGroupBase postData = new DataGroupBase()
            .withName(dgName)
            .withDescription(description)
            .withServiceAgreementId(serviceAgreementId)
            .withItems(asList(item01, item02))
            .withType("ARRANGEMENTS");

        AccountArrangementsLegalEntities arrangementIdsBody = new AccountArrangementsLegalEntities()
            .arrangementsLegalEntities(
                singletonList(new AccountPresentationArrangementLegalEntityIds().arrangementId("1")));

        addStubGet(
            new UrlBuilder(PERSISTENCE_ARRANGEMENTS)
                .addQueryParameter("arrangementIds", item01)
                .addQueryParameter("arrangementIds", item02)
                .addQueryParameter("legalEntityIds", rootLegalEntity.getId())
                .build(),
            arrangementIdsBody,
            200
        );

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> executeClientRequest(url, HttpMethod.POST, postData, "user",
                ENTITLEMENTS_MANAGE_DATA_GROUPS, PRIVILEGE_CREATE));

        assertThat(exception, new BadRequestErrorMatcher(ERR_AG_089.getErrorMessage(), ERR_AG_089.getErrorCode()));
    }
}