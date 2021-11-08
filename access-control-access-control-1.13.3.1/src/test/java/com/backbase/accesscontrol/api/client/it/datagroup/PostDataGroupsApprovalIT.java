package com.backbase.accesscontrol.api.client.it.datagroup;

import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_032;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_028;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_082;
import static com.backbase.accesscontrol.util.helpers.ResourceAndFunctionNameConstants.ENTITLEMENTS_MANAGE_DATA_GROUPS;
import static com.backbase.accesscontrol.util.helpers.ResourceAndFunctionNameConstants.PRIVILEGE_CREATE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;

import com.backbase.accesscontrol.api.TestDbWireMock;
import com.backbase.accesscontrol.domain.ApprovalDataGroupDetails;
import com.backbase.accesscontrol.domain.DataGroup;
import com.backbase.accesscontrol.util.helpers.DataGroupUtil;
import com.backbase.accesscontrol.util.helpers.UrlBuilder;
import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.buildingblocks.presentation.errors.ForbiddenException;
import com.backbase.dbs.approval.api.client.v2.model.ApprovalDto;
import com.backbase.dbs.approval.api.client.v2.model.ApprovalStatus;
import com.backbase.dbs.approval.api.client.v2.model.PresentationPostApprovalResponse;
import com.backbase.presentation.accessgroup.event.spec.v1.DataGroupBase;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.datagroups.DataGroupsPostResponseBody;
import java.util.Arrays;
import java.util.Collections;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = {
    "backbase.approval.validation.enabled=true"}
)
public class PostDataGroupsApprovalIT extends TestDbWireMock {


    private String approvalId = getUuid();
    private static final String postApprovalsUrl = baseServiceUrl
        + "/approvals";

    private static final String cancelApprovalUrl = baseServiceUrl
        + "/approvals/{approvalId}/status";
    private static final String url = "/accessgroups/data-groups";

    @Test
    public void testCreatePendingDataGroupWithApprovalOn() throws Exception {
        String dgName = "Name";
        String description = "Test Description";
        String serviceAgreementId = rootMsa.getId();
        String approvalId = getUuid();
        String someItemId = getUuid();

        DataGroupBase postData = new DataGroupBase()
            .withName(dgName)
            .withDescription(description)
            .withServiceAgreementId(serviceAgreementId)
            .withItems(Arrays.asList("1", "2"))
            .withType("ARRANGEMENTS");

        String contextSA = getUuid();

        ApprovalDto approval = new ApprovalDto()
            .id(approvalId)
            .userId("user")
            .serviceAgreementId(contextSA)
            .status(ApprovalStatus.PENDING)
            .itemId(someItemId)
            .resource("Entitlements")
            .function("Manage Data Groups")
            .action("CREATE");

        PresentationPostApprovalResponse approvalResponse = new PresentationPostApprovalResponse()
            .approval(approval);

        DataGroupsPostResponseBody dataGroupsApprovalCreatePostResponseBody =
            new DataGroupsPostResponseBody().withId(approvalId);

        addStubPost(postApprovalsUrl, approvalResponse, 200);

        String responseAsString = executeClientRequest(url, HttpMethod.POST, postData, "user",
            ENTITLEMENTS_MANAGE_DATA_GROUPS, PRIVILEGE_CREATE);

        DataGroupsPostResponseBody dataGroupsPostResponseBody = readValue(
            responseAsString,
            DataGroupsPostResponseBody.class
        );
        assertNotNull(dataGroupsPostResponseBody);
        assertEquals(dataGroupsApprovalCreatePostResponseBody.getId(), dataGroupsPostResponseBody.getId());
    }

    @Test
    public void shouldThrowBadRequestIfServiceAgreementDoesNotExist() throws Exception {
        DataGroupBase dataGroupWithNonExistingSa = new DataGroupBase()
            .withDescription("desc.dg")
            .withName("dg-name")
            .withServiceAgreementId(getUuid())
            .withType("ARRANGEMENTS")
            .withItems(Collections.singletonList("item 1"));

        ForbiddenException exception = assertThrows(ForbiddenException.class,
            () -> {
                executeClientRequest(url, HttpMethod.POST, dataGroupWithNonExistingSa, "user",
                    ENTITLEMENTS_MANAGE_DATA_GROUPS, PRIVILEGE_CREATE);
            });

        assertEquals(ERR_AG_032.getErrorCode(), exception.getErrors().get(0).getKey());
        assertEquals(ERR_AG_032.getErrorMessage(), exception.getErrors().get(0).getMessage());
    }

