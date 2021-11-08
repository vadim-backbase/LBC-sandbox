
package com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.users;

import com.backbase.buildingblocks.persistence.model.AdditionalPropertiesAware;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Generated;
import javax.validation.constraints.Pattern;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Data item object
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
    "id",
    "dataType"
})
public class PersistenceDataItem implements AdditionalPropertiesAware
{

    /**
     * Universally Unique Identifier.
     * 
     */
    @JsonProperty("id")
    @Pattern(regexp = "^[0-9a-f]{32}$|^[0-9a-f-]{36}$")
    private String id;
    /**
     * Type of the data item
     * 
     */
    @JsonProperty("dataType")
    private String dataType;
    /**
     * Additional Properties
     * 
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Map<String, String> additions = new HashMap<String, String>();

    /**
     * Universally Unique Identifier.
     * 
     * @return
     *     The id
     */
    @JsonProperty("id")
    public String getId() {
        return id;
    }

    /**
     * Universally Unique Identifier.
     * 
     * @param id
     *     The id
     */
    @JsonProperty("id")
    public void setId(String id) {
        this.id = id;
    }

    public PersistenceDataItem withId(String id) {
        this.id = id;
        return this;
    }

    /**
     * Type of the data item
     * 
     * @return
     *     The dataType
     */
    @JsonProperty("dataType")
    public String getDataType() {
        return dataType;
    }

    /**
     * Type of the data item
     * 
     * @param dataType
     *     The dataType
     */
    @JsonProperty("dataType")
    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public PersistenceDataItem withDataType(String dataType) {
        this.dataType = dataType;
        return this;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(id).append(dataType).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof PersistenceDataItem) == false) {
            return false;
        }
        PersistenceDataItem rhs = ((PersistenceDataItem) other);
        return new EqualsBuilder().append(id, rhs.id).append(dataType, rhs.dataType).isEquals();
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
