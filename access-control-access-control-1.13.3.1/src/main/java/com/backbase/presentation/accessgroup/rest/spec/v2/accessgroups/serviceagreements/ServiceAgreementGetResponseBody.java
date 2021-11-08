
package com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements;

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
    "approvalId"
})
@JsonIgnoreProperties(ignoreUnknown = true)
public class ServiceAgreementGetResponseBody
    extends ServiceAgreementGetResponseBodyParent
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
     * Id of approval request.
     * 
     */
    @JsonProperty("approvalId")
    @Size(min = 1, max = 36)
    private String approvalId;

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

    public ServiceAgreementGetResponseBody withCreatorLegalEntity(String creatorLegalEntity) {
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

    public ServiceAgreementGetResponseBody withStatus(Status status) {
        this.status = status;
        return this;
    }

    /**
     * Id of approval request.
     * 
     * @return
     *     The approvalId
     */
    @JsonProperty("approvalId")
    public String getApprovalId() {
        return approvalId;
    }

    /**
     * Id of approval request.
     * 
     * @param approvalId
     *     The approvalId
     */
    @JsonProperty("approvalId")
    public void setApprovalId(String approvalId) {
        this.approvalId = approvalId;
    }

    public ServiceAgreementGetResponseBody withApprovalId(String approvalId) {
        this.approvalId = approvalId;
        return this;
    }

    @Override
    public ServiceAgreementGetResponseBody withId(String id) {
        super.withId(id);
        return this;
    }

    @Override
    public ServiceAgreementGetResponseBody withExternalId(String externalId) {
        super.withExternalId(externalId);
        return this;
    }

    @Override
    public ServiceAgreementGetResponseBody withName(String name) {
        super.withName(name);
        return this;
    }

    @Override
    public ServiceAgreementGetResponseBody withDescription(String description) {
        super.withDescription(description);
        return this;
    }

    @Override
    public ServiceAgreementGetResponseBody withIsMaster(Boolean isMaster) {
        super.withIsMaster(isMaster);
        return this;
    }

    @Override
    public ServiceAgreementGetResponseBody withValidFromDate(String validFromDate) {
        super.withValidFromDate(validFromDate);
        return this;
    }

    @Override
    public ServiceAgreementGetResponseBody withValidFromTime(String validFromTime) {
        super.withValidFromTime(validFromTime);
        return this;
    }

    @Override
    public ServiceAgreementGetResponseBody withValidUntilDate(String validUntilDate) {
        super.withValidUntilDate(validUntilDate);
        return this;
    }

    @Override
    public ServiceAgreementGetResponseBody withValidUntilTime(String validUntilTime) {
        super.withValidUntilTime(validUntilTime);
        return this;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(creatorLegalEntity).append(status).append(approvalId).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ServiceAgreementGetResponseBody) == false) {
            return false;
        }
        ServiceAgreementGetResponseBody rhs = ((ServiceAgreementGetResponseBody) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(creatorLegalEntity, rhs.creatorLegalEntity).append(status, rhs.status).append(approvalId, rhs.approvalId).isEquals();
    }

}
