
package com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Generated;
import javax.validation.Valid;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import com.backbase.buildingblocks.persistence.model.AdditionalPropertiesAware;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.PresentationSelfApprovalPolicy;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Function Groups and Data Groups Pairs.
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
    "id",
    "name",
    "description",
    "type",
    "dataGroups"
})
public class PresentationFunctionGroupsDataGroupsPair implements AdditionalPropertiesAware
{

    /**
     * Universally Unique Identifier.
     * 
     */
    @JsonProperty("id")
    @Pattern(regexp = "^[0-9a-f]{32}$|^[0-9a-f-]{36}$")
    @Size(min = 1, max = 36)
    private String id;
    /**
     * Name of function group
     * 
     */
    @JsonProperty("name")
    private String name;
    /**
     * Description of function group
     * 
     */
    @JsonProperty("description")
    private String description;
    @JsonProperty("type")
    private Type type = Type.fromValue("REGULAR");
    @JsonProperty("dataGroups")
    @Valid
    private List<com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.PresentationDataGroupApprovalItem> dataGroups = new ArrayList<com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.PresentationDataGroupApprovalItem>();
    @JsonProperty("selfApprovalPolicies")
    @Valid
    private List<PresentationSelfApprovalPolicy> selfApprovalPolicies = new ArrayList<>();
    /**
     * Additional Properties
     * 
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Map<String, String> additions = new HashMap<String, String>();

    /**
     * Universally Unique Identifier.
     * 
     * @return
     *     The id
     */
    @JsonProperty("id")
    public String getId() {
        return id;
    }

    /**
     * Universally Unique Identifier.
     * 
     * @param id
     *     The id
     */
    @JsonProperty("id")
    public void setId(String id) {
        this.id = id;
    }

    public PresentationFunctionGroupsDataGroupsPair withId(String id) {
        this.id = id;
        return this;
    }

    /**
     * Name of function group
     * 
     * @return
     *     The name
     */
    @JsonProperty("name")
    public String getName() {
        return name;
    }

    /**
     * Name of function group
     * 
     * @param name
     *     The name
     */
    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    public PresentationFunctionGroupsDataGroupsPair withName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Description of function group
     * 
     * @return
     *     The description
     */
    @JsonProperty("description")
    public String getDescription() {
        return description;
    }

    /**
     * Description of function group
     * 
     * @param description
     *     The description
     */
    @JsonProperty("description")
    public void setDescription(String description) {
        this.description = description;
    }

    public PresentationFunctionGroupsDataGroupsPair withDescription(String description) {
        this.description = description;
        return this;
    }

    /**
     * 
     * @return
     *     The type
     */
    @JsonProperty("type")
    public Type getType() {
        return type;
    }

    /**
     * 
     * @param type
     *     The type
     */
    @JsonProperty("type")
    public void setType(Type type) {
        this.type = type;
    }

    public PresentationFunctionGroupsDataGroupsPair withType(Type type) {
        this.type = type;
        return this;
    }

    /**
     * 
     * @return
     *     The dataGroups
     */
    @JsonProperty("dataGroups")
    public List<com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.PresentationDataGroupApprovalItem> getDataGroups() {
        return dataGroups;
    }

    /**
     * 
     * @param dataGroups
     *     The dataGroups
     */
    @JsonProperty("dataGroups")
    public void setDataGroups(List<com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.PresentationDataGroupApprovalItem> dataGroups) {
        this.dataGroups = dataGroups;
    }

    public PresentationFunctionGroupsDataGroupsPair withDataGroups(List<PresentationDataGroupApprovalItem> dataGroups) {
        this.dataGroups = dataGroups;
        return this;
    }

    @JsonProperty("selfApprovalPolicies")
    public List<PresentationSelfApprovalPolicy> getSelfApprovalPolicies() {
        return selfApprovalPolicies;
    }

    @JsonProperty("selfApprovalPolicies")
    public void setSelfApprovalPolicies(List<PresentationSelfApprovalPolicy> selfApprovalPolicies) {
        this.selfApprovalPolicies = selfApprovalPolicies;
    }

    public PresentationFunctionGroupsDataGroupsPair withSelfApprovalPolicies(
        List<PresentationSelfApprovalPolicy> selfApprovalPolicies) {
        this.selfApprovalPolicies = selfApprovalPolicies;
        return this;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(id).append(name).append(description).append(type).append(dataGroups)
            .append(selfApprovalPolicies).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof PresentationFunctionGroupsDataGroupsPair) == false) {
            return false;
        }
        PresentationFunctionGroupsDataGroupsPair rhs = ((PresentationFunctionGroupsDataGroupsPair) other);
        return new EqualsBuilder().append(id, rhs.id).append(name, rhs.name).append(description, rhs.description)
            .append(type, rhs.type).append(dataGroups, rhs.dataGroups)
            .append(selfApprovalPolicies, rhs.selfApprovalPolicies).isEquals();
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

    @Generated("org.jsonschema2pojo")
    public enum Type {
        SYSTEM("SYSTEM"),
        REGULAR("REGULAR"),
        TEMPLATE("TEMPLATE");
        private final String value;
        private final static Map<String, Type> CONSTANTS = new HashMap<String, Type>();

        static {
            for (Type c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        private Type(String value) {
            this.value = value;
        }

        @JsonValue
        @Override
        public String toString() {
            return this.value;
        }

        @JsonCreator
        public static Type fromValue(String value) {
            Type constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

}
