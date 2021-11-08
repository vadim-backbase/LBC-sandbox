package com.backbase.accesscontrol.audit.annotation;

import com.backbase.accesscontrol.audit.AuditObjectType;
import com.backbase.accesscontrol.audit.EventAction;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Identifying that a method is to be audited.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuditEvent {

    /**
     * Type of audit object.
     */
    AuditObjectType objectType();

    /**
     * Audit action.
     */
    EventAction eventAction();
}

