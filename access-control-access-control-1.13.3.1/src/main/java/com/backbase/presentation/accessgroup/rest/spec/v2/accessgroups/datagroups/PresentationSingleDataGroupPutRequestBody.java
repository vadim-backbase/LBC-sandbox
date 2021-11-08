
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
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.PresentationIdentifier;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.PresentationItemIdentifier;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Data group update put
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
    "name",
    "description",
    "type",
    "dataGroupIdentifier",
    "dataItems"
})
@JsonIgnoreProperties(ignoreUnknown = true)
public class PresentationSingleDataGroupPutRequestBody implements AdditionalPropertiesAware
{

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("name")
    @Size(min = 1, max = 128)
    @NotNull
    private String name;
    /**
     * 
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
     * 
     * (Required)
     * 
     */
    @JsonProperty("dataGroupIdentifier")
    @Valid
    @NotNull
    private PresentationIdentifier dataGroupIdentifier;
    @JsonProperty("dataItems")
    @Valid
    private List<PresentationItemIdentifier> dataItems = new ArrayList<PresentationItemIdentifier>();
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
     *     The name
     */
    @JsonProperty("name")
    public String getName() {
        return name;
    }

    /**
     * 
     * (Required)
     * 
     * @param name
     *     The name
     */
    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    public PresentationSingleDataGroupPutRequestBody withName(String name) {
        this.name = name;
        return this;
    }

    /**
     * 
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
     * 
     * (Required)
     * 
     * @param description
     *     The description
     */
    @JsonProperty("description")
    public void setDescription(String description) {
        this.description = description;
    }

    public PresentationSingleDataGroupPutRequestBody withDescription(String description) {
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

    public PresentationSingleDataGroupPutRequestBody withType(String type) {
        this.type = type;
        return this;
    }

    /**
     * 
     * (Required)
     * 
     * @return
     *     The dataGroupIdentifier
     */
    @JsonProperty("dataGroupIdentifier")
    public PresentationIdentifier getDataGroupIdentifier() {
        return dataGroupIdentifier;
    }

    /**
     * 
     * (Required)
     * 
     * @param dataGroupIdentifier
     *     The dataGroupIdentifier
     */
    @JsonProperty("dataGroupIdentifier")
    public void setDataGroupIdentifier(PresentationIdentifier dataGroupIdentifier) {
        this.dataGroupIdentifier = dataGroupIdentifier;
    }

    public PresentationSingleDataGroupPutRequestBody withDataGroupIdentifier(PresentationIdentifier dataGroupIdentifier) {
        this.dataGroupIdentifier = dataGroupIdentifier;
        return this;
    }

    /**
     * 
     * @return
     *     The dataItems
     */
    @JsonProperty("dataItems")
    public List<PresentationItemIdentifier> getDataItems() {
        return dataItems;
    }

    /**
     * 
     * @param dataItems
     *     The dataItems
     */
    @JsonProperty("dataItems")
    public void setDataItems(List<PresentationItemIdentifier> dataItems) {
        this.dataItems = dataItems;
    }

    public PresentationSingleDataGroupPutRequestBody withDataItems(List<PresentationItemIdentifier> dataItems) {
        this.dataItems = dataItems;
        return this;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(name).append(description).append(type).append(dataGroupIdentifier).append(dataItems).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof PresentationSingleDataGroupPutRequestBody) == false) {
            return false;
        }
        PresentationSingleDataGroupPutRequestBody rhs = ((PresentationSingleDataGroupPutRequestBody) other);
        return new EqualsBuilder().append(name, rhs.name).append(description, rhs.description).append(type, rhs.type).append(dataGroupIdentifier, rhs.dataGroupIdentifier).append(dataItems, rhs.dataItems).isEquals();
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