    @Test
    public void shouldThrowBadRequestIfDataGroupNameIsNotUnique() throws Exception {
        String existingDgName = "dg-name";
        DataGroup existingDataGroup = DataGroupUtil
            .createDataGroup(existingDgName, "ARRANGEMENTS", "desc", rootMsa);

        dataGroupJpaRepository.save(existingDataGroup);

        DataGroupBase dataGroupWithNonExistingSa = new DataGroupBase()
            .withDescription("desc.dg")
            .withName(existingDgName)
            .withServiceAgreementId(rootMsa.getId())
            .withType("ARRANGEMENTS")
            .withItems(Collections.singletonList("item 1"));

        ApprovalDto approval = new ApprovalDto()
            .id(approvalId)
            .userId("user")
            .serviceAgreementId(rootMsa.getId())
            .status(ApprovalStatus.PENDING)
            .itemId("itemid")
            .resource("Entitlements")
            .function("Manage Data Groups")
            .action("CREATE");

        PresentationPostApprovalResponse approvalResponse = new PresentationPostApprovalResponse()
            .approval(approval);

        addStubPost(postApprovalsUrl, approvalResponse, 200);

        addStubPut(new UrlBuilder(cancelApprovalUrl)
            .addPathParameter(approvalId).build(), null, 200);

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> {
                executeClientRequest(url, HttpMethod.POST, dataGroupWithNonExistingSa, "user",
                    ENTITLEMENTS_MANAGE_DATA_GROUPS, PRIVILEGE_CREATE);
            });

        assertEquals(ERR_ACC_028.getErrorCode(), exception.getErrors().get(0).getKey());
        assertEquals(ERR_ACC_028.getErrorMessage(), exception.getErrors().get(0).getMessage());
    }

    @Test
    public void shouldThrowBadRequestIfDataGroupExistInTemporaryPendingTable() throws Exception {

        DataGroupBase dataGroupApprovalCreate = new DataGroupBase()
            .withDescription("desc.dg")
            .withName("dg-name")
            .withServiceAgreementId(rootMsa.getId())
            .withType("ARRANGEMENTS")
            .withItems(Collections.singletonList("item 1"));
        ApprovalDataGroupDetails approvalDataGroupDetails = new ApprovalDataGroupDetails();
        approvalDataGroupDetails.setApprovalId("1235e686d31e4216b3dd5d66161d536d");
        approvalDataGroupDetails.setName("dg-name");
        approvalDataGroupDetails.setServiceAgreementId(rootMsa.getId());
        approvalDataGroupDetails.setType("ARRANGEMENTS");
        approvalDataGroupDetails.setDescription("desc.dg");
        approvalDataGroupDetailsJpaRepository.save(approvalDataGroupDetails);

        ApprovalDto approval = new ApprovalDto()
            .id(approvalId)
            .userId(contextUserId)
            .serviceAgreementId(rootMsa.getId())
            .status(ApprovalStatus.PENDING)
            .itemId(getUuid())
            .resource("Entitlements")
            .function("Manage Data Groups")
            .action("CREATE");

        PresentationPostApprovalResponse approvalResponse = new PresentationPostApprovalResponse()
            .approval(approval);

        addStubPost(postApprovalsUrl, approvalResponse, 200);

        addStubPut(new UrlBuilder(cancelApprovalUrl)
            .addPathParameter(approvalId).build(), null, 200);

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> {
                executeClientRequest(url, HttpMethod.POST, dataGroupApprovalCreate, "user",
                    ENTITLEMENTS_MANAGE_DATA_GROUPS, PRIVILEGE_CREATE);
            });

        assertEquals(ERR_ACC_082.getErrorCode(), exception.getErrors().get(0).getKey());
        assertEquals(ERR_ACC_082.getErrorMessage(), exception.getErrors().get(0).getMessage());
    }
}
