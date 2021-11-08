
package com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.users;

import com.backbase.buildingblocks.persistence.model.AdditionalPropertiesAware;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import javax.annotation.Generated;
import javax.validation.Valid;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
    "userId",
    "functionGroupIds"
})
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserFunctionGroupsGetResponseBody implements AdditionalPropertiesAware
{

    /**
     * Internal id of the user.
     * 
     */
    @JsonProperty("userId")
    private String userId;
    /**
     * List of internal function group ids.
     * 
     */
    @JsonProperty("functionGroupIds")
    @JsonDeserialize(as = LinkedHashSet.class)
    @Valid
    private Set<String> functionGroupIds = new LinkedHashSet<String>();
    /**
     * Additional Properties
     * 
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Map<String, String> additions = new HashMap<String, String>();

    /**
     * Internal id of the user.
     * 
     * @return
     *     The userId
     */
    @JsonProperty("userId")
    public String getUserId() {
        return userId;
    }

    /**
     * Internal id of the user.
     * 
     * @param userId
     *     The userId
     */
    @JsonProperty("userId")
    public void setUserId(String userId) {
        this.userId = userId;
    }

    public UserFunctionGroupsGetResponseBody withUserId(String userId) {
        this.userId = userId;
        return this;
    }

    /**
     * List of internal function group ids.
     * 
     * @return
     *     The functionGroupIds
     */
    @JsonProperty("functionGroupIds")
    public Set<String> getFunctionGroupIds() {
        return functionGroupIds;
    }

    /**
     * List of internal function group ids.
     * 
     * @param functionGroupIds
     *     The functionGroupIds
     */
    @JsonProperty("functionGroupIds")
    public void setFunctionGroupIds(Set<String> functionGroupIds) {
        this.functionGroupIds = functionGroupIds;
    }

    public UserFunctionGroupsGetResponseBody withFunctionGroupIds(Set<String> functionGroupIds) {
        this.functionGroupIds = functionGroupIds;
        return this;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(userId).append(functionGroupIds).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof UserFunctionGroupsGetResponseBody) == false) {
            return false;
        }
        UserFunctionGroupsGetResponseBody rhs = ((UserFunctionGroupsGetResponseBody) other);
        return new EqualsBuilder().append(userId, rhs.userId).append(functionGroupIds, rhs.functionGroupIds).isEquals();
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
