
package com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.config.functions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Generated;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import com.backbase.buildingblocks.persistence.model.AdditionalPropertiesAware;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.function.PresentationPrivilege;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
    "functionCode",
    "resource",
    "resourceCode",
    "name",
    "privileges"
})
@JsonIgnoreProperties(ignoreUnknown = true)
public class FunctionsGetResponseBody implements AdditionalPropertiesAware
{

    /**
     * Internal id of business function
     * (Required)
     * 
     */
    @JsonProperty("functionId")
    @Size(min = 1, max = 36)
    @NotNull
    private String functionId;
    /**
     * Code of business function
     * (Required)
     * 
     */
    @JsonProperty("functionCode")
    @Size(min = 1, max = 32)
    @NotNull
    private String functionCode;
    /**
     * Name of resource
     * (Required)
     * 
     */
    @JsonProperty("resource")
    @Size(min = 1, max = 32)
    @NotNull
    private String resource;
    /**
     * Code of resource
     * 
     */
    @JsonProperty("resourceCode")
    private String resourceCode;
    /**
     * Business function name
     * (Required)
     * 
     */
    @JsonProperty("name")
    @Size(min = 1, max = 32)
    @NotNull
    private String name;
    /**
     * Applicable privileges for business function
     * (Required)
     * 
     */
    @JsonProperty("privileges")
    @Valid
    @NotNull
    private List<PresentationPrivilege> privileges = new ArrayList<PresentationPrivilege>();
    /**
     * Additional Properties
     * 
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Map<String, String> additions = new HashMap<String, String>();

    /**
     * Internal id of business function
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
     * Internal id of business function
     * (Required)
     * 
     * @param functionId
     *     The functionId
     */
    @JsonProperty("functionId")
    public void setFunctionId(String functionId) {
        this.functionId = functionId;
    }

    public FunctionsGetResponseBody withFunctionId(String functionId) {
        this.functionId = functionId;
        return this;
    }

    /**
     * Code of business function
     * (Required)
     * 
     * @return
     *     The functionCode
     */
    @JsonProperty("functionCode")
    public String getFunctionCode() {
        return functionCode;
    }

    /**
     * Code of business function
     * (Required)
     * 
     * @param functionCode
     *     The functionCode
     */
    @JsonProperty("functionCode")
    public void setFunctionCode(String functionCode) {
        this.functionCode = functionCode;
    }

    public FunctionsGetResponseBody withFunctionCode(String functionCode) {
        this.functionCode = functionCode;
        return this;
    }

    /**
     * Name of resource
     * (Required)
     * 
     * @return
     *     The resource
     */
    @JsonProperty("resource")
    public String getResource() {
        return resource;
    }

    /**
     * Name of resource
     * (Required)
     * 
     * @param resource
     *     The resource
     */
    @JsonProperty("resource")
    public void setResource(String resource) {
        this.resource = resource;
    }

    public FunctionsGetResponseBody withResource(String resource) {
        this.resource = resource;
        return this;
    }

    /**
     * Code of resource
     * 
     * @return
     *     The resourceCode
     */
    @JsonProperty("resourceCode")
    public String getResourceCode() {
        return resourceCode;
    }

    /**
     * Code of resource
     * 
     * @param resourceCode
     *     The resourceCode
     */
    @JsonProperty("resourceCode")
    public void setResourceCode(String resourceCode) {
        this.resourceCode = resourceCode;
    }

    public FunctionsGetResponseBody withResourceCode(String resourceCode) {
        this.resourceCode = resourceCode;
        return this;
    }

    /**
     * Business function name
     * (Required)
     * 
     * @return
     *     The name
     */
    @JsonProperty("name")
    public String getName() {
        return name;
    }

    /**
     * Business function name
     * (Required)
     * 
     * @param name
     *     The name
     */
    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    public FunctionsGetResponseBody withName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Applicable privileges for business function
     * (Required)
     * 
     * @return
     *     The privileges
     */
    @JsonProperty("privileges")
    public List<PresentationPrivilege> getPrivileges() {
        return privileges;
    }

    /**
     * Applicable privileges for business function
     * (Required)
     * 
     * @param privileges
     *     The privileges
     */
    @JsonProperty("privileges")
    public void setPrivileges(List<PresentationPrivilege> privileges) {
        this.privileges = privileges;
    }

    public FunctionsGetResponseBody withPrivileges(List<PresentationPrivilege> privileges) {
        this.privileges = privileges;
        return this;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(functionId).append(functionCode).append(resource).append(resourceCode).append(name).append(privileges).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof FunctionsGetResponseBody) == false) {
            return false;
        }
        FunctionsGetResponseBody rhs = ((FunctionsGetResponseBody) other);
        return new EqualsBuilder().append(functionId, rhs.functionId).append(functionCode, rhs.functionCode).append(resource, rhs.resource).append(resourceCode, rhs.resourceCode).append(name, rhs.name).append(privileges, rhs.privileges).isEquals();
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
