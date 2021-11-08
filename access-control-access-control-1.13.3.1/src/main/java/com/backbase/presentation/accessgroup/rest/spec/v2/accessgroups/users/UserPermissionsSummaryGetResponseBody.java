
package com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.users;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Generated;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import com.backbase.buildingblocks.persistence.model.AdditionalPropertiesAware;
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
    "resource",
    "function",
    "permissions"
})
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserPermissionsSummaryGetResponseBody implements AdditionalPropertiesAware
{

    /**
     * Resource name
     * (Required)
     * 
     */
    @JsonProperty("resource")
    @Size(min = 1, max = 32)
    @NotNull
    private String resource;
    /**
     * Business function name
     * (Required)
     * 
     */
    @JsonProperty("function")
    @Size(min = 1, max = 32)
    @NotNull
    private String function;
    /**
     * Allowed user permissions
     * (Required)
     * 
     */
    @JsonProperty("permissions")
    @Valid
    @NotNull
    private Map<String, Boolean> permissions;
    /**
     * Additional Properties
     * 
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Map<String, String> additions = new HashMap<String, String>();

    /**
     * Resource name
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
     * Resource name
     * (Required)
     * 
     * @param resource
     *     The resource
     */
    @JsonProperty("resource")
    public void setResource(String resource) {
        this.resource = resource;
    }

    public UserPermissionsSummaryGetResponseBody withResource(String resource) {
        this.resource = resource;
        return this;
    }

    /**
     * Business function name
     * (Required)
     * 
     * @return
     *     The function
     */
    @JsonProperty("function")
    public String getFunction() {
        return function;
    }

    /**
     * Business function name
     * (Required)
     * 
     * @param function
     *     The function
     */
    @JsonProperty("function")
    public void setFunction(String function) {
        this.function = function;
    }

    public UserPermissionsSummaryGetResponseBody withFunction(String function) {
        this.function = function;
        return this;
    }

    /**
     * Allowed user permissions
     * (Required)
     * 
     * @return
     *     The permissions
     */
    @JsonProperty("permissions")
    public Map<String, Boolean> getPermissions() {
        return permissions;
    }

    /**
     * Allowed user permissions
     * (Required)
     * 
     * @param permissions
     *     The permissions
     */
    @JsonProperty("permissions")
    public void setPermissions(Map<String, Boolean> permissions) {
        this.permissions = permissions;
    }

    public UserPermissionsSummaryGetResponseBody withPermissions(Map<String, Boolean> permissions) {
        this.permissions = permissions;
        return this;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(resource).append(function).append(permissions).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof UserPermissionsSummaryGetResponseBody) == false) {
            return false;
        }
        UserPermissionsSummaryGetResponseBody rhs = ((UserPermissionsSummaryGetResponseBody) other);
        return new EqualsBuilder().append(resource, rhs.resource).append(function, rhs.function).append(permissions, rhs.permissions).isEquals();
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
