package com.backbase.accesscontrol.api.service.it.on;

import static com.backbase.accesscontrol.domain.GraphConstants.DATA_GROUP_SERVICE_AGREEMENT;
import static com.backbase.pandp.accesscontrol.event.spec.v1.Action.ADD;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.backbase.accesscontrol.api.TestDbWireMock;
import com.backbase.accesscontrol.api.service.DataGroupsServiceApiController;
import com.backbase.accesscontrol.domain.DataGroup;
import com.backbase.accesscontrol.util.helpers.UrlBuilder;
import com.backbase.dbs.arrangement.api.client.v2.model.AccountArrangementsLegalEntities;
import com.backbase.dbs.arrangement.api.client.v2.model.AccountPresentationArrangementLegalEntityIds;
import com.backbase.pandp.accesscontrol.event.spec.v1.DataGroupEvent;
import com.backbase.presentation.accessgroup.event.spec.v1.DataGroupBase;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.datagroups.DataGroupsPostResponseBody;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.TestPropertySource;

/**
 * Test for {@link DataGroupsServiceApiController#postDataGroups}. The endpoint was chosen randomly in order to test if
 * the context is passed properly in the audit client.
 */
@TestPropertySource(properties = {
    "wiremock=true",
    "backbase.data-group.validation.enabled=true",
    "backbase.audit.mode=SYNC"
},
    inheritProperties = false
)
public class PostDataGroupsIT extends TestDbWireMock {

    private static final String URL = "/accessgroups/data-groups";
    private static final String PERSISTENCE_ARRANGEMENTS = "/service-api/v2/arrangements/legalentities";
    private static final String PERSISTENCE_AUDIT_URL = "/service-api/v3/audit-messages";

    @Test
    public void testCreateDataGroup() throws Exception {
        String dgName = "Name";
        String description = "Test Description";
        String serviceAgreementId = rootMsa.getId();
        String leCreatorId = rootLegalEntity.getId();

        DataGroupBase postData = new DataGroupBase()
            .withName(dgName)
            .withDescription(description)
            .withServiceAgreementId(serviceAgreementId)
            .withItems(Arrays.asList("1", "2"))
            .withType("ARRANGEMENTS");

        AccountArrangementsLegalEntities persistenceArrangementsLegalEntitiesBody = new AccountArrangementsLegalEntities()
            .arrangementsLegalEntities(new ArrayList<AccountPresentationArrangementLegalEntityIds>() {{
                add(new AccountPresentationArrangementLegalEntityIds().arrangementId("arrangamentId1"));
                add(new AccountPresentationArrangementLegalEntityIds().arrangementId("arrangamentId2"));
            }});

        addStubGet(
            new UrlBuilder(PERSISTENCE_ARRANGEMENTS)
                .addQueryParameter("arrangementIds", postData.getItems().get(0))
                .addQueryParameter("arrangementIds", postData.getItems().get(1))
                .addQueryParameter("legalEntityIds", leCreatorId)
                .build(),
            persistenceArrangementsLegalEntitiesBody,
            200
        );

        addStubPost(PERSISTENCE_AUDIT_URL, "", 200);

        String responseAsString = executeRequest(URL, postData, HttpMethod.POST);

        DataGroupsPostResponseBody dataGroupsPostResponseBodyResult = readValue(responseAsString,
            DataGroupsPostResponseBody.class);

        List<DataGroup> dataGroups = dataGroupJpaRepository
            .findByServiceAgreementId(serviceAgreementId, DATA_GROUP_SERVICE_AGREEMENT);

        assertNotNull(dataGroupsPostResponseBodyResult);
        assertEquals(dataGroups.get(0).getId(), dataGroupsPostResponseBodyResult.getId());

        verifyDataGroupEvents(Sets.newHashSet(new DataGroupEvent()
            .withAction(ADD)
            .withId(dataGroupsPostResponseBodyResult.getId())));
    }
}
