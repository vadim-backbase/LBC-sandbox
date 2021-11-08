
package com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.function;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Generated;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import com.backbase.buildingblocks.persistence.model.AdditionalPropertiesAware;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Privilege
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
    "privilege",
    "supportsLimit"
})
public class PresentationPrivilege implements AdditionalPropertiesAware
{

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("privilege")
    @Size(min = 1, max = 16)
    @NotNull
    private String privilege;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("supportsLimit")
    @NotNull
    private Boolean supportsLimit;
    /**
     * Additional Properties
     * 
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Map<String, String> additions = new HashMap<String, String>();

    /**
     * 
     * (Required)
     * 
     * @return
     *     The privilege
     */
    @JsonProperty("privilege")
    public String getPrivilege() {
        return privilege;
    }

    /**
     * 
     * (Required)
     * 
     * @param privilege
     *     The privilege
     */
    @JsonProperty("privilege")
    public void setPrivilege(String privilege) {
        this.privilege = privilege;
    }

    public PresentationPrivilege withPrivilege(String privilege) {
        this.privilege = privilege;
        return this;
    }

    /**
     * 
     * (Required)
     * 
     * @return
     *     The supportsLimit
     */
    @JsonProperty("supportsLimit")
    public Boolean getSupportsLimit() {
        return supportsLimit;
    }

    /**
     * 
     * (Required)
     * 
     * @param supportsLimit
     *     The supportsLimit
     */
    @JsonProperty("supportsLimit")
    public void setSupportsLimit(Boolean supportsLimit) {
        this.supportsLimit = supportsLimit;
    }

    public PresentationPrivilege withSupportsLimit(Boolean supportsLimit) {
        this.supportsLimit = supportsLimit;
        return this;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(privilege).append(supportsLimit).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof PresentationPrivilege) == false) {
            return false;
        }
        PresentationPrivilege rhs = ((PresentationPrivilege) other);
        return new EqualsBuilder().append(privilege, rhs.privilege).append(supportsLimit, rhs.supportsLimit).isEquals();
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
