
package com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals;

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
 * State of the function group
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
    "name",
    "description",
    "approvalTypeId",
    "validFromDate",
    "validFromTime",
    "validUntilDate",
    "validUntilTime"
})
public class PresentationFunctionGroupState implements AdditionalPropertiesAware
{

    /**
     * Job role name
     * 
     */
    @JsonProperty("name")
    private String name;
    /**
     * Job role description
     * 
     */
    @JsonProperty("description")
    private String description;
    /**
     * Approval type id.
     * 
     */
    @JsonProperty("approvalTypeId")
    private String approvalTypeId;
    /**
     * Start date of the function group.
     * 
     */
    @JsonProperty("validFromDate")
    private String validFromDate;
    /**
     * Start time of the function group.
     * 
     */
    @JsonProperty("validFromTime")
    private String validFromTime;
    /**
     * End date of the function group.
     * 
     */
    @JsonProperty("validUntilDate")
    private String validUntilDate;
    /**
     * End time of the function group.
     * 
     */
    @JsonProperty("validUntilTime")
    private String validUntilTime;
    /**
     * Additional Properties
     * 
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Map<String, String> additions = new HashMap<String, String>();

    /**
     * Job role name
     * 
     * @return
     *     The name
     */
    @JsonProperty("name")
    public String getName() {
        return name;
    }

    /**
     * Job role name
     * 
     * @param name
     *     The name
     */
    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    public PresentationFunctionGroupState withName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Job role description
     * 
     * @return
     *     The description
     */
    @JsonProperty("description")
    public String getDescription() {
        return description;
    }

    /**
     * Job role description
     * 
     * @param description
     *     The description
     */
    @JsonProperty("description")
    public void setDescription(String description) {
        this.description = description;
    }

    public PresentationFunctionGroupState withDescription(String description) {
        this.description = description;
        return this;
    }

    /**
     * Approval type id.
     * 
     * @return
     *     The approvalTypeId
     */
    @JsonProperty("approvalTypeId")
    public String getApprovalTypeId() {
        return approvalTypeId;
    }

    /**
     * Approval type id.
     * 
     * @param approvalTypeId
     *     The approvalTypeId
     */
    @JsonProperty("approvalTypeId")
    public void setApprovalTypeId(String approvalTypeId) {
        this.approvalTypeId = approvalTypeId;
    }

    public PresentationFunctionGroupState withApprovalTypeId(String approvalTypeId) {
        this.approvalTypeId = approvalTypeId;
        return this;
    }

    /**
     * Start date of the function group.
     * 
     * @return
     *     The validFromDate
     */
    @JsonProperty("validFromDate")
    public String getValidFromDate() {
        return validFromDate;
    }

    /**
     * Start date of the function group.
     * 
     * @param validFromDate
     *     The validFromDate
     */
    @JsonProperty("validFromDate")
    public void setValidFromDate(String validFromDate) {
        this.validFromDate = validFromDate;
    }

    public PresentationFunctionGroupState withValidFromDate(String validFromDate) {
        this.validFromDate = validFromDate;
        return this;
    }

    /**
     * Start time of the function group.
     * 
     * @return
     *     The validFromTime
     */
    @JsonProperty("validFromTime")
    public String getValidFromTime() {
        return validFromTime;
    }

    /**
     * Start time of the function group.
     * 
     * @param validFromTime
     *     The validFromTime
     */
    @JsonProperty("validFromTime")
    public void setValidFromTime(String validFromTime) {
        this.validFromTime = validFromTime;
    }

    public PresentationFunctionGroupState withValidFromTime(String validFromTime) {
        this.validFromTime = validFromTime;
        return this;
    }

    /**
     * End date of the function group.
     * 
     * @return
     *     The validUntilDate
     */
    @JsonProperty("validUntilDate")
    public String getValidUntilDate() {
        return validUntilDate;
    }

    /**
     * End date of the function group.
     * 
     * @param validUntilDate
     *     The validUntilDate
     */
    @JsonProperty("validUntilDate")
    public void setValidUntilDate(String validUntilDate) {
        this.validUntilDate = validUntilDate;
    }

    public PresentationFunctionGroupState withValidUntilDate(String validUntilDate) {
        this.validUntilDate = validUntilDate;
        return this;
    }

    /**
     * End time of the function group.
     * 
     * @return
     *     The validUntilTime
     */
    @JsonProperty("validUntilTime")
    public String getValidUntilTime() {
        return validUntilTime;
    }

    /**
     * End time of the function group.
     * 
     * @param validUntilTime
     *     The validUntilTime
     */
    @JsonProperty("validUntilTime")
    public void setValidUntilTime(String validUntilTime) {
        this.validUntilTime = validUntilTime;
    }

    public PresentationFunctionGroupState withValidUntilTime(String validUntilTime) {
        this.validUntilTime = validUntilTime;
        return this;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(name).append(description).append(approvalTypeId).append(validFromDate).append(validFromTime).append(validUntilDate).append(validUntilTime).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof PresentationFunctionGroupState) == false) {
            return false;
        }
        PresentationFunctionGroupState rhs = ((PresentationFunctionGroupState) other);
        return new EqualsBuilder().append(name, rhs.name).append(description, rhs.description).append(approvalTypeId, rhs.approvalTypeId).append(validFromDate, rhs.validFromDate).append(validFromTime, rhs.validFromTime).append(validUntilDate, rhs.validUntilDate).append(validUntilTime, rhs.validUntilTime).isEquals();
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
