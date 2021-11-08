package com.backbase.accesscontrol.api.client.it.approval;

import static com.backbase.accesscontrol.domain.GraphConstants.DATA_GROUP_EXTENDED;
import static com.backbase.accesscontrol.domain.enums.FunctionGroupType.DEFAULT;
import static com.backbase.accesscontrol.util.constants.ResourceAndFunctionNameConstants.ACTION_CREATE;
import static com.backbase.accesscontrol.util.constants.ResourceAndFunctionNameConstants.ACTION_DELETE;
import static com.backbase.accesscontrol.util.constants.ResourceAndFunctionNameConstants.ACTION_EDIT;
import static com.backbase.accesscontrol.util.constants.ResourceAndFunctionNameConstants.ENTITLEMENTS_MANAGE_DATA_GROUPS;
import static com.backbase.accesscontrol.util.constants.ResourceAndFunctionNameConstants.ENTITLEMENTS_MANAGE_FUNCTION_GROUPS;
import static com.backbase.accesscontrol.util.constants.ResourceAndFunctionNameConstants.FUNCTION_ASSIGN_PERMISSONS;
import static com.backbase.accesscontrol.util.constants.ResourceAndFunctionNameConstants.MANAGE_SERVICE_AGREEMENTS_FUNCTION_NAME;
import static com.backbase.accesscontrol.util.constants.ResourceAndFunctionNameConstants.PRIVILEGE_APPROVE;
import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_049;
import static com.backbase.pandp.accesscontrol.event.spec.v1.Action.ADD;
import static com.backbase.pandp.accesscontrol.event.spec.v1.Action.DELETE;
import static com.backbase.pandp.accesscontrol.event.spec.v1.Action.UPDATE;
import static com.google.common.collect.Sets.newHashSet;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import com.backbase.accesscontrol.api.TestDbWireMock;
import com.backbase.accesscontrol.api.client.ApprovalsController;
import com.backbase.accesscontrol.domain.ApprovalDataGroup;
import com.backbase.accesscontrol.domain.ApprovalDataGroupDetails;
import com.backbase.accesscontrol.domain.ApprovalFunctionGroup;
import com.backbase.accesscontrol.domain.ApprovalFunctionGroupRef;
import com.backbase.accesscontrol.domain.ApprovalServiceAgreement;
import com.backbase.accesscontrol.domain.ApprovalUserContext;
import com.backbase.accesscontrol.domain.ApprovalUserContextAssignFunctionGroup;
import com.backbase.accesscontrol.domain.AssignablePermissionSet;
import com.backbase.accesscontrol.domain.DataGroup;
import com.backbase.accesscontrol.domain.FunctionGroup;
import com.backbase.accesscontrol.domain.ServiceAgreement;
import com.backbase.accesscontrol.domain.ServiceAgreementState;
import com.backbase.accesscontrol.domain.enums.AssignablePermissionType;
import com.backbase.accesscontrol.matchers.NotFoundErrorMatcher;
import com.backbase.accesscontrol.util.helpers.UrlBuilder;
import com.backbase.buildingblocks.presentation.errors.NotFoundException;
import com.backbase.dbs.approval.api.client.v2.model.PresentationApprovalDetailDto;
import com.backbase.dbs.approval.api.client.v2.model.PresentationGetApprovalDetailResponse;
import com.backbase.dbs.approval.api.client.v2.model.PresentationPostApprovalResponse;
import com.backbase.dbs.approval.api.client.v2.model.ApprovalDto;
import com.backbase.pandp.accesscontrol.event.spec.v1.DataGroupEvent;
import com.backbase.pandp.accesscontrol.event.spec.v1.FunctionGroupEvent;
import com.backbase.pandp.accesscontrol.event.spec.v1.ServiceAgreementEvent;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.ApprovalStatus;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.PresentationApprovalStatus;
import com.google.common.collect.Sets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.TestPropertySource;

/**
 * Test for {@link ApprovalsController#postApproveApprovalRequest}
 */
@TestPropertySource(properties = "backbase.approval.validation.enabled=true")
public class PostApproveApprovalRequestIT extends TestDbWireMock {

    private static final String URL = "/accessgroups/approvals/{approvalId}/approve";
    public static final String GET_APPROVAL_BY_ID = "/service-api/v2/approvals/{approvalId}";
    public static final String POST_APPROVAL = "/service-api/v2/approvals/{approvalId}/records";

