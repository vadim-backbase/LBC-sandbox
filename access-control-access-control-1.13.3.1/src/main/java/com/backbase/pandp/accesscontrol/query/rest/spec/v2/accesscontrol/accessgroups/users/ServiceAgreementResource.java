
package com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.users;

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
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
    "contextServiceAgreementId",
    "serviceAgreementId",
    "userLegalEntityId",
    "accessResourceType"
})
@JsonIgnoreProperties(ignoreUnknown = true)
public class ServiceAgreementResource implements AdditionalPropertiesAware
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
    @JsonProperty("serviceAgreementId")
    @Pattern(regexp = "^[0-9a-f]{32}$|^[0-9a-f-]{36}$")
    @NotNull
    private String serviceAgreementId;
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

    public ServiceAgreementResource withContextServiceAgreementId(String contextServiceAgreementId) {
        this.contextServiceAgreementId = contextServiceAgreementId;
        return this;
    }

    /**
     * Universally Unique Identifier.
     * (Required)
     * 
     * @return
     *     The serviceAgreementId
     */
    @JsonProperty("serviceAgreementId")
    public String getServiceAgreementId() {
        return serviceAgreementId;
    }

    /**
     * Universally Unique Identifier.
     * (Required)
     * 
     * @param serviceAgreementId
     *     The serviceAgreementId
     */
    @JsonProperty("serviceAgreementId")
    public void setServiceAgreementId(String serviceAgreementId) {
        this.serviceAgreementId = serviceAgreementId;
    }

    public ServiceAgreementResource withServiceAgreementId(String serviceAgreementId) {
        this.serviceAgreementId = serviceAgreementId;
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

    public ServiceAgreementResource withUserLegalEntityId(String userLegalEntityId) {
        this.userLegalEntityId = userLegalEntityId;
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

    public ServiceAgreementResource withAccessResourceType(AccessResourceType accessResourceType) {
        this.accessResourceType = accessResourceType;
        return this;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(contextServiceAgreementId).append(serviceAgreementId).append(userLegalEntityId).append(accessResourceType).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ServiceAgreementResource) == false) {
            return false;
        }
        ServiceAgreementResource rhs = ((ServiceAgreementResource) other);
        return new EqualsBuilder().append(contextServiceAgreementId, rhs.contextServiceAgreementId).append(serviceAgreementId, rhs.serviceAgreementId).append(userLegalEntityId, rhs.userLegalEntityId).append(accessResourceType, rhs.accessResourceType).isEquals();
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
