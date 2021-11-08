
package com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Generated;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import com.backbase.buildingblocks.persistence.model.AdditionalPropertiesAware;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
    "externalServiceAgreementId",
    "name"
})
public class NameIdentifier implements AdditionalPropertiesAware
{

    /**
     * Id of Service Agreement on which entity belongs.
     * (Required)
     * 
     */
    @JsonProperty("externalServiceAgreementId")
    @Pattern(regexp = "^\\S(.*(\\S))?$")
    @Size(min = 1, max = 64)
    @NotNull
    private String externalServiceAgreementId;
    /**
     * Entity name.
     * (Required)
     * 
     */
    @JsonProperty("name")
    @Pattern(regexp = "^\\S(.*(\\S))?$")
    @Size(min = 1, max = 128)
    @NotNull
    private String name;
    /**
     * Additional Properties
     * 
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Map<String, String> additions = new HashMap<String, String>();

    /**
     * Id of Service Agreement on which entity belongs.
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
     * Id of Service Agreement on which entity belongs.
     * (Required)
     * 
     * @param externalServiceAgreementId
     *     The externalServiceAgreementId
     */
    @JsonProperty("externalServiceAgreementId")
    public void setExternalServiceAgreementId(String externalServiceAgreementId) {
        this.externalServiceAgreementId = externalServiceAgreementId;
    }

    public NameIdentifier withExternalServiceAgreementId(String externalServiceAgreementId) {
        this.externalServiceAgreementId = externalServiceAgreementId;
        return this;
    }

    /**
     * Entity name.
     * (Required)
     * 
     * @return
     *     The name
     */
    @JsonProperty("name")
    public String getName() {
        return name;
    }

    /**
     * Entity name.
     * (Required)
     * 
     * @param name
     *     The name
     */
    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    public NameIdentifier withName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(externalServiceAgreementId).append(name).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof NameIdentifier) == false) {
            return false;
        }
        NameIdentifier rhs = ((NameIdentifier) other);
        return new EqualsBuilder().append(externalServiceAgreementId, rhs.externalServiceAgreementId).append(name, rhs.name).isEquals();
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