    @Test
    public void shouldApproveApprovalRequestUsingDefaultApproverService() throws Exception {

        String serviceAgreementId = rootMsa.getId();
        String legalEntityId = rootLegalEntity.getId();
        String approvalId = getUuid();

        ApprovalUserContext approvalUserContext = new ApprovalUserContext();
        approvalUserContext.setLegalEntityId(legalEntityId);
        approvalUserContext.setServiceAgreementId(serviceAgreementId);
        approvalUserContext.setUserId(contextUserId);
        approvalUserContext.setApprovalId(UUID.randomUUID().toString());
        approvalUserContext.setApprovalId(approvalId);
        approvalUserContextJpaRepository.save(approvalUserContext);

        ApprovalDto approvalDto = new ApprovalDto()
            .function(FUNCTION_ASSIGN_PERMISSONS)
            .userId(contextUserId)
            .serviceAgreementId(serviceAgreementId)
            .status(com.backbase.dbs.approval.api.client.v2.model.ApprovalStatus.APPROVED);
        PresentationPostApprovalResponse postApprovalResponse = new PresentationPostApprovalResponse()
            .approval(approvalDto);

        PresentationApprovalDetailDto presentationApprovalDetailDto = new PresentationApprovalDetailDto()
            .function(FUNCTION_ASSIGN_PERMISSONS)
            .userId(contextUserId)
            .serviceAgreementId(serviceAgreementId)
            .status(com.backbase.dbs.approval.api.client.v2.model.ApprovalStatus.APPROVED);
        PresentationGetApprovalDetailResponse approvalDetails = new PresentationGetApprovalDetailResponse()
            .approvalDetails(presentationApprovalDetailDto);

        addStubPost(new UrlBuilder(POST_APPROVAL)
                .addPathParameter(approvalId)
                .build(),
            postApprovalResponse,
            200);

        addStubGet(
            new UrlBuilder(GET_APPROVAL_BY_ID)
                .addPathParameter(approvalId)
                .addQueryParameter("serviceAgreementId", serviceAgreementId)
                .addQueryParameter("userId", contextUserId)
                .addQueryParameter("enrichUsersWithFullName", "false")
                .build(),
            approvalDetails,
            200);

        String response = executeClientRequest(new UrlBuilder(URL).addPathParameter(approvalId).build(),
            HttpMethod.POST, "USER", FUNCTION_ASSIGN_PERMISSONS, PRIVILEGE_APPROVE);

        PresentationApprovalStatus expectedResult = new PresentationApprovalStatus()
            .withApprovalStatus(ApprovalStatus.APPROVED);
        PresentationApprovalStatus responseData = readValue(response, PresentationApprovalStatus.class);

        assertEquals(expectedResult, responseData);
    }

    @Test
    public void testShouldApproveApprovalRequestForAssignPermissions() throws Exception {
        String approvalId = getUuid();

        FunctionGroup functionGroup = new FunctionGroup()
            .withType(DEFAULT)
            .withServiceAgreement(rootMsa)
            .withName("fn_name")
            .withDescription("fn_description");
        functionGroupJpaRepository.saveAndFlush(functionGroup);

        DataGroup dataGroup = new DataGroup()
            .withDataItemType("ARRANGEMENTS")
            .withName("dg_name")
            .withServiceAgreement(rootMsa)
            .withDescription("dg_description");
        dataGroupJpaRepository.saveAndFlush(dataGroup);

        ApprovalUserContext approvalUserContext = new ApprovalUserContext();
        approvalUserContext.setLegalEntityId(rootLegalEntity.getId());
        approvalUserContext.setServiceAgreementId(rootMsa.getId());
        approvalUserContext.setUserId(contextUserId);
        approvalUserContext.setApprovalId(approvalId);
        approvalUserContext.getApprovalUserContextAssignFunctionGroups().add(
            new ApprovalUserContextAssignFunctionGroup()
                .withApprovalUserContext(approvalUserContext)
                .withFunctionGroupId(functionGroup.getId()).withDataGroups(newHashSet(dataGroup.getId())));

        approvalUserContextJpaRepository.save(approvalUserContext);

        ApprovalDto approvalDto = new ApprovalDto()
            .function(FUNCTION_ASSIGN_PERMISSONS)
            .userId(contextUserId)
            .serviceAgreementId(rootMsa.getId())
            .status(com.backbase.dbs.approval.api.client.v2.model.ApprovalStatus.APPROVED);
        PresentationPostApprovalResponse postApprovalResponse = new PresentationPostApprovalResponse()
            .approval(approvalDto);

        PresentationApprovalDetailDto presentationApprovalDetailDto = new PresentationApprovalDetailDto()
            .function(FUNCTION_ASSIGN_PERMISSONS)
            .userId(contextUserId)
            .serviceAgreementId(rootMsa.getId())
            .status(com.backbase.dbs.approval.api.client.v2.model.ApprovalStatus.APPROVED);
        PresentationGetApprovalDetailResponse approvalDetails = new PresentationGetApprovalDetailResponse()
            .approvalDetails(presentationApprovalDetailDto);

        addStubPost(new UrlBuilder(POST_APPROVAL)
                .addPathParameter(approvalId)
                .build(),
            postApprovalResponse,
            200);

        addStubGet(
            new UrlBuilder(GET_APPROVAL_BY_ID)
                .addPathParameter(approvalId)
                .addQueryParameter("serviceAgreementId", rootMsa.getId())
                .addQueryParameter("userId", contextUserId)
                .addQueryParameter("enrichUsersWithFullName", "false")
                .build(),
            approvalDetails,
            200);

        assertEquals(0, userAssignedFunctionGroupJpaRepository.findAll().size());

        executeClientRequest(new UrlBuilder(URL).addPathParameter(approvalId).build(),
            HttpMethod.POST, "USER", FUNCTION_ASSIGN_PERMISSONS, PRIVILEGE_APPROVE);

        assertEquals(1L, userAssignedFunctionGroupJpaRepository.findAll().stream().filter(
            userAssignedFunctionGroup -> userAssignedFunctionGroup
                .getFunctionGroup().getId().equals(functionGroup.getId())).count());
    }

