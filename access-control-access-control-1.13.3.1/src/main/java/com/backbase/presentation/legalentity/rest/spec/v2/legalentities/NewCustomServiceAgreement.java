
package com.backbase.presentation.legalentity.rest.spec.v2.legalentities;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Generated;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import com.backbase.buildingblocks.persistence.model.AdditionalPropertiesAware;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
    "serviceAgreementName",
    "serviceAgreementDescription",
    "serviceAgreementExternalId",
    "serviceAgreementValidFromDate",
    "serviceAgreementValidFromTime",
    "serviceAgreementValidUntilDate",
    "serviceAgreementValidUntilTime",
    "serviceAgreementState",
    "participantInfo"
})
public class NewCustomServiceAgreement implements AdditionalPropertiesAware
{

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("serviceAgreementName")
    @Pattern(regexp = "^\\S(.*(\\S))?$")
    @Size(min = 1, max = 128)
    @NotNull
    private String serviceAgreementName;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("serviceAgreementDescription")
    @Size(min = 1, max = 255)
    @NotNull
    private String serviceAgreementDescription;
    /**
     * External legal entity identifier.
     * 
     */
    @JsonProperty("serviceAgreementExternalId")
    @Pattern(regexp = "^[^\\r\\n]{1,64}$")
    @Size(min = 1, max = 64)
    private String serviceAgreementExternalId;
    /**
     * Start date of the service agreement.
     * 
     */
    @JsonProperty("serviceAgreementValidFromDate")
    private String serviceAgreementValidFromDate;
    /**
     * Start time of the service agreement.
     * 
     */
    @JsonProperty("serviceAgreementValidFromTime")
    private String serviceAgreementValidFromTime;
    /**
     * End date of the service agreement.
     * 
     */
    @JsonProperty("serviceAgreementValidUntilDate")
    private String serviceAgreementValidUntilDate;
    /**
     * End time of the service agreement.
     * 
     */
    @JsonProperty("serviceAgreementValidUntilTime")
    private String serviceAgreementValidUntilTime;
    /**
     * Status of the entity
     * 
     */
    @JsonProperty("serviceAgreementState")
    private Status serviceAgreementState;
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
     * 
     * (Required)
     * 
     * @return
     *     The serviceAgreementName
     */
    @JsonProperty("serviceAgreementName")
    public String getServiceAgreementName() {
        return serviceAgreementName;
    }

    /**
     * 
     * (Required)
     * 
     * @param serviceAgreementName
     *     The serviceAgreementName
     */
    @JsonProperty("serviceAgreementName")
    public void setServiceAgreementName(String serviceAgreementName) {
        this.serviceAgreementName = serviceAgreementName;
    }

    public NewCustomServiceAgreement withServiceAgreementName(String serviceAgreementName) {
        this.serviceAgreementName = serviceAgreementName;
        return this;
    }

    /**
     * 
     * (Required)
     * 
     * @return
     *     The serviceAgreementDescription
     */
    @JsonProperty("serviceAgreementDescription")
    public String getServiceAgreementDescription() {
        return serviceAgreementDescription;
    }

    /**
     * 
     * (Required)
     * 
     * @param serviceAgreementDescription
     *     The serviceAgreementDescription
     */
    @JsonProperty("serviceAgreementDescription")
    public void setServiceAgreementDescription(String serviceAgreementDescription) {
        this.serviceAgreementDescription = serviceAgreementDescription;
    }

    public NewCustomServiceAgreement withServiceAgreementDescription(String serviceAgreementDescription) {
        this.serviceAgreementDescription = serviceAgreementDescription;
        return this;
    }

    /**
     * External service agreement identifier.
     * 
     * @return
     *     The serviceAgreementExternalId
     */
    @JsonProperty("serviceAgreementExternalId")
    public String getServiceAgreementExternalId() {
        return serviceAgreementExternalId;
    }

    /**
     * External service agreement identifier.
     * 
     * @param serviceAgreementExternalId
     *     The serviceAgreementExternalId
     */
    @JsonProperty("serviceAgreementExternalId")
    public void setServiceAgreementExternalId(String serviceAgreementExternalId) {
        this.serviceAgreementExternalId = serviceAgreementExternalId;
    }

    public NewCustomServiceAgreement withServiceAgreementExternalId(String serviceAgreementExternalId) {
        this.serviceAgreementExternalId = serviceAgreementExternalId;
        return this;
    }

    /**
     * Start date of the service agreement.
     * 
     * @return
     *     The serviceAgreementValidFromDate
     */
    @JsonProperty("serviceAgreementValidFromDate")
    public String getServiceAgreementValidFromDate() {
        return serviceAgreementValidFromDate;
    }

    /**
     * Start date of the service agreement.
     * 
     * @param serviceAgreementValidFromDate
     *     The serviceAgreementValidFromDate
     */
    @JsonProperty("serviceAgreementValidFromDate")
    public void setServiceAgreementValidFromDate(String serviceAgreementValidFromDate) {
        this.serviceAgreementValidFromDate = serviceAgreementValidFromDate;
    }

