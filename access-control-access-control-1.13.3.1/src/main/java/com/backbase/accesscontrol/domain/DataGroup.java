package com.backbase.accesscontrol.domain;

import com.backbase.accesscontrol.domain.validator.DataGroupType;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedAttributeNode;
import javax.persistence.NamedEntityGraph;
import javax.persistence.NamedEntityGraphs;
import javax.persistence.NamedSubgraph;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "data_group")
@NamedEntityGraphs(value = {
    @NamedEntityGraph(name = GraphConstants.DATA_GROUP_SERVICE_AGREEMENT, attributeNodes = {
        @NamedAttributeNode("serviceAgreement")}),
    @NamedEntityGraph(name = GraphConstants.DATA_GROUP_EXTENDED, attributeNodes = {
        @NamedAttributeNode(value = "dataItemIds"),
        @NamedAttributeNode("serviceAgreement")}),
    @NamedEntityGraph(name = GraphConstants.DATA_GROUP_WITH_ITEMS, attributeNodes = {
        @NamedAttributeNode(value = "dataItemIds")}),
    @NamedEntityGraph(name = GraphConstants.DATA_GROUP_WITH_SA_CREATOR, attributeNodes = {
        @NamedAttributeNode(value = "serviceAgreement", subgraph = "creator")},
        subgraphs = @NamedSubgraph(name = "creator", attributeNodes = @NamedAttributeNode("creatorLegalEntity"))),
    @NamedEntityGraph(name = GraphConstants.DATA_GROUP_EXTENDED_WITH_SA_CREATOR, attributeNodes = {
        @NamedAttributeNode(value = "dataItemIds"),
        @NamedAttributeNode(value = "serviceAgreement", subgraph = "creator")},
        subgraphs = {
            @NamedSubgraph(name = "creator", attributeNodes = @NamedAttributeNode("creatorLegalEntity"))})
})
@NoArgsConstructor
@Setter
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class DataGroup {

    @EqualsAndHashCode.Include
    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid")
    @Column(name = "id", updatable = false, nullable = false, length = 36)
    private String id;

    @EqualsAndHashCode.Include
    @Column(name = "name", nullable = false, length = 128)
    private String name;

    @EqualsAndHashCode.Include
    @DataGroupType
    @Column(name = "type", nullable = false, length = 36)
    private String dataItemType;

    @Column(name = "description", nullable = false)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.REFRESH)
    @JoinColumn(name = "service_agreement_id", nullable = false)
    private ServiceAgreement serviceAgreement;

    @EqualsAndHashCode.Include
    @Column(name = "service_agreement_id", nullable = false, updatable = false, insertable = false)
    private String serviceAgreementId;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "data_group_item", joinColumns = {@JoinColumn(name = "data_group_id")})
    @Column(name = "data_item_id", length = 36)
    private Set<String> dataItemIds = new LinkedHashSet<>();

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "data_group_id", updatable = false, insertable = false)
    private Set<DataGroupItem> dataGroupItems = new LinkedHashSet<>();

    /**
     * Sets description and trims white spaces.
     *
     * @param description - data group description.
     */
    public void setDescription(String description) {
        this.description = Optional.ofNullable(description)
            .map(String::trim)
            .orElse("");
    }

    public DataGroup withId(String id) {
        this.id = id;
        return this;
    }

    public DataGroup withName(String name) {
        this.name = name;
        return this;
    }

    public DataGroup withDescription(String description) {
        this.setDescription(description);
        return this;
    }

    public DataGroup withServiceAgreement(ServiceAgreement serviceAgreement) {
        this.serviceAgreement = serviceAgreement;
        return this;
    }

    public DataGroup withServiceAgreementId(String serviceAgreementId) {
        this.serviceAgreementId = serviceAgreementId;
        return this;
    }

    public DataGroup withDataItemIds(Set<String> dataItemIds) {
        this.dataItemIds = dataItemIds;
        return this;
    }

    public DataGroup withDataItemType(String dataItemType) {
        this.dataItemType = dataItemType;
        return this;
    }
}
