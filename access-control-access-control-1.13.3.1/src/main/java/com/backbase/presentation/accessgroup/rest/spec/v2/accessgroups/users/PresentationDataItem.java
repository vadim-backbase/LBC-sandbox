
package com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.users;

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
public class PresentationDataItem implements AdditionalPropertiesAware
{

    /**
     * Universally Unique Identifier.
     * 
     */
    @JsonProperty("id")
    @Pattern(regexp = "^[0-9a-f]{32}$|^[0-9a-f-]{36}$")
    @Size(min = 1, max = 36)
    private String id;
    /**
     * Data group type
     * 
     */
    @JsonProperty("dataType")
    @Pattern(regexp = "^\\S+$")
    @Size(min = 1, max = 36)
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

    public PresentationDataItem withId(String id) {
        this.id = id;
        return this;
    }

    /**
     * Data group type
     * 
     * @return
     *     The dataType
     */
    @JsonProperty("dataType")
    public String getDataType() {
        return dataType;
    }

    /**
     * Data group type
     * 
     * @param dataType
     *     The dataType
     */
    @JsonProperty("dataType")
    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public PresentationDataItem withDataType(String dataType) {
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
        if ((other instanceof PresentationDataItem) == false) {
            return false;
        }
        PresentationDataItem rhs = ((PresentationDataItem) other);
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
