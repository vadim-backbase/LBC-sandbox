
package com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import javax.annotation.Generated;
import javax.validation.Valid;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import com.backbase.buildingblocks.persistence.model.AdditionalPropertiesAware;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.approvals.PersistenceDataGroupState;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Approval details
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
    "dataGroupId",
    "serviceAgreementId",
    "serviceAgreementName",
    "type",
    "approvalId",
    "action",
    "oldState",
    "newState",
    "addedDataItems",
    "removedDataItems",
    "unmodifiedDataItems",
    "legalEntityIds"
})
@JsonIgnoreProperties(ignoreUnknown = true)
public class PresentationDataGroupApprovalDetailsItem implements AdditionalPropertiesAware
{

    /**
     * Universally Unique Identifier.
     * 
     */
    @JsonProperty("dataGroupId")
    @Pattern(regexp = "^[0-9a-f]{32}$|^[0-9a-f-]{36}$")
    @Size(min = 1, max = 36)
    private String dataGroupId;
    /**
     * Universally Unique Identifier.
     * 
     */
    @JsonProperty("serviceAgreementId")
    @Pattern(regexp = "^[0-9a-f]{32}$|^[0-9a-f-]{36}$")
    @Size(min = 1, max = 36)
    private String serviceAgreementId;
    /**
     * Name of the service agreement
     * 
     */
    @JsonProperty("serviceAgreementName")
    private String serviceAgreementName;
    /**
     * Data group type
     * 
     */
    @JsonProperty("type")
    @Pattern(regexp = "^\\S+$")
    @Size(min = 1, max = 36)
    private String type;
    /**
     * Approval id
     * 
     */
    @JsonProperty("approvalId")
    private String approvalId;
    @JsonProperty("action")
    private com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.PresentationApprovalAction action;
    /**
     * State of the name and description of the data group
     * 
     */
    @JsonProperty("oldState")
    @Valid
    private PersistenceDataGroupState oldState;
    /**
     * State of the name and description of the data group
     * 
     */
    @JsonProperty("newState")
    @Valid
    private PersistenceDataGroupState newState;
    @JsonProperty("addedDataItems")
    @JsonDeserialize(as = LinkedHashSet.class)
    @Valid
    private Set<String> addedDataItems = new LinkedHashSet<String>();
    @JsonProperty("removedDataItems")
    @JsonDeserialize(as = LinkedHashSet.class)
    @Valid
    private Set<String> removedDataItems = new LinkedHashSet<String>();
    @JsonProperty("unmodifiedDataItems")
    @JsonDeserialize(as = LinkedHashSet.class)
    @Valid
    private Set<String> unmodifiedDataItems = new LinkedHashSet<String>();
    @JsonProperty("legalEntityIds")
    @JsonDeserialize(as = LinkedHashSet.class)
    @Valid
    private Set<String> legalEntityIds = new LinkedHashSet<String>();
    /**
     * Additional Properties
     * 
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Map<String, String> additions = new HashMap<String, String>();

    /**
     * Universally Unique Identifier.
     * 
     * @return
     *     The dataGroupId
     */
    @JsonProperty("dataGroupId")
    public String getDataGroupId() {
        return dataGroupId;
    }

    /**
     * Universally Unique Identifier.
     * 
     * @param dataGroupId
     *     The dataGroupId
     */
    @JsonProperty("dataGroupId")
    public void setDataGroupId(String dataGroupId) {
        this.dataGroupId = dataGroupId;
    }

    public PresentationDataGroupApprovalDetailsItem withDataGroupId(String dataGroupId) {
        this.dataGroupId = dataGroupId;
        return this;
    }

    /**
     * Universally Unique Identifier.
     * 
     * @return
     *     The serviceAgreementId
     */
    @JsonProperty("serviceAgreementId")
    public String getServiceAgreementId() {
        return serviceAgreementId;
    }

