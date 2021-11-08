package com.backbase.accesscontrol.util.helpers;

import com.backbase.accesscontrol.domain.ApplicableFunctionPrivilege;
import com.backbase.accesscontrol.domain.BusinessFunction;
import com.backbase.accesscontrol.domain.Privilege;

public class ApplicableFunctionPrivilegeUtil {

    public static ApplicableFunctionPrivilege getApplicableFunctionPrivilege(String id,
        BusinessFunction businessFunction, Privilege privilege, boolean supportLimits) {
        ApplicableFunctionPrivilege applicableFunctionPrivilege = new ApplicableFunctionPrivilege();
        applicableFunctionPrivilege.setId(id);
        applicableFunctionPrivilege.setBusinessFunction(businessFunction);
        applicableFunctionPrivilege.setPrivilege(privilege);
        applicableFunctionPrivilege.setPrivilegeName(privilege != null ? privilege.getName() : null);
        applicableFunctionPrivilege.setSupportsLimit(supportLimits);
        return applicableFunctionPrivilege;
    }
}
