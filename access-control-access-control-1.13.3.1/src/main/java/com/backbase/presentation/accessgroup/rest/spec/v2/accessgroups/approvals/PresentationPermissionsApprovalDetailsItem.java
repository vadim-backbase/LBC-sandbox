
package com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals;

import java.util.ArrayList;
import java.util.Date;
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
    "approvalId",
    "creatorUserFullName",
    "creatorUserId",
    "createdAt",
    "userId",
    "userFullName",
    "serviceAgreementId",
    "serviceAgreementName",
    "serviceAgreementDescription",
    "action",
    "category",
    "newFunctionGroups",
    "removedFunctionGroups",
    "modifiedFunctionGroups",
    "unmodifiedFunctionGroups",
    "requiredApproves",
    "completedApproves",
    "approvalLog"
})
@JsonIgnoreProperties(ignoreUnknown = true)
public class PresentationPermissionsApprovalDetailsItem implements AdditionalPropertiesAware
{

    /**
     * ID of the approval.
     * 
     */
    @JsonProperty("approvalId")
    @Size(min = 1, max = 36)
    private String approvalId;
    /**
     * Full name of the user who created the approval. Deprecated since 2.18.0, planned for removal in 2.19.0. This property is no longer used and will be ignored.
     * 
     */
    @JsonProperty("creatorUserFullName")
    @Size(min = 1, max = 255)
    private String creatorUserFullName;
    /**
     * Universally Unique Identifier.
     * 
     */
    @JsonProperty("creatorUserId")
    @Pattern(regexp = "^[0-9a-f]{32}$|^[0-9a-f-]{36}$")
    @Size(min = 1, max = 36)
    private String creatorUserId;
    /**
     * Deprecated since 2.18.0, planned for removal in 2.19.0. This property is no longer used and will be ignored.
     * 
     */
    @JsonProperty("createdAt")
    private Date createdAt;
    /**
     * Universally Unique Identifier.
     * 
     */
    @JsonProperty("userId")
    @Pattern(regexp = "^[0-9a-f]{32}$|^[0-9a-f-]{36}$")
    @Size(min = 1, max = 36)
    private String userId;
    /**
     * User full name to whom the permissions are assigned
     * 
     */
    @JsonProperty("userFullName")
    @Size(min = 1, max = 255)
    private String userFullName;
    /**
     * Universally Unique Identifier.
     * 
     */
    @JsonProperty("serviceAgreementId")
    @Pattern(regexp = "^[0-9a-f]{32}$|^[0-9a-f-]{36}$")
    @Size(min = 1, max = 36)
    private String serviceAgreementId;
    /**
     * Service Agreement name.
     * 
     */
    @JsonProperty("serviceAgreementName")
    private String serviceAgreementName;
    /**
     * Service Agreement descriptioin.
     * 
     */
    @JsonProperty("serviceAgreementDescription")
    @Size(min = 1, max = 255)
    private String serviceAgreementDescription;
    @JsonProperty("action")
    private com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.PresentationApprovalAction action;
    /**
     * Category of approval
     * 
     */
    @JsonProperty("category")
    private com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.PresentationApprovalCategory category;
    @JsonProperty("newFunctionGroups")
    @Valid
    private List<com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.PresentationFunctionGroupsDataGroupsPair> newFunctionGroups = new ArrayList<com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.PresentationFunctionGroupsDataGroupsPair>();
    @JsonProperty("removedFunctionGroups")
    @Valid
    private List<com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.PresentationFunctionGroupsDataGroupsPair> removedFunctionGroups = new ArrayList<com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.PresentationFunctionGroupsDataGroupsPair>();
    @JsonProperty("modifiedFunctionGroups")
    @Valid
    private List<PresentationFunctionGroupsDataGroupsExtendedPair> modifiedFunctionGroups = new ArrayList<PresentationFunctionGroupsDataGroupsExtendedPair>();
    @JsonProperty("unmodifiedFunctionGroups")
    @Valid
    private List<com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.PresentationFunctionGroupsDataGroupsPair> unmodifiedFunctionGroups = new ArrayList<com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.PresentationFunctionGroupsDataGroupsPair>();
    /**
     * Number of required approves for the action to be successful.
     * 
     */
    @JsonProperty("requiredApproves")
    private Integer requiredApproves;
    /**
     * Number of approves that the action has.
     * 
     */
    @JsonProperty("completedApproves")
    private Integer completedApproves;
    /**
     * Deprecated since 2.18.0, planned for removal in 2.19.0. This property is no longer used and will be ignored.
     * 
     */
    @JsonProperty("approvalLog")
    @Valid
    private List<com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.PresentationApprovalLogItem> approvalLog = new ArrayList<com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.PresentationApprovalLogItem>();
    /**
     * Additional Properties
     * 
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Map<String, String> additions = new HashMap<String, String>();

    /**
     * ID of the approval.
     * 
     * @return
     *     The approvalId
     */
    @JsonProperty("approvalId")
    public String getApprovalId() {
        return approvalId;
    }

