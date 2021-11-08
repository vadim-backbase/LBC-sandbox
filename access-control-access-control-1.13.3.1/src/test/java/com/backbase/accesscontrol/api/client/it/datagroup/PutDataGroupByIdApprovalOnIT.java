package com.backbase.accesscontrol.api.client.it.datagroup;

import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_103;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_028;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_083;
import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_001;
import static com.backbase.accesscontrol.util.helpers.ResourceAndFunctionNameConstants.ENTITLEMENTS_MANAGE_DATA_GROUPS;
import static com.backbase.accesscontrol.util.helpers.ResourceAndFunctionNameConstants.PRIVILEGE_EDIT;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertThrows;

import com.backbase.accesscontrol.api.TestDbWireMock;
import com.backbase.accesscontrol.domain.ApprovalDataGroupDetails;
import com.backbase.accesscontrol.domain.DataGroup;
import com.backbase.accesscontrol.domain.LegalEntity;
import com.backbase.accesscontrol.domain.Participant;
import com.backbase.accesscontrol.domain.ServiceAgreement;
import com.backbase.accesscontrol.domain.enums.LegalEntityType;
import com.backbase.accesscontrol.matchers.BadRequestErrorMatcher;
import com.backbase.accesscontrol.matchers.NotFoundErrorMatcher;
import com.backbase.accesscontrol.util.helpers.DataGroupUtil;
import com.backbase.accesscontrol.util.helpers.LegalEntityUtil;
import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.buildingblocks.presentation.errors.NotFoundException;
import com.backbase.dbs.approval.api.client.v2.model.ApprovalDto;
import com.backbase.dbs.approval.api.client.v2.model.ApprovalStatus;
import com.backbase.dbs.approval.api.client.v2.model.PresentationPostApprovalResponse;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.datagroups.DataGroupByIdPutRequestBody;
import java.util.Collections;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = {
    "backbase.approval.validation.enabled=true"}
)
public class PutDataGroupByIdApprovalOnIT extends TestDbWireMock {


    private static final String postApprovalsUrl = baseServiceUrl + "/approvals";

    private static final String postApprovalStatussUrl = baseServiceUrl + "/approvals/approvalId/status";

    private String url = "/accessgroups/data-groups/";

    private DataGroup dataGroup;

    @Before
    public void setUp() {
        LegalEntity legalEntity = LegalEntityUtil
            .createLegalEntity(null, "le-name", "ex-id3", null, LegalEntityType.BANK);
        legalEntityJpaRepository.save(legalEntity);

        dataGroup = DataGroupUtil
            .createDataGroup("dg-name", "ARRANGEMENTS", "description", rootMsa);

        dataGroup = dataGroupJpaRepository.save(dataGroup);
    }


    @Test
    @SuppressWarnings("squid:S2699")
    public void shouldSuccessfullyUpdateDataGroup() throws Exception {
        DataGroupByIdPutRequestBody updateBody = new DataGroupByIdPutRequestBody()
            .withId(dataGroup.getId())
            .withDescription("desc.dg")
            .withApprovalId("1235e686d31e4216b3dd5d66161d536d")
            .withName("dg-name")
            .withServiceAgreementId(rootMsa.getId())
            .withType("ARRANGEMENTS")
            .withItems(Collections.singletonList("item 1"));

        String contextSA = getUuid();
        ApprovalDto approval = new ApprovalDto()
            .id("approvalId")
            .userId("user")
            .serviceAgreementId(contextSA)
            .status(ApprovalStatus.PENDING)
            .itemId("someItemId")
            .resource("Entitlements")
            .function("Manage Data Groups")
            .action("CREATE");

        PresentationPostApprovalResponse approvalResponse = new PresentationPostApprovalResponse()
            .approval(approval);

        addStubPost(postApprovalsUrl, approvalResponse, 200);

        executeClientRequest(url + "/" + updateBody.getId(),
            HttpMethod.PUT, updateBody, "user", ENTITLEMENTS_MANAGE_DATA_GROUPS, PRIVILEGE_EDIT);

    }

