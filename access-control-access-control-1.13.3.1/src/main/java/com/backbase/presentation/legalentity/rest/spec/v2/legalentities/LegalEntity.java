
package com.backbase.presentation.legalentity.rest.spec.v2.legalentities;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Generated;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import com.backbase.buildingblocks.persistence.model.AdditionalPropertiesAware;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.enumeration.LegalEntityType;
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
    "parentExternalId",
    "type",
    "activateSingleServiceAgreement"
})
public class LegalEntity implements AdditionalPropertiesAware
{

    /**
     * External legal entity identifier.
     * (Required)
     * 
     */
    @JsonProperty("externalId")
    @Pattern(regexp = "^[^\\r\\n]{1,64}$")
    @Size(min = 1, max = 64)
    @NotNull
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
     * External legal entity identifier.
     * 
     */
    @JsonProperty("parentExternalId")
    @Pattern(regexp = "^[^\\r\\n]{1,64}$")
    @Size(min = 1, max = 64)
    private String parentExternalId;
    /**
     * Type of the legal entity. Bank or Customer.
     * (Required)
     * 
     */
    @JsonProperty("type")
    @NotNull
    private LegalEntityType type;
    @JsonProperty("activateSingleServiceAgreement")
    private Boolean activateSingleServiceAgreement = true;
    /**
     * Additional Properties
     * 
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Map<String, String> additions = new HashMap<String, String>();

    /**
     * External legal entity identifier.
     * (Required)
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
     * (Required)
     * 
     * @param externalId
     *     The externalId
     */
    @JsonProperty("externalId")
    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public LegalEntity withExternalId(String externalId) {
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

    public LegalEntity withName(String name) {
        this.name = name;
        return this;
    }

    /**
     * External legal entity identifier.
     * 
     * @return
     *     The parentExternalId
     */
    @JsonProperty("parentExternalId")
    public String getParentExternalId() {
        return parentExternalId;
    }

    /**
     * External legal entity identifier.
     * 
     * @param parentExternalId
     *     The parentExternalId
     */
    @JsonProperty("parentExternalId")
    public void setParentExternalId(String parentExternalId) {
        this.parentExternalId = parentExternalId;
    }

    public LegalEntity withParentExternalId(String parentExternalId) {
        this.parentExternalId = parentExternalId;
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

    public LegalEntity withType(LegalEntityType type) {
        this.type = type;
        return this;
    }

    /**
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
     * @param activateSingleServiceAgreement
     *     The activateSingleServiceAgreement
     */
    @JsonProperty("activateSingleServiceAgreement")
    public void setActivateSingleServiceAgreement(Boolean activateSingleServiceAgreement) {
        this.activateSingleServiceAgreement = activateSingleServiceAgreement;
    }

    public LegalEntity withActivateSingleServiceAgreement(Boolean activateSingleServiceAgreement) {
        this.activateSingleServiceAgreement = activateSingleServiceAgreement;
        return this;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(externalId).append(name).append(parentExternalId).append(type).append(activateSingleServiceAgreement).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof LegalEntity) == false) {
            return false;
        }
        LegalEntity rhs = ((LegalEntity) other);
        return new EqualsBuilder().append(externalId, rhs.externalId).append(name, rhs.name).append(parentExternalId, rhs.parentExternalId).append(type, rhs.type).append(activateSingleServiceAgreement, rhs.activateSingleServiceAgreement).isEquals();
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
