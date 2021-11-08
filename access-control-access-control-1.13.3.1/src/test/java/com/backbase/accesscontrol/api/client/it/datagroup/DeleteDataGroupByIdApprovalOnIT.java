package com.backbase.accesscontrol.api.client.it.datagroup;

import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_083;
import static com.backbase.accesscontrol.util.helpers.ResourceAndFunctionNameConstants.ENTITLEMENTS_MANAGE_DATA_GROUPS;
import static com.backbase.accesscontrol.util.helpers.ResourceAndFunctionNameConstants.PRIVILEGE_DELETE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertThrows;

import com.backbase.accesscontrol.api.TestDbWireMock;
import com.backbase.accesscontrol.api.client.DataGroupClientController;
import com.backbase.accesscontrol.domain.ApprovalDataGroupDetails;
import com.backbase.accesscontrol.domain.DataGroup;
import com.backbase.accesscontrol.domain.LegalEntity;
import com.backbase.accesscontrol.domain.enums.LegalEntityType;
import com.backbase.accesscontrol.matchers.BadRequestErrorMatcher;
import com.backbase.accesscontrol.util.helpers.DataGroupUtil;
import com.backbase.accesscontrol.util.helpers.LegalEntityUtil;
import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.dbs.approval.api.client.v2.model.ApprovalDto;
import com.backbase.dbs.approval.api.client.v2.model.ApprovalStatus;
import com.backbase.dbs.approval.api.client.v2.model.PresentationPostApprovalResponse;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

/**
 * Test for {@link DataGroupClientController#deleteDataGroupById}
 */
@TestPropertySource(properties = {
    "backbase.approval.validation.enabled=true"}
)
public class DeleteDataGroupByIdApprovalOnIT extends TestDbWireMock {

    private static final String postApprovalsUrl = baseServiceUrl
        + "/approvals";
    private String url = "/accessgroups/data-groups/";

    private static final String postApprovalStatussUrl = baseServiceUrl
        + "/approvals/approvalId/status";
    private DataGroup dataGroup;

    @Before
    public void setUp() {
        LegalEntity legalEntity = LegalEntityUtil
            .createLegalEntity(null, "le-name", "ex-id3", null, LegalEntityType.BANK);
        legalEntityJpaRepository.save(legalEntity);
        dataGroup = DataGroupUtil.
            createDataGroup("dg-name", "ARRANGEMENTS", "description", rootMsa);

        dataGroup = dataGroupJpaRepository.save(dataGroup);
    }

    @Test
    @SuppressWarnings("squid:S2699")
    public void shouldSuccessfullyDeleteDataGroup() throws Exception {
        ApprovalDto approval = new ApprovalDto()
            .id("approvalId")
            .userId("user")
            .serviceAgreementId("said")
            .status(ApprovalStatus.PENDING)
            .itemId("someItemId")
            .resource("Entitlements")
            .function("Manage Data Groups")
            .action("DELETE");

        PresentationPostApprovalResponse approvalResponse = new PresentationPostApprovalResponse()
            .approval(approval);

        addStubPost(postApprovalsUrl, approvalResponse, 200);

        executeClientRequest(url + "/" + dataGroup.getId(), HttpMethod.DELETE, "user",
            ENTITLEMENTS_MANAGE_DATA_GROUPS, PRIVILEGE_DELETE);
    }


    @Test
    public void shouldThrowBadRequestIfDataGroupExistInTemporaryPendingTable() {
        ApprovalDto approval = new ApprovalDto()
            .id("approvalId")
            .userId("user")
            .serviceAgreementId("said")
            .status(ApprovalStatus.PENDING)
            .itemId("someItemId")
            .resource("Entitlements")
            .function("Manage Data Groups")
            .action("DELETE");

        PresentationPostApprovalResponse approvalResponse = new PresentationPostApprovalResponse()
            .approval(approval);

        addStubPost(postApprovalsUrl, approvalResponse, 200);
        addStubPut(postApprovalStatussUrl, new ResponseEntity<Void>(HttpStatus.OK), 200);

        ApprovalDataGroupDetails approvalDataGroupDetails = new ApprovalDataGroupDetails();
        approvalDataGroupDetails.setApprovalId("1235e686d31e4216b3dd5d66161d536d");
        approvalDataGroupDetails.setName("dg-name");
        approvalDataGroupDetails.setServiceAgreementId(rootMsa.getId());
        approvalDataGroupDetails.setType("ARRANGEMENTS");
        approvalDataGroupDetails.setDescription("desc.dg");
        approvalDataGroupDetails.setDataGroupId(dataGroup.getId());
        approvalDataGroupDetailsJpaRepository.save(approvalDataGroupDetails);

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> executeClientRequest(url + "/" + dataGroup.getId(), HttpMethod.DELETE, "user",
                ENTITLEMENTS_MANAGE_DATA_GROUPS, PRIVILEGE_DELETE));

        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_083.getErrorMessage(), ERR_ACC_083.getErrorCode()));
    }


}
