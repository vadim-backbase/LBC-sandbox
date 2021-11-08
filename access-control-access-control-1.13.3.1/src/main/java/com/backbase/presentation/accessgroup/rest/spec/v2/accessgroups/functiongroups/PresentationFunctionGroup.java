
package com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.functiongroups;

import java.math.BigDecimal;
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
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
    "name",
    "description",
    "externalServiceAgreementId",
    "permissions",
    "validFromDate",
    "validFromTime",
    "validUntilDate",
    "validUntilTime",
    "apsId",
    "apsName",
    "type"
})
@JsonIgnoreProperties(ignoreUnknown = true)
public class PresentationFunctionGroup implements AdditionalPropertiesAware
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
    @Pattern(regexp = "^\\S(.*(\\S))?$")
    @Size(min = 1, max = 255)
    @NotNull
    private String description;
    /**
     * External Unique Identifier.
     * (Required)
     * 
     */
    @JsonProperty("externalServiceAgreementId")
    @Pattern(regexp = "^[^\\r\\n]{1,64}$")
    @Size(min = 1, max = 64)
    @NotNull
    private String externalServiceAgreementId;
    @JsonProperty("permissions")
    @Valid
    private List<PresentationPermission> permissions = new ArrayList<PresentationPermission>();
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
     * Assignable Permission Set Id
     * 
     */
    @JsonProperty("apsId")
    private BigDecimal apsId;
    /**
     * Assignable Permission Set name
     * 
     */
    @JsonProperty("apsName")
    @Size(max = 128)
    private String apsName;
    @JsonProperty("type")
    private Type type = Type.fromValue("REGULAR");
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

    public PresentationFunctionGroup withName(String name) {
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

    public PresentationFunctionGroup withDescription(String description) {
        this.description = description;
        return this;
    }

    /**
     * External Unique Identifier.
     * (Required)
     * 
     * @return
     *     The externalServiceAgreementId
     */
    @JsonProperty("externalServiceAgreementId")
    public String getExternalServiceAgreementId() {
        return externalServiceAgreementId;
    }

    /**
     * External Unique Identifier.
     * (Required)
     * 
     * @param externalServiceAgreementId
     *     The externalServiceAgreementId
     */
    @JsonProperty("externalServiceAgreementId")
    public void setExternalServiceAgreementId(String externalServiceAgreementId) {
        this.externalServiceAgreementId = externalServiceAgreementId;
    }

    public PresentationFunctionGroup withExternalServiceAgreementId(String externalServiceAgreementId) {
        this.externalServiceAgreementId = externalServiceAgreementId;
        return this;
    }

    /**
     * 
     * @return
     *     The permissions
     */
    @JsonProperty("permissions")
    public List<PresentationPermission> getPermissions() {
        return permissions;
    }

    /**
     * 
     * @param permissions
     *     The permissions
     */
    @JsonProperty("permissions")
    public void setPermissions(List<PresentationPermission> permissions) {
        this.permissions = permissions;
    }

    public PresentationFunctionGroup withPermissions(List<PresentationPermission> permissions) {
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

    public PresentationFunctionGroup withValidFromDate(String validFromDate) {
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

    public PresentationFunctionGroup withValidFromTime(String validFromTime) {
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

    public PresentationFunctionGroup withValidUntilDate(String validUntilDate) {
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

    public PresentationFunctionGroup withValidUntilTime(String validUntilTime) {
        this.validUntilTime = validUntilTime;
        return this;
    }

    /**
     * Assignable Permission Set Id
     * 
     * @return
     *     The apsId
     */
    @JsonProperty("apsId")
    public BigDecimal getApsId() {
        return apsId;
    }

    /**
     * Assignable Permission Set Id
     * 
     * @param apsId
     *     The apsId
     */
    @JsonProperty("apsId")
    public void setApsId(BigDecimal apsId) {
        this.apsId = apsId;
    }

    public PresentationFunctionGroup withApsId(BigDecimal apsId) {
        this.apsId = apsId;
        return this;
    }

    /**
     * Assignable Permission Set name
     * 
     * @return
     *     The apsName
     */
    @JsonProperty("apsName")
    public String getApsName() {
        return apsName;
    }

    /**
     * Assignable Permission Set name
     * 
     * @param apsName
     *     The apsName
     */
    @JsonProperty("apsName")
    public void setApsName(String apsName) {
        this.apsName = apsName;
    }

    public PresentationFunctionGroup withApsName(String apsName) {
        this.apsName = apsName;
        return this;
    }

    /**
     * 
     * @return
     *     The type
     */
    @JsonProperty("type")
    public Type getType() {
        return type;
    }

    /**
     * 
     * @param type
     *     The type
     */
    @JsonProperty("type")
    public void setType(Type type) {
        this.type = type;
    }

    public PresentationFunctionGroup withType(Type type) {
        this.type = type;
        return this;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(name).append(description).append(externalServiceAgreementId).append(permissions).append(validFromDate).append(validFromTime).append(validUntilDate).append(validUntilTime).append(apsId).append(apsName).append(type).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof PresentationFunctionGroup) == false) {
            return false;
        }
        PresentationFunctionGroup rhs = ((PresentationFunctionGroup) other);
        return new EqualsBuilder().append(name, rhs.name).append(description, rhs.description).append(externalServiceAgreementId, rhs.externalServiceAgreementId).append(permissions, rhs.permissions).append(validFromDate, rhs.validFromDate).append(validFromTime, rhs.validFromTime).append(validUntilDate, rhs.validUntilDate).append(validUntilTime, rhs.validUntilTime).append(apsId, rhs.apsId).append(apsName, rhs.apsName).append(type, rhs.type).isEquals();
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

        REGULAR("REGULAR"),
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
