
package com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.serviceagreements;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.math.BigDecimal;
import java.util.Date;
import javax.annotation.Generated;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Service agreement
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
    "numberOfParticipants",
    "creatorLegalEntityName"
})
public class PersistenceServiceAgreement
    extends ServiceAgreementItem
{

    /**
     * number of legal entities participating in the service agreement
     * 
     */
    @JsonProperty("numberOfParticipants")
    private BigDecimal numberOfParticipants;
    /**
     * Service agreement creator legal entity name
     * 
     */
    @JsonProperty("creatorLegalEntityName")
    private String creatorLegalEntityName;

    /**
     * number of legal entities participating in the service agreement
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
     * 
     * @param numberOfParticipants
     *     The numberOfParticipants
     */
    @JsonProperty("numberOfParticipants")
    public void setNumberOfParticipants(BigDecimal numberOfParticipants) {
        this.numberOfParticipants = numberOfParticipants;
    }

    public PersistenceServiceAgreement withNumberOfParticipants(BigDecimal numberOfParticipants) {
        this.numberOfParticipants = numberOfParticipants;
        return this;
    }

    /**
     * Service agreement creator legal entity name
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
     * 
     * @param creatorLegalEntityName
     *     The creatorLegalEntityName
     */
    @JsonProperty("creatorLegalEntityName")
    public void setCreatorLegalEntityName(String creatorLegalEntityName) {
        this.creatorLegalEntityName = creatorLegalEntityName;
    }

    public PersistenceServiceAgreement withCreatorLegalEntityName(String creatorLegalEntityName) {
        this.creatorLegalEntityName = creatorLegalEntityName;
        return this;
    }

    @Override
    public PersistenceServiceAgreement withCreatorLegalEntity(String creatorLegalEntity) {
        super.withCreatorLegalEntity(creatorLegalEntity);
        return this;
    }

    @Override
    public PersistenceServiceAgreement withStatus(Status status) {
        super.withStatus(status);
        return this;
    }

    @Override
    public PersistenceServiceAgreement withId(String id) {
        super.withId(id);
        return this;
    }

    @Override
    public PersistenceServiceAgreement withExternalId(String externalId) {
        super.withExternalId(externalId);
        return this;
    }

    @Override
    public PersistenceServiceAgreement withName(String name) {
        super.withName(name);
        return this;
    }

    @Override
    public PersistenceServiceAgreement withDescription(String description) {
        super.withDescription(description);
        return this;
    }

    @Override
    public PersistenceServiceAgreement withIsMaster(Boolean isMaster) {
        super.withIsMaster(isMaster);
        return this;
    }

    @Override
    public PersistenceServiceAgreement withValidFrom(Date validFrom) {
        super.withValidFrom(validFrom);
        return this;
    }

    @Override
    public PersistenceServiceAgreement withValidUntil(Date validUntil) {
        super.withValidUntil(validUntil);
        return this;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(numberOfParticipants).append(creatorLegalEntityName).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof PersistenceServiceAgreement) == false) {
            return false;
        }
        PersistenceServiceAgreement rhs = ((PersistenceServiceAgreement) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(numberOfParticipants, rhs.numberOfParticipants).append(creatorLegalEntityName, rhs.creatorLegalEntityName).isEquals();
    }

}
