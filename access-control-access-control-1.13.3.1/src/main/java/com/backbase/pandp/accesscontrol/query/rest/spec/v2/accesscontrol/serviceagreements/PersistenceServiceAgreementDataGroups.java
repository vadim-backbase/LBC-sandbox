
package com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.serviceagreements;

import com.backbase.buildingblocks.persistence.model.AdditionalPropertiesAware;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Generated;
import javax.validation.Valid;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * An object which contain service agreement id with data group ids and data item ids.
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
    "serviceAgreementId",
    "dataGroups"
})
@JsonIgnoreProperties(ignoreUnknown = true)
public class PersistenceServiceAgreementDataGroups implements AdditionalPropertiesAware
{

    /**
     * Service agreement id
     * 
     */
    @JsonProperty("serviceAgreementId")
    private String serviceAgreementId;
    /**
     * List of data group ids with data item ids
     * 
     */
    @JsonProperty("dataGroups")
    @Valid
    private List<PersistenceDataGroupDataItems> dataGroups = new ArrayList<PersistenceDataGroupDataItems>();
    /**
     * Additional Properties
     * 
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Map<String, String> additions = new HashMap<String, String>();

    /**
     * Service agreement id
     * 
     * @return
     *     The serviceAgreementId
     */
    @JsonProperty("serviceAgreementId")
    public String getServiceAgreementId() {
        return serviceAgreementId;
    }

    /**
     * Service agreement id
     * 
     * @param serviceAgreementId
     *     The serviceAgreementId
     */
    @JsonProperty("serviceAgreementId")
    public void setServiceAgreementId(String serviceAgreementId) {
        this.serviceAgreementId = serviceAgreementId;
    }

    public PersistenceServiceAgreementDataGroups withServiceAgreementId(String serviceAgreementId) {
        this.serviceAgreementId = serviceAgreementId;
        return this;
    }

    /**
     * List of data group ids with data item ids
     * 
     * @return
     *     The dataGroups
     */
    @JsonProperty("dataGroups")
    public List<PersistenceDataGroupDataItems> getDataGroups() {
        return dataGroups;
    }

    /**
     * List of data group ids with data item ids
     * 
     * @param dataGroups
     *     The dataGroups
     */
    @JsonProperty("dataGroups")
    public void setDataGroups(List<PersistenceDataGroupDataItems> dataGroups) {
        this.dataGroups = dataGroups;
    }

    public PersistenceServiceAgreementDataGroups withDataGroups(List<PersistenceDataGroupDataItems> dataGroups) {
        this.dataGroups = dataGroups;
        return this;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(serviceAgreementId).append(dataGroups).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof PersistenceServiceAgreementDataGroups) == false) {
            return false;
        }
        PersistenceServiceAgreementDataGroups rhs = ((PersistenceServiceAgreementDataGroups) other);
        return new EqualsBuilder().append(serviceAgreementId, rhs.serviceAgreementId).append(dataGroups, rhs.dataGroups).isEquals();
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
