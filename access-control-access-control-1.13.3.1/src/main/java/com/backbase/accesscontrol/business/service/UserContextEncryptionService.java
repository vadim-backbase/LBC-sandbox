package com.backbase.accesscontrol.business.service;

import com.backbase.accesscontrol.configuration.UserContextConfiguration;
import com.backbase.accesscontrol.dto.UserContextDto;
import com.backbase.buildingblocks.jwt.core.JsonWebTokenProducerType;
import com.backbase.buildingblocks.jwt.core.exception.JsonWebTokenException;
import com.backbase.buildingblocks.jwt.core.token.JsonWebTokenClaimsSet;
import com.backbase.buildingblocks.jwt.internal.InternalJwtConsumer;
import com.backbase.buildingblocks.jwt.internal.exception.InternalJwtException;
import com.backbase.buildingblocks.jwt.internal.token.InternalJwt;
import com.backbase.buildingblocks.jwt.internal.token.InternalJwtClaimsSet;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang.time.DateUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * This class provides methods to encrypt and decrypt the data includes in the class {@link UserContextDto}.
 */
@Service
public class UserContextEncryptionService {

    private static final String USER_CONTEXT_SERVICE_AGREEMENT_CLAIM_KEY = "said";
    private static final String USER_CONTEXT_EXPIRE_CLAIM_KEY = "exp";
    private static final String USER_CONTEXT_EXTERNAL_USER_ID_CLAIM_KEY = "sub";

    private Integer userContextExpiration;

    private UserContextConfiguration userContextConfiguration;

    /**
     * Constructor.
     *
     * @param userContextExpiration    user token expiration value
     * @param userContextConfiguration user context configuration
     */
    public UserContextEncryptionService(
        @Value("${backbase.usercontext.jwtTokenExpiration}") Integer userContextExpiration,
        UserContextConfiguration userContextConfiguration) {
        this.userContextExpiration = userContextExpiration;
        this.userContextConfiguration = userContextConfiguration;
    }

    private InternalJwtConsumer getConsumer() {
        return userContextConfiguration.getInternalJwtConsumer();
    }

    private JsonWebTokenProducerType<JsonWebTokenClaimsSet, String> getProducer() {
        return userContextConfiguration.getJsonWebTokenProducerType();
    }

    /**
     * Encryption.
     *
     * @param userContextEncryptionDto user context to be encrypted
     * @return encrypted token
     * @throws JsonWebTokenException if fail to encrypt
     */
    public String encryptUserContextToJwt(UserContextDto userContextEncryptionDto) throws JsonWebTokenException {
        JsonWebTokenClaimsSet claims = getClaimsToEncrypt(userContextEncryptionDto);
        return getProducer().createToken(claims);
    }

    private JsonWebTokenClaimsSet getClaimsToEncrypt(UserContextDto userContextEncryptionDto) {
        Map<String, Object> claimsMap = new HashMap<>();
        claimsMap.put(USER_CONTEXT_SERVICE_AGREEMENT_CLAIM_KEY, userContextEncryptionDto.getServiceAgreementId());
        claimsMap.put(USER_CONTEXT_EXTERNAL_USER_ID_CLAIM_KEY, userContextEncryptionDto.getExternalUserId());
        claimsMap.put(USER_CONTEXT_EXPIRE_CLAIM_KEY, getSecondsExpirationDataTime());
        return new InternalJwtClaimsSet(claimsMap);
    }

    private long getSecondsExpirationDataTime() {
        Date expDate = DateUtils.addSeconds(new Date(), userContextExpiration);
        return ZonedDateTime.ofInstant(expDate.toInstant(), ZoneId.systemDefault()).toEpochSecond();
    }

    /**
     * Decryption.
     *
     * @param token value to decrypt
     * @return {@link InternalJwt} object
     * @throws InternalJwtException  if fail to decrypt
     * @throws JsonWebTokenException if fail to decrypt
     */
    public InternalJwt decryptUserContextToken(String token) throws InternalJwtException, JsonWebTokenException {
        return getConsumer().parseToken(token);
    }
}
