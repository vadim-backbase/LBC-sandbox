
package com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.aps;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Generated;
import javax.validation.Valid;
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
    "functionName",
    "resourceName",
    "privileges"
})
public class PresentationPermissionItem implements AdditionalPropertiesAware
{

    /**
     * Business function id.
     * 
     */
    @JsonProperty("functionId")
    private String functionId;
    /**
     * Function name.
     * 
     */
    @JsonProperty("functionName")
    private String functionName;
    /**
     * Resource name.
     * 
     */
    @JsonProperty("resourceName")
    private String resourceName;
    /**
     * Assignable permissions.
     * 
     */
    @JsonProperty("privileges")
    @Valid
    private List<String> privileges = new ArrayList<String>();
    /**
     * Additional Properties
     * 
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Map<String, String> additions = new HashMap<String, String>();

    /**
     * Business function id.
     * 
     * @return
     *     The functionId
     */
    @JsonProperty("functionId")
    public String getFunctionId() {
        return functionId;
    }

    /**
     * Business function id.
     * 
     * @param functionId
     *     The functionId
     */
    @JsonProperty("functionId")
    public void setFunctionId(String functionId) {
        this.functionId = functionId;
    }

    public PresentationPermissionItem withFunctionId(String functionId) {
        this.functionId = functionId;
        return this;
    }

    /**
     * Function name.
     * 
     * @return
     *     The functionName
     */
    @JsonProperty("functionName")
    public String getFunctionName() {
        return functionName;
    }

    /**
     * Function name.
     * 
     * @param functionName
     *     The functionName
     */
    @JsonProperty("functionName")
    public void setFunctionName(String functionName) {
        this.functionName = functionName;
    }

    public PresentationPermissionItem withFunctionName(String functionName) {
        this.functionName = functionName;
        return this;
    }

    /**
     * Resource name.
     * 
     * @return
     *     The resourceName
     */
    @JsonProperty("resourceName")
    public String getResourceName() {
        return resourceName;
    }

    /**
     * Resource name.
     * 
     * @param resourceName
     *     The resourceName
     */
    @JsonProperty("resourceName")
    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    public PresentationPermissionItem withResourceName(String resourceName) {
        this.resourceName = resourceName;
        return this;
    }

    /**
     * Assignable permissions.
     * 
     * @return
     *     The privileges
     */
    @JsonProperty("privileges")
    public List<String> getPrivileges() {
        return privileges;
    }

    /**
     * Assignable permissions.
     * 
     * @param privileges
     *     The privileges
     */
    @JsonProperty("privileges")
    public void setPrivileges(List<String> privileges) {
        this.privileges = privileges;
    }

    public PresentationPermissionItem withPrivileges(List<String> privileges) {
        this.privileges = privileges;
        return this;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(functionId).append(functionName).append(resourceName).append(privileges).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof PresentationPermissionItem) == false) {
            return false;
        }
        PresentationPermissionItem rhs = ((PresentationPermissionItem) other);
        return new EqualsBuilder().append(functionId, rhs.functionId).append(functionName, rhs.functionName).append(resourceName, rhs.resourceName).append(privileges, rhs.privileges).isEquals();
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
