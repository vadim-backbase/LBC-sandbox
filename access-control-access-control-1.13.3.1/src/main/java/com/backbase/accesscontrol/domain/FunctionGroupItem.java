package com.backbase.accesscontrol.domain;

import javax.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Embeddable
@AllArgsConstructor
@NoArgsConstructor
public class FunctionGroupItem {

    private String applicableFunctionPrivilegeId;
}
