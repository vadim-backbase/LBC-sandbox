
package com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Generated;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
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
    "id",
    "externalId",
    "name",
    "sharingUsers",
    "sharingAccounts"
})
@JsonIgnoreProperties(ignoreUnknown = true)
public class ServiceAgreementParticipantsGetResponseBody implements AdditionalPropertiesAware
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
    @Size(min = 1, max = 128)
    @NotNull
    private String name;
    /**
     * Defines if the Legal Entity shares Users in the Service Agreement
     * (Required)
     * 
     */
    @JsonProperty("sharingUsers")
    @NotNull
    private Boolean sharingUsers;
    /**
     * Defines if the Legal Entity shares Accounts in the Service Agreement
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

    public ServiceAgreementParticipantsGetResponseBody withId(String id) {
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

    public ServiceAgreementParticipantsGetResponseBody withExternalId(String externalId) {
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

    public ServiceAgreementParticipantsGetResponseBody withName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Defines if the Legal Entity shares Users in the Service Agreement
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
     * Defines if the Legal Entity shares Users in the Service Agreement
     * (Required)
     * 
     * @param sharingUsers
     *     The sharingUsers
     */
    @JsonProperty("sharingUsers")
    public void setSharingUsers(Boolean sharingUsers) {
        this.sharingUsers = sharingUsers;
    }

    public ServiceAgreementParticipantsGetResponseBody withSharingUsers(Boolean sharingUsers) {
        this.sharingUsers = sharingUsers;
        return this;
    }

    /**
     * Defines if the Legal Entity shares Accounts in the Service Agreement
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
     * Defines if the Legal Entity shares Accounts in the Service Agreement
     * (Required)
     * 
     * @param sharingAccounts
     *     The sharingAccounts
     */
    @JsonProperty("sharingAccounts")
    public void setSharingAccounts(Boolean sharingAccounts) {
        this.sharingAccounts = sharingAccounts;
    }

    public ServiceAgreementParticipantsGetResponseBody withSharingAccounts(Boolean sharingAccounts) {
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
        if ((other instanceof ServiceAgreementParticipantsGetResponseBody) == false) {
            return false;
        }
        ServiceAgreementParticipantsGetResponseBody rhs = ((ServiceAgreementParticipantsGetResponseBody) other);
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
