
package com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.aps;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Generated;
import javax.validation.Valid;
import com.backbase.buildingblocks.persistence.model.AdditionalPropertiesAware;
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
    "id",
    "name",
    "description",
    "type",
    "permissions"
})
@JsonIgnoreProperties(ignoreUnknown = true)
public class PresentationPermissionSetResponseItem implements AdditionalPropertiesAware
{

    /**
     * Internal id of the assignable permission set.
     * 
     */
    @JsonProperty("id")
    private BigDecimal id;
    /**
     * Name of the assignable permission set.
     * 
     */
    @JsonProperty("name")
    private String name;
    /**
     * Description of the assignable permission set.
     * 
     */
    @JsonProperty("description")
    private String description;
    /**
     * Type of the assignable permission set.
     * 
     */
    @JsonProperty("type")
    private String type;
    /**
     * Assignable permissions.
     * 
     */
    @JsonProperty("permissions")
    @Valid
    private List<PresentationPermissionItem> permissions = new ArrayList<PresentationPermissionItem>();
    /**
     * Additional Properties
     * 
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Map<String, String> additions = new HashMap<String, String>();

    /**
     * Internal id of the assignable permission set.
     * 
     * @return
     *     The id
     */
    @JsonProperty("id")
    public BigDecimal getId() {
        return id;
    }

    /**
     * Internal id of the assignable permission set.
     * 
     * @param id
     *     The id
     */
    @JsonProperty("id")
    public void setId(BigDecimal id) {
        this.id = id;
    }

    public PresentationPermissionSetResponseItem withId(BigDecimal id) {
        this.id = id;
        return this;
    }

    /**
     * Name of the assignable permission set.
     * 
     * @return
     *     The name
     */
    @JsonProperty("name")
    public String getName() {
        return name;
    }

    /**
     * Name of the assignable permission set.
     * 
     * @param name
     *     The name
     */
    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    public PresentationPermissionSetResponseItem withName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Description of the assignable permission set.
     * 
     * @return
     *     The description
     */
    @JsonProperty("description")
    public String getDescription() {
        return description;
    }

    /**
     * Description of the assignable permission set.
     * 
     * @param description
     *     The description
     */
    @JsonProperty("description")
    public void setDescription(String description) {
        this.description = description;
    }

    public PresentationPermissionSetResponseItem withDescription(String description) {
        this.description = description;
        return this;
    }

    /**
     * Type of the assignable permission set.
     * 
     * @return
     *     The type
     */
    @JsonProperty("type")
    public String getType() {
        return type;
    }

    /**
     * Type of the assignable permission set.
     * 
     * @param type
     *     The type
     */
    @JsonProperty("type")
    public void setType(String type) {
        this.type = type;
    }

    public PresentationPermissionSetResponseItem withType(String type) {
        this.type = type;
        return this;
    }

    /**
     * Assignable permissions.
     * 
     * @return
     *     The permissions
     */
    @JsonProperty("permissions")
    public List<PresentationPermissionItem> getPermissions() {
        return permissions;
    }

    /**
     * Assignable permissions.
     * 
     * @param permissions
     *     The permissions
     */
    @JsonProperty("permissions")
    public void setPermissions(List<PresentationPermissionItem> permissions) {
        this.permissions = permissions;
    }

    public PresentationPermissionSetResponseItem withPermissions(List<PresentationPermissionItem> permissions) {
        this.permissions = permissions;
        return this;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(id).append(name).append(description).append(type).append(permissions).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof PresentationPermissionSetResponseItem) == false) {
            return false;
        }
        PresentationPermissionSetResponseItem rhs = ((PresentationPermissionSetResponseItem) other);
        return new EqualsBuilder().append(id, rhs.id).append(name, rhs.name).append(description, rhs.description).append(type, rhs.type).append(permissions, rhs.permissions).isEquals();
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
