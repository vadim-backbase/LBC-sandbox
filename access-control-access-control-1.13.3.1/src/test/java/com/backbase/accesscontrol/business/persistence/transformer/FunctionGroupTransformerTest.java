package com.backbase.accesscontrol.business.persistence.transformer;

import static com.backbase.accesscontrol.matchers.FunctionGroupBaseMatcher.getFunctionGroupTMatcher;
import static com.backbase.accesscontrol.matchers.FunctionGroupBaseMatcher.getPermissionMatcher;
import static com.backbase.accesscontrol.matchers.FunctionGroupBaseMatcher.getPrivilegeMatcher;
import static com.backbase.accesscontrol.util.helpers.ApplicableFunctionPrivilegeUtil.getApplicableFunctionPrivilege;
import static com.backbase.accesscontrol.util.helpers.BusinessFunctionUtil.getBusinessFunction;
import static com.backbase.accesscontrol.util.helpers.FunctionGroupUtil.getFunctionGroup;
import static com.backbase.accesscontrol.util.helpers.GroupedFunctionPrivilegeUtil.getGroupedFunctionPrivilege;
import static com.backbase.accesscontrol.util.helpers.GroupedFunctionPrivilegeUtil.getGroupedFunctionPrivileges;
import static com.backbase.accesscontrol.util.helpers.LegalEntityUtil.createLegalEntity;
import static com.backbase.accesscontrol.util.helpers.PrivilegeUtil.getPrivilege;
import static com.backbase.accesscontrol.util.helpers.ServiceAgreementUtil.createServiceAgreement;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.domain.ApplicableFunctionPrivilege;
import com.backbase.accesscontrol.domain.FunctionGroup;
import com.backbase.accesscontrol.domain.LegalEntity;
import com.backbase.accesscontrol.domain.ServiceAgreement;
import com.backbase.accesscontrol.domain.enums.FunctionGroupType;
import com.backbase.accesscontrol.domain.enums.LegalEntityType;
import com.backbase.accesscontrol.service.BusinessFunctionCache;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.functiongroups.FunctionGroupByIdGetResponseBody;
import java.util.Collections;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class FunctionGroupTransformerTest {

    @Mock
    private BusinessFunctionCache businessFunctionCache;
    @InjectMocks
    private FunctionGroupTransformer functionGroupTransformer;

    @Test
    public void shouldTransformFunctionGroup() {

        // Given
        final String appFnPrvId = "Id2";
        final String busFnId = "bfId";
        final String privilegeName = "privilege";
        final String bank = "bank";
        final String name = "name";
        final String description = "description";
        ApplicableFunctionPrivilege applicableFunctionPrivilege = getApplicableFunctionPrivilege(
            appFnPrvId, getBusinessFunction(busFnId, "functionName", "functionCode", "rosource", "resorceCode"),
            getPrivilege("pId", privilegeName, "code"),
            true
        );

        LegalEntity legalEntity = createLegalEntity(bank, bank, bank, null, LegalEntityType.BANK);
        ServiceAgreement serviceAgreement = createServiceAgreement("SA1_1", "id.external1_1",
            description, legalEntity, null, null);

        FunctionGroup functionGroup = getFunctionGroup(
            "id", name, description,
            getGroupedFunctionPrivileges(getGroupedFunctionPrivilege(null, applicableFunctionPrivilege, null)),
            FunctionGroupType.DEFAULT, serviceAgreement);

        when(businessFunctionCache.getApplicableFunctionPrivileges(eq(Collections.singleton(appFnPrvId))))
            .thenReturn(Collections.singleton(applicableFunctionPrivilege));

        // When
        FunctionGroupByIdGetResponseBody functionGroupByIdGetResponseBody = functionGroupTransformer
            .transformFunctionGroup(FunctionGroupByIdGetResponseBody.class, functionGroup);

        // Then
        assertThat(
            functionGroupByIdGetResponseBody,
            getFunctionGroupTMatcher(is(name), is(description),
                hasItems(
                    getPermissionMatcher(
                        is(busFnId),
                        hasItems(
                            getPrivilegeMatcher(is(privilegeName))
                        )
                    )
                )
            )
        );
    }
}