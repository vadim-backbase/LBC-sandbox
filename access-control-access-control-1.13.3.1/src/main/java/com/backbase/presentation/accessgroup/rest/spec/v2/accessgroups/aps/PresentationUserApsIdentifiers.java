
package com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.aps;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import javax.annotation.Generated;
import javax.validation.Valid;
import com.backbase.buildingblocks.persistence.model.AdditionalPropertiesAware;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
    "nameIdentifiers",
    "idIdentifiers"
})
public class PresentationUserApsIdentifiers implements AdditionalPropertiesAware
{

    @JsonProperty("nameIdentifiers")
    @JsonDeserialize(as = LinkedHashSet.class)
    @Valid
    private Set<String> nameIdentifiers = new LinkedHashSet<String>();
    @JsonProperty("idIdentifiers")
    @JsonDeserialize(as = LinkedHashSet.class)
    @Valid
    private Set<BigDecimal> idIdentifiers = new LinkedHashSet<BigDecimal>();
    /**
     * Additional Properties
     * 
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Map<String, String> additions = new HashMap<String, String>();

    /**
     * 
     * @return
     *     The nameIdentifiers
     */
    @JsonProperty("nameIdentifiers")
    public Set<String> getNameIdentifiers() {
        return nameIdentifiers;
    }

    /**
     * 
     * @param nameIdentifiers
     *     The nameIdentifiers
     */
    @JsonProperty("nameIdentifiers")
    public void setNameIdentifiers(Set<String> nameIdentifiers) {
        this.nameIdentifiers = nameIdentifiers;
    }

    public PresentationUserApsIdentifiers withNameIdentifiers(Set<String> nameIdentifiers) {
        this.nameIdentifiers = nameIdentifiers;
        return this;
    }

    /**
     * 
     * @return
     *     The idIdentifiers
     */
    @JsonProperty("idIdentifiers")
    public Set<BigDecimal> getIdIdentifiers() {
        return idIdentifiers;
    }

    /**
     * 
     * @param idIdentifiers
     *     The idIdentifiers
     */
    @JsonProperty("idIdentifiers")
    public void setIdIdentifiers(Set<BigDecimal> idIdentifiers) {
        this.idIdentifiers = idIdentifiers;
    }

    public PresentationUserApsIdentifiers withIdIdentifiers(Set<BigDecimal> idIdentifiers) {
        this.idIdentifiers = idIdentifiers;
        return this;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(nameIdentifiers).append(idIdentifiers).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof PresentationUserApsIdentifiers) == false) {
            return false;
        }
        PresentationUserApsIdentifiers rhs = ((PresentationUserApsIdentifiers) other);
        return new EqualsBuilder().append(nameIdentifiers, rhs.nameIdentifiers).append(idIdentifiers, rhs.idIdentifiers).isEquals();
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