    @Test
    public void testShouldApproveApprovalRequestForDataGroup() throws Exception {
        String approvalId = getUuid();

        ApprovalDataGroupDetails approvalUserContext = new ApprovalDataGroupDetails();
        approvalUserContext.setDataGroupId(null);
        approvalUserContext.setServiceAgreementId(rootMsa.getId());
        approvalUserContext.setApprovalId(approvalId);
        approvalUserContext.setName("Dg-name1");
        approvalUserContext.setDescription("Dg-desc");
        approvalUserContext.setItems(newHashSet("items1", "item2"));
        approvalUserContext.setType("ARRANGEMENTS");

        accessControlApprovalJpaRepository.save(approvalUserContext);

        ApprovalDto approvalDto = new ApprovalDto()
            .function(ENTITLEMENTS_MANAGE_DATA_GROUPS)
            .action(ACTION_CREATE)
            .userId(contextUserId)
            .serviceAgreementId(rootMsa.getId())
            .status(com.backbase.dbs.approval.api.client.v2.model.ApprovalStatus.APPROVED);
        PresentationPostApprovalResponse postApprovalResponse = new PresentationPostApprovalResponse()
            .approval(approvalDto);

        PresentationApprovalDetailDto presentationApprovalDetailDto = new PresentationApprovalDetailDto()
            .function(ENTITLEMENTS_MANAGE_DATA_GROUPS)
            .action(ACTION_CREATE)
            .userId(contextUserId)
            .serviceAgreementId(rootMsa.getId())
            .status(com.backbase.dbs.approval.api.client.v2.model.ApprovalStatus.APPROVED);
        PresentationGetApprovalDetailResponse approvalDetails = new PresentationGetApprovalDetailResponse()
            .approvalDetails(presentationApprovalDetailDto);

        addStubPost(new UrlBuilder(POST_APPROVAL)
                .addPathParameter(approvalId)
                .build(),
            postApprovalResponse,
            200);

        addStubGet(
            new UrlBuilder(GET_APPROVAL_BY_ID)
                .addPathParameter(approvalId)
                .addQueryParameter("serviceAgreementId", rootMsa.getId())
                .addQueryParameter("userId", contextUserId)
                .addQueryParameter("enrichUsersWithFullName", "false")
                .build(),
            approvalDetails,
            200);

        assertEquals(0, dataGroupJpaRepository.findAll().size());

        executeClientRequest(new UrlBuilder(URL).addPathParameter(approvalId).build(),
            HttpMethod.POST, "USER", ENTITLEMENTS_MANAGE_DATA_GROUPS, PRIVILEGE_APPROVE);

        List<DataGroup> dataGroups = dataGroupJpaRepository.findAll();
        assertThat(dataGroups, hasSize(1));
        assertThat(dataGroups, contains(
            allOf(
                hasProperty("name", is("Dg-name1")),
                hasProperty("dataItemType", is("ARRANGEMENTS")),
                hasProperty("description", is("Dg-desc")),
                hasProperty("serviceAgreementId", is(rootMsa.getId()))
            )
        ));
        verifyDataGroupEvents(Sets.newHashSet(new DataGroupEvent()
            .withAction(ADD)
            .withId(dataGroups.get(0).getId())));
    }

