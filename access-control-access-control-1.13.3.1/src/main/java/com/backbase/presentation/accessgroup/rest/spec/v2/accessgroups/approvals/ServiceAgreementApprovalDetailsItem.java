
package com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Generated;
import javax.validation.Valid;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import com.backbase.buildingblocks.persistence.model.AdditionalPropertiesAware;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
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
    "serviceAgreementId",
    "approvalId",
    "action",
    "oldState",
    "newState"
})
@JsonIgnoreProperties(ignoreUnknown = true)
public class ServiceAgreementApprovalDetailsItem implements AdditionalPropertiesAware
{

    /**
     * Universally Unique Identifier.
     * 
     */
    @JsonProperty("serviceAgreementId")
    @Pattern(regexp = "^[0-9a-f]{32}$|^[0-9a-f-]{36}$")
    @Size(min = 1, max = 36)
    private String serviceAgreementId;
    /**
     * Approval id
     * 
     */
    @JsonProperty("approvalId")
    private String approvalId;
    @JsonProperty("action")
    private com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.PresentationApprovalAction action;
    /**
     * State of the service agreement approval
     * 
     */
    @JsonProperty("oldState")
    @Valid
    private com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.ServiceAgreementState oldState;
    /**
     * State of the service agreement approval
     * 
     */
    @JsonProperty("newState")
    @Valid
    private com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.ServiceAgreementState newState;
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

    public ServiceAgreementApprovalDetailsItem withServiceAgreementId(String serviceAgreementId) {
        this.serviceAgreementId = serviceAgreementId;
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

    public ServiceAgreementApprovalDetailsItem withApprovalId(String approvalId) {
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

    public ServiceAgreementApprovalDetailsItem withAction(PresentationApprovalAction action) {
        this.action = action;
        return this;
    }

    /**
     * State of the service agreement approval
     * 
     * @return
     *     The oldState
     */
    @JsonProperty("oldState")
    public com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.ServiceAgreementState getOldState() {
        return oldState;
    }

    /**
     * State of the service agreement approval
     * 
     * @param oldState
     *     The oldState
     */
    @JsonProperty("oldState")
    public void setOldState(
        com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.ServiceAgreementState oldState) {
        this.oldState = oldState;
    }

    public ServiceAgreementApprovalDetailsItem withOldState(
        com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.ServiceAgreementState oldState) {
        this.oldState = oldState;
        return this;
    }

    /**
     * State of the service agreement approval
     * 
     * @return
     *     The newState
     */
    @JsonProperty("newState")
    public com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.ServiceAgreementState getNewState() {
        return newState;
    }

    /**
     * State of the service agreement approval
     * 
     * @param newState
     *     The newState
     */
    @JsonProperty("newState")
    public void setNewState(
        com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.ServiceAgreementState newState) {
        this.newState = newState;
    }

    public ServiceAgreementApprovalDetailsItem withNewState(ServiceAgreementState newState) {
        this.newState = newState;
        return this;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(serviceAgreementId).append(approvalId).append(action).append(oldState).append(newState).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ServiceAgreementApprovalDetailsItem) == false) {
            return false;
        }
        ServiceAgreementApprovalDetailsItem rhs = ((ServiceAgreementApprovalDetailsItem) other);
        return new EqualsBuilder().append(serviceAgreementId, rhs.serviceAgreementId).append(approvalId, rhs.approvalId).append(action, rhs.action).append(oldState, rhs.oldState).append(newState, rhs.newState).isEquals();
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
