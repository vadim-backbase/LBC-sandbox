
package com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.users;

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


/**
 * User access object
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
    "dataItem",
    "permissions"
})
@JsonIgnoreProperties(ignoreUnknown = true)
public class PresentationUserDataItemPermission implements AdditionalPropertiesAware
{

    /**
     * Data item object
     * 
     */
    @JsonProperty("dataItem")
    @Valid
    private com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.users.PresentationDataItem dataItem;
    @JsonProperty("permissions")
    @Valid
    private List<com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.users.PresentationUserPermission> permissions = new ArrayList<com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.users.PresentationUserPermission>();
    /**
     * Additional Properties
     * 
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Map<String, String> additions = new HashMap<String, String>();

    /**
     * Data item object
     * 
     * @return
     *     The dataItem
     */
    @JsonProperty("dataItem")
    public com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.users.PresentationDataItem getDataItem() {
        return dataItem;
    }

    /**
     * Data item object
     * 
     * @param dataItem
     *     The dataItem
     */
    @JsonProperty("dataItem")
    public void setDataItem(
        com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.users.PresentationDataItem dataItem) {
        this.dataItem = dataItem;
    }

    public PresentationUserDataItemPermission withDataItem(PresentationDataItem dataItem) {
        this.dataItem = dataItem;
        return this;
    }

    /**
     * 
     * @return
     *     The permissions
     */
    @JsonProperty("permissions")
    public List<com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.users.PresentationUserPermission> getPermissions() {
        return permissions;
    }

    /**
     * 
     * @param permissions
     *     The permissions
     */
    @JsonProperty("permissions")
    public void setPermissions(List<com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.users.PresentationUserPermission> permissions) {
        this.permissions = permissions;
    }

    public PresentationUserDataItemPermission withPermissions(List<PresentationUserPermission> permissions) {
        this.permissions = permissions;
        return this;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(dataItem).append(permissions).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof PresentationUserDataItemPermission) == false) {
            return false;
        }
        PresentationUserDataItemPermission rhs = ((PresentationUserDataItemPermission) other);
        return new EqualsBuilder().append(dataItem, rhs.dataItem).append(permissions, rhs.permissions).isEquals();
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
