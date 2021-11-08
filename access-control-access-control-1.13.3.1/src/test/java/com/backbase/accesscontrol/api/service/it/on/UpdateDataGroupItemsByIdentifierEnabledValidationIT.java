package com.backbase.accesscontrol.api.service.it.on;

import static com.backbase.accesscontrol.matchers.BatchResponseItemExtendedMatcher.getMatchers;
import static com.backbase.pandp.accesscontrol.event.spec.v1.Action.UPDATE;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;

import com.backbase.accesscontrol.api.TestDbWireMock;
import com.backbase.accesscontrol.api.service.DataGroupsServiceApiController;
import com.backbase.accesscontrol.domain.DataGroup;
import com.backbase.accesscontrol.util.helpers.DataGroupUtil;
import com.backbase.accesscontrol.util.helpers.UrlBuilder;
import com.backbase.dbs.arrangement.api.client.v2.model.AccountArrangementItem;
import com.backbase.dbs.arrangement.api.client.v2.model.AccountArrangementItems;
import com.backbase.dbs.arrangement.api.client.v2.model.AccountArrangementsFilter;
import com.backbase.dbs.arrangement.api.client.v2.model.AccountArrangementsLegalEntities;
import com.backbase.dbs.arrangement.api.client.v2.model.AccountPresentationArrangementLegalEntityIds;
import com.backbase.pandp.accesscontrol.event.spec.v1.DataGroupEvent;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.BatchResponseItemExtended;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.BatchResponseStatusCode;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.NameIdentifier;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.PresentationAction;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.PresentationIdentifier;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.PresentationItemIdentifier;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.datagroups.PresentationDataGroupItemPutRequestBody;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.TestPropertySource;

/**
 * Test for {@link DataGroupsServiceApiController#putDataGroupItemsUpdate}
 */
@TestPropertySource(properties = {
    "backbase.data-group.validation.enabled=true"
})
public class UpdateDataGroupItemsByIdentifierEnabledValidationIT extends TestDbWireMock {

    private static final String URL = "/accessgroups/data-groups/batch/update/data-items";
    private static final String GET_LEGAL_ENTITIES_URL = baseServiceUrl + "/arrangements/legalentities";
    private static final String PRODUCT_SUMMARY_GET_ARRANGEMENTS_URL =
        baseServiceUrl + "/arrangements/filter";

    private static final String ARRANGEMENTS = "ARRANGEMENTS";

    private DataGroup dataGroup1;
    private DataGroup dataGroup2;

    private final String item1Id = getUuid();
    private final String item2Id = getUuid();
    private final String item3Id = getUuid();

    @Before
    public void setUp() throws Exception {
        dataGroup1 = DataGroupUtil.createDataGroup("dg-name", ARRANGEMENTS, "desc", rootMsa);
        dataGroup1.setDataItemIds(Collections.singleton(item1Id));
        dataGroup1 = dataGroupJpaRepository.save(dataGroup1);

        dataGroup2 = DataGroupUtil.createDataGroup("dg-name-2", ARRANGEMENTS, "desc", rootMsa);
        dataGroup2.setDataItemIds(newHashSet(item2Id, item3Id));
        dataGroup2 = dataGroupJpaRepository.save(dataGroup2);
    }

