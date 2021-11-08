
package com.backbase.presentation.accessgroup.event.spec.v1;

import java.util.List;
import javax.annotation.Generated;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.datagroups.DataGroupsPostRequestBodyParent_;
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
    "serviceAgreementId",
    "externalServiceAgreementId",
    "areItemsInternalIds"
})
@JsonIgnoreProperties(ignoreUnknown = true)
public class DataGroupBase
    extends DataGroupsPostRequestBodyParent_
{

    /**
     * Universally Unique Identifier.
     * 
     */
    @JsonProperty("serviceAgreementId")
    @Pattern(regexp = "^[0-9a-f]{32}$|^[0-9a-f-]{36}$")
    @Size(min = 1, max = 36)
    private String serviceAgreementId;
    /**
     * External Unique Identifier.
     * 
     */
    @JsonProperty("externalServiceAgreementId")
    @Pattern(regexp = "^[^\\r\\n]{1,64}$")
    @Size(min = 1, max = 64)
    private String externalServiceAgreementId;
    /**
     * If true all items are with internal ids, otherwise are external ids
     * 
     */
    @JsonProperty("areItemsInternalIds")
    private Boolean areItemsInternalIds = true;

    /**
     * Universally Unique Identifier.
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
     * 
     * @param serviceAgreementId
     *     The serviceAgreementId
     */
    @JsonProperty("serviceAgreementId")
    public void setServiceAgreementId(String serviceAgreementId) {
        this.serviceAgreementId = serviceAgreementId;
    }

    public DataGroupBase withServiceAgreementId(String serviceAgreementId) {
        this.serviceAgreementId = serviceAgreementId;
        return this;
    }

    /**
     * External Unique Identifier.
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
     * 
     * @param externalServiceAgreementId
     *     The externalServiceAgreementId
     */
    @JsonProperty("externalServiceAgreementId")
    public void setExternalServiceAgreementId(String externalServiceAgreementId) {
        this.externalServiceAgreementId = externalServiceAgreementId;
    }

    public DataGroupBase withExternalServiceAgreementId(String externalServiceAgreementId) {
        this.externalServiceAgreementId = externalServiceAgreementId;
        return this;
    }

    /**
     * If true all items are with internal ids, otherwise are external ids
     * 
     * @return
     *     The areItemsInternalIds
     */
    @JsonProperty("areItemsInternalIds")
    public Boolean getAreItemsInternalIds() {
        return areItemsInternalIds;
    }

    /**
     * If true all items are with internal ids, otherwise are external ids
     * 
     * @param areItemsInternalIds
     *     The areItemsInternalIds
     */
    @JsonProperty("areItemsInternalIds")
    public void setAreItemsInternalIds(Boolean areItemsInternalIds) {
        this.areItemsInternalIds = areItemsInternalIds;
    }

    public DataGroupBase withAreItemsInternalIds(Boolean areItemsInternalIds) {
        this.areItemsInternalIds = areItemsInternalIds;
        return this;
    }

    @Override
    public DataGroupBase withName(String name) {
        super.withName(name);
        return this;
    }

    @Override
    public DataGroupBase withDescription(String description) {
        super.withDescription(description);
        return this;
    }

    @Override
    public DataGroupBase withType(String type) {
        super.withType(type);
        return this;
    }

    @Override
    public DataGroupBase withItems(List<String> items) {
        super.withItems(items);
        return this;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(serviceAgreementId).append(externalServiceAgreementId).append(areItemsInternalIds).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof DataGroupBase) == false) {
            return false;
        }
        DataGroupBase rhs = ((DataGroupBase) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(serviceAgreementId, rhs.serviceAgreementId).append(externalServiceAgreementId, rhs.externalServiceAgreementId).append(areItemsInternalIds, rhs.areItemsInternalIds).isEquals();
    }

}
