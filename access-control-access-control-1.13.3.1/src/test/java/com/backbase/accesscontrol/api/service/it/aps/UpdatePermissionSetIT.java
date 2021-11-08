package com.backbase.accesscontrol.api.service.it.aps;

import static com.backbase.accesscontrol.util.helpers.FunctionGroupUtil.getFunctionGroup;
import static com.backbase.accesscontrol.util.helpers.GroupedFunctionPrivilegeUtil.getGroupedFunctionPrivilege;
import static com.backbase.pandp.accesscontrol.event.spec.v1.Action.UPDATE;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.mapstruct.ap.internal.util.Collections.asSet;

import com.backbase.accesscontrol.api.TestDbWireMock;
import com.backbase.accesscontrol.api.service.PermissionSetServiceApiController;
import com.backbase.accesscontrol.domain.ApplicableFunctionPrivilege;
import com.backbase.accesscontrol.domain.AssignablePermissionSet;
import com.backbase.accesscontrol.domain.FunctionGroup;
import com.backbase.accesscontrol.domain.GroupedFunctionPrivilege;
import com.backbase.accesscontrol.domain.LegalEntity;
import com.backbase.accesscontrol.domain.ServiceAgreement;
import com.backbase.accesscontrol.domain.UserAssignedFunctionGroup;
import com.backbase.accesscontrol.domain.UserContext;
import com.backbase.accesscontrol.domain.enums.AssignablePermissionType;
import com.backbase.accesscontrol.domain.enums.FunctionGroupType;
import com.backbase.accesscontrol.domain.enums.LegalEntityType;
import com.backbase.accesscontrol.util.helpers.LegalEntityUtil;
import com.backbase.accesscontrol.util.helpers.ServiceAgreementUtil;
import com.backbase.pandp.accesscontrol.event.spec.v1.ServiceAgreementEvent;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.aps.PresentationPermissionSetItemPut;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.aps.PresentationUserApsIdentifiers;
import com.google.common.collect.Sets;
import java.math.BigDecimal;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpMethod;

/**
 * Test for {@link PermissionSetServiceApiController#putPermissionSet(PresentationPermissionSetItemPut
 * presentationPermissionSetItemPut, HttpServletRequest, HttpServletResponse)}
 */
public class UpdatePermissionSetIT extends TestDbWireMock {

    private static final String URL = "/accessgroups/permission-sets";

    private ServiceAgreement serviceAgreement;

    private AssignablePermissionSet apsUser1;