    @Test
    public void testShouldApproveApprovalRequestForDataGroupUpdate() throws Exception {
        String approvalId = getUuid();

        DataGroup dataGroup = new DataGroup()
            .withName("Dg-name")
            .withDescription("Dg-desc")
            .withServiceAgreementId(rootMsa.getId())
            .withServiceAgreement(rootMsa)
            .withDataItemType("ARRANGEMENTS")
            .withDataItemIds(newHashSet("items1", "item2"));

        dataGroup = dataGroupJpaRepository.save(dataGroup);

        ApprovalDataGroupDetails approvalUserContext = new ApprovalDataGroupDetails();
        approvalUserContext.setDataGroupId(dataGroup.getId());
        approvalUserContext.setServiceAgreementId(rootMsa.getId());
        approvalUserContext.setApprovalId(approvalId);
        approvalUserContext.setName("Dg-name1");
        approvalUserContext.setDescription("Dg-desc");
        approvalUserContext.setItems(newHashSet("items1", "item2"));
        approvalUserContext.setType("ARRANGEMENTS");

        accessControlApprovalJpaRepository.save(approvalUserContext);

        ApprovalDto approvalDto = new ApprovalDto()
            .function(ENTITLEMENTS_MANAGE_DATA_GROUPS)
            .action(ACTION_EDIT)
            .userId(contextUserId)
            .serviceAgreementId(rootMsa.getId())
            .status(com.backbase.dbs.approval.api.client.v2.model.ApprovalStatus.APPROVED);
        PresentationPostApprovalResponse postApprovalResponse = new PresentationPostApprovalResponse()
            .approval(approvalDto);

        PresentationApprovalDetailDto presentationApprovalDetailDto = new PresentationApprovalDetailDto()
            .function(ENTITLEMENTS_MANAGE_DATA_GROUPS)
            .action(ACTION_EDIT)
            .userId(contextUserId)
            .serviceAgreementId(rootMsa.getId())
            .status(com.backbase.dbs.approval.api.client.v2.model.ApprovalStatus.APPROVED);
        PresentationGetApprovalDetailResponse approvalDetails = new PresentationGetApprovalDetailResponse()
            .approvalDetails(presentationApprovalDetailDto);

        addStubPost(new UrlBuilder(POST_APPROVAL)
                .addPathParameter(approvalId)
                .build(),
            postApprovalResponse,
            200);

        addStubGet(
            new UrlBuilder(GET_APPROVAL_BY_ID)
                .addPathParameter(approvalId)
                .addQueryParameter("serviceAgreementId", rootMsa.getId())
                .addQueryParameter("userId", contextUserId)
                .addQueryParameter("enrichUsersWithFullName", "false")
                .build(),
            approvalDetails,
            200);

        assertEquals("Dg-name",
            dataGroupJpaRepository.findById(dataGroup.getId(), DATA_GROUP_EXTENDED).get().getName());

        executeClientRequest(new UrlBuilder(URL).addPathParameter(approvalId).build(),
            HttpMethod.POST, "USER", ENTITLEMENTS_MANAGE_DATA_GROUPS, PRIVILEGE_APPROVE);

        assertEquals("Dg-name1",
            dataGroupJpaRepository.findById(dataGroup.getId(), DATA_GROUP_EXTENDED).get().getName());
        verifyDataGroupEvents(Sets.newHashSet(new DataGroupEvent()
            .withAction(UPDATE)
            .withId(dataGroup.getId())));
    }

    @Test
    public void testShouldApproveApprovalRequestForDataGroupDelete() throws Exception {
        String approvalId = getUuid();

        DataGroup dataGroup = new DataGroup()
            .withName("Dg-name")
            .withDescription("Dg-desc")
            .withServiceAgreementId(rootMsa.getId())
            .withServiceAgreement(rootMsa)
            .withDataItemType("ARRANGEMENTS")
            .withDataItemIds(newHashSet("items1", "item2"));
        dataGroup = dataGroupJpaRepository.save(dataGroup);

        ApprovalDataGroup approvalDataGroup = new ApprovalDataGroup();
        approvalDataGroup.setDataGroupId(dataGroup.getId());
        approvalDataGroup.setApprovalId(approvalId);
        accessControlApprovalJpaRepository.save(approvalDataGroup);

        ApprovalDto approvalDto = new ApprovalDto()
            .function(ENTITLEMENTS_MANAGE_DATA_GROUPS)
            .action(ACTION_DELETE)
            .userId(contextUserId)
            .serviceAgreementId(rootMsa.getId())
            .status(com.backbase.dbs.approval.api.client.v2.model.ApprovalStatus.APPROVED);
        PresentationPostApprovalResponse postApprovalResponse = new PresentationPostApprovalResponse()
            .approval(approvalDto);

        PresentationApprovalDetailDto presentationApprovalDetailDto = new PresentationApprovalDetailDto()
            .function(ENTITLEMENTS_MANAGE_DATA_GROUPS)
            .action(ACTION_DELETE)
            .userId(contextUserId)
            .serviceAgreementId(rootMsa.getId())
            .status(com.backbase.dbs.approval.api.client.v2.model.ApprovalStatus.APPROVED);
        PresentationGetApprovalDetailResponse approvalDetails = new PresentationGetApprovalDetailResponse()
            .approvalDetails(presentationApprovalDetailDto);

        addStubPost(new UrlBuilder(POST_APPROVAL)
                .addPathParameter(approvalId)
                .build(),
            postApprovalResponse,
            200);

        addStubGet(
            new UrlBuilder(GET_APPROVAL_BY_ID)
                .addPathParameter(approvalId)
                .addQueryParameter("serviceAgreementId", rootMsa.getId())
                .addQueryParameter("userId", contextUserId)
                .addQueryParameter("enrichUsersWithFullName", "false")
                .build(),
            approvalDetails,
            200);

        assertEquals(1, dataGroupJpaRepository.findAll().size());

        executeClientRequest(new UrlBuilder(URL).addPathParameter(approvalId).build(),
            HttpMethod.POST, "USER", ENTITLEMENTS_MANAGE_DATA_GROUPS, PRIVILEGE_APPROVE);

        assertEquals(0, dataGroupJpaRepository.findAll().size());
        verifyDataGroupEvents(Sets.newHashSet(new DataGroupEvent()
            .withAction(DELETE)
            .withId(dataGroup.getId())));
    }

