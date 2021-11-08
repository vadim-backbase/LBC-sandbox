package com.backbase.accesscontrol.business.service;

import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_105;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.business.persistence.functiongroup.AddFunctionGroupApprovalHandler;
import com.backbase.accesscontrol.business.persistence.functiongroup.CreateFunctionGroupHandler;
import com.backbase.accesscontrol.business.persistence.functiongroup.DeleteFunctionGroupApprovalHandler;
import com.backbase.accesscontrol.business.persistence.functiongroup.DeleteFunctionGroupHandler;
import com.backbase.accesscontrol.business.persistence.functiongroup.UpdateFunctionGroupApprovalHandler;
import com.backbase.accesscontrol.business.persistence.functiongroup.UpdateFunctionGroupHandler;
import com.backbase.accesscontrol.domain.dto.FunctionGroupApprovalBase;
import com.backbase.accesscontrol.domain.dto.FunctionGroupBase;
import com.backbase.accesscontrol.dto.ApprovalDto;
import com.backbase.accesscontrol.dto.parameterholder.EmptyParameterHolder;
import com.backbase.accesscontrol.dto.parameterholder.FunctionGroupIdApprovalIdParameterHolder;
import com.backbase.accesscontrol.dto.parameterholder.SingleParameterHolder;
import com.backbase.accesscontrol.mappers.FunctionGroupMapper;
import com.backbase.accesscontrol.matchers.BadRequestErrorMatcher;
import com.backbase.accesscontrol.service.DateTimeService;
import com.backbase.accesscontrol.service.FunctionGroupService;
import com.backbase.accesscontrol.service.ObjectConverter;
import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.functiongroups.FunctionGroupBase.Type;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.functiongroups.FunctionGroupByIdGetResponseBody;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.functiongroups.FunctionGroupsGetResponseBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.function.Permission;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.function.Privilege;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.functiongroups.FunctionGroupByIdPutRequestBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.functiongroups.FunctionGroupsPostResponseBody;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class FunctionGroupPAndPServiceTest {

    @Mock
    private FunctionGroupService functionGroupService;

    @Mock
    private DeleteFunctionGroupApprovalHandler deleteFunctionGroupApprovalHandler;

    @Mock
    private DeleteFunctionGroupHandler deleteFunctionGroupHandler;

    @Mock
    private AddFunctionGroupApprovalHandler addFunctionGroupApprovalHandler;

    @Spy
    private DateTimeService dateTimeService = new DateTimeService("UTC");

    @Mock
    private CreateFunctionGroupHandler createFunctionGroupHandler;

    @Mock
    private UpdateFunctionGroupHandler updateFunctionGroupHandler;

    @Mock
    private UpdateFunctionGroupApprovalHandler updateFunctionGroupApprovalHandler;

    @Spy
    private ObjectConverter objectConverter = new ObjectConverter(spy(ObjectMapper.class));

    @Spy
    private FunctionGroupMapper functionGroupMapper = Mappers.getMapper(FunctionGroupMapper.class);

    @InjectMocks
    private FunctionGroupPAndPService functionGroupPAndPService;


    @Test
    public void testCreateFunctionGroup() {
        String serviceAgreementId = "SA-ID";
        String functionAccessGroupName = "fag name";
        String createdFagId = "FAG ID";

        ArgumentCaptor<FunctionGroupBase> requestCaptor = ArgumentCaptor.forClass(FunctionGroupBase.class);
        com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.functiongroups.FunctionGroupBase postData =
            new com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.functiongroups.FunctionGroupBase()
                .withServiceAgreementId(serviceAgreementId)
                .withName(functionAccessGroupName);

        when(functionGroupMapper
            .functionGroupBasePresentationToFunctionGroupBaseDto(any(
                com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.functiongroups.FunctionGroupBase.class)))
            .thenReturn(new FunctionGroupBase().withServiceAgreementId(serviceAgreementId)
                .withName(functionAccessGroupName));
        when(createFunctionGroupHandler.handleRequest(any(EmptyParameterHolder.class), any(FunctionGroupBase.class)))
            .thenReturn(new FunctionGroupsPostResponseBody().withId(createdFagId));

        FunctionGroupsPostResponseBody postResponse =
            functionGroupPAndPService.createFunctionGroup(postData);

        verify(createFunctionGroupHandler, times(1))
            .handleRequest(any(EmptyParameterHolder.class), requestCaptor.capture());
        FunctionGroupBase captorData = requestCaptor.getValue();

        assertEquals(serviceAgreementId, captorData.getServiceAgreementId());
        assertEquals(functionAccessGroupName, captorData.getName());
        assertEquals(createdFagId, postResponse.getId());
    }

    @Test
    public void testGetFunctionGroupById() {
        String fgId = "FG ID";

        FunctionGroupByIdGetResponseBody putData = new FunctionGroupByIdGetResponseBody()
            .withId(fgId);

        when(functionGroupService.getFunctionGroupById(fgId)).thenReturn(putData);

        FunctionGroupByIdGetResponseBody response = functionGroupPAndPService
            .getFunctionGroupById(fgId);

        verify(functionGroupService).getFunctionGroupById(eq(fgId));
        assertEquals(fgId, response.getId());
    }

    @Test
    public void testGetFunctionGroupWhenExists() {
        String serviceAgreementId = "sa-id";
        String functionGroupId = "FG ID";

        FunctionGroupsGetResponseBody functionGroup = new FunctionGroupsGetResponseBody()
            .withId(functionGroupId)
            .withServiceAgreementId(serviceAgreementId);

        when(functionGroupService.getFunctionGroupsByServiceAgreementId(eq(serviceAgreementId)))
            .thenReturn(singletonList(functionGroup));

        List<FunctionGroupsGetResponseBody> response = functionGroupPAndPService
            .getFunctionGroups(serviceAgreementId);
        assertEquals(functionGroupId, response.get(0).getId());
    }

    @Test
    public void testUpdateFunctionGroup() {
        String functionGroupId = "FG-01";

        FunctionGroupByIdPutRequestBody functionGroupByIdPutRequestBody = new FunctionGroupByIdPutRequestBody()
            .withServiceAgreementId("SA-ID")
            .withName("fg name")
            .withDescription("description")
            .withValidFromDate("2019-01-01")
            .withValidFromTime("01:05:00")
            .withValidUntilDate("2019-02-01")
            .withValidUntilTime("01:05:00")
            .withPermissions(singletonList(new Permission().withFunctionId("bfId")
                .withAssignedPrivileges(singletonList(new Privilege().withPrivilege("edit")))));

        doNothing().when(updateFunctionGroupHandler)
            .handleRequest(any(SingleParameterHolder.class), any(FunctionGroupByIdPutRequestBody.class));

        functionGroupPAndPService.updateFunctionGroup(functionGroupByIdPutRequestBody, functionGroupId);

        ArgumentCaptor<SingleParameterHolder> captor = ArgumentCaptor.forClass(SingleParameterHolder.class);

        verify(updateFunctionGroupHandler).handleRequest(captor.capture(), eq(functionGroupByIdPutRequestBody));
        assertEquals(functionGroupId, captor.getValue().getParameter());
    }

    @Test
    public void testCreateFunctionGroupWithApproval() {
        String serviceAgreementId = "service_agreement";
        String functionAccessGroupName = "fname";
        String createdFagId = "FAG ID";
        String approvalId = "approvalId";

        com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.functiongroups.FunctionGroupBase postData =
            new com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.functiongroups.FunctionGroupBase()
                .withServiceAgreementId(serviceAgreementId)
                .withName(functionAccessGroupName).withValidFromDate("2019-01-01")
                .withValidFromTime("01:05:00").withValidUntilDate("2019-02-01").withValidUntilTime("01:05:00");

        FunctionGroupApprovalBase fgab = new FunctionGroupApprovalBase().withApprovalId(approvalId)
            .withServiceAgreementId(serviceAgreementId)
            .withName(functionAccessGroupName);
        when(functionGroupMapper.toFunctionGroupCreate(any(
            com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.functiongroups.FunctionGroupBase.class),
            any())).thenReturn(
            fgab);
        when(addFunctionGroupApprovalHandler.handleRequest(any(EmptyParameterHolder.class), eq(fgab)))
            .thenReturn(new FunctionGroupsPostResponseBody().withId(createdFagId));
        FunctionGroupsPostResponseBody postResponse = functionGroupPAndPService
            .createFunctionGroupWithApproval(postData, approvalId);

        verify(addFunctionGroupApprovalHandler, times(1))
            .handleRequest(any(), eq(fgab));

        assertEquals(createdFagId, postResponse.getId());
    }

    @Test
    public void testUpdateFunctionGroupWithApproval() {
        String approvalId = "approvalId";
        String functionGroupId = "fgId";

        FunctionGroupByIdPutRequestBody request = new FunctionGroupByIdPutRequestBody()
            .withApprovalTypeId("approvalTypeId")
            .withDescription("desc")
            .withName("name")
            .withServiceAgreementId("saId")
            .withValidFromDate("2019-01-01")
            .withValidFromTime("01:05:00")
            .withValidUntilDate("2019-02-01")
            .withValidUntilTime("01:05:00")
            .withPermissions(singletonList(new Permission().withFunctionId("bfId")
                .withAssignedPrivileges(singletonList(new Privilege().withPrivilege("edit")))));

        doNothing().when(updateFunctionGroupApprovalHandler)
            .handleRequest(any(FunctionGroupIdApprovalIdParameterHolder.class),
                any(FunctionGroupByIdPutRequestBody.class));

        functionGroupPAndPService.updateFunctionGroupWithApproval(request, functionGroupId, approvalId);

        ArgumentCaptor<FunctionGroupIdApprovalIdParameterHolder> captor = ArgumentCaptor
            .forClass(FunctionGroupIdApprovalIdParameterHolder.class);
        verify(updateFunctionGroupApprovalHandler).handleRequest(captor.capture(), eq(request));

        assertEquals(functionGroupId, captor.getValue().getFunctionGroupId());
        assertEquals(approvalId, captor.getValue().getApprovalId());
    }

    @Test
    public void testDeleteFunctionGroup() {
        String fgId = "fgId";
        FunctionGroupByIdGetResponseBody functionGroup = new FunctionGroupByIdGetResponseBody().withId(fgId)
            .withType(Type.DEFAULT);
        when(functionGroupService.getFunctionGroupById(fgId)).thenReturn(functionGroup);
        functionGroupPAndPService.deleteFunctionGroup(fgId);
        verify(deleteFunctionGroupHandler, times(1))
            .handleRequest(any(SingleParameterHolder.class), eq(null));
    }

    @Test
    public void testNotDeleteFunctionGroupWithTypeTemplate() {
        String fgId = "fgId";
        FunctionGroupByIdGetResponseBody functionGroup = new FunctionGroupByIdGetResponseBody().withId(fgId)
            .withType(Type.TEMPLATE);
        when(functionGroupService.getFunctionGroupById(fgId)).thenReturn(functionGroup);

        BadRequestException badRequestException = assertThrows(BadRequestException.class,
            () -> functionGroupPAndPService.deleteFunctionGroup(fgId));

        assertThat(badRequestException,
            is(new BadRequestErrorMatcher(ERR_ACC_105.getErrorMessage(), ERR_ACC_105.getErrorCode())));
    }

    @Test
    public void testDeleteFunctionGroupApproval() {
        String fgId = "fgId";
        String approvalId = "approvalId";
        functionGroupPAndPService.deleteFunctionGroup(fgId, approvalId);
        verify(deleteFunctionGroupApprovalHandler, times(1))
            .handleRequest(any(SingleParameterHolder.class), eq(new ApprovalDto(approvalId, null)));
    }

}
