package com.backbase.accesscontrol.api.client.it.approval;

import static com.backbase.accesscontrol.util.helpers.ResourceAndFunctionNameConstants.PRIVILEGE_APPROVE;
import static com.backbase.accesscontrol.util.helpers.ResourceAndFunctionNameConstants.SERVICE_AGREEMENT_FUNCTION_NAME;
import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.backbase.accesscontrol.api.TestDbWireMock;
import com.backbase.accesscontrol.api.client.ApprovalsController;
import com.backbase.accesscontrol.domain.ApprovalServiceAgreement;
import com.backbase.accesscontrol.domain.ApprovalServiceAgreementRef;
import com.backbase.accesscontrol.util.helpers.UrlBuilder;
import com.backbase.dbs.approval.api.client.v2.model.PresentationApprovalDetailDto;
import com.backbase.dbs.approval.api.client.v2.model.PresentationGetApprovalDetailResponse;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.PresentationApprovalAction;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.ServiceAgreementApprovalDetailsItem;
import java.util.Date;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.TestPropertySource;

/**
 * Test for {@link ApprovalsController#getServiceAgreementApprovalDetailsItem}
 */
@TestPropertySource(properties = {
    "backbase.approval.validation.enabled=true",
    "backbase.approval.level.enabled=true"
}
)
public class GetServiceAgreementApprovalDetailsItemIT extends TestDbWireMock {

    public static String URL = "/accessgroups/approvals/{approvalId}/service-agreement";
    public static String getApprovalUrl = "/service-api/v2/approvals/{approvalId}";


    @Test
    public void shouldGetDetailsForPendingUpdateServiceAgreement() throws Exception {
        String userId = contextUserId;
        String approvalId = "606d4532-f8d9-4a5f-36kl-887baf88fa24";
        String saId = rootMsa.getId();

        ApprovalServiceAgreement approvalServiceAgreement = new ApprovalServiceAgreement();
        approvalServiceAgreement.setApprovalId(approvalId);
        approvalServiceAgreement.setServiceAgreementId(saId);
        approvalServiceAgreement.setName("appName");
        approvalServiceAgreement.setDescription("appDesc");
        approvalServiceAgreement.setStartDate(new Date());
        approvalServiceAgreement.setEndDate(new Date());
        approvalServiceAgreement.setCreatorLegalEntityId(rootMsa.getCreatorLegalEntity().getId());
        approvalServiceAgreementJpaRepository.save(approvalServiceAgreement);

        PresentationGetApprovalDetailResponse presentationGetApprovalDetailResponse =
            new PresentationGetApprovalDetailResponse()
                .approvalDetails(new PresentationApprovalDetailDto()
                    .function("Manage Service Agreements")
                    .userId(userId)
                    .serviceAgreementId(saId));

        addStubGet(new UrlBuilder(getApprovalUrl)
                .addPathParameter(approvalId)
                .addQueryParameter("serviceAgreementId", rootMsa.getId())
                .addQueryParameter("userId",userId)
                .addQueryParameter("enrichUsersWithFullName", "false")
                .build(),
            presentationGetApprovalDetailResponse, 200);

        String response = executeClientRequest(new UrlBuilder(URL).addPathParameter(approvalId).build(),
            HttpMethod.GET, "USER", SERVICE_AGREEMENT_FUNCTION_NAME, PRIVILEGE_APPROVE);
        ServiceAgreementApprovalDetailsItem responseObject = readValue(response,
            ServiceAgreementApprovalDetailsItem.class);

        assertEquals(PresentationApprovalAction.EDIT, responseObject.getAction());
        assertEquals(saId, responseObject.getServiceAgreementId());
        assertEquals(approvalId, responseObject.getApprovalId());
        assertEquals("appName", responseObject.getNewState().getName());
        assertEquals(rootMsa.getName(), responseObject.getOldState().getName());
    }

