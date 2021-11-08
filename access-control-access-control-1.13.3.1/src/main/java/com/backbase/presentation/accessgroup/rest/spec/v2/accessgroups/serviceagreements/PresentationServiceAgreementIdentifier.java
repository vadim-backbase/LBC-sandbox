
package com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Generated;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import com.backbase.buildingblocks.persistence.model.AdditionalPropertiesAware;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
    "idIdentifier",
    "nameIdentifier",
    "externalIdIdentifier"
})
public class PresentationServiceAgreementIdentifier implements AdditionalPropertiesAware
{

    /**
     * Entity ID.
     * 
     */
    @JsonProperty("idIdentifier")
    @Pattern(regexp = "^[0-9a-f]{32}$|^[0-9a-f-]{36}$")
    @Size(min = 1, max = 36)
    private String idIdentifier;
    /**
     * Entity name.
     * 
     */
    @JsonProperty("nameIdentifier")
    @Pattern(regexp = "^\\S(.*(\\S))?$")
    @Size(min = 1, max = 128)
    private String nameIdentifier;
    /**
     * External service agreement identifier.
     * 
     */
    @JsonProperty("externalIdIdentifier")
    @Pattern(regexp = "^[^\\r\\n]{1,64}$")
    @Size(min = 1, max = 64)
    private String externalIdIdentifier;
    /**
     * Additional Properties
     * 
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Map<String, String> additions = new HashMap<String, String>();

    /**
     * Entity ID.
     * 
     * @return
     *     The idIdentifier
     */
    @JsonProperty("idIdentifier")
    public String getIdIdentifier() {
        return idIdentifier;
    }

    /**
     * Entity ID.
     * 
     * @param idIdentifier
     *     The idIdentifier
     */
    @JsonProperty("idIdentifier")
    public void setIdIdentifier(String idIdentifier) {
        this.idIdentifier = idIdentifier;
    }

    public PresentationServiceAgreementIdentifier withIdIdentifier(String idIdentifier) {
        this.idIdentifier = idIdentifier;
        return this;
    }

    /**
     * Entity name.
     * 
     * @return
     *     The nameIdentifier
     */
    @JsonProperty("nameIdentifier")
    public String getNameIdentifier() {
        return nameIdentifier;
    }

    /**
     * Entity name.
     * 
     * @param nameIdentifier
     *     The nameIdentifier
     */
    @JsonProperty("nameIdentifier")
    public void setNameIdentifier(String nameIdentifier) {
        this.nameIdentifier = nameIdentifier;
    }

    public PresentationServiceAgreementIdentifier withNameIdentifier(String nameIdentifier) {
        this.nameIdentifier = nameIdentifier;
        return this;
    }

    /**
     * External service agreement identifier.
     * 
     * @return
     *     The externalIdIdentifier
     */
    @JsonProperty("externalIdIdentifier")
    public String getExternalIdIdentifier() {
        return externalIdIdentifier;
    }

    /**
     * External service agreement identifier.
     * 
     * @param externalIdIdentifier
     *     The externalIdIdentifier
     */
    @JsonProperty("externalIdIdentifier")
    public void setExternalIdIdentifier(String externalIdIdentifier) {
        this.externalIdIdentifier = externalIdIdentifier;
    }

    public PresentationServiceAgreementIdentifier withExternalIdIdentifier(String externalIdIdentifier) {
        this.externalIdIdentifier = externalIdIdentifier;
        return this;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(idIdentifier).append(nameIdentifier).append(externalIdIdentifier).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof PresentationServiceAgreementIdentifier) == false) {
            return false;
        }
        PresentationServiceAgreementIdentifier rhs = ((PresentationServiceAgreementIdentifier) other);
        return new EqualsBuilder().append(idIdentifier, rhs.idIdentifier).append(nameIdentifier, rhs.nameIdentifier).append(externalIdIdentifier, rhs.externalIdIdentifier).isEquals();
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
