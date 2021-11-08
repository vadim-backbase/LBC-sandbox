package com.backbase.accesscontrol.api.client.it.approval;

import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_067;
import static com.backbase.accesscontrol.util.helpers.ResourceAndFunctionNameConstants.ENTITLEMENTS_MANAGE_FUNCTION_GROUPS;
import static com.backbase.accesscontrol.util.helpers.ResourceAndFunctionNameConstants.PRIVILEGE_APPROVE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import com.backbase.accesscontrol.api.TestDbWireMock;
import com.backbase.accesscontrol.domain.ApprovalFunctionGroup;
import com.backbase.accesscontrol.domain.ApprovalFunctionGroupRef;
import com.backbase.accesscontrol.domain.FunctionGroup;
import com.backbase.accesscontrol.domain.enums.FunctionGroupType;
import com.backbase.accesscontrol.matchers.NotFoundErrorMatcher;
import com.backbase.buildingblocks.presentation.errors.NotFoundException;
import com.backbase.dbs.approval.api.client.v2.model.ApprovalTypeDto;
import com.backbase.dbs.approval.api.client.v2.model.GetApprovalTypeResponse;
import com.backbase.dbs.approval.api.client.v2.model.PresentationApprovalDetailDto;
import com.backbase.dbs.approval.api.client.v2.model.PresentationGetApprovalDetailResponse;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.PresentationApprovalAction;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.PresentationFunctionGroupApprovalDetailsItem;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.PresentationFunctionGroupState;
import java.io.IOException;
import org.json.JSONException;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = {
    "backbase.approval.validation.enabled=true",
    "backbase.approval.level.enabled=true"
}
)
public class GetFunctionGroupApprovalDetailsItemIT extends TestDbWireMock {

    public static String URL = "/accessgroups/approvals/%s/function-group";

    @Test
    public void testShouldGetFunctionGroupApprovalById() throws IOException, JSONException {
        String userId = contextUserId;
        String approvalId = "606d4532-f8d9-4a5f-36kl-887baf88fa24";
        String saId = rootMsa.getId();

        FunctionGroup functionGroup = new FunctionGroup()
            .withType(FunctionGroupType.DEFAULT)
            .withName("fgApproval")
            .withDescription("Desc")
            .withServiceAgreement(rootMsa);
        FunctionGroup functionGroupSave = functionGroupJpaRepository.save(functionGroup);
        String functionGroupId = functionGroupSave.getId();
        ApprovalFunctionGroupRef approval = new ApprovalFunctionGroupRef();
        approval.setFunctionGroupId(functionGroupId);
        approval.setApprovalId(approvalId);
        approvalFunctionGroupRefJpaRepository.save(approval);
        PresentationFunctionGroupState oldState = new PresentationFunctionGroupState()
            .withName(functionGroup.getName())
            .withApprovalTypeId("approvalTypeIdA")
            .withDescription(functionGroup.getDescription());
        PresentationFunctionGroupApprovalDetailsItem presentationFunctionGroupApprovalDetailsItem =
            new PresentationFunctionGroupApprovalDetailsItem()
                .withFunctionGroupId(functionGroupId)
                .withServiceAgreementId(rootMsa.getId())
                .withServiceAgreementName(rootMsa.getName())
                .withAction(PresentationApprovalAction.DELETE)
                .withApprovalId(approvalId)
                .withOldState(oldState);

        PresentationGetApprovalDetailResponse presentationGetApprovalDetailResponse =
            new PresentationGetApprovalDetailResponse()
                .approvalDetails(new PresentationApprovalDetailDto()
                    .function("Manage Function Groups")
                    .userId(userId)
                    .serviceAgreementId(saId));

        GetApprovalTypeResponse persistenceGetApprovalTypeResponse = new GetApprovalTypeResponse()
            .approvalType(new ApprovalTypeDto().id("approvalTypeIdA"));

        addStubGet(
            "/service-api/v2/approvals/" + approvalId + "?serviceAgreementId=" + saId
                + "&userId=" + userId
                + "&enrichUsersWithFullName=false",
            presentationGetApprovalDetailResponse, 200);

        addStubGet(
            "/service-api/v2/approval-type-assignments/" + functionGroupId,
            persistenceGetApprovalTypeResponse, 200);

        String response = executeClientRequest(String.format(URL, approvalId),
            HttpMethod.GET, "USER", ENTITLEMENTS_MANAGE_FUNCTION_GROUPS, PRIVILEGE_APPROVE);
        PresentationFunctionGroupApprovalDetailsItem responseObject = readValue(response,
            PresentationFunctionGroupApprovalDetailsItem.class);
        assertEquals(presentationFunctionGroupApprovalDetailsItem, responseObject);
    }

