package com.backbase.accesscontrol.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FunctionGroupItemId implements Serializable {

    @Column(name = "function_group_id", nullable = false)
    private String functionGroupId;

    @Column(name = "afp_id", nullable = false)
    private String applicableFunctionPrivilegeId;
}
