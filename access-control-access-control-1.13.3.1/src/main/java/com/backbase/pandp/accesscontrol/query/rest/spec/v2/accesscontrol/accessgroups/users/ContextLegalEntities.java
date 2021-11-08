
package com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.users;

import com.backbase.buildingblocks.persistence.model.AdditionalPropertiesAware;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Generated;
import javax.validation.Valid;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * List of legal entity ids
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
    "legalEntities"
})
@JsonIgnoreProperties(ignoreUnknown = true)
public class ContextLegalEntities implements AdditionalPropertiesAware
{

    @JsonProperty("legalEntities")
    @Valid
    private List<String> legalEntities = new ArrayList<String>();
    /**
     * Additional Properties
     * 
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Map<String, String> additions = new HashMap<String, String>();

    /**
     * 
     * @return
     *     The legalEntities
     */
    @JsonProperty("legalEntities")
    public List<String> getLegalEntities() {
        return legalEntities;
    }

    /**
     * 
     * @param legalEntities
     *     The legalEntities
     */
    @JsonProperty("legalEntities")
    public void setLegalEntities(List<String> legalEntities) {
        this.legalEntities = legalEntities;
    }

    public ContextLegalEntities withLegalEntities(List<String> legalEntities) {
        this.legalEntities = legalEntities;
        return this;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(legalEntities).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ContextLegalEntities) == false) {
            return false;
        }
        ContextLegalEntities rhs = ((ContextLegalEntities) other);
        return new EqualsBuilder().append(legalEntities, rhs.legalEntities).isEquals();
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
