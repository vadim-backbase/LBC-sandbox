
package com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreement;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Generated;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import com.backbase.buildingblocks.persistence.model.AdditionalPropertiesAware;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.PresentationAction;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Add Participants in Service Agreement
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
    "action",
    "externalServiceAgreementId",
    "externalParticipantId",
    "sharingUsers",
    "sharingAccounts"
})
public class PresentationParticipantPutBody implements AdditionalPropertiesAware
{

    /**
     * Presentation action
     * (Required)
     * 
     */
    @JsonProperty("action")
    @NotNull
    private PresentationAction action;
    /**
     * External Unique Identifier.
     * (Required)
     * 
     */
    @JsonProperty("externalServiceAgreementId")
    @Pattern(regexp = "^[^\\r\\n]{1,64}$")
    @Size(min = 1, max = 64)
    @NotNull
    private String externalServiceAgreementId;
    /**
     * External Unique Identifier.
     * (Required)
     * 
     */
    @JsonProperty("externalParticipantId")
    @Pattern(regexp = "^[^\\r\\n]{1,64}$")
    @Size(min = 1, max = 64)
    @NotNull
    private String externalParticipantId;
    /**
     * Boolean value if the Participant is sharing users
     * 
     */
    @JsonProperty("sharingUsers")
    private Boolean sharingUsers = false;
    /**
     * Boolean value if the Participant is sharing accounts
     * 
     */
    @JsonProperty("sharingAccounts")
    private Boolean sharingAccounts = false;
    /**
     * Additional Properties
     * 
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Map<String, String> additions = new HashMap<String, String>();

    /**
     * Presentation action
     * (Required)
     * 
     * @return
     *     The action
     */
    @JsonProperty("action")
    public PresentationAction getAction() {
        return action;
    }

    /**
     * Presentation action
     * (Required)
     * 
     * @param action
     *     The action
     */
    @JsonProperty("action")
    public void setAction(PresentationAction action) {
        this.action = action;
    }

    public PresentationParticipantPutBody withAction(PresentationAction action) {
        this.action = action;
        return this;
    }

    /**
     * External Unique Identifier.
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
     * External Unique Identifier.
     * (Required)
     * 
     * @param externalServiceAgreementId
     *     The externalServiceAgreementId
     */
    @JsonProperty("externalServiceAgreementId")
    public void setExternalServiceAgreementId(String externalServiceAgreementId) {
        this.externalServiceAgreementId = externalServiceAgreementId;
    }

    public PresentationParticipantPutBody withExternalServiceAgreementId(String externalServiceAgreementId) {
        this.externalServiceAgreementId = externalServiceAgreementId;
        return this;
    }

    /**
     * External Unique Identifier.
     * (Required)
     * 
     * @return
     *     The externalParticipantId
     */
    @JsonProperty("externalParticipantId")
    public String getExternalParticipantId() {
        return externalParticipantId;
    }

    /**
     * External Unique Identifier.
     * (Required)
     * 
     * @param externalParticipantId
     *     The externalParticipantId
     */
    @JsonProperty("externalParticipantId")
    public void setExternalParticipantId(String externalParticipantId) {
        this.externalParticipantId = externalParticipantId;
    }

    public PresentationParticipantPutBody withExternalParticipantId(String externalParticipantId) {
        this.externalParticipantId = externalParticipantId;
        return this;
    }

    /**
     * Boolean value if the Participant is sharing users
     * 
     * @return
     *     The sharingUsers
     */
    @JsonProperty("sharingUsers")
    public Boolean getSharingUsers() {
        return sharingUsers;
    }

    /**
     * Boolean value if the Participant is sharing users
     * 
     * @param sharingUsers
     *     The sharingUsers
     */
    @JsonProperty("sharingUsers")
    public void setSharingUsers(Boolean sharingUsers) {
        this.sharingUsers = sharingUsers;
    }

    public PresentationParticipantPutBody withSharingUsers(Boolean sharingUsers) {
        this.sharingUsers = sharingUsers;
        return this;
    }

    /**
     * Boolean value if the Participant is sharing accounts
     * 
     * @return
     *     The sharingAccounts
     */
    @JsonProperty("sharingAccounts")
    public Boolean getSharingAccounts() {
        return sharingAccounts;
    }

    /**
     * Boolean value if the Participant is sharing accounts
     * 
     * @param sharingAccounts
     *     The sharingAccounts
     */
    @JsonProperty("sharingAccounts")
    public void setSharingAccounts(Boolean sharingAccounts) {
        this.sharingAccounts = sharingAccounts;
    }

    public PresentationParticipantPutBody withSharingAccounts(Boolean sharingAccounts) {
        this.sharingAccounts = sharingAccounts;
        return this;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(action).append(externalServiceAgreementId).append(externalParticipantId).append(sharingUsers).append(sharingAccounts).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof PresentationParticipantPutBody) == false) {
            return false;
        }
        PresentationParticipantPutBody rhs = ((PresentationParticipantPutBody) other);
        return new EqualsBuilder().append(action, rhs.action).append(externalServiceAgreementId, rhs.externalServiceAgreementId).append(externalParticipantId, rhs.externalParticipantId).append(sharingUsers, rhs.sharingUsers).append(sharingAccounts, rhs.sharingAccounts).isEquals();
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
