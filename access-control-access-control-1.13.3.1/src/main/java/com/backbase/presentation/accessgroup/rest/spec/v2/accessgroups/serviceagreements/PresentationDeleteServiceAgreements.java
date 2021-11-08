
package com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Generated;
import javax.validation.Valid;
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
    "serviceAgreementIdentifiers",
    "accessToken"
})
@JsonIgnoreProperties(ignoreUnknown = true)
public class PresentationDeleteServiceAgreements implements AdditionalPropertiesAware
{

    @JsonProperty("serviceAgreementIdentifiers")
    @Size(min = 1)
    @Valid
    private List<PresentationServiceAgreementIdentifier> serviceAgreementIdentifiers = new ArrayList<PresentationServiceAgreementIdentifier>();
    /**
     * Universally Unique Identifier.
     * 
     */
    @JsonProperty("accessToken")
    @Pattern(regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$")
    @Size(min = 1, max = 36)
    private String accessToken;
    /**
     * Additional Properties
     * 
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Map<String, String> additions = new HashMap<String, String>();

    /**
     * 
     * @return
     *     The serviceAgreementIdentifiers
     */
    @JsonProperty("serviceAgreementIdentifiers")
    public List<PresentationServiceAgreementIdentifier> getServiceAgreementIdentifiers() {
        return serviceAgreementIdentifiers;
    }

    /**
     * 
     * @param serviceAgreementIdentifiers
     *     The serviceAgreementIdentifiers
     */
    @JsonProperty("serviceAgreementIdentifiers")
    public void setServiceAgreementIdentifiers(List<PresentationServiceAgreementIdentifier> serviceAgreementIdentifiers) {
        this.serviceAgreementIdentifiers = serviceAgreementIdentifiers;
    }

    public PresentationDeleteServiceAgreements withServiceAgreementIdentifiers(List<PresentationServiceAgreementIdentifier> serviceAgreementIdentifiers) {
        this.serviceAgreementIdentifiers = serviceAgreementIdentifiers;
        return this;
    }

    /**
     * Universally Unique Identifier.
     * 
     * @return
     *     The accessToken
     */
    @JsonProperty("accessToken")
    public String getAccessToken() {
        return accessToken;
    }

    /**
     * Universally Unique Identifier.
     * 
     * @param accessToken
     *     The accessToken
     */
    @JsonProperty("accessToken")
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public PresentationDeleteServiceAgreements withAccessToken(String accessToken) {
        this.accessToken = accessToken;
        return this;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(serviceAgreementIdentifiers).append(accessToken).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof PresentationDeleteServiceAgreements) == false) {
            return false;
        }
        PresentationDeleteServiceAgreements rhs = ((PresentationDeleteServiceAgreements) other);
        return new EqualsBuilder().append(serviceAgreementIdentifiers, rhs.serviceAgreementIdentifiers).append(accessToken, rhs.accessToken).isEquals();
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
