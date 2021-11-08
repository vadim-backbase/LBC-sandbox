package com.backbase.accesscontrol.api.service.it.datagroup;

import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_081;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_051;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_053;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_079;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_081;
import static com.backbase.pandp.accesscontrol.event.spec.v1.Action.UPDATE;
import static java.util.Arrays.asList;
import static junit.framework.TestCase.assertEquals;

import com.backbase.accesscontrol.api.TestDbWireMock;
import com.backbase.accesscontrol.domain.DataGroup;
import com.backbase.accesscontrol.domain.LegalEntity;
import com.backbase.accesscontrol.domain.ServiceAgreement;
import com.backbase.accesscontrol.domain.enums.LegalEntityType;
import com.backbase.accesscontrol.util.helpers.DataGroupUtil;
import com.backbase.accesscontrol.util.helpers.LegalEntityUtil;
import com.backbase.accesscontrol.util.helpers.ServiceAgreementUtil;
import com.backbase.pandp.accesscontrol.event.spec.v1.DataGroupEvent;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.BatchResponseItemExtended;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.BatchResponseStatusCode;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.NameIdentifier;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.PresentationAction;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.PresentationIdentifier;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.PresentationItemIdentifier;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.datagroups.PresentationDataGroupItemPutRequestBody;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpMethod;

public class PutDataGroupItemsUpdateIT extends TestDbWireMock {

    private static final String ARRANGEMENTS = "ARRANGEMENTS";

    private String url = "/accessgroups/data-groups/batch/update/data-items";

    private ServiceAgreement serviceAgreement;
    private DataGroup dataGroup1;
    private DataGroup dataGroup2;

    private String item1Id = getUuid();
    private String item2Id = getUuid();
    private String item3Id = getUuid();

    @Before
    public void setUp() throws Exception {
        LegalEntity legalEntity = LegalEntityUtil
            .createLegalEntity(null, "le-name", "ex-id3", null, LegalEntityType.BANK);
        legalEntity = legalEntityJpaRepository.save(legalEntity);
        serviceAgreement = ServiceAgreementUtil
            .createServiceAgreement("sa-name", "sa-exid", "sa-desc", legalEntity, null, null);
        serviceAgreement.setMaster(true);
        serviceAgreement = serviceAgreementJpaRepository.save(serviceAgreement);

        dataGroup1 = DataGroupUtil.createDataGroup("dg-name", ARRANGEMENTS, "desc", serviceAgreement);
        dataGroup1.setDataItemIds(Collections.singleton(item1Id));
        dataGroupJpaRepository.save(dataGroup1);

        dataGroup2 = DataGroupUtil.createDataGroup("dg-name-2", ARRANGEMENTS, "desc", serviceAgreement);
        dataGroup2.setDataItemIds(Sets.newHashSet(item2Id, item3Id));
        dataGroupJpaRepository.save(dataGroup2);
    }

    @Test
    public void shouldAddAndRemoveDataItemsFromToDataGroupWithDifferentIdentifiers() throws Exception {
        List<PresentationDataGroupItemPutRequestBody> request = asList(
            new PresentationDataGroupItemPutRequestBody()
                .withDataGroupIdentifier(
                    new PresentationIdentifier().withIdIdentifier(dataGroup1.getId())
                )
                .withAction(PresentationAction.ADD)
                .withType(ARRANGEMENTS)
                .withDataItems(asList(
                    new PresentationItemIdentifier().withInternalIdIdentifier(getUuid()),
                    new PresentationItemIdentifier().withInternalIdIdentifier(getUuid())
                )),
            new PresentationDataGroupItemPutRequestBody()
                .withDataGroupIdentifier(
                    new PresentationIdentifier()
                        .withNameIdentifier(
                            new com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.NameIdentifier()
                                .withName(dataGroup2.getName())
                                .withExternalServiceAgreementId(serviceAgreement.getExternalId())
                        )
                )
                .withAction(PresentationAction.REMOVE)
                .withType(ARRANGEMENTS)
                .withDataItems(Collections.singletonList(
                    new PresentationItemIdentifier().withInternalIdIdentifier(item2Id)
                ))
        );

        String response = executeRequest(url, request, HttpMethod.PUT);
        List<BatchResponseItemExtended> responseItemsExtended = objectMapper
            .readValue(response, new TypeReference<List<BatchResponseItemExtended>>() {
            });
        assertEquals(2, responseItemsExtended.size());
        assertEquals("200", responseItemsExtended.get(0).getStatus().toString());

        verifyDataGroupEvents(Sets.newHashSet(new DataGroupEvent()
                .withAction(UPDATE)
                .withId(dataGroup1.getId()),
            new DataGroupEvent()
                .withAction(UPDATE)
                .withId(dataGroup2.getId())));

    }

