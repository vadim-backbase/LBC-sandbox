package com.backbase.presentation.legalentity.rest.spec.v2.legalentities.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

@Constraint(validatedBy = ParticipantOfValidator.class)
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ParticipantOfType {

    String message() default
      "Must provide exactly one of existingCustomServiceAgreement, newCustomServiceAgreement or newMasterServiceAgreement";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}