
package com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.serviceagreements;

import com.backbase.buildingblocks.persistence.model.AdditionalPropertiesAware;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.SelfApprovalPolicy;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.ArrayList;
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

@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
    "functionGroupId",
    "dataGroupIds"
})
public class PersistenceApprovalPermissionsGetResponseBody implements AdditionalPropertiesAware
{

    /**
     * Universally Unique Identifier.
     * (Required)
     * 
     */
    @JsonProperty("functionGroupId")
    @Pattern(regexp = "^[0-9a-f]{32}$|^[0-9a-f-]{36}$")
    @NotNull
    private String functionGroupId;
    @JsonProperty("dataGroupIds")
    @Size(min = 0)
    @Valid
    private List<String> dataGroupIds = new ArrayList<String>();

    private List<SelfApprovalPolicy> selfApprovalPolicies = new ArrayList<>();
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
     *     The functionGroupId
     */
    @JsonProperty("functionGroupId")
    public String getFunctionGroupId() {
        return functionGroupId;
    }

    /**
     * Universally Unique Identifier.
     * (Required)
     * 
     * @param functionGroupId
     *     The functionGroupId
     */
    @JsonProperty("functionGroupId")
    public void setFunctionGroupId(String functionGroupId) {
        this.functionGroupId = functionGroupId;
    }

    public PersistenceApprovalPermissionsGetResponseBody withFunctionGroupId(String functionGroupId) {
        this.functionGroupId = functionGroupId;
        return this;
    }

    /**
     * 
     * @return
     *     The dataGroupIds
     */
    @JsonProperty("dataGroupIds")
    public List<String> getDataGroupIds() {
        return dataGroupIds;
    }

    /**
     * 
     * @param dataGroupIds
     *     The dataGroupIds
     */
    @JsonProperty("dataGroupIds")
    public void setDataGroupIds(List<String> dataGroupIds) {
        this.dataGroupIds = dataGroupIds;
    }

    public PersistenceApprovalPermissionsGetResponseBody withDataGroupIds(List<String> dataGroupIds) {
        this.dataGroupIds = dataGroupIds;
        return this;
    }

    public PersistenceApprovalPermissionsGetResponseBody withSelfApprovalPolicies(List<SelfApprovalPolicy> selfApprovalPolicies) {
        this.selfApprovalPolicies = selfApprovalPolicies;
        return this;
    }

    public List<SelfApprovalPolicy> getSelfApprovalPolicies() {
        return selfApprovalPolicies;
    }

    public void setSelfApprovalPolicies(List<SelfApprovalPolicy> selfApprovalPolicies) {
        this.selfApprovalPolicies = selfApprovalPolicies;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(functionGroupId).append(dataGroupIds).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof PersistenceApprovalPermissionsGetResponseBody) == false) {
            return false;
        }
        PersistenceApprovalPermissionsGetResponseBody rhs = ((PersistenceApprovalPermissionsGetResponseBody) other);
        return new EqualsBuilder().append(functionGroupId, rhs.functionGroupId).append(dataGroupIds, rhs.dataGroupIds).isEquals();
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
