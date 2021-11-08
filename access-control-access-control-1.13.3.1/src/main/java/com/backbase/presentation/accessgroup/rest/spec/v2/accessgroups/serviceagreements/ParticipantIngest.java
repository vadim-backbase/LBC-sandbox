
package com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import javax.annotation.Generated;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import com.backbase.buildingblocks.persistence.model.AdditionalPropertiesAware;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
    "externalId",
    "sharingUsers",
    "sharingAccounts",
    "admins",
    "users"
})
public class ParticipantIngest implements AdditionalPropertiesAware
{

    /**
     * External Unique Identifier.
     * (Required)
     * 
     */
    @JsonProperty("externalId")
    @Pattern(regexp = "^[^\\r\\n]{1,64}$")
    @Size(min = 1, max = 64)
    @NotNull
    private String externalId;
    /**
     * Boolean value if the Participant is sharing users
     * (Required)
     * 
     */
    @JsonProperty("sharingUsers")
    @NotNull
    private Boolean sharingUsers;
    /**
     * Boolean value if the participant is sharing accounts
     * (Required)
     * 
     */
    @JsonProperty("sharingAccounts")
    @NotNull
    private Boolean sharingAccounts;
    @JsonProperty("admins")
    @JsonDeserialize(as = LinkedHashSet.class)
    @Valid
    private Set<String> admins = new LinkedHashSet<String>();
    /**
     * User external ids
     * 
     */
    @JsonProperty("users")
    @JsonDeserialize(as = LinkedHashSet.class)
    @Valid
    private Set<String> users = new LinkedHashSet<String>();
    /**
     * Additional Properties
     * 
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Map<String, String> additions = new HashMap<String, String>();

    /**
     * External Unique Identifier.
     * (Required)
     * 
     * @return
     *     The externalId
     */
    @JsonProperty("externalId")
    public String getExternalId() {
        return externalId;
    }

    /**
     * External Unique Identifier.
     * (Required)
     * 
     * @param externalId
     *     The externalId
     */
    @JsonProperty("externalId")
    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public ParticipantIngest withExternalId(String externalId) {
        this.externalId = externalId;
        return this;
    }

    /**
     * Boolean value if the Participant is sharing users
     * (Required)
     * 
     * @return
     *     The sharingUsers
     */
    @JsonProperty("sharingUsers")
    public Boolean getSharingUsers() {
        return sharingUsers;
    }

    /**
     * Boolean value if the Participant is sharing users
     * (Required)
     * 
     * @param sharingUsers
     *     The sharingUsers
     */
    @JsonProperty("sharingUsers")
    public void setSharingUsers(Boolean sharingUsers) {
        this.sharingUsers = sharingUsers;
    }

    public ParticipantIngest withSharingUsers(Boolean sharingUsers) {
        this.sharingUsers = sharingUsers;
        return this;
    }

    /**
     * Boolean value if the participant is sharing accounts
     * (Required)
     * 
     * @return
     *     The sharingAccounts
     */
    @JsonProperty("sharingAccounts")
    public Boolean getSharingAccounts() {
        return sharingAccounts;
    }

    /**
     * Boolean value if the participant is sharing accounts
     * (Required)
     * 
     * @param sharingAccounts
     *     The sharingAccounts
     */
    @JsonProperty("sharingAccounts")
    public void setSharingAccounts(Boolean sharingAccounts) {
        this.sharingAccounts = sharingAccounts;
    }

    public ParticipantIngest withSharingAccounts(Boolean sharingAccounts) {
        this.sharingAccounts = sharingAccounts;
        return this;
    }

    /**
     * 
     * @return
     *     The admins
     */
    @JsonProperty("admins")
    public Set<String> getAdmins() {
        return admins;
    }

    /**
     * 
     * @param admins
     *     The admins
     */
    @JsonProperty("admins")
    public void setAdmins(Set<String> admins) {
        this.admins = admins;
    }

    public ParticipantIngest withAdmins(Set<String> admins) {
        this.admins = admins;
        return this;
    }

    /**
     * User external ids
     * 
     * @return
     *     The users
     */
    @JsonProperty("users")
    public Set<String> getUsers() {
        return users;
    }

    /**
     * User external ids
     * 
     * @param users
     *     The users
     */
    @JsonProperty("users")
    public void setUsers(Set<String> users) {
        this.users = users;
    }

    public ParticipantIngest withUsers(Set<String> users) {
        this.users = users;
        return this;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(externalId).append(sharingUsers).append(sharingAccounts).append(admins).append(users).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ParticipantIngest) == false) {
            return false;
        }
        ParticipantIngest rhs = ((ParticipantIngest) other);
        return new EqualsBuilder().append(externalId, rhs.externalId).append(sharingUsers, rhs.sharingUsers).append(sharingAccounts, rhs.sharingAccounts).append(admins, rhs.admins).append(users, rhs.users).isEquals();
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
