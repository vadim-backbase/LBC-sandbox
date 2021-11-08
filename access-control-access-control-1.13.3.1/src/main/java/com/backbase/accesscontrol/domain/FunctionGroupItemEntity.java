package com.backbase.accesscontrol.domain;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity(name = "FunctionGroupItem")
@Table(name = "function_group_item", indexes = {@Index(name = "ix_fgi_01", columnList = "afp_id")})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FunctionGroupItemEntity {

    @EmbeddedId
    private FunctionGroupItemId functionGroupItemId;

    @ManyToOne
    @JoinColumn(name = "function_group_id", nullable = false, insertable = false, updatable = false)
    private FunctionGroup functionGroup;
    
    @ManyToOne
    @JoinColumn(name = "afp_id", nullable = false, insertable = false, updatable = false)
    private ApplicableFunctionPrivilege applicableFunctionPrivilege;

}
