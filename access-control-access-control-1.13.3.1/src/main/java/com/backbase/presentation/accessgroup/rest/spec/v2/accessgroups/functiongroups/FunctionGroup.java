
package com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.functiongroups;

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
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.function.PresentationPermissionFunctionGroupUpdate;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
    "name",
    "description",
    "permissions",
    "validFromDate",
    "validFromTime",
    "validUntilDate",
    "validUntilTime"
})
public class FunctionGroup implements AdditionalPropertiesAware
{

    /**
     * Function group name
     * (Required)
     * 
     */
    @JsonProperty("name")
    @Pattern(regexp = "^\\S(.*(\\S))?$")
    @Size(min = 1, max = 128)
    @NotNull
    private String name;
    /**
     * Function group description
     * (Required)
     * 
     */
    @JsonProperty("description")
    @Pattern(regexp = "^(\\S|\\n)((.|\\n)*(\\S|\\n))?$")
    @Size(min = 1, max = 255)
    @NotNull
    private String description;
    @JsonProperty("permissions")
    @Valid
    private List<PresentationPermissionFunctionGroupUpdate> permissions = new ArrayList<PresentationPermissionFunctionGroupUpdate>();
    /**
     * Start date of the function group.
     * 
     */
    @JsonProperty("validFromDate")
    private String validFromDate;
    /**
     * Start time of the function group.
     * 
     */
    @JsonProperty("validFromTime")
    private String validFromTime;
    /**
     * End date of the function group.
     * 
     */
    @JsonProperty("validUntilDate")
    private String validUntilDate;
    /**
     * End time of the function group.
     * 
     */
    @JsonProperty("validUntilTime")
    private String validUntilTime;
    /**
     * Additional Properties
     * 
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Map<String, String> additions = new HashMap<String, String>();

    /**
     * Function group name
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
     * Function group name
     * (Required)
     * 
     * @param name
     *     The name
     */
    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    public FunctionGroup withName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Function group description
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
     * Function group description
     * (Required)
     * 
     * @param description
     *     The description
     */
    @JsonProperty("description")
    public void setDescription(String description) {
        this.description = description;
    }

    public FunctionGroup withDescription(String description) {
        this.description = description;
        return this;
    }

    /**
     * 
     * @return
     *     The permissions
     */
    @JsonProperty("permissions")
    public List<PresentationPermissionFunctionGroupUpdate> getPermissions() {
        return permissions;
    }

    /**
     * 
     * @param permissions
     *     The permissions
     */
    @JsonProperty("permissions")
    public void setPermissions(List<PresentationPermissionFunctionGroupUpdate> permissions) {
        this.permissions = permissions;
    }

    public FunctionGroup withPermissions(List<PresentationPermissionFunctionGroupUpdate> permissions) {
        this.permissions = permissions;
        return this;
    }

    /**
     * Start date of the function group.
     * 
     * @return
     *     The validFromDate
     */
    @JsonProperty("validFromDate")
    public String getValidFromDate() {
        return validFromDate;
    }

    /**
     * Start date of the function group.
     * 
     * @param validFromDate
     *     The validFromDate
     */
    @JsonProperty("validFromDate")
    public void setValidFromDate(String validFromDate) {
        this.validFromDate = validFromDate;
    }

    public FunctionGroup withValidFromDate(String validFromDate) {
        this.validFromDate = validFromDate;
        return this;
    }

    /**
     * Start time of the function group.
     * 
     * @return
     *     The validFromTime
     */
    @JsonProperty("validFromTime")
    public String getValidFromTime() {
        return validFromTime;
    }

    /**
     * Start time of the function group.
     * 
     * @param validFromTime
     *     The validFromTime
     */
    @JsonProperty("validFromTime")
    public void setValidFromTime(String validFromTime) {
        this.validFromTime = validFromTime;
    }

    public FunctionGroup withValidFromTime(String validFromTime) {
        this.validFromTime = validFromTime;
        return this;
    }

    /**
     * End date of the function group.
     * 
     * @return
     *     The validUntilDate
     */
    @JsonProperty("validUntilDate")
    public String getValidUntilDate() {
        return validUntilDate;
    }

    /**
     * End date of the function group.
     * 
     * @param validUntilDate
     *     The validUntilDate
     */
    @JsonProperty("validUntilDate")
    public void setValidUntilDate(String validUntilDate) {
        this.validUntilDate = validUntilDate;
    }

    public FunctionGroup withValidUntilDate(String validUntilDate) {
        this.validUntilDate = validUntilDate;
        return this;
    }

    /**
     * End time of the function group.
     * 
     * @return
     *     The validUntilTime
     */
    @JsonProperty("validUntilTime")
    public String getValidUntilTime() {
        return validUntilTime;
    }

    /**
     * End time of the function group.
     * 
     * @param validUntilTime
     *     The validUntilTime
     */
    @JsonProperty("validUntilTime")
    public void setValidUntilTime(String validUntilTime) {
        this.validUntilTime = validUntilTime;
    }

    public FunctionGroup withValidUntilTime(String validUntilTime) {
        this.validUntilTime = validUntilTime;
        return this;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(name).append(description).append(permissions).append(validFromDate).append(validFromTime).append(validUntilDate).append(validUntilTime).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof FunctionGroup) == false) {
            return false;
        }
        FunctionGroup rhs = ((FunctionGroup) other);
        return new EqualsBuilder().append(name, rhs.name).append(description, rhs.description).append(permissions, rhs.permissions).append(validFromDate, rhs.validFromDate).append(validFromTime, rhs.validFromTime).append(validUntilDate, rhs.validUntilDate).append(validUntilTime, rhs.validUntilTime).isEquals();
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
