
package com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.functiongroups;

import com.backbase.buildingblocks.persistence.model.AdditionalPropertiesAware;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.function.Permission;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Generated;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Function group item
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
    "serviceAgreementId",
    "name",
    "description",
    "type",
    "permissions",
    "validFrom",
    "validUntil",
    "approvalId"
})
public class FunctionGroupBase implements AdditionalPropertiesAware
{

    /**
     * Universally Unique Identifier.
     * (Required)
     * 
     */
    @JsonProperty("serviceAgreementId")
    @Pattern(regexp = "^[0-9a-f]{32}$|^[0-9a-f-]{36}$")
    @NotNull
    private String serviceAgreementId;
    /**
     * Name of function group
     * (Required)
     * 
     */
    @JsonProperty("name")
    @NotNull
    private String name;
    /**
     * Description of function group
     * (Required)
     * 
     */
    @JsonProperty("description")
    @NotNull
    private String description;
    /**
     * Type of function group
     * (Required)
     * 
     */
    @JsonProperty("type")
    @NotNull
    private FunctionGroupBase.Type type = Type.fromValue("DEFAULT");
    /**
     * Applicable permissions for the function group
     *
     */
    @JsonProperty("permissions")
    @Valid
    private List<Permission> permissions = new ArrayList<Permission>();
    /**
     * Start date and time of the function group.
     *
     */
    @JsonProperty("validFrom")
    private Date validFrom;
    /**
     * End date and time of the function group.
     *
     */
    @JsonProperty("validUntil")
    private Date validUntil;
    /**
     * Id of approval request.
     *
     */
    @JsonProperty("approvalId")
    @Size(min = 1, max = 36)
    private String approvalId;
    /**
     * Additional Properties
     *
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Map<String, String> additions = new HashMap<String, String>();

    /**
     * Universally Unique Identifier.
     * (Required)
     *
     * @return
     *     The serviceAgreementId
     */
    @JsonProperty("serviceAgreementId")
    public String getServiceAgreementId() {
        return serviceAgreementId;
    }

    /**
     * Universally Unique Identifier.
     * (Required)
     *
     * @param serviceAgreementId
     *     The serviceAgreementId
     */
    @JsonProperty("serviceAgreementId")
    public void setServiceAgreementId(String serviceAgreementId) {
        this.serviceAgreementId = serviceAgreementId;
    }

    public FunctionGroupBase withServiceAgreementId(String serviceAgreementId) {
        this.serviceAgreementId = serviceAgreementId;
        return this;
    }

    /**
     * Name of function group
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
     * Name of function group
     * (Required)
     *
     * @param name
     *     The name
     */
    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    public FunctionGroupBase withName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Description of function group
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
     * Description of function group
     * (Required)
     *
     * @param description
     *     The description
     */
    @JsonProperty("description")
    public void setDescription(String description) {
        this.description = description;
    }

    public FunctionGroupBase withDescription(String description) {
        this.description = description;
        return this;
    }

    /**
     * Type of function group
     * (Required)
     *
     * @return
     *     The type
     */
    @JsonProperty("type")
    public Type getType() {
        return type;
    }

    /**
     * Type of function group
     * (Required)
     *
     * @param type
     *     The type
     */
    @JsonProperty("type")
    public void setType(Type type) {
        this.type = type;
    }

    public FunctionGroupBase withType(Type type) {
        this.type = type;
        return this;
    }

    /**
     * Applicable permissions for the function group
     *
     * @return
     *     The permissions
     */
    @JsonProperty("permissions")
    public List<Permission> getPermissions() {
        return permissions;
    }

    /**
     * Applicable permissions for the function group
     *
     * @param permissions
     *     The permissions
     */
    @JsonProperty("permissions")
    public void setPermissions(List<Permission> permissions) {
        this.permissions = permissions;
    }

    public FunctionGroupBase withPermissions(List<Permission> permissions) {
        this.permissions = permissions;
        return this;
    }

    /**
     * Start date and time of the function group.
     *
     * @return
     *     The validFrom
     */
    @JsonProperty("validFrom")
    public Date getValidFrom() {
        return validFrom;
    }

    /**
     * Start date and time of the function group.
     *
     * @param validFrom
     *     The validFrom
     */
    @JsonProperty("validFrom")
    public void setValidFrom(Date validFrom) {
        this.validFrom = validFrom;
    }

    public FunctionGroupBase withValidFrom(Date validFrom) {
        this.validFrom = validFrom;
        return this;
    }

    /**
     * End date and time of the function group.
     *
     * @return
     *     The validUntil
     */
    @JsonProperty("validUntil")
    public Date getValidUntil() {
        return validUntil;
    }

    /**
     * End date and time of the function group.
     *
     * @param validUntil
     *     The validUntil
     */
    @JsonProperty("validUntil")
    public void setValidUntil(Date validUntil) {
        this.validUntil = validUntil;
    }

    public FunctionGroupBase withValidUntil(Date validUntil) {
        this.validUntil = validUntil;
        return this;
    }

    /**
     * Id of approval request.
     *
     * @return
     *     The approvalId
     */
    @JsonProperty("approvalId")
    public String getApprovalId() {
        return approvalId;
    }

    /**
     * Id of approval request.
     *
     * @param approvalId
     *     The approvalId
     */
    @JsonProperty("approvalId")
    public void setApprovalId(String approvalId) {
        this.approvalId = approvalId;
    }

    public FunctionGroupBase withApprovalId(String approvalId) {
        this.approvalId = approvalId;
        return this;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(serviceAgreementId).append(name).append(description).append(type).append(permissions).append(validFrom).append(validUntil).append(approvalId).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof FunctionGroupBase) == false) {
            return false;
        }
        FunctionGroupBase rhs = ((FunctionGroupBase) other);
        return new EqualsBuilder().append(serviceAgreementId, rhs.serviceAgreementId).append(name, rhs.name).append(description, rhs.description).append(type, rhs.type).append(permissions, rhs.permissions).append(validFrom, rhs.validFrom).append(validUntil, rhs.validUntil).append(approvalId, rhs.approvalId).isEquals();
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

    @Generated("org.jsonschema2pojo")
    public enum Type {

        DEFAULT("DEFAULT"),
        SYSTEM("SYSTEM"),
        TEMPLATE("TEMPLATE");
        private final String value;
        private final static Map<String, Type> CONSTANTS = new HashMap<String, Type>();

        static {
            for (Type c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        private Type(String value) {
            this.value = value;
        }

        @JsonValue
        @Override
        public String toString() {
            return this.value;
        }

        @JsonCreator
        public static Type fromValue(String value) {
            Type constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

}
