
package com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.functiongroups;

import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.function.Permission;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.Date;
import java.util.List;
import javax.annotation.Generated;
import javax.validation.constraints.Pattern;
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
    "id"
})
@JsonIgnoreProperties(ignoreUnknown = true)
public class BulkFunctionGroupsPostResponseBody
    extends FunctionGroupBase
{

    /**
     * Universally Unique Identifier.
     * 
     */
    @JsonProperty("id")
    @Pattern(regexp = "^[0-9a-f]{32}$|^[0-9a-f-]{36}$")
    private String id;

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

    public BulkFunctionGroupsPostResponseBody withId(String id) {
        this.id = id;
        return this;
    }

    @Override
    public BulkFunctionGroupsPostResponseBody withServiceAgreementId(String serviceAgreementId) {
        super.withServiceAgreementId(serviceAgreementId);
        return this;
    }

    @Override
    public BulkFunctionGroupsPostResponseBody withName(String name) {
        super.withName(name);
        return this;
    }

    @Override
    public BulkFunctionGroupsPostResponseBody withDescription(String description) {
        super.withDescription(description);
        return this;
    }

    @Override
    public BulkFunctionGroupsPostResponseBody withType(Type type) {
        super.withType(type);
        return this;
    }

    @Override
    public BulkFunctionGroupsPostResponseBody withPermissions(List<Permission> permissions) {
        super.withPermissions(permissions);
        return this;
    }

    @Override
    public BulkFunctionGroupsPostResponseBody withValidFrom(Date validFrom) {
        super.withValidFrom(validFrom);
        return this;
    }

    @Override
    public BulkFunctionGroupsPostResponseBody withValidUntil(Date validUntil) {
        super.withValidUntil(validUntil);
        return this;
    }

    @Override
    public BulkFunctionGroupsPostResponseBody withApprovalId(String approvalId) {
        super.withApprovalId(approvalId);
        return this;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(id).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof BulkFunctionGroupsPostResponseBody) == false) {
            return false;
        }
        BulkFunctionGroupsPostResponseBody rhs = ((BulkFunctionGroupsPostResponseBody) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(id, rhs.id).isEquals();
    }

}
