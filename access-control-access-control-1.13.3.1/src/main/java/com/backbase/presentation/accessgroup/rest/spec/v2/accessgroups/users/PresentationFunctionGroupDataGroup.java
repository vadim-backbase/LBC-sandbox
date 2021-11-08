
package com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.users;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Generated;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import com.backbase.buildingblocks.persistence.model.AdditionalPropertiesAware;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.PresentationIdentifier;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Function/Data group pair
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
    "functionGroupIdentifier",
    "dataGroupIdentifiers"
})
public class PresentationFunctionGroupDataGroup implements AdditionalPropertiesAware
{

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("functionGroupIdentifier")
    @Valid
    @NotNull
    private PresentationIdentifier functionGroupIdentifier;
    /**
     * Identifiers of the data groups that belong to the given function group
     * 
     */
    @JsonProperty("dataGroupIdentifiers")
    @Valid
    private List<PresentationIdentifier> dataGroupIdentifiers = new ArrayList<PresentationIdentifier>();
    /**
     * Additional Properties
     * 
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Map<String, String> additions = new HashMap<String, String>();

    /**
     * 
     * (Required)
     * 
     * @return
     *     The functionGroupIdentifier
     */
    @JsonProperty("functionGroupIdentifier")
    public PresentationIdentifier getFunctionGroupIdentifier() {
        return functionGroupIdentifier;
    }

    /**
     * 
     * (Required)
     * 
     * @param functionGroupIdentifier
     *     The functionGroupIdentifier
     */
    @JsonProperty("functionGroupIdentifier")
    public void setFunctionGroupIdentifier(PresentationIdentifier functionGroupIdentifier) {
        this.functionGroupIdentifier = functionGroupIdentifier;
    }

    public PresentationFunctionGroupDataGroup withFunctionGroupIdentifier(PresentationIdentifier functionGroupIdentifier) {
        this.functionGroupIdentifier = functionGroupIdentifier;
        return this;
    }

    /**
     * Identifiers of the data groups that belong to the given function group
     * 
     * @return
     *     The dataGroupIdentifiers
     */
    @JsonProperty("dataGroupIdentifiers")
    public List<PresentationIdentifier> getDataGroupIdentifiers() {
        return dataGroupIdentifiers;
    }

    /**
     * Identifiers of the data groups that belong to the given function group
     * 
     * @param dataGroupIdentifiers
     *     The dataGroupIdentifiers
     */
    @JsonProperty("dataGroupIdentifiers")
    public void setDataGroupIdentifiers(List<PresentationIdentifier> dataGroupIdentifiers) {
        this.dataGroupIdentifiers = dataGroupIdentifiers;
    }

    public PresentationFunctionGroupDataGroup withDataGroupIdentifiers(List<PresentationIdentifier> dataGroupIdentifiers) {
        this.dataGroupIdentifiers = dataGroupIdentifiers;
        return this;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(functionGroupIdentifier).append(dataGroupIdentifiers).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof PresentationFunctionGroupDataGroup) == false) {
            return false;
        }
        PresentationFunctionGroupDataGroup rhs = ((PresentationFunctionGroupDataGroup) other);
        return new EqualsBuilder().append(functionGroupIdentifier, rhs.functionGroupIdentifier).append(dataGroupIdentifiers, rhs.dataGroupIdentifiers).isEquals();
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
