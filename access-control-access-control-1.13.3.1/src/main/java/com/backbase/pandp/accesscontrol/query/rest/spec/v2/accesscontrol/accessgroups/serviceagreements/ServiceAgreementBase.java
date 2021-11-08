
package com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.serviceagreements;

import com.backbase.buildingblocks.persistence.model.AdditionalPropertiesAware;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Generated;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Service agreement item
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
    "id",
    "externalId",
    "name",
    "description",
    "isMaster",
    "validFrom",
    "validUntil"
})
public class ServiceAgreementBase implements AdditionalPropertiesAware
{

    /**
     * Universally Unique Identifier.
     * (Required)
     * 
     */
    @JsonProperty("id")
    @Pattern(regexp = "^[0-9a-f]{32}$|^[0-9a-f-]{36}$")
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
     * Service agreement name
     * (Required)
     * 
     */
    @JsonProperty("name")
    @Size(min = 1)
    @NotNull
    private String name;
    /**
     * Service agreement description
     * (Required)
     * 
     */
    @JsonProperty("description")
    @Size(min = 1)
    @NotNull
    private String description;
    /**
     * Is master service agreement
     * 
     */
    @JsonProperty("isMaster")
    private Boolean isMaster = false;
    /**
     * Start date and time of the service agreement.
     * 
     */
    @JsonProperty("validFrom")
    private Date validFrom;
    /**
     * End date and time of the service agreement.
     * 
     */
    @JsonProperty("validUntil")
    private Date validUntil;
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

    public ServiceAgreementBase withId(String id) {
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

    public ServiceAgreementBase withExternalId(String externalId) {
        this.externalId = externalId;
        return this;
    }

    /**
     * Service agreement name
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
     * Service agreement name
     * (Required)
     * 
     * @param name
     *     The name
     */
    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    public ServiceAgreementBase withName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Service agreement description
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
     * Service agreement description
     * (Required)
     * 
     * @param description
     *     The description
     */
    @JsonProperty("description")
    public void setDescription(String description) {
        this.description = description;
    }

    public ServiceAgreementBase withDescription(String description) {
        this.description = description;
        return this;
    }

    /**
     * Is master service agreement
     * 
     * @return
     *     The isMaster
     */
    @JsonProperty("isMaster")
    public Boolean getIsMaster() {
        return isMaster;
    }

    /**
     * Is master service agreement
     * 
     * @param isMaster
     *     The isMaster
     */
    @JsonProperty("isMaster")
    public void setIsMaster(Boolean isMaster) {
        this.isMaster = isMaster;
    }

    public ServiceAgreementBase withIsMaster(Boolean isMaster) {
        this.isMaster = isMaster;
        return this;
    }

    /**
     * Start date and time of the service agreement.
     * 
     * @return
     *     The validFrom
     */
    @JsonProperty("validFrom")
    public Date getValidFrom() {
        return validFrom;
    }

    /**
     * Start date and time of the service agreement.
     * 
     * @param validFrom
     *     The validFrom
     */
    @JsonProperty("validFrom")
    public void setValidFrom(Date validFrom) {
        this.validFrom = validFrom;
    }

    public ServiceAgreementBase withValidFrom(Date validFrom) {
        this.validFrom = validFrom;
        return this;
    }

    /**
     * End date and time of the service agreement.
     * 
     * @return
     *     The validUntil
     */
    @JsonProperty("validUntil")
    public Date getValidUntil() {
        return validUntil;
    }

    /**
     * End date and time of the service agreement.
     * 
     * @param validUntil
     *     The validUntil
     */
    @JsonProperty("validUntil")
    public void setValidUntil(Date validUntil) {
        this.validUntil = validUntil;
    }

    public ServiceAgreementBase withValidUntil(Date validUntil) {
        this.validUntil = validUntil;
        return this;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(id).append(externalId).append(name).append(description).append(isMaster).append(validFrom).append(validUntil).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ServiceAgreementBase) == false) {
            return false;
        }
        ServiceAgreementBase rhs = ((ServiceAgreementBase) other);
        return new EqualsBuilder().append(id, rhs.id).append(externalId, rhs.externalId).append(name, rhs.name).append(description, rhs.description).append(isMaster, rhs.isMaster).append(validFrom, rhs.validFrom).append(validUntil, rhs.validUntil).isEquals();
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