    /**
     * ID of the approval.
     * 
     * @param approvalId
     *     The approvalId
     */
    @JsonProperty("approvalId")
    public void setApprovalId(String approvalId) {
        this.approvalId = approvalId;
    }

    public PresentationPermissionsApprovalDetailsItem withApprovalId(String approvalId) {
        this.approvalId = approvalId;
        return this;
    }

    /**
     * Full name of the user who created the approval. Deprecated since 2.18.0, planned for removal in 2.19.0. This property is no longer used and will be ignored.
     * 
     * @return
     *     The creatorUserFullName
     */
    @JsonProperty("creatorUserFullName")
    public String getCreatorUserFullName() {
        return creatorUserFullName;
    }

    /**
     * Full name of the user who created the approval. Deprecated since 2.18.0, planned for removal in 2.19.0. This property is no longer used and will be ignored.
     * 
     * @param creatorUserFullName
     *     The creatorUserFullName
     */
    @JsonProperty("creatorUserFullName")
    public void setCreatorUserFullName(String creatorUserFullName) {
        this.creatorUserFullName = creatorUserFullName;
    }

    public PresentationPermissionsApprovalDetailsItem withCreatorUserFullName(String creatorUserFullName) {
        this.creatorUserFullName = creatorUserFullName;
        return this;
    }

    /**
     * Universally Unique Identifier.
     * 
     * @return
     *     The creatorUserId
     */
    @JsonProperty("creatorUserId")
    public String getCreatorUserId() {
        return creatorUserId;
    }

    /**
     * Universally Unique Identifier.
     * 
     * @param creatorUserId
     *     The creatorUserId
     */
    @JsonProperty("creatorUserId")
    public void setCreatorUserId(String creatorUserId) {
        this.creatorUserId = creatorUserId;
    }

    public PresentationPermissionsApprovalDetailsItem withCreatorUserId(String creatorUserId) {
        this.creatorUserId = creatorUserId;
        return this;
    }

    /**
     * Deprecated since 2.18.0, planned for removal in 2.19.0. This property is no longer used and will be ignored.
     * 
     * @return
     *     The createdAt
     */
    @JsonProperty("createdAt")
    public Date getCreatedAt() {
        return createdAt;
    }

    /**
     * Deprecated since 2.18.0, planned for removal in 2.19.0. This property is no longer used and will be ignored.
     * 
     * @param createdAt
     *     The createdAt
     */
    @JsonProperty("createdAt")
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public PresentationPermissionsApprovalDetailsItem withCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    /**
     * Universally Unique Identifier.
     * 
     * @return
     *     The userId
     */
    @JsonProperty("userId")
    public String getUserId() {
        return userId;
    }

    /**
     * Universally Unique Identifier.
     * 
     * @param userId
     *     The userId
     */
    @JsonProperty("userId")
    public void setUserId(String userId) {
        this.userId = userId;
    }

    public PresentationPermissionsApprovalDetailsItem withUserId(String userId) {
        this.userId = userId;
        return this;
    }

    /**
     * User full name to whom the permissions are assigned
     * 
     * @return
     *     The userFullName
     */
    @JsonProperty("userFullName")
    public String getUserFullName() {
        return userFullName;
    }

    /**
     * User full name to whom the permissions are assigned
     * 
     * @param userFullName
     *     The userFullName
     */
    @JsonProperty("userFullName")
    public void setUserFullName(String userFullName) {
        this.userFullName = userFullName;
    }