    @Test
    public void testShouldApproveApprovalRequestForFunctionGroupCreate() throws Exception {
        String approvalId = getUuid();

        AssignablePermissionSet aps = createAssignablePermissionSet(
            "apsRegularCreate",
            AssignablePermissionType.CUSTOM,
            "apsRegularCreate",
            businessFunctionCache.getByFunctionIdAndPrivilege("1002", "create").getId(),
            businessFunctionCache.getByFunctionIdAndPrivilege("1002", "edit").getId()
        );
        aps = assignablePermissionSetJpaRepository.save(aps);

        rootMsa.setPermissionSetsRegular(newHashSet(aps));
        rootMsa = serviceAgreementJpaRepository.save(rootMsa);

        ApprovalFunctionGroup approvalFunctionGroup = new ApprovalFunctionGroup();
        approvalFunctionGroup.setFunctionGroupId(null);
        approvalFunctionGroup.setServiceAgreementId(rootMsa.getId());
        approvalFunctionGroup.setApprovalId(approvalId);
        approvalFunctionGroup.setName("Fg-name1");
        approvalFunctionGroup.setDescription("Fg-desc");
        approvalFunctionGroup.setStartDate(new Date());
        approvalFunctionGroup.setPrivileges(newHashSet(
            businessFunctionCache.getByFunctionIdAndPrivilege("1002", "create").getId(),
            businessFunctionCache.getByFunctionIdAndPrivilege("1002", "edit").getId()));
        accessControlApprovalJpaRepository.saveAndFlush(approvalFunctionGroup);

        ApprovalDto approvalDto = new ApprovalDto()
            .function(ENTITLEMENTS_MANAGE_FUNCTION_GROUPS)
            .action(ACTION_CREATE)
            .userId(contextUserId)
            .serviceAgreementId(rootMsa.getId())
            .status(com.backbase.dbs.approval.api.client.v2.model.ApprovalStatus.APPROVED);
        PresentationPostApprovalResponse postApprovalResponse = new PresentationPostApprovalResponse()
            .approval(approvalDto);

        PresentationApprovalDetailDto presentationApprovalDetailDto = new PresentationApprovalDetailDto()
            .function(ENTITLEMENTS_MANAGE_FUNCTION_GROUPS)
            .action(ACTION_CREATE)
            .userId(contextUserId)
            .serviceAgreementId(rootMsa.getId())
            .status(com.backbase.dbs.approval.api.client.v2.model.ApprovalStatus.APPROVED);
        PresentationGetApprovalDetailResponse approvalDetails = new PresentationGetApprovalDetailResponse()
            .approvalDetails(presentationApprovalDetailDto);

        addStubPost(new UrlBuilder(POST_APPROVAL)
                .addPathParameter(approvalId)
                .build(),
            postApprovalResponse,
            200);

        addStubGet(
            new UrlBuilder(GET_APPROVAL_BY_ID)
                .addPathParameter(approvalId)
                .addQueryParameter("serviceAgreementId", rootMsa.getId())
                .addQueryParameter("userId", contextUserId)
                .addQueryParameter("enrichUsersWithFullName", "false")
                .build(),
            approvalDetails,
            200);

        assertEquals(0, functionGroupJpaRepository.findAll().size());

        executeClientRequest(new UrlBuilder(URL).addPathParameter(approvalId).build(),
            HttpMethod.POST, "USER", ENTITLEMENTS_MANAGE_FUNCTION_GROUPS, PRIVILEGE_APPROVE);

        FunctionGroup functionGroup = functionGroupJpaRepository
            .findByNameAndServiceAgreementId("Fg-name1", rootMsa.getId()).get();
        assertEquals("Fg-name1", functionGroup.getName());
        assertEquals("Fg-desc", functionGroup.getDescription());
        assertEquals("DEFAULT", functionGroup.getType().toString());
        assertEquals(rootMsa.getId(), functionGroup.getServiceAgreementId());
        verifyFunctionGroupEvents(Sets.newHashSet(new FunctionGroupEvent()
            .withAction(ADD)
            .withId(functionGroup.getId())));
    }

