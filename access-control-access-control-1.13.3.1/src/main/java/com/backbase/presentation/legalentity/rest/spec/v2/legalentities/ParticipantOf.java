
package com.backbase.presentation.legalentity.rest.spec.v2.legalentities;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Generated;
import javax.validation.Valid;
import com.backbase.buildingblocks.persistence.model.AdditionalPropertiesAware;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.validation.ParticipantOfType;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
    "existingCustomServiceAgreement",
    "newCustomServiceAgreement",
    "newMasterServiceAgreement"
})
@ParticipantOfType
public class ParticipantOf implements AdditionalPropertiesAware
{

    /**
     * 
     */
    @JsonProperty("existingCustomServiceAgreement")
    @Valid
    private ExistingCustomServiceAgreement existingCustomServiceAgreement;
    /**
     * 
     */
    @JsonProperty("newCustomServiceAgreement")
    @Valid
    private NewCustomServiceAgreement newCustomServiceAgreement;
    /**
     * 
     */
    @JsonProperty("newMasterServiceAgreement")
    @Valid
    private NewMasterServiceAgreement newMasterServiceAgreement;
    /**
     * Additional Properties
     * 
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Map<String, String> additions = new HashMap<String, String>();

    /**
     * 
     * @return
     *     The existingCustomServiceAgreement
     */
    @JsonProperty("existingCustomServiceAgreement")
    public ExistingCustomServiceAgreement getExistingCustomServiceAgreement() {
        return existingCustomServiceAgreement;
    }

    /**
     * 
     * @param existingCustomServiceAgreement
     *     The existingCustomServiceAgreement
     */
    @JsonProperty("existingCustomServiceAgreement")
    public void setExistingCustomServiceAgreement(ExistingCustomServiceAgreement existingCustomServiceAgreement) {
        this.existingCustomServiceAgreement = existingCustomServiceAgreement;
    }

    public ParticipantOf withExistingCustomServiceAgreement(ExistingCustomServiceAgreement existingCustomServiceAgreement) {
        this.existingCustomServiceAgreement = existingCustomServiceAgreement;
        return this;
    }

    /**
     * 
     * @return
     *     The newCustomServiceAgreement
     */
    @JsonProperty("newCustomServiceAgreement")
    public NewCustomServiceAgreement getNewCustomServiceAgreement() {
        return newCustomServiceAgreement;
    }

    /**
     * 
     * @param newCustomServiceAgreement
     *     The newCustomServiceAgreement
     */
    @JsonProperty("newCustomServiceAgreement")
    public void setNewCustomServiceAgreement(NewCustomServiceAgreement newCustomServiceAgreement) {
        this.newCustomServiceAgreement = newCustomServiceAgreement;
    }

    public ParticipantOf withNewCustomServiceAgreement(NewCustomServiceAgreement newCustomServiceAgreement) {
        this.newCustomServiceAgreement = newCustomServiceAgreement;
        return this;
    }

    /**
     * 
     * @return
     *     The newMasterServiceAgreement
     */
    @JsonProperty("newMasterServiceAgreement")
    public NewMasterServiceAgreement getNewMasterServiceAgreement() {
        return newMasterServiceAgreement;
    }

    /**
     * 
     * @param newMasterServiceAgreement
     *     The newMasterServiceAgreement
     */
    @JsonProperty("newMasterServiceAgreement")
    public void setNewMasterServiceAgreement(NewMasterServiceAgreement newMasterServiceAgreement) {
        this.newMasterServiceAgreement = newMasterServiceAgreement;
    }

    public ParticipantOf withNewMasterServiceAgreement(NewMasterServiceAgreement newMasterServiceAgreement) {
        this.newMasterServiceAgreement = newMasterServiceAgreement;
        return this;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(existingCustomServiceAgreement).append(newCustomServiceAgreement).append(newMasterServiceAgreement).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ParticipantOf) == false) {
            return false;
        }
        ParticipantOf rhs = ((ParticipantOf) other);
        return new EqualsBuilder().append(existingCustomServiceAgreement, rhs.existingCustomServiceAgreement).append(newCustomServiceAgreement, rhs.newCustomServiceAgreement).append(newMasterServiceAgreement, rhs.newMasterServiceAgreement).isEquals();
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
