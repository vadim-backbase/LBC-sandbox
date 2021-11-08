package com.backbase.accesscontrol.util.validation;

import static com.backbase.accesscontrol.util.ExceptionUtil.getNotFoundException;

import com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes;
import java.util.Arrays;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ValidatePermissionSetIdentifiers {

    private static final String[] IDENTIFIER_TYPES = new String[]{"id", "name"};
    private static final Logger LOGGER = LoggerFactory.getLogger(ValidatePermissionSetIdentifiers.class);

    /**
     * Validate identifier type  and if type is id identifier must be number.
     *
     * @param identifierType type of the identifier (id or name)
     * @param identifier     identifier value
     */
    public static void validateIdentifiers(String identifierType, String identifier) {

        LOGGER.info("Validate identifiers type{}, identifier{}", identifierType, identifier);
        if (Arrays.stream(IDENTIFIER_TYPES).noneMatch(identifierType::equals)) {
            LOGGER.warn("Identifier type not valid");
            throw getNotFoundException(
                CommandErrorCodes.ERR_ACC_089.getErrorMessage(),
                CommandErrorCodes.ERR_ACC_089.getErrorCode()
            );
        }

        if (identifierType.equals(IDENTIFIER_TYPES[0])) {
            try {
                Long.parseLong(identifier);
            } catch (NumberFormatException e) {
                LOGGER.warn("Provided ID is not numeric value");
                throw getNotFoundException(
                    CommandErrorCodes.ERR_ACC_089.getErrorMessage(),
                    CommandErrorCodes.ERR_ACC_089.getErrorCode()
                );
            }
        }
    }

    /**
     * Check if identifier type is id.
     *
     * @param identifierType - identifier type
     * @return true if type is id
     */
    public static boolean isIdIdentifier(String identifierType) {
        return identifierType.equals("id");
    }

}
