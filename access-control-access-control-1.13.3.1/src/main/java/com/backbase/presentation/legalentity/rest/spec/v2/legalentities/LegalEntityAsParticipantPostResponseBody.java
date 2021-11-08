package com.backbase.presentation.legalentity.rest.spec.v2.legalentities;

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
    "legalEntityId",
    "serviceAgreementId"
})
@JsonIgnoreProperties(ignoreUnknown = true)
public class LegalEntityAsParticipantPostResponseBody implements AdditionalPropertiesAware
{

    /**
     * Universally Unique IDentifier.
     * (Required)
     * 
     */
    @JsonProperty("legalEntityId")
    @Pattern(regexp = "^[0-9a-f]{32}$|^[0-9a-f-]{36}$")
    @Size(min = 1, max = 36)
    @NotNull
    private String legalEntityId;
    /**
     * Universally Unique IDentifier.
     * 
     */
    @JsonProperty("serviceAgreementId")
    @Pattern(regexp = "^[0-9a-f]{32}$|^[0-9a-f-]{36}$")
    @Size(min = 1, max = 36)
    private String serviceAgreementId;
    /**
     * Additional Properties
     * 
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Map<String, String> additions = new HashMap<String, String>();

    /**
     * Universally Unique IDentifier.
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
     * Universally Unique IDentifier.
     * (Required)
     * 
     * @param legalEntityId
     *     The legalEntityId
     */
    @JsonProperty("legalEntityId")
    public void setLegalEntityId(String legalEntityId) {
        this.legalEntityId = legalEntityId;
    }

    public LegalEntityAsParticipantPostResponseBody withLegalEntityId(String legalEntityId) {
        this.legalEntityId = legalEntityId;
        return this;
    }

    /**
     * Universally Unique IDentifier.
     * 
     * @return
     *     The serviceAgreementId
     */
    @JsonProperty("serviceAgreementId")
    public String getServiceAgreementId() {
        return serviceAgreementId;
    }

    /**
     * Universally Unique IDentifier.
     * 
     * @param serviceAgreementId
     *     The serviceAgreementId
     */
    @JsonProperty("serviceAgreementId")
    public void setServiceAgreementId(String serviceAgreementId) {
        this.serviceAgreementId = serviceAgreementId;
    }

    public LegalEntityAsParticipantPostResponseBody withServiceAgreementId(String serviceAgreementId) {
        this.serviceAgreementId = serviceAgreementId;
        return this;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(legalEntityId).append(serviceAgreementId).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof LegalEntityAsParticipantPostResponseBody) == false) {
            return false;
        }
        LegalEntityAsParticipantPostResponseBody rhs = ((LegalEntityAsParticipantPostResponseBody) other);
        return new EqualsBuilder().append(legalEntityId, rhs.legalEntityId).append(serviceAgreementId, rhs.serviceAgreementId).isEquals();
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