    public PresentationPermissionsApprovalDetailsItem withUserFullName(String userFullName) {
        this.userFullName = userFullName;
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

    public PresentationPermissionsApprovalDetailsItem withServiceAgreementId(String serviceAgreementId) {
        this.serviceAgreementId = serviceAgreementId;
        return this;
    }

    /**
     * Service Agreement name.
     * 
     * @return
     *     The serviceAgreementName
     */
    @JsonProperty("serviceAgreementName")
    public String getServiceAgreementName() {
        return serviceAgreementName;
    }

    /**
     * Service Agreement name.
     * 
     * @param serviceAgreementName
     *     The serviceAgreementName
     */
    @JsonProperty("serviceAgreementName")
    public void setServiceAgreementName(String serviceAgreementName) {
        this.serviceAgreementName = serviceAgreementName;
    }

    public PresentationPermissionsApprovalDetailsItem withServiceAgreementName(String serviceAgreementName) {
        this.serviceAgreementName = serviceAgreementName;
        return this;
    }

    /**
     * Service Agreement descriptioin.
     * 
     * @return
     *     The serviceAgreementDescription
     */
    @JsonProperty("serviceAgreementDescription")
    public String getServiceAgreementDescription() {
        return serviceAgreementDescription;
    }

    /**
     * Service Agreement descriptioin.
     * 
     * @param serviceAgreementDescription
     *     The serviceAgreementDescription
     */
    @JsonProperty("serviceAgreementDescription")
    public void setServiceAgreementDescription(String serviceAgreementDescription) {
        this.serviceAgreementDescription = serviceAgreementDescription;
    }

    public PresentationPermissionsApprovalDetailsItem withServiceAgreementDescription(String serviceAgreementDescription) {
        this.serviceAgreementDescription = serviceAgreementDescription;
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

    public PresentationPermissionsApprovalDetailsItem withAction(PresentationApprovalAction action) {
        this.action = action;
        return this;
    }

    /**
     * Category of approval
     * 
     * @return
     *     The category
     */
    @JsonProperty("category")
    public com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.PresentationApprovalCategory getCategory() {
        return category;
    }

    /**
     * Category of approval
     * 
     * @param category
     *     The category
     */
    @JsonProperty("category")
    public void setCategory(
        com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.PresentationApprovalCategory category) {
        this.category = category;
    }

    public PresentationPermissionsApprovalDetailsItem withCategory(PresentationApprovalCategory category) {
        this.category = category;
        return this;
    }

    /**
     * 
     * @return
     *     The newFunctionGroups
     */
    @JsonProperty("newFunctionGroups")
    public List<com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.PresentationFunctionGroupsDataGroupsPair> getNewFunctionGroups() {
        return newFunctionGroups;
    }

    /**
     * 
     * @param newFunctionGroups
     *     The newFunctionGroups
     */
    @JsonProperty("newFunctionGroups")
    public void setNewFunctionGroups(List<com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.PresentationFunctionGroupsDataGroupsPair> newFunctionGroups) {
        this.newFunctionGroups = newFunctionGroups;
    }

    public PresentationPermissionsApprovalDetailsItem withNewFunctionGroups(List<com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.PresentationFunctionGroupsDataGroupsPair> newFunctionGroups) {
        this.newFunctionGroups = newFunctionGroups;
        return this;
    }

    /**
     * 
     * @return
     *     The removedFunctionGroups
     */
    @JsonProperty("removedFunctionGroups")
    public List<com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.PresentationFunctionGroupsDataGroupsPair> getRemovedFunctionGroups() {
        return removedFunctionGroups;
    }

    /**
     * 
     * @param removedFunctionGroups
     *     The removedFunctionGroups
     */
    @JsonProperty("removedFunctionGroups")
    public void setRemovedFunctionGroups(List<com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.PresentationFunctionGroupsDataGroupsPair> removedFunctionGroups) {
        this.removedFunctionGroups = removedFunctionGroups;
    }

    public PresentationPermissionsApprovalDetailsItem withRemovedFunctionGroups(List<com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.PresentationFunctionGroupsDataGroupsPair> removedFunctionGroups) {
        this.removedFunctionGroups = removedFunctionGroups;
        return this;
    }

    /**
     * 
     * @return
     *     The modifiedFunctionGroups
     */
    @JsonProperty("modifiedFunctionGroups")
    public List<PresentationFunctionGroupsDataGroupsExtendedPair> getModifiedFunctionGroups() {
        return modifiedFunctionGroups;
    }

    /**
     * 
     * @param modifiedFunctionGroups
     *     The modifiedFunctionGroups
     */
    @JsonProperty("modifiedFunctionGroups")
    public void setModifiedFunctionGroups(List<PresentationFunctionGroupsDataGroupsExtendedPair> modifiedFunctionGroups) {
        this.modifiedFunctionGroups = modifiedFunctionGroups;
    }

    public PresentationPermissionsApprovalDetailsItem withModifiedFunctionGroups(List<PresentationFunctionGroupsDataGroupsExtendedPair> modifiedFunctionGroups) {
        this.modifiedFunctionGroups = modifiedFunctionGroups;
        return this;
    }

    /**
     * 
     * @return
     *     The unmodifiedFunctionGroups
     */
    @JsonProperty("unmodifiedFunctionGroups")
    public List<com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.PresentationFunctionGroupsDataGroupsPair> getUnmodifiedFunctionGroups() {
        return unmodifiedFunctionGroups;
    }

    /**
     * 
     * @param unmodifiedFunctionGroups
     *     The unmodifiedFunctionGroups
     */
    @JsonProperty("unmodifiedFunctionGroups")
    public void setUnmodifiedFunctionGroups(List<com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.PresentationFunctionGroupsDataGroupsPair> unmodifiedFunctionGroups) {
        this.unmodifiedFunctionGroups = unmodifiedFunctionGroups;
    }

    public PresentationPermissionsApprovalDetailsItem withUnmodifiedFunctionGroups(List<PresentationFunctionGroupsDataGroupsPair> unmodifiedFunctionGroups) {
        this.unmodifiedFunctionGroups = unmodifiedFunctionGroups;
        return this;
    }

    /**
     * Number of required approves for the action to be successful.
     * 
     * @return
     *     The requiredApproves
     */
    @JsonProperty("requiredApproves")
    public Integer getRequiredApproves() {
        return requiredApproves;
    }

    /**
     * Number of required approves for the action to be successful.
     * 
     * @param requiredApproves
     *     The requiredApproves
     */
    @JsonProperty("requiredApproves")
    public void setRequiredApproves(Integer requiredApproves) {
        this.requiredApproves = requiredApproves;
    }

    public PresentationPermissionsApprovalDetailsItem withRequiredApproves(Integer requiredApproves) {
        this.requiredApproves = requiredApproves;
        return this;
    }

    /**
     * Number of approves that the action has.
     * 
     * @return
     *     The completedApproves
     */
    @JsonProperty("completedApproves")
    public Integer getCompletedApproves() {
        return completedApproves;
    }

    /**
     * Number of approves that the action has.
     * 
     * @param completedApproves
     *     The completedApproves
     */
    @JsonProperty("completedApproves")
    public void setCompletedApproves(Integer completedApproves) {
        this.completedApproves = completedApproves;
    }

    public PresentationPermissionsApprovalDetailsItem withCompletedApproves(Integer completedApproves) {
        this.completedApproves = completedApproves;
        return this;
    }

    /**
     * Deprecated since 2.18.0, planned for removal in 2.19.0. This property is no longer used and will be ignored.
     * 
     * @return
     *     The approvalLog
     */
    @JsonProperty("approvalLog")
    public List<com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.PresentationApprovalLogItem> getApprovalLog() {
        return approvalLog;
    }

    /**
     * Deprecated since 2.18.0, planned for removal in 2.19.0. This property is no longer used and will be ignored.
     * 
     * @param approvalLog
     *     The approvalLog
     */
    @JsonProperty("approvalLog")
    public void setApprovalLog(List<com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.PresentationApprovalLogItem> approvalLog) {
        this.approvalLog = approvalLog;
    }

    public PresentationPermissionsApprovalDetailsItem withApprovalLog(List<PresentationApprovalLogItem> approvalLog) {
        this.approvalLog = approvalLog;
        return this;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(approvalId).append(creatorUserFullName).append(creatorUserId).append(createdAt).append(userId).append(userFullName).append(serviceAgreementId).append(serviceAgreementName).append(serviceAgreementDescription).append(action).append(category).append(newFunctionGroups).append(removedFunctionGroups).append(modifiedFunctionGroups).append(unmodifiedFunctionGroups).append(requiredApproves).append(completedApproves).append(approvalLog).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof PresentationPermissionsApprovalDetailsItem) == false) {
            return false;
        }
        PresentationPermissionsApprovalDetailsItem rhs = ((PresentationPermissionsApprovalDetailsItem) other);
        return new EqualsBuilder().append(approvalId, rhs.approvalId).append(creatorUserFullName, rhs.creatorUserFullName).append(creatorUserId, rhs.creatorUserId).append(createdAt, rhs.createdAt).append(userId, rhs.userId).append(userFullName, rhs.userFullName).append(serviceAgreementId, rhs.serviceAgreementId).append(serviceAgreementName, rhs.serviceAgreementName).append(serviceAgreementDescription, rhs.serviceAgreementDescription).append(action, rhs.action).append(category, rhs.category).append(newFunctionGroups, rhs.newFunctionGroups).append(removedFunctionGroups, rhs.removedFunctionGroups).append(modifiedFunctionGroups, rhs.modifiedFunctionGroups).append(unmodifiedFunctionGroups, rhs.unmodifiedFunctionGroups).append(requiredApproves, rhs.requiredApproves).append(completedApproves, rhs.completedApproves).append(approvalLog, rhs.approvalLog).isEquals();
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
