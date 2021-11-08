package com.backbase.accesscontrol.audit;

import java.util.Objects;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class AuditEventAction {

    private EventAction eventAction;

    private AuditObjectType objectType;

    public EventAction getEventAction() {
        return eventAction;
    }

    public void setEventAction(EventAction eventAction) {
        this.eventAction = eventAction;
    }

    public AuditObjectType getObjectType() {
        return objectType;
    }

    public void setObjectType(AuditObjectType objectType) {
        this.objectType = objectType;
    }

    public AuditEventAction withEventAction(EventAction eventAction) {
        this.eventAction = eventAction;
        return this;
    }

    public AuditEventAction withObjectType(AuditObjectType objectType) {
        this.objectType = objectType;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AuditEventAction that = (AuditEventAction) o;
        return eventAction == that.eventAction
            && objectType == that.objectType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventAction, objectType);
    }
}
