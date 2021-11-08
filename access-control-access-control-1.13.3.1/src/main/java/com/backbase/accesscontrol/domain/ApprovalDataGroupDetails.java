package com.backbase.accesscontrol.domain;

import com.backbase.accesscontrol.domain.enums.ApprovalAction;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.NamedAttributeNode;
import javax.persistence.NamedEntityGraph;
import javax.persistence.NamedEntityGraphs;
import javax.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "approval_data_group_detail")
@NamedEntityGraphs(value = {
    @NamedEntityGraph(name = GraphConstants.APPROVAL_DATA_GROUP_WITH_ITEMS, attributeNodes = {
        @NamedAttributeNode(value = "items")}
    )
})
@Data
@NoArgsConstructor
public class ApprovalDataGroupDetails extends ApprovalDataGroup {

    @Column(name = "service_agreement_id", nullable = false)
    private String serviceAgreementId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "type", nullable = false)
    private String type;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "approval_data_group_item", joinColumns = {@JoinColumn(name = "approval_data_group_id")})
    @Column(name = "data_item_id")
    private Set<String> items = new HashSet<>();

    @Override
    public ApprovalAction getApprovalAction() {
        return Objects.nonNull(getDataGroupId()) ? ApprovalAction.EDIT : ApprovalAction.CREATE;
    }

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
        if (!(o instanceof ApprovalDataGroupDetails)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        ApprovalDataGroupDetails that = (ApprovalDataGroupDetails) o;
        return Objects.equals(serviceAgreementId, that.serviceAgreementId)
            && Objects.equals(name, that.name)
            && Objects.equals(description, that.description)
            && Objects.equals(type, that.type)
            && Objects.equals(items, that.items);
    }

    /**
     * Hashcode for uniqueness.
     *
     * @return hashcode
     */
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), serviceAgreementId, name, description, type, items);
    }
}
