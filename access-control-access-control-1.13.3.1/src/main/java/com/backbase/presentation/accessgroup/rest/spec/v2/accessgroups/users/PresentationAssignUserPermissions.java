
package com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.users;

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

@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
    "externalUserId",
    "externalServiceAgreementId",
    "functionGroupDataGroups"
})
@JsonIgnoreProperties(ignoreUnknown = true)
public class PresentationAssignUserPermissions implements AdditionalPropertiesAware
{

    /**PresentationAssignUserPermissions
     * External Unique Identifier.
     * (Required)
     * 
     */
    @JsonProperty("externalUserId")
    @Pattern(regexp = "^[^\\r\\n]{1,64}$")
    @Size(min = 1, max = 64)
    @NotNull
    private String externalUserId;
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
    /**
     * Function/Data group pairs
     * (Required)
     * 
     */
    @JsonProperty("functionGroupDataGroups")
    @Valid
    @NotNull
    private List<PresentationFunctionGroupDataGroup> functionGroupDataGroups = new ArrayList<PresentationFunctionGroupDataGroup>();
    /**
     * Additional Properties
     * 
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Map<String, String> additions = new HashMap<String, String>();

    /**
     * External Unique Identifier.
     * (Required)
     * 
     * @return
     *     The externalUserId
     */
    @JsonProperty("externalUserId")
    public String getExternalUserId() {
        return externalUserId;
    }

    /**
     * External Unique Identifier.
     * (Required)
     * 
     * @param externalUserId
     *     The externalUserId
     */
    @JsonProperty("externalUserId")
    public void setExternalUserId(String externalUserId) {
        this.externalUserId = externalUserId;
    }

    public PresentationAssignUserPermissions withExternalUserId(String externalUserId) {
        this.externalUserId = externalUserId;
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

    public PresentationAssignUserPermissions withExternalServiceAgreementId(String externalServiceAgreementId) {
        this.externalServiceAgreementId = externalServiceAgreementId;
        return this;
    }

    /**
     * Function/Data group pairs
     * (Required)
     * 
     * @return
     *     The functionGroupDataGroups
     */
    @JsonProperty("functionGroupDataGroups")
    public List<PresentationFunctionGroupDataGroup> getFunctionGroupDataGroups() {
        return functionGroupDataGroups;
    }

    /**
     * Function/Data group pairs
     * (Required)
     * 
     * @param functionGroupDataGroups
     *     The functionGroupDataGroups
     */
    @JsonProperty("functionGroupDataGroups")
    public void setFunctionGroupDataGroups(List<PresentationFunctionGroupDataGroup> functionGroupDataGroups) {
        this.functionGroupDataGroups = functionGroupDataGroups;
    }

    public PresentationAssignUserPermissions withFunctionGroupDataGroups(List<PresentationFunctionGroupDataGroup> functionGroupDataGroups) {
        this.functionGroupDataGroups = functionGroupDataGroups;
        return this;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(externalUserId).append(externalServiceAgreementId).append(functionGroupDataGroups).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof PresentationAssignUserPermissions) == false) {
            return false;
        }
        PresentationAssignUserPermissions rhs = ((PresentationAssignUserPermissions) other);
        return new EqualsBuilder().append(externalUserId, rhs.externalUserId).append(externalServiceAgreementId, rhs.externalServiceAgreementId).append(functionGroupDataGroups, rhs.functionGroupDataGroups).isEquals();
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