    @Test
    public void testUpdateDataGroupBatch() throws Exception {
        String arrangementId = "internal-1";
        String leId = rootLegalEntity.getId();
        PresentationDataGroupItemPutRequestBody dataGroupRequest = new PresentationDataGroupItemPutRequestBody()
            .withAction(PresentationAction.ADD)
            .withType(ARRANGEMENTS)
            .withDataGroupIdentifier(new PresentationIdentifier()
                .withIdIdentifier(dataGroup1.getId()))
            .withDataItems(singletonList(new PresentationItemIdentifier()
                .withInternalIdIdentifier(arrangementId)));

        List<PresentationDataGroupItemPutRequestBody> putData = Lists.newArrayList(dataGroupRequest);

        BatchResponseItemExtended batchResponseItemSuccessful = new BatchResponseItemExtended()
            .withResourceId(dataGroup1.getId())
            .withAction(PresentationAction.ADD)
            .withStatus(BatchResponseStatusCode.HTTP_STATUS_OK);

        List<BatchResponseItemExtended> allRequestData = Lists.newArrayList(batchResponseItemSuccessful);

        AccountPresentationArrangementLegalEntityIds persistenceArrangementLegalEntityIds = new AccountPresentationArrangementLegalEntityIds()
            .arrangementId(arrangementId)
            .legalEntityIds(singletonList(leId));
        AccountArrangementsLegalEntities persistenceArrangementsLegalEntitiesBody = new AccountArrangementsLegalEntities()
            .arrangementsLegalEntities(singletonList(persistenceArrangementLegalEntityIds));

        addStubGet(new UrlBuilder(GET_LEGAL_ENTITIES_URL)
                .addQueryParameter("arrangementIds", arrangementId)
                .addQueryParameter("legalEntityIds", leId)
                .build(),
            persistenceArrangementsLegalEntitiesBody, 200);

        String contentAsString = executeRequest(URL, putData, HttpMethod.PUT);

        List<BatchResponseItemExtended> response = readValue(contentAsString,
            new TypeReference<List<BatchResponseItemExtended>>() {
            });

        assertThat(response,
            containsInAnyOrder(
                getMatchers(allRequestData)
            )
        );

        verifyDataGroupEvents(Sets.newHashSet(new DataGroupEvent()
            .withAction(UPDATE)
            .withId(dataGroup1.getId())));
    }

    @Test
    public void testUpdateDataGroupBatchWithDataGroupWithNameIdentifier() throws Exception {
        String leId = rootLegalEntity.getId();
        String externalId = "external-2";

        PresentationDataGroupItemPutRequestBody dataGroup1 = new PresentationDataGroupItemPutRequestBody()
            .withAction(PresentationAction.REMOVE)
            .withType("ARRANGEMENTS")
            .withDataGroupIdentifier(new PresentationIdentifier()
                .withNameIdentifier(new NameIdentifier()
                    .withName(dataGroup2.getName())
                    .withExternalServiceAgreementId(rootMsa.getExternalId())))
            .withDataItems(singletonList(new PresentationItemIdentifier()
                .withExternalIdIdentifier(externalId)));

        List<PresentationDataGroupItemPutRequestBody> putData = Lists.newArrayList(dataGroup1);

        BatchResponseItemExtended batchResponseItemSuccessful = new BatchResponseItemExtended()
            .withResourceId(dataGroup2.getName())
            .withAction(PresentationAction.REMOVE)
            .withExternalServiceAgreementId(rootMsa.getExternalId())
            .withStatus(
                BatchResponseStatusCode.HTTP_STATUS_OK);

        List<BatchResponseItemExtended> allRequestData = Lists.newArrayList(batchResponseItemSuccessful);

        AccountArrangementItem accountArrangementItem = new AccountArrangementItem();
        accountArrangementItem.setId(item2Id);
        accountArrangementItem.setExternalArrangementId(externalId);

        AccountArrangementItems psResponse = new AccountArrangementItems()
            .arrangementElements(singletonList(accountArrangementItem));

        List<String> externalIds = singletonList(externalId);
        addStubPostEqualToJson(PRODUCT_SUMMARY_GET_ARRANGEMENTS_URL, psResponse, 200,
            new AccountArrangementsFilter().externalArrangementIds(externalIds).size(externalIds.size()));

        AccountPresentationArrangementLegalEntityIds persistenceArrangementLegalEntityIds = new AccountPresentationArrangementLegalEntityIds()
            .arrangementId(item2Id)
            .legalEntityIds(singletonList(leId));
        AccountArrangementsLegalEntities persistenceArrangementsLegalEntitiesBody = new AccountArrangementsLegalEntities()
            .arrangementsLegalEntities(singletonList(persistenceArrangementLegalEntityIds));

        addStubGet(new UrlBuilder(GET_LEGAL_ENTITIES_URL)
                .addQueryParameter("arrangementIds", item2Id)
                .addQueryParameter("legalEntityIds", leId)
                .build(),
            persistenceArrangementsLegalEntitiesBody, 200);

        String contentAsString = executeRequest(URL, putData, HttpMethod.PUT);

        List<BatchResponseItemExtended> response = readValue(contentAsString,
            new TypeReference<List<BatchResponseItemExtended>>() {
            });

        assertThat(response,
            containsInAnyOrder(
                getMatchers(allRequestData)
            )
        );

        verifyDataGroupEvents(Sets.newHashSet(new DataGroupEvent()
            .withAction(UPDATE)
            .withId(dataGroup2.getId())));
    }
}
