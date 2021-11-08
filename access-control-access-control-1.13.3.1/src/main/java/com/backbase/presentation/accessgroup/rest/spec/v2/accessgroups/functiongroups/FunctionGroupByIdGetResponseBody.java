
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
public class FunctionGroupByIdGetResponseBody
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

    public FunctionGroupByIdGetResponseBody withId(String id) {
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

    public FunctionGroupByIdGetResponseBody withApprovalId(String approvalId) {
        this.approvalId = approvalId;
        return this;
    }

    @Override
    public FunctionGroupByIdGetResponseBody withName(String name) {
        super.withName(name);
        return this;
    }

    @Override
    public FunctionGroupByIdGetResponseBody withDescription(String description) {
        super.withDescription(description);
        return this;
    }

    @Override
    public FunctionGroupByIdGetResponseBody withServiceAgreementId(String serviceAgreementId) {
        super.withServiceAgreementId(serviceAgreementId);
        return this;
    }

    @Override
    public FunctionGroupByIdGetResponseBody withPermissions(List<Permission> permissions) {
        super.withPermissions(permissions);
        return this;
    }

    @Override
    public FunctionGroupByIdGetResponseBody withValidFromDate(String validFromDate) {
        super.withValidFromDate(validFromDate);
        return this;
    }

    @Override
    public FunctionGroupByIdGetResponseBody withValidFromTime(String validFromTime) {
        super.withValidFromTime(validFromTime);
        return this;
    }

    @Override
    public FunctionGroupByIdGetResponseBody withValidUntilDate(String validUntilDate) {
        super.withValidUntilDate(validUntilDate);
        return this;
    }

    @Override
    public FunctionGroupByIdGetResponseBody withValidUntilTime(String validUntilTime) {
        super.withValidUntilTime(validUntilTime);
        return this;
    }

    @Override
    public FunctionGroupByIdGetResponseBody withApprovalTypeId(String approvalTypeId) {
        super.withApprovalTypeId(approvalTypeId);
        return this;
    }

    @Override
    public FunctionGroupByIdGetResponseBody withType(Type type) {
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
        if ((other instanceof FunctionGroupByIdGetResponseBody) == false) {
            return false;
        }
        FunctionGroupByIdGetResponseBody rhs = ((FunctionGroupByIdGetResponseBody) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(id, rhs.id).append(approvalId, rhs.approvalId).isEquals();
    }

}
