
package com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Generated;
import com.backbase.buildingblocks.persistence.model.AdditionalPropertiesAware;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Approver information.
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
    "approverId",
    "approverFullName",
    "approvedAt"
})
public class PresentationApprovalLogItem implements AdditionalPropertiesAware
{

    /**
     * This is the user id of the approver that approved the action.
     * 
     */
    @JsonProperty("approverId")
    private String approverId;
    /**
     * This is the name of the user that approved the action.
     * 
     */
    @JsonProperty("approverFullName")
    private String approverFullName;
    @JsonProperty("approvedAt")
    private Date approvedAt;
    /**
     * Additional Properties
     * 
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Map<String, String> additions = new HashMap<String, String>();

    /**
     * This is the user id of the approver that approved the action.
     * 
     * @return
     *     The approverId
     */
    @JsonProperty("approverId")
    public String getApproverId() {
        return approverId;
    }

    /**
     * This is the user id of the approver that approved the action.
     * 
     * @param approverId
     *     The approverId
     */
    @JsonProperty("approverId")
    public void setApproverId(String approverId) {
        this.approverId = approverId;
    }

    public PresentationApprovalLogItem withApproverId(String approverId) {
        this.approverId = approverId;
        return this;
    }

    /**
     * This is the name of the user that approved the action.
     * 
     * @return
     *     The approverFullName
     */
    @JsonProperty("approverFullName")
    public String getApproverFullName() {
        return approverFullName;
    }

    /**
     * This is the name of the user that approved the action.
     * 
     * @param approverFullName
     *     The approverFullName
     */
    @JsonProperty("approverFullName")
    public void setApproverFullName(String approverFullName) {
        this.approverFullName = approverFullName;
    }

    public PresentationApprovalLogItem withApproverFullName(String approverFullName) {
        this.approverFullName = approverFullName;
        return this;
    }

    /**
     * 
     * @return
     *     The approvedAt
     */
    @JsonProperty("approvedAt")
    public Date getApprovedAt() {
        return approvedAt;
    }

    /**
     * 
     * @param approvedAt
     *     The approvedAt
     */
    @JsonProperty("approvedAt")
    public void setApprovedAt(Date approvedAt) {
        this.approvedAt = approvedAt;
    }

    public PresentationApprovalLogItem withApprovedAt(Date approvedAt) {
        this.approvedAt = approvedAt;
        return this;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(approverId).append(approverFullName).append(approvedAt).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof PresentationApprovalLogItem) == false) {
            return false;
        }
        PresentationApprovalLogItem rhs = ((PresentationApprovalLogItem) other);
        return new EqualsBuilder().append(approverId, rhs.approverId).append(approverFullName, rhs.approverFullName).append(approvedAt, rhs.approvedAt).isEquals();
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
