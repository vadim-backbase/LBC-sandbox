
package com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Generated;
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
    "creatorUserFullName",
    "creatorUserId",
    "createdAt",
    "action",
    "category",
    "approvalId"
})
@JsonIgnoreProperties(ignoreUnknown = true)
public class PresentationApprovalItem implements AdditionalPropertiesAware
{

    /**
     * Full name of the user who created the approval.
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
    @JsonProperty("createdAt")
    private Date createdAt;
    @JsonProperty("action")
    private PresentationApprovalAction action;
    /**
     * Category of approval
     * 
     */
    @JsonProperty("category")
    private PresentationApprovalCategory category;
    /**
     * Universally Unique Identifier.
     * 
     */
    @JsonProperty("approvalId")
    @Pattern(regexp = "^[0-9a-f]{32}$|^[0-9a-f-]{36}$")
    @Size(min = 1, max = 36)
    private String approvalId;
    /**
     * Additional Properties
     * 
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Map<String, String> additions = new HashMap<String, String>();

    /**
     * Full name of the user who created the approval.
     * 
     * @return
     *     The creatorUserFullName
     */
    @JsonProperty("creatorUserFullName")
    public String getCreatorUserFullName() {
        return creatorUserFullName;
    }

    /**
     * Full name of the user who created the approval.
     * 
     * @param creatorUserFullName
     *     The creatorUserFullName
     */
    @JsonProperty("creatorUserFullName")
    public void setCreatorUserFullName(String creatorUserFullName) {
        this.creatorUserFullName = creatorUserFullName;
    }

    public PresentationApprovalItem withCreatorUserFullName(String creatorUserFullName) {
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

    public PresentationApprovalItem withCreatorUserId(String creatorUserId) {
        this.creatorUserId = creatorUserId;
        return this;
    }

    /**
     * 
     * @return
     *     The createdAt
     */
    @JsonProperty("createdAt")
    public Date getCreatedAt() {
        return createdAt;
    }

    /**
     * 
     * @param createdAt
     *     The createdAt
     */
    @JsonProperty("createdAt")
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public PresentationApprovalItem withCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    /**
     * 
     * @return
     *     The action
     */
    @JsonProperty("action")
    public PresentationApprovalAction getAction() {
        return action;
    }

    /**
     * 
     * @param action
     *     The action
     */
    @JsonProperty("action")
    public void setAction(PresentationApprovalAction action) {
        this.action = action;
    }

    public PresentationApprovalItem withAction(PresentationApprovalAction action) {
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
    public PresentationApprovalCategory getCategory() {
        return category;
    }

    /**
     * Category of approval
     * 
     * @param category
     *     The category
     */
    @JsonProperty("category")
    public void setCategory(PresentationApprovalCategory category) {
        this.category = category;
    }

    public PresentationApprovalItem withCategory(PresentationApprovalCategory category) {
        this.category = category;
        return this;
    }

    /**
     * Universally Unique Identifier.
     * 
     * @return
     *     The approvalId
     */
    @JsonProperty("approvalId")
    public String getApprovalId() {
        return approvalId;
    }

    /**
     * Universally Unique Identifier.
     * 
     * @param approvalId
     *     The approvalId
     */
    @JsonProperty("approvalId")
    public void setApprovalId(String approvalId) {
        this.approvalId = approvalId;
    }

    public PresentationApprovalItem withApprovalId(String approvalId) {
        this.approvalId = approvalId;
        return this;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(creatorUserFullName).append(creatorUserId).append(createdAt).append(action).append(category).append(approvalId).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof PresentationApprovalItem) == false) {
            return false;
        }
        PresentationApprovalItem rhs = ((PresentationApprovalItem) other);
        return new EqualsBuilder().append(creatorUserFullName, rhs.creatorUserFullName).append(creatorUserId, rhs.creatorUserId).append(createdAt, rhs.createdAt).append(action, rhs.action).append(category, rhs.category).append(approvalId, rhs.approvalId).isEquals();
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
