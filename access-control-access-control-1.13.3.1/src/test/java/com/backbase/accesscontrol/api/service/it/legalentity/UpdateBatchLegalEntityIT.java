package com.backbase.accesscontrol.api.service.it.legalentity;

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
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.enumeration.LegalEntityType;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Sets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import org.springframework.http.HttpMethod;

public class UpdateBatchLegalEntityIT extends TestDbWireMock {

    private static final String PUT_LEGAL_ENTITIES = "/legalentities";

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

        Map<String, String> invalidAdditions = new HashMap<>();
        invalidAdditions.put("invalid", "invalid");

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
        LegalEntity legalEntity3 = new LegalEntity()
            .withName("ok name")
            .withExternalId(legalEntity.getExternalId())
            .withParentExternalId(rootLegalEntity.getExternalId())
            .withType(LegalEntityType.CUSTOMER);
        legalEntity3.setAdditions(invalidAdditions);
        com.backbase.presentation.legalentity.rest.spec.v2.legalentities.LegalEntityPut legalEntityPut1 = new com.backbase.presentation.legalentity.rest.spec.v2.legalentities.LegalEntityPut()
            .withExternalId(rootLegalEntity.getExternalId())
            .withLegalEntity(legalEntity1);
        com.backbase.presentation.legalentity.rest.spec.v2.legalentities.LegalEntityPut legalEntityPut2 = new com.backbase.presentation.legalentity.rest.spec.v2.legalentities.LegalEntityPut()
            .withExternalId(legalEntity.getExternalId())
            .withLegalEntity(legalEntity2);
        com.backbase.presentation.legalentity.rest.spec.v2.legalentities.LegalEntityPut legalEntityPut3 = new com.backbase.presentation.legalentity.rest.spec.v2.legalentities.LegalEntityPut()
            .withExternalId(legalEntity.getExternalId())
            .withLegalEntity(legalEntity3);

        String responseJson = executeRequest(PUT_LEGAL_ENTITIES,
            asList(legalEntityPut1, legalEntityPut2, legalEntityPut3),
            HttpMethod.PUT);

        List<com.backbase.accesscontrol.service.rest.spec.model.BatchResponseItem> responseItems = readValue(
            responseJson,
            new TypeReference<List<com.backbase.accesscontrol.service.rest.spec.model.BatchResponseItem>>() {
            });

        BatchResponseItem responseItem1 = new BatchResponseItem();
        responseItem1.setStatus(StatusEnum.HTTP_STATUS_BAD_REQUEST);
        responseItem1.setErrors(asList("Legal Entity must be bank"));
        responseItem1.setResourceId(rootLegalEntity.getExternalId());

        BatchResponseItem responseItem2 = new BatchResponseItem();
        responseItem2.setStatus(StatusEnum.HTTP_STATUS_BAD_REQUEST);
        responseItem2.setErrors(asList("legalEntity.additions[invalid] The key is unexpected"));
        responseItem2.setResourceId(legalEntity.getExternalId());

        BatchResponseItem responseItem3 = new BatchResponseItem();
        responseItem3.setStatus(StatusEnum.HTTP_STATUS_OK);
        responseItem3.setResourceId(legalEntity.getExternalId());
        assertTrue(containsFailedResponseItem(responseItems, responseItem1));
        assertTrue(containsFailedResponseItem(responseItems, responseItem2));
        assertTrue(containsSuccessfulResponseItem(responseItems, responseItem3));

        verifyLegalEntityEvents(Sets.newHashSet(new LegalEntityEvent()
            .withAction(UPDATE)
            .withId(legalEntity.getId())));
    }
}
