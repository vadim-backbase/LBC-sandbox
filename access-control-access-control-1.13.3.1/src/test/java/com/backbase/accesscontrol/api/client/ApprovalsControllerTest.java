package com.backbase.accesscontrol.api.client;

import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_093;
import static com.backbase.accesscontrol.util.helpers.ApplicationPropertiesUtils.mockApprovalValidation;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.backbase.accesscontrol.auth.AccessResourceType;
import com.backbase.accesscontrol.dto.ApprovalsListDto;
import com.backbase.accesscontrol.dto.GetDataGroupApprovalDetailsParametersFlow;
import com.backbase.accesscontrol.dto.GetFunctionGroupApprovalDetailsParametersFlow;
import com.backbase.accesscontrol.dto.UserContextDetailsDto;
import com.backbase.accesscontrol.dto.parameterholder.ApprovalsParametersHolder;
import com.backbase.accesscontrol.mappers.model.PayloadConverter;
import com.backbase.accesscontrol.mappers.model.accessgroup.client.PresentationApprovalItemToPresentationApprovalItemMapper;
import com.backbase.accesscontrol.mappers.model.accessgroup.client.PresentationApprovalStatusConverter;
import com.backbase.accesscontrol.mappers.model.accessgroup.client.PresentationDataGroupApprovalDetailsItemConverter;
import com.backbase.accesscontrol.mappers.model.accessgroup.client.PresentationFunctionGroupApprovalDetailsItemConverter;
import com.backbase.accesscontrol.mappers.model.accessgroup.client.PresentationPermissionsApprovalDetailsItemConverter;
import com.backbase.accesscontrol.mappers.model.accessgroup.client.ServiceAgreeementApprovalDetailsItemConverter;
import com.backbase.accesscontrol.service.PermissionValidationService;
import com.backbase.accesscontrol.service.facades.ApprovalFlowService;
import com.backbase.accesscontrol.service.facades.ApprovalsService;
import com.backbase.accesscontrol.util.ApplicationProperties;
import com.backbase.accesscontrol.util.UserContextUtil;
import com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes;
import com.backbase.buildingblocks.presentation.errors.ForbiddenException;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.ApprovalStatus;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.PresentationApprovalStatus;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.Approve;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.Cancel;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.Create;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.Delete;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.Edit;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.Execute;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.OldNewPrivileges;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.PermissionMatrix;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.PermissionMatrixAction;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.PresentationApprovalAction;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.PresentationApprovalCategory;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.PresentationApprovalItem;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.PresentationApprovalLogItem;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.PresentationDataGroupApprovalDetailsItem;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.PresentationFunctionGroupApprovalDetailsItem;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.PresentationFunctionGroupState;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.PresentationPermissionsApprovalDetailsItem;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.ServiceAgreementApprovalDetailsItem;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.View;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@RunWith(MockitoJUnitRunner.class)
@WebAppConfiguration
public class ApprovalsControllerTest {

    private static final String approvalUrl = "/client-api/v2/accessgroups/approvals/";
    @Spy
    public ObjectMapper objectMapper;
    private MockMvc mockMvc;
    @Mock
    private PermissionValidationService permissionValidationService;
    @Mock
    private UserContextUtil userContextUtil;
    @Mock
    private ApprovalsService approvalsService;
    @Mock
    private ApprovalFlowService approvalFlowService;
    @InjectMocks
    private ApprovalsController approvalsController;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ApplicationProperties applicationProperties;

    @Spy
    private PayloadConverter payloadConverter =
        new PayloadConverter(asList(
            spy(Mappers.getMapper(PresentationApprovalItemToPresentationApprovalItemMapper.class)),
            spy(Mappers.getMapper(PresentationApprovalStatusConverter.class)),
            spy(Mappers.getMapper(PresentationFunctionGroupApprovalDetailsItemConverter.class)),
            spy(Mappers.getMapper(PresentationDataGroupApprovalDetailsItemConverter.class)),
            spy(Mappers.getMapper(ServiceAgreeementApprovalDetailsItemConverter.class)),
            spy(Mappers.getMapper(PresentationPermissionsApprovalDetailsItemConverter.class))
        ));

    @Before
    public void setup() {
        this.mockMvc = MockMvcBuilders
            .standaloneSetup(approvalsController)
            .setHandlerExceptionResolvers()
            .build();
    }

