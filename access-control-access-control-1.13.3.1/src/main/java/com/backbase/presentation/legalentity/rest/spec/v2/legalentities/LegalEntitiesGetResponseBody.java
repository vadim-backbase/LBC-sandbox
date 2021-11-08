
package com.backbase.presentation.legalentity.rest.spec.v2.legalentities;

import javax.annotation.Generated;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.enumeration.LegalEntityType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
    "parentId",
    "isParent"
})
@JsonIgnoreProperties(ignoreUnknown = true)
public class LegalEntitiesGetResponseBody
    extends LegalEntityBase
{

    /**
     * Universally Unique IDentifier.
     * 
     */
    @JsonProperty("parentId")
    @Pattern(regexp = "^[0-9a-f]{32}$|^[0-9a-f-]{36}$")
    @Size(min = 1, max = 36)
    private String parentId;
    @JsonProperty("isParent")
    private Boolean isParent = false;

    /**
     * Universally Unique IDentifier.
     * 
     * @return
     *     The parentId
     */
    @JsonProperty("parentId")
    public String getParentId() {
        return parentId;
    }

    /**
     * Universally Unique IDentifier.
     * 
     * @param parentId
     *     The parentId
     */
    @JsonProperty("parentId")
    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public LegalEntitiesGetResponseBody withParentId(String parentId) {
        this.parentId = parentId;
        return this;
    }

    /**
     * 
     * @return
     *     The isParent
     */
    @JsonProperty("isParent")
    public Boolean getIsParent() {
        return isParent;
    }

    /**
     * 
     * @param isParent
     *     The isParent
     */
    @JsonProperty("isParent")
    public void setIsParent(Boolean isParent) {
        this.isParent = isParent;
    }

    public LegalEntitiesGetResponseBody withIsParent(Boolean isParent) {
        this.isParent = isParent;
        return this;
    }

    @Override
    public LegalEntitiesGetResponseBody withId(String id) {
        super.withId(id);
        return this;
    }

    @Override
    public LegalEntitiesGetResponseBody withExternalId(String externalId) {
        super.withExternalId(externalId);
        return this;
    }

    @Override
    public LegalEntitiesGetResponseBody withName(String name) {
        super.withName(name);
        return this;
    }

    @Override
    public LegalEntitiesGetResponseBody withType(LegalEntityType type) {
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
        if ((other instanceof LegalEntitiesGetResponseBody) == false) {
            return false;
        }
        LegalEntitiesGetResponseBody rhs = ((LegalEntitiesGetResponseBody) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(parentId, rhs.parentId).append(isParent, rhs.isParent).isEquals();
    }

}
