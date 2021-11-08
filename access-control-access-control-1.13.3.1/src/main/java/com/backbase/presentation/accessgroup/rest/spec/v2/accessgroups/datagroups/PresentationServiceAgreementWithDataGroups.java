
package com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.datagroups;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Generated;
import javax.validation.Valid;
import com.backbase.buildingblocks.persistence.model.AdditionalPropertiesAware;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.PresentationServiceAgreementIds;
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
    "serviceAgreement",
    "dataGroups"
})
@JsonIgnoreProperties(ignoreUnknown = true)
public class PresentationServiceAgreementWithDataGroups implements AdditionalPropertiesAware
{

    @JsonProperty("serviceAgreement")
    @Valid
    private PresentationServiceAgreementIds serviceAgreement;
    /**
     * List of data groups
     * 
     */
    @JsonProperty("dataGroups")
    @Valid
    private List<com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.datagroups.PresentationDataGroupDetails> dataGroups = new ArrayList<com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.datagroups.PresentationDataGroupDetails>();
    /**
     * Additional Properties
     * 
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Map<String, String> additions = new HashMap<String, String>();

    /**
     * 
     * @return
     *     The serviceAgreement
     */
    @JsonProperty("serviceAgreement")
    public PresentationServiceAgreementIds getServiceAgreement() {
        return serviceAgreement;
    }

    /**
     * 
     * @param serviceAgreement
     *     The serviceAgreement
     */
    @JsonProperty("serviceAgreement")
    public void setServiceAgreement(PresentationServiceAgreementIds serviceAgreement) {
        this.serviceAgreement = serviceAgreement;
    }

    public PresentationServiceAgreementWithDataGroups withServiceAgreement(PresentationServiceAgreementIds serviceAgreement) {
        this.serviceAgreement = serviceAgreement;
        return this;
    }

    /**
     * List of data groups
     * 
     * @return
     *     The dataGroups
     */
    @JsonProperty("dataGroups")
    public List<com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.datagroups.PresentationDataGroupDetails> getDataGroups() {
        return dataGroups;
    }

    /**
     * List of data groups
     * 
     * @param dataGroups
     *     The dataGroups
     */
    @JsonProperty("dataGroups")
    public void setDataGroups(List<com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.datagroups.PresentationDataGroupDetails> dataGroups) {
        this.dataGroups = dataGroups;
    }

    public PresentationServiceAgreementWithDataGroups withDataGroups(List<PresentationDataGroupDetails> dataGroups) {
        this.dataGroups = dataGroups;
        return this;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(serviceAgreement).append(dataGroups).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof PresentationServiceAgreementWithDataGroups) == false) {
            return false;
        }
        PresentationServiceAgreementWithDataGroups rhs = ((PresentationServiceAgreementWithDataGroups) other);
        return new EqualsBuilder().append(serviceAgreement, rhs.serviceAgreement).append(dataGroups, rhs.dataGroups).isEquals();
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