    /**
     * Universally Unique Identifier.
     * 
     * @param serviceAgreementId
     *     The serviceAgreementId
     */
    @JsonProperty("serviceAgreementId")
    public void setServiceAgreementId(String serviceAgreementId) {
        this.serviceAgreementId = serviceAgreementId;
    }

    public PresentationDataGroupApprovalDetailsItem withServiceAgreementId(String serviceAgreementId) {
        this.serviceAgreementId = serviceAgreementId;
        return this;
    }

    /**
     * Name of the service agreement
     * 
     * @return
     *     The serviceAgreementName
     */
    @JsonProperty("serviceAgreementName")
    public String getServiceAgreementName() {
        return serviceAgreementName;
    }

    /**
     * Name of the service agreement
     * 
     * @param serviceAgreementName
     *     The serviceAgreementName
     */
    @JsonProperty("serviceAgreementName")
    public void setServiceAgreementName(String serviceAgreementName) {
        this.serviceAgreementName = serviceAgreementName;
    }

    public PresentationDataGroupApprovalDetailsItem withServiceAgreementName(String serviceAgreementName) {
        this.serviceAgreementName = serviceAgreementName;
        return this;
    }

    /**
     * Data group type
     * 
     * @return
     *     The type
     */
    @JsonProperty("type")
    public String getType() {
        return type;
    }

    /**
     * Data group type
     * 
     * @param type
     *     The type
     */
    @JsonProperty("type")
    public void setType(String type) {
        this.type = type;
    }

    public PresentationDataGroupApprovalDetailsItem withType(String type) {
        this.type = type;
        return this;
    }

    /**
     * Approval id
     * 
     * @return
     *     The approvalId
     */
    @JsonProperty("approvalId")
    public String getApprovalId() {
        return approvalId;
    }

    /**
     * Approval id
     * 
     * @param approvalId
     *     The approvalId
     */
    @JsonProperty("approvalId")
    public void setApprovalId(String approvalId) {
        this.approvalId = approvalId;
    }

    public PresentationDataGroupApprovalDetailsItem withApprovalId(String approvalId) {
        this.approvalId = approvalId;
        return this;
    }

    /**
     * 
     * @return
     *     The action
     */
    @JsonProperty("action")
    public com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.PresentationApprovalAction getAction() {
        return action;
    }

    /**
     * 
     * @param action
     *     The action
     */
    @JsonProperty("action")
    public void setAction(
        com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.PresentationApprovalAction action) {
        this.action = action;
    }

    public PresentationDataGroupApprovalDetailsItem withAction(PresentationApprovalAction action) {
        this.action = action;
        return this;
    }

    /**
     * State of the name and description of the data group
     * 
     * @return
     *     The oldState
     */
    @JsonProperty("oldState")
    public PersistenceDataGroupState getOldState() {
        return oldState;
    }

    /**
     * State of the name and description of the data group
     * 
     * @param oldState
     *     The oldState
     */
    @JsonProperty("oldState")
    public void setOldState(PersistenceDataGroupState oldState) {
        this.oldState = oldState;
    }

    public PresentationDataGroupApprovalDetailsItem withOldState(PersistenceDataGroupState oldState) {
        this.oldState = oldState;
        return this;
    }

    /**
     * State of the name and description of the data group
     * 
     * @return
     *     The newState
     */
    @JsonProperty("newState")
    public PersistenceDataGroupState getNewState() {
        return newState;
    }

    /**
     * State of the name and description of the data group
     * 
     * @param newState
     *     The newState
     */
    @JsonProperty("newState")
    public void setNewState(PersistenceDataGroupState newState) {
        this.newState = newState;
    }

    public PresentationDataGroupApprovalDetailsItem withNewState(PersistenceDataGroupState newState) {
        this.newState = newState;
        return this;
    }

    /**
     * 
     * @return
     *     The addedDataItems
     */
    @JsonProperty("addedDataItems")
    public Set<String> getAddedDataItems() {
        return addedDataItems;
    }

