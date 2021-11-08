
package com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Generated;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import com.backbase.buildingblocks.persistence.model.AdditionalPropertiesAware;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.PresentationAction;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Payload for batch adding or removing admins/users to service agreement
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
    "action",
    "users"
})
@JsonIgnoreProperties(ignoreUnknown = true)
public class PresentationServiceAgreementUsersUpdate implements AdditionalPropertiesAware
{

    /**
     * Presentation action
     * (Required)
     * 
     */
    @JsonProperty("action")
    @NotNull
    private PresentationAction action;
    @JsonProperty("users")
    @Size(min = 1)
    @Valid
    private List<com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.PresentationServiceAgreementUserPair> users = new ArrayList<com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.PresentationServiceAgreementUserPair>();
    /**
     * Additional Properties
     * 
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Map<String, String> additions = new HashMap<String, String>();

    /**
     * Presentation action
     * (Required)
     * 
     * @return
     *     The action
     */
    @JsonProperty("action")
    public PresentationAction getAction() {
        return action;
    }

    /**
     * Presentation action
     * (Required)
     * 
     * @param action
     *     The action
     */
    @JsonProperty("action")
    public void setAction(PresentationAction action) {
        this.action = action;
    }

    public PresentationServiceAgreementUsersUpdate withAction(PresentationAction action) {
        this.action = action;
        return this;
    }

    /**
     * 
     * @return
     *     The users
     */
    @JsonProperty("users")
    public List<com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.PresentationServiceAgreementUserPair> getUsers() {
        return users;
    }

    /**
     * 
     * @param users
     *     The users
     */
    @JsonProperty("users")
    public void setUsers(List<com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.PresentationServiceAgreementUserPair> users) {
        this.users = users;
    }

    public PresentationServiceAgreementUsersUpdate withUsers(List<PresentationServiceAgreementUserPair> users) {
        this.users = users;
        return this;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(action).append(users).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof PresentationServiceAgreementUsersUpdate) == false) {
            return false;
        }
        PresentationServiceAgreementUsersUpdate rhs = ((PresentationServiceAgreementUsersUpdate) other);
        return new EqualsBuilder().append(action, rhs.action).append(users, rhs.users).isEquals();
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
