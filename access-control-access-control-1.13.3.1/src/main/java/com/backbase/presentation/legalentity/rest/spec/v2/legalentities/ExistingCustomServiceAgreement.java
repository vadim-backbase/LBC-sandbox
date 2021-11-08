
package com.backbase.presentation.legalentity.rest.spec.v2.legalentities;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Generated;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import com.backbase.buildingblocks.persistence.model.AdditionalPropertiesAware;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
    "serviceAgreementId",
    "participantInfo"
})
public class ExistingCustomServiceAgreement implements AdditionalPropertiesAware
{

    /**
     * Universally Unique IDentifier.
     * (Required)
     * 
     */
    @JsonProperty("serviceAgreementId")
    @Pattern(regexp = "^[0-9a-f]{32}$|^[0-9a-f-]{36}$")
    @Size(min = 1, max = 36)
    @NotNull
    private String serviceAgreementId;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("participantInfo")
    @Valid
    @NotNull
    private ParticipantInfo participantInfo;
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
     *     The serviceAgreementId
     */
    @JsonProperty("serviceAgreementId")
    public String getServiceAgreementId() {
        return serviceAgreementId;
    }

    /**
     * Universally Unique IDentifier.
     * (Required)
     * 
     * @param serviceAgreementId
     *     The serviceAgreementId
     */
    @JsonProperty("serviceAgreementId")
    public void setServiceAgreementId(String serviceAgreementId) {
        this.serviceAgreementId = serviceAgreementId;
    }

    public ExistingCustomServiceAgreement withServiceAgreementId(String serviceAgreementId) {
        this.serviceAgreementId = serviceAgreementId;
        return this;
    }

    /**
     * 
     * (Required)
     * 
     * @return
     *     The participantInfo
     */
    @JsonProperty("participantInfo")
    public ParticipantInfo getParticipantInfo() {
        return participantInfo;
    }

    /**
     * 
     * (Required)
     * 
     * @param participantInfo
     *     The participantInfo
     */
    @JsonProperty("participantInfo")
    public void setParticipantInfo(ParticipantInfo participantInfo) {
        this.participantInfo = participantInfo;
    }

    public ExistingCustomServiceAgreement withParticipantInfo(ParticipantInfo participantInfo) {
        this.participantInfo = participantInfo;
        return this;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(serviceAgreementId).append(participantInfo).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ExistingCustomServiceAgreement) == false) {
            return false;
        }
        ExistingCustomServiceAgreement rhs = ((ExistingCustomServiceAgreement) other);
        return new EqualsBuilder().append(serviceAgreementId, rhs.serviceAgreementId).append(participantInfo, rhs.participantInfo).isEquals();
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
