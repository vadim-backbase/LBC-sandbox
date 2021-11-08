
package com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.users;

import com.backbase.buildingblocks.persistence.model.AdditionalPropertiesAware;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Generated;

@Generated("org.jsonschema2pojo")
public enum AccessResourceType implements AdditionalPropertiesAware
{

    NONE("NONE"),
    USER("USER"),
    ACCOUNT("ACCOUNT"),
    USER_OR_ACCOUNT("USER_OR_ACCOUNT"),
    USER_AND_ACCOUNT("USER_AND_ACCOUNT");
    private final String value;
    private final static Map<String, AccessResourceType> CONSTANTS = new HashMap<String, AccessResourceType>();
    /**
     * Additional Properties
     * 
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Map<String, String> additions = new HashMap<String, String>();

    static {
        for (AccessResourceType c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private AccessResourceType(String value) {
        this.value = value;
    }

    @JsonValue
    @Override
    public String toString() {
        return this.value;
    }

    @JsonCreator
    public static AccessResourceType fromValue(String value) {
        AccessResourceType constant = CONSTANTS.get(value);
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
