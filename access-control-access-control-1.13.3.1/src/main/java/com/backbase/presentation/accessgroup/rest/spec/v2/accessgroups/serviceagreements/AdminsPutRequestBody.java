
package com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Generated;
import javax.validation.Valid;
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
    "participants"
})
@JsonIgnoreProperties(ignoreUnknown = true)
public class AdminsPutRequestBody implements AdditionalPropertiesAware
{

    @JsonProperty("participants")
    @Size(min = 1)
    @Valid
    private List<com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.LegalEntityAdmins> participants = new ArrayList<com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.LegalEntityAdmins>();
    /**
     * Additional Properties
     * 
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Map<String, String> additions = new HashMap<String, String>();

    /**
     * 
     * @return
     *     The participants
     */
    @JsonProperty("participants")
    public List<com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.LegalEntityAdmins> getParticipants() {
        return participants;
    }

    /**
     * 
     * @param participants
     *     The participants
     */
    @JsonProperty("participants")
    public void setParticipants(List<com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.LegalEntityAdmins> participants) {
        this.participants = participants;
    }

    public AdminsPutRequestBody withParticipants(List<LegalEntityAdmins> participants) {
        this.participants = participants;
        return this;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(participants).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof AdminsPutRequestBody) == false) {
            return false;
        }
        AdminsPutRequestBody rhs = ((AdminsPutRequestBody) other);
        return new EqualsBuilder().append(participants, rhs.participants).isEquals();
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
