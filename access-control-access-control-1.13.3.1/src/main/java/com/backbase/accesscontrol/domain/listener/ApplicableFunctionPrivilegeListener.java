package com.backbase.accesscontrol.domain.listener;

import com.backbase.accesscontrol.domain.ApplicableFunctionPrivilege;
import com.backbase.accesscontrol.domain.BusinessFunction;
import com.backbase.accesscontrol.domain.Privilege;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

public class ApplicableFunctionPrivilegeListener {

    /**
     * Set table additional fields.
     *
     * @param functionPrivilege - applicable function privilege
     */
    @PrePersist
    @PreUpdate
    public void setAdditionalFields(ApplicableFunctionPrivilege functionPrivilege) {
        BusinessFunction businessFunction = functionPrivilege.getBusinessFunction();
        Privilege privilege = functionPrivilege.getPrivilege();
        functionPrivilege.setBusinessFunctionName(businessFunction.getFunctionName());
        functionPrivilege.setBusinessFunctionResourceName(businessFunction.getResourceName());
        functionPrivilege.setPrivilegeName(privilege.getName());
    }
}
