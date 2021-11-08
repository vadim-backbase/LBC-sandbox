
package com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Generated;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import com.backbase.buildingblocks.persistence.model.AdditionalPropertiesAware;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
    "id",
    "externalId",
    "name",
    "description",
    "isMaster",
    "validFromDate",
    "validFromTime",
    "validUntilDate",
    "validUntilTime"
})
public class ServiceAgreementExternalIdGetResponseBodyParent implements AdditionalPropertiesAware
{

    /**
     * Universally Unique Identifier.
     * (Required)
     * 
     */
    @JsonProperty("id")
    @Pattern(regexp = "^[0-9a-f]{32}$|^[0-9a-f-]{36}$")
    @Size(min = 1, max = 36)
    @NotNull
    private String id;
    /**
     * External Unique Identifier.
     * 
     */
    @JsonProperty("externalId")
    @Pattern(regexp = "^[^\\r\\n]{1,64}$")
    @Size(min = 1, max = 64)
    private String externalId;
    /**
     * Service agreement name to display
     * (Required)
     * 
     */
    @JsonProperty("name")
    @Size(min = 1, max = 128)
    @NotNull
    private String name;
    /**
     * Brief text to describe service agreement
     * (Required)
     * 
     */
    @JsonProperty("description")
    @Size(min = 1, max = 255)
    @NotNull
    private String description;
    /**
     * Defines default service agreement when none selected, only one can hold true value
     * (Required)
     * 
     */
    @JsonProperty("isMaster")
    @NotNull
    private Boolean isMaster = false;
    /**
     * Start date of the service agreement. (date-only from RFC3339 - yyyy-MM-dd)
     * 
     */
    @JsonProperty("validFromDate")
    private String validFromDate;
    /**
     * Start time of the service agreement. (time-only from RFC3339 - HH:mm:ss)
     * 
     */
    @JsonProperty("validFromTime")
    private String validFromTime;
    /**
     * End date of the service agreement. (date-only from RFC3339 - yyyy-MM-dd)
     * 
     */
    @JsonProperty("validUntilDate")
    private String validUntilDate;
    /**
     * End time of the service agreement. (time-only from RFC3339 - HH:mm:ss)
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
     * Universally Unique Identifier.
     * (Required)
     * 
     * @return
     *     The id
     */
    @JsonProperty("id")
    public String getId() {
        return id;
    }

    /**
     * Universally Unique Identifier.
     * (Required)
     * 
     * @param id
     *     The id
     */
    @JsonProperty("id")
    public void setId(String id) {
        this.id = id;
    }

    public ServiceAgreementExternalIdGetResponseBodyParent withId(String id) {
        this.id = id;
        return this;
    }

    /**
     * External Unique Identifier.
     * 
     * @return
     *     The externalId
     */
    @JsonProperty("externalId")
    public String getExternalId() {
        return externalId;
    }

    /**
     * External Unique Identifier.
     * 
     * @param externalId
     *     The externalId
     */
    @JsonProperty("externalId")
    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public ServiceAgreementExternalIdGetResponseBodyParent withExternalId(String externalId) {
        this.externalId = externalId;
        return this;
    }

    /**
     * Service agreement name to display
     * (Required)
     * 
     * @return
     *     The name
     */
    @JsonProperty("name")
    public String getName() {
        return name;
    }

    /**
     * Service agreement name to display
     * (Required)
     * 
     * @param name
     *     The name
     */
    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    public ServiceAgreementExternalIdGetResponseBodyParent withName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Brief text to describe service agreement
     * (Required)
     * 
     * @return
     *     The description
     */
    @JsonProperty("description")
    public String getDescription() {
        return description;
    }

    /**
     * Brief text to describe service agreement
     * (Required)
     * 
     * @param description
     *     The description
     */
    @JsonProperty("description")
    public void setDescription(String description) {
        this.description = description;
    }

    public ServiceAgreementExternalIdGetResponseBodyParent withDescription(String description) {
        this.description = description;
        return this;
    }

    /**
     * Defines default service agreement when none selected, only one can hold true value
     * (Required)
     * 
     * @return
     *     The isMaster
     */
    @JsonProperty("isMaster")
    public Boolean getIsMaster() {
        return isMaster;
    }

