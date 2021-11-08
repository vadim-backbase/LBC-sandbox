
package com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements;

import com.backbase.buildingblocks.persistence.model.AdditionalPropertiesAware;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.aps.PresentationUserApsIdentifiers;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import javax.annotation.Generated;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
    "name",
    "externalId",
    "description",
    "participantsToIngest",
    "validFromDate",
    "validFromTime",
    "validUntilDate",
    "validUntilTime",
    "status",
    "isMaster",
    "regularUserAps",
    "adminUserAps",
    "creatorLegalEntity"
})
@JsonIgnoreProperties(ignoreUnknown = true)
public class ServiceAgreementIngestPostRequestBody implements AdditionalPropertiesAware
{

    /**
     * The service agreement name
     * (Required)
     * 
     */
    @JsonProperty("name")
    @Pattern(regexp = "^\\S(.*(\\S))?$")
    @Size(min = 1, max = 128)
    @NotNull
    private String name;
    /**
     * External Unique Identifier.
     * 
     */
    @JsonProperty("externalId")
    @Pattern(regexp = "^[^\\r\\n]{1,64}$")
    @Size(min = 1, max = 64)
    private String externalId;
    /**
     * Description
     * (Required)
     * 
     */
    @JsonProperty("description")
    @Pattern(regexp = "^(\\S|\\n)((.|\\n)*(\\S|\\n))?$")
    @Size(min = 1, max = 255)
    @NotNull
    private String description;
    /**
     * Participants of the service agreement
     * (Required)
     * 
     */
    @JsonProperty("participantsToIngest")
    @JsonDeserialize(as = LinkedHashSet.class)
    @Size(min = 0)
    @Valid
    @NotNull
    private Set<com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ParticipantIngest> participantsToIngest = new LinkedHashSet<com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ParticipantIngest>();
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
     * Master flag
     * 
     */
    @JsonProperty("isMaster")
    private Boolean isMaster = false;
    @JsonProperty("regularUserAps")
    @Valid
    private PresentationUserApsIdentifiers regularUserAps;
    @JsonProperty("adminUserAps")
    @Valid
    private PresentationUserApsIdentifiers adminUserAps;
    @JsonProperty("creatorLegalEntity")
    private String creatorLegalEntity;
    /**
     * Additional Properties
     * 
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Map<String, String> additions = new HashMap<String, String>();

    /**
     * The service agreement name
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
     * The service agreement name
     * (Required)
     * 
     * @param name
     *     The name
     */
    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    public ServiceAgreementIngestPostRequestBody withName(String name) {
        this.name = name;
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

    public ServiceAgreementIngestPostRequestBody withExternalId(String externalId) {
        this.externalId = externalId;
        return this;
    }

    /**
     * Description
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
     * Description
     * (Required)
     * 
     * @param description
     *     The description
     */
    @JsonProperty("description")
    public void setDescription(String description) {
        this.description = description;
    }

    public ServiceAgreementIngestPostRequestBody withDescription(String description) {
        this.description = description;
        return this;
    }

    /**
     * Participants of the service agreement
     * (Required)
     * 
     * @return
     *     The participantsToIngest
     */
    @JsonProperty("participantsToIngest")
    public Set<com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ParticipantIngest> getParticipantsToIngest() {
        return participantsToIngest;
    }

    /**
     * Participants of the service agreement
     * (Required)
     * 
     * @param participantsToIngest
     *     The participantsToIngest
     */
    @JsonProperty("participantsToIngest")
    public void setParticipantsToIngest(Set<com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ParticipantIngest> participantsToIngest) {
        this.participantsToIngest = participantsToIngest;
    }

    public ServiceAgreementIngestPostRequestBody withParticipantsToIngest(Set<ParticipantIngest> participantsToIngest) {
        this.participantsToIngest = participantsToIngest;
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

    public ServiceAgreementIngestPostRequestBody withValidFromDate(String validFromDate) {
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

    public ServiceAgreementIngestPostRequestBody withValidFromTime(String validFromTime) {
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

    public ServiceAgreementIngestPostRequestBody withValidUntilDate(String validUntilDate) {
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

    public ServiceAgreementIngestPostRequestBody withValidUntilTime(String validUntilTime) {
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

    public ServiceAgreementIngestPostRequestBody withStatus(CreateStatus status) {
        this.status = status;
        return this;
    }

    /**
     * Master flag
     * 
     * @return
     *     The isMaster
     */
    @JsonProperty("isMaster")
    public Boolean getIsMaster() {
        return isMaster;
    }

    /**
     * Master flag
     * 
     * @param isMaster
     *     The isMaster
     */
    @JsonProperty("isMaster")
    public void setIsMaster(Boolean isMaster) {
        this.isMaster = isMaster;
    }

    public ServiceAgreementIngestPostRequestBody withIsMaster(Boolean isMaster) {
        this.isMaster = isMaster;
        return this;
    }

    /**
     * 
     * @return
     *     The regularUserAps
     */
    @JsonProperty("regularUserAps")
    public PresentationUserApsIdentifiers getRegularUserAps() {
        return regularUserAps;
    }

    /**
     * 
     * @param regularUserAps
     *     The regularUserAps
     */
    @JsonProperty("regularUserAps")
    public void setRegularUserAps(PresentationUserApsIdentifiers regularUserAps) {
        this.regularUserAps = regularUserAps;
    }

    public ServiceAgreementIngestPostRequestBody withRegularUserAps(PresentationUserApsIdentifiers regularUserAps) {
        this.regularUserAps = regularUserAps;
        return this;
    }

    /**
     * 
     * @return
     *     The adminUserAps
     */
    @JsonProperty("adminUserAps")
    public PresentationUserApsIdentifiers getAdminUserAps() {
        return adminUserAps;
    }

    /**
     * 
     * @param adminUserAps
     *     The adminUserAps
     */
    @JsonProperty("adminUserAps")
    public void setAdminUserAps(PresentationUserApsIdentifiers adminUserAps) {
        this.adminUserAps = adminUserAps;
    }

    public ServiceAgreementIngestPostRequestBody withAdminUserAps(PresentationUserApsIdentifiers adminUserAps) {
        this.adminUserAps = adminUserAps;
        return this;
    }

    /**
     * Service agreement creator legal entity id
     *
     * @param creatorLegalEntity
     *     The creatorLegalEntity
     */
    @JsonProperty("creatorLegalEntity")
    public void setCreatorLegalEntity(String creatorLegalEntity) {
        this.creatorLegalEntity = creatorLegalEntity;
    }

    public ServiceAgreementIngestPostRequestBody withCreatorLegalEntity(String creatorLegalEntity) {
        this.creatorLegalEntity = creatorLegalEntity;
        return this;
    }

    /**
     * Universally Unique Identifier.
     *
     * @return
     *     The creatorLegalEntity
     */
    @JsonProperty("creatorLegalEntity")
    public String getCreatorLegalEntity() {
        return creatorLegalEntity;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(name).append(externalId).append(description).append(participantsToIngest)
            .append(validFromDate).append(validFromTime).append(validUntilDate).append(validUntilTime).append(status)
            .append(isMaster).append(regularUserAps).append(adminUserAps).append(creatorLegalEntity).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ServiceAgreementIngestPostRequestBody) == false) {
            return false;
        }
        ServiceAgreementIngestPostRequestBody rhs = ((ServiceAgreementIngestPostRequestBody) other);
        return new EqualsBuilder().append(name, rhs.name).append(externalId, rhs.externalId)
            .append(description, rhs.description).append(participantsToIngest, rhs.participantsToIngest)
            .append(validFromDate, rhs.validFromDate).append(validFromTime, rhs.validFromTime)
            .append(validUntilDate, rhs.validUntilDate).append(validUntilTime, rhs.validUntilTime)
            .append(status, rhs.status).append(isMaster, rhs.isMaster).append(regularUserAps, rhs.regularUserAps)
            .append(adminUserAps, rhs.adminUserAps).append(creatorLegalEntity, rhs.creatorLegalEntity).isEquals();
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
