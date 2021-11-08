
package com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.usercontext;

import com.backbase.buildingblocks.persistence.model.AdditionalPropertiesAware;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
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
 * Context Service Agreement item
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
    "serviceAgreementName",
    "serviceAgreementId",
    "serviceAgreementMaster",
    "externalId",
    "description"
})
public class Element implements AdditionalPropertiesAware
{

    /**
     * Service Agreement name
     * (Required)
     * 
     */
    @JsonProperty("serviceAgreementName")
    @NotNull
    private String serviceAgreementName;
    /**
     * Universally Unique Identifier.
     * (Required)
     * 
     */
    @JsonProperty("serviceAgreementId")
    @Pattern(regexp = "^[0-9a-f]{32}$|^[0-9a-f-]{36}$")
    @NotNull
    private String serviceAgreementId;
    /**
     * Service Agreement Master
     * (Required)
     * 
     */
    @JsonProperty("serviceAgreementMaster")
    @NotNull
    private Boolean serviceAgreementMaster;
    /**
     * External Unique Identifier.
     * 
     */
    @JsonProperty("externalId")
    @Pattern(regexp = "^[^\\r\\n]{1,64}$")
    @Size(min = 1, max = 64)
    private String externalId;
    /**
     * Brief text to describe service agreement
     * 
     */
    @JsonProperty("description")
    @Size(min = 1, max = 255)
    private String description;
    /**
     * Additional Properties
     * 
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Map<String, String> additions = new HashMap<String, String>();

    /**
     * Service Agreement name
     * (Required)
     * 
     * @return
     *     The serviceAgreementName
     */
    @JsonProperty("serviceAgreementName")
    public String getServiceAgreementName() {
        return serviceAgreementName;
    }

    /**
     * Service Agreement name
     * (Required)
     * 
     * @param serviceAgreementName
     *     The serviceAgreementName
     */
    @JsonProperty("serviceAgreementName")
    public void setServiceAgreementName(String serviceAgreementName) {
        this.serviceAgreementName = serviceAgreementName;
    }

    public Element withServiceAgreementName(String serviceAgreementName) {
        this.serviceAgreementName = serviceAgreementName;
        return this;
    }

    /**
     * Universally Unique Identifier.
     * (Required)
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
     * (Required)
     * 
     * @param serviceAgreementId
     *     The serviceAgreementId
     */
    @JsonProperty("serviceAgreementId")
    public void setServiceAgreementId(String serviceAgreementId) {
        this.serviceAgreementId = serviceAgreementId;
    }

    public Element withServiceAgreementId(String serviceAgreementId) {
        this.serviceAgreementId = serviceAgreementId;
        return this;
    }

    /**
     * Service Agreement Master
     * (Required)
     * 
     * @return
     *     The serviceAgreementMaster
     */
    @JsonProperty("serviceAgreementMaster")
    public Boolean getServiceAgreementMaster() {
        return serviceAgreementMaster;
    }

    /**
     * Service Agreement Master
     * (Required)
     * 
     * @param serviceAgreementMaster
     *     The serviceAgreementMaster
     */
    @JsonProperty("serviceAgreementMaster")
    public void setServiceAgreementMaster(Boolean serviceAgreementMaster) {
        this.serviceAgreementMaster = serviceAgreementMaster;
    }

    public Element withServiceAgreementMaster(Boolean serviceAgreementMaster) {
        this.serviceAgreementMaster = serviceAgreementMaster;
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

    public Element withExternalId(String externalId) {
        this.externalId = externalId;
        return this;
    }

    /**
     * Brief text to describe service agreement
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
     * 
     * @param description
     *     The description
     */
    @JsonProperty("description")
    public void setDescription(String description) {
        this.description = description;
    }

    public Element withDescription(String description) {
        this.description = description;
        return this;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(serviceAgreementName).append(serviceAgreementId).append(serviceAgreementMaster).append(externalId).append(description).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Element) == false) {
            return false;
        }
        Element rhs = ((Element) other);
        return new EqualsBuilder().append(serviceAgreementName, rhs.serviceAgreementName).append(serviceAgreementId, rhs.serviceAgreementId).append(serviceAgreementMaster, rhs.serviceAgreementMaster).append(externalId, rhs.externalId).append(description, rhs.description).isEquals();
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
