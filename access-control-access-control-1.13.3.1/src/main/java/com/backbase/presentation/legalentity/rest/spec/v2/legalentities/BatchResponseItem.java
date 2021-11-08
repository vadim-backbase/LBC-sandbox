
package com.backbase.presentation.legalentity.rest.spec.v2.legalentities;

import com.backbase.buildingblocks.persistence.model.AdditionalPropertiesAware;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
import javax.validation.constraints.Size;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
    "resourceId",
    "status",
    "errors"
})
@JsonIgnoreProperties(ignoreUnknown = true)
public class BatchResponseItem implements AdditionalPropertiesAware {

    /**
     * Id of the resource (Required)
     */
    @JsonProperty("resourceId")
    @Size(min = 1)
    @NotNull
    private String resourceId;
    /**
     * Http status codes available for response (Required)
     */
    @JsonProperty("status")
    @NotNull
    private BatchResponseStatusCode status;
    /**
     * List of error messages
     */
    @JsonProperty("errors")
    @Valid
    private List<String> errors = new ArrayList<String>();
    /**
     * Additional Properties
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Map<String, String> additions = new HashMap<String, String>();

    /**
     * Id of the resource (Required)
     *
     * @return The resourceId
     */
    @JsonProperty("resourceId")
    public String getResourceId() {
        return resourceId;
    }

    /**
     * Id of the resource (Required)
     *
     * @param resourceId The resourceId
     */
    @JsonProperty("resourceId")
    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public BatchResponseItem withResourceId(String resourceId) {
        this.resourceId = resourceId;
        return this;
    }

    /**
     * Http status codes available for response (Required)
     *
     * @return The status
     */
    @JsonProperty("status")
    public BatchResponseStatusCode getStatus() {
        return status;
    }

    /**
     * Http status codes available for response (Required)
     *
     * @param status The status
     */
    @JsonProperty("status")
    public void setStatus(BatchResponseStatusCode status) {
        this.status = status;
    }

    public BatchResponseItem withStatus(BatchResponseStatusCode status) {
        this.status = status;
        return this;
    }

    /**
     * List of error messages
     *
     * @return The errors
     */
    @JsonProperty("errors")
    public List<String> getErrors() {
        return errors;
    }

    /**
     * List of error messages
     *
     * @param errors The errors
     */
    @JsonProperty("errors")
    public void setErrors(List<String> errors) {
        this.errors = errors;
    }

    public BatchResponseItem withErrors(List<String> errors) {
        this.errors = errors;
        return this;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(resourceId).append(status).append(errors).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof BatchResponseItem) == false) {
            return false;
        }
        BatchResponseItem rhs = ((BatchResponseItem) other);
        return new EqualsBuilder().append(resourceId, rhs.resourceId).append(status, rhs.status)
            .append(errors, rhs.errors).isEquals();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @JsonProperty("additions")
    public Map<String, String> getAdditions() {
        return this.additions;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @JsonProperty("additions")
    public void setAdditions(Map<String, String> additions) {
        this.additions = additions;
    }

}
