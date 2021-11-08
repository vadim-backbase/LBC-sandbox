
package com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.users;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Generated;
import javax.validation.Valid;
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
    "userIds"
})
@JsonIgnoreProperties(ignoreUnknown = true)
public class UsersByPermissionsResponseBody implements AdditionalPropertiesAware
{

    /**
     * List of internal userIds
     * 
     */
    @JsonProperty("userIds")
    @Valid
    private List<String> userIds = new ArrayList<String>();
    /**
     * Additional Properties
     * 
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Map<String, String> additions = new HashMap<String, String>();

    /**
     * List of internal userIds
     * 
     * @return
     *     The userIds
     */
    @JsonProperty("userIds")
    public List<String> getUserIds() {
        return userIds;
    }

    /**
     * List of internal userIds
     * 
     * @param userIds
     *     The userIds
     */
    @JsonProperty("userIds")
    public void setUserIds(List<String> userIds) {
        this.userIds = userIds;
    }

    public UsersByPermissionsResponseBody withUserIds(List<String> userIds) {
        this.userIds = userIds;
        return this;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(userIds).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof UsersByPermissionsResponseBody) == false) {
            return false;
        }
        UsersByPermissionsResponseBody rhs = ((UsersByPermissionsResponseBody) other);
        return new EqualsBuilder().append(userIds, rhs.userIds).isEquals();
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
