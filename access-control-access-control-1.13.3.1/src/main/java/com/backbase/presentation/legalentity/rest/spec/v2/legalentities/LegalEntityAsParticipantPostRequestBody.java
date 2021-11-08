package com.backbase.presentation.legalentity.rest.spec.v2.legalentities;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Generated;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import com.backbase.buildingblocks.persistence.model.AdditionalPropertiesAware;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.enumeration.LegalEntityType;
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
    "legalEntityName",
    "legalEntityExternalId",
    "legalEntityParentId",
    "legalEntityType",
    "participantOf"
})
@JsonIgnoreProperties(ignoreUnknown = true)
public class LegalEntityAsParticipantPostRequestBody implements AdditionalPropertiesAware
{

    /**
     * Legal Entity name
     * (Required)
     * 
     */
    @JsonProperty("legalEntityName")
    @Pattern(regexp = "^\\S(.*(\\S))?$")
    @Size(min = 1, max = 128)
    @NotNull
    private String legalEntityName;
    /**
     * External legal entity identifier.
     * 
     */
    @JsonProperty("legalEntityExternalId")
    @Pattern(regexp = "^[^\\r\\n]{1,64}$")
    @Size(min = 1, max = 64)
    private String legalEntityExternalId;
    /**
     * External legal entity identifier.
     * 
     */
    @JsonProperty("legalEntityParentId")
    @Pattern(regexp = "^[^\\r\\n]{1,64}$")
    @Size(min = 1, max = 64)
    private String legalEntityParentId;
    /**
     * Type of the legal entity. Bank or Customer.
     * (Required)
     * 
     */
    @JsonProperty("legalEntityType")
    @NotNull
    private LegalEntityType legalEntityType;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("participantOf")
    @Valid
    @NotNull
    private ParticipantOf participantOf;
    /**
     * Additional Properties
     * 
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Map<String, String> additions = new HashMap<String, String>();

    /**
     * Legal Entity name
     * (Required)
     * 
     * @return
     *     The legalEntityName
     */
    @JsonProperty("legalEntityName")
    public String getLegalEntityName() {
        return legalEntityName;
    }

    /**
     * Legal Entity name
     * (Required)
     * 
     * @param legalEntityName
     *     The legalEntityName
     */
    @JsonProperty("legalEntityName")
    public void setLegalEntityName(String legalEntityName) {
        this.legalEntityName = legalEntityName;
    }

    public LegalEntityAsParticipantPostRequestBody withLegalEntityName(String legalEntityName) {
        this.legalEntityName = legalEntityName;
        return this;
    }

    /**
     * External legal entity identifier.
     * 
     * @return
     *     The legalEntityExternalId
     */
    @JsonProperty("legalEntityExternalId")
    public String getLegalEntityExternalId() {
        return legalEntityExternalId;
    }

    /**
     * External legal entity identifier.
     * 
     * @param legalEntityExternalId
     *     The legalEntityExternalId
     */
    @JsonProperty("legalEntityExternalId")
    public void setLegalEntityExternalId(String legalEntityExternalId) {
        this.legalEntityExternalId = legalEntityExternalId;
    }

    public LegalEntityAsParticipantPostRequestBody withLegalEntityExternalId(String legalEntityExternalId) {
        this.legalEntityExternalId = legalEntityExternalId;
        return this;
    }

    /**
     * External legal entity identifier.
     * 
     * @return
     *     The legalEntityParentId
     */
    @JsonProperty("legalEntityParentId")
    public String getLegalEntityParentId() {
        return legalEntityParentId;
    }

    /**
     * External legal entity identifier.
     * 
     * @param legalEntityParentId
     *     The legalEntityParentId
     */
    @JsonProperty("legalEntityParentId")
    public void setLegalEntityParentId(String legalEntityParentId) {
        this.legalEntityParentId = legalEntityParentId;
    }

    public LegalEntityAsParticipantPostRequestBody withLegalEntityParentId(String legalEntityParentId) {
        this.legalEntityParentId = legalEntityParentId;
        return this;
    }

    /**
     * Type of the legal entity. Bank or Customer.
     * (Required)
     * 
     * @return
     *     The legalEntityType
     */
    @JsonProperty("legalEntityType")
    public LegalEntityType getLegalEntityType() {
        return legalEntityType;
    }

    /**
     * Type of the legal entity. Bank or Customer.
     * (Required)
     * 
     * @param legalEntityType
     *     The legalEntityType
     */
    @JsonProperty("legalEntityType")
    public void setLegalEntityType(LegalEntityType legalEntityType) {
        this.legalEntityType = legalEntityType;
    }

    public LegalEntityAsParticipantPostRequestBody withLegalEntityType(LegalEntityType legalEntityType) {
        this.legalEntityType = legalEntityType;
        return this;
    }

    /**
     * 
     * (Required)
     * 
     * @return
     *     The participantOf
     */
    @JsonProperty("participantOf")
    public ParticipantOf getParticipantOf() {
        return participantOf;
    }

    /**
     * 
     * (Required)
     * 
     * @param participantOf
     *     The participantOf
     */
    @JsonProperty("participantOf")
    public void setParticipantOf(ParticipantOf participantOf) {
        this.participantOf = participantOf;
    }

    public LegalEntityAsParticipantPostRequestBody withParticipantOf(ParticipantOf participantOf) {
        this.participantOf = participantOf;
        return this;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(legalEntityName).append(legalEntityExternalId).append(legalEntityParentId).append(legalEntityType).append(participantOf).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof LegalEntityAsParticipantPostRequestBody) == false) {
            return false;
        }
        LegalEntityAsParticipantPostRequestBody rhs = ((LegalEntityAsParticipantPostRequestBody) other);
        return new EqualsBuilder().append(legalEntityName, rhs.legalEntityName).append(legalEntityExternalId, rhs.legalEntityExternalId).append(legalEntityParentId, rhs.legalEntityParentId).append(legalEntityType, rhs.legalEntityType).append(participantOf, rhs.participantOf).isEquals();
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
