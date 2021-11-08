
package com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.serviceagreements;

import com.backbase.buildingblocks.persistence.model.AdditionalPropertiesAware;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.serviceagreements.PersistenceServiceAgreement;
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
import javax.validation.constraints.NotNull;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * List of service agreements
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
    "totalElements",
    "serviceAgreements"
})
@JsonIgnoreProperties(ignoreUnknown = true)
public class PersistenceServiceAgreements implements AdditionalPropertiesAware
{

    /**
     * total number of elements matching the query
     * (Required)
     * 
     */
    @JsonProperty("totalElements")
    @NotNull
    private Long totalElements;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("serviceAgreements")
    @Valid
    @NotNull
    private List<PersistenceServiceAgreement> serviceAgreements = new ArrayList<PersistenceServiceAgreement>();
    /**
     * Additional Properties
     * 
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Map<String, String> additions = new HashMap<String, String>();

    /**
     * total number of elements matching the query
     * (Required)
     * 
     * @return
     *     The totalElements
     */
    @JsonProperty("totalElements")
    public Long getTotalElements() {
        return totalElements;
    }

    /**
     * total number of elements matching the query
     * (Required)
     * 
     * @param totalElements
     *     The totalElements
     */
    @JsonProperty("totalElements")
    public void setTotalElements(Long totalElements) {
        this.totalElements = totalElements;
    }

    public PersistenceServiceAgreements withTotalElements(Long totalElements) {
        this.totalElements = totalElements;
        return this;
    }

    /**
     * 
     * (Required)
     * 
     * @return
     *     The serviceAgreements
     */
    @JsonProperty("serviceAgreements")
    public List<PersistenceServiceAgreement> getServiceAgreements() {
        return serviceAgreements;
    }

    /**
     * 
     * (Required)
     * 
     * @param serviceAgreements
     *     The serviceAgreements
     */
    @JsonProperty("serviceAgreements")
    public void setServiceAgreements(List<PersistenceServiceAgreement> serviceAgreements) {
        this.serviceAgreements = serviceAgreements;
    }

    public PersistenceServiceAgreements withServiceAgreements(List<PersistenceServiceAgreement> serviceAgreements) {
        this.serviceAgreements = serviceAgreements;
        return this;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(totalElements).append(serviceAgreements).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof PersistenceServiceAgreements) == false) {
            return false;
        }
        PersistenceServiceAgreements rhs = ((PersistenceServiceAgreements) other);
        return new EqualsBuilder().append(totalElements, rhs.totalElements).append(serviceAgreements, rhs.serviceAgreements).isEquals();
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
