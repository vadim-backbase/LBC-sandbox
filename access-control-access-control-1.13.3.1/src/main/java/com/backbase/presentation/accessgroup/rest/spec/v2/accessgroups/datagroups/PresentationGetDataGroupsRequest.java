
package com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.datagroups;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Generated;
import javax.validation.Valid;
import com.backbase.buildingblocks.persistence.model.AdditionalPropertiesAware;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.LegalEntityIdentifier;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.PresentationItemIdentifier;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.PresentationServiceAgreementIdentifier;
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
    "serviceAgreementIdentifier",
    "dataItemIdentifier"
})
@JsonIgnoreProperties(ignoreUnknown = true)
public class PresentationGetDataGroupsRequest implements AdditionalPropertiesAware
{

    @JsonProperty("serviceAgreementIdentifier")
    @Valid
    private PresentationServiceAgreementIdentifier serviceAgreementIdentifier;
    @JsonProperty("dataItemIdentifier")
    @Valid
    private PresentationItemIdentifier dataItemIdentifier;
    @JsonProperty("legalEntityIdentifier")
    private LegalEntityIdentifier legalEntityIdentifier;
    /**
     * Additional Properties
     * 
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Map<String, String> additions = new HashMap<String, String>();

    /**
     * 
     * @return
     *     The serviceAgreementIdentifier
     */
    @JsonProperty("serviceAgreementIdentifier")
    public PresentationServiceAgreementIdentifier getServiceAgreementIdentifier() {
        return serviceAgreementIdentifier;
    }

    /**
     * 
     * @param serviceAgreementIdentifier
     *     The serviceAgreementIdentifier
     */
    @JsonProperty("serviceAgreementIdentifier")
    public void setServiceAgreementIdentifier(PresentationServiceAgreementIdentifier serviceAgreementIdentifier) {
        this.serviceAgreementIdentifier = serviceAgreementIdentifier;
    }

    public PresentationGetDataGroupsRequest withServiceAgreementIdentifier(PresentationServiceAgreementIdentifier serviceAgreementIdentifier) {
        this.serviceAgreementIdentifier = serviceAgreementIdentifier;
        return this;
    }

    /**
     *
     * @return
     *     The legalEntityExternalId
     */
    @JsonProperty("legalEntityExternalId")
    public LegalEntityIdentifier getLegalEntityIdentifier() {
        return legalEntityIdentifier;
    }

    /**
     *
     * @param String
     *     The legalEntityExternalId
     */
    @JsonProperty("legalEntityExternalId")
    public void setLegalEntityIdentifier(LegalEntityIdentifier legalEntityIdentifier) {
        this.legalEntityIdentifier = legalEntityIdentifier;
    }

    public PresentationGetDataGroupsRequest withLegalEntityIdentifier(LegalEntityIdentifier legalEntityIdentifier) {
        this.legalEntityIdentifier = legalEntityIdentifier;
        return this;
    }

    /**
     * 
     * @return
     *     The dataItemIdentifier
     */
    @JsonProperty("dataItemIdentifier")
    public PresentationItemIdentifier getDataItemIdentifier() {
        return dataItemIdentifier;
    }

    /**
     * 
     * @param dataItemIdentifier
     *     The dataItemIdentifier
     */
    @JsonProperty("dataItemIdentifier")
    public void setDataItemIdentifier(PresentationItemIdentifier dataItemIdentifier) {
        this.dataItemIdentifier = dataItemIdentifier;
    }

    public PresentationGetDataGroupsRequest withDataItemIdentifier(PresentationItemIdentifier dataItemIdentifier) {
        this.dataItemIdentifier = dataItemIdentifier;
        return this;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(serviceAgreementIdentifier).append(dataItemIdentifier).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof PresentationGetDataGroupsRequest) == false) {
            return false;
        }
        PresentationGetDataGroupsRequest rhs = ((PresentationGetDataGroupsRequest) other);
        return new EqualsBuilder().append(serviceAgreementIdentifier, rhs.serviceAgreementIdentifier).append(dataItemIdentifier, rhs.dataItemIdentifier).isEquals();
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
