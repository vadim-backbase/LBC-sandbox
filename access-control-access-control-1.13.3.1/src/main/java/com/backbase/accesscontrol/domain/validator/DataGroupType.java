package com.backbase.accesscontrol.domain.validator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = DataGroupTypeValidator.class)
public @interface DataGroupType {

    /**
     * Type of data group.
     */
    String message() default "Invalid data group type";

    /**
     * Group of constraint declaration.
     */
    Class<?>[] groups() default {};

    /**
     * Payload type that can be attached to a given constraint declaration.
     */
    Class<? extends Payload>[] payload() default {};
}
