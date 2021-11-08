package com.backbase.accesscontrol.util.validation;

import static com.backbase.accesscontrol.util.ExceptionUtil.getBadRequestException;
import static java.util.Objects.isNull;

import com.backbase.accesscontrol.util.AesDecrypt;
import com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AccessToken {

    private static final Logger LOGGER = LoggerFactory.getLogger(AccessToken.class);

    private Long validity;
    private String key;
    private String initPhrase;

    /**
     * Create instance for access token validation.
     *
     * @param validity   - time in seconds that define validity period of the token
     * @param key        - configured key
     * @param initPhrase - configured initPhrase
     */
    public AccessToken(
        @Value("${backbase.accesscontrol.token.expiration:empty}") String validity,
        @Value("${backbase.accesscontrol.token.key:empty}") String key,
        @Value("${backbase.accesscontrol.token.initPhrase:empty}") String initPhrase) {
        if (!validity.equals("empty")) {
            this.validity = Long.parseLong(validity) * 1000;
        }
        this.initPhrase = initPhrase;
        this.key = key;
    }

    /**
     * Check validity of the access token, according to configured variable.
     *
     * @param token  - string generated from access group endpoint
     * @param object - object
     */
    public void validateAccessToken(String token, Object object) {

        validateKeyAndPhrase(object);

        validateTokenFormat(token, object);

        String timeStr = AesDecrypt.decrypt(key, initPhrase, token);

        if (isNull(timeStr)) {
            LOGGER.warn("Decryption error not valid token {}", object);
            throwError();
        }

        long time = -1;

        try {
            time = Long.parseLong(timeStr);
        } catch (NumberFormatException e) {
            LOGGER.warn("Decryption error not valid token {} {}", object, timeStr);
            throwError();
        }

        if (isNull(validity) || System.currentTimeMillis() >= time + validity || time > System.currentTimeMillis()) {
            LOGGER.warn("Access token expired {}", object);
            throwError();
        }
    }

    private void validateTokenFormat(String token, Object object) {
        if (isNull(token)) {
            LOGGER.warn("Decryption error token {}", object);
            throwError();
        }

        try {
            UUID.fromString(token);
        } catch (IllegalArgumentException exception) {
            LOGGER.warn("Decryption error not valid UUID {}", object);
            throwError();
        }
    }

    private void validateKeyAndPhrase(Object object) {
        if (Objects.isNull(key)
            || key.getBytes(StandardCharsets.UTF_8).length != 16
            || Objects.isNull(initPhrase)
            || initPhrase.getBytes(StandardCharsets.UTF_8).length != 16) {
            LOGGER.error("Configuration error {}", object);
            throwError();
        }
    }

    private void throwError() {
        throw getBadRequestException(
            QueryErrorCodes.ERR_ACQ_054.getErrorMessage(),
            QueryErrorCodes.ERR_ACQ_054.getErrorCode()
        );
    }


}
