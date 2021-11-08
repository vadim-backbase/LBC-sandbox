
package com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.aps;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Generated;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
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
    "externalServiceAgreementId",
    "regularUserAps",
    "adminUserAps"
})
@JsonIgnoreProperties(ignoreUnknown = true)
public class PresentationPermissionSetItemPut implements AdditionalPropertiesAware
{

    /**
     * External service agreement id.
     * (Required)
     * 
     */
    @JsonProperty("externalServiceAgreementId")
    @NotNull
    private String externalServiceAgreementId;
    @JsonProperty("regularUserAps")
    @Valid
    private PresentationUserApsIdentifiers regularUserAps;
    @JsonProperty("adminUserAps")
    @Valid
    private PresentationUserApsIdentifiers adminUserAps;
    /**
     * Additional Properties
     * 
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Map<String, String> additions = new HashMap<String, String>();

    /**
     * External service agreement id.
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
     * External service agreement id.
     * (Required)
     * 
     * @param externalServiceAgreementId
     *     The externalServiceAgreementId
     */
    @JsonProperty("externalServiceAgreementId")
    public void setExternalServiceAgreementId(String externalServiceAgreementId) {
        this.externalServiceAgreementId = externalServiceAgreementId;
    }

    public PresentationPermissionSetItemPut withExternalServiceAgreementId(String externalServiceAgreementId) {
        this.externalServiceAgreementId = externalServiceAgreementId;
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

    public PresentationPermissionSetItemPut withRegularUserAps(PresentationUserApsIdentifiers regularUserAps) {
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

    public PresentationPermissionSetItemPut withAdminUserAps(PresentationUserApsIdentifiers adminUserAps) {
        this.adminUserAps = adminUserAps;
        return this;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(externalServiceAgreementId).append(regularUserAps).append(adminUserAps).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof PresentationPermissionSetItemPut) == false) {
            return false;
        }
        PresentationPermissionSetItemPut rhs = ((PresentationPermissionSetItemPut) other);
        return new EqualsBuilder().append(externalServiceAgreementId, rhs.externalServiceAgreementId).append(regularUserAps, rhs.regularUserAps).append(adminUserAps, rhs.adminUserAps).isEquals();
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
