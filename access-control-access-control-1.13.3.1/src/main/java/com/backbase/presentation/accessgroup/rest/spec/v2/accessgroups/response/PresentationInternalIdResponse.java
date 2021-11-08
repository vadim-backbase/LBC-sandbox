
package com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.response;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Generated;
import com.backbase.buildingblocks.persistence.model.AdditionalPropertiesAware;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Object that defines an id attribute only.
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
    "id"
})
@JsonIgnoreProperties(ignoreUnknown = true)
public class PresentationInternalIdResponse implements AdditionalPropertiesAware
{

    /**
     * Internal id of the entity.
     * 
     */
    @JsonProperty("id")
    private BigDecimal id;
    /**
     * Additional Properties
     * 
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Map<String, String> additions = new HashMap<String, String>();

    /**
     * Internal id of the entity.
     * 
     * @return
     *     The id
     */
    @JsonProperty("id")
    public BigDecimal getId() {
        return id;
    }

    /**
     * Internal id of the entity.
     * 
     * @param id
     *     The id
     */
    @JsonProperty("id")
    public void setId(BigDecimal id) {
        this.id = id;
    }

    public PresentationInternalIdResponse withId(BigDecimal id) {
        this.id = id;
        return this;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(id).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof PresentationInternalIdResponse) == false) {
            return false;
        }
        PresentationInternalIdResponse rhs = ((PresentationInternalIdResponse) other);
        return new EqualsBuilder().append(id, rhs.id).isEquals();
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
