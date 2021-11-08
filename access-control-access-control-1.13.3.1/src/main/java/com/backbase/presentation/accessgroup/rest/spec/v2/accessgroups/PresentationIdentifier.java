
package com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups;

import java.util.HashMap;
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
    "idIdentifier",
    "nameIdentifier"
})
@JsonIgnoreProperties(ignoreUnknown = true)
public class PresentationIdentifier implements AdditionalPropertiesAware
{

    /**
     * Entity ID.
     * 
     */
    @JsonProperty("idIdentifier")
    @Pattern(regexp = "^\\S(.*(\\S))?$")
    @Size(min = 1, max = 36)
    private String idIdentifier;
    /**
     * 
     */
    @JsonProperty("nameIdentifier")
    @Valid
    private com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.NameIdentifier nameIdentifier;
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

    public PresentationIdentifier withIdIdentifier(String idIdentifier) {
        this.idIdentifier = idIdentifier;
        return this;
    }

    /**
     * 
     * @return
     *     The nameIdentifier
     */
    @JsonProperty("nameIdentifier")
    public com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.NameIdentifier getNameIdentifier() {
        return nameIdentifier;
    }

    /**
     * 
     * @param nameIdentifier
     *     The nameIdentifier
     */
    @JsonProperty("nameIdentifier")
    public void setNameIdentifier(
        com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.NameIdentifier nameIdentifier) {
        this.nameIdentifier = nameIdentifier;
    }

    public PresentationIdentifier withNameIdentifier(NameIdentifier nameIdentifier) {
        this.nameIdentifier = nameIdentifier;
        return this;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(idIdentifier).append(nameIdentifier).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof PresentationIdentifier) == false) {
            return false;
        }
        PresentationIdentifier rhs = ((PresentationIdentifier) other);
        return new EqualsBuilder().append(idIdentifier, rhs.idIdentifier).append(nameIdentifier, rhs.nameIdentifier).isEquals();
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
