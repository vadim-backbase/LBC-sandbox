
package com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.usercontext;

import com.backbase.buildingblocks.persistence.model.AdditionalPropertiesAware;
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

@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
    "elements",
    "totalElements"
})
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserContextsGetResponseBody implements AdditionalPropertiesAware
{

    /**
     * List of user contexts
     * (Required)
     * 
     */
    @JsonProperty("elements")
    @Valid
    @NotNull
    private List<Element> elements = new ArrayList<Element>();
    /**
     * total number of elements matching the query
     * (Required)
     * 
     */
    @JsonProperty("totalElements")
    @NotNull
    private Long totalElements;
    /**
     * Additional Properties
     * 
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Map<String, String> additions = new HashMap<String, String>();

    /**
     * List of user contexts
     * (Required)
     * 
     * @return
     *     The elements
     */
    @JsonProperty("elements")
    public List<Element> getElements() {
        return elements;
    }

    /**
     * List of user contexts
     * (Required)
     * 
     * @param elements
     *     The elements
     */
    @JsonProperty("elements")
    public void setElements(List<Element> elements) {
        this.elements = elements;
    }

    public UserContextsGetResponseBody withElements(List<Element> elements) {
        this.elements = elements;
        return this;
    }

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

    public UserContextsGetResponseBody withTotalElements(Long totalElements) {
        this.totalElements = totalElements;
        return this;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(elements).append(totalElements).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof UserContextsGetResponseBody) == false) {
            return false;
        }
        UserContextsGetResponseBody rhs = ((UserContextsGetResponseBody) other);
        return new EqualsBuilder().append(elements, rhs.elements).append(totalElements, rhs.totalElements).isEquals();
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
