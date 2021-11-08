
package com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements;

import java.math.BigDecimal;
import javax.annotation.Generated;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
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
    "creatorLegalEntity",
    "status",
    "numberOfParticipants",
    "creatorLegalEntityName"
})
@JsonIgnoreProperties(ignoreUnknown = true)
public class PresentationServiceAgreement
    extends ServiceAgreementsGetResponseBodyParent
{

    /**
     * Universally Unique Identifier.
     * (Required)
     * 
     */
    @JsonProperty("creatorLegalEntity")
    @Pattern(regexp = "^[0-9a-f]{32}$|^[0-9a-f-]{36}$")
    @Size(min = 1, max = 36)
    @NotNull
    private String creatorLegalEntity;
    /**
     * Status of the entity
     * (Required)
     * 
     */
    @JsonProperty("status")
    @NotNull
    private com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.Status status;
    /**
     * number of legal entities participating in the service agreement
     * (Required)
     * 
     */
    @JsonProperty("numberOfParticipants")
    @NotNull
    private BigDecimal numberOfParticipants;
    /**
     * Service agreement creator legal entity name
     * (Required)
     * 
     */
    @JsonProperty("creatorLegalEntityName")
    @NotNull
    private String creatorLegalEntityName;

    /**
     * Universally Unique Identifier.
     * (Required)
     * 
     * @return
     *     The creatorLegalEntity
     */
    @JsonProperty("creatorLegalEntity")
    public String getCreatorLegalEntity() {
        return creatorLegalEntity;
    }

    /**
     * Universally Unique Identifier.
     * (Required)
     * 
     * @param creatorLegalEntity
     *     The creatorLegalEntity
     */
    @JsonProperty("creatorLegalEntity")
    public void setCreatorLegalEntity(String creatorLegalEntity) {
        this.creatorLegalEntity = creatorLegalEntity;
    }

    public PresentationServiceAgreement withCreatorLegalEntity(String creatorLegalEntity) {
        this.creatorLegalEntity = creatorLegalEntity;
        return this;
    }

    /**
     * Status of the entity
     * (Required)
     * 
     * @return
     *     The status
     */
    @JsonProperty("status")
    public com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.Status getStatus() {
        return status;
    }

    /**
     * Status of the entity
     * (Required)
     * 
     * @param status
     *     The status
     */
    @JsonProperty("status")
    public void setStatus(
        com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.Status status) {
        this.status = status;
    }

    public PresentationServiceAgreement withStatus(Status status) {
        this.status = status;
        return this;
    }

    /**
     * number of legal entities participating in the service agreement
     * (Required)
     * 
     * @return
     *     The numberOfParticipants
     */
    @JsonProperty("numberOfParticipants")
    public BigDecimal getNumberOfParticipants() {
        return numberOfParticipants;
    }

    /**
     * number of legal entities participating in the service agreement
     * (Required)
     * 
     * @param numberOfParticipants
     *     The numberOfParticipants
     */
    @JsonProperty("numberOfParticipants")
    public void setNumberOfParticipants(BigDecimal numberOfParticipants) {
        this.numberOfParticipants = numberOfParticipants;
    }

    public PresentationServiceAgreement withNumberOfParticipants(BigDecimal numberOfParticipants) {
        this.numberOfParticipants = numberOfParticipants;
        return this;
    }

    /**
     * Service agreement creator legal entity name
     * (Required)
     * 
     * @return
     *     The creatorLegalEntityName
     */
    @JsonProperty("creatorLegalEntityName")
    public String getCreatorLegalEntityName() {
        return creatorLegalEntityName;
    }

    /**
     * Service agreement creator legal entity name
     * (Required)
     * 
     * @param creatorLegalEntityName
     *     The creatorLegalEntityName
     */
    @JsonProperty("creatorLegalEntityName")
    public void setCreatorLegalEntityName(String creatorLegalEntityName) {
        this.creatorLegalEntityName = creatorLegalEntityName;
    }

    public PresentationServiceAgreement withCreatorLegalEntityName(String creatorLegalEntityName) {
        this.creatorLegalEntityName = creatorLegalEntityName;
        return this;
    }

    @Override
    public PresentationServiceAgreement withId(String id) {
        super.withId(id);
        return this;
    }

    @Override
    public PresentationServiceAgreement withExternalId(String externalId) {
        super.withExternalId(externalId);
        return this;
    }

    @Override
    public PresentationServiceAgreement withName(String name) {
        super.withName(name);
        return this;
    }

    @Override
    public PresentationServiceAgreement withDescription(String description) {
        super.withDescription(description);
        return this;
    }

    @Override
    public PresentationServiceAgreement withIsMaster(Boolean isMaster) {
        super.withIsMaster(isMaster);
        return this;
    }

    @Override
    public PresentationServiceAgreement withValidFromDate(String validFromDate) {
        super.withValidFromDate(validFromDate);
        return this;
    }

    @Override
    public PresentationServiceAgreement withValidFromTime(String validFromTime) {
        super.withValidFromTime(validFromTime);
        return this;
    }

    @Override
    public PresentationServiceAgreement withValidUntilDate(String validUntilDate) {
        super.withValidUntilDate(validUntilDate);
        return this;
    }

    @Override
    public PresentationServiceAgreement withValidUntilTime(String validUntilTime) {
        super.withValidUntilTime(validUntilTime);
        return this;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(creatorLegalEntity).append(status).append(numberOfParticipants).append(creatorLegalEntityName).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof PresentationServiceAgreement) == false) {
            return false;
        }
        PresentationServiceAgreement rhs = ((PresentationServiceAgreement) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(creatorLegalEntity, rhs.creatorLegalEntity).append(status, rhs.status).append(numberOfParticipants, rhs.numberOfParticipants).append(creatorLegalEntityName, rhs.creatorLegalEntityName).isEquals();
    }

}