    @Test
    public void shouldCallListPendingApprovalsWhenUserContextIsProvided() throws Exception {
        mockApprovalValidation(applicationProperties, true);
        String legalEntityId = "LE-01";
        String userId = "U-01";
        String serviceAgreementId = "SA-01";
        String cursor = "3";

        mockGetServiceAgreementId(serviceAgreementId);
        mockGetUserContextDetails(userId, legalEntityId);

        ApprovalsListDto mockResponseData = new ApprovalsListDto()
            .withPresentationApprovalItems(Lists.newArrayList(
                new PresentationApprovalItem()
                    .withApprovalId("AP-01"),
                new PresentationApprovalItem()
                    .withApprovalId("AP-02")))
            .withCursor(cursor);

        doNothing().when(permissionValidationService).validateAccessToServiceAgreementResource(serviceAgreementId,
            AccessResourceType.USER_AND_ACCOUNT);

        when(approvalsService.listPendingApprovals(any(ApprovalsParametersHolder.class))).thenReturn(mockResponseData);

        MockHttpServletResponse response = mockMvc.perform(get("/client-api/v2/accessgroups/approvals")
            .param("from", "0")
            .param("size", "10")
            .param("cursor", cursor))
            .andExpect(status().isOk()).andReturn().getResponse();
        String contentAsString = response.getContentAsString();

        List<LinkedHashMap<String, Object>> returnedListOfData = objectMapper.readValue(contentAsString, List.class);

        List<PresentationApprovalItem> actualResponse = returnedListOfData
            .stream()
            .map(serviceAgreement -> objectMapper.convertValue(serviceAgreement,
                PresentationApprovalItem.class))
            .collect(Collectors.toList());

        verify(approvalsService).listPendingApprovals(eq(new ApprovalsParametersHolder()
            .withServiceAgreementId(serviceAgreementId)
            .withLegalEntityId(legalEntityId)
            .withUserId(userId)
            .withFrom(0)
            .withSize(10)
            .withCursor(cursor))
        );

        assertEquals(mockResponseData.getPresentationApprovalItems().size(), actualResponse.size());
        assertEquals(cursor, response.getHeader("X-Cursor"));
    }

    @Test
    public void shouldCallGetApprovalDetailsByIdWhenApprovalIsOn() throws Exception {
        mockApprovalValidation(applicationProperties, true);

        String approvalId = "app-Id";
        String serviceAgreementId = "sa-Id";
        String userId = "userId";

        mockGetServiceAgreementId(serviceAgreementId);
        mockGetUserContextDetails(userId, "le");

        PresentationPermissionsApprovalDetailsItem mockResponseData = new PresentationPermissionsApprovalDetailsItem()
            .withAction(PresentationApprovalAction.EDIT)
            .withCategory(PresentationApprovalCategory.ASSIGN_PERMISSIONS)
            .withCreatedAt(new Date())
            .withCreatorUserFullName("fullName")
            .withCreatorUserId(userId)
            .withServiceAgreementId(serviceAgreementId)
            .withServiceAgreementName("sa-name")
            .withServiceAgreementDescription("sa-desc")
            .withUserId("uId")
            .withUserFullName("name")
            .withApprovalLog(Collections.singletonList(new PresentationApprovalLogItem()
                .withApprovedAt(new Date())
                .withApproverFullName("appName")
                .withApproverId("approvedId")));

        when(approvalsService.getPermissionsApprovalDetailsById(eq(approvalId), eq(serviceAgreementId), eq(userId)))
            .thenReturn(mockResponseData);

        String contentAsString = mockMvc.perform(get(approvalUrl + approvalId + "/permissions"))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

        com.backbase.accesscontrol.client.rest.spec.model.PresentationPermissionsApprovalDetailsItem returnedApproval =
            objectMapper
                .readValue(contentAsString,
                    com.backbase.accesscontrol.client.rest.spec.model.PresentationPermissionsApprovalDetailsItem.class);

        assertEquals(mockResponseData.getUserId(), returnedApproval.getUserId());
        assertEquals(mockResponseData.getServiceAgreementId(), returnedApproval.getServiceAgreementId());
        assertEquals(mockResponseData.getApprovalId(), returnedApproval.getApprovalId());
        assertEquals(mockResponseData.getCreatedAt(), returnedApproval.getCreatedAt());
    }