    @Before
    public void setUp() {
        repositoryCleaner.clean();

        FunctionGroup systemFG = null;
        FunctionGroup defaultFG1 = null;
        FunctionGroup defaultFg2 = null;

        ApplicableFunctionPrivilege apfBf1020View = businessFunctionCache
            .getByFunctionIdAndPrivilege("1020", "view");
        ApplicableFunctionPrivilege apfBf1002Create = businessFunctionCache
            .getByFunctionIdAndPrivilege("1002", "create");
        ApplicableFunctionPrivilege apfBf1003View = businessFunctionCache
            .getByFunctionIdAndPrivilege("1003", "view");

        String userId = getUuid();
        AssignablePermissionSet apsAdmin1 = new AssignablePermissionSet();
        apsAdmin1.setName("name0");
        apsAdmin1.setDescription("dsc1");
        apsAdmin1.setType(AssignablePermissionType.CUSTOM);
        apsAdmin1.setPermissions(Sets.newHashSet(asList(apfBf1020View.getId(), apfBf1002Create.getId())));
        AssignablePermissionSet apsAdmin2 = new AssignablePermissionSet();
        apsAdmin2.setName("name11");
        apsAdmin2.setDescription("dsc2");
        apsAdmin2.setPermissions(Sets.newHashSet(asList(apfBf1020View.getId())));
        apsAdmin2.setType(AssignablePermissionType.CUSTOM);
        apsUser1 = new AssignablePermissionSet();
        apsUser1.setName("name1");
        apsUser1.setDescription("dsc3");
        apsUser1.setType(AssignablePermissionType.CUSTOM);
        apsUser1.setPermissions(
            Sets.newHashSet(asList(apfBf1003View.getId(), apfBf1002Create.getId())));
        AssignablePermissionSet apsUser2 = new AssignablePermissionSet();
        apsUser2.setName("name3");
        apsUser2.setDescription("dsc4");
        apsUser2.setType(AssignablePermissionType.CUSTOM);
        apsUser2.setPermissions(Sets.newHashSet(asList(apfBf1003View.getId(), apfBf1002Create.getId())));
        AssignablePermissionSet randomAps = new AssignablePermissionSet();
        randomAps.setName("name12");
        randomAps.setDescription("desc5");
        randomAps.setType(AssignablePermissionType.CUSTOM);
        randomAps.setPermissions(Sets.newHashSet(singletonList(apfBf1003View.getId())));
        assignablePermissionSetJpaRepository
            .saveAll(asList(apsAdmin1, apsAdmin2, apsUser1, apsUser2, randomAps));

        GroupedFunctionPrivilege gfItem1 = getGroupedFunctionPrivilege(null, apfBf1020View, systemFG);
        GroupedFunctionPrivilege gfItem11 = getGroupedFunctionPrivilege(null, apfBf1002Create, systemFG);
        GroupedFunctionPrivilege gfItem2 = getGroupedFunctionPrivilege(null, apfBf1002Create, defaultFG1);
        GroupedFunctionPrivilege gfItem3 = getGroupedFunctionPrivilege(null, apfBf1003View, defaultFg2);

        Set<GroupedFunctionPrivilege> gfpSet1 = Sets.newHashSet(asList(gfItem1, gfItem11));
        Set<GroupedFunctionPrivilege> gfpSet2 = Sets.newHashSet(singletonList(gfItem2));
        Set<GroupedFunctionPrivilege> gfpSet3 = Sets.newHashSet(singletonList(gfItem3));
        LegalEntity legalEntity = LegalEntityUtil
            .createLegalEntity(null, "le-name", "ex-id3", null, LegalEntityType.BANK);
        legalEntity = legalEntityJpaRepository.save(legalEntity);
        serviceAgreement = ServiceAgreementUtil
            .createServiceAgreement("sa-name", "sa-exid", "sa-desc", legalEntity, legalEntity.getId(),
                legalEntity.getId());
        serviceAgreement.setMaster(true);
        serviceAgreement.getPermissionSetsAdmin().addAll(singletonList(apsAdmin1));
        serviceAgreement.getPermissionSetsRegular().addAll(singletonList(apsUser2));
        serviceAgreement.getParticipants().get(legalEntity.getId()).addAdmin(userId);
        serviceAgreement = serviceAgreementJpaRepository.save(serviceAgreement);

        systemFG = getFunctionGroup(null, "SYSTEM_FUNCTION_GROUP", "desc system fg", gfpSet1,
            FunctionGroupType.SYSTEM, serviceAgreement);
        defaultFG1 = getFunctionGroup(null, "default fg1 name", "desc default fg1", gfpSet2,
            FunctionGroupType.DEFAULT, serviceAgreement);
        defaultFg2 = getFunctionGroup(null, "default fg2 name", "desc default fg2", gfpSet3,
            FunctionGroupType.DEFAULT, serviceAgreement);
        Set<FunctionGroup> functionGroups = Sets.newHashSet(asList(systemFG, defaultFG1, defaultFg2));
        functionGroupJpaRepository.saveAll(functionGroups);
        functionGroupJpaRepository.flush();

        UserContext userContext = userContextJpaRepository
            .saveAndFlush(new UserContext(userId, serviceAgreement.getId()));
        UserAssignedFunctionGroup userAssignedFunctionGroup = new UserAssignedFunctionGroup(systemFG, userContext);

        userAssignedFunctionGroupJpaRepository.saveAndFlush(userAssignedFunctionGroup);

    }

    @Test
    @SuppressWarnings("squid:S2699")
    public void shouldUpdatePermissionSet() throws Exception {
        PresentationPermissionSetItemPut itemPut = new PresentationPermissionSetItemPut()
            .withExternalServiceAgreementId(serviceAgreement.getExternalId())
            .withAdminUserAps(new PresentationUserApsIdentifiers()
                .withIdIdentifiers(asSet(new BigDecimal(1L))))
            .withRegularUserAps(new PresentationUserApsIdentifiers()
                .withNameIdentifiers(asSet("name1")));

        executeServiceRequest(URL, objectMapper.writeValueAsString(itemPut), null, null, HttpMethod.PUT);

        verifyServiceAgreementEvents(Sets.newHashSet(new ServiceAgreementEvent()
            .withAction(UPDATE)
            .withId(serviceAgreement.getId())));
    }
}
