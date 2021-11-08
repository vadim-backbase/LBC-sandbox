
package com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Generated;
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
    "internalIdIdentifier",
    "externalIdIdentifier"
})
public class PresentationItemIdentifier implements AdditionalPropertiesAware
{

    /**
     * Data item id.
     * 
     */
    @JsonProperty("internalIdIdentifier")
    @Size(min = 1, max = 36)
    private String internalIdIdentifier;
    /**
     * External Unique Identifier.
     * 
     */
    @JsonProperty("externalIdIdentifier")
    @Pattern(regexp = "^[^\\r\\n]{1,64}$")
    @Size(min = 1, max = 64)
    private String externalIdIdentifier;
    /**
     * Additional Properties
     * 
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Map<String, String> additions = new HashMap<String, String>();

    /**
     * Data item id.
     * 
     * @return
     *     The internalIdIdentifier
     */
    @JsonProperty("internalIdIdentifier")
    public String getInternalIdIdentifier() {
        return internalIdIdentifier;
    }

    /**
     * Data item id.
     * 
     * @param internalIdIdentifier
     *     The internalIdIdentifier
     */
    @JsonProperty("internalIdIdentifier")
    public void setInternalIdIdentifier(String internalIdIdentifier) {
        this.internalIdIdentifier = internalIdIdentifier;
    }

    public PresentationItemIdentifier withInternalIdIdentifier(String internalIdIdentifier) {
        this.internalIdIdentifier = internalIdIdentifier;
        return this;
    }

    /**
     * External Unique Identifier.
     * 
     * @return
     *     The externalIdIdentifier
     */
    @JsonProperty("externalIdIdentifier")
    public String getExternalIdIdentifier() {
        return externalIdIdentifier;
    }

    /**
     * External Unique Identifier.
     * 
     * @param externalIdIdentifier
     *     The externalIdIdentifier
     */
    @JsonProperty("externalIdIdentifier")
    public void setExternalIdIdentifier(String externalIdIdentifier) {
        this.externalIdIdentifier = externalIdIdentifier;
    }

    public PresentationItemIdentifier withExternalIdIdentifier(String externalIdIdentifier) {
        this.externalIdIdentifier = externalIdIdentifier;
        return this;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(internalIdIdentifier).append(externalIdIdentifier).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof PresentationItemIdentifier) == false) {
            return false;
        }
        PresentationItemIdentifier rhs = ((PresentationItemIdentifier) other);
        return new EqualsBuilder().append(internalIdIdentifier, rhs.internalIdIdentifier).append(externalIdIdentifier, rhs.externalIdIdentifier).isEquals();
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
