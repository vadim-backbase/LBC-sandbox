
package com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.datagroups;

import java.util.List;
import javax.annotation.Generated;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
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
public class DataGroupsGetResponseBody
    extends DataGroupItem
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

    public DataGroupsGetResponseBody withId(String id) {
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

    public DataGroupsGetResponseBody withApprovalId(String approvalId) {
        this.approvalId = approvalId;
        return this;
    }

    @Override
    public DataGroupsGetResponseBody withServiceAgreementId(String serviceAgreementId) {
        super.withServiceAgreementId(serviceAgreementId);
        return this;
    }

    @Override
    public DataGroupsGetResponseBody withName(String name) {
        super.withName(name);
        return this;
    }

    @Override
    public DataGroupsGetResponseBody withDescription(String description) {
        super.withDescription(description);
        return this;
    }

    @Override
    public DataGroupsGetResponseBody withType(String type) {
        super.withType(type);
        return this;
    }

    @Override
    public DataGroupsGetResponseBody withItems(List<String> items) {
        super.withItems(items);
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
        if ((other instanceof DataGroupsGetResponseBody) == false) {
            return false;
        }
        DataGroupsGetResponseBody rhs = ((DataGroupsGetResponseBody) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(id, rhs.id).append(approvalId, rhs.approvalId).isEquals();
    }

}
