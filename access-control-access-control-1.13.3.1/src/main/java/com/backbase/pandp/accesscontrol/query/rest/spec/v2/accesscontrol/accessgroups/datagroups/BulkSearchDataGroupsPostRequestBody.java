
package com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.datagroups;

import com.backbase.buildingblocks.persistence.model.AdditionalPropertiesAware;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import javax.annotation.Generated;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
    "ids"
})
@JsonIgnoreProperties(ignoreUnknown = true)
public class BulkSearchDataGroupsPostRequestBody implements AdditionalPropertiesAware
{

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("ids")
    @JsonDeserialize(as = LinkedHashSet.class)
    @Size(min = 1)
    @Valid
    @NotNull
    private Set<String> ids = new LinkedHashSet<String>();
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
     *     The ids
     */
    @JsonProperty("ids")
    public Set<String> getIds() {
        return ids;
    }

    /**
     * 
     * (Required)
     * 
     * @param ids
     *     The ids
     */
    @JsonProperty("ids")
    public void setIds(Set<String> ids) {
        this.ids = ids;
    }

    public BulkSearchDataGroupsPostRequestBody withIds(Set<String> ids) {
        this.ids = ids;
        return this;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(ids).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof BulkSearchDataGroupsPostRequestBody) == false) {
            return false;
        }
        BulkSearchDataGroupsPostRequestBody rhs = ((BulkSearchDataGroupsPostRequestBody) other);
        return new EqualsBuilder().append(ids, rhs.ids).isEquals();
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
