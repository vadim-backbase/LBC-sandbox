
package com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.legalentities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import javax.annotation.Generated;
import javax.validation.constraints.Pattern;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Legal entity item
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
    "parentId",
    "isParent"
})
@JsonIgnoreProperties(ignoreUnknown = true)
public class SegmentationGetResponseBodyQuery
    extends LegalEntityBase
{

    /**
     * Universally Unique Identifier.
     * 
     */
    @JsonProperty("parentId")
    @Pattern(regexp = "^[0-9a-f]{32}$|^[0-9a-f-]{36}$")
    private String parentId;
    /**
     * Defines whether the legal entity is parent
     * 
     */
    @JsonProperty("isParent")
    private Boolean isParent = false;

    /**
     * Universally Unique Identifier.
     * 
     * @return
     *     The parentId
     */
    @JsonProperty("parentId")
    public String getParentId() {
        return parentId;
    }

    /**
     * Universally Unique Identifier.
     * 
     * @param parentId
     *     The parentId
     */
    @JsonProperty("parentId")
    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public SegmentationGetResponseBodyQuery withParentId(String parentId) {
        this.parentId = parentId;
        return this;
    }

    /**
     * Defines whether the legal entity is parent
     * 
     * @return
     *     The isParent
     */
    @JsonProperty("isParent")
    public Boolean getIsParent() {
        return isParent;
    }

    /**
     * Defines whether the legal entity is parent
     * 
     * @param isParent
     *     The isParent
     */
    @JsonProperty("isParent")
    public void setIsParent(Boolean isParent) {
        this.isParent = isParent;
    }

    public SegmentationGetResponseBodyQuery withIsParent(Boolean isParent) {
        this.isParent = isParent;
        return this;
    }

    @Override
    public SegmentationGetResponseBodyQuery withId(String id) {
        super.withId(id);
        return this;
    }

    @Override
    public SegmentationGetResponseBodyQuery withExternalId(String externalId) {
        super.withExternalId(externalId);
        return this;
    }

    @Override
    public SegmentationGetResponseBodyQuery withName(String name) {
        super.withName(name);
        return this;
    }

    @Override
    public SegmentationGetResponseBodyQuery withType(Type type) {
        super.withType(type);
        return this;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(parentId).append(isParent).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof SegmentationGetResponseBodyQuery) == false) {
            return false;
        }
        SegmentationGetResponseBodyQuery rhs = ((SegmentationGetResponseBodyQuery) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(parentId, rhs.parentId).append(isParent, rhs.isParent).isEquals();
    }

}
