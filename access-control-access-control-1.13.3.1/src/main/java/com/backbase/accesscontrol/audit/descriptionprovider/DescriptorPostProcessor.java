package com.backbase.accesscontrol.audit.descriptionprovider;

import static com.backbase.accesscontrol.util.ExceptionUtil.getInternalServerErrorException;

import com.backbase.accesscontrol.audit.AuditEventAction;
import com.backbase.accesscontrol.audit.annotation.AuditEvent;
import com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes;
import java.lang.reflect.Method;
import java.util.Map;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * Post Processor that checks if Descriptor is provided for all methods annotated with AuditEvent.
 */
@Component
@ConditionalOnProperty(name = "backbase.audit.enabled", havingValue = "true", matchIfMissing = true)
public class DescriptorPostProcessor implements BeanPostProcessor {

    @Autowired
    private ApplicationContext applicationContext;

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        Method[] methods = AopUtils.getTargetClass(bean).getDeclaredMethods();
        for (Method method : methods) {
            if (method.isAnnotationPresent(AuditEvent.class)) {
                AuditEvent annotation = method.getAnnotation(AuditEvent.class);
                AuditEventAction auditEventAction = new AuditEventAction()
                    .withEventAction(annotation.eventAction())
                    .withObjectType(annotation.objectType());

                Map<String, AbstractDescriptionProvider> beansOfType = applicationContext
                    .getBeansOfType(AbstractDescriptionProvider.class);

                checkIfDescriptorWithEventActionTypeExists(auditEventAction, beansOfType, beanName);
            }
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        return bean;
    }

    private void checkIfDescriptorWithEventActionTypeExists(AuditEventAction auditEventAction,
        Map<String, AbstractDescriptionProvider> beansOfType, String beanName) {
        if (beansOfType.entrySet()
            .stream()
            .noneMatch(mapEntry -> mapEntry.getValue()
                .getAuditEventAction().equals(auditEventAction))) {
            throw getInternalServerErrorException(
                String.format("%s for Event Action [%s] and object type [%s] defined in bean [%s]",
                    AccessGroupErrorCodes.ERR_AG_068.getErrorMessage(),
                    auditEventAction.getEventAction(),
                    auditEventAction.getObjectType(),
                    beanName));
        }
    }
}
