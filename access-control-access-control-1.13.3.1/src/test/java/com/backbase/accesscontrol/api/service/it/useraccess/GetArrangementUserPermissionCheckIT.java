package com.backbase.accesscontrol.api.service.it.useraccess;

import static com.backbase.accesscontrol.util.helpers.FunctionGroupUtil.getFunctionGroup;
import static com.backbase.accesscontrol.util.helpers.GroupedFunctionPrivilegeUtil.getGroupedFunctionPrivilege;
import static com.backbase.accesscontrol.util.helpers.GroupedFunctionPrivilegeUtil.getGroupedFunctionPrivileges;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.Arrays.asList;

import com.backbase.accesscontrol.api.TestDbWireMock;
import com.backbase.accesscontrol.domain.ApplicableFunctionPrivilege;
import com.backbase.accesscontrol.domain.DataGroup;
import com.backbase.accesscontrol.domain.FunctionGroup;
import com.backbase.accesscontrol.domain.GroupedFunctionPrivilege;
import com.backbase.accesscontrol.domain.LegalEntity;
import com.backbase.accesscontrol.domain.ServiceAgreement;
import com.backbase.accesscontrol.domain.UserAssignedFunctionGroupCombination;
import com.backbase.accesscontrol.domain.UserAssignedFunctionGroup;
import com.backbase.accesscontrol.domain.UserContext;
import com.backbase.accesscontrol.domain.enums.FunctionGroupType;
import com.backbase.accesscontrol.util.helpers.DataGroupUtil;
import com.backbase.accesscontrol.util.helpers.UrlBuilder;
import com.google.common.collect.Sets;
import java.io.IOException;
import java.util.HashMap;
import org.json.JSONException;
import org.junit.Test;
import org.springframework.http.HttpMethod;

public class GetArrangementUserPermissionCheckIT extends TestDbWireMock {


    private static final String GET_ARRANGEMENT_USER_PRIVILEGES_URL = "/accessgroups/users/privileges/arrangements/{id}";

    @Test
    @SuppressWarnings("squid:S2699")
    public void testGetArrangementPermissionCheck() throws IOException, JSONException {

        UserContext userContext;
        FunctionGroup functionGroup;
        DataGroup dataGroup;
        DataGroup dataGroup2;

        String userId = contextUserId;
        String serviceAgreementId = rootMsa.getId();
        String contextLegalEntrirtyId = rootLegalEntity.getId();
        String arrangementId = "00001";
        String username = "username";

        ApplicableFunctionPrivilege apfBf1020View = businessFunctionCache
            .getByFunctionIdAndPrivilege("1020", "view");

        LegalEntity legalEntity = rootLegalEntity;

        // create SA
        ServiceAgreement serviceAgreement = rootMsa;

        //save function groupapfBf1020View
        GroupedFunctionPrivilege viewEntitlementsWithLimit = getGroupedFunctionPrivilege(null, apfBf1020View, null);

        functionGroup = functionGroupJpaRepository.saveAndFlush(
            getFunctionGroup(null, "function-group-name", "function-group-description",
                getGroupedFunctionPrivileges(
                    viewEntitlementsWithLimit
                ),
                FunctionGroupType.DEFAULT, serviceAgreement)
        );

        // create data group
        dataGroup = DataGroupUtil.createDataGroup("name", "ARRANGEMENTS", "desc", serviceAgreement);
        dataGroup.setDataItemIds(newHashSet("00001", "00002", "00003"));
         dataGroup = dataGroupJpaRepository.saveAndFlush(dataGroup);
        dataGroup2 = DataGroupUtil.createDataGroup("name2", "ARRANGEMENTS", "desc2", serviceAgreement);
        dataGroup2.setDataItemIds(newHashSet("00004", "00005", "00006"));
        dataGroup2=dataGroupJpaRepository.saveAndFlush(dataGroup2);

        userContext = new UserContext(userId, serviceAgreement.getId());

        UserAssignedFunctionGroup uafg = new UserAssignedFunctionGroup(functionGroup, userContext);
        uafg.setUserAssignedFunctionGroupCombinations(
            newHashSet(new UserAssignedFunctionGroupCombination(Sets.newHashSet(dataGroup.getId()), uafg)));

        userContext.setUserAssignedFunctionGroups(newHashSet(uafg));
        userContextJpaRepository.save(userContext);

        executeServiceRequest(
            new UrlBuilder(GET_ARRANGEMENT_USER_PRIVILEGES_URL)
                .addPathParameter(arrangementId)
                .addQueryParameter("userId", userId)
                .addQueryParameter("serviceAgreementId", serviceAgreement.getId())
                .addQueryParameter("function", apfBf1020View.getBusinessFunctionName())
                .addQueryParameter("resource", apfBf1020View.getBusinessFunctionResourceName())
                .addQueryParameter("privilege", apfBf1020View.getPrivilegeName())
                .build(), "", username, serviceAgreementId, userId, contextLegalEntrirtyId,
            HttpMethod.GET, new HashMap<>());
    }
}
