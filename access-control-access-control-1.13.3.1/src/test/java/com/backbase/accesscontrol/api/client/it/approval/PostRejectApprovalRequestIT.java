package com.backbase.accesscontrol.api.client.it.approval;

import static com.backbase.accesscontrol.util.constants.ResourceAndFunctionNameConstants.FUNCTION_ASSIGN_PERMISSONS;
import static com.backbase.accesscontrol.util.constants.ResourceAndFunctionNameConstants.PRIVILEGE_APPROVE;
import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_049;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import com.backbase.accesscontrol.api.TestDbWireMock;
import com.backbase.accesscontrol.api.client.ApprovalsController;
import com.backbase.accesscontrol.domain.ApprovalUserContext;
import com.backbase.accesscontrol.matchers.NotFoundErrorMatcher;
import com.backbase.accesscontrol.util.helpers.UrlBuilder;
import com.backbase.buildingblocks.presentation.errors.NotFoundException;
import com.backbase.dbs.approval.api.client.v2.model.ApprovalDto;
import com.backbase.dbs.approval.api.client.v2.model.PresentationApprovalDetailDto;
import com.backbase.dbs.approval.api.client.v2.model.PresentationGetApprovalDetailResponse;
import com.backbase.dbs.approval.api.client.v2.model.PresentationPostApprovalResponse;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.ApprovalStatus;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.PresentationApprovalStatus;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.TestPropertySource;

/**
 * Test for {@link ApprovalsController#postRejectApprovalRequest}
 */
@TestPropertySource(properties = "backbase.approval.validation.enabled=true")
public class PostRejectApprovalRequestIT extends TestDbWireMock {


    private static final String URL = "/accessgroups/approvals/{approvalId}/reject";
    private static final String POST_APPROVAL = "/service-api/v2/approvals/{approvalId}/records";
    private static final String GET_APPROVAL_BY_ID = "/service-api/v2/approvals/{approvalId}";

    @Test
    public void shouldRejectApprovalRequest() throws Exception {
        String serviceAgreementId = rootMsa.getId();
        String userId = contextUserId;
        String approvalId = getUuid();

        ApprovalUserContext approvalUserContext = new ApprovalUserContext();
        approvalUserContext.setLegalEntityId(getUuid());
        approvalUserContext.setServiceAgreementId(getUuid());
        approvalUserContext.setUserId(getUuid());
        approvalUserContext.setApprovalId(getUuid());
        approvalUserContext.setApprovalId(approvalId);
        approvalUserContextJpaRepository.save(approvalUserContext);

        ApprovalDto approval = new ApprovalDto()
            .status(com.backbase.dbs.approval.api.client.v2.model.ApprovalStatus.REJECTED.REJECTED)
            .serviceAgreementId(serviceAgreementId)
            .userId(userId);

        PresentationPostApprovalResponse responseData = new PresentationPostApprovalResponse()
            .approval(approval);
        addStubPost(new UrlBuilder(POST_APPROVAL).addPathParameter(approvalId).build(), responseData, 200);

        PresentationApprovalStatus approvalStatus = new PresentationApprovalStatus()
            .withApprovalStatus(ApprovalStatus.REJECTED);

        PresentationGetApprovalDetailResponse presentationGetApprovalDetailResponse = new PresentationGetApprovalDetailResponse()
            .approvalDetails(new PresentationApprovalDetailDto()
                .function("Assign Permissions")
                .userId(userId)
                .serviceAgreementId(serviceAgreementId));

        addStubGet(new UrlBuilder(GET_APPROVAL_BY_ID).addPathParameter(approvalId)
                .addQueryParameter("serviceAgreementId", serviceAgreementId)
                .addQueryParameter("userId", userId)
                .addQueryParameter("enrichUsersWithFullName", "false").build(),
            presentationGetApprovalDetailResponse, 200);

        String contentAsString = executeClientRequest(new UrlBuilder(URL).addPathParameter(approvalId).build(),
            HttpMethod.POST, "USER",
            FUNCTION_ASSIGN_PERMISSONS, PRIVILEGE_APPROVE);
        PresentationApprovalStatus presentationApprovalStatus = readValue(contentAsString,
            PresentationApprovalStatus.class);

        assertEquals(approvalStatus.getApprovalStatus(), presentationApprovalStatus.getApprovalStatus());
    }

    @Test
    public void shouldThrowNotFoundIfApprovalDoesNotExist() {
        String serviceAgreementId = rootMsa.getId();
        String userId = contextUserId;
        String approvalId = getUuid();

        ApprovalDto approval = new ApprovalDto()
            .status(com.backbase.dbs.approval.api.client.v2.model.ApprovalStatus.REJECTED)
            .serviceAgreementId(serviceAgreementId)
            .userId(userId);

        PresentationPostApprovalResponse responseData = new PresentationPostApprovalResponse()
            .approval(approval);
        addStubPost(new UrlBuilder(POST_APPROVAL).addPathParameter(approvalId).build(), responseData, 200);

        PresentationGetApprovalDetailResponse presentationGetApprovalDetailResponse = new PresentationGetApprovalDetailResponse()
            .approvalDetails(new PresentationApprovalDetailDto()
                .function("Assign Permissions")
                .userId(userId)
                .serviceAgreementId(serviceAgreementId));

        addStubGet(new UrlBuilder(GET_APPROVAL_BY_ID).addPathParameter(approvalId)
                .addQueryParameter("serviceAgreementId", serviceAgreementId)
                .addQueryParameter("userId", userId)
                .addQueryParameter("enrichUsersWithFullName", "false").build(),
            presentationGetApprovalDetailResponse, 200);

        NotFoundException exception = assertThrows(
            NotFoundException.class,
            () -> executeClientRequest(new UrlBuilder(URL).addPathParameter(approvalId).build(),
                HttpMethod.POST, "USER",
                FUNCTION_ASSIGN_PERMISSONS, PRIVILEGE_APPROVE));

        assertThat(exception, new NotFoundErrorMatcher(ERR_ACQ_049.getErrorMessage(), ERR_ACQ_049.getErrorCode()));
    }
}
