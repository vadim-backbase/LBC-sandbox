
package com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.users;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Generated;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import com.backbase.buildingblocks.persistence.model.AdditionalPropertiesAware;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.function.Privilege;
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
    "arrangementId",
    "privileges"
})
@JsonIgnoreProperties(ignoreUnknown = true)
public class ArrangementPrivilegesGetResponseBody implements AdditionalPropertiesAware
{

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("arrangementId")
    @Size(min = 1, max = 36)
    @NotNull
    private String arrangementId;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("privileges")
    @Valid
    @NotNull
    private List<Privilege> privileges = new ArrayList<Privilege>();
    /**
     * Additional Properties
     * 
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Map<String, String> additions = new HashMap<String, String>();

    /**
     * 
     * (Required)
     * 
     * @return
     *     The arrangementId
     */
    @JsonProperty("arrangementId")
    public String getArrangementId() {
        return arrangementId;
    }

    /**
     * 
     * (Required)
     * 
     * @param arrangementId
     *     The arrangementId
     */
    @JsonProperty("arrangementId")
    public void setArrangementId(String arrangementId) {
        this.arrangementId = arrangementId;
    }

    public ArrangementPrivilegesGetResponseBody withArrangementId(String arrangementId) {
        this.arrangementId = arrangementId;
        return this;
    }

    /**
     * 
     * (Required)
     * 
     * @return
     *     The privileges
     */
    @JsonProperty("privileges")
    public List<Privilege> getPrivileges() {
        return privileges;
    }

    /**
     * 
     * (Required)
     * 
     * @param privileges
     *     The privileges
     */
    @JsonProperty("privileges")
    public void setPrivileges(List<Privilege> privileges) {
        this.privileges = privileges;
    }

    public ArrangementPrivilegesGetResponseBody withPrivileges(List<Privilege> privileges) {
        this.privileges = privileges;
        return this;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(arrangementId).append(privileges).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ArrangementPrivilegesGetResponseBody) == false) {
            return false;
        }
        ArrangementPrivilegesGetResponseBody rhs = ((ArrangementPrivilegesGetResponseBody) other);
        return new EqualsBuilder().append(arrangementId, rhs.arrangementId).append(privileges, rhs.privileges).isEquals();
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
