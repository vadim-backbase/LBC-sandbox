
package com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals;

import java.util.HashMap;
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


/**
 * Permission matrix
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
    "functionId",
    "functionCode",
    "resource",
    "name",
    "action",
    "privileges"
})
public class PermissionMatrix implements AdditionalPropertiesAware
{

    /**
     * Id
     * 
     */
    @JsonProperty("functionId")
    private String functionId;
    /**
     * Code
     * 
     */
    @JsonProperty("functionCode")
    private String functionCode;
    /**
     * Resource
     * 
     */
    @JsonProperty("resource")
    private String resource;
    /**
     * Name
     * 
     */
    @JsonProperty("name")
    private String name;
    /**
     * Presentation action
     * 
     */
    @JsonProperty("action")
    private PermissionMatrixAction action;
    /**
     * Presentation action
     * 
     */
    @JsonProperty("privileges")
    @Valid
    private com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.OldNewPrivileges privileges;
    /**
     * Additional Properties
     * 
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Map<String, String> additions = new HashMap<String, String>();

    /**
     * Id
     * 
     * @return
     *     The functionId
     */
    @JsonProperty("functionId")
    public String getFunctionId() {
        return functionId;
    }

    /**
     * Id
     * 
     * @param functionId
     *     The functionId
     */
    @JsonProperty("functionId")
    public void setFunctionId(String functionId) {
        this.functionId = functionId;
    }

    public PermissionMatrix withFunctionId(String functionId) {
        this.functionId = functionId;
        return this;
    }

    /**
     * Code
     * 
     * @return
     *     The functionCode
     */
    @JsonProperty("functionCode")
    public String getFunctionCode() {
        return functionCode;
    }

    /**
     * Code
     * 
     * @param functionCode
     *     The functionCode
     */
    @JsonProperty("functionCode")
    public void setFunctionCode(String functionCode) {
        this.functionCode = functionCode;
    }

    public PermissionMatrix withFunctionCode(String functionCode) {
        this.functionCode = functionCode;
        return this;
    }

    /**
     * Resource
     * 
     * @return
     *     The resource
     */
    @JsonProperty("resource")
    public String getResource() {
        return resource;
    }

    /**
     * Resource
     * 
     * @param resource
     *     The resource
     */
    @JsonProperty("resource")
    public void setResource(String resource) {
        this.resource = resource;
    }

    public PermissionMatrix withResource(String resource) {
        this.resource = resource;
        return this;
    }

    /**
     * Name
     * 
     * @return
     *     The name
     */
    @JsonProperty("name")
    public String getName() {
        return name;
    }

    /**
     * Name
     * 
     * @param name
     *     The name
     */
    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    public PermissionMatrix withName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Presentation action
     * 
     * @return
     *     The action
     */
    @JsonProperty("action")
    public PermissionMatrixAction getAction() {
        return action;
    }

    /**
     * Presentation action
     * 
     * @param action
     *     The action
     */
    @JsonProperty("action")
    public void setAction(PermissionMatrixAction action) {
        this.action = action;
    }

    public PermissionMatrix withAction(PermissionMatrixAction action) {
        this.action = action;
        return this;
    }

    /**
     * Presentation action
     * 
     * @return
     *     The privileges
     */
    @JsonProperty("privileges")
    public com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.OldNewPrivileges getPrivileges() {
        return privileges;
    }

    /**
     * Presentation action
     * 
     * @param privileges
     *     The privileges
     */
    @JsonProperty("privileges")
    public void setPrivileges(
        com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.OldNewPrivileges privileges) {
        this.privileges = privileges;
    }

    public PermissionMatrix withPrivileges(OldNewPrivileges privileges) {
        this.privileges = privileges;
        return this;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(functionId).append(functionCode).append(resource).append(name).append(action).append(privileges).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof PermissionMatrix) == false) {
            return false;
        }
        PermissionMatrix rhs = ((PermissionMatrix) other);
        return new EqualsBuilder().append(functionId, rhs.functionId).append(functionCode, rhs.functionCode).append(resource, rhs.resource).append(name, rhs.name).append(action, rhs.action).append(privileges, rhs.privileges).isEquals();
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