    @Test
    public void testShouldApproveApprovalRequestForFunctionGroupUpdate() throws Exception {
        String approvalId = getUuid();

        AssignablePermissionSet assignablePermissionSetRegularUpdate = createAssignablePermissionSet(
            "apsRegularUpdate1",
            AssignablePermissionType.CUSTOM,
            "apsRegularIngestUpdate1",
            businessFunctionCache.getByFunctionIdAndPrivilege("1002", "create").getId(),
            businessFunctionCache.getByFunctionIdAndPrivilege("1002", "edit").getId()
        );
        assignablePermissionSetRegularUpdate = assignablePermissionSetJpaRepository
            .save(assignablePermissionSetRegularUpdate);

        rootMsa.setPermissionSetsRegular(newHashSet(assignablePermissionSetRegularUpdate));
        rootMsa = serviceAgreementJpaRepository.save(rootMsa);

        FunctionGroup functionGroup = new FunctionGroup()
            .withName("Fg-name")
            .withDescription("Fg-desc")
            .withServiceAgreement(rootMsa)
            .withType(DEFAULT);
        functionGroup = functionGroupJpaRepository.save(functionGroup);

        ApprovalFunctionGroup approvalFunctionGroup = new ApprovalFunctionGroup();
        approvalFunctionGroup.setFunctionGroupId(functionGroup.getId());
        approvalFunctionGroup.setServiceAgreementId(rootMsa.getId());
        approvalFunctionGroup.setApprovalId(approvalId);
        approvalFunctionGroup.setName("Fg-name1");
        approvalFunctionGroup.setDescription("Fg-desc");
        approvalFunctionGroup.setStartDate(new Date());
        approvalFunctionGroup.setPrivileges(newHashSet(
            businessFunctionCache.getByFunctionIdAndPrivilege("1002", "create").getId(),
            businessFunctionCache.getByFunctionIdAndPrivilege("1002", "edit").getId()));
        accessControlApprovalJpaRepository.save(approvalFunctionGroup);

        ApprovalDto approvalDto = new ApprovalDto()
            .function(ENTITLEMENTS_MANAGE_FUNCTION_GROUPS)
            .action(ACTION_EDIT)
            .userId(contextUserId)
            .serviceAgreementId(rootMsa.getId())
            .status(com.backbase.dbs.approval.api.client.v2.model.ApprovalStatus.APPROVED);
        PresentationPostApprovalResponse postApprovalResponse = new PresentationPostApprovalResponse()
            .approval(approvalDto);

        PresentationApprovalDetailDto presentationApprovalDetailDto = new PresentationApprovalDetailDto()
            .function(ENTITLEMENTS_MANAGE_FUNCTION_GROUPS)
            .action(ACTION_EDIT)
            .userId(contextUserId)
            .serviceAgreementId(rootMsa.getId())
            .status(com.backbase.dbs.approval.api.client.v2.model.ApprovalStatus.APPROVED);
        PresentationGetApprovalDetailResponse approvalDetails = new PresentationGetApprovalDetailResponse()
            .approvalDetails(presentationApprovalDetailDto);

        addStubPost(new UrlBuilder(POST_APPROVAL)
                .addPathParameter(approvalId)
                .build(),
            postApprovalResponse,
            200);

        addStubGet(
            new UrlBuilder(GET_APPROVAL_BY_ID)
                .addPathParameter(approvalId)
                .addQueryParameter("serviceAgreementId", rootMsa.getId())
                .addQueryParameter("userId", contextUserId)
                .addQueryParameter("enrichUsersWithFullName", "false")
                .build(),
            approvalDetails,
            200);

        assertEquals("Fg-name", functionGroupJpaRepository.findById(functionGroup.getId()).get().getName());

        executeClientRequest(new UrlBuilder(URL).addPathParameter(approvalId).build(),
            HttpMethod.POST, "USER", ENTITLEMENTS_MANAGE_FUNCTION_GROUPS, PRIVILEGE_APPROVE);

        assertEquals("Fg-name1", functionGroupJpaRepository.findById(functionGroup.getId()).get().getName());
        verifyFunctionGroupEvents(Sets.newHashSet(new FunctionGroupEvent()
            .withAction(UPDATE)
            .withId(functionGroup.getId())));
    }

    @Test
    public void testShouldApproveApprovalRequestForFunctionGroupDelete() throws Exception {
        String approvalId = getUuid();

        FunctionGroup functionGroup = new FunctionGroup()
            .withName("Fg-name")
            .withDescription("Fg-desc")
            .withServiceAgreement(this.rootMsa)
            .withType(DEFAULT);
        functionGroup = functionGroupJpaRepository.save(functionGroup);

        ApprovalFunctionGroupRef approvalFunctionGroupRef = new ApprovalFunctionGroupRef();
        approvalFunctionGroupRef.setApprovalId(approvalId);
        approvalFunctionGroupRef.setFunctionGroupId(functionGroup.getId());
        accessControlApprovalJpaRepository.save(approvalFunctionGroupRef);

        ApprovalDto approvalDto = new ApprovalDto()
            .function(ENTITLEMENTS_MANAGE_FUNCTION_GROUPS)
            .action(ACTION_DELETE)
            .userId(contextUserId)
            .serviceAgreementId(rootMsa.getId())
            .status(com.backbase.dbs.approval.api.client.v2.model.ApprovalStatus.APPROVED);
        PresentationPostApprovalResponse postApprovalResponse = new PresentationPostApprovalResponse()
            .approval(approvalDto);

        PresentationApprovalDetailDto presentationApprovalDetailDto = new PresentationApprovalDetailDto()
            .function(ENTITLEMENTS_MANAGE_FUNCTION_GROUPS)
            .action(ACTION_DELETE)
            .userId(contextUserId)
            .serviceAgreementId(rootMsa.getId())
            .status(com.backbase.dbs.approval.api.client.v2.model.ApprovalStatus.APPROVED);
        PresentationGetApprovalDetailResponse approvalDetails = new PresentationGetApprovalDetailResponse()
            .approvalDetails(presentationApprovalDetailDto);

        addStubPost(new UrlBuilder(POST_APPROVAL)
                .addPathParameter(approvalId)
                .build(),
            postApprovalResponse,
            200);

        addStubGet(
            new UrlBuilder(GET_APPROVAL_BY_ID)
                .addPathParameter(approvalId)
                .addQueryParameter("serviceAgreementId", rootMsa.getId())
                .addQueryParameter("userId", contextUserId)
                .addQueryParameter("enrichUsersWithFullName", "false")
                .build(),
            approvalDetails,
            200);

        // Before approve function group is present
        assertTrue(functionGroupJpaRepository.findById(functionGroup.getId()).isPresent());

        executeClientRequest(new UrlBuilder(URL).addPathParameter(approvalId).build(),
            HttpMethod.POST, "USER", ENTITLEMENTS_MANAGE_FUNCTION_GROUPS, PRIVILEGE_APPROVE);

        // After approve function group is NOT present
        assertFalse(functionGroupJpaRepository.findById(functionGroup.getId()).isPresent());
        verifyFunctionGroupEvents(Sets.newHashSet(new FunctionGroupEvent()
            .withAction(DELETE)
            .withId(functionGroup.getId())));
    }

