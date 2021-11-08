
package com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.serviceagreements;

import com.backbase.buildingblocks.persistence.model.AdditionalPropertiesAware;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Generated;
import javax.validation.Valid;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * An object which contain data group id and data item ids.
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
    "id",
    "items"
})
public class PersistenceDataGroupDataItems implements AdditionalPropertiesAware
{

    /**
     * Data group id
     * 
     */
    @JsonProperty("id")
    private String id;
    /**
     * List of data item ids
     * 
     */
    @JsonProperty("items")
    @Valid
    private List<String> items = new ArrayList<String>();
    /**
     * Additional Properties
     * 
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Map<String, String> additions = new HashMap<String, String>();

    /**
     * Data group id
     * 
     * @return
     *     The id
     */
    @JsonProperty("id")
    public String getId() {
        return id;
    }

    /**
     * Data group id
     * 
     * @param id
     *     The id
     */
    @JsonProperty("id")
    public void setId(String id) {
        this.id = id;
    }

    public PersistenceDataGroupDataItems withId(String id) {
        this.id = id;
        return this;
    }

    /**
     * List of data item ids
     * 
     * @return
     *     The items
     */
    @JsonProperty("items")
    public List<String> getItems() {
        return items;
    }

    /**
     * List of data item ids
     * 
     * @param items
     *     The items
     */
    @JsonProperty("items")
    public void setItems(List<String> items) {
        this.items = items;
    }

    public PersistenceDataGroupDataItems withItems(List<String> items) {
        this.items = items;
        return this;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(id).append(items).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof PersistenceDataGroupDataItems) == false) {
            return false;
        }
        PersistenceDataGroupDataItems rhs = ((PersistenceDataGroupDataItems) other);
        return new EqualsBuilder().append(id, rhs.id).append(items, rhs.items).isEquals();
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
