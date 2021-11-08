package com.backbase.accesscontrol.audit;

/**
 * Event action type.
 */
public enum EventAction {
    CREATE("Create"),
    UPDATE("Update"),
    DELETE("Delete"),
    UPDATE_PERMISSIONS("Update Permissions"),
    REQUEST_PERMISSIONS_UPDATE("Request Permissions Update"),
    APPROVE("Approve pending operation"),
    REJECT("Reject pending operation"),
    UPDATE_PENDING("Request Update"),
    CREATE_PENDING("Request Create"),
    DELETE_PENDING("Request Delete"),
    ADD_USERS("Add Users"),
    REMOVE_USERS("Remove Users"),
    UPDATE_ADMINS("Update Admins"),
    UPDATE_USERS("Update Users"),
    UPDATE_PARTICIPANTS("Update Participants"),
    UPDATE_ASSOCIATED_APS("Update associated APSs"),
    UPDATE_STATE("Update State");

    String actionEvent;

    EventAction(String actionEvent) {
        this.actionEvent = actionEvent;
    }

    public String getActionEvent() {
        return actionEvent;
    }
}
