package com.backbase.accesscontrol.api.service.it.on;

import static com.backbase.accesscontrol.matchers.BatchResponseItemMatcher.containsFailedResponseItem;
import static com.backbase.accesscontrol.matchers.BatchResponseItemMatcher.containsSuccessfulResponseItem;
import static com.backbase.pandp.accesscontrol.event.spec.v1.Action.UPDATE;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertTrue;

import com.backbase.accesscontrol.api.TestDbWireMock;
import com.backbase.accesscontrol.service.rest.spec.model.BatchResponseItem;
import com.backbase.accesscontrol.service.rest.spec.model.BatchResponseItem.StatusEnum;
import com.backbase.accesscontrol.util.helpers.LegalEntityUtil;
import com.backbase.pandp.accesscontrol.event.spec.v1.LegalEntityEvent;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.LegalEntity;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.LegalEntityPut;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.enumeration.LegalEntityType;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Sets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.TestPropertySource;

/**
 * The endpoint was chosen randomly in order to test if the context is passed properly in the audit client.
 */
@TestPropertySource(properties = {
    "wiremock=true",
    "backbase.data-group.validation.enabled=true",
    "backbase.audit.mode=SYNC"
},
    inheritProperties = false
)
public class UpdateBatchLegalEntityIT extends TestDbWireMock {

    private static final String PUT_LEGAL_ENTITIES = "/legalentities";
    private static final String PERSISTENCE_AUDIT_URL = "/service-api/v3/audit-messages";

    @Test
    public void shouldUpdateBatchLegalEntity() throws Exception {

        com.backbase.accesscontrol.domain.LegalEntity legalEntity = LegalEntityUtil
            .createLegalEntity(null, "newLe", "newLe", rootLegalEntity,
                com.backbase.accesscontrol.domain.enums.LegalEntityType.BANK);
        legalEntity = legalEntityJpaRepository.save(legalEntity);

        Map<String, String> additions = new HashMap<>();
        String key = "leExternalId";
        String value = "123456789";
        additions.put(key, value);

        LegalEntity legalEntity1 = new LegalEntity().withName("New root name")
            .withExternalId(rootLegalEntity.getExternalId())
            .withParentExternalId(null)
            .withType(LegalEntityType.CUSTOMER);
        LegalEntity legalEntity2 = new LegalEntity()
            .withName("name")
            .withExternalId(legalEntity.getExternalId())
            .withParentExternalId(rootLegalEntity.getExternalId())
            .withType(LegalEntityType.CUSTOMER);
        legalEntity2.setAdditions(additions);
        LegalEntityPut legalEntityPut1 = new LegalEntityPut()
            .withExternalId(rootLegalEntity.getExternalId())
            .withLegalEntity(legalEntity1);
        LegalEntityPut legalEntityPut2 = new LegalEntityPut()
            .withExternalId(legalEntity.getExternalId())
            .withLegalEntity(legalEntity2);

        addStubPost(PERSISTENCE_AUDIT_URL, "", 200);

        String responseJson = executeRequest(PUT_LEGAL_ENTITIES, asList(legalEntityPut1, legalEntityPut2),
            HttpMethod.PUT);

        List<BatchResponseItem> responseItems = readValue(
            responseJson,
            new TypeReference<List<BatchResponseItem>>() {
            });

        BatchResponseItem responseItem1 = new BatchResponseItem();
        responseItem1.setStatus(StatusEnum.HTTP_STATUS_BAD_REQUEST);
        responseItem1.setErrors(asList("Legal Entity must be bank"));
        responseItem1.setResourceId(rootLegalEntity.getExternalId());

        BatchResponseItem responseItem2 = new BatchResponseItem();
        responseItem2.setStatus(StatusEnum.HTTP_STATUS_OK);
        responseItem2.setResourceId(legalEntity.getExternalId());
        assertTrue(containsFailedResponseItem(responseItems, responseItem1));
        assertTrue(containsSuccessfulResponseItem(responseItems, responseItem2));

        verifyLegalEntityEvents(Sets.newHashSet(new LegalEntityEvent()
            .withAction(UPDATE)
            .withId(legalEntity.getId())));
    }
}
