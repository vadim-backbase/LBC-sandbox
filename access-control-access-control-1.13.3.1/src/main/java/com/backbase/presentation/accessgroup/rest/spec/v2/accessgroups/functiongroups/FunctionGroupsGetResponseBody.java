
package com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.functiongroups;

import java.util.List;
import javax.annotation.Generated;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.function.Permission;
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
    "approvalId"
})
@JsonIgnoreProperties(ignoreUnknown = true)
public class FunctionGroupsGetResponseBody
    extends FunctionGroupBase
{

    /**
     * Universally Unique Identifier.
     * (Required)
     * 
     */
    @JsonProperty("id")
    @Pattern(regexp = "^[0-9a-f]{32}$|^[0-9a-f-]{36}$")
    @Size(min = 1, max = 36)
    @NotNull
    private String id;
    /**
     * Id of approval request.
     * 
     */
    @JsonProperty("approvalId")
    @Size(min = 1, max = 36)
    private String approvalId;

    /**
     * Universally Unique Identifier.
     * (Required)
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
     * (Required)
     * 
     * @param id
     *     The id
     */
    @JsonProperty("id")
    public void setId(String id) {
        this.id = id;
    }

    public FunctionGroupsGetResponseBody withId(String id) {
        this.id = id;
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

    public FunctionGroupsGetResponseBody withApprovalId(String approvalId) {
        this.approvalId = approvalId;
        return this;
    }

    @Override
    public FunctionGroupsGetResponseBody withName(String name) {
        super.withName(name);
        return this;
    }

    @Override
    public FunctionGroupsGetResponseBody withDescription(String description) {
        super.withDescription(description);
        return this;
    }

    @Override
    public FunctionGroupsGetResponseBody withServiceAgreementId(String serviceAgreementId) {
        super.withServiceAgreementId(serviceAgreementId);
        return this;
    }

    @Override
    public FunctionGroupsGetResponseBody withPermissions(List<Permission> permissions) {
        super.withPermissions(permissions);
        return this;
    }

    @Override
    public FunctionGroupsGetResponseBody withValidFromDate(String validFromDate) {
        super.withValidFromDate(validFromDate);
        return this;
    }

    @Override
    public FunctionGroupsGetResponseBody withValidFromTime(String validFromTime) {
        super.withValidFromTime(validFromTime);
        return this;
    }

    @Override
    public FunctionGroupsGetResponseBody withValidUntilDate(String validUntilDate) {
        super.withValidUntilDate(validUntilDate);
        return this;
    }

    @Override
    public FunctionGroupsGetResponseBody withValidUntilTime(String validUntilTime) {
        super.withValidUntilTime(validUntilTime);
        return this;
    }

    @Override
    public FunctionGroupsGetResponseBody withApprovalTypeId(String approvalTypeId) {
        super.withApprovalTypeId(approvalTypeId);
        return this;
    }

    @Override
    public FunctionGroupsGetResponseBody withType(Type type) {
        super.withType(type);
        return this;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(id).append(approvalId).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof FunctionGroupsGetResponseBody) == false) {
            return false;
        }
        FunctionGroupsGetResponseBody rhs = ((FunctionGroupsGetResponseBody) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(id, rhs.id).append(approvalId, rhs.approvalId).isEquals();
    }

}
