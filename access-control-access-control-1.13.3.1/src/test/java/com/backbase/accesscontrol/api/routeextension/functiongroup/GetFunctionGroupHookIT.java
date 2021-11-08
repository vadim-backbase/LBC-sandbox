package com.backbase.accesscontrol.api.routeextension.functiongroup;

import static com.backbase.accesscontrol.util.helpers.RequestUtils.getVoidInternalRequest;
import static junit.framework.TestCase.assertEquals;

import com.backbase.accesscontrol.api.TestDbWireMock;
import com.backbase.accesscontrol.business.functiongroup.GetFunctionGroupById;
import com.backbase.accesscontrol.domain.ApplicableFunctionPrivilege;
import com.backbase.accesscontrol.domain.FunctionGroup;
import com.backbase.accesscontrol.domain.GroupedFunctionPrivilege;
import com.backbase.accesscontrol.domain.LegalEntity;
import com.backbase.accesscontrol.domain.ServiceAgreement;
import com.backbase.accesscontrol.domain.enums.FunctionGroupType;
import com.backbase.accesscontrol.routes.functiongroup.GetFunctionGroupByIdRouteProxy;
import com.backbase.accesscontrol.util.constants.EndpointConstants;
import com.backbase.accesscontrol.util.helpers.FunctionGroupUtil;
import com.backbase.accesscontrol.util.helpers.GroupedFunctionPrivilegeUtil;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.functiongroups.FunctionGroupByIdGetResponseBody;
import java.util.LinkedHashSet;
import junit.framework.TestCase;
import org.apache.camel.Produce;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.context.ActiveProfiles;

/**
 * Test for {@link GetFunctionGroupById#getFunctionGroupById}
 */
@ActiveProfiles({"live", "routes", "h2"})
public class GetFunctionGroupHookIT extends TestDbWireMock {

    @Produce(value = EndpointConstants.DIRECT_BUSINESS_GET_FUNCTION_GROUP_BY_ID)
    private GetFunctionGroupByIdRouteProxy getFunctionGroupRouteProxy;

    private FunctionGroup functionGroup;
    private ApplicableFunctionPrivilege apfBf1002Create;

    @Before
    public void setup() {

        LegalEntity legalEntity = rootLegalEntity;

        apfBf1002Create = businessFunctionCache
            .getByFunctionIdAndPrivilege("1002", "create");

        ServiceAgreement serviceAgreement = new ServiceAgreement();
        serviceAgreement.setId("SA-01");
        serviceAgreement.setDescription("Service Agreement");
        serviceAgreement.setName("SA Name");
        serviceAgreement.setCreatorLegalEntity(legalEntity);
        serviceAgreement = serviceAgreementJpaRepository.save(serviceAgreement);

        LinkedHashSet<GroupedFunctionPrivilege> groupedFunctionPrivilegeList1 = new LinkedHashSet<>();
        groupedFunctionPrivilegeList1
            .add(GroupedFunctionPrivilegeUtil.getGroupedFunctionPrivilege(null, apfBf1002Create, null));

        functionGroup = FunctionGroupUtil
            .getFunctionGroup(null, "fg1", "desc.fg1", groupedFunctionPrivilegeList1,
                FunctionGroupType.DEFAULT, serviceAgreement);

        functionGroup = functionGroupJpaRepository.saveAndFlush(functionGroup);
    }

    @Test
    public void testGetFunctionGroupByIdHook() {

        FunctionGroupByIdGetResponseBody response =
            getFunctionGroupRouteProxy.getFunctionGroupById(getVoidInternalRequest(), functionGroup.getId()).getData();
        assertEquals(functionGroup.getName(), response.getName());
        TestCase.assertEquals(GetFunctionGroupHookService.FG_HOOK_DESCRIPTION, response.getDescription());
    }
}
