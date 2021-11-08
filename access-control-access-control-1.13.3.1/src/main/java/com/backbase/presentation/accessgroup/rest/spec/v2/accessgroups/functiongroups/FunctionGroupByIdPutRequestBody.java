
package com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.functiongroups;

import java.util.List;
import javax.annotation.Generated;
import javax.validation.constraints.Pattern;
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
    "approvalTypeId"
})
@JsonIgnoreProperties(ignoreUnknown = true)
public class FunctionGroupByIdPutRequestBody
    extends FunctionGroupBase
{

    /**
     * The approval type to assign.
     * 
     */
    @JsonProperty("approvalTypeId")
    @Pattern(regexp = "^[0-9a-fA-F]{8}-?[0-9a-fA-F]{4}-?[1-5][0-9a-fA-F]{3}-?[89abAB][0-9a-fA-F]{3}-?[0-9a-fA-F]{12}$")
    private String approvalTypeId;

    /**
     * The approval type to assign.
     * 
     * @return
     *     The approvalTypeId
     */
    @JsonProperty("approvalTypeId")
    public String getApprovalTypeId() {
        return approvalTypeId;
    }

    /**
     * The approval type to assign.
     * 
     * @param approvalTypeId
     *     The approvalTypeId
     */
    @JsonProperty("approvalTypeId")
    public void setApprovalTypeId(String approvalTypeId) {
        this.approvalTypeId = approvalTypeId;
    }

    public FunctionGroupByIdPutRequestBody withApprovalTypeId(String approvalTypeId) {
        this.approvalTypeId = approvalTypeId;
        return this;
    }

    @Override
    public FunctionGroupByIdPutRequestBody withName(String name) {
        super.withName(name);
        return this;
    }

    @Override
    public FunctionGroupByIdPutRequestBody withDescription(String description) {
        super.withDescription(description);
        return this;
    }

    @Override
    public FunctionGroupByIdPutRequestBody withServiceAgreementId(String serviceAgreementId) {
        super.withServiceAgreementId(serviceAgreementId);
        return this;
    }

    @Override
    public FunctionGroupByIdPutRequestBody withPermissions(List<Permission> permissions) {
        super.withPermissions(permissions);
        return this;
    }

    @Override
    public FunctionGroupByIdPutRequestBody withValidFromDate(String validFromDate) {
        super.withValidFromDate(validFromDate);
        return this;
    }

    @Override
    public FunctionGroupByIdPutRequestBody withValidFromTime(String validFromTime) {
        super.withValidFromTime(validFromTime);
        return this;
    }

    @Override
    public FunctionGroupByIdPutRequestBody withValidUntilDate(String validUntilDate) {
        super.withValidUntilDate(validUntilDate);
        return this;
    }

    @Override
    public FunctionGroupByIdPutRequestBody withValidUntilTime(String validUntilTime) {
        super.withValidUntilTime(validUntilTime);
        return this;
    }

    @Override
    public FunctionGroupByIdPutRequestBody withType(Type type) {
        super.withType(type);
        return this;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(approvalTypeId).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof FunctionGroupByIdPutRequestBody) == false) {
            return false;
        }
        FunctionGroupByIdPutRequestBody rhs = ((FunctionGroupByIdPutRequestBody) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(approvalTypeId, rhs.approvalTypeId).isEquals();
    }

}
