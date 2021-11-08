package com.backbase.presentation.legalentity.rest.spec.v2.legalentities;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Generated;
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
    "shareUsers",
    "shareAccounts"
})
public class ParticipantInfo implements AdditionalPropertiesAware
{

    @JsonProperty("shareUsers")
    private Boolean shareUsers;
    
    @JsonProperty("shareAccounts")
    private Boolean shareAccounts;
    /**
     * Additional Properties
     * 
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Map<String, String> additions = new HashMap<String, String>();

    /**
     * 
     * @return
     *     The shareUsers
     */
    @JsonProperty("shareUsers")
    public Boolean getShareUsers() {
        return shareUsers;
    }

    /**
     * 
     * @param shareUsers
     *     The shareUsers
     */
    @JsonProperty("shareUsers")
    public void setShareUsers(Boolean shareUsers) {
        this.shareUsers = shareUsers;
    }

    public ParticipantInfo withShareUsers(Boolean shareUsers) {
        this.shareUsers = shareUsers;
        return this;
    }

    /**
     * 
     * @return
     *     The shareAccounts
     */
    @JsonProperty("shareAccounts")
    public Boolean getShareAccounts() {
        return shareAccounts;
    }

    /**
     * 
     * @param shareAccounts
     *     The shareAccounts
     */
    @JsonProperty("shareAccounts")
    public void setShareAccounts(Boolean shareAccounts) {
        this.shareAccounts = shareAccounts;
    }

    public ParticipantInfo withShareAccounts(Boolean shareAccounts) {
        this.shareAccounts = shareAccounts;
        return this;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(shareUsers).append(shareAccounts).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ParticipantInfo) == false) {
            return false;
        }
        ParticipantInfo rhs = ((ParticipantInfo) other);
        return new EqualsBuilder().append(shareUsers, rhs.shareUsers).append(shareAccounts, rhs.shareAccounts).isEquals();
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
