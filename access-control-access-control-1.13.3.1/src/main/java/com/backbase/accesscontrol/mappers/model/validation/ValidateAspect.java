package com.backbase.accesscontrol.mappers.model.validation;

import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.buildingblocks.presentation.errors.Error;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import lombok.AllArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Component
@Aspect
@AllArgsConstructor
public class ValidateAspect {

    private Validator validator;

    @Around("@annotation(com.backbase.accesscontrol.mappers.model.validation.ValidatePayload)")
    public Object validatePayload(ProceedingJoinPoint joinPoint) throws Throwable {

        Object result = joinPoint.proceed();

        Set<ConstraintViolation<Object>> violations = validator.validate(result);
        if (!violations.isEmpty()) {
            throw new BadRequestException()
                .withErrors(createViolationErrors(violations))
                .withMessage("Bad Request");
        }

        return result;
    }

    private List<Error> createViolationErrors(Set<ConstraintViolation<Object>> violations) {
        return violations.stream().map(violation -> new Error()
            .withMessage(violation.getMessage())
            .withKey(violation.getPropertyPath().toString()))
            .collect(Collectors.toList());
    }
}
