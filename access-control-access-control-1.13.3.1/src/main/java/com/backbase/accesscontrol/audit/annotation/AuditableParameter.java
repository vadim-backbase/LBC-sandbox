package com.backbase.accesscontrol.audit.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Identifying that a method is to be audited.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuditableParameter {

    /**
     * The name of the auditable parameter.
     *
     * @return the name of the parameter
     */
    String objectName();
}
