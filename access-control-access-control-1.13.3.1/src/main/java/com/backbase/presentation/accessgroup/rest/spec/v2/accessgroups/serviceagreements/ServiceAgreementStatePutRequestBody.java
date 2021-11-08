
package com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Generated;
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
    "state"
})
@JsonIgnoreProperties(ignoreUnknown = true)
public class ServiceAgreementStatePutRequestBody implements AdditionalPropertiesAware
{

    /**
     * Status of the entity
     * (Required)
     * 
     */
    @JsonProperty("state")
    @NotNull
    private com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.Status state;
    /**
     * Additional Properties
     * 
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Map<String, String> additions = new HashMap<String, String>();

    /**
     * Status of the entity
     * (Required)
     * 
     * @return
     *     The state
     */
    @JsonProperty("state")
    public com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.Status getState() {
        return state;
    }

    /**
     * Status of the entity
     * (Required)
     * 
     * @param state
     *     The state
     */
    @JsonProperty("state")
    public void setState(com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.Status state) {
        this.state = state;
    }

    public ServiceAgreementStatePutRequestBody withState(Status state) {
        this.state = state;
        return this;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(state).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ServiceAgreementStatePutRequestBody) == false) {
            return false;
        }
        ServiceAgreementStatePutRequestBody rhs = ((ServiceAgreementStatePutRequestBody) other);
        return new EqualsBuilder().append(state, rhs.state).isEquals();
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
