
package com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Generated;
import com.backbase.buildingblocks.persistence.model.AdditionalPropertiesAware;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

@Generated("org.jsonschema2pojo")
public enum BatchResponseStatusCode implements AdditionalPropertiesAware
{

    HTTP_STATUS_OK("200"),
    HTTP_STATUS_BAD_REQUEST("400"),
    HTTP_STATUS_NOT_FOUND("404"),
    HTTP_STATUS_INTERNAL_SERVER_ERROR("500");
    private final String value;
    private final static Map<String, BatchResponseStatusCode> CONSTANTS = new HashMap<String, BatchResponseStatusCode>();
    /**
     * Additional Properties
     * 
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Map<String, String> additions = new HashMap<String, String>();

    static {
        for (BatchResponseStatusCode c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private BatchResponseStatusCode(String value) {
        this.value = value;
    }

    @JsonValue
    @Override
    public String toString() {
        return this.value;
    }

    @JsonCreator
    public static BatchResponseStatusCode fromValue(String value) {
        BatchResponseStatusCode constant = CONSTANTS.get(value);
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
