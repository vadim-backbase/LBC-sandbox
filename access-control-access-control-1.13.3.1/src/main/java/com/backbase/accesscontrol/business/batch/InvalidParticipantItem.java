package com.backbase.accesscontrol.business.batch;

import java.util.List;

public class InvalidParticipantItem {

    private final int order;
    private List<String> errors;

    public InvalidParticipantItem(DataItemValidatableItem item, List<String> errors) {
        order = item.getBatchBody().getOrder();
        this.errors = errors;
    }

    public InvalidParticipantItem(int order, List<String> errors) {
        this.order = order;
        this.errors = errors;
    }

    public int getOrder() {
        return order;
    }

    public List<String> getErrors() {
        return errors;
    }
}