    /**
     * 
     * @param addedDataItems
     *     The addedDataItems
     */
    @JsonProperty("addedDataItems")
    public void setAddedDataItems(Set<String> addedDataItems) {
        this.addedDataItems = addedDataItems;
    }

    public PresentationDataGroupApprovalDetailsItem withAddedDataItems(Set<String> addedDataItems) {
        this.addedDataItems = addedDataItems;
        return this;
    }

    /**
     * 
     * @return
     *     The removedDataItems
     */
    @JsonProperty("removedDataItems")
    public Set<String> getRemovedDataItems() {
        return removedDataItems;
    }

    /**
     * 
     * @param removedDataItems
     *     The removedDataItems
     */
    @JsonProperty("removedDataItems")
    public void setRemovedDataItems(Set<String> removedDataItems) {
        this.removedDataItems = removedDataItems;
    }

    public PresentationDataGroupApprovalDetailsItem withRemovedDataItems(Set<String> removedDataItems) {
        this.removedDataItems = removedDataItems;
        return this;
    }

    /**
     * 
     * @return
     *     The unmodifiedDataItems
     */
    @JsonProperty("unmodifiedDataItems")
    public Set<String> getUnmodifiedDataItems() {
        return unmodifiedDataItems;
    }

    /**
     * 
     * @param unmodifiedDataItems
     *     The unmodifiedDataItems
     */
    @JsonProperty("unmodifiedDataItems")
    public void setUnmodifiedDataItems(Set<String> unmodifiedDataItems) {
        this.unmodifiedDataItems = unmodifiedDataItems;
    }

    public PresentationDataGroupApprovalDetailsItem withUnmodifiedDataItems(Set<String> unmodifiedDataItems) {
        this.unmodifiedDataItems = unmodifiedDataItems;
        return this;
    }

    /**
     * 
     * @return
     *     The legalEntityIds
     */
    @JsonProperty("legalEntityIds")
    public Set<String> getLegalEntityIds() {
        return legalEntityIds;
    }

    /**
     * 
     * @param legalEntityIds
     *     The legalEntityIds
     */
    @JsonProperty("legalEntityIds")
    public void setLegalEntityIds(Set<String> legalEntityIds) {
        this.legalEntityIds = legalEntityIds;
    }

    public PresentationDataGroupApprovalDetailsItem withLegalEntityIds(Set<String> legalEntityIds) {
        this.legalEntityIds = legalEntityIds;
        return this;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(dataGroupId).append(serviceAgreementId).append(serviceAgreementName).append(type).append(approvalId).append(action).append(oldState).append(newState).append(addedDataItems).append(removedDataItems).append(unmodifiedDataItems).append(legalEntityIds).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof PresentationDataGroupApprovalDetailsItem) == false) {
            return false;
        }
        PresentationDataGroupApprovalDetailsItem rhs = ((PresentationDataGroupApprovalDetailsItem) other);
        return new EqualsBuilder().append(dataGroupId, rhs.dataGroupId).append(serviceAgreementId, rhs.serviceAgreementId).append(serviceAgreementName, rhs.serviceAgreementName).append(type, rhs.type).append(approvalId, rhs.approvalId).append(action, rhs.action).append(oldState, rhs.oldState).append(newState, rhs.newState).append(addedDataItems, rhs.addedDataItems).append(removedDataItems, rhs.removedDataItems).append(unmodifiedDataItems, rhs.unmodifiedDataItems).append(legalEntityIds, rhs.legalEntityIds).isEquals();
    }

    /**
     * {@inheritDoc}
     * 
     */
    @Override
    @JsonProperty("additions")
    public Map<String, String> getAdditions() {
        return this.additions;
    }

    /**
     * {@inheritDoc}
     * 
     */
    @Override
    @JsonProperty("additions")
    public void setAdditions(Map<String, String> additions) {
        this.additions = additions;
    }

}
