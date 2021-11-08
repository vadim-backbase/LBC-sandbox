package com.backbase.accesscontrol.domain;

import static java.util.Objects.nonNull;
import static javax.persistence.FetchType.LAZY;

import com.backbase.accesscontrol.domain.enums.LegalEntityType;
import com.backbase.accesscontrol.domain.listener.LegalEntityHierarchyListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.MapKeyColumn;
import javax.persistence.NamedAttributeNode;
import javax.persistence.NamedEntityGraph;
import javax.persistence.NamedSubgraph;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.With;
import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "legal_entity",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = "external_id", name = "uk_legal_entity_external_id"),
        @UniqueConstraint(columnNames = "name", name = "uk_legal_entity_name")
    })
@NamedEntityGraph(name = GraphConstants.GRAPH_LEGAL_ENTITY_WITH_CHILDREN_AND_PARENT, attributeNodes = {
    @NamedAttributeNode(value = "additions"),
    @NamedAttributeNode("children"),
    @NamedAttributeNode("parent")
})
@NamedEntityGraph(name = GraphConstants.GRAPH_LEGAL_ENTITY_WITH_PARENT_AND_ADDITIONS, attributeNodes = {
    @NamedAttributeNode(value = "additions"),
    @NamedAttributeNode("parent")
})
@NamedEntityGraph(name = GraphConstants.GRAPH_LEGAL_ENTITY_WITH_ANCESTORS_AND_ADDITIONS, attributeNodes = {
    @NamedAttributeNode(value = "additions"),
    @NamedAttributeNode(value = "legalEntityAncestors", subgraph = "ancestors")},
    subgraphs = @NamedSubgraph(
        name = "ancestors", attributeNodes = {
        @NamedAttributeNode("additions"),
        @NamedAttributeNode("children"),
        @NamedAttributeNode("parent")}
    ))
@NamedEntityGraph(name = GraphConstants.GRAPH_LEGAL_ENTITY_WITH_ADDITIONS, attributeNodes = {
    @NamedAttributeNode(value = "additions")
})
@NamedEntityGraph(name = GraphConstants.GRAPH_LEGAL_ENTITY_WITH_ANCESTORS, attributeNodes = {
    @NamedAttributeNode(value = "legalEntityAncestors"),
})
@EntityListeners(value = LegalEntityHierarchyListener.class)
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@With
public class LegalEntity {

    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid")
    @Column(name = "id", updatable = false, nullable = false, length = 36)
    private String id;

    @Column(name = "external_id", nullable = false, length = 64)
    private String externalId;

    @Column(name = "name", nullable = false, length = 128)
    private String name;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "parent_id",
        referencedColumnName = "id",
        foreignKey = @ForeignKey(name = "fk_legal_entity_parent_id")
    )
    private LegalEntity parent;

    @OneToMany(mappedBy = "parent", fetch = LAZY)
    private Set<LegalEntity> children = new LinkedHashSet<>();

    @ManyToMany(fetch = LAZY)
    @JoinTable(name = "legal_entity_ancestor",
        joinColumns = {@JoinColumn(name = "descendent_id",
            referencedColumnName = "id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_ancestor_legal_entity_id"))},
        inverseJoinColumns = {@JoinColumn(name = "ancestor_id",
            referencedColumnName = "id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_legal_entity_id"))
        },
        uniqueConstraints = @UniqueConstraint(name = "uk_le_ancestor_leid_leid",
            columnNames = {"ancestor_id", "descendent_id"})
    )
    private List<LegalEntity> legalEntityAncestors = new ArrayList<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @MapKeyColumn(name = "property_key", length = 50)
    @Column(name = "property_value", length = 500)
    @CollectionTable(name = "add_prop_legal_entity", joinColumns = @JoinColumn(name = "add_prop_legal_entity_id"))
    private Map<String, String> additions = new HashMap<>();

    @Column(name = "type", length = 8)
    @Enumerated(EnumType.STRING)
    private LegalEntityType type;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "descendent_id", referencedColumnName = "id", updatable = false, insertable = false)
    private List<LegalEntityAncestor> legalEntityDescendents;

    /**
     * Proper equals method.
     *
     * @param o - object for comparision
     * @return true/false
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        LegalEntity that = (LegalEntity) o;
        return Objects.equals(id, that.id)
            && Objects.equals(externalId, that.externalId)
            && Objects.equals(name, that.name);
    }

    /**
     * Hashcode for uniqueness.
     *
     * @return hashcode
     */
    @Override
    public int hashCode() {
        return Objects.hash(id, externalId, name);
    }

    /**
     * Gets additions.
     *
     * @return map with all additions wrapped up.
     */
    public Map<String, String> getAdditions() {
        return this.additions;
    }

    /**
     * Additions setter.
     *
     * @param additions - additions map
     */
    public void setAdditions(Map<String, String> additions) {
        this.additions.clear();
        if(nonNull(additions)) {
            this.additions.putAll(additions);
        }
    }

    public void setAddition(String key, String value) {
        this.additions.put(key, value);
    }
}
