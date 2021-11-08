
package com.backbase.presentation.legalentity.rest.spec.v2.legalentities;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Generated;
import javax.validation.constraints.NotNull;
import com.backbase.buildingblocks.persistence.model.AdditionalPropertiesAware;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.enumeration.LegalEntityType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Legal entity update item.
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
    "type"
})
@JsonIgnoreProperties(ignoreUnknown = true)
public class LegalEntityByExternalIdPutRequestBody implements AdditionalPropertiesAware
{

    /**
     * Type of the legal entity. Bank or Customer.
     * (Required)
     * 
     */
    @JsonProperty("type")
    @NotNull
    private LegalEntityType type;
    /**
     * Additional Properties
     * 
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Map<String, String> additions = new HashMap<String, String>();

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

    public LegalEntityByExternalIdPutRequestBody withType(LegalEntityType type) {
        this.type = type;
        return this;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(type).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof LegalEntityByExternalIdPutRequestBody) == false) {
            return false;
        }
        LegalEntityByExternalIdPutRequestBody rhs = ((LegalEntityByExternalIdPutRequestBody) other);
        return new EqualsBuilder().append(type, rhs.type).isEquals();
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
