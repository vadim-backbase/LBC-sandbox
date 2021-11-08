
package com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.serviceagreements;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.Date;
import javax.annotation.Generated;
import javax.validation.constraints.Pattern;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Service agreement item
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
    "creatorLegalEntity",
    "status"
})
@JsonIgnoreProperties(ignoreUnknown = true)
public class ServiceAgreementItem
    extends ServiceAgreementBase
{

    /**
     * Universally Unique Identifier.
     * 
     */
    @JsonProperty("creatorLegalEntity")
    @Pattern(regexp = "^[0-9a-f]{32}$|^[0-9a-f-]{36}$")
    private String creatorLegalEntity;
    /**
     * Status of the entity
     * 
     */
    @JsonProperty("status")
    private Status status;

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

    /**
     * Universally Unique Identifier.
     * 
     * @param creatorLegalEntity
     *     The creatorLegalEntity
     */
    @JsonProperty("creatorLegalEntity")
    public void setCreatorLegalEntity(String creatorLegalEntity) {
        this.creatorLegalEntity = creatorLegalEntity;
    }

    public ServiceAgreementItem withCreatorLegalEntity(String creatorLegalEntity) {
        this.creatorLegalEntity = creatorLegalEntity;
        return this;
    }

    /**
     * Status of the entity
     * 
     * @return
     *     The status
     */
    @JsonProperty("status")
    public Status getStatus() {
        return status;
    }

    /**
     * Status of the entity
     * 
     * @param status
     *     The status
     */
    @JsonProperty("status")
    public void setStatus(Status status) {
        this.status = status;
    }

    public ServiceAgreementItem withStatus(Status status) {
        this.status = status;
        return this;
    }

    @Override
    public ServiceAgreementItem withId(String id) {
        super.withId(id);
        return this;
    }

    @Override
    public ServiceAgreementItem withExternalId(String externalId) {
        super.withExternalId(externalId);
        return this;
    }

    @Override
    public ServiceAgreementItem withName(String name) {
        super.withName(name);
        return this;
    }

    @Override
    public ServiceAgreementItem withDescription(String description) {
        super.withDescription(description);
        return this;
    }

    @Override
    public ServiceAgreementItem withIsMaster(Boolean isMaster) {
        super.withIsMaster(isMaster);
        return this;
    }

    @Override
    public ServiceAgreementItem withValidFrom(Date validFrom) {
        super.withValidFrom(validFrom);
        return this;
    }

    @Override
    public ServiceAgreementItem withValidUntil(Date validUntil) {
        super.withValidUntil(validUntil);
        return this;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(creatorLegalEntity).append(status).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ServiceAgreementItem) == false) {
            return false;
        }
        ServiceAgreementItem rhs = ((ServiceAgreementItem) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(creatorLegalEntity, rhs.creatorLegalEntity).append(status, rhs.status).isEquals();
    }

}
