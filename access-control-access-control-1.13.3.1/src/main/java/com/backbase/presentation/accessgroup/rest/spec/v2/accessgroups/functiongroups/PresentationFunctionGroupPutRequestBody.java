
package com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.functiongroups;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Generated;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import com.backbase.buildingblocks.persistence.model.AdditionalPropertiesAware;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.PresentationIdentifier;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Function group update put
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
    "functionGroup",
    "identifier"
})
@JsonIgnoreProperties(ignoreUnknown = true)
public class PresentationFunctionGroupPutRequestBody implements AdditionalPropertiesAware
{

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("functionGroup")
    @Valid
    @NotNull
    private FunctionGroup functionGroup;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("identifier")
    @Valid
    @NotNull
    private PresentationIdentifier identifier;
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
     *     The functionGroup
     */
    @JsonProperty("functionGroup")
    public FunctionGroup getFunctionGroup() {
        return functionGroup;
    }

    /**
     * 
     * (Required)
     * 
     * @param functionGroup
     *     The functionGroup
     */
    @JsonProperty("functionGroup")
    public void setFunctionGroup(FunctionGroup functionGroup) {
        this.functionGroup = functionGroup;
    }

    public PresentationFunctionGroupPutRequestBody withFunctionGroup(FunctionGroup functionGroup) {
        this.functionGroup = functionGroup;
        return this;
    }

    /**
     * 
     * (Required)
     * 
     * @return
     *     The identifier
     */
    @JsonProperty("identifier")
    public PresentationIdentifier getIdentifier() {
        return identifier;
    }

    /**
     * 
     * (Required)
     * 
     * @param identifier
     *     The identifier
     */
    @JsonProperty("identifier")
    public void setIdentifier(PresentationIdentifier identifier) {
        this.identifier = identifier;
    }

    public PresentationFunctionGroupPutRequestBody withIdentifier(PresentationIdentifier identifier) {
        this.identifier = identifier;
        return this;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(functionGroup).append(identifier).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof PresentationFunctionGroupPutRequestBody) == false) {
            return false;
        }
        PresentationFunctionGroupPutRequestBody rhs = ((PresentationFunctionGroupPutRequestBody) other);
        return new EqualsBuilder().append(functionGroup, rhs.functionGroup).append(identifier, rhs.identifier).isEquals();
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
