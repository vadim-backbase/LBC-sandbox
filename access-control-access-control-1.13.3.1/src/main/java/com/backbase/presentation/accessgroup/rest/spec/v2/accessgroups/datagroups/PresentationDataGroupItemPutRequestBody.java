
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
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.PresentationAction;
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
 * Data group items by identifier update put
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
    "dataGroupIdentifier",
    "action",
    "type",
    "dataItems"
})
@JsonIgnoreProperties(ignoreUnknown = true)
public class PresentationDataGroupItemPutRequestBody implements AdditionalPropertiesAware
{

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("dataGroupIdentifier")
    @Valid
    @NotNull
    private PresentationIdentifier dataGroupIdentifier;
    /**
     * Presentation action
     * (Required)
     * 
     */
    @JsonProperty("action")
    @NotNull
    private PresentationAction action;
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

    public PresentationDataGroupItemPutRequestBody withDataGroupIdentifier(PresentationIdentifier dataGroupIdentifier) {
        this.dataGroupIdentifier = dataGroupIdentifier;
        return this;
    }

    /**
     * Presentation action
     * (Required)
     * 
     * @return
     *     The action
     */
    @JsonProperty("action")
    public PresentationAction getAction() {
        return action;
    }

    /**
     * Presentation action
     * (Required)
     * 
     * @param action
     *     The action
     */
    @JsonProperty("action")
    public void setAction(PresentationAction action) {
        this.action = action;
    }

    public PresentationDataGroupItemPutRequestBody withAction(PresentationAction action) {
        this.action = action;
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

    public PresentationDataGroupItemPutRequestBody withType(String type) {
        this.type = type;
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

    public PresentationDataGroupItemPutRequestBody withDataItems(List<PresentationItemIdentifier> dataItems) {
        this.dataItems = dataItems;
        return this;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(dataGroupIdentifier).append(action).append(type).append(dataItems).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof PresentationDataGroupItemPutRequestBody) == false) {
            return false;
        }
        PresentationDataGroupItemPutRequestBody rhs = ((PresentationDataGroupItemPutRequestBody) other);
        return new EqualsBuilder().append(dataGroupIdentifier, rhs.dataGroupIdentifier).append(action, rhs.action).append(type, rhs.type).append(dataItems, rhs.dataItems).isEquals();
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