    @Test
    public void shouldGetDetailsForPendingCreateServiceAgreement() throws Exception {
        String userId = contextUserId;
        String approvalId = "606d4532-f8d9-4a5f-36kl-887baf88fa24";

        ApprovalServiceAgreement approvalServiceAgreement = new ApprovalServiceAgreement();
        approvalServiceAgreement.setApprovalId(approvalId);
        approvalServiceAgreement.setServiceAgreementId(null);
        approvalServiceAgreement.setName("appName");
        approvalServiceAgreement.setDescription("appDesc");
        approvalServiceAgreement.setStartDate(new Date());
        approvalServiceAgreement.setEndDate(new Date());
        approvalServiceAgreement.setCreatorLegalEntityId(rootMsa.getCreatorLegalEntity().getId());
        approvalServiceAgreementJpaRepository.save(approvalServiceAgreement);

        PresentationGetApprovalDetailResponse presentationGetApprovalDetailResponse =
            new PresentationGetApprovalDetailResponse()
                .approvalDetails(new PresentationApprovalDetailDto()
                    .function("Manage Service Agreements")
                    .userId(userId)
                    .serviceAgreementId(rootMsa.getId()));

        addStubGet(new UrlBuilder(getApprovalUrl)
                .addPathParameter(approvalId)
                .addQueryParameter("serviceAgreementId", rootMsa.getId())
                .addQueryParameter("userId",userId)
                .addQueryParameter("enrichUsersWithFullName", "false")
                .build(),
            presentationGetApprovalDetailResponse, 200);

        String response = executeClientRequest(new UrlBuilder(URL).addPathParameter(approvalId).build(),
            HttpMethod.GET, "USER", SERVICE_AGREEMENT_FUNCTION_NAME, PRIVILEGE_APPROVE);
        ServiceAgreementApprovalDetailsItem responseObject = readValue(response,
            ServiceAgreementApprovalDetailsItem.class);

        assertEquals(PresentationApprovalAction.CREATE, responseObject.getAction());
        assertEquals(approvalId, responseObject.getApprovalId());
        assertEquals("appName", responseObject.getNewState().getName());
        assertEquals("appDesc", responseObject.getNewState().getDescription());
        assertNull(responseObject.getOldState());
    }


    @Test
    public void shouldGetDetailsForPendingDeleteServiceAgreement() throws Exception {
        String userId = contextUserId;
        String approvalId = "606d4532-f8d9-4a5f-36kl-887baf88fa24";
        String saId = rootMsa.getId();

        ApprovalServiceAgreementRef approvalServiceAgreement = new ApprovalServiceAgreementRef();
        approvalServiceAgreement.setApprovalId(approvalId);
        approvalServiceAgreement.setServiceAgreementId(saId);
        approvalServiceAgreementRefJpaRepository.save(approvalServiceAgreement);

        PresentationGetApprovalDetailResponse presentationGetApprovalDetailResponse =
            new PresentationGetApprovalDetailResponse()
                .approvalDetails(new PresentationApprovalDetailDto()
                    .function("Manage Service Agreements")
                    .userId(userId)
                    .serviceAgreementId(saId));

        addStubGet(new UrlBuilder(getApprovalUrl)
                .addPathParameter(approvalId)
                .addQueryParameter("serviceAgreementId", rootMsa.getId())
                .addQueryParameter("userId",userId)
                .addQueryParameter("enrichUsersWithFullName", "false")
                .build(),
            presentationGetApprovalDetailResponse, 200);

        String response = executeClientRequest(new UrlBuilder(URL).addPathParameter(approvalId).build(),
            HttpMethod.GET, "USER", SERVICE_AGREEMENT_FUNCTION_NAME, PRIVILEGE_APPROVE);
        ServiceAgreementApprovalDetailsItem responseObject = readValue(response,
            ServiceAgreementApprovalDetailsItem.class);

        assertEquals(PresentationApprovalAction.DELETE, responseObject.getAction());
        assertEquals(saId, responseObject.getServiceAgreementId());
        assertEquals(approvalId, responseObject.getApprovalId());
        assertNull(responseObject.getNewState());
    }
}