    @Test
    public void shouldGetDetailsForPendingCreateFunctionGroup() throws Exception {
        String userId = contextUserId;
        String approvalId = "606d4532-f8d9-4a5f-36kl-887baf88fa24";
        String saId = rootMsa.getId();

        ApprovalFunctionGroup approvalFunctionGroup = new ApprovalFunctionGroup();
        approvalFunctionGroup.setApprovalId(approvalId);
        approvalFunctionGroup.setServiceAgreementId(saId);
        approvalFunctionGroup.setName("appName");
        approvalFunctionGroup.setDescription("appDesc");
        approvalFunctionGroupJpaRepository.save(approvalFunctionGroup);

        PresentationFunctionGroupState newState = new PresentationFunctionGroupState()
            .withName(approvalFunctionGroup.getName())
            .withDescription(approvalFunctionGroup.getDescription());
        PresentationFunctionGroupApprovalDetailsItem presentationFunctionGroupApprovalDetailsItem =
            new PresentationFunctionGroupApprovalDetailsItem()
                .withServiceAgreementId(rootMsa.getId())
                .withServiceAgreementName(rootMsa.getName())
                .withAction(PresentationApprovalAction.CREATE)
                .withApprovalId(approvalId)
                .withNewState(newState);

        PresentationGetApprovalDetailResponse presentationGetApprovalDetailResponse =
            new PresentationGetApprovalDetailResponse()
                .approvalDetails(new PresentationApprovalDetailDto()
                    .function("Manage Function Groups")
                    .userId(userId)
                    .serviceAgreementId(saId));

        addStubGet(
            "/service-api/v2/approvals/" + approvalId + "?serviceAgreementId=" + saId
                + "&userId=" + userId
                + "&enrichUsersWithFullName=false",
            presentationGetApprovalDetailResponse, 200);

        String response = executeClientRequest(String.format(URL, approvalId),
            HttpMethod.GET, "USER", ENTITLEMENTS_MANAGE_FUNCTION_GROUPS, PRIVILEGE_APPROVE);
        PresentationFunctionGroupApprovalDetailsItem responseObject = readValue(response,
            PresentationFunctionGroupApprovalDetailsItem.class);
        assertEquals(presentationFunctionGroupApprovalDetailsItem, responseObject);
    }

