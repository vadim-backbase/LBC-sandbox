package com.backbase.accesscontrol.api.client.it.seviceagreement;

import static com.backbase.accesscontrol.util.constants.ResourceAndFunctionNameConstants.MANAGE_SERVICE_AGREEMENTS_FUNCTION_NAME;
import static com.backbase.accesscontrol.util.helpers.ResourceAndFunctionNameConstants.PRIVILEGE_VIEW;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.backbase.accesscontrol.api.TestDbWireMock;
import com.backbase.accesscontrol.domain.ApprovalServiceAgreement;
import com.backbase.accesscontrol.util.helpers.DateFormatterUtil;
import com.backbase.accesscontrol.util.helpers.UrlBuilder;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.serviceagreements.ServiceAgreementItem;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.serviceagreements.Status;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementItemGetResponseBody;
import java.util.Date;
import org.junit.Test;
import org.springframework.http.HttpMethod;

public class GetServiceAgreementByIdIT extends TestDbWireMock {

    public static final String USER = "USER";
    private static final String url = "/accessgroups/serviceagreements/{serviceAgreementId}";

    @Test
    public void testGetServiceAgreementById() throws Exception {
        String serviceAgreementName = "name";
        String serviceAgreementId = "005";
        String externalIdKey = "externalId";
        String externalIdValue = "ex123";

        Date from = rootMsa.getStartDate();
        Date until = rootMsa.getEndDate();

        ServiceAgreementItem serviceAgreementByIdData = new ServiceAgreementItem()
            .withName(serviceAgreementName)
            .withStatus(Status.DISABLED)
            .withValidFrom(from)
            .withValidUntil(until)
            .withId(serviceAgreementId);
        serviceAgreementByIdData.withAddition(externalIdKey, externalIdValue);

        String responseAsString = executeClientRequest(new UrlBuilder(url).addPathParameter(rootMsa.getId()).build(),
            HttpMethod.GET, null, "user",
            MANAGE_SERVICE_AGREEMENTS_FUNCTION_NAME, PRIVILEGE_VIEW);

        ServiceAgreementItemGetResponseBody returnedServiceAgreement = objectMapper
            .readValue(responseAsString, ServiceAgreementItemGetResponseBody.class);

        assertEquals(rootMsa.getId(), returnedServiceAgreement.getId());
        assertNull(returnedServiceAgreement.getApprovalId());
        assertEquals(rootMsa.getName(), returnedServiceAgreement.getName());
        assertEquals(Status.ENABLED.toString(), returnedServiceAgreement.getStatus().toString());
        assertEquals(DateFormatterUtil.utcFormatDateOnly(from), returnedServiceAgreement.getValidFromDate());
        assertEquals(DateFormatterUtil.utcFormatTimeOnly(from), returnedServiceAgreement.getValidFromTime());
        assertEquals(DateFormatterUtil.utcFormatDateOnly(until), returnedServiceAgreement.getValidUntilDate());
        assertEquals(DateFormatterUtil.utcFormatTimeOnly(until), returnedServiceAgreement.getValidUntilTime());
    }

    @Test
    public void testGetServiceAgreementByIdWithApprovalIdWhenPending() throws Exception {
        Date from = rootMsa.getStartDate();
        Date until = rootMsa.getEndDate();

        ApprovalServiceAgreement approvalSa = new ApprovalServiceAgreement();
        approvalSa.setApprovalId("appId");
        approvalSa.setServiceAgreementId(rootMsa.getId());
        approvalSa.setExternalId(rootMsa.getExternalId());
        approvalSa.setName("name");
        approvalSa.setDescription("desc");
        approvalSa.setCreatorLegalEntityId("cle");

        approvalServiceAgreementJpaRepository.save(approvalSa);
        String responseAsString = executeClientRequest(new UrlBuilder(url).addPathParameter(rootMsa.getId()).build(),
            HttpMethod.GET, null, "user",
            MANAGE_SERVICE_AGREEMENTS_FUNCTION_NAME, PRIVILEGE_VIEW);

        ServiceAgreementItemGetResponseBody returnedServiceAgreement = objectMapper
            .readValue(responseAsString, ServiceAgreementItemGetResponseBody.class);

        assertEquals(approvalSa.getApprovalId(), returnedServiceAgreement.getApprovalId());
        assertEquals(rootMsa.getId(), returnedServiceAgreement.getId());
        assertEquals(rootMsa.getName(), returnedServiceAgreement.getName());
        assertEquals(Status.ENABLED.toString(), returnedServiceAgreement.getStatus().toString());
        assertEquals(DateFormatterUtil.utcFormatDateOnly(from), returnedServiceAgreement.getValidFromDate());
        assertEquals(DateFormatterUtil.utcFormatTimeOnly(from), returnedServiceAgreement.getValidFromTime());
        assertEquals(DateFormatterUtil.utcFormatDateOnly(until), returnedServiceAgreement.getValidUntilDate());
        assertEquals(DateFormatterUtil.utcFormatTimeOnly(until), returnedServiceAgreement.getValidUntilTime());
    }
}
