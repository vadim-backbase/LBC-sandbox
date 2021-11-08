
package com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.functiongroups;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Generated;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
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
    "functionId",
    "privileges"
})
public class PresentationPermission implements AdditionalPropertiesAware
{

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("functionId")
    @Size(min = 1, max = 36)
    @NotNull
    private String functionId;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("privileges")
    @Valid
    @NotNull
    private List<String> privileges = new ArrayList<String>();
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
     *     The functionId
     */
    @JsonProperty("functionId")
    public String getFunctionId() {
        return functionId;
    }

    /**
     * 
     * (Required)
     * 
     * @param functionId
     *     The functionId
     */
    @JsonProperty("functionId")
    public void setFunctionId(String functionId) {
        this.functionId = functionId;
    }

    public PresentationPermission withFunctionId(String functionId) {
        this.functionId = functionId;
        return this;
    }

    /**
     * 
     * (Required)
     * 
     * @return
     *     The privileges
     */
    @JsonProperty("privileges")
    public List<String> getPrivileges() {
        return privileges;
    }

    /**
     * 
     * (Required)
     * 
     * @param privileges
     *     The privileges
     */
    @JsonProperty("privileges")
    public void setPrivileges(List<String> privileges) {
        this.privileges = privileges;
    }

    public PresentationPermission withPrivileges(List<String> privileges) {
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
        if ((other instanceof PresentationPermission) == false) {
            return false;
        }
        PresentationPermission rhs = ((PresentationPermission) other);
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
