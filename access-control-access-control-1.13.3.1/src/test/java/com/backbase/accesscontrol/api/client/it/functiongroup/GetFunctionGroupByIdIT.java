package com.backbase.accesscontrol.api.client.it.functiongroup;

import static com.backbase.accesscontrol.util.helpers.ResourceAndFunctionNameConstants.ENTITLEMENTS_MANAGE_FUNCTION_GROUPS;
import static com.backbase.accesscontrol.util.helpers.ResourceAndFunctionNameConstants.PRIVILEGE_VIEW;
import static org.junit.Assert.assertEquals;

import com.backbase.accesscontrol.api.TestDbWireMock;
import com.backbase.accesscontrol.api.client.FunctionGroupsController;
import com.backbase.accesscontrol.domain.ApplicableFunctionPrivilege;
import com.backbase.accesscontrol.domain.FunctionGroup;
import com.backbase.accesscontrol.domain.GroupedFunctionPrivilege;
import com.backbase.accesscontrol.domain.LegalEntity;
import com.backbase.accesscontrol.domain.ServiceAgreement;
import com.backbase.accesscontrol.domain.enums.FunctionGroupType;
import com.backbase.accesscontrol.util.helpers.FunctionGroupUtil;
import com.backbase.accesscontrol.util.helpers.GroupedFunctionPrivilegeUtil;
import com.backbase.accesscontrol.util.helpers.UrlBuilder;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.functiongroups.FunctionGroupByIdGetResponseBody;
import java.util.LinkedHashSet;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpMethod;

/**
 * Test for {@link FunctionGroupsController#getFunctionGroupById}
 */
public class GetFunctionGroupByIdIT extends TestDbWireMock {

    private static final String GET_FUNCTION_GROUP_BY_ID_PRESENTATION_URL = "/accessgroups/function-groups/{fgId}";

    private ServiceAgreement csa;
    private FunctionGroup fgUnderMsa;
    private FunctionGroup fgUnderCsa;
    private ApplicableFunctionPrivilege afpManageFg;

    @Before
    public void setup() {

        afpManageFg = businessFunctionCache
            .getByFunctionIdAndPrivilege("1020", "view");

        LinkedHashSet<GroupedFunctionPrivilege> groupedFunctionPrivilegeList1 = new LinkedHashSet<>();
        groupedFunctionPrivilegeList1
            .add(GroupedFunctionPrivilegeUtil.getGroupedFunctionPrivilege(null, afpManageFg, null));

        LegalEntity legalEntity = rootLegalEntity;

        csa = new ServiceAgreement();
        csa.setDescription("Custom Service Agreement");
        csa.setName("Custom SA Name");
        csa.setCreatorLegalEntity(legalEntity);
        csa.setMaster(false);
        csa = serviceAgreementJpaRepository.save(csa);

        fgUnderMsa = FunctionGroupUtil
            .getFunctionGroup(null, "fg1", "desc.fg1", groupedFunctionPrivilegeList1,
                FunctionGroupType.DEFAULT, rootMsa);

        fgUnderMsa = functionGroupJpaRepository.saveAndFlush(fgUnderMsa);

        fgUnderCsa = FunctionGroupUtil
            .getFunctionGroup(null, "fg1", "desc.fg1", groupedFunctionPrivilegeList1,
                FunctionGroupType.DEFAULT, csa);

        fgUnderCsa = functionGroupJpaRepository.saveAndFlush(fgUnderCsa);
    }

    @Test
    public void getFunctionGroupByIdUnderMSA() throws Exception {

        String contentAsString = executeClientRequest(
            new UrlBuilder(GET_FUNCTION_GROUP_BY_ID_PRESENTATION_URL).addPathParameter(fgUnderMsa.getId()).build()
            , HttpMethod.GET
            , "user", ENTITLEMENTS_MANAGE_FUNCTION_GROUPS, PRIVILEGE_VIEW
        );

        FunctionGroupByIdGetResponseBody responseBody = objectMapper
            .readValue(contentAsString, FunctionGroupByIdGetResponseBody.class);

        assertEquals(fgUnderMsa.getId(), responseBody.getId());
        assertEquals(fgUnderMsa.getServiceAgreementId(), responseBody.getServiceAgreementId());
        assertEquals(fgUnderMsa.getName(), responseBody.getName());
        assertEquals(fgUnderMsa.getDescription(), responseBody.getDescription());
        assertEquals("REGULAR", responseBody.getType().toString());
    }

    @Test
    public void getFunctionGroupByIdUnderCSA() throws Exception {

        String contentAsString = executeClientRequest(
            new UrlBuilder(GET_FUNCTION_GROUP_BY_ID_PRESENTATION_URL).addPathParameter(fgUnderCsa.getId()).build()
            , HttpMethod.GET
            , "user", ENTITLEMENTS_MANAGE_FUNCTION_GROUPS, PRIVILEGE_VIEW
        );

        FunctionGroupByIdGetResponseBody returnedListOfData = objectMapper
            .readValue(contentAsString, com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.functiongroups
                .FunctionGroupByIdGetResponseBody.class);

        assertEquals(fgUnderCsa.getId(), returnedListOfData.getId());
        assertEquals(fgUnderCsa.getServiceAgreementId(), returnedListOfData.getServiceAgreementId());
        assertEquals(fgUnderCsa.getName(), returnedListOfData.getName());
        assertEquals(fgUnderCsa.getDescription(), returnedListOfData.getDescription());
        assertEquals("REGULAR", returnedListOfData.getType().toString());
    }
}
