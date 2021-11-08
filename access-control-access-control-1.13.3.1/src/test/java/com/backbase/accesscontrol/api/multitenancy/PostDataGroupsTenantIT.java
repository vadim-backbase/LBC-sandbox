package com.backbase.accesscontrol.api.multitenancy;

import static com.backbase.accesscontrol.util.helpers.ResourceAndFunctionNameConstants.ENTITLEMENTS_MANAGE_DATA_GROUPS;
import static com.backbase.accesscontrol.util.helpers.ResourceAndFunctionNameConstants.PRIVILEGE_CREATE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.backbase.accesscontrol.api.MultiTenantTestDbWireMock;
import com.backbase.buildingblocks.multitenancy.TenantContext;
import com.backbase.dbs.approval.api.client.v2.model.ApprovalDto;
import com.backbase.dbs.approval.api.client.v2.model.ApprovalStatus;
import com.backbase.dbs.approval.api.client.v2.model.PresentationPostApprovalResponse;
import com.backbase.presentation.accessgroup.event.spec.v1.DataGroupBase;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.datagroups.DataGroupsPostResponseBody;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpMethod;

public class PostDataGroupsTenantIT extends MultiTenantTestDbWireMock {

    private static final String postApprovalsUrl = baseServiceUrl
        + "/approvals";
    private static final String url = "/accessgroups/data-groups";

    private String tenantId = "T2";

    @Before
    public void setUp() {

        TenantContext.setTenant(tenantProvider.findTenantById(tenantId).orElse(null));
        rootDataSetup();
        TenantContext.clear();
    }

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

        ApprovalDto approval = new ApprovalDto()
            .id(approvalId)
            .userId("user")
            .serviceAgreementId("said")
            .status(ApprovalStatus.PENDING)
            .itemId(someItemId)
            .resource("Entitlements")
            .function("Manage Data Groups")
            .action("CREATE");

        PresentationPostApprovalResponse approvalResponse = new PresentationPostApprovalResponse()
            .approval(approval);

        DataGroupsPostResponseBody dataGroupsApprovalCreatePostResponseBody =
            new DataGroupsPostResponseBody().withId(approvalId);

        addStubPost(postApprovalsUrl, approvalResponse, 200, Collections.singletonMap("X-TID", tenantId),
            new HashMap<>());

        String responseAsString = executeClientRequest(url, HttpMethod.POST, postData, "user",
            Collections.singletonMap("X-TID", tenantId), ENTITLEMENTS_MANAGE_DATA_GROUPS, PRIVILEGE_CREATE);

        DataGroupsPostResponseBody dataGroupsPostResponseBody = readValue(
            responseAsString,
            DataGroupsPostResponseBody.class
        );
        assertNotNull(dataGroupsPostResponseBody);
        assertEquals(dataGroupsApprovalCreatePostResponseBody.getId(), dataGroupsPostResponseBody.getId());
    }

}
