package com.backbase.accesscontrol.api.client.it.datagroup;

import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_074;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_083;
import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_001;
import static com.backbase.accesscontrol.util.helpers.ResourceAndFunctionNameConstants.ENTITLEMENTS_MANAGE_DATA_GROUPS;
import static com.backbase.accesscontrol.util.helpers.ResourceAndFunctionNameConstants.PRIVILEGE_DELETE;
import static com.backbase.pandp.accesscontrol.event.spec.v1.Action.DELETE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertThrows;

import com.backbase.accesscontrol.api.TestDbWireMock;
import com.backbase.accesscontrol.api.client.DataGroupClientController;
import com.backbase.accesscontrol.domain.ApprovalDataGroupDetails;
import com.backbase.accesscontrol.domain.ApprovalUserContext;
import com.backbase.accesscontrol.domain.ApprovalUserContextAssignFunctionGroup;
import com.backbase.accesscontrol.domain.DataGroup;
import com.backbase.accesscontrol.domain.FunctionGroup;
import com.backbase.accesscontrol.domain.LegalEntity;
import com.backbase.accesscontrol.domain.enums.FunctionGroupType;
import com.backbase.accesscontrol.domain.enums.LegalEntityType;
import com.backbase.accesscontrol.matchers.BadRequestErrorMatcher;
import com.backbase.accesscontrol.matchers.NotFoundErrorMatcher;
import com.backbase.accesscontrol.util.helpers.DataGroupUtil;
import com.backbase.accesscontrol.util.helpers.FunctionGroupUtil;
import com.backbase.accesscontrol.util.helpers.LegalEntityUtil;
import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.buildingblocks.presentation.errors.NotFoundException;
import com.backbase.pandp.accesscontrol.event.spec.v1.DataGroupEvent;
import com.google.common.collect.Sets;
import java.io.IOException;
import java.util.HashSet;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.TestPropertySource;

/**
 * Test for {@link DataGroupClientController#deleteDataGroupById(String, HttpServletRequest, HttpServletResponse)}
 */
@TestPropertySource(properties = {
    "backbase.approval.validation.enabled=false"}
)
public class DeleteDataGroupByIdIT extends TestDbWireMock {

    private String url = "/accessgroups/data-groups/";

    private DataGroup dataGroup;
    private FunctionGroup functionGroup;
    private LegalEntity legalEntity;

    @Before
    public void setUp() {
        legalEntity = LegalEntityUtil
            .createLegalEntity(null, "le-name", "ex-id3", null, LegalEntityType.BANK);
        legalEntityJpaRepository.save(legalEntity);
        dataGroup = DataGroupUtil.
            createDataGroup("dg-name", "ARRANGEMENTS", "description", rootMsa);

        dataGroup = dataGroupJpaRepository.save(dataGroup);

        functionGroup = functionGroupJpaRepository.save(
            FunctionGroupUtil
                .getFunctionGroup(null, "fg-name", "desc", new HashSet<>(), FunctionGroupType.DEFAULT,
                    rootMsa));
    }

    @Test
    @SuppressWarnings("squid:S2699")
    public void shouldSuccessfullyDeleteDataGroup() throws IOException, JSONException {
        executeClientRequest(url + "/" + dataGroup.getId(), HttpMethod.DELETE, "user",
            ENTITLEMENTS_MANAGE_DATA_GROUPS, PRIVILEGE_DELETE);
        verifyDataGroupEvents(Sets.newHashSet(new DataGroupEvent()
            .withAction(DELETE)
            .withId(dataGroup.getId())));
    }

    @Test
    public void shouldThrowNotFoundIfDataGroupDoesNotExist() {

        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> executeClientRequest(url + "/" + "random", HttpMethod.DELETE, "user",
                ENTITLEMENTS_MANAGE_DATA_GROUPS, PRIVILEGE_DELETE));

        assertThat(exception, new NotFoundErrorMatcher(ERR_ACQ_001.getErrorMessage(), ERR_ACQ_001.getErrorCode()));
    }

    @Test
    public void shouldThrowBadRequestIfDataGroupExistInTemporaryPendingTable() {

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

    @Test
    public void shouldThrowBadRequestIfThereIsPendingAssignmentForDataGroup() {
        createPendingRequestForDataGroup();

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> executeClientRequest(url + "/" + dataGroup.getId(), HttpMethod.DELETE, "user",
                ENTITLEMENTS_MANAGE_DATA_GROUPS, PRIVILEGE_DELETE));

        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_074.getErrorMessage(), ERR_ACC_074.getErrorCode()));
    }

    private void createPendingRequestForDataGroup() {

        ApprovalUserContext approvalUserContext = new ApprovalUserContext(UUID.randomUUID().toString(), rootMsa.getId(),
            legalEntity.getId(), new HashSet<>());
        approvalUserContext.setApprovalId("1");
        approvalUserContext = approvalUserContextJpaRepository.save(approvalUserContext);

        ApprovalUserContextAssignFunctionGroup approvalUserContextAssignFunctionGroup =
            new ApprovalUserContextAssignFunctionGroup(null, functionGroup.getId(),
                approvalUserContext, Sets.newHashSet(dataGroup.getId()), null);
        approvalUserContextAssignFunctionGroupJpaRepository.save(approvalUserContextAssignFunctionGroup);
    }

}
