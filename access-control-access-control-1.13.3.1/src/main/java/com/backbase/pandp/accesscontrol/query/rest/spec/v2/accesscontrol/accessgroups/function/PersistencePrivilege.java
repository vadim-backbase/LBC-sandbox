
package com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.function;

import com.backbase.buildingblocks.persistence.model.AdditionalPropertiesAware;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Generated;
import javax.validation.constraints.NotNull;
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
public class PersistencePrivilege implements AdditionalPropertiesAware
{

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("privilege")
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

    public PersistencePrivilege withPrivilege(String privilege) {
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

    public PersistencePrivilege withSupportsLimit(Boolean supportsLimit) {
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
        if ((other instanceof PersistencePrivilege) == false) {
            return false;
        }
        PersistencePrivilege rhs = ((PersistencePrivilege) other);
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