    @Test
    public void shouldGetDetailsForPendingUpdateFunctionGroup() throws IOException, JSONException {

        String userId = contextUserId;
        String approvalId = "606d4532-f8d9-4a5f-36kl-887baf88fa24";
        String saId = rootMsa.getId();

        FunctionGroup functionGroup = new FunctionGroup()
            .withType(FunctionGroupType.DEFAULT)
            .withName("fgApproval")
            .withDescription("Desc")
            .withServiceAgreement(rootMsa);
        FunctionGroup functionGroupSave = functionGroupJpaRepository.save(functionGroup);

        ApprovalFunctionGroup approvalFunctionGroup = new ApprovalFunctionGroup();
        approvalFunctionGroup.setApprovalId(approvalId);
        approvalFunctionGroup.setFunctionGroupId(functionGroup.getId());
        approvalFunctionGroup.setServiceAgreementId(rootMsa.getId());
        approvalFunctionGroup.setName("appName");
        approvalFunctionGroup.setDescription("appDesc");
        approvalFunctionGroupJpaRepository.save(approvalFunctionGroup);
        String functionGroupId = functionGroupSave.getId();

        PresentationFunctionGroupState oldState = new PresentationFunctionGroupState()
            .withName(functionGroup.getName())
            .withApprovalTypeId("approvalTypeIdA")
            .withDescription(functionGroup.getDescription());
        PresentationFunctionGroupState newState = new PresentationFunctionGroupState()
            .withName(approvalFunctionGroup.getName())
            .withDescription(approvalFunctionGroup.getDescription());
        PresentationFunctionGroupApprovalDetailsItem presentationFunctionGroupApprovalDetailsItem =
            new PresentationFunctionGroupApprovalDetailsItem()
                .withFunctionGroupId(functionGroupId)
                .withServiceAgreementId(rootMsa.getId())
                .withServiceAgreementName(rootMsa.getName())
                .withAction(PresentationApprovalAction.EDIT)
                .withApprovalId(approvalId)
                .withOldState(oldState)
                .withNewState(newState);

        PresentationGetApprovalDetailResponse presentationGetApprovalDetailResponse =
            new PresentationGetApprovalDetailResponse()
                .approvalDetails(new PresentationApprovalDetailDto()
                    .function("Manage Function Groups")
                    .userId(userId)
                    .serviceAgreementId(saId));

        GetApprovalTypeResponse persistenceGetApprovalTypeResponse = new GetApprovalTypeResponse()
            .approvalType(new ApprovalTypeDto().id("approvalTypeIdA"));

        addStubGet(
            "/service-api/v2/approvals/" + approvalId + "?serviceAgreementId=" + saId
                + "&userId=" + userId
                + "&enrichUsersWithFullName=false",
            presentationGetApprovalDetailResponse, 200);

        addStubGet(
            "/service-api/v2/approval-type-assignments/" + functionGroupId,
            persistenceGetApprovalTypeResponse, 200);

        String response = executeClientRequest(String.format(URL, approvalId),
            HttpMethod.GET, "USER", ENTITLEMENTS_MANAGE_FUNCTION_GROUPS, PRIVILEGE_APPROVE);
        PresentationFunctionGroupApprovalDetailsItem responseObject = readValue(response,
            PresentationFunctionGroupApprovalDetailsItem.class);
        assertEquals(presentationFunctionGroupApprovalDetailsItem, responseObject);
    }

    @Test
    public void shouldThrowNotFoundWhenApprovalRequestDoesNotExist() {
        String userId = contextUserId;
        String approvalId = "606d4532-f8d9-4a5f-36kl-887baf88fa24";
        String saId = rootMsa.getId();
        PresentationGetApprovalDetailResponse presentationGetApprovalDetailResponse =
            new PresentationGetApprovalDetailResponse()
                .approvalDetails(new PresentationApprovalDetailDto()
                    .function("Manage Function Groups")
                    .userId(userId)
                    .serviceAgreementId(saId));

        addStubGet(
            "/service-api/v2/approvals/" + approvalId + "?serviceAgreementId=" + saId
                + "&userId=" + userId
                + "&enrichUsersWithFullName=false",
            presentationGetApprovalDetailResponse, 200);

        NotFoundException exception = assertThrows(
            NotFoundException.class, () ->  executeClientRequest(String.format(URL, approvalId),
            HttpMethod.GET, "USER", ENTITLEMENTS_MANAGE_FUNCTION_GROUPS, PRIVILEGE_APPROVE));
        assertThat(exception, new NotFoundErrorMatcher(ERR_ACQ_067.getErrorMessage(), ERR_ACQ_067.getErrorCode()));
    }

}