    /**
     * Defines default service agreement when none selected, only one can hold true value
     * (Required)
     * 
     * @param isMaster
     *     The isMaster
     */
    @JsonProperty("isMaster")
    public void setIsMaster(Boolean isMaster) {
        this.isMaster = isMaster;
    }

    public ServiceAgreementExternalIdGetResponseBodyParent withIsMaster(Boolean isMaster) {
        this.isMaster = isMaster;
        return this;
    }

    /**
     * Start date of the service agreement. (date-only from RFC3339 - yyyy-MM-dd)
     * 
     * @return
     *     The validFromDate
     */
    @JsonProperty("validFromDate")
    public String getValidFromDate() {
        return validFromDate;
    }

    /**
     * Start date of the service agreement. (date-only from RFC3339 - yyyy-MM-dd)
     * 
     * @param validFromDate
     *     The validFromDate
     */
    @JsonProperty("validFromDate")
    public void setValidFromDate(String validFromDate) {
        this.validFromDate = validFromDate;
    }

    public ServiceAgreementExternalIdGetResponseBodyParent withValidFromDate(String validFromDate) {
        this.validFromDate = validFromDate;
        return this;
    }

    /**
     * Start time of the service agreement. (time-only from RFC3339 - HH:mm:ss)
     * 
     * @return
     *     The validFromTime
     */
    @JsonProperty("validFromTime")
    public String getValidFromTime() {
        return validFromTime;
    }

    /**
     * Start time of the service agreement. (time-only from RFC3339 - HH:mm:ss)
     * 
     * @param validFromTime
     *     The validFromTime
     */
    @JsonProperty("validFromTime")
    public void setValidFromTime(String validFromTime) {
        this.validFromTime = validFromTime;
    }

    public ServiceAgreementExternalIdGetResponseBodyParent withValidFromTime(String validFromTime) {
        this.validFromTime = validFromTime;
        return this;
    }

    /**
     * End date of the service agreement. (date-only from RFC3339 - yyyy-MM-dd)
     * 
     * @return
     *     The validUntilDate
     */
    @JsonProperty("validUntilDate")
    public String getValidUntilDate() {
        return validUntilDate;
    }

    /**
     * End date of the service agreement. (date-only from RFC3339 - yyyy-MM-dd)
     * 
     * @param validUntilDate
     *     The validUntilDate
     */
    @JsonProperty("validUntilDate")
    public void setValidUntilDate(String validUntilDate) {
        this.validUntilDate = validUntilDate;
    }

    public ServiceAgreementExternalIdGetResponseBodyParent withValidUntilDate(String validUntilDate) {
        this.validUntilDate = validUntilDate;
        return this;
    }

    /**
     * End time of the service agreement. (time-only from RFC3339 - HH:mm:ss)
     * 
     * @return
     *     The validUntilTime
     */
    @JsonProperty("validUntilTime")
    public String getValidUntilTime() {
        return validUntilTime;
    }

    /**
     * End time of the service agreement. (time-only from RFC3339 - HH:mm:ss)
     * 
     * @param validUntilTime
     *     The validUntilTime
     */
    @JsonProperty("validUntilTime")
    public void setValidUntilTime(String validUntilTime) {
        this.validUntilTime = validUntilTime;
    }

    public ServiceAgreementExternalIdGetResponseBodyParent withValidUntilTime(String validUntilTime) {
        this.validUntilTime = validUntilTime;
        return this;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(id).append(externalId).append(name).append(description).append(isMaster).append(validFromDate).append(validFromTime).append(validUntilDate).append(validUntilTime).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ServiceAgreementExternalIdGetResponseBodyParent) == false) {
            return false;
        }
        ServiceAgreementExternalIdGetResponseBodyParent rhs = ((ServiceAgreementExternalIdGetResponseBodyParent) other);
        return new EqualsBuilder().append(id, rhs.id).append(externalId, rhs.externalId).append(name, rhs.name).append(description, rhs.description).append(isMaster, rhs.isMaster).append(validFromDate, rhs.validFromDate).append(validFromTime, rhs.validFromTime).append(validUntilDate, rhs.validUntilDate).append(validUntilTime, rhs.validUntilTime).isEquals();
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
