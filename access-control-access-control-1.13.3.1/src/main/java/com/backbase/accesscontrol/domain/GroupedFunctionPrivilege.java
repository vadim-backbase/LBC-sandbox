package com.backbase.accesscontrol.domain;

import java.util.Objects;

public class GroupedFunctionPrivilege extends FunctionGroupItem {

    private FunctionGroup functionGroup;

    private String functionGroupId;

    public GroupedFunctionPrivilege() {

    }

    public GroupedFunctionPrivilege(FunctionGroup functionGroup, String applicableFunctionPrivilegeId) {
        super(applicableFunctionPrivilegeId);
        this.setFunctionGroup(functionGroup);
    }

    public FunctionGroup getFunctionGroup() {
        return functionGroup;
    }

    /**
     * Sets function group.
     *
     * @param functionGroup function group to set
     */
    public void setFunctionGroup(FunctionGroup functionGroup) {
        this.functionGroup = functionGroup;
        if (Objects.nonNull(functionGroup)) {
            this.functionGroupId = functionGroup.getId();
        }
    }

    public String getFunctionGroupId() {
        return functionGroupId;
    }

    public void setFunctionGroupId(String functionGroupId) {
        this.functionGroupId = functionGroupId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof GroupedFunctionPrivilege)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        GroupedFunctionPrivilege that = (GroupedFunctionPrivilege) o;
        return Objects.equals(functionGroupId, that.functionGroupId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), functionGroupId);
    }
}
