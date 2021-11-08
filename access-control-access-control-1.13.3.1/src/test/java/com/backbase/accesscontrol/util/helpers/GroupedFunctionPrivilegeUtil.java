package com.backbase.accesscontrol.util.helpers;

import com.backbase.accesscontrol.domain.ApplicableFunctionPrivilege;
import com.backbase.accesscontrol.domain.FunctionGroup;
import com.backbase.accesscontrol.domain.GroupedFunctionPrivilege;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public class GroupedFunctionPrivilegeUtil {

    public static Set<GroupedFunctionPrivilege> getGroupedFunctionPrivileges(
        GroupedFunctionPrivilege... groupedFunctionPrivileges) {
        LinkedHashSet<GroupedFunctionPrivilege> groupedFunctionPrivilegeList = new LinkedHashSet<>();
        Collections.addAll(groupedFunctionPrivilegeList, groupedFunctionPrivileges);
        return groupedFunctionPrivilegeList;
    }

    public static GroupedFunctionPrivilege getGroupedFunctionPrivilege(String id,
        ApplicableFunctionPrivilege applicableFunctionPrivilege, FunctionGroup functionGroup) {
        GroupedFunctionPrivilege groupedFunctionPrivilege = new GroupedFunctionPrivilege();
        groupedFunctionPrivilege.setApplicableFunctionPrivilegeId(applicableFunctionPrivilege.getId());
        groupedFunctionPrivilege.setFunctionGroup(functionGroup);
        return groupedFunctionPrivilege;
    }

}
