
package com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.datagroups;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Generated;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
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
    "name",
    "description",
    "type",
    "items"
})
public class DataGroupsPostRequestBodyParent implements AdditionalPropertiesAware
{

    /**
     * Data group name
     * (Required)
     * 
     */
    @JsonProperty("name")
    @Pattern(regexp = "^\\S(.*(\\S))?$")
    @Size(min = 1, max = 128)
    @NotNull
    private String name;
    /**
     * Data group description
     * (Required)
     * 
     */
    @JsonProperty("description")
    @Pattern(regexp = "^(\\S|\\n)((.|\\n)*(\\S|\\n))?$")
    @Size(min = 1, max = 255)
    @NotNull
    private String description;
    /**
     * Data group type
     * (Required)
     * 
     */
    @JsonProperty("type")
    @Pattern(regexp = "^\\S+$")
    @Size(min = 1, max = 36)
    @NotNull
    private String type;
    /**
     * Data group items
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
     * Data group name
     * (Required)
     * 
     * @return
     *     The name
     */
    @JsonProperty("name")
    public String getName() {
        return name;
    }

    /**
     * Data group name
     * (Required)
     * 
     * @param name
     *     The name
     */
    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    public DataGroupsPostRequestBodyParent withName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Data group description
     * (Required)
     * 
     * @return
     *     The description
     */
    @JsonProperty("description")
    public String getDescription() {
        return description;
    }

    /**
     * Data group description
     * (Required)
     * 
     * @param description
     *     The description
     */
    @JsonProperty("description")
    public void setDescription(String description) {
        this.description = description;
    }

    public DataGroupsPostRequestBodyParent withDescription(String description) {
        this.description = description;
        return this;
    }

    /**
     * Data group type
     * (Required)
     * 
     * @return
     *     The type
     */
    @JsonProperty("type")
    public String getType() {
        return type;
    }

    /**
     * Data group type
     * (Required)
     * 
     * @param type
     *     The type
     */
    @JsonProperty("type")
    public void setType(String type) {
        this.type = type;
    }

    public DataGroupsPostRequestBodyParent withType(String type) {
        this.type = type;
        return this;
    }

    /**
     * Data group items
     * 
     * @return
     *     The items
     */
    @JsonProperty("items")
    public List<String> getItems() {
        return items;
    }

    /**
     * Data group items
     * 
     * @param items
     *     The items
     */
    @JsonProperty("items")
    public void setItems(List<String> items) {
        this.items = items;
    }

    public DataGroupsPostRequestBodyParent withItems(List<String> items) {
        this.items = items;
        return this;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(name).append(description).append(type).append(items).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof DataGroupsPostRequestBodyParent) == false) {
            return false;
        }
        DataGroupsPostRequestBodyParent rhs = ((DataGroupsPostRequestBodyParent) other);
        return new EqualsBuilder().append(name, rhs.name).append(description, rhs.description).append(type, rhs.type).append(items, rhs.items).isEquals();
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
