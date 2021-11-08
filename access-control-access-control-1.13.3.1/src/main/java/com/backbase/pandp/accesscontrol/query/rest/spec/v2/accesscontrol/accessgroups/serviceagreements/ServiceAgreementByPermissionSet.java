
package com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.serviceagreements;

import com.backbase.buildingblocks.persistence.model.AdditionalPropertiesAware;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import javax.annotation.Generated;
import javax.validation.Valid;
import javax.validation.constraints.Pattern;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
    "id",
    "externalId",
    "name",
    "isMaster",
    "regularUserAps",
    "adminUserAps"
})
@JsonIgnoreProperties(ignoreUnknown = true)
public class ServiceAgreementByPermissionSet implements AdditionalPropertiesAware
{

    /**
     * Universally Unique Identifier.
     * 
     */
    @JsonProperty("id")
    @Pattern(regexp = "^[0-9a-f]{32}$|^[0-9a-f-]{36}$")
    private String id;
    /**
     * Service agreement external id
     * 
     */
    @JsonProperty("externalId")
    private String externalId;
    /**
     * Service agreement name
     * 
     */
    @JsonProperty("name")
    private String name;
    /**
     * Is Service agreement master
     * 
     */
    @JsonProperty("isMaster")
    private Boolean isMaster;
    /**
     * Set of assignable permission sets identified by ids, used for regular users.
     * 
     */
    @JsonProperty("regularUserAps")
    @JsonDeserialize(as = LinkedHashSet.class)
    @Valid
    private Set<BigDecimal> regularUserAps = new LinkedHashSet<BigDecimal>();
    /**
     * Set of assignable permission sets identified by ids, used for admin users.
     *
     */
    @JsonProperty("adminUserAps")
    @JsonDeserialize(as = LinkedHashSet.class)
    @Valid
    private Set<BigDecimal> adminUserAps = new LinkedHashSet<BigDecimal>();
    /**
     * Additional Properties
     * 
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Map<String, String> additions = new HashMap<String, String>();

    /**
     * Universally Unique Identifier.
     * 
     * @return
     *     The id
     */
    @JsonProperty("id")
    public String getId() {
        return id;
    }

    /**
     * Universally Unique Identifier.
     * 
     * @param id
     *     The id
     */
    @JsonProperty("id")
    public void setId(String id) {
        this.id = id;
    }

    public ServiceAgreementByPermissionSet withId(String id) {
        this.id = id;
        return this;
    }

    /**
     * Service agreement external id
     * 
     * @return
     *     The externalId
     */
    @JsonProperty("externalId")
    public String getExternalId() {
        return externalId;
    }

    /**
     * Service agreement external id
     * 
     * @param externalId
     *     The externalId
     */
    @JsonProperty("externalId")
    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public ServiceAgreementByPermissionSet withExternalId(String externalId) {
        this.externalId = externalId;
        return this;
    }

    /**
     * Service agreement name
     * 
     * @return
     *     The name
     */
    @JsonProperty("name")
    public String getName() {
        return name;
    }

    /**
     * Service agreement name
     * 
     * @param name
     *     The name
     */
    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    public ServiceAgreementByPermissionSet withName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Is Service agreement master
     * 
     * @return
     *     The isMaster
     */
    @JsonProperty("isMaster")
    public Boolean getIsMaster() {
        return isMaster;
    }

    /**
     * Is Service agreement master
     * 
     * @param isMaster
     *     The isMaster
     */
    @JsonProperty("isMaster")
    public void setIsMaster(Boolean isMaster) {
        this.isMaster = isMaster;
    }

    public ServiceAgreementByPermissionSet withIsMaster(Boolean isMaster) {
        this.isMaster = isMaster;
        return this;
    }

    /**
     * Set of assignable permission sets identified by ids, used for regular users.
     * 
     * @return
     *     The regularUserAps
     */
    @JsonProperty("regularUserAps")
    public Set<BigDecimal> getRegularUserAps() {
        return regularUserAps;
    }

    /**
     * Set of assignable permission sets identified by ids, used for regular users.
     * 
     * @param regularUserAps
     *     The regularUserAps
     */
    @JsonProperty("regularUserAps")
    public void setRegularUserAps(Set<BigDecimal> regularUserAps) {
        this.regularUserAps = regularUserAps;
    }

    public ServiceAgreementByPermissionSet withRegularUserAps(Set<BigDecimal> regularUserAps) {
        this.regularUserAps = regularUserAps;
        return this;
    }

    /**
     * Set of assignable permission sets identified by ids, used for admin users.
     * 
     * @return
     *     The adminUserAps
     */
    @JsonProperty("adminUserAps")
    public Set<BigDecimal> getAdminUserAps() {
        return adminUserAps;
    }

    /**
     * Set of assignable permission sets identified by ids, used for admin users.
     * 
     * @param adminUserAps
     *     The adminUserAps
     */
    @JsonProperty("adminUserAps")
    public void setAdminUserAps(Set<BigDecimal> adminUserAps) {
        this.adminUserAps = adminUserAps;
    }

    public ServiceAgreementByPermissionSet withAdminUserAps(Set<BigDecimal> adminUserAps) {
        this.adminUserAps = adminUserAps;
        return this;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(id).append(externalId).append(name).append(isMaster).append(regularUserAps).append(adminUserAps).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ServiceAgreementByPermissionSet) == false) {
            return false;
        }
        ServiceAgreementByPermissionSet rhs = ((ServiceAgreementByPermissionSet) other);
        return new EqualsBuilder().append(id, rhs.id).append(externalId, rhs.externalId).append(name, rhs.name).append(isMaster, rhs.isMaster).append(regularUserAps, rhs.regularUserAps).append(adminUserAps, rhs.adminUserAps).isEquals();
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
