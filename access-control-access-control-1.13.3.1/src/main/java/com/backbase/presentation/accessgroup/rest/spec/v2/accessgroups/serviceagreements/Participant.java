
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
    "id",
    "sharingUsers",
    "sharingAccounts",
    "admins"
})
public class Participant implements AdditionalPropertiesAware
{

    /**
     * Universally Unique Identifier.
     * (Required)
     * 
     */
    @JsonProperty("id")
    @Pattern(regexp = "^[0-9a-f]{32}$|^[0-9a-f-]{36}$")
    @Size(min = 1, max = 36)
    @NotNull
    private String id;
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
     * Additional Properties
     * 
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Map<String, String> additions = new HashMap<String, String>();

    /**
     * Universally Unique Identifier.
     * (Required)
     * 
     * @return
     *     The id
     */
    @JsonProperty("id")
    public String getId() {
        return id;
    }

    /**
     * Universally Unique Identifier.
     * (Required)
     * 
     * @param id
     *     The id
     */
    @JsonProperty("id")
    public void setId(String id) {
        this.id = id;
    }

    public Participant withId(String id) {
        this.id = id;
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

    public Participant withSharingUsers(Boolean sharingUsers) {
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

    public Participant withSharingAccounts(Boolean sharingAccounts) {
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

    public Participant withAdmins(Set<String> admins) {
        this.admins = admins;
        return this;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(id).append(sharingUsers).append(sharingAccounts).append(admins).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Participant) == false) {
            return false;
        }
        Participant rhs = ((Participant) other);
        return new EqualsBuilder().append(id, rhs.id).append(sharingUsers, rhs.sharingUsers).append(sharingAccounts, rhs.sharingAccounts).append(admins, rhs.admins).isEquals();
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
