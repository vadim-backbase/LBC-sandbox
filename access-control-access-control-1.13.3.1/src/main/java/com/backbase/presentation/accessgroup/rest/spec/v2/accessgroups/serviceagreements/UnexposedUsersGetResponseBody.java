
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
    "fullName",
    "legalEntityId",
    "legalEntityName"
})
@JsonIgnoreProperties(ignoreUnknown = true)
public class UnexposedUsersGetResponseBody implements AdditionalPropertiesAware
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
     * 
     * (Required)
     * 
     */
    @JsonProperty("fullName")
    @Size(min = 1, max = 128)
    @NotNull
    private String fullName;
    /**
     * Universally Unique Identifier.
     * (Required)
     * 
     */
    @JsonProperty("legalEntityId")
    @Pattern(regexp = "^[0-9a-f]{32}$|^[0-9a-f-]{36}$")
    @Size(min = 1, max = 36)
    @NotNull
    private String legalEntityId;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("legalEntityName")
    @Size(min = 1, max = 128)
    @NotNull
    private String legalEntityName;
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

    public UnexposedUsersGetResponseBody withId(String id) {
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

    public UnexposedUsersGetResponseBody withExternalId(String externalId) {
        this.externalId = externalId;
        return this;
    }

    /**
     * 
     * (Required)
     * 
     * @return
     *     The fullName
     */
    @JsonProperty("fullName")
    public String getFullName() {
        return fullName;
    }

    /**
     * 
     * (Required)
     * 
     * @param fullName
     *     The fullName
     */
    @JsonProperty("fullName")
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public UnexposedUsersGetResponseBody withFullName(String fullName) {
        this.fullName = fullName;
        return this;
    }

    /**
     * Universally Unique Identifier.
     * (Required)
     * 
     * @return
     *     The legalEntityId
     */
    @JsonProperty("legalEntityId")
    public String getLegalEntityId() {
        return legalEntityId;
    }

    /**
     * Universally Unique Identifier.
     * (Required)
     * 
     * @param legalEntityId
     *     The legalEntityId
     */
    @JsonProperty("legalEntityId")
    public void setLegalEntityId(String legalEntityId) {
        this.legalEntityId = legalEntityId;
    }

    public UnexposedUsersGetResponseBody withLegalEntityId(String legalEntityId) {
        this.legalEntityId = legalEntityId;
        return this;
    }

    /**
     * 
     * (Required)
     * 
     * @return
     *     The legalEntityName
     */
    @JsonProperty("legalEntityName")
    public String getLegalEntityName() {
        return legalEntityName;
    }

    /**
     * 
     * (Required)
     * 
     * @param legalEntityName
     *     The legalEntityName
     */
    @JsonProperty("legalEntityName")
    public void setLegalEntityName(String legalEntityName) {
        this.legalEntityName = legalEntityName;
    }

    public UnexposedUsersGetResponseBody withLegalEntityName(String legalEntityName) {
        this.legalEntityName = legalEntityName;
        return this;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(id).append(externalId).append(fullName).append(legalEntityId).append(legalEntityName).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof UnexposedUsersGetResponseBody) == false) {
            return false;
        }
        UnexposedUsersGetResponseBody rhs = ((UnexposedUsersGetResponseBody) other);
        return new EqualsBuilder().append(id, rhs.id).append(externalId, rhs.externalId).append(fullName, rhs.fullName).append(legalEntityId, rhs.legalEntityId).append(legalEntityName, rhs.legalEntityName).isEquals();
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
