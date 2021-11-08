
package com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups;

import java.util.List;
import javax.annotation.Generated;
import javax.validation.constraints.NotNull;
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
    "externalServiceAgreementId",
    "action"
})
@JsonIgnoreProperties(ignoreUnknown = true)
public class BatchResponseItemExtended
    extends BatchResponseItem
{

    /**
     * External Service Agreement ID.
     * (Required)
     * 
     */
    @JsonProperty("externalServiceAgreementId")
    @Size(min = 1, max = 64)
    @NotNull
    private String externalServiceAgreementId;
    /**
     * Presentation action
     * 
     */
    @JsonProperty("action")
    private com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.PresentationAction action;

    /**
     * External Service Agreement ID.
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
     * External Service Agreement ID.
     * (Required)
     * 
     * @param externalServiceAgreementId
     *     The externalServiceAgreementId
     */
    @JsonProperty("externalServiceAgreementId")
    public void setExternalServiceAgreementId(String externalServiceAgreementId) {
        this.externalServiceAgreementId = externalServiceAgreementId;
    }

    public BatchResponseItemExtended withExternalServiceAgreementId(String externalServiceAgreementId) {
        this.externalServiceAgreementId = externalServiceAgreementId;
        return this;
    }

    /**
     * Presentation action
     * 
     * @return
     *     The action
     */
    @JsonProperty("action")
    public com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.PresentationAction getAction() {
        return action;
    }

    /**
     * Presentation action
     * 
     * @param action
     *     The action
     */
    @JsonProperty("action")
    public void setAction(com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.PresentationAction action) {
        this.action = action;
    }

    public BatchResponseItemExtended withAction(PresentationAction action) {
        this.action = action;
        return this;
    }

    @Override
    public BatchResponseItemExtended withResourceId(String resourceId) {
        super.withResourceId(resourceId);
        return this;
    }

    @Override
    public BatchResponseItemExtended withStatus(BatchResponseStatusCode status) {
        super.withStatus(status);
        return this;
    }

    @Override
    public BatchResponseItemExtended withErrors(List<String> errors) {
        super.withErrors(errors);
        return this;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(externalServiceAgreementId).append(action).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof BatchResponseItemExtended) == false) {
            return false;
        }
        BatchResponseItemExtended rhs = ((BatchResponseItemExtended) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(externalServiceAgreementId, rhs.externalServiceAgreementId).append(action, rhs.action).isEquals();
    }

}
