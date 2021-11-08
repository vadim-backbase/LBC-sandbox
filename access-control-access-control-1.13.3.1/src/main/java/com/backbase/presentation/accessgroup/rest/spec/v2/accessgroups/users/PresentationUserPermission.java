
package com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.users;

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
    "resource",
    "businessFunction",
    "privileges"
})
public class PresentationUserPermission implements AdditionalPropertiesAware
{

    @JsonProperty("resource")
    private String resource;
    @JsonProperty("businessFunction")
    private String businessFunction;
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

    public PresentationUserPermission withResource(String resource) {
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

    public PresentationUserPermission withBusinessFunction(String businessFunction) {
        this.businessFunction = businessFunction;
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

    public PresentationUserPermission withPrivileges(List<String> privileges) {
        this.privileges = privileges;
        return this;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(resource).append(businessFunction).append(privileges).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof PresentationUserPermission) == false) {
            return false;
        }
        PresentationUserPermission rhs = ((PresentationUserPermission) other);
        return new EqualsBuilder().append(resource, rhs.resource).append(businessFunction, rhs.businessFunction).append(privileges, rhs.privileges).isEquals();
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
