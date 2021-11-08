
package com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.users;

import com.backbase.buildingblocks.persistence.model.AdditionalPropertiesAware;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.config.functions.FunctionsGetResponseBody;
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
    "resource",
    "businessFunction",
    "functionId",
    "functionCode",
    "privileges"
})
public class PersistenceUserPermission implements AdditionalPropertiesAware
{

    @JsonProperty("resource")
    private String resource;
    @JsonProperty("businessFunction")
    private String businessFunction;
    @JsonProperty("functionId")
    @NotNull
    private String functionId;
    @JsonProperty("functionCode")
    private String functionCode;
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
     * 
     * @return
     *     The resource
     */
    @JsonProperty("resource")
    public String getResource() {
        return resource;
    }

    /**
     * 
     * @param resource
     *     The resource
     */
    @JsonProperty("resource")
    public void setResource(String resource) {
        this.resource = resource;
    }

    public PersistenceUserPermission withResource(String resource) {
        this.resource = resource;
        return this;
    }

    /**
     * 
     * @return
     *     The businessFunction
     */
    @JsonProperty("businessFunction")
    public String getBusinessFunction() {
        return businessFunction;
    }

    /**
     * 
     * @param businessFunction
     *     The businessFunction
     */
    @JsonProperty("businessFunction")
    public void setBusinessFunction(String businessFunction) {
        this.businessFunction = businessFunction;
    }

    public PersistenceUserPermission withBusinessFunction(String businessFunction) {
        this.businessFunction = businessFunction;
        return this;
    }

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

    @JsonProperty("functionId")
    public void setFunctionId(String functionId) {
        this.functionId = functionId;
    }

    public PersistenceUserPermission withFunctionId(String functionId) {
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

    public PersistenceUserPermission withFunctionCode(String functionCode) {
        this.functionCode = functionCode;
        return this;
    }

    /**
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
     * @param privileges
     *     The privileges
     */
    @JsonProperty("privileges")
    public void setPrivileges(List<String> privileges) {
        this.privileges = privileges;
    }

    public PersistenceUserPermission withPrivileges(List<String> privileges) {
        this.privileges = privileges;
        return this;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(resource).append(businessFunction).append(functionId).append(functionCode).append(privileges).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof PersistenceUserPermission) == false) {
            return false;
        }
        PersistenceUserPermission rhs = ((PersistenceUserPermission) other);
        return new EqualsBuilder().append(resource, rhs.resource).append(businessFunction, rhs.businessFunction).append(functionId, rhs.functionId)
            .append(functionCode, rhs.functionCode).append(privileges, rhs.privileges).isEquals();
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