    @Test
    public void shouldCallGetApprovalDetailsForDataGroupByIdWhenApprovalIsOn() throws Exception {
        mockApprovalValidation(applicationProperties, true);

        String approvalId = "app-Id";
        String serviceAgreementId = "sa-Id";
        String userId = "userId";

        mockGetServiceAgreementId(serviceAgreementId);
        mockGetUserContextDetails(userId, "le");

        PresentationDataGroupApprovalDetailsItem mockResponseData = new PresentationDataGroupApprovalDetailsItem()
            .withAction(PresentationApprovalAction.CREATE)
            .withServiceAgreementId(serviceAgreementId)
            .withServiceAgreementName("sa-name")
            .withDataGroupId("dataGroupId")
            .withApprovalId("approvalId")
            .withAddedDataItems(Sets.newHashSet("1", "2"));

        when(approvalFlowService
            .getDataGroupApprovalDetailsById(any(GetDataGroupApprovalDetailsParametersFlow.class)))
            .thenReturn(mockResponseData);

        String contentAsString = mockMvc.perform(get(approvalUrl + approvalId + "/data-group"))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

        PresentationDataGroupApprovalDetailsItem returnedApproval = objectMapper
            .readValue(contentAsString, PresentationDataGroupApprovalDetailsItem.class);

        assertEquals(mockResponseData, returnedApproval);
    }

    @Test
    public void shouldCallGetApprovalDetailsForFunctionGroupByIdWhenApprovalIsOn() throws Exception {

        String approvalId = "app-Id";
        String serviceAgreementId = "sa-Id";
        String userId = "userId";

        mockApprovalValidation(applicationProperties, true);
        mockGetServiceAgreementId(serviceAgreementId);
        mockGetUserContextDetails(userId, "le");

        PresentationFunctionGroupApprovalDetailsItem mockResponseData =
            getPresentationFunctionGroupApprovalDetailsItemTest();

        when(approvalFlowService
            .getFunctionGroupApprovalDetailsById(any(GetFunctionGroupApprovalDetailsParametersFlow.class)))
            .thenReturn(mockResponseData);

        String contentAsString = mockMvc.perform(get(approvalUrl + approvalId + "/function-group"))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

        PresentationFunctionGroupApprovalDetailsItem returnedApproval = objectMapper
            .readValue(contentAsString, PresentationFunctionGroupApprovalDetailsItem.class);

        assertEquals(mockResponseData, returnedApproval);
    }

    @Test
    public void shoudCallGetServiceAgreementApprovalDetailsItem() throws Exception {
        String approvalId = "app-Id";
        String serviceAgreementId = "sa-Id";
        String userId = "userId";

        mockApprovalValidation(applicationProperties, true);
        mockGetServiceAgreementId(serviceAgreementId);
        mockGetUserContextDetails(userId, "le");

        ServiceAgreementApprovalDetailsItem response = new ServiceAgreementApprovalDetailsItem();
        response.setApprovalId(approvalId);
        response.setServiceAgreementId(serviceAgreementId);

        when(approvalFlowService
            .getServiceAgreementDetailsById(eq(approvalId), eq(serviceAgreementId), eq(userId)))
            .thenReturn(response);

        String contentAsString = mockMvc.perform(get(approvalUrl + approvalId + "/service-agreement"))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

        ServiceAgreementApprovalDetailsItem returnedApproval = objectMapper
            .readValue(contentAsString, ServiceAgreementApprovalDetailsItem.class);

        assertEquals(response, returnedApproval);

    }

    @Test
    public void testRejectApprovalRequest() throws Exception {
        mockApprovalValidation(applicationProperties, true);

        String approvalId = "app-Id";
        String serviceAgreementId = "sa-Id";
        String userId = "userId";

        mockGetServiceAgreementId(serviceAgreementId);
        mockGetUserContextDetails(userId, "le");
        PresentationApprovalStatus presentationApprovalStatus = new PresentationApprovalStatus()
            .withApprovalStatus(ApprovalStatus.REJECTED);
        when(approvalsService.rejectApprovalRequest(eq(approvalId), eq(serviceAgreementId), eq(userId)))
            .thenReturn(presentationApprovalStatus);
        String contentAsString = mockMvc.perform(post(approvalUrl + approvalId + "/reject"))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

        PresentationApprovalStatus returnedApproval = objectMapper
            .readValue(contentAsString, PresentationApprovalStatus.class);
        verify(approvalsService, times(1)).rejectApprovalRequest(eq(approvalId), eq(serviceAgreementId), eq(userId));

        assertEquals(presentationApprovalStatus.getApprovalStatus(), returnedApproval.getApprovalStatus());
    }

    @Test
    public void shouldThrowForbiddenOnGetApprovalByIdIfApprovalIsOff() throws Exception {
        mockApprovalValidation(applicationProperties, false);

        mockMvc = MockMvcBuilders
            .standaloneSetup(approvalsController)
            .setHandlerExceptionResolvers()
            .build();

        String approvalId = "app-Id";

        mockMvc.perform(get(approvalUrl + approvalId + "/permissions"))
            .andExpect(status().isForbidden())
            .andDo(mvcResult -> {
                ForbiddenException exception = (ForbiddenException) mvcResult.getResolvedException();
                assertEquals(ERR_AG_093.getErrorCode(), Objects.requireNonNull(exception).getErrors().get(0).getKey());
                assertEquals(ERR_AG_093.getErrorMessage(), exception.getErrors().get(0).getMessage());
            });
    }

