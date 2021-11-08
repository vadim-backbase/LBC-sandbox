package com.backbase.accesscontrol.domain.dto;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class Permission {

    private String functionId;
    private List<PrivilegeDto> assignedPrivileges = new ArrayList<>();

    public Permission withFunctionId(String functionId) {
        this.functionId = functionId;
        return this;
    }

    public Permission withAssignedPrivileges(List<PrivilegeDto> assignedPrivileges) {
        this.assignedPrivileges = assignedPrivileges;
        return this;
    }
}
