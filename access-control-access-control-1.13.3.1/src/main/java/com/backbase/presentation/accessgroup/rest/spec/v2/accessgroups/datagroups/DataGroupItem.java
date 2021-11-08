
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
    "serviceAgreementId"
})
@JsonIgnoreProperties(ignoreUnknown = true)
public class DataGroupItem
    extends DataGroupsPostRequestBodyParent
{

    /**
     * Universally Unique Identifier.
     * (Required)
     * 
     */
    @JsonProperty("serviceAgreementId")
    @Pattern(regexp = "^[0-9a-f]{32}$|^[0-9a-f-]{36}$")
    @Size(min = 1, max = 36)
    @NotNull
    private String serviceAgreementId;

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

    public DataGroupItem withServiceAgreementId(String serviceAgreementId) {
        this.serviceAgreementId = serviceAgreementId;
        return this;
    }

    @Override
    public DataGroupItem withName(String name) {
        super.withName(name);
        return this;
    }

    @Override
    public DataGroupItem withDescription(String description) {
        super.withDescription(description);
        return this;
    }

    @Override
    public DataGroupItem withType(String type) {
        super.withType(type);
        return this;
    }

    @Override
    public DataGroupItem withItems(List<String> items) {
        super.withItems(items);
        return this;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(serviceAgreementId).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof DataGroupItem) == false) {
            return false;
        }
        DataGroupItem rhs = ((DataGroupItem) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(serviceAgreementId, rhs.serviceAgreementId).isEquals();
    }

}
