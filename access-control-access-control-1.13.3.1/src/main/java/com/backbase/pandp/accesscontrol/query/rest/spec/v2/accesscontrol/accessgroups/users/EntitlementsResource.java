
package com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.users;

import com.backbase.buildingblocks.persistence.model.AdditionalPropertiesAware;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Generated;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
    "contextServiceAgreementId",
    "userLegalEntityId",
    "legalEntityIds",
    "accessResourceType"
})
@JsonIgnoreProperties(ignoreUnknown = true)
public class EntitlementsResource implements AdditionalPropertiesAware
{

    /**
     * Universally Unique Identifier.
     * 
     */
    @JsonProperty("contextServiceAgreementId")
    @Pattern(regexp = "^[0-9a-f]{32}$|^[0-9a-f-]{36}$")
    private String contextServiceAgreementId;
    /**
     * Universally Unique Identifier.
     * (Required)
     * 
     */
    @JsonProperty("userLegalEntityId")
    @Pattern(regexp = "^[0-9a-f]{32}$|^[0-9a-f-]{36}$")
    @NotNull
    private String userLegalEntityId;
    /**
     * List of legal entity ids to check
     * (Required)
     * 
     */
    @JsonProperty("legalEntityIds")
    @Valid
    @NotNull
    private List<String> legalEntityIds = new ArrayList<String>();
    /**
     * Type of the resource
     * (Required)
     * 
     */
    @JsonProperty("accessResourceType")
    @NotNull
    private AccessResourceType accessResourceType;
    /**
     * Additional Properties
     * 
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Map<String, String> additions = new HashMap<String, String>();

    /**
     * Universally Unique Identifier.
     * 
     * @return
     *     The contextServiceAgreementId
     */
    @JsonProperty("contextServiceAgreementId")
    public String getContextServiceAgreementId() {
        return contextServiceAgreementId;
    }

    /**
     * Universally Unique Identifier.
     * 
     * @param contextServiceAgreementId
     *     The contextServiceAgreementId
     */
    @JsonProperty("contextServiceAgreementId")
    public void setContextServiceAgreementId(String contextServiceAgreementId) {
        this.contextServiceAgreementId = contextServiceAgreementId;
    }

    public EntitlementsResource withContextServiceAgreementId(String contextServiceAgreementId) {
        this.contextServiceAgreementId = contextServiceAgreementId;
        return this;
    }

    /**
     * Universally Unique Identifier.
     * (Required)
     * 
     * @return
     *     The userLegalEntityId
     */
    @JsonProperty("userLegalEntityId")
    public String getUserLegalEntityId() {
        return userLegalEntityId;
    }

    /**
     * Universally Unique Identifier.
     * (Required)
     * 
     * @param userLegalEntityId
     *     The userLegalEntityId
     */
    @JsonProperty("userLegalEntityId")
    public void setUserLegalEntityId(String userLegalEntityId) {
        this.userLegalEntityId = userLegalEntityId;
    }

    public EntitlementsResource withUserLegalEntityId(String userLegalEntityId) {
        this.userLegalEntityId = userLegalEntityId;
        return this;
    }

    /**
     * List of legal entity ids to check
     * (Required)
     * 
     * @return
     *     The legalEntityIds
     */
    @JsonProperty("legalEntityIds")
    public List<String> getLegalEntityIds() {
        return legalEntityIds;
    }

    /**
     * List of legal entity ids to check
     * (Required)
     * 
     * @param legalEntityIds
     *     The legalEntityIds
     */
    @JsonProperty("legalEntityIds")
    public void setLegalEntityIds(List<String> legalEntityIds) {
        this.legalEntityIds = legalEntityIds;
    }

    public EntitlementsResource withLegalEntityIds(List<String> legalEntityIds) {
        this.legalEntityIds = legalEntityIds;
        return this;
    }

    /**
     * Type of the resource
     * (Required)
     * 
     * @return
     *     The accessResourceType
     */
    @JsonProperty("accessResourceType")
    public AccessResourceType getAccessResourceType() {
        return accessResourceType;
    }

    /**
     * Type of the resource
     * (Required)
     * 
     * @param accessResourceType
     *     The accessResourceType
     */
    @JsonProperty("accessResourceType")
    public void setAccessResourceType(AccessResourceType accessResourceType) {
        this.accessResourceType = accessResourceType;
    }

    public EntitlementsResource withAccessResourceType(AccessResourceType accessResourceType) {
        this.accessResourceType = accessResourceType;
        return this;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(contextServiceAgreementId).append(userLegalEntityId).append(legalEntityIds).append(accessResourceType).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof EntitlementsResource) == false) {
            return false;
        }
        EntitlementsResource rhs = ((EntitlementsResource) other);
        return new EqualsBuilder().append(contextServiceAgreementId, rhs.contextServiceAgreementId).append(userLegalEntityId, rhs.userLegalEntityId).append(legalEntityIds, rhs.legalEntityIds).append(accessResourceType, rhs.accessResourceType).isEquals();
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
