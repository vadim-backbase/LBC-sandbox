
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
    "query",
    "cursor",
    "from",
    "size"
})
public class PageableSearchableParameters implements AdditionalPropertiesAware
{

    /**
     * Parameter which is used for searching.
     * 
     */
    @JsonProperty("query")
    private String query;
    /**
     * Record UUID. As an alternative for specifying 'from' this allows to point to the record to start the selection from.
     * 
     */
    @JsonProperty("cursor")
    private String cursor;
    /**
     * Page Number. Skip over pages of elements by specifying a start value for the query.
     * 
     */
    @JsonProperty("from")
    private Integer from = 0;
    /**
     * Query parameter for pagination - number of records that will be shown on the page.
     * 
     */
    @JsonProperty("size")
    private Integer size = 10;
    /**
     * Additional Properties
     * 
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Map<String, String> additions = new HashMap<String, String>();

    /**
     * Parameter which is used for searching.
     * 
     * @return
     *     The query
     */
    @JsonProperty("query")
    public String getQuery() {
        return query;
    }

    /**
     * Parameter which is used for searching.
     * 
     * @param query
     *     The query
     */
    @JsonProperty("query")
    public void setQuery(String query) {
        this.query = query;
    }

    public PageableSearchableParameters withQuery(String query) {
        this.query = query;
        return this;
    }

    /**
     * Record UUID. As an alternative for specifying 'from' this allows to point to the record to start the selection from.
     * 
     * @return
     *     The cursor
     */
    @JsonProperty("cursor")
    public String getCursor() {
        return cursor;
    }

    /**
     * Record UUID. As an alternative for specifying 'from' this allows to point to the record to start the selection from.
     * 
     * @param cursor
     *     The cursor
     */
    @JsonProperty("cursor")
    public void setCursor(String cursor) {
        this.cursor = cursor;
    }

    public PageableSearchableParameters withCursor(String cursor) {
        this.cursor = cursor;
        return this;
    }

    /**
     * Page Number. Skip over pages of elements by specifying a start value for the query.
     * 
     * @return
     *     The from
     */
    @JsonProperty("from")
    public Integer getFrom() {
        return from;
    }

    /**
     * Page Number. Skip over pages of elements by specifying a start value for the query.
     * 
     * @param from
     *     The from
     */
    @JsonProperty("from")
    public void setFrom(Integer from) {
        this.from = from;
    }

    public PageableSearchableParameters withFrom(Integer from) {
        this.from = from;
        return this;
    }

    /**
     * Query parameter for pagination - number of records that will be shown on the page.
     * 
     * @return
     *     The size
     */
    @JsonProperty("size")
    public Integer getSize() {
        return size;
    }

    /**
     * Query parameter for pagination - number of records that will be shown on the page.
     * 
     * @param size
     *     The size
     */
    @JsonProperty("size")
    public void setSize(Integer size) {
        this.size = size;
    }

    public PageableSearchableParameters withSize(Integer size) {
        this.size = size;
        return this;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(query).append(cursor).append(from).append(size).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof PageableSearchableParameters) == false) {
            return false;
        }
        PageableSearchableParameters rhs = ((PageableSearchableParameters) other);
        return new EqualsBuilder().append(query, rhs.query).append(cursor, rhs.cursor).append(from, rhs.from).append(size, rhs.size).isEquals();
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
