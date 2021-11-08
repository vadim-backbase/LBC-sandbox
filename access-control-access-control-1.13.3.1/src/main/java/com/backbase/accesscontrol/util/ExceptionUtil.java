package com.backbase.accesscontrol.util;

import static java.util.Arrays.asList;

import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.buildingblocks.presentation.errors.Error;
import com.backbase.buildingblocks.presentation.errors.ForbiddenException;
import com.backbase.buildingblocks.presentation.errors.InternalServerErrorException;
import com.backbase.buildingblocks.presentation.errors.NotFoundException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ExceptionUtil {

    public static final String BAD_REQUEST_MESSAGE = "Bad Request";
    public static final String FORBIDDEN_MESSAGE = "Forbidden";
    public static final String NOT_FOUND_MESSAGE = "Not Found";

    /**
     * Creates and return {@link BadRequestException}.
     *
     * @param errorMessage - message
     * @param errorCode    - code
     * @return {@link BadRequestException}
     */
    public static BadRequestException getBadRequestException(String errorMessage, String errorCode) {
        return new BadRequestException()
            .withMessage(BAD_REQUEST_MESSAGE)
            .withErrors(asList(new Error()
                .withMessage(errorMessage)
                .withKey(errorCode)));
    }

    /**
     * Creates and return {@link NotFoundException}.
     *
     * @param errorMessage - message
     * @param errorCode    - code
     * @return {@link NotFoundException}
     */
    public static NotFoundException getNotFoundException(String errorMessage, String errorCode) {
        return new NotFoundException()
            .withMessage(NOT_FOUND_MESSAGE)
            .withErrors(asList(new Error()
                .withMessage(errorMessage)
                .withKey(errorCode)));
    }

    /**
     * Creates and return {@link ForbiddenException}.
     *
     * @param errorMessage - message
     * @param errorCode    - code
     * @return {@link ForbiddenException}
     */
    public static ForbiddenException getForbiddenException(String errorMessage, String errorCode) {
        return new ForbiddenException()
            .withMessage(FORBIDDEN_MESSAGE)
            .withErrors(asList(new Error()
                .withMessage(errorMessage)
                .withKey(errorCode)));
    }

    /**
     * Creates and return {@link BadRequestException}.
     *
     * @param errorMessage - message
     * @return {@link InternalServerErrorException}
     */
    public static InternalServerErrorException getInternalServerErrorException(String errorMessage) {
        return new InternalServerErrorException()
            .withMessage(errorMessage);
    }
}