package com.backbase.legalentity.integration.external.mock.util;

import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ParameterValidationUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(ParameterValidationUtil.class);

    /**
     * Validate "from" and "size" parameters.
     *
     * @param size - size parameter
     * @param from - from parameter
     */
    public void validateFromAndSizeParameter(Integer from, Integer size) {
        LOGGER.info("validate from {} and size parameter {}", from, size);
        boolean notNullFromAndSize = !(from == null && size == null);
        boolean isFromNegative = from != null && from < 0;
        boolean isSizeNegative = size != null && size <= 0;
        boolean nullFromOrSize = from == null || size == null;

        if (notNullFromAndSize) {
            if (isFromNegative) {
                LOGGER.warn("validation failed for from parameter {}", from);
                throw new BadRequestException()
                    .withMessage("Invalid from parameter");
            }
            if (isSizeNegative) {
                LOGGER.warn("validation failed for size parameter {}", size);
                throw new BadRequestException()
                    .withMessage("Invalid size parameter");
            }
            if (nullFromOrSize) {
                LOGGER.warn("Invalid parameters from {} or size {}", from, size);
                throw new BadRequestException()
                    .withMessage("Invalid query parameters");
            }
        }
    }
}
