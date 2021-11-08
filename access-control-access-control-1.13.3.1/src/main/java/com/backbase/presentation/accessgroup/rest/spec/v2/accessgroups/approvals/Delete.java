
package com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals;

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
    "old",
    "new"
})
public class Delete implements AdditionalPropertiesAware
{

    @JsonProperty("old")
    private Boolean old;
    @JsonProperty("new")
    private Boolean _new;
    /**
     * Additional Properties
     * 
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Map<String, String> additions = new HashMap<String, String>();

    /**
     * 
     * @return
     *     The old
     */
    @JsonProperty("old")
    public Boolean getOld() {
        return old;
    }

    /**
     * 
     * @param old
     *     The old
     */
    @JsonProperty("old")
    public void setOld(Boolean old) {
        this.old = old;
    }

    public Delete withOld(Boolean old) {
        this.old = old;
        return this;
    }

    /**
     * 
     * @return
     *     The _new
     */
    @JsonProperty("new")
    public Boolean getNew() {
        return _new;
    }

    /**
     * 
     * @param _new
     *     The new
     */
    @JsonProperty("new")
    public void setNew(Boolean _new) {
        this._new = _new;
    }

    public Delete withNew(Boolean _new) {
        this._new = _new;
        return this;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(old).append(_new).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Delete) == false) {
            return false;
        }
        Delete rhs = ((Delete) other);
        return new EqualsBuilder().append(old, rhs.old).append(_new, rhs._new).isEquals();
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
