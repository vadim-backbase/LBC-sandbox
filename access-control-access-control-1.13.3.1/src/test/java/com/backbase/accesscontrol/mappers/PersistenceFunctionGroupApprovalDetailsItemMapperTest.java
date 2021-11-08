package com.backbase.accesscontrol.mappers;

import static com.backbase.accesscontrol.util.helpers.ApplicableFunctionPrivilegeUtil.getApplicableFunctionPrivilege;
import static com.backbase.accesscontrol.util.helpers.FunctionGroupUtil.getFunctionGroup;
import static com.backbase.accesscontrol.util.helpers.GroupedFunctionPrivilegeUtil.getGroupedFunctionPrivilege;
import static com.backbase.accesscontrol.util.helpers.GroupedFunctionPrivilegeUtil.getGroupedFunctionPrivileges;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.domain.ApplicableFunctionPrivilege;
import com.backbase.accesscontrol.domain.ApprovalFunctionGroup;
import com.backbase.accesscontrol.domain.ApprovalFunctionGroupRef;
import com.backbase.accesscontrol.domain.BusinessFunction;
import com.backbase.accesscontrol.domain.FunctionGroup;
import com.backbase.accesscontrol.domain.Privilege;
import com.backbase.accesscontrol.domain.ServiceAgreement;
import com.backbase.accesscontrol.domain.enums.FunctionGroupType;
import com.backbase.accesscontrol.service.BusinessFunctionCache;
import com.backbase.accesscontrol.util.helpers.ApplicableFunctionPrivilegeUtil;
import com.backbase.accesscontrol.util.helpers.BusinessFunctionUtil;
import com.backbase.accesscontrol.util.helpers.PrivilegeUtil;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.PermissionMatrix;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.PermissionMatrixAction;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.PresentationApprovalAction;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.PresentationFunctionGroupApprovalDetailsItem;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.PresentationFunctionGroupState;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PersistenceFunctionGroupApprovalDetailsItemMapperTest {

    @Spy
    protected FunctionGroupMapperPersistence functionGroupMapperPersistence = Mappers.getMapper(
        FunctionGroupMapperPersistence.class);

    @Mock
    protected BusinessFunctionCache businessFunctionCache;

    @Mock
    protected FunctionGroupMapper functionGroupMapper;

    @InjectMocks
    protected PersistenceFunctionGroupApprovalDetailsItemMapper mapper;

    @Test
    public void getResultForDelete() {
        String approvalId = "approvalId";
        String serviceAgreementName = "SA";
        Privilege privilege = PrivilegeUtil.getPrivilege("1", "view", "view");
        BusinessFunction businessFunction = BusinessFunctionUtil
            .getBusinessFunction("1001", "functionName", "functionCode", "resource", "resourceCode");
        FunctionGroup fg = getFunctionGroup("1", "fg1", "desc.fg1", getGroupedFunctionPrivileges(
            getGroupedFunctionPrivilege("grFunPrId",
                getApplicableFunctionPrivilege("1", businessFunction, privilege, false),
                new FunctionGroup().withId("1").withName("fg1"))
            ),
            FunctionGroupType.DEFAULT, new ServiceAgreement().withId("11").withName(serviceAgreementName));

        ApprovalFunctionGroupRef afg = new ApprovalFunctionGroupRef();
        afg.setApprovalId(approvalId);

        ApplicableFunctionPrivilege aplbfp = ApplicableFunctionPrivilegeUtil
            .getApplicableFunctionPrivilege("1", businessFunction, privilege, false);

        when(businessFunctionCache.getApplicableFunctionPrivilegeById(eq("1")))
            .thenReturn(aplbfp);
        when(businessFunctionCache.getApplicableFunctionPrivilegeByBusinessFunctionId(any()))
            .thenReturn(
                createApplicableFunctionList("view", "create", "edit", "delete", "execute", "approve", "cancel"));

        when(functionGroupMapper.createFromFunctionGroupOldState(eq(fg)))
            .thenReturn(new PresentationFunctionGroupState().withApprovalTypeId(approvalId).withName("fg1")
                .withDescription("desc.fg1"));
        when(functionGroupMapper.toPermissionMatrixDetailsFromBusinessFunction(any(BusinessFunction.class)))
            .thenReturn(new PermissionMatrix().withName("functionName").withFunctionCode("functionCode")
                .withResource("resource"));

        PresentationFunctionGroupApprovalDetailsItem result = mapper.getResult(fg, afg, "SA");
        assertEquals(PresentationApprovalAction.DELETE.toString(), result.getAction().toString());
        assertEquals(approvalId, result.getApprovalId());
        assertEquals(fg.getId(), result.getFunctionGroupId());
        assertNull(result.getNewState());
        assertEquals(fg.getDescription(), result.getOldState().getDescription());
        assertEquals(fg.getName(), result.getOldState().getName());
        assertEquals(businessFunction.getFunctionCode(), result.getPermissionMatrix().get(0).getFunctionCode());
        assertEquals(businessFunction.getFunctionName(), result.getPermissionMatrix().get(0).getName());
        assertEquals(businessFunction.getResourceName(), result.getPermissionMatrix().get(0).getResource());
        assertEquals(PermissionMatrixAction.REMOVED, result.getPermissionMatrix().get(0).getAction());
        assertNotNull(result.getPermissionMatrix().get(0).getPrivileges().getView());
        assertEquals(serviceAgreementName, result.getServiceAgreementName());
    }

    @Test
    public void getResultForUpdate() {
        String approvalId = "approvalId";
        String serviceAgreementName = "SA";

        Privilege privilege1 = PrivilegeUtil.getPrivilege("1", "view", "view");
        Privilege privilege2 = PrivilegeUtil.getPrivilege("2", "edit", "edit");
        Privilege privilege3 = PrivilegeUtil.getPrivilege("3", "execute", "execute");

        ServiceAgreement sa = new ServiceAgreement().withId("11").withName(serviceAgreementName);

        BusinessFunction businessFunction = BusinessFunctionUtil
            .getBusinessFunction("1001", "functionName", "functionCode", "resource", "resourceCode");
        FunctionGroup fg = getFunctionGroup("1", "fg1", "desc.fg1", getGroupedFunctionPrivileges(
            getGroupedFunctionPrivilege("grFunPrId",
                getApplicableFunctionPrivilege("1", businessFunction, privilege1, false),
                new FunctionGroup().withId("1").withName("fg1")),
            getGroupedFunctionPrivilege("grFunPrId",
                getApplicableFunctionPrivilege("2", businessFunction, privilege2, false),
                new FunctionGroup().withId("1").withName("fg1"))
            ),
            FunctionGroupType.DEFAULT, sa);

        ApprovalFunctionGroup afg = getApprovalFunctionGroup(1L, fg.getId(), "fg2", "desc.fg2", sa.getId(),
            "approvaltypeId", new Date(),
            new Date(),
            "1", "3");
        afg.setApprovalId(approvalId);

        ApplicableFunctionPrivilege aplbfp1 = ApplicableFunctionPrivilegeUtil
            .getApplicableFunctionPrivilege("1", businessFunction, privilege1, false);
        ApplicableFunctionPrivilege aplbfp2 = ApplicableFunctionPrivilegeUtil
            .getApplicableFunctionPrivilege("2", businessFunction, privilege2, false);
        ApplicableFunctionPrivilege aplbfp3 = ApplicableFunctionPrivilegeUtil
            .getApplicableFunctionPrivilege("3", businessFunction, privilege3, false);

        when(businessFunctionCache.getApplicableFunctionPrivilegeById(refEq("1")))
            .thenReturn(aplbfp1);
        when(businessFunctionCache.getApplicableFunctionPrivilegeById(refEq("2")))
            .thenReturn(aplbfp2);
        when(businessFunctionCache.getApplicableFunctionPrivilegeById(refEq("3")))
            .thenReturn(aplbfp3);
        when(businessFunctionCache.getApplicableFunctionPrivilegeByBusinessFunctionId(any()))
            .thenReturn(
                createApplicableFunctionList("view", "create", "edit", "delete", "execute", "approve", "cancel"));

        when(functionGroupMapper.createFromApprovalFunctionGroupNewState(eq(afg)))
            .thenReturn(new PresentationFunctionGroupState().withApprovalTypeId(approvalId).withName("fg2")
                .withDescription("desc.fg2"));
        when(functionGroupMapper.createFromFunctionGroupOldState(eq(fg)))
            .thenReturn(new PresentationFunctionGroupState().withApprovalTypeId(approvalId).withName("fg1")
                .withDescription("desc.fg1"));
        when(functionGroupMapper.toPermissionMatrixDetailsFromBusinessFunction(any(BusinessFunction.class)))
            .thenReturn(new PermissionMatrix().withName("functionName").withFunctionCode("functionCode")
                .withResource("resource"));

        mapper.getResult(fg, afg, serviceAgreementName);

        PresentationFunctionGroupApprovalDetailsItem result = mapper.getResult(fg, afg, serviceAgreementName);
        assertEquals(PresentationApprovalAction.EDIT.toString(), result.getAction().toString());
        assertEquals(approvalId, result.getApprovalId());
        assertEquals(fg.getId(), result.getFunctionGroupId());
        assertEquals(fg.getDescription(), result.getOldState().getDescription());
        assertEquals(fg.getName(), result.getOldState().getName());
        assertEquals(afg.getName(), result.getNewState().getName());
        assertEquals(afg.getDescription(), result.getNewState().getDescription());
        assertEquals(businessFunction.getFunctionCode(), result.getPermissionMatrix().get(0).getFunctionCode());
        assertEquals(businessFunction.getFunctionName(), result.getPermissionMatrix().get(0).getName());
        assertEquals(businessFunction.getResourceName(), result.getPermissionMatrix().get(0).getResource());
        assertEquals(PermissionMatrixAction.CHANGED, result.getPermissionMatrix().get(0).getAction());
        assertNotNull(result.getPermissionMatrix().get(0).getPrivileges().getView());
        assertNotNull(result.getPermissionMatrix().get(0).getPrivileges().getExecute());
        assertNotNull(result.getPermissionMatrix().get(0).getPrivileges().getEdit());
        assertNotNull(result.getPermissionMatrix().get(0).getPrivileges().getExecute());
        assertEquals(serviceAgreementName, result.getServiceAgreementName());
    }

    @Test
    public void getResultForCreate() {
        String approvalId = "approvalId";
        String serviceAgreementName = "SA";

        Privilege privilege1 = PrivilegeUtil.getPrivilege("1", "view", "view");
        Privilege privilege2 = PrivilegeUtil.getPrivilege("1", "cancel", "cancel");

        ServiceAgreement sa = new ServiceAgreement().withId("11").withName(serviceAgreementName);

        BusinessFunction businessFunction = BusinessFunctionUtil
            .getBusinessFunction("1001", "functionName", "functionCode", "resource", "resourceCode");

        ApprovalFunctionGroup afg = getApprovalFunctionGroup(1L, null, "fg2", "desc.fg2", sa.getId(), "approvaltypeId",
            new Date(),
            new Date(),
            "1", "3");
        afg.setApprovalId(approvalId);

        ApplicableFunctionPrivilege aplbfp1 = ApplicableFunctionPrivilegeUtil
            .getApplicableFunctionPrivilege("1", businessFunction, privilege1, false);
        ApplicableFunctionPrivilege aplbfp2 = ApplicableFunctionPrivilegeUtil
            .getApplicableFunctionPrivilege("3", businessFunction, privilege2, false);

        when(businessFunctionCache.getApplicableFunctionPrivilegeById(refEq("1")))
            .thenReturn(aplbfp1);
        when(businessFunctionCache.getApplicableFunctionPrivilegeById(refEq("3")))
            .thenReturn(aplbfp2);
        when(businessFunctionCache.getApplicableFunctionPrivilegeByBusinessFunctionId(any()))
            .thenReturn(
                createApplicableFunctionList("view", "create", "edit", "delete", "execute", "approve", "cancel"));

        when(functionGroupMapper.createFromApprovalFunctionGroupNewState(eq(afg)))
            .thenReturn(new PresentationFunctionGroupState().withApprovalTypeId(approvalId).withName("fg2")
                .withDescription("desc.fg2"));

        when(functionGroupMapper.toPermissionMatrixDetailsFromBusinessFunction(any(BusinessFunction.class)))
            .thenReturn(new PermissionMatrix().withName("functionName").withFunctionCode("functionCode")
                .withResource("resource"));

        PresentationFunctionGroupApprovalDetailsItem result = mapper.getResult(null, afg, serviceAgreementName);

        assertEquals(PresentationApprovalAction.CREATE.toString(), result.getAction().toString());
        assertEquals(approvalId, result.getApprovalId());
        assertEquals(afg.getName(), result.getNewState().getName());
        assertEquals(afg.getDescription(), result.getNewState().getDescription());
        assertEquals(businessFunction.getFunctionCode(), result.getPermissionMatrix().get(0).getFunctionCode());
        assertEquals(businessFunction.getFunctionName(), result.getPermissionMatrix().get(0).getName());
        assertEquals(businessFunction.getResourceName(), result.getPermissionMatrix().get(0).getResource());
        assertEquals(PermissionMatrixAction.ADDED, result.getPermissionMatrix().get(0).getAction());
        assertTrue(result.getPermissionMatrix().get(0).getPrivileges().getView().getNew());
        assertTrue(result.getPermissionMatrix().get(0).getPrivileges().getCancel().getNew());
        assertNotNull(result.getPermissionMatrix().get(0).getPrivileges().getExecute());
        assertEquals(serviceAgreementName, result.getServiceAgreementName());
    }

    private List<ApplicableFunctionPrivilege> createApplicableFunctionList(String... privilegeNames) {
        List<ApplicableFunctionPrivilege> result = new ArrayList<>();
        for (String s : privilegeNames) {
            ApplicableFunctionPrivilege afp = new ApplicableFunctionPrivilege();
            afp.setPrivilegeName(s);
            result.add(afp);
        }
        return result;

    }

    private ApprovalFunctionGroup getApprovalFunctionGroup(Long id, String functionGroupId, String name,
        String description,
        String serviceAgreementId,
        String approvalTypeId,
        Date startDate, Date endDate, String... privileges) {
        ApprovalFunctionGroup functionGroup = new ApprovalFunctionGroup();
        functionGroup.setId(id);
        functionGroup.setFunctionGroupId(functionGroupId);
        functionGroup.setName(name);
        functionGroup.setDescription(description);
        functionGroup.setServiceAgreementId(serviceAgreementId);
        functionGroup.setApprovalTypeId(approvalTypeId);
        functionGroup.setStartDate(startDate);
        functionGroup.setEndDate(endDate);
        functionGroup.setPrivileges(new HashSet<>(Arrays.asList(privileges)));
        return functionGroup;
    }
}