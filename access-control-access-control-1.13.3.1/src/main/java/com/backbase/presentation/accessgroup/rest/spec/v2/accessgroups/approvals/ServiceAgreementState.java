
package com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import javax.annotation.Generated;
import javax.validation.Valid;
import com.backbase.buildingblocks.persistence.model.AdditionalPropertiesAware;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * State of the service agreement approval
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
    "name",
    "description",
    "validFromDate",
    "validFromTime",
    "validUntilDate",
    "validUntilTime",
    "legalEntities",
    "admins"
})
public class ServiceAgreementState implements AdditionalPropertiesAware
{

    /**
     * Service agreement name
     * 
     */
    @JsonProperty("name")
    private String name;
    /**
     * Service agreement description
     * 
     */
    @JsonProperty("description")
    private String description;
    /**
     * Start date of the Service agreement.
     * 
     */
    @JsonProperty("validFromDate")
    private String validFromDate;
    /**
     * Start time of the Service agreement.
     * 
     */
    @JsonProperty("validFromTime")
    private String validFromTime;
    /**
     * End date of the Service agreement.
     * 
     */
    @JsonProperty("validUntilDate")
    private String validUntilDate;
    /**
     * End time of the Service agreement.
     * 
     */
    @JsonProperty("validUntilTime")
    private String validUntilTime;
    @JsonProperty("legalEntities")
    @JsonDeserialize(as = LinkedHashSet.class)
    @Valid
    private Set<ServiceAgreementStateLegalEntities> legalEntities = new LinkedHashSet<ServiceAgreementStateLegalEntities>();
    /**
     * List of admin ids.
     * 
     */
    @JsonProperty("admins")
    @JsonDeserialize(as = LinkedHashSet.class)
    @Valid
    private Set<String> admins = new LinkedHashSet<String>();
    /**
     * Additional Properties
     * 
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Map<String, String> additions = new HashMap<String, String>();

    /**
     * Service agreement name
     * 
     * @return
     *     The name
     */
    @JsonProperty("name")
    public String getName() {
        return name;
    }

    /**
     * Service agreement name
     * 
     * @param name
     *     The name
     */
    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    public ServiceAgreementState withName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Service agreement description
     * 
     * @return
     *     The description
     */
    @JsonProperty("description")
    public String getDescription() {
        return description;
    }

    /**
     * Service agreement description
     * 
     * @param description
     *     The description
     */
    @JsonProperty("description")
    public void setDescription(String description) {
        this.description = description;
    }

    public ServiceAgreementState withDescription(String description) {
        this.description = description;
        return this;
    }

    /**
     * Start date of the Service agreement.
     * 
     * @return
     *     The validFromDate
     */
    @JsonProperty("validFromDate")
    public String getValidFromDate() {
        return validFromDate;
    }

    /**
     * Start date of the Service agreement.
     * 
     * @param validFromDate
     *     The validFromDate
     */
    @JsonProperty("validFromDate")
    public void setValidFromDate(String validFromDate) {
        this.validFromDate = validFromDate;
    }

    public ServiceAgreementState withValidFromDate(String validFromDate) {
        this.validFromDate = validFromDate;
        return this;
    }

    /**
     * Start time of the Service agreement.
     * 
     * @return
     *     The validFromTime
     */
    @JsonProperty("validFromTime")
    public String getValidFromTime() {
        return validFromTime;
    }

    /**
     * Start time of the Service agreement.
     * 
     * @param validFromTime
     *     The validFromTime
     */
    @JsonProperty("validFromTime")
    public void setValidFromTime(String validFromTime) {
        this.validFromTime = validFromTime;
    }

    public ServiceAgreementState withValidFromTime(String validFromTime) {
        this.validFromTime = validFromTime;
        return this;
    }

    /**
     * End date of the Service agreement.
     * 
     * @return
     *     The validUntilDate
     */
    @JsonProperty("validUntilDate")
    public String getValidUntilDate() {
        return validUntilDate;
    }

    /**
     * End date of the Service agreement.
     * 
     * @param validUntilDate
     *     The validUntilDate
     */
    @JsonProperty("validUntilDate")
    public void setValidUntilDate(String validUntilDate) {
        this.validUntilDate = validUntilDate;
    }

    public ServiceAgreementState withValidUntilDate(String validUntilDate) {
        this.validUntilDate = validUntilDate;
        return this;
    }

    /**
     * End time of the Service agreement.
     * 
     * @return
     *     The validUntilTime
     */
    @JsonProperty("validUntilTime")
    public String getValidUntilTime() {
        return validUntilTime;
    }

    /**
     * End time of the Service agreement.
     * 
     * @param validUntilTime
     *     The validUntilTime
     */
    @JsonProperty("validUntilTime")
    public void setValidUntilTime(String validUntilTime) {
        this.validUntilTime = validUntilTime;
    }

    public ServiceAgreementState withValidUntilTime(String validUntilTime) {
        this.validUntilTime = validUntilTime;
        return this;
    }

    /**
     * 
     * @return
     *     The legalEntities
     */
    @JsonProperty("legalEntities")
    public Set<ServiceAgreementStateLegalEntities> getLegalEntities() {
        return legalEntities;
    }

    /**
     * 
     * @param legalEntities
     *     The legalEntities
     */
    @JsonProperty("legalEntities")
    public void setLegalEntities(Set<ServiceAgreementStateLegalEntities> legalEntities) {
        this.legalEntities = legalEntities;
    }

    public ServiceAgreementState withLegalEntities(Set<ServiceAgreementStateLegalEntities> legalEntities) {
        this.legalEntities = legalEntities;
        return this;
    }

    /**
     * List of admin ids.
     * 
     * @return
     *     The admins
     */
    @JsonProperty("admins")
    public Set<String> getAdmins() {
        return admins;
    }

    /**
     * List of admin ids.
     * 
     * @param admins
     *     The admins
     */
    @JsonProperty("admins")
    public void setAdmins(Set<String> admins) {
        this.admins = admins;
    }

    public ServiceAgreementState withAdmins(Set<String> admins) {
        this.admins = admins;
        return this;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(name).append(description).append(validFromDate).append(validFromTime).append(validUntilDate).append(validUntilTime).append(legalEntities).append(admins).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ServiceAgreementState) == false) {
            return false;
        }
        ServiceAgreementState rhs = ((ServiceAgreementState) other);
        return new EqualsBuilder().append(name, rhs.name).append(description, rhs.description).append(validFromDate, rhs.validFromDate).append(validFromTime, rhs.validFromTime).append(validUntilDate, rhs.validUntilDate).append(validUntilTime, rhs.validUntilTime).append(legalEntities, rhs.legalEntities).append(admins, rhs.admins).isEquals();
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
