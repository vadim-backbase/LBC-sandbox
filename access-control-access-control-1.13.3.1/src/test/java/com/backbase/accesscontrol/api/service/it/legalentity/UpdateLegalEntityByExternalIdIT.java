package com.backbase.accesscontrol.api.service.it.legalentity;

import static com.backbase.pandp.accesscontrol.event.spec.v1.Action.UPDATE;

import com.backbase.accesscontrol.api.TestDbWireMock;
import com.backbase.accesscontrol.domain.LegalEntity;
import com.backbase.accesscontrol.util.helpers.UrlBuilder;
import com.backbase.pandp.accesscontrol.event.spec.v1.LegalEntityEvent;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.LegalEntityByExternalIdPutRequestBody;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.enumeration.LegalEntityType;
import com.google.common.collect.Sets;
import org.junit.Test;
import org.springframework.http.HttpMethod;


public class UpdateLegalEntityByExternalIdIT extends TestDbWireMock {

    private static final String PUT_LEGAL_ENTITY_BY_EXTERNAL_ID = "/legalentities/external/{externalId}";

    @Test
    @SuppressWarnings("squid:S2699")
    public void shouldUpdateLegalEntityByExternalId() {
        String externalId = "externalId";

        LegalEntity legalEntity1 = new LegalEntity().withName("Name")
            .withExternalId(externalId)
            .withType(com.backbase.accesscontrol.domain.enums.LegalEntityType.CUSTOMER);
        legalEntityJpaRepository.save(legalEntity1);

        LegalEntityByExternalIdPutRequestBody data = new LegalEntityByExternalIdPutRequestBody()
            .withType(LegalEntityType.BANK);

        executeRequest(new UrlBuilder(PUT_LEGAL_ENTITY_BY_EXTERNAL_ID).addPathParameter(externalId).build(), data,
            HttpMethod.PUT);

        verifyLegalEntityEvents(Sets.newHashSet(new LegalEntityEvent()
            .withAction(UPDATE)
            .withId(legalEntity1.getId())));
    }
}
