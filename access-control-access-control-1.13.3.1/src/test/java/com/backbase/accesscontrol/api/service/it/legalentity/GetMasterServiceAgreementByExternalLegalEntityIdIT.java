package com.backbase.accesscontrol.api.service.it.legalentity;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.beans.HasPropertyWithValue.hasProperty;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThrows;

import com.backbase.accesscontrol.api.TestDbWireMock;
import com.backbase.accesscontrol.domain.LegalEntity;
import com.backbase.accesscontrol.domain.Participant;
import com.backbase.accesscontrol.domain.ServiceAgreement;
import com.backbase.accesscontrol.domain.enums.LegalEntityType;
import com.backbase.accesscontrol.matchers.ForbiddenErrorMatcher;
import com.backbase.accesscontrol.util.errorcodes.LegalEntityErrorCodes;
import com.backbase.accesscontrol.util.helpers.DateFormatterUtil;
import com.backbase.accesscontrol.util.helpers.UrlBuilder;
import com.backbase.buildingblocks.presentation.errors.ForbiddenException;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.MasterServiceAgreementGetResponseBody;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.Status;
import java.util.Date;
import java.util.HashMap;
import org.junit.Test;
import org.springframework.http.HttpMethod;

/**
 * GetMasterServiceAgreementByExternalLegalEntityIdIT.
 */
public class GetMasterServiceAgreementByExternalLegalEntityIdIT extends TestDbWireMock {

    private static final String GET_MASTER_SERVICE_AGREEMENT_BY_EXTERNAL_LEGAL_ENTITY_PRESENTATION_URL = "/legalentities/external/{externalId}/serviceagreements/master";

    @Test
    public void testGetMasterServiceAgreementByExternalId() throws Exception {
        ServiceAgreement msa = rootMsa;
        String externalIdString = rootLegalEntity.getExternalId();
        String saId = msa.getId();
        String userName = "admin";

        Date from = msa.getStartDate();
        Date until = msa.getEndDate();

        String responseJson = executeServiceRequest(
            new UrlBuilder(GET_MASTER_SERVICE_AGREEMENT_BY_EXTERNAL_LEGAL_ENTITY_PRESENTATION_URL)
                .addPathParameter(externalIdString).build(), "", userName,
            saId, contextUserId, rootLegalEntity.getId(), HttpMethod.GET, new HashMap<>());

        MasterServiceAgreementGetResponseBody externalId = readValue(responseJson,
            MasterServiceAgreementGetResponseBody.class);

        assertThat(externalId,
            allOf(
                hasProperty("id", equalTo(msa.getId())),
                hasProperty("externalId", equalTo(msa.getExternalId())),
                hasProperty("status", equalTo(Status.ENABLED)),
                hasProperty("creatorLegalEntity", equalTo(msa.getCreatorLegalEntity().getId())),
                hasProperty("additions", equalTo(msa.getAdditions())),
                hasProperty("name", equalTo(msa.getName())),
                hasProperty("isMaster", equalTo(msa.isMaster())),
                hasProperty("description", equalTo(msa.getDescription())),
                hasProperty("validFromDate", equalTo(DateFormatterUtil.utcFormatDateOnly(from))),
                hasProperty("validFromTime", equalTo(DateFormatterUtil.utcFormatTimeOnly(from))),
                hasProperty("validUntilDate", equalTo(DateFormatterUtil.utcFormatDateOnly(until))),
                hasProperty("validUntilTime", equalTo(DateFormatterUtil.utcFormatTimeOnly(until)))
            )
        );
    }

    @Test
    public void shouldThrowForbiddenWhenAccessingMsaFromChildLe()  {
        String externalChildLeId = "externalChildId";
        String externalParentLeId = rootLegalEntity.getExternalId();
        String childUserName = "testUserName";

        LegalEntity childLe = new LegalEntity()
            .withExternalId(externalChildLeId)
            .withName("child le")
            .withParent(rootLegalEntity)
            .withType(LegalEntityType.BANK);
        LegalEntity childLeSaved = legalEntityJpaRepository.save(childLe);

        ServiceAgreement childMsa = new ServiceAgreement()
            .withName("childMsa")
            .withDescription("childMsa")
            .withExternalId("externalChildSaid")
            .withCreatorLegalEntity(childLeSaved)
            .withMaster(true);
        childMsa.addParticipant(new Participant()
            .withShareUsers(true)
            .withShareAccounts(true)
            .withLegalEntity(childLeSaved));
        ServiceAgreement childMsaSaved = serviceAgreementJpaRepository.save(childMsa);


        // When
        ForbiddenException exception = assertThrows(ForbiddenException.class, () -> executeServiceRequest(
            new UrlBuilder(GET_MASTER_SERVICE_AGREEMENT_BY_EXTERNAL_LEGAL_ENTITY_PRESENTATION_URL)
                .addPathParameter(externalParentLeId).build(), "", childUserName, childMsaSaved.getId(), contextUserId,
            childLeSaved.getId(), HttpMethod.GET, new HashMap<>()));

        assertThat(exception, new ForbiddenErrorMatcher(LegalEntityErrorCodes.ERR_AG_013.getErrorMessage(),
            LegalEntityErrorCodes.ERR_AG_013.getErrorCode()));
    }
}