
package com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
    "name",
    "description",
    "participants",
    "validFromDate",
    "validFromTime",
    "validUntilDate",
    "validUntilTime",
    "status"
})
@JsonIgnoreProperties(ignoreUnknown = true)
public class ServiceAgreementPostRequestBody implements AdditionalPropertiesAware
{

    /**
     * Service Agreement name
     * (Required)
     * 
     */
    @JsonProperty("name")
    @Pattern(regexp = "^\\S(.*(\\S))?$")
    @Size(min = 1, max = 128)
    @NotNull
    private String name;
    /**
     * Service Agreement description
     * (Required)
     * 
     */
    @JsonProperty("description")
    @Pattern(regexp = "^(\\S|\\n)((.|\\n)*(\\S|\\n))?$")
    @Size(min = 1, max = 255)
    @NotNull
    private String description;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("participants")
    @Size(min = 1)
    @Valid
    @NotNull
    private List<com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.Participant> participants = new ArrayList<com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.Participant>();
    /**
     * Start date of the service agreement.
     * 
     */
    @JsonProperty("validFromDate")
    private String validFromDate;
    /**
     * Start time of the service agreement.
     * 
     */
    @JsonProperty("validFromTime")
    private String validFromTime;
    /**
     * End date of the service agreement.
     * 
     */
    @JsonProperty("validUntilDate")
    private String validUntilDate;
    /**
     * End time of the service agreement.
     * 
     */
    @JsonProperty("validUntilTime")
    private String validUntilTime;
    /**
     * Status of the entity
     * 
     */
    @JsonProperty("status")
    private com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.CreateStatus status = com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.CreateStatus
        .fromValue("ENABLED");
    /**
     * Additional Properties
     * 
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Map<String, String> additions = new HashMap<String, String>();

    /**
     * Service Agreement name
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
     * Service Agreement name
     * (Required)
     * 
     * @param name
     *     The name
     */
    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    public ServiceAgreementPostRequestBody withName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Service Agreement description
     * (Required)
     * 
     * @return
     *     The description
     */
    @JsonProperty("description")
    public String getDescription() {
        return description;
    }

    /**
     * Service Agreement description
     * (Required)
     * 
     * @param description
     *     The description
     */
    @JsonProperty("description")
    public void setDescription(String description) {
        this.description = description;
    }

    public ServiceAgreementPostRequestBody withDescription(String description) {
        this.description = description;
        return this;
    }

    /**
     * 
     * (Required)
     * 
     * @return
     *     The participants
     */
    @JsonProperty("participants")
    public List<com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.Participant> getParticipants() {
        return participants;
    }

    /**
     * 
     * (Required)
     * 
     * @param participants
     *     The participants
     */
    @JsonProperty("participants")
    public void setParticipants(List<com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.Participant> participants) {
        this.participants = participants;
    }

    public ServiceAgreementPostRequestBody withParticipants(List<Participant> participants) {
        this.participants = participants;
        return this;
    }

    /**
     * Start date of the service agreement.
     * 
     * @return
     *     The validFromDate
     */
    @JsonProperty("validFromDate")
    public String getValidFromDate() {
        return validFromDate;
    }

    /**
     * Start date of the service agreement.
     * 
     * @param validFromDate
     *     The validFromDate
     */
    @JsonProperty("validFromDate")
    public void setValidFromDate(String validFromDate) {
        this.validFromDate = validFromDate;
    }

    public ServiceAgreementPostRequestBody withValidFromDate(String validFromDate) {
        this.validFromDate = validFromDate;
        return this;
    }

    /**
     * Start time of the service agreement.
     * 
     * @return
     *     The validFromTime
     */
    @JsonProperty("validFromTime")
    public String getValidFromTime() {
        return validFromTime;
    }

    /**
     * Start time of the service agreement.
     * 
     * @param validFromTime
     *     The validFromTime
     */
    @JsonProperty("validFromTime")
    public void setValidFromTime(String validFromTime) {
        this.validFromTime = validFromTime;
    }

    public ServiceAgreementPostRequestBody withValidFromTime(String validFromTime) {
        this.validFromTime = validFromTime;
        return this;
    }

    /**
     * End date of the service agreement.
     * 
     * @return
     *     The validUntilDate
     */
    @JsonProperty("validUntilDate")
    public String getValidUntilDate() {
        return validUntilDate;
    }

    /**
     * End date of the service agreement.
     * 
     * @param validUntilDate
     *     The validUntilDate
     */
    @JsonProperty("validUntilDate")
    public void setValidUntilDate(String validUntilDate) {
        this.validUntilDate = validUntilDate;
    }

    public ServiceAgreementPostRequestBody withValidUntilDate(String validUntilDate) {
        this.validUntilDate = validUntilDate;
        return this;
    }

    /**
     * End time of the service agreement.
     * 
     * @return
     *     The validUntilTime
     */
    @JsonProperty("validUntilTime")
    public String getValidUntilTime() {
        return validUntilTime;
    }

    /**
     * End time of the service agreement.
     * 
     * @param validUntilTime
     *     The validUntilTime
     */
    @JsonProperty("validUntilTime")
    public void setValidUntilTime(String validUntilTime) {
        this.validUntilTime = validUntilTime;
    }

    public ServiceAgreementPostRequestBody withValidUntilTime(String validUntilTime) {
        this.validUntilTime = validUntilTime;
        return this;
    }

    /**
     * Status of the entity
     * 
     * @return
     *     The status
     */
    @JsonProperty("status")
    public com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.CreateStatus getStatus() {
        return status;
    }

    /**
     * Status of the entity
     * 
     * @param status
     *     The status
     */
    @JsonProperty("status")
    public void setStatus(
        com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.CreateStatus status) {
        this.status = status;
    }

    public ServiceAgreementPostRequestBody withStatus(CreateStatus status) {
        this.status = status;
        return this;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(name).append(description).append(participants).append(validFromDate).append(validFromTime).append(validUntilDate).append(validUntilTime).append(status).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ServiceAgreementPostRequestBody) == false) {
            return false;
        }
        ServiceAgreementPostRequestBody rhs = ((ServiceAgreementPostRequestBody) other);
        return new EqualsBuilder().append(name, rhs.name).append(description, rhs.description).append(participants, rhs.participants).append(validFromDate, rhs.validFromDate).append(validFromTime, rhs.validFromTime).append(validUntilDate, rhs.validUntilDate).append(validUntilTime, rhs.validUntilTime).append(status, rhs.status).isEquals();
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
