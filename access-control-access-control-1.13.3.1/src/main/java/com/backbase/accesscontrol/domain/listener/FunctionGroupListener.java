package com.backbase.accesscontrol.domain.listener;

import com.backbase.accesscontrol.domain.FunctionGroup;
import com.backbase.accesscontrol.domain.GroupedFunctionPrivilege;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

public class FunctionGroupListener {

    /**
     * Set functionGroup of grouped privilege. <br>
     * Trim description.
     *
     * @param functionGroup functionGroup;
     */
    @PrePersist
    @PreUpdate
    public void beforePersistUpdate(FunctionGroup functionGroup) {
        functionGroup.getPermissions()
            .stream()
            .filter(item -> item instanceof GroupedFunctionPrivilege)
            .map(item -> (GroupedFunctionPrivilege)item)
            .forEach(groupedFunctionPrivilege -> groupedFunctionPrivilege.setFunctionGroup(functionGroup));
    }
}
