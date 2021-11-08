
package com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Generated;
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
    "externalUserId",
    "externalServiceAgreementId"
})
public class PresentationServiceAgreementUserPair implements AdditionalPropertiesAware
{

    /**
     * External Unique Identifier.
     * (Required)
     * 
     */
    @JsonProperty("externalUserId")
    @Pattern(regexp = "^[^\\r\\n]{1,64}$")
    @Size(min = 1, max = 64)
    @NotNull
    private String externalUserId;
    /**
     * External Unique Identifier.
     * (Required)
     * 
     */
    @JsonProperty("externalServiceAgreementId")
    @Pattern(regexp = "^[^\\r\\n]{1,64}$")
    @Size(min = 1, max = 64)
    @NotNull
    private String externalServiceAgreementId;
    /**
     * Additional Properties
     * 
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Map<String, String> additions = new HashMap<String, String>();

    /**
     * External Unique Identifier.
     * (Required)
     * 
     * @return
     *     The externalUserId
     */
    @JsonProperty("externalUserId")
    public String getExternalUserId() {
        return externalUserId;
    }

    /**
     * External Unique Identifier.
     * (Required)
     * 
     * @param externalUserId
     *     The externalUserId
     */
    @JsonProperty("externalUserId")
    public void setExternalUserId(String externalUserId) {
        this.externalUserId = externalUserId;
    }

    public PresentationServiceAgreementUserPair withExternalUserId(String externalUserId) {
        this.externalUserId = externalUserId;
        return this;
    }

    /**
     * External Unique Identifier.
     * (Required)
     * 
     * @return
     *     The externalServiceAgreementId
     */
    @JsonProperty("externalServiceAgreementId")
    public String getExternalServiceAgreementId() {
        return externalServiceAgreementId;
    }

    /**
     * External Unique Identifier.
     * (Required)
     * 
     * @param externalServiceAgreementId
     *     The externalServiceAgreementId
     */
    @JsonProperty("externalServiceAgreementId")
    public void setExternalServiceAgreementId(String externalServiceAgreementId) {
        this.externalServiceAgreementId = externalServiceAgreementId;
    }

    public PresentationServiceAgreementUserPair withExternalServiceAgreementId(String externalServiceAgreementId) {
        this.externalServiceAgreementId = externalServiceAgreementId;
        return this;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(externalUserId).append(externalServiceAgreementId).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof PresentationServiceAgreementUserPair) == false) {
            return false;
        }
        PresentationServiceAgreementUserPair rhs = ((PresentationServiceAgreementUserPair) other);
        return new EqualsBuilder().append(externalUserId, rhs.externalUserId).append(externalServiceAgreementId, rhs.externalServiceAgreementId).isEquals();
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
