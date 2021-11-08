
package com.backbase.presentation.legalentity.rest.spec.v2.legalentities;

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
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
    "externalIds",
    "accessToken"
})
@JsonIgnoreProperties(ignoreUnknown = true)
public class PresentationBatchDeleteLegalEntities implements AdditionalPropertiesAware
{

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("externalIds")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    @Size(min = 1)
    @Valid
    @NotNull
    private Set<String> externalIds = new LinkedHashSet<String>();
    /**
     * Universally Unique IDentifier.
     * 
     */
    @JsonProperty("accessToken")
    @Pattern(regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$")
    @Size(min = 1, max = 36)
    private String accessToken;
    /**
     * Additional Properties
     * 
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Map<String, String> additions = new HashMap<String, String>();

    /**
     * 
     * (Required)
     * 
     * @return
     *     The externalIds
     */
    @JsonProperty("externalIds")
    public Set<String> getExternalIds() {
        return externalIds;
    }

    /**
     * 
     * (Required)
     * 
     * @param externalIds
     *     The externalIds
     */
    @JsonProperty("externalIds")
    public void setExternalIds(Set<String> externalIds) {
        this.externalIds = externalIds;
    }

    public PresentationBatchDeleteLegalEntities withExternalIds(Set<String> externalIds) {
        this.externalIds = externalIds;
        return this;
    }

    /**
     * Universally Unique IDentifier.
     * 
     * @return
     *     The accessToken
     */
    @JsonProperty("accessToken")
    public String getAccessToken() {
        return accessToken;
    }

    /**
     * Universally Unique IDentifier.
     * 
     * @param accessToken
     *     The accessToken
     */
    @JsonProperty("accessToken")
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public PresentationBatchDeleteLegalEntities withAccessToken(String accessToken) {
        this.accessToken = accessToken;
        return this;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(externalIds).append(accessToken).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof PresentationBatchDeleteLegalEntities) == false) {
            return false;
        }
        PresentationBatchDeleteLegalEntities rhs = ((PresentationBatchDeleteLegalEntities) other);
        return new EqualsBuilder().append(externalIds, rhs.externalIds).append(accessToken, rhs.accessToken).isEquals();
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
