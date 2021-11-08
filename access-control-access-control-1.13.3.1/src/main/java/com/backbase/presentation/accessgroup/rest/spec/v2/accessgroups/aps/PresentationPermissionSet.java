
package com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.aps;

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
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Assignable permission set
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
    "name",
    "description",
    "permissions"
})
@JsonIgnoreProperties(ignoreUnknown = true)
public class PresentationPermissionSet implements AdditionalPropertiesAware
{

    /**
     * Assignable permission set name
     * (Required)
     * 
     */
    @JsonProperty("name")
    @Pattern(regexp = "^\\S(.*(\\S))?$")
    @Size(min = 1, max = 128)
    @NotNull
    private String name;
    /**
     * Assignable permission set description
     * (Required)
     * 
     */
    @JsonProperty("description")
    @Pattern(regexp = "^(\\S|\\n)((.|\\n)*(\\S|\\n))?$")
    @Size(min = 1, max = 255)
    @NotNull
    private String description;
    /**
     * List of paired business function id and privileges
     * 
     */
    @JsonProperty("permissions")
    @Valid
    private List<PresentationPermissionSetItem> permissions = new ArrayList<PresentationPermissionSetItem>();
    /**
     * Additional Properties
     * 
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Map<String, String> additions = new HashMap<String, String>();

    /**
     * Assignable permission set name
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
     * Assignable permission set name
     * (Required)
     * 
     * @param name
     *     The name
     */
    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    public PresentationPermissionSet withName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Assignable permission set description
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
     * Assignable permission set description
     * (Required)
     * 
     * @param description
     *     The description
     */
    @JsonProperty("description")
    public void setDescription(String description) {
        this.description = description;
    }

    public PresentationPermissionSet withDescription(String description) {
        this.description = description;
        return this;
    }

    /**
     * List of paired business function id and privileges
     * 
     * @return
     *     The permissions
     */
    @JsonProperty("permissions")
    public List<PresentationPermissionSetItem> getPermissions() {
        return permissions;
    }

    /**
     * List of paired business function id and privileges
     * 
     * @param permissions
     *     The permissions
     */
    @JsonProperty("permissions")
    public void setPermissions(List<PresentationPermissionSetItem> permissions) {
        this.permissions = permissions;
    }

    public PresentationPermissionSet withPermissions(List<PresentationPermissionSetItem> permissions) {
        this.permissions = permissions;
        return this;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(name).append(description).append(permissions).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof PresentationPermissionSet) == false) {
            return false;
        }
        PresentationPermissionSet rhs = ((PresentationPermissionSet) other);
        return new EqualsBuilder().append(name, rhs.name).append(description, rhs.description).append(permissions, rhs.permissions).isEquals();
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
