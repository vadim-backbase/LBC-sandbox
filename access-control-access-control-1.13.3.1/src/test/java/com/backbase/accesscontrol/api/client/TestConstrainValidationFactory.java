package com.backbase.accesscontrol.api.client;

import com.backbase.buildingblocks.backend.validation.AdditionalPropertiesValidator;
import javax.validation.ConstraintValidator;
import org.springframework.web.bind.support.SpringWebConstraintValidatorFactory;
import org.springframework.web.context.WebApplicationContext;

public class TestConstrainValidationFactory extends SpringWebConstraintValidatorFactory {

    private WebApplicationContext ctx;

    public TestConstrainValidationFactory(WebApplicationContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public <T extends ConstraintValidator<?, ?>> T getInstance(Class<T> key) {
        ConstraintValidator instance;
        if (key.equals(AdditionalPropertiesValidator.class)) {
            instance = ctx.getBean(key);
        } else {
            instance = super.getInstance(key);
        }
        return (T) instance;
    }

    @Override
    protected WebApplicationContext getWebApplicationContext() {
        return ctx;
    }

}