    @Test
    public void testBadRequestsForIdentifiersUpdateDataGroupBatch() throws Exception {
        List<PresentationDataGroupItemPutRequestBody> request = asList(
            new PresentationDataGroupItemPutRequestBody()
                .withDataGroupIdentifier(new PresentationIdentifier())
                .withAction(PresentationAction.REMOVE)
                .withType(ARRANGEMENTS)
                .withDataItems(Collections.singletonList(
                    new PresentationItemIdentifier().withInternalIdIdentifier(item2Id)
                    )
                ));

        String response = executeRequest(url, request, HttpMethod.PUT);
        List<BatchResponseItemExtended> responseItemsExtended = objectMapper
            .readValue(response, new TypeReference<List<BatchResponseItemExtended>>() {
            });

        assertEquals(1, responseItemsExtended.size());
        assertEquals(1, responseItemsExtended.get(0).getErrors().size());
        assertEquals(ERR_AG_081.getErrorMessage(), responseItemsExtended.get(0).getErrors().get(0));

    }


    @Test
    public void shouldReturnErrorsWhenDataGroupDoesNotExist() throws Exception {
        String randomId = getUuid();
        List<PresentationDataGroupItemPutRequestBody> request = asList(
            new PresentationDataGroupItemPutRequestBody()
                .withDataGroupIdentifier(
                    new PresentationIdentifier().withIdIdentifier(randomId)
                )
                .withAction(PresentationAction.ADD)
                .withType(ARRANGEMENTS)
                .withDataItems(asList(
                    new PresentationItemIdentifier().withInternalIdIdentifier(getUuid()),
                    new PresentationItemIdentifier().withInternalIdIdentifier(getUuid())
                )),
            new PresentationDataGroupItemPutRequestBody()
                .withDataGroupIdentifier(
                    new PresentationIdentifier()
                        .withNameIdentifier(
                            new com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.NameIdentifier()
                                .withName("random-name")
                                .withExternalServiceAgreementId("random-external-id")
                        )
                )
                .withAction(PresentationAction.REMOVE)
                .withType(ARRANGEMENTS)
                .withDataItems(Collections.singletonList(
                    new PresentationItemIdentifier().withInternalIdIdentifier(item2Id)
                ))
        );

        String response = executeRequest(url, request, HttpMethod.PUT);
        List<BatchResponseItemExtended> responseItemsExtended = objectMapper
            .readValue(response, new TypeReference<List<BatchResponseItemExtended>>() {
            });

        assertEquals(2, responseItemsExtended.size());
        assertEquals(randomId, responseItemsExtended.get(0).getResourceId());
        assertEquals(BatchResponseStatusCode.HTTP_STATUS_NOT_FOUND, responseItemsExtended.get(0).getStatus());
        assertEquals(1, responseItemsExtended.get(0).getErrors().size());
        assertEquals(ERR_ACC_051.getErrorMessage(), responseItemsExtended.get(0).getErrors().get(0));

        assertEquals("random-name", responseItemsExtended.get(1).getResourceId());
        assertEquals("random-external-id",
            responseItemsExtended.get(1).getExternalServiceAgreementId());
        assertEquals(BatchResponseStatusCode.HTTP_STATUS_NOT_FOUND, responseItemsExtended.get(1).getStatus());
        assertEquals(1, responseItemsExtended.get(1).getErrors().size());
        assertEquals(ERR_ACC_051.getErrorMessage(), responseItemsExtended.get(1).getErrors().get(0));
    }

    @Test
    public void shouldReturnErrorsIfTypeIsChanged() throws Exception {
        List<PresentationDataGroupItemPutRequestBody> request = asList(
            new PresentationDataGroupItemPutRequestBody()
                .withDataGroupIdentifier(
                    new PresentationIdentifier().withIdIdentifier(dataGroup1.getId())
                )
                .withAction(PresentationAction.ADD)
                .withType("CUSTOMERS")
                .withDataItems(asList(
                    new PresentationItemIdentifier().withInternalIdIdentifier(getUuid()),
                    new PresentationItemIdentifier().withInternalIdIdentifier(getUuid())
                )),
            new PresentationDataGroupItemPutRequestBody()
                .withDataGroupIdentifier(
                    new PresentationIdentifier()
                        .withNameIdentifier(
                            new com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.NameIdentifier()
                                .withName(dataGroup2.getName())
                                .withExternalServiceAgreementId(serviceAgreement.getExternalId())
                        )
                )
                .withAction(PresentationAction.REMOVE)
                .withType("CUSTOMERS")
                .withDataItems(Collections.singletonList(
                    new PresentationItemIdentifier().withInternalIdIdentifier(item2Id)
                ))
        );

        String response = executeRequest(url, request, HttpMethod.PUT);
        List<BatchResponseItemExtended> responseItemsExtended = objectMapper
            .readValue(response, new TypeReference<List<BatchResponseItemExtended>>() {
            });

        assertEquals(2, responseItemsExtended.size());
        assertEquals(dataGroup1.getId(), responseItemsExtended.get(0).getResourceId());
        assertEquals(BatchResponseStatusCode.HTTP_STATUS_BAD_REQUEST, responseItemsExtended.get(0).getStatus());
        assertEquals(1, responseItemsExtended.get(0).getErrors().size());
        assertEquals(ERR_ACC_053.getErrorMessage(), responseItemsExtended.get(0).getErrors().get(0));

        assertEquals(dataGroup2.getName(), responseItemsExtended.get(1).getResourceId());
        assertEquals(serviceAgreement.getExternalId(),
            responseItemsExtended.get(1).getExternalServiceAgreementId());
        assertEquals(BatchResponseStatusCode.HTTP_STATUS_BAD_REQUEST, responseItemsExtended.get(1).getStatus());
        assertEquals(1, responseItemsExtended.get(1).getErrors().size());
        assertEquals(ERR_ACC_053.getErrorMessage(), responseItemsExtended.get(1).getErrors().get(0));
    }

