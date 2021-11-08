
package com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.function;

import com.backbase.buildingblocks.persistence.model.AdditionalPropertiesAware;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Generated;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
    "functionId",
    "assignedPrivileges"
})
public class Permission implements AdditionalPropertiesAware
{

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("functionId")
    @NotNull
    private String functionId;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("assignedPrivileges")
    @Valid
    @NotNull
    private List<Privilege> assignedPrivileges = new ArrayList<Privilege>();
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

    public Permission withFunctionId(String functionId) {
        this.functionId = functionId;
        return this;
    }

    /**
     * 
     * (Required)
     * 
     * @return
     *     The assignedPrivileges
     */
    @JsonProperty("assignedPrivileges")
    public List<Privilege> getAssignedPrivileges() {
        return assignedPrivileges;
    }

    /**
     * 
     * (Required)
     * 
     * @param assignedPrivileges
     *     The assignedPrivileges
     */
    @JsonProperty("assignedPrivileges")
    public void setAssignedPrivileges(List<Privilege> assignedPrivileges) {
        this.assignedPrivileges = assignedPrivileges;
    }

    public Permission withAssignedPrivileges(List<Privilege> assignedPrivileges) {
        this.assignedPrivileges = assignedPrivileges;
        return this;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(functionId).append(assignedPrivileges).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Permission) == false) {
            return false;
        }
        Permission rhs = ((Permission) other);
        return new EqualsBuilder().append(functionId, rhs.functionId).append(assignedPrivileges, rhs.assignedPrivileges).isEquals();
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
