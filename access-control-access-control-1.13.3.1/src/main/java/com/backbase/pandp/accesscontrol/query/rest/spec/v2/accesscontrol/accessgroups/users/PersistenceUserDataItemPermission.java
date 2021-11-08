
package com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.users;

import com.backbase.buildingblocks.persistence.model.AdditionalPropertiesAware;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Generated;
import javax.validation.Valid;
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
public class PersistenceUserDataItemPermission implements AdditionalPropertiesAware
{

    /**
     * Data item object
     * 
     */
    @JsonProperty("dataItem")
    @Valid
    private PersistenceDataItem dataItem;
    @JsonProperty("permissions")
    @Valid
    private List<PersistenceUserPermission> permissions = new ArrayList<PersistenceUserPermission>();
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
    public PersistenceDataItem getDataItem() {
        return dataItem;
    }

    /**
     * Data item object
     * 
     * @param dataItem
     *     The dataItem
     */
    @JsonProperty("dataItem")
    public void setDataItem(PersistenceDataItem dataItem) {
        this.dataItem = dataItem;
    }

    public PersistenceUserDataItemPermission withDataItem(PersistenceDataItem dataItem) {
        this.dataItem = dataItem;
        return this;
    }

    /**
     * 
     * @return
     *     The permissions
     */
    @JsonProperty("permissions")
    public List<PersistenceUserPermission> getPermissions() {
        return permissions;
    }

    /**
     * 
     * @param permissions
     *     The permissions
     */
    @JsonProperty("permissions")
    public void setPermissions(List<PersistenceUserPermission> permissions) {
        this.permissions = permissions;
    }

    public PersistenceUserDataItemPermission withPermissions(List<PersistenceUserPermission> permissions) {
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
        if ((other instanceof PersistenceUserDataItemPermission) == false) {
            return false;
        }
        PersistenceUserDataItemPermission rhs = ((PersistenceUserDataItemPermission) other);
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
