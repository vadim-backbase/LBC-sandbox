
package com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    "functionGroupId",
    "serviceAgreementId",
    "serviceAgreementName",
    "approvalId",
    "action",
    "oldState",
    "newState",
    "permissionMatrix"
})
@JsonIgnoreProperties(ignoreUnknown = true)
public class PresentationFunctionGroupApprovalDetailsItem implements AdditionalPropertiesAware
{

    /**
     * Universally Unique Identifier.
     * 
     */
    @JsonProperty("functionGroupId")
    @Pattern(regexp = "^[0-9a-f]{32}$|^[0-9a-f-]{36}$")
    @Size(min = 1, max = 36)
    private String functionGroupId;
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
     * Approval id
     * 
     */
    @JsonProperty("approvalId")
    private String approvalId;
    @JsonProperty("action")
    private com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.PresentationApprovalAction action;
    /**
     * State of the function group
     * 
     */
    @JsonProperty("oldState")
    @Valid
    private com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.PresentationFunctionGroupState oldState;
    /**
     * State of the function group
     * 
     */
    @JsonProperty("newState")
    @Valid
    private com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.PresentationFunctionGroupState newState;
    @JsonProperty("permissionMatrix")
    @Valid
    private List<com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.PermissionMatrix> permissionMatrix = new ArrayList<com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.PermissionMatrix>();
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
     *     The functionGroupId
     */
    @JsonProperty("functionGroupId")
    public String getFunctionGroupId() {
        return functionGroupId;
    }

    /**
     * Universally Unique Identifier.
     * 
     * @param functionGroupId
     *     The functionGroupId
     */
    @JsonProperty("functionGroupId")
    public void setFunctionGroupId(String functionGroupId) {
        this.functionGroupId = functionGroupId;
    }

    public PresentationFunctionGroupApprovalDetailsItem withFunctionGroupId(String functionGroupId) {
        this.functionGroupId = functionGroupId;
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

    public PresentationFunctionGroupApprovalDetailsItem withServiceAgreementId(String serviceAgreementId) {
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

    public PresentationFunctionGroupApprovalDetailsItem withServiceAgreementName(String serviceAgreementName) {
        this.serviceAgreementName = serviceAgreementName;
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

    public PresentationFunctionGroupApprovalDetailsItem withApprovalId(String approvalId) {
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

    public PresentationFunctionGroupApprovalDetailsItem withAction(PresentationApprovalAction action) {
        this.action = action;
        return this;
    }

    /**
     * State of the function group
     * 
     * @return
     *     The oldState
     */
    @JsonProperty("oldState")
    public com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.PresentationFunctionGroupState getOldState() {
        return oldState;
    }

    /**
     * State of the function group
     * 
     * @param oldState
     *     The oldState
     */
    @JsonProperty("oldState")
    public void setOldState(
        com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.PresentationFunctionGroupState oldState) {
        this.oldState = oldState;
    }

    public PresentationFunctionGroupApprovalDetailsItem withOldState(
        com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.PresentationFunctionGroupState oldState) {
        this.oldState = oldState;
        return this;
    }

    /**
     * State of the function group
     * 
     * @return
     *     The newState
     */
    @JsonProperty("newState")
    public com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.PresentationFunctionGroupState getNewState() {
        return newState;
    }

    /**
     * State of the function group
     * 
     * @param newState
     *     The newState
     */
    @JsonProperty("newState")
    public void setNewState(
        com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.PresentationFunctionGroupState newState) {
        this.newState = newState;
    }

    public PresentationFunctionGroupApprovalDetailsItem withNewState(PresentationFunctionGroupState newState) {
        this.newState = newState;
        return this;
    }

    /**
     * 
     * @return
     *     The permissionMatrix
     */
    @JsonProperty("permissionMatrix")
    public List<com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.PermissionMatrix> getPermissionMatrix() {
        return permissionMatrix;
    }

    /**
     * 
     * @param permissionMatrix
     *     The permissionMatrix
     */
    @JsonProperty("permissionMatrix")
    public void setPermissionMatrix(List<com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.PermissionMatrix> permissionMatrix) {
        this.permissionMatrix = permissionMatrix;
    }

    public PresentationFunctionGroupApprovalDetailsItem withPermissionMatrix(List<PermissionMatrix> permissionMatrix) {
        this.permissionMatrix = permissionMatrix;
        return this;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(functionGroupId).append(serviceAgreementId).append(serviceAgreementName).append(approvalId).append(action).append(oldState).append(newState).append(permissionMatrix).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof PresentationFunctionGroupApprovalDetailsItem) == false) {
            return false;
        }
        PresentationFunctionGroupApprovalDetailsItem rhs = ((PresentationFunctionGroupApprovalDetailsItem) other);
        return new EqualsBuilder().append(functionGroupId, rhs.functionGroupId).append(serviceAgreementId, rhs.serviceAgreementId).append(serviceAgreementName, rhs.serviceAgreementName).append(approvalId, rhs.approvalId).append(action, rhs.action).append(oldState, rhs.oldState).append(newState, rhs.newState).append(permissionMatrix, rhs.permissionMatrix).isEquals();
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
