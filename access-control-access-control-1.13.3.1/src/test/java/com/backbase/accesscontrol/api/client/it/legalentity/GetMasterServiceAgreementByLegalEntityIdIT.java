package com.backbase.accesscontrol.api.client.it.legalentity;

import static com.backbase.accesscontrol.util.helpers.ResourceAndFunctionNameConstants.MANAGE_LEGAL_ENTITIES_FUNCTION_NAME;
import static com.backbase.accesscontrol.util.helpers.ResourceAndFunctionNameConstants.PRIVILEGE_VIEW;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.backbase.accesscontrol.api.TestDbWireMock;
import com.backbase.accesscontrol.domain.LegalEntity;
import com.backbase.accesscontrol.domain.Participant;
import com.backbase.accesscontrol.domain.ServiceAgreement;
import com.backbase.accesscontrol.domain.ServiceAgreementState;
import com.backbase.accesscontrol.domain.enums.LegalEntityType;
import com.backbase.accesscontrol.util.helpers.DateFormatterUtil;
import com.backbase.accesscontrol.util.helpers.UrlBuilder;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.MasterServiceAgreementGetResponseBody;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.Date;
import org.junit.Test;
import org.springframework.http.HttpMethod;

public class GetMasterServiceAgreementByLegalEntityIdIT extends TestDbWireMock {

    private static final String GET_MASTER_SERVICE_AGREEMENT_URL = "/legalentities/{legalEntityId}/serviceagreements/master";

    @Test
    public void testGetMasterServiceAgreement() throws Exception {

        String externalIdKey = "externalId";
        String externalIdValue = "ex id";
        Date from = new Date((System.currentTimeMillis() / 1000) * 1000 + 3600 * 1000);
        Date until = new Date((System.currentTimeMillis() / 1000) * 1000 + 2 * 3600 * 1000);

        LegalEntity legalEntity = legalEntityJpaRepository.saveAndFlush(new LegalEntity()
            .withExternalId("externalChildId")
            .withName("child name")
            .withType(LegalEntityType.CUSTOMER)
            .withParent(rootLegalEntity));

        ServiceAgreement serviceAgreement = new ServiceAgreement()
            .withName("testName")
            .withState(ServiceAgreementState.DISABLED)
            .withDescription("test description")
            .withCreatorLegalEntity(legalEntity)
            .withMaster(true)
            .withStartDate(from)
            .withEndDate(until)
            .withAdditions(Maps.toMap(Lists.newArrayList(externalIdKey), key -> externalIdValue));
        serviceAgreement.addParticipant(new Participant()
            .withServiceAgreement(serviceAgreement)
            .withShareUsers(true)
            .withShareAccounts(true)
            .withLegalEntity(legalEntity));

        serviceAgreement = serviceAgreementJpaRepository.saveAndFlush(serviceAgreement);

        String legalEntityId = legalEntity.getId();

        String responseFromPandP = executeClientRequest(
            new UrlBuilder(GET_MASTER_SERVICE_AGREEMENT_URL).addPathParameter(legalEntityId).build(), HttpMethod.GET,
            "admin",
            MANAGE_LEGAL_ENTITIES_FUNCTION_NAME, PRIVILEGE_VIEW);

        MasterServiceAgreementGetResponseBody masterServiceAgreement = readValue(responseFromPandP,
            MasterServiceAgreementGetResponseBody.class);

        assertEquals(serviceAgreement.getId(), masterServiceAgreement.getId());
        assertEquals("testName", masterServiceAgreement.getName());
        assertEquals("test description", masterServiceAgreement.getDescription());
        assertEquals("DISABLED", masterServiceAgreement.getStatus().toString());
        assertEquals(1, masterServiceAgreement.getAdditions().size());
        assertTrue(masterServiceAgreement.getAdditions().containsKey(externalIdKey));
        assertTrue(masterServiceAgreement.getAdditions().containsValue(externalIdValue));
        assertEquals(DateFormatterUtil.utcFormatDateOnly(from), masterServiceAgreement.getValidFromDate());
        assertEquals(DateFormatterUtil.utcFormatTimeOnly(from), masterServiceAgreement.getValidFromTime());
        assertEquals(DateFormatterUtil.utcFormatDateOnly(until), masterServiceAgreement.getValidUntilDate());
        assertEquals(DateFormatterUtil.utcFormatTimeOnly(until), masterServiceAgreement.getValidUntilTime());
    }
}