    @Test
    public void shouldThrowForbiddenOnGetApprovalDataGroupByIdIfApprovalIsOff() throws Exception {
        mockApprovalValidation(applicationProperties, false);

        mockMvc = MockMvcBuilders
            .standaloneSetup(approvalsController)
            .setHandlerExceptionResolvers()
            .build();

        String approvalId = "app-Id";

        mockMvc.perform(get(approvalUrl + approvalId + "/data-group"))
            .andExpect(status().isForbidden())
            .andDo(mvcResult -> {
                ForbiddenException exception = (ForbiddenException) mvcResult.getResolvedException();
                assertEquals(ERR_AG_093.getErrorCode(), Objects.requireNonNull(exception).getErrors().get(0).getKey());
                assertEquals(ERR_AG_093.getErrorMessage(), exception.getErrors().get(0).getMessage());
            });
    }

    @Test
    public void shouldThrowForbiddenExceptionWhenRejectingApprovalRequestApprovalOff() throws Exception {
        mockApprovalValidation(applicationProperties, false);
        String approvalId = "appId";
        mockMvc = MockMvcBuilders
            .standaloneSetup(approvalsController)
            .setHandlerExceptionResolvers()
            .build();

        mockMvc.perform(post(approvalUrl + approvalId + "/reject"))
            .andExpect(status().isForbidden())
            .andDo(mvcResult -> {
                ForbiddenException exception = (ForbiddenException) mvcResult.getResolvedException();
                assertEquals(AccessGroupErrorCodes.ERR_AG_093.getErrorCode(), exception.getErrors().get(0).getKey());
                assertEquals(AccessGroupErrorCodes.ERR_AG_093.getErrorMessage(),
                    exception.getErrors().get(0).getMessage());
            });
    }

    @Test
    public void testApproveApprovalRequest() throws Exception {
        mockApprovalValidation(applicationProperties, true);

        String approvalId = "app-Id";
        String serviceAgreementId = "sa-Id";
        String userId = "userId";

        mockGetServiceAgreementId(serviceAgreementId);
        mockGetUserContextDetails(userId, "le");

        PresentationApprovalStatus presentationApprovalStatus = new PresentationApprovalStatus()
            .withApprovalStatus(ApprovalStatus.APPROVED);

        when(approvalsService.acceptApprovalRequest(eq(approvalId), eq(serviceAgreementId), eq(userId)))
            .thenReturn(presentationApprovalStatus);

        String contentAsString = mockMvc.perform(post(approvalUrl + approvalId + "/approve"))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

        PresentationApprovalStatus returnedApproval = objectMapper
            .readValue(contentAsString, PresentationApprovalStatus.class);
        verify(approvalsService, times(1)).acceptApprovalRequest(eq(approvalId), eq(serviceAgreementId), eq(userId));

        assertEquals(presentationApprovalStatus.getApprovalStatus(), returnedApproval.getApprovalStatus());
    }

    @Test
    public void shouldThrowForbiddenExceptionWhenApprovingApprovalRequestApprovalOff() throws Exception {
        mockApprovalValidation(applicationProperties, false);
        String approvalId = "appId";
        mockMvc = MockMvcBuilders
            .standaloneSetup(approvalsController)
            .setHandlerExceptionResolvers()
            .build();

        mockMvc.perform(post(approvalUrl + approvalId + "/approve"))
            .andExpect(status().isForbidden())
            .andDo(mvcResult -> {
                ForbiddenException exception = (ForbiddenException) mvcResult.getResolvedException();
                assertEquals(AccessGroupErrorCodes.ERR_AG_093.getErrorCode(), exception.getErrors().get(0).getKey());
                assertEquals(AccessGroupErrorCodes.ERR_AG_093.getErrorMessage(),
                    exception.getErrors().get(0).getMessage());
            });
    }


    private void mockGetUserContextDetails(String userId, String legalEntityId) {
        when(userContextUtil.getUserContextDetails())
            .thenReturn(new UserContextDetailsDto(userId, legalEntityId));
    }

    private void mockGetServiceAgreementId(String serviceAgreementId) {
        when(userContextUtil.getServiceAgreementId())
            .thenReturn(serviceAgreementId);
    }