    public NewCustomServiceAgreement withServiceAgreementValidFromDate(String serviceAgreementValidFromDate) {
        this.serviceAgreementValidFromDate = serviceAgreementValidFromDate;
        return this;
    }

    /**
     * Start time of the service agreement.
     * 
     * @return
     *     The serviceAgreementValidFromTime
     */
    @JsonProperty("serviceAgreementValidFromTime")
    public String getServiceAgreementValidFromTime() {
        return serviceAgreementValidFromTime;
    }

    /**
     * Start time of the service agreement.
     * 
     * @param serviceAgreementValidFromTime
     *     The serviceAgreementValidFromTime
     */
    @JsonProperty("serviceAgreementValidFromTime")
    public void setServiceAgreementValidFromTime(String serviceAgreementValidFromTime) {
        this.serviceAgreementValidFromTime = serviceAgreementValidFromTime;
    }

    public NewCustomServiceAgreement withServiceAgreementValidFromTime(String serviceAgreementValidFromTime) {
        this.serviceAgreementValidFromTime = serviceAgreementValidFromTime;
        return this;
    }

    /**
     * End date of the service agreement.
     * 
     * @return
     *     The serviceAgreementValidUntilDate
     */
    @JsonProperty("serviceAgreementValidUntilDate")
    public String getServiceAgreementValidUntilDate() {
        return serviceAgreementValidUntilDate;
    }

    /**
     * End date of the service agreement.
     * 
     * @param serviceAgreementValidUntilDate
     *     The serviceAgreementValidUntilDate
     */
    @JsonProperty("serviceAgreementValidUntilDate")
    public void setServiceAgreementValidUntilDate(String serviceAgreementValidUntilDate) {
        this.serviceAgreementValidUntilDate = serviceAgreementValidUntilDate;
    }

    public NewCustomServiceAgreement withServiceAgreementValidUntilDate(String serviceAgreementValidUntilDate) {
        this.serviceAgreementValidUntilDate = serviceAgreementValidUntilDate;
        return this;
    }

    /**
     * End time of the service agreement.
     * 
     * @return
     *     The serviceAgreementValidUntilTime
     */
    @JsonProperty("serviceAgreementValidUntilTime")
    public String getServiceAgreementValidUntilTime() {
        return serviceAgreementValidUntilTime;
    }

    /**
     * End time of the service agreement.
     * 
     * @param serviceAgreementValidUntilTime
     *     The serviceAgreementValidUntilTime
     */
    @JsonProperty("serviceAgreementValidUntilTime")
    public void setServiceAgreementValidUntilTime(String serviceAgreementValidUntilTime) {
        this.serviceAgreementValidUntilTime = serviceAgreementValidUntilTime;
    }

    public NewCustomServiceAgreement withServiceAgreementValidUntilTime(String serviceAgreementValidUntilTime) {
        this.serviceAgreementValidUntilTime = serviceAgreementValidUntilTime;
        return this;
    }

    /**
     * Status of the entity
     * 
     * @return
     *     The serviceAgreementState
     */
    @JsonProperty("serviceAgreementState")
    public Status getServiceAgreementState() {
        return serviceAgreementState;
    }

    /**
     * Status of the entity
     * 
     * @param serviceAgreementState
     *     The serviceAgreementState
     */
    @JsonProperty("serviceAgreementState")
    public void setServiceAgreementState(Status serviceAgreementState) {
        this.serviceAgreementState = serviceAgreementState;
    }

    public NewCustomServiceAgreement withServiceAgreementState(Status serviceAgreementState) {
        this.serviceAgreementState = serviceAgreementState;
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

    public NewCustomServiceAgreement withParticipantInfo(ParticipantInfo participantInfo) {
        this.participantInfo = participantInfo;
        return this;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(serviceAgreementName).append(serviceAgreementDescription).append(serviceAgreementExternalId).append(serviceAgreementValidFromDate).append(serviceAgreementValidFromTime).append(serviceAgreementValidUntilDate).append(serviceAgreementValidUntilTime).append(serviceAgreementState).append(participantInfo).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof NewCustomServiceAgreement) == false) {
            return false;
        }
        NewCustomServiceAgreement rhs = ((NewCustomServiceAgreement) other);
        return new EqualsBuilder().append(serviceAgreementName, rhs.serviceAgreementName).append(serviceAgreementDescription, rhs.serviceAgreementDescription).append(serviceAgreementExternalId, rhs.serviceAgreementExternalId).append(serviceAgreementValidFromDate, rhs.serviceAgreementValidFromDate).append(serviceAgreementValidFromTime, rhs.serviceAgreementValidFromTime).append(serviceAgreementValidUntilDate, rhs.serviceAgreementValidUntilDate).append(serviceAgreementValidUntilTime, rhs.serviceAgreementValidUntilTime).append(serviceAgreementState, rhs.serviceAgreementState).append(participantInfo, rhs.participantInfo).isEquals();
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