    @Test
    public void shouldThrowBadRequestIfDataGroupDoesNotExist() {
        DataGroupByIdPutRequestBody updateBody = new DataGroupByIdPutRequestBody()
            .withId(getUuid())
            .withDescription("desc.dg")
            .withApprovalId("1235e686d31e4216b3dd5d66161d536d")
            .withName("dg-name")
            .withServiceAgreementId(rootMsa.getId())
            .withType("ARRANGEMENTS")
            .withItems(Collections.singletonList("item 1"));

        String contextSA = getUuid();
        ApprovalDto approval = new ApprovalDto()
            .id("approvalId")
            .userId("user")
            .serviceAgreementId(contextSA)
            .status(ApprovalStatus.PENDING)
            .itemId("someItemId")
            .resource("Entitlements")
            .function("Manage Data Groups")
            .action("CREATE");

        PresentationPostApprovalResponse approvalResponse = new PresentationPostApprovalResponse()
            .approval(approval);

        addStubPost(postApprovalsUrl, approvalResponse, 200);

        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> executeClientRequest(url + "/" + updateBody.getId(),
                HttpMethod.PUT, updateBody, "user", ENTITLEMENTS_MANAGE_DATA_GROUPS, PRIVILEGE_EDIT));

        assertThat(exception, new NotFoundErrorMatcher(ERR_ACQ_001.getErrorMessage(), ERR_ACQ_001.getErrorCode()));
    }

    @Test
    public void shouldThrowBadRequestIfDataGroupNameIsNotUnique() {
        String existingDgName = "random-name";
        DataGroup existingDataGroup = DataGroupUtil
            .createDataGroup(existingDgName, "ARRANGEMENTS", "desc", rootMsa);

        dataGroupJpaRepository.save(existingDataGroup);

        DataGroupByIdPutRequestBody dataGroupWithNonExistingSa = new DataGroupByIdPutRequestBody()
            .withId(dataGroup.getId())
            .withDescription("desc.dg")
            .withApprovalId("1235e686d31e4216b3dd5d66161d536d")
            .withName(existingDgName)
            .withServiceAgreementId(rootMsa.getId())
            .withType("ARRANGEMENTS")
            .withItems(Collections.singletonList("item 1"));

        String contextSA = getUuid();
        ApprovalDto approval = new ApprovalDto()
            .id("approvalId")
            .userId("user")
            .serviceAgreementId(contextSA)
            .status(ApprovalStatus.PENDING)
            .itemId("someItemId")
            .resource("Entitlements")
            .function("Manage Data Groups")
            .action("CREATE");

        PresentationPostApprovalResponse approvalResponse = new PresentationPostApprovalResponse()
            .approval(approval);

        addStubPost(postApprovalsUrl, approvalResponse, 200);

        addStubPut(postApprovalStatussUrl, new ResponseEntity<Void>(HttpStatus.OK), 200);

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> executeClientRequest(url + "/" + dataGroupWithNonExistingSa.getId(),
                HttpMethod.PUT, dataGroupWithNonExistingSa, "user", ENTITLEMENTS_MANAGE_DATA_GROUPS, PRIVILEGE_EDIT));

        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_028.getErrorMessage(), ERR_ACC_028.getErrorCode()));
    }

    @Test
    public void shouldThrowBadRequestIfDataGroupExistInTemporaryPendingTable() {
        DataGroupByIdPutRequestBody updateBody = new DataGroupByIdPutRequestBody()
            .withId(dataGroup.getId())
            .withDescription("desc.dg")
            .withApprovalId("1235e686d31e4216b3dd5d66161d536d")
            .withName("dg-new-name")
            .withServiceAgreementId(rootMsa.getId())
            .withType("ARRANGEMENTS")
            .withItems(Collections.singletonList("item 1"));

        ApprovalDataGroupDetails approvalDataGroupDetails = new ApprovalDataGroupDetails();
        approvalDataGroupDetails.setApprovalId("1235e686d31e4216b3dd5d66161d536d");
        approvalDataGroupDetails.setName("dg-new-name");
        approvalDataGroupDetails.setServiceAgreementId(rootMsa.getId());
        approvalDataGroupDetails.setType("ARRANGEMENTS");
        approvalDataGroupDetails.setDescription("desc.dg");
        approvalDataGroupDetailsJpaRepository.save(approvalDataGroupDetails);

        String contextSA = getUuid();
        ApprovalDto approval = new ApprovalDto()
            .id("approvalId")
            .userId("user")
            .serviceAgreementId(contextSA)
            .status(ApprovalStatus.PENDING)
            .itemId("someItemId")
            .resource("Entitlements")
            .function("Manage Data Groups")
            .action("CREATE");

        PresentationPostApprovalResponse approvalResponse = new PresentationPostApprovalResponse()
            .approval(approval);

        addStubPost(postApprovalsUrl, approvalResponse, 200);
        addStubPut(postApprovalStatussUrl, new ResponseEntity<Void>(HttpStatus.OK), 200);

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> executeClientRequest(url + "/" + updateBody.getId(),
                HttpMethod.PUT, updateBody, "user", ENTITLEMENTS_MANAGE_DATA_GROUPS, PRIVILEGE_EDIT));

        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_083.getErrorMessage(), ERR_ACC_083.getErrorCode()));
    }

    @Test
    @SuppressWarnings("squid:S2699")
    public void shouldUpdateDataGroupUnderCSA() throws Exception {

        ServiceAgreement custom = new ServiceAgreement()
            .withName("customMsa")
            .withDescription("customMsa")
            .withExternalId("externalCustomMsa")
            .withCreatorLegalEntity(rootLegalEntity)
            .withMaster(false);
        custom.addParticipant(new Participant()
            .withShareUsers(true)
            .withShareAccounts(true)
            .withLegalEntity(rootLegalEntity));
        custom = serviceAgreementJpaRepository.save(custom);

        DataGroup dataGroup2 = DataGroupUtil
            .createDataGroup("dg-name", "ARRANGEMENTS", "description", custom);

        dataGroup2 = dataGroupJpaRepository.save(dataGroup2);

        DataGroupByIdPutRequestBody updateBody = new DataGroupByIdPutRequestBody()
            .withId(dataGroup2.getId())
            .withDescription("desc.dg")
            .withApprovalId("1235e686d31e4216b3dd5d66161d536d")
            .withName("dg-name2")
            .withServiceAgreementId(custom.getId())
            .withType("ARRANGEMENTS")
            .withItems(Collections.singletonList("item 1"));

        String contextSA = getUuid();
        ApprovalDto approval = new ApprovalDto()
            .id("approvalId")
            .userId("user")
            .serviceAgreementId(contextSA)
            .status(ApprovalStatus.PENDING)
            .itemId("someItemId")
            .resource("Entitlements")
            .function("Manage Data Groups")
            .action("CREATE");

        PresentationPostApprovalResponse approvalResponse = new PresentationPostApprovalResponse()
            .approval(approval);

        addStubPost(postApprovalsUrl, approvalResponse, 200);

        executeClientRequest(url + "/" + updateBody.getId(),
            HttpMethod.PUT, updateBody, "user", ENTITLEMENTS_MANAGE_DATA_GROUPS, PRIVILEGE_EDIT);

    }

    @Test
    public void shouldCatchBadRequestErrorForNotAllowedDataGroupType() {
        DataGroupByIdPutRequestBody updateBody = new DataGroupByIdPutRequestBody()
            .withId(dataGroup.getId())
            .withDescription("desc.dg")
            .withApprovalId("1235e686d31e4216b3dd5d66161d536d")
            .withName("dg-name")
            .withServiceAgreementId(rootMsa.getId())
            .withType("CUSTOMERS")
            .withItems(Collections.singletonList("item 1"));

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> executeClientRequest(url + "/" + updateBody.getId(),
                HttpMethod.PUT, updateBody, "user", ENTITLEMENTS_MANAGE_DATA_GROUPS, PRIVILEGE_EDIT));

        assertThat(exception, new BadRequestErrorMatcher(ERR_AG_103.getErrorMessage(), ERR_AG_103.getErrorCode()));
    }
}