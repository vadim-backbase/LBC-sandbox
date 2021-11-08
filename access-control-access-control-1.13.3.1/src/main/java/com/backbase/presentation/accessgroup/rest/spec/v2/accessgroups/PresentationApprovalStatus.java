
package com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Generated;
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
    "approvalStatus"
})
@JsonIgnoreProperties(ignoreUnknown = true)
public class PresentationApprovalStatus implements AdditionalPropertiesAware
{

    /**
     * Presentation approval status
     * 
     */
    @JsonProperty("approvalStatus")
    private com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.ApprovalStatus approvalStatus;
    /**
     * Additional Properties
     * 
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Map<String, String> additions = new HashMap<String, String>();

    /**
     * Presentation approval status
     * 
     * @return
     *     The approvalStatus
     */
    @JsonProperty("approvalStatus")
    public com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.ApprovalStatus getApprovalStatus() {
        return approvalStatus;
    }

    /**
     * Presentation approval status
     * 
     * @param approvalStatus
     *     The approvalStatus
     */
    @JsonProperty("approvalStatus")
    public void setApprovalStatus(
        com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.ApprovalStatus approvalStatus) {
        this.approvalStatus = approvalStatus;
    }

    public PresentationApprovalStatus withApprovalStatus(ApprovalStatus approvalStatus) {
        this.approvalStatus = approvalStatus;
        return this;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(approvalStatus).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof PresentationApprovalStatus) == false) {
            return false;
        }
        PresentationApprovalStatus rhs = ((PresentationApprovalStatus) other);
        return new EqualsBuilder().append(approvalStatus, rhs.approvalStatus).isEquals();
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
