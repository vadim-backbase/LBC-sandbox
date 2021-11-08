
package com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Generated;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import com.backbase.buildingblocks.persistence.model.AdditionalPropertiesAware;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Service agreement state legal entities
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
    "name",
    "id",
    "externalId",
    "contributeUsers",
    "contributeAccount"
})
public class ServiceAgreementStateLegalEntities implements AdditionalPropertiesAware
{

    /**
     * Name of the legal entities.
     * 
     */
    @JsonProperty("name")
    private String name;
    /**
     * Universally Unique Identifier.
     * 
     */
    @JsonProperty("id")
    @Pattern(regexp = "^[0-9a-f]{32}$|^[0-9a-f-]{36}$")
    @Size(min = 1, max = 36)
    private String id;
    /**
     * External Unique Identifier.
     * 
     */
    @JsonProperty("externalId")
    @Pattern(regexp = "^[^\\r\\n]{1,64}$")
    @Size(min = 1, max = 64)
    private String externalId;
    /**
     * true if legal entities shares users.
     * 
     */
    @JsonProperty("contributeUsers")
    private Boolean contributeUsers;
    /**
     * true if legal entities shares account.
     * 
     */
    @JsonProperty("contributeAccount")
    private Boolean contributeAccount;
    /**
     * Additional Properties
     * 
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Map<String, String> additions = new HashMap<String, String>();

    /**
     * Name of the legal entities.
     * 
     * @return
     *     The name
     */
    @JsonProperty("name")
    public String getName() {
        return name;
    }

    /**
     * Name of the legal entities.
     * 
     * @param name
     *     The name
     */
    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    public ServiceAgreementStateLegalEntities withName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Universally Unique Identifier.
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
     * 
     * @param id
     *     The id
     */
    @JsonProperty("id")
    public void setId(String id) {
        this.id = id;
    }

    public ServiceAgreementStateLegalEntities withId(String id) {
        this.id = id;
        return this;
    }

    /**
     * External Unique Identifier.
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
     * 
     * @param externalId
     *     The externalId
     */
    @JsonProperty("externalId")
    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public ServiceAgreementStateLegalEntities withExternalId(String externalId) {
        this.externalId = externalId;
        return this;
    }

    /**
     * true if legal entities shares users.
     * 
     * @return
     *     The contributeUsers
     */
    @JsonProperty("contributeUsers")
    public Boolean getContributeUsers() {
        return contributeUsers;
    }

    /**
     * true if legal entities shares users.
     * 
     * @param contributeUsers
     *     The contributeUsers
     */
    @JsonProperty("contributeUsers")
    public void setContributeUsers(Boolean contributeUsers) {
        this.contributeUsers = contributeUsers;
    }

    public ServiceAgreementStateLegalEntities withContributeUsers(Boolean contributeUsers) {
        this.contributeUsers = contributeUsers;
        return this;
    }

    /**
     * true if legal entities shares account.
     * 
     * @return
     *     The contributeAccount
     */
    @JsonProperty("contributeAccount")
    public Boolean getContributeAccount() {
        return contributeAccount;
    }

    /**
     * true if legal entities shares account.
     * 
     * @param contributeAccount
     *     The contributeAccount
     */
    @JsonProperty("contributeAccount")
    public void setContributeAccount(Boolean contributeAccount) {
        this.contributeAccount = contributeAccount;
    }

    public ServiceAgreementStateLegalEntities withContributeAccount(Boolean contributeAccount) {
        this.contributeAccount = contributeAccount;
        return this;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(name).append(id).append(externalId).append(contributeUsers).append(contributeAccount).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ServiceAgreementStateLegalEntities) == false) {
            return false;
        }
        ServiceAgreementStateLegalEntities rhs = ((ServiceAgreementStateLegalEntities) other);
        return new EqualsBuilder().append(name, rhs.name).append(id, rhs.id).append(externalId, rhs.externalId).append(contributeUsers, rhs.contributeUsers).append(contributeAccount, rhs.contributeAccount).isEquals();
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