    @Test
    public void testShouldThrowNotFoundIfApprovalDoesNotExist() throws Exception {
        String approvalId = getUuid();

        ApprovalDto approvalDto = new ApprovalDto()
            .function(FUNCTION_ASSIGN_PERMISSONS)
            .userId(contextUserId)
            .serviceAgreementId(rootMsa.getId())
            .status(com.backbase.dbs.approval.api.client.v2.model.ApprovalStatus.APPROVED);
        PresentationPostApprovalResponse postApprovalResponse = new PresentationPostApprovalResponse()
            .approval(approvalDto);

        PresentationApprovalDetailDto presentationApprovalDetailDto = new PresentationApprovalDetailDto()
            .function(FUNCTION_ASSIGN_PERMISSONS)
            .userId(contextUserId)
            .serviceAgreementId(rootMsa.getId())
            .status(com.backbase.dbs.approval.api.client.v2.model.ApprovalStatus.APPROVED);
        PresentationGetApprovalDetailResponse approvalDetails = new PresentationGetApprovalDetailResponse()
            .approvalDetails(presentationApprovalDetailDto);

        addStubPost(new UrlBuilder(POST_APPROVAL)
                .addPathParameter(approvalId)
                .build(),
            postApprovalResponse,
            200);

        addStubGet(
            new UrlBuilder(GET_APPROVAL_BY_ID)
                .addPathParameter(approvalId)
                .addQueryParameter("serviceAgreementId", rootMsa.getId())
                .addQueryParameter("userId", contextUserId)
                .addQueryParameter("enrichUsersWithFullName", "false")
                .build(),
            approvalDetails,
            200);

        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> executeClientRequest(new UrlBuilder(URL).addPathParameter(approvalId).build(),
                HttpMethod.POST, "USER", FUNCTION_ASSIGN_PERMISSONS, PRIVILEGE_APPROVE));

