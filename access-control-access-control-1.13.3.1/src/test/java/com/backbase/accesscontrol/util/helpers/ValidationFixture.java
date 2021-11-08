package com.backbase.accesscontrol.util.helpers;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.reflect.Field;
import javax.validation.ConstraintViolation;
import javax.validation.Path;
import javax.validation.metadata.ConstraintDescriptor;
import org.hibernate.validator.internal.engine.ConstraintViolationImpl;
import org.hibernate.validator.internal.engine.path.PathImpl;
import org.hibernate.validator.internal.metadata.core.ConstraintHelper;
import org.hibernate.validator.internal.metadata.descriptor.ConstraintDescriptorImpl;
import org.hibernate.validator.internal.metadata.location.ConstraintLocation.ConstraintLocationKind;
import org.hibernate.validator.internal.util.annotation.ConstraintAnnotationDescriptor;

/**
 * Test fixtures for validation constraints and handling
 */
public class ValidationFixture {

    private static final String MESSAGE_TEMPLATE = "{messageTemplate}";
    private static final String MESSAGE = "message";

    private ValidationFixture() {
    }


    public static <T, A extends Annotation> ConstraintViolation<T> createConstraintViolation(
        T rootBean,
        Class<T> clazz,
        Class<A> annotationClazz,
        Object leafBean,
        String field,
        Object offendingValue) {

        ConstraintViolation<T> violation;
        try {
            Field member = leafBean.getClass().getDeclaredField(field);
            A annotation = member.getAnnotation(annotationClazz);
            ConstraintDescriptor<A> descriptor = new ConstraintDescriptorImpl<A>(
                ConstraintHelper.forAllBuiltinConstraints(),
                null,
                new ConstraintAnnotationDescriptor<A>(annotation),
                ConstraintLocationKind.FIELD);
            Path p = PathImpl.createPathFromString(field);
            violation = ConstraintViolationImpl.forBeanValidation(
                MESSAGE_TEMPLATE,
                null,
                null,
                MESSAGE,
                clazz,
                rootBean,
                leafBean,
                offendingValue,
                p,
                descriptor,
                ElementType.FIELD);
        } catch (NoSuchFieldException exception) {
            throw new RuntimeException(exception);
        }

        return violation;
    }
}