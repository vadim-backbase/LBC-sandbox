package com.backbase.accesscontrol.util.helpers;

import com.backbase.accesscontrol.domain.FunctionGroup;
import com.backbase.accesscontrol.domain.GroupedFunctionPrivilege;
import com.backbase.accesscontrol.domain.ServiceAgreement;
import com.backbase.accesscontrol.domain.enums.FunctionGroupType;
import java.util.Set;

public class FunctionGroupUtil {

    public static FunctionGroup getFunctionGroup(String id, String name, String description,
        Set<GroupedFunctionPrivilege> groupedFunctionPrivilegeList, FunctionGroupType type,
        ServiceAgreement serviceAgreement) {
        FunctionGroup functionGroup = new FunctionGroup();
        functionGroup.setId(id);
        functionGroup.setName(name);
        functionGroup.setDescription(description);
        functionGroup.setPermissions(groupedFunctionPrivilegeList);
        functionGroup.setType(type);
        functionGroup.setServiceAgreementId(serviceAgreement != null ? serviceAgreement.getId() : null);
        functionGroup.setServiceAgreement(serviceAgreement);
        return functionGroup;
    }
}