        assertThat(exception, new NotFoundErrorMatcher(ERR_ACQ_049.getErrorMessage(), ERR_ACQ_049.getErrorCode()));
    }

    @Test
    public void testShouldApproveApprovalRequestForServiceAgreementCreate() throws Exception {
        String approvalId = getUuid();
        String externalId = "externalSA";

        ApprovalServiceAgreement pendingServiceAgreement =
            setupPendingServiceAgreement(approvalId, externalId, null);

        ApprovalDto approvalDto = new ApprovalDto()
            .function(MANAGE_SERVICE_AGREEMENTS_FUNCTION_NAME)
            .action(ACTION_CREATE)
            .userId(contextUserId)
            .serviceAgreementId(rootMsa.getId())
            .status(com.backbase.dbs.approval.api.client.v2.model.ApprovalStatus.APPROVED);
        PresentationPostApprovalResponse postApprovalResponse = new PresentationPostApprovalResponse()
            .approval(approvalDto);

        PresentationApprovalDetailDto presentationApprovalDetailDto = new PresentationApprovalDetailDto()
            .function(MANAGE_SERVICE_AGREEMENTS_FUNCTION_NAME)
            .action(ACTION_CREATE)
            .userId(contextUserId)
            .serviceAgreementId(rootMsa.getId())
            .status(com.backbase.dbs.approval.api.client.v2.model.ApprovalStatus.APPROVED);
        PresentationGetApprovalDetailResponse approvalDetails = new PresentationGetApprovalDetailResponse()
            .approvalDetails(presentationApprovalDetailDto);

        addStubGet(
            new UrlBuilder(GET_APPROVAL_BY_ID)
                .addPathParameter(approvalId)
                .addQueryParameter("serviceAgreementId", rootMsa.getId())
                .addQueryParameter("userId", contextUserId)
                .addQueryParameter("enrichUsersWithFullName", "false")
                .build(),
            approvalDetails,
            200);

        addStubPost(new UrlBuilder(POST_APPROVAL)
                .addPathParameter(approvalId)
                .build(),
            postApprovalResponse,
            200);

        String responseAsString = executeClientRequest(new UrlBuilder(URL).addPathParameter(approvalId).build(),
            HttpMethod.POST, "USER", MANAGE_SERVICE_AGREEMENTS_FUNCTION_NAME, PRIVILEGE_APPROVE);
        PresentationApprovalStatus status = objectMapper
            .readValue(responseAsString, PresentationApprovalStatus.class);
        assertEquals("APPROVED", status.getApprovalStatus().toString());
        String id = serviceAgreementJpaRepository.findByExternalId(externalId).get().getId();
        verifyServiceAgreementEvents(Sets.newHashSet(new ServiceAgreementEvent()
            .withAction(ADD)
            .withId(id)));
    }

    @Test
    public void testShouldApproveApprovalRequestForServiceAgreementUpdate() throws Exception {
        String approvalId = getUuid();
        String externalId = "externalSA";

        ServiceAgreement serviceAgreementToUpdate = new ServiceAgreement()
            .withName("customMsa")
            .withDescription("customMsa")
            .withExternalId("externalCustomMsa")
            .withCreatorLegalEntity(rootLegalEntity)
            .withMaster(false);
        serviceAgreementToUpdate = serviceAgreementJpaRepository.save(serviceAgreementToUpdate);

        ApprovalServiceAgreement pendingServiceAgreement =
            setupPendingServiceAgreement(approvalId, externalId, serviceAgreementToUpdate.getId());

        ApprovalDto approvalDto = new ApprovalDto()
            .function(MANAGE_SERVICE_AGREEMENTS_FUNCTION_NAME)
            .action(ACTION_EDIT)
            .userId(contextUserId)
            .serviceAgreementId(rootMsa.getId())
            .status(com.backbase.dbs.approval.api.client.v2.model.ApprovalStatus.APPROVED);
        PresentationPostApprovalResponse postApprovalResponse = new PresentationPostApprovalResponse()
            .approval(approvalDto);

        PresentationApprovalDetailDto presentationApprovalDetailDto = new PresentationApprovalDetailDto()
            .function(MANAGE_SERVICE_AGREEMENTS_FUNCTION_NAME)
            .action(ACTION_EDIT)
            .userId(contextUserId)
            .serviceAgreementId(rootMsa.getId())
            .status(com.backbase.dbs.approval.api.client.v2.model.ApprovalStatus.APPROVED);
        PresentationGetApprovalDetailResponse approvalDetails = new PresentationGetApprovalDetailResponse()
            .approvalDetails(presentationApprovalDetailDto);

        addStubGet(
            new UrlBuilder(GET_APPROVAL_BY_ID)
                .addPathParameter(approvalId)
                .addQueryParameter("serviceAgreementId", rootMsa.getId())
                .addQueryParameter("userId", contextUserId)
                .addQueryParameter("enrichUsersWithFullName", "false")
                .build(),
            approvalDetails,
            200);

        addStubPost(new UrlBuilder(POST_APPROVAL)
                .addPathParameter(approvalId)
                .build(),
            postApprovalResponse,
            200);

        String responseAsString = executeClientRequest(new UrlBuilder(URL).addPathParameter(approvalId).build(),
            HttpMethod.POST, "USER", MANAGE_SERVICE_AGREEMENTS_FUNCTION_NAME, PRIVILEGE_APPROVE);
        PresentationApprovalStatus status = objectMapper
            .readValue(responseAsString, PresentationApprovalStatus.class);
        assertEquals("APPROVED", status.getApprovalStatus().toString());
        verifyServiceAgreementEvents(Sets.newHashSet(new ServiceAgreementEvent()
            .withAction(UPDATE)
            .withId(serviceAgreementToUpdate.getId())));
    }

    private ApprovalServiceAgreement setupPendingServiceAgreement(String approvalId, String externalSAId,
        String internalSAId) {

        ApprovalServiceAgreement approvalServiceAgreement = new ApprovalServiceAgreement();
        approvalServiceAgreement.setServiceAgreementId(internalSAId);
        approvalServiceAgreement.setCreatorLegalEntityId(rootLegalEntity.getId());
        approvalServiceAgreement.setExternalId(externalSAId);
        approvalServiceAgreement.setApprovalId(approvalId);
        approvalServiceAgreement.setName("saName");
        approvalServiceAgreement.setMaster(false);
        approvalServiceAgreement.setDescription("test");
        approvalServiceAgreement.setState(ServiceAgreementState.ENABLED);
        approvalServiceAgreement.setStartDate(new Date());
        LocalDateTime localDateTIme = LocalDateTime.now();
        Date endDate = Date.from(localDateTIme.plusDays(2).atZone(ZoneId.systemDefault()).toInstant());
        approvalServiceAgreement.setEndDate(endDate);
        Set<Long> assignablePermissionSetsRegular = Sets.newHashSet(assignablePermissionSetJpaRepository
            .findFirstByType(AssignablePermissionType.ADMIN_USER_DEFAULT.getValue()).get().getId());
        Set<Long> assignablePermissionSetsAdmin = Sets.newHashSet(assignablePermissionSetJpaRepository
            .findFirstByType(AssignablePermissionType.REGULAR_USER_DEFAULT.getValue()).get().getId());
        approvalServiceAgreement.setPermissionSetsAdmin(assignablePermissionSetsAdmin);
        approvalServiceAgreement.setPermissionSetsRegular(assignablePermissionSetsRegular);
        return accessControlApprovalJpaRepository.save(approvalServiceAgreement);
    }

}
