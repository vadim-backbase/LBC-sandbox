package com.backbase.accesscontrol.business.batch;

import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.BatchResponseItemExtended;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class ProcessableBatchBody<T> {

    private final T item;
    private final Integer order;
    private BatchResponseItemExtended response;

    public ProcessableBatchBody(T item, Integer order) {
        this.item = item;
        this.order = order;
    }

    public T getItem() {
        return item;
    }

    public Integer getOrder() {
        return order;
    }

    public BatchResponseItemExtended getResponse() {
        return response;
    }

    public void setResponse(BatchResponseItemExtended response) {
        this.response = response;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ProcessableBatchBody<?> that = (ProcessableBatchBody<?>) o;

        return new EqualsBuilder()
            .append(order, that.order)
            .append(item, that.item)
            .append(response, that.response)
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
            .append(item)
            .append(order)
            .append(response)
            .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("item", item)
            .append("order", order)
            .append("response", response)
            .toString();
    }
}
