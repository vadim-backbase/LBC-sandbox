package com.backbase.accesscontrol.audit.descriptionprovider;

import static com.backbase.accesscontrol.util.ExceptionUtil.getInternalServerErrorException;
import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_067;
import static java.util.Objects.nonNull;
import static org.apache.commons.collections.CollectionUtils.isEmpty;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;

import com.backbase.accesscontrol.audit.annotation.AuditableParameter;
import com.backbase.accesscontrol.service.rest.spec.model.PresentationUserApsIdentifiers;
import com.backbase.audit.client.model.AuditMessage;
import java.lang.annotation.Annotation;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DescriptorUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(
        DescriptorUtils.class);

    private DescriptorUtils() {
    }

    /**
     * Returns specific argument from clazz type from proceeding join point.
     *
     * @param proceedingJoinPoint - join point
     * @param clazz               - class type
     * @param position            - position of the element you want to get
     */
    public static <T> T getArgument(ProceedingJoinPoint proceedingJoinPoint, Class<T> clazz, int position) {
        Predicate<Object> predicate = (Object obj) -> clazz.isAssignableFrom(obj.getClass());
        return getArgument(proceedingJoinPoint, predicate, position);
    }

    /**
     * Returns specific argument from clazz type from proceeding join point with default position 0.
     *
     * @param proceedingJoinPoint - join point
     * @param clazz               - class type
     */
    public static <T> T getArgument(ProceedingJoinPoint proceedingJoinPoint, Class<T> clazz) {
        return getArgument(proceedingJoinPoint, clazz, 0);
    }

    /**
     * Returns specific argument depending on predicate from proceeding join point with default position 0.
     *
     * @param proceedingJoinPoint - join point
     * @param predicate           - predicate
     */
    public static <T> T getArgument(ProceedingJoinPoint proceedingJoinPoint, Predicate<Object> predicate) {
        return getArgument(proceedingJoinPoint, predicate, 0);
    }

    /**
     * Returns specific argument depending on predicate from proceeding join point.
     *
     * @param proceedingJoinPoint - join point
     * @param predicate           - predicate
     * @param position            - position of the element you want to get
     */
    public static <T> T getArgument(ProceedingJoinPoint proceedingJoinPoint, Predicate<Object> predicate,
        int position) {
        T requestBody = null;
        int counter = 0;
        Object[] methodArguments = proceedingJoinPoint.getArgs();
        for (Object argument : methodArguments) {
            if (Objects.nonNull(argument) && predicate.test(argument)) {
                requestBody = (T) argument;
                counter++;
                if (counter > position) {
                    break;
                }
            }
        }
        return Optional.ofNullable(requestBody).orElseThrow(() -> {
            LOGGER.warn("Failed extracting audit data from request.");
            return getInternalServerErrorException(ERR_AG_067.getErrorMessage());
        });
    }

    /**
     * Returns specific argument from clazz type from proceeding join point annotead with {@link AuditableParameter}
     * having {@link AuditableParameter#objectName()} equal to provided annotated name.
     *
     * @param proceedingJoinPoint - join point
     * @param clazz               - class type
     */
    public static <T> T getArgument(ProceedingJoinPoint proceedingJoinPoint, Class<T> clazz,
        String annotatedArgumentName) {

        T requestBody = null;
        final Signature signature = proceedingJoinPoint.getSignature();
        if (signature instanceof MethodSignature) {
            final MethodSignature ms = (MethodSignature) signature;
            requestBody = getAnnotatedParameter(clazz, annotatedArgumentName, requestBody,
                proceedingJoinPoint.getArgs(), ms.getMethod().getParameterAnnotations());
        }
        return requestBody;
    }

    /**
     * Returns specific path parameter from HttpServletRequest.
     *
     * @param joinPoint     - join point
     * @param parameterName - the name of the parameter to be returned
     */
    public static String getPathParameter(ProceedingJoinPoint joinPoint, String parameterName) {
        String parameterValue = getArgument(joinPoint, String.class);
        if (parameterValue == null) {
            LOGGER.warn("Failed to extract Audit data from request");
            throw getInternalServerErrorException(ERR_AG_067.getErrorMessage());
        }
        return parameterValue;
    }

    private static <T> T getAnnotatedParameter(Class<T> clazz, String annotatedArgumentName, T requestBody,
        Object[] methodArguments, Annotation[][] parameterAnnotations) {
        for (int i = 0; i < parameterAnnotations.length; i++) {
            final AuditableParameter paramAnnotation =
                getAnnotationByType(parameterAnnotations[i], annotatedArgumentName);
            if (paramAnnotation != null && clazz.isAssignableFrom(methodArguments[i].getClass())) {
                requestBody = clazz.cast(methodArguments[i]);
            }

        }
        return requestBody;
    }

    /**
     * In an array of annotations, find the annotation of the specified type, if any.
     *
     * @return the annotation if available, or null
     */
    private static AuditableParameter getAnnotationByType(final Annotation[] annotations,
        String annotatedArgumentName) {

        AuditableParameter result = null;
        for (final Annotation annotation : annotations) {
            if (AuditableParameter.class.isAssignableFrom(annotation.getClass())) {
                AuditableParameter auditableParameter = (AuditableParameter) annotation;
                if (auditableParameter.objectName().equals(annotatedArgumentName)) {
                    result = auditableParameter;
                    break;
                }
            }
        }
        return result;
    }

    /**
     * Populates audit meta data for initiated and failed events.
     */
    public static void populateUserApsIdentifiersMetadata(AuditMessage auditMessage,
        PresentationUserApsIdentifiers userApsIdentifiers, String userApsIdsFieldName,
        String userApsNamesFieldName) {
        if (nonNull(userApsIdentifiers)) {
            if (isEmpty(userApsIdentifiers.getIdIdentifiers()) && isEmpty(userApsIdentifiers.getNameIdentifiers())) {
                auditMessage.withEventMetaDatum(userApsIdsFieldName, "");
                auditMessage.withEventMetaDatum(userApsNamesFieldName, "");
            }

            if (isNotEmpty(userApsIdentifiers.getIdIdentifiers())) {
                auditMessage.withEventMetaDatum(userApsIdsFieldName,
                    userApsIdentifiers.getIdIdentifiers().stream().map(BigDecimal::toString)
                        .collect(Collectors.joining(", ")));
            }

            if (isNotEmpty(userApsIdentifiers.getNameIdentifiers())) {
                auditMessage.withEventMetaDatum(userApsNamesFieldName,
                    String.join(", ", userApsIdentifiers.getNameIdentifiers()));
            }
        }
    }

    /**
     * Populates audit meta data for success events.
     */
    public static void populateSuccessUserApsIdentifiersMetadata(AuditMessage auditMessage,
        PresentationUserApsIdentifiers userAps,
        String idMetaData, String nameMetaData) {
        if (nonNull(userAps) && isNotEmpty(
            userAps.getIdIdentifiers())) {
            auditMessage.withEventMetaDatum(idMetaData,
                userAps.getIdIdentifiers().stream().map(BigDecimal::toString)
                    .collect(Collectors.joining(", ")));
        }

        if (nonNull(userAps) && isNotEmpty(
            userAps.getNameIdentifiers())) {
            auditMessage.withEventMetaDatum(nameMetaData,
                String.join(", ", userAps.getNameIdentifiers()));
        }
    }

}