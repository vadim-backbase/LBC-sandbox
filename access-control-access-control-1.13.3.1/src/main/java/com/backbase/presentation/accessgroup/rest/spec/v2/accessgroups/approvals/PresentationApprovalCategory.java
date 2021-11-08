
package com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Generated;
import com.backbase.buildingblocks.persistence.model.AdditionalPropertiesAware;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

@Generated("org.jsonschema2pojo")
public enum PresentationApprovalCategory implements AdditionalPropertiesAware
{

    ASSIGN_PERMISSIONS("Assign Permissions"),
    MANAGE_DATA_GROUPS("Manage Data Groups"),
    MANAGE_LIMITS("Manage Limits"),
    MANAGE_SHADOW_LIMITS("Manage Shadow Limits"),
    UNLOCK_USER("Unlock User"),
    MANAGE_FUNCTION_GROUPS("Manage Function Groups"),
    MANAGE_SERVICE_AGREEMENTS("Manage Service Agreements");
    private final String value;
    private final static Map<String, PresentationApprovalCategory> CONSTANTS = new HashMap<String, PresentationApprovalCategory>();
    /**
     * Additional Properties
     * 
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Map<String, String> additions = new HashMap<String, String>();

    static {
        for (PresentationApprovalCategory c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private PresentationApprovalCategory(String value) {
        this.value = value;
    }

    @JsonValue
    @Override
    public String toString() {
        return this.value;
    }

    @JsonCreator
    public static PresentationApprovalCategory fromValue(String value) {
        PresentationApprovalCategory constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
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
