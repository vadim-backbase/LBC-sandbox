
package com.backbase.presentation.legalentity.rest.spec.v2.legalentities;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Generated;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import com.backbase.buildingblocks.persistence.model.AdditionalPropertiesAware;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.enumeration.LegalEntityType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
    "externalId",
    "name",
    "parentInternalId",
    "type",
    "activateSingleServiceAgreement"
})
@JsonIgnoreProperties(ignoreUnknown = true)
public class PresentationCreateLegalEntityItemPostRequestBody implements AdditionalPropertiesAware
{

    /**
     * External legal entity identifier.
     * 
     */
    @JsonProperty("externalId")
    @Pattern(regexp = "^[^\\r\\n]{1,64}$")
    @Size(min = 1, max = 64)
    private String externalId;
    /**
     * Legal Entity name
     * (Required)
     * 
     */
    @JsonProperty("name")
    @Pattern(regexp = "^\\S(.*(\\S))?$")
    @Size(min = 1, max = 128)
    @NotNull
    private String name;
    /**
     * Universally Unique IDentifier.
     * (Required)
     * 
     */
    @JsonProperty("parentInternalId")
    @Pattern(regexp = "^[0-9a-f]{32}$|^[0-9a-f-]{36}$")
    @Size(min = 1, max = 36)
    @NotNull
    private String parentInternalId;
    /**
     * Type of the legal entity. Bank or Customer.
     * (Required)
     * 
     */
    @JsonProperty("type")
    @NotNull
    private LegalEntityType type;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("activateSingleServiceAgreement")
    @NotNull
    private Boolean activateSingleServiceAgreement = true;
    /**
     * Additional Properties
     * 
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Map<String, String> additions = new HashMap<String, String>();

    /**
     * External legal entity identifier.
     * 
     * @return
     *     The externalId
     */
    @JsonProperty("externalId")
    public String getExternalId() {
        return externalId;
    }

    /**
     * External legal entity identifier.
     * 
     * @param externalId
     *     The externalId
     */
    @JsonProperty("externalId")
    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public PresentationCreateLegalEntityItemPostRequestBody withExternalId(String externalId) {
        this.externalId = externalId;
        return this;
    }

    /**
     * Legal Entity name
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
     * Legal Entity name
     * (Required)
     * 
     * @param name
     *     The name
     */
    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    public PresentationCreateLegalEntityItemPostRequestBody withName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Universally Unique IDentifier.
     * (Required)
     * 
     * @return
     *     The parentInternalId
     */
    @JsonProperty("parentInternalId")
    public String getParentInternalId() {
        return parentInternalId;
    }

    /**
     * Universally Unique IDentifier.
     * (Required)
     * 
     * @param parentInternalId
     *     The parentInternalId
     */
    @JsonProperty("parentInternalId")
    public void setParentInternalId(String parentInternalId) {
        this.parentInternalId = parentInternalId;
    }

    public PresentationCreateLegalEntityItemPostRequestBody withParentInternalId(String parentInternalId) {
        this.parentInternalId = parentInternalId;
        return this;
    }

    /**
     * Type of the legal entity. Bank or Customer.
     * (Required)
     * 
     * @return
     *     The type
     */
    @JsonProperty("type")
    public LegalEntityType getType() {
        return type;
    }

    /**
     * Type of the legal entity. Bank or Customer.
     * (Required)
     * 
     * @param type
     *     The type
     */
    @JsonProperty("type")
    public void setType(LegalEntityType type) {
        this.type = type;
    }

    public PresentationCreateLegalEntityItemPostRequestBody withType(LegalEntityType type) {
        this.type = type;
        return this;
    }

    /**
     * 
     * (Required)
     * 
     * @return
     *     The activateSingleServiceAgreement
     */
    @JsonProperty("activateSingleServiceAgreement")
    public Boolean getActivateSingleServiceAgreement() {
        return activateSingleServiceAgreement;
    }

    /**
     * 
     * (Required)
     * 
     * @param activateSingleServiceAgreement
     *     The activateSingleServiceAgreement
     */
    @JsonProperty("activateSingleServiceAgreement")
    public void setActivateSingleServiceAgreement(Boolean activateSingleServiceAgreement) {
        this.activateSingleServiceAgreement = activateSingleServiceAgreement;
    }

    public PresentationCreateLegalEntityItemPostRequestBody withActivateSingleServiceAgreement(Boolean activateSingleServiceAgreement) {
        this.activateSingleServiceAgreement = activateSingleServiceAgreement;
        return this;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(externalId).append(name).append(parentInternalId).append(type).append(activateSingleServiceAgreement).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof PresentationCreateLegalEntityItemPostRequestBody) == false) {
            return false;
        }
        PresentationCreateLegalEntityItemPostRequestBody rhs = ((PresentationCreateLegalEntityItemPostRequestBody) other);
        return new EqualsBuilder().append(externalId, rhs.externalId).append(name, rhs.name).append(parentInternalId, rhs.parentInternalId).append(type, rhs.type).append(activateSingleServiceAgreement, rhs.activateSingleServiceAgreement).isEquals();
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
