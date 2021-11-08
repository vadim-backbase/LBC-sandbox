package com.backbase.accesscontrol.api.client.it.user;

import static com.backbase.accesscontrol.util.helpers.FunctionGroupUtil.getFunctionGroup;
import static com.backbase.accesscontrol.util.helpers.GroupedFunctionPrivilegeUtil.getGroupedFunctionPrivilege;
import static com.backbase.accesscontrol.util.helpers.GroupedFunctionPrivilegeUtil.getGroupedFunctionPrivileges;
import static com.backbase.accesscontrol.util.helpers.ServiceAgreementUtil.createServiceAgreement;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.backbase.accesscontrol.api.TestDbWireMock;
import com.backbase.accesscontrol.api.client.UsersController;
import com.backbase.accesscontrol.domain.ApplicableFunctionPrivilege;
import com.backbase.accesscontrol.domain.FunctionGroup;
import com.backbase.accesscontrol.domain.GroupedFunctionPrivilege;
import com.backbase.accesscontrol.domain.LegalEntity;
import com.backbase.accesscontrol.domain.ServiceAgreement;
import com.backbase.accesscontrol.domain.UserAssignedFunctionGroup;
import com.backbase.accesscontrol.domain.UserContext;
import com.backbase.accesscontrol.domain.enums.FunctionGroupType;
import com.backbase.accesscontrol.util.helpers.UrlBuilder;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.users.UserPermissionsSummaryGetResponseBody;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpMethod;

/**
 * Test for {@link UsersController#getUserPermissionsSummary
 * (HttpServletRequest, HttpServletResponse)}
 */
public class GetPrivilegesSummaryIT extends TestDbWireMock {

    private static final String PRIVILEGES_URL = "/accessgroups/users/permissions/summary";

    private String userId;

    private ServiceAgreement serviceAgreement;
    private ApplicableFunctionPrivilege apfBf1002Create;
    private ApplicableFunctionPrivilege apfBf1003View;
    private ApplicableFunctionPrivilege apfBf1003Edit;

    @Before
    public void setUp() {

        userId = contextUserId;
        LegalEntity legalEntity = rootLegalEntity;
        apfBf1002Create = businessFunctionCache
            .getByFunctionIdAndPrivilege("1002", "create");
        apfBf1003View = businessFunctionCache
            .getByFunctionIdAndPrivilege("1003", "view");
        apfBf1003Edit = businessFunctionCache
            .getByFunctionIdAndPrivilege("1003", "edit");

        LocalDateTime localDateTIme = LocalDateTime.now();
        Date startDate = Date.from(localDateTIme.minusDays(2).atZone(ZoneId.systemDefault()).toInstant());
        Date endDate = Date.from(localDateTIme.plusDays(2).atZone(ZoneId.systemDefault()).toInstant());

        serviceAgreement =
            createServiceAgreement("BB between self", "id.external", "desc", legalEntity, legalEntity.getId(),
                legalEntity.getId());
        serviceAgreement.setStartDate(startDate);
        serviceAgreement.setEndDate(endDate);
        serviceAgreementJpaRepository.save(serviceAgreement);

        UserContext userContext = new UserContext(userId, serviceAgreement.getId());
        userContext = userContextJpaRepository.save(userContext);

        GroupedFunctionPrivilege apfBf1002CreateAfp = getGroupedFunctionPrivilege(null, apfBf1002Create, null);
        GroupedFunctionPrivilege apfBf1003ViewAfp = getGroupedFunctionPrivilege(null, apfBf1003View,
            null);
        GroupedFunctionPrivilege apfBf1003EditAfp = getGroupedFunctionPrivilege(null, apfBf1003Edit, null);

        FunctionGroup functionGroup = functionGroupJpaRepository.save(
            getFunctionGroup(null, "function-group-name", "function-group-description",
                getGroupedFunctionPrivileges(
                    apfBf1002CreateAfp,
                    apfBf1003ViewAfp,
                    apfBf1003EditAfp
                ),
                FunctionGroupType.DEFAULT, serviceAgreement)
        );

        functionGroupJpaRepository.flush();

        UserAssignedFunctionGroup userAssignedFunctionGroup = new UserAssignedFunctionGroup(functionGroup, userContext);
        userAssignedFunctionGroupJpaRepository.save(userAssignedFunctionGroup);
    }

    @Test
    public void shouldGetPrivilegesSummary() throws Exception {

        String contentAsString = executeClientRequestWithContext(
            new UrlBuilder(PRIVILEGES_URL)
                .build(), HttpMethod.GET, "USER",
            new UserContext(userId, serviceAgreement.getId()), rootLegalEntity.getId());

        UserPermissionsSummaryGetResponseBody[] returnedPrivileges = objectMapper
            .readValue(contentAsString,
                UserPermissionsSummaryGetResponseBody[].class);
        assertEquals(2, returnedPrivileges.length);
        assertEquals(apfBf1003Edit.getBusinessFunctionName(), returnedPrivileges[0].getFunction());
        assertEquals(apfBf1003Edit.getBusinessFunctionResourceName(), returnedPrivileges[0].getResource());
        assertTrue(returnedPrivileges[0].getPermissions().get("view"));
        assertTrue(returnedPrivileges[0].getPermissions().get("edit"));
        assertEquals(2, returnedPrivileges.length);
        assertEquals(apfBf1002Create.getBusinessFunctionName(), returnedPrivileges[1].getFunction());
        assertEquals(apfBf1002Create.getBusinessFunctionResourceName(), returnedPrivileges[1].getResource());
        assertTrue(returnedPrivileges[1].getPermissions().get("create"));
    }
}