    private PresentationFunctionGroupApprovalDetailsItem getPresentationFunctionGroupApprovalDetailsItemTest() {

        PresentationFunctionGroupApprovalDetailsItem presentationFunctionGroupApprovalDetailsItem =
            new PresentationFunctionGroupApprovalDetailsItem();
        OldNewPrivileges oldNewPrivileges1 = new OldNewPrivileges();
        OldNewPrivileges oldNewPrivileges2 = new OldNewPrivileges();
        oldNewPrivileges1.setApprove(new Approve().withOld(true).withNew(true));
        oldNewPrivileges1.setView(new View().withOld(true).withNew(true));
        oldNewPrivileges1.setEdit(new Edit().withOld(true).withNew(true));
        oldNewPrivileges1.setCreate(new Create().withOld(false).withNew(true));
        oldNewPrivileges1.setExecute(new Execute().withOld(false).withNew(true));
        oldNewPrivileges1.setCancel(new Cancel().withOld(false).withNew(true));
        oldNewPrivileges1.setDelete(new Delete().withOld(false).withNew(false));

        oldNewPrivileges2.setApprove(new Approve().withNew(false));
        oldNewPrivileges2.setView(new View().withNew(false));
        oldNewPrivileges2.setEdit(new Edit().withNew(false));
        oldNewPrivileges2.setCreate(new Create().withNew(false));
        oldNewPrivileges2.setExecute(new Execute().withNew(true));
        oldNewPrivileges2.setCancel(new Cancel().withNew(true));
        oldNewPrivileges2.setDelete(new Delete().withNew(false));
        PermissionMatrix permissionMatrix1 = new PermissionMatrix();
        permissionMatrix1.setFunctionId("funcID1");
        permissionMatrix1.setFunctionCode("funcCode1");
        permissionMatrix1.setResource("resource1");
        permissionMatrix1.setAction(PermissionMatrixAction.CHANGED);
        permissionMatrix1.setName("name1");
        permissionMatrix1.setPrivileges(oldNewPrivileges1);
        PermissionMatrix permissionMatrix2 = new PermissionMatrix();
        permissionMatrix2.setFunctionId("funcID2");
        permissionMatrix2.setFunctionCode("funcCode2");
        permissionMatrix2.setResource("resource2");
        permissionMatrix2.setAction(PermissionMatrixAction.ADDED);
        permissionMatrix2.setName("name2");
        permissionMatrix2.setPrivileges(oldNewPrivileges2);
        List<PermissionMatrix> permissionMatrixList = new ArrayList<>();
        permissionMatrixList.add(permissionMatrix1);
        permissionMatrixList.add(permissionMatrix2);
        PresentationFunctionGroupState oldFunctionGroupState = new PresentationFunctionGroupState();
        PresentationFunctionGroupState newFunctionGroupState = new PresentationFunctionGroupState();
        oldFunctionGroupState.setName("FG1");
        oldFunctionGroupState.setDescription("FG1 description");
        oldFunctionGroupState.setApprovalTypeId("approvalTypeIdA");
        oldFunctionGroupState.setValidFromDate("2020-01-01");
        oldFunctionGroupState.setValidFromTime("00:00:00");
        oldFunctionGroupState.setValidUntilDate("2025-01-01");
        oldFunctionGroupState.setValidUntilTime("23:59:59");
        newFunctionGroupState.setName("FG2");
        newFunctionGroupState.setDescription("FG2 description");
        newFunctionGroupState.setApprovalTypeId("approvalTypeIdB");
        newFunctionGroupState.setValidFromDate("2020-06-10");
        newFunctionGroupState.setValidFromTime("04:32:24");
        newFunctionGroupState.setValidUntilDate("2023-01-01");
        newFunctionGroupState.setValidUntilTime("23:53:51");

        presentationFunctionGroupApprovalDetailsItem.setFunctionGroupId("0955e686d31e4216b3dd5d66161d536d");
        presentationFunctionGroupApprovalDetailsItem.setApprovalId("606d4532-f8d9-4a5f-36kl-887baf88fa24");
        presentationFunctionGroupApprovalDetailsItem.setServiceAgreementId("0889e686d31e4216b3dd5d66163d2b14");
        presentationFunctionGroupApprovalDetailsItem.setServiceAgreementName("saName");
        presentationFunctionGroupApprovalDetailsItem.setAction(PresentationApprovalAction.EDIT);
        presentationFunctionGroupApprovalDetailsItem.setOldState(oldFunctionGroupState);
        presentationFunctionGroupApprovalDetailsItem.setNewState(newFunctionGroupState);
        presentationFunctionGroupApprovalDetailsItem.setPermissionMatrix(permissionMatrixList);
        return presentationFunctionGroupApprovalDetailsItem;
    }

}