
package com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Generated;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import com.backbase.buildingblocks.persistence.model.AdditionalPropertiesAware;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
    "approvalId",
    "items"
})
@JsonIgnoreProperties(ignoreUnknown = true)
public class PresentationApprovalPermissions implements AdditionalPropertiesAware
{

    /**
     * Id of approval request.
     * 
     */
    @JsonProperty("approvalId")
    private String approvalId;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("items")
    @Size(min = 0)
    @Valid
    @NotNull
    private List<com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.PresentationFunctionDataGroup> items = new ArrayList<com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.PresentationFunctionDataGroup>();
    /**
     * Additional Properties
     * 
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Map<String, String> additions = new HashMap<String, String>();

    /**
     * Id of approval request.
     * 
     * @return
     *     The approvalId
     */
    @JsonProperty("approvalId")
    public String getApprovalId() {
        return approvalId;
    }

    /**
     * Id of approval request.
     * 
     * @param approvalId
     *     The approvalId
     */
    @JsonProperty("approvalId")
    public void setApprovalId(String approvalId) {
        this.approvalId = approvalId;
    }

    public PresentationApprovalPermissions withApprovalId(String approvalId) {
        this.approvalId = approvalId;
        return this;
    }

    /**
     * 
     * (Required)
     * 
     * @return
     *     The items
     */
    @JsonProperty("items")
    public List<com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.PresentationFunctionDataGroup> getItems() {
        return items;
    }

    /**
     * 
     * (Required)
     * 
     * @param items
     *     The items
     */
    @JsonProperty("items")
    public void setItems(List<com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.PresentationFunctionDataGroup> items) {
        this.items = items;
    }

    public PresentationApprovalPermissions withItems(List<PresentationFunctionDataGroup> items) {
        this.items = items;
        return this;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(approvalId).append(items).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof PresentationApprovalPermissions) == false) {
            return false;
        }
        PresentationApprovalPermissions rhs = ((PresentationApprovalPermissions) other);
        return new EqualsBuilder().append(approvalId, rhs.approvalId).append(items, rhs.items).isEquals();
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
