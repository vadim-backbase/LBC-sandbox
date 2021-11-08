
package com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.serviceagreements;

import com.backbase.buildingblocks.persistence.model.AdditionalPropertiesAware;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Generated;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Legal Entity participant item
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
    "id",
    "externalId",
    "name",
    "sharingUsers",
    "sharingAccounts"
})
@JsonIgnoreProperties(ignoreUnknown = true)
public class Participant implements AdditionalPropertiesAware
{

    /**
     * Universally Unique Identifier.
     * (Required)
     * 
     */
    @JsonProperty("id")
    @Pattern(regexp = "^[0-9a-f]{32}$|^[0-9a-f-]{36}$")
    @NotNull
    private String id;
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
     * Name of the Legal Entity
     * (Required)
     * 
     */
    @JsonProperty("name")
    @NotNull
    private String name;
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

    public Participant withExternalId(String externalId) {
        this.externalId = externalId;
        return this;
    }

    /**
     * Name of the Legal Entity
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
     * Name of the Legal Entity
     * (Required)
     * 
     * @param name
     *     The name
     */
    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    public Participant withName(String name) {
        this.name = name;
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

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(id).append(externalId).append(name).append(sharingUsers).append(sharingAccounts).toHashCode();
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
        return new EqualsBuilder().append(id, rhs.id).append(externalId, rhs.externalId).append(name, rhs.name).append(sharingUsers, rhs.sharingUsers).append(sharingAccounts, rhs.sharingAccounts).isEquals();
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
