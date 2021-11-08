
package com.backbase.presentation.legalentity.rest.spec.v2.legalentities;

import java.util.LinkedHashSet;
import java.util.Set;
import javax.annotation.Generated;
import javax.validation.Valid;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
    "parentEntityId",
    "excludeIds"
})
@JsonIgnoreProperties(ignoreUnknown = true)
public class SearchSubEntitiesParameters
    extends PageableSearchableParameters
{

    /**
     * Universally Unique IDentifier.
     * 
     */
    @JsonProperty("parentEntityId")
    @Pattern(regexp = "^[0-9a-f]{32}$|^[0-9a-f-]{36}$")
    @Size(min = 1, max = 36)
    private String parentEntityId;
    @JsonProperty("excludeIds")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    @Valid
    private Set<String> excludeIds = new LinkedHashSet<String>();

    /**
     * Universally Unique IDentifier.
     * 
     * @return
     *     The parentEntityId
     */
    @JsonProperty("parentEntityId")
    public String getParentEntityId() {
        return parentEntityId;
    }

    /**
     * Universally Unique IDentifier.
     * 
     * @param parentEntityId
     *     The parentEntityId
     */
    @JsonProperty("parentEntityId")
    public void setParentEntityId(String parentEntityId) {
        this.parentEntityId = parentEntityId;
    }

    public SearchSubEntitiesParameters withParentEntityId(String parentEntityId) {
        this.parentEntityId = parentEntityId;
        return this;
    }

    /**
     * 
     * @return
     *     The excludeIds
     */
    @JsonProperty("excludeIds")
    public Set<String> getExcludeIds() {
        return excludeIds;
    }

    /**
     * 
     * @param excludeIds
     *     The excludeIds
     */
    @JsonProperty("excludeIds")
    public void setExcludeIds(Set<String> excludeIds) {
        this.excludeIds = excludeIds;
    }

    public SearchSubEntitiesParameters withExcludeIds(Set<String> excludeIds) {
        this.excludeIds = excludeIds;
        return this;
    }

    @Override
    public SearchSubEntitiesParameters withQuery(String query) {
        super.withQuery(query);
        return this;
    }

    @Override
    public SearchSubEntitiesParameters withCursor(String cursor) {
        super.withCursor(cursor);
        return this;
    }

    @Override
    public SearchSubEntitiesParameters withFrom(Integer from) {
        super.withFrom(from);
        return this;
    }

    @Override
    public SearchSubEntitiesParameters withSize(Integer size) {
        super.withSize(size);
        return this;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(parentEntityId).append(excludeIds).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof SearchSubEntitiesParameters) == false) {
            return false;
        }
        SearchSubEntitiesParameters rhs = ((SearchSubEntitiesParameters) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(parentEntityId, rhs.parentEntityId).append(excludeIds, rhs.excludeIds).isEquals();
    }

}
