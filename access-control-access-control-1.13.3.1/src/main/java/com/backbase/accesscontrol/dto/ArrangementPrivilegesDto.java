package com.backbase.accesscontrol.dto;

import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.function.Privilege;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class ArrangementPrivilegesDto {

    private String arrangementId;
    private List<Privilege> privileges = new ArrayList<>();


    public ArrangementPrivilegesDto withArrangementId(String arrangementId) {
        this.arrangementId = arrangementId;
        return this;
    }

    public ArrangementPrivilegesDto withPrivileges(List<Privilege> privileges) {
        this.privileges = privileges;
        return this;
    }

}