    @Test
    public void shouldReturnErrorsIfDataItemToAddAlreadyExistInDataGroup() throws Exception {
        List<PresentationDataGroupItemPutRequestBody> request = Collections.singletonList(
            new PresentationDataGroupItemPutRequestBody()
                .withDataGroupIdentifier(
                    new PresentationIdentifier().withIdIdentifier(dataGroup1.getId())
                )
                .withAction(PresentationAction.ADD)
                .withType(ARRANGEMENTS)
                .withDataItems(Collections.singletonList(
                    new PresentationItemIdentifier().withInternalIdIdentifier(item1Id)
                ))
        );

        String response = executeRequest(url, request, HttpMethod.PUT);
        List<BatchResponseItemExtended> responseItemsExtended = objectMapper
            .readValue(response, new TypeReference<List<BatchResponseItemExtended>>() {
            });

        assertEquals(1, responseItemsExtended.size());
        assertEquals(dataGroup1.getId(), responseItemsExtended.get(0).getResourceId());
        assertEquals(BatchResponseStatusCode.HTTP_STATUS_BAD_REQUEST, responseItemsExtended.get(0).getStatus());
        assertEquals(1, responseItemsExtended.get(0).getErrors().size());
        assertEquals(ERR_ACC_081.getErrorMessage(), responseItemsExtended.get(0).getErrors().get(0));
    }

    @Test
    public void shouldReturnErrorIfDataItemDoesNotExistInDataGroup() throws Exception {
        List<PresentationDataGroupItemPutRequestBody> request = asList(
            new PresentationDataGroupItemPutRequestBody()
                .withDataGroupIdentifier(
                    new PresentationIdentifier().withIdIdentifier(dataGroup1.getId())
                )
                .withAction(PresentationAction.REMOVE)
                .withType(ARRANGEMENTS)
                .withDataItems(Collections.singletonList(
                    new PresentationItemIdentifier().withInternalIdIdentifier(item1Id)
                )),
            new PresentationDataGroupItemPutRequestBody()
                .withDataGroupIdentifier(
                    new PresentationIdentifier()
                        .withNameIdentifier(
                            new NameIdentifier()
                                .withName(dataGroup2.getName())
                                .withExternalServiceAgreementId(serviceAgreement.getExternalId())
                        )
                )
                .withAction(PresentationAction.REMOVE)
                .withType(ARRANGEMENTS)
                .withDataItems(Collections.singletonList(
                    new PresentationItemIdentifier().withInternalIdIdentifier(getUuid())
                ))
        );

        String response = executeRequest(url, request, HttpMethod.PUT);
        List<BatchResponseItemExtended> responseItemsExtended = objectMapper
            .readValue(response, new TypeReference<List<BatchResponseItemExtended>>() {
            });

        assertEquals(2, responseItemsExtended.size());
        assertEquals(dataGroup1.getId(), responseItemsExtended.get(0).getResourceId());
        assertEquals(BatchResponseStatusCode.HTTP_STATUS_OK, responseItemsExtended.get(0).getStatus());

        assertEquals(dataGroup2.getName(), responseItemsExtended.get(1).getResourceId());
        assertEquals(serviceAgreement.getExternalId(),
            responseItemsExtended.get(1).getExternalServiceAgreementId());
        assertEquals(BatchResponseStatusCode.HTTP_STATUS_BAD_REQUEST, responseItemsExtended.get(1).getStatus());
        assertEquals(1, responseItemsExtended.get(1).getErrors().size());
        assertEquals(ERR_ACC_079.getErrorMessage(), responseItemsExtended.get(1).getErrors().get(0));

        verifyDataGroupEvents(Sets.newHashSet(new DataGroupEvent()
            .withAction(UPDATE)
            .withId(dataGroup1.getId())));
    }

}
