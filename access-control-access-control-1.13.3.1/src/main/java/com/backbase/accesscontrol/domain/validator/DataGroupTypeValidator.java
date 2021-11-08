package com.backbase.accesscontrol.domain.validator;

import static com.backbase.accesscontrol.util.ExceptionUtil.getBadRequestException;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_053;

import com.backbase.accesscontrol.configuration.ValidationConfig;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class DataGroupTypeValidator implements ConstraintValidator<DataGroupType, String> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataGroupTypeValidator.class);

    @Autowired
    ValidationConfig validationConfig;

    /**
     * Initialize with data group type.
     *
     * @param dataGroupType - data group type.
     */
    @Override
    public void initialize(DataGroupType dataGroupType) {
        //This validator should not implement this method.
    }

    /**
     * Validate data group type.
     *
     * @param dataGroupType - data group type.
     * @param cxt           {@link ConstraintValidatorContext}
     * @return true/false
     */
    @Override
    public boolean isValid(String dataGroupType, ConstraintValidatorContext cxt) {
        if (!validationConfig.getTypes().contains(dataGroupType)) {
            LOGGER.warn("Type {} is not valid", dataGroupType);
            throw getBadRequestException(ERR_ACC_053.getErrorMessage(), ERR_ACC_053.getErrorCode());
        }
        return true;
    }
}