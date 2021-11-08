package com.backbase.accesscontrol.api.client;

import com.backbase.buildingblocks.backend.validation.AdditionalProperties;
import com.backbase.buildingblocks.backend.validation.AdditionalPropertiesValidator;
import com.backbase.buildingblocks.persistence.model.AdditionalPropertiesAware;
import com.backbase.buildingblocks.validation.AdditionalPropertiesUtil;
import com.backbase.buildingblocks.validation.AdditionalPropertiesValidatorImpl;
import com.backbase.buildingblocks.validation.config.ApiExtensionConfig;
import javax.validation.ConstraintValidator;
import org.hibernate.validator.HibernateValidator;
import org.junit.Ignore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.context.support.GenericWebApplicationContext;

@Ignore
public class ValidatorTestSetup {

    @Autowired
    private MockServletContext servletContext;

    LocalValidatorFactoryBean getLocalValidatorFactoryBean() {
        final GenericWebApplicationContext context = new GenericWebApplicationContext(servletContext);
        final ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();

        ApiExtensionConfig apiExtensionConfig = new ApiExtensionConfig();
        beanFactory.registerSingleton(ApiExtensionConfig.class.getCanonicalName(), apiExtensionConfig);

        AdditionalPropertiesUtil additionalPropertiesUtil = new AdditionalPropertiesUtil();
        ReflectionTestUtils.setField(additionalPropertiesUtil, "config", apiExtensionConfig);
        beanFactory.registerSingleton(AdditionalPropertiesUtil.class.getCanonicalName(), additionalPropertiesUtil);

        ConstraintValidator<AdditionalProperties, AdditionalPropertiesAware> additionalPropertiesValidator = new AdditionalPropertiesValidatorImpl();
        ReflectionTestUtils.setField(additionalPropertiesValidator, "util", additionalPropertiesUtil);
        beanFactory.registerSingleton(additionalPropertiesValidator.getClass().getCanonicalName(),
            additionalPropertiesValidator);

        AdditionalPropertiesValidator singletonObject = new AdditionalPropertiesValidatorImpl();
        beanFactory.registerSingleton(AdditionalPropertiesValidator.class.getCanonicalName(), singletonObject);
        context.refresh();

        LocalValidatorFactoryBean validatorFactoryBean = new LocalValidatorFactoryBean();
        validatorFactoryBean.setApplicationContext(context);
        TestConstrainValidationFactory constraintFactory = new TestConstrainValidationFactory(context);
        validatorFactoryBean.setConstraintValidatorFactory(constraintFactory);
        validatorFactoryBean.setProviderClass(HibernateValidator.class);
        validatorFactoryBean.afterPropertiesSet();
        return validatorFactoryBean;
    }
}
