
package com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.aps;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import javax.annotation.Generated;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import com.backbase.buildingblocks.persistence.model.AdditionalPropertiesAware;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
    "functionId",
    "privileges"
})
public class PresentationPermissionSetItem implements AdditionalPropertiesAware
{

    /**
     * Business function id
     * (Required)
     * 
     */
    @JsonProperty("functionId")
    @Size(min = 1, max = 36)
    @NotNull
    private String functionId;
    /**
     * List of privileges
     * (Required)
     * 
     */
    @JsonProperty("privileges")
    @JsonDeserialize(as = LinkedHashSet.class)
    @Size(min = 1)
    @Valid
    @NotNull
    private Set<String> privileges = new LinkedHashSet<String>();
    /**
     * Additional Properties
     * 
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Map<String, String> additions = new HashMap<String, String>();

    /**
     * Business function id
     * (Required)
     * 
     * @return
     *     The functionId
     */
    @JsonProperty("functionId")
    public String getFunctionId() {
        return functionId;
    }

    /**
     * Business function id
     * (Required)
     * 
     * @param functionId
     *     The functionId
     */
    @JsonProperty("functionId")
    public void setFunctionId(String functionId) {
        this.functionId = functionId;
    }

    public PresentationPermissionSetItem withFunctionId(String functionId) {
        this.functionId = functionId;
        return this;
    }

    /**
     * List of privileges
     * (Required)
     * 
     * @return
     *     The privileges
     */
    @JsonProperty("privileges")
    public Set<String> getPrivileges() {
        return privileges;
    }

    /**
     * List of privileges
     * (Required)
     * 
     * @param privileges
     *     The privileges
     */
    @JsonProperty("privileges")
    public void setPrivileges(Set<String> privileges) {
        this.privileges = privileges;
    }

    public PresentationPermissionSetItem withPrivileges(Set<String> privileges) {
        this.privileges = privileges;
        return this;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(functionId).append(privileges).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof PresentationPermissionSetItem) == false) {
            return false;
        }
        PresentationPermissionSetItem rhs = ((PresentationPermissionSetItem) other);
        return new EqualsBuilder().append(functionId, rhs.functionId).append(privileges, rhs.privileges).isEquals();
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
