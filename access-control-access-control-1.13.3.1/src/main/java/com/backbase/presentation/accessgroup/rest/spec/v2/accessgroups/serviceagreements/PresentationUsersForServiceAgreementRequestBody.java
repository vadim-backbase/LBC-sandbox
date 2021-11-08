
package com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Generated;
import javax.validation.Valid;
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
    "users"
})
@JsonIgnoreProperties(ignoreUnknown = true)
public class PresentationUsersForServiceAgreementRequestBody implements AdditionalPropertiesAware
{

    @JsonProperty("users")
    @Size(min = 1)
    @Valid
    private List<String> users = new ArrayList<String>();
    /**
     * Additional Properties
     * 
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Map<String, String> additions = new HashMap<String, String>();

    /**
     * 
     * @return
     *     The users
     */
    @JsonProperty("users")
    public List<String> getUsers() {
        return users;
    }

    /**
     * 
     * @param users
     *     The users
     */
    @JsonProperty("users")
    public void setUsers(List<String> users) {
        this.users = users;
    }

    public PresentationUsersForServiceAgreementRequestBody withUsers(List<String> users) {
        this.users = users;
        return this;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(users).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof PresentationUsersForServiceAgreementRequestBody) == false) {
            return false;
        }
        PresentationUsersForServiceAgreementRequestBody rhs = ((PresentationUsersForServiceAgreementRequestBody) other);
        return new EqualsBuilder().append(users, rhs.users).isEquals();
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
