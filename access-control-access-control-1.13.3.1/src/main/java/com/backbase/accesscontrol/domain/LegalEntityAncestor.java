package com.backbase.accesscontrol.domain;

import com.backbase.accesscontrol.domain.idclass.LegalEntityAncestorIdClass;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Immutable;

@Entity
@Table(name = "legal_entity_ancestor")
@IdClass(LegalEntityAncestorIdClass.class)
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Immutable
public class LegalEntityAncestor {

    @Id
    @Column(name = "descendent_id", insertable = false, updatable = false, nullable = false, length = 36)
    private String descendentId;

    @Id
    @Column(name = "ancestor_id", insertable = false, updatable = false, nullable = false, length = 36)
    private String ancestorId;

}
