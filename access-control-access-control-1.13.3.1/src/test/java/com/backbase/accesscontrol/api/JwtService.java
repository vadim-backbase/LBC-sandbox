package com.backbase.accesscontrol.api;

import com.backbase.accesscontrol.configuration.UserContextConfiguration;
import com.backbase.buildingblocks.jwt.core.JsonWebTokenConsumerType;
import com.backbase.buildingblocks.jwt.core.JsonWebTokenProducerType;
import com.backbase.buildingblocks.jwt.core.exception.JsonWebTokenException;
import com.backbase.buildingblocks.jwt.core.properties.JsonWebTokenProperties;
import com.backbase.buildingblocks.jwt.core.properties.TokenKeyType;
import com.backbase.buildingblocks.jwt.core.type.JsonWebTokenTypeFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jmx.access.InvalidInvocationException;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private JsonWebTokenProperties properties;

    private JsonWebTokenConsumerType consumerType;

    /**
     * Constructor for {@link UserContextConfiguration} class.
     *
     * @param signatureAndEncryptionKey     signature and encryption key
     * @param signatureAndEncryptionKeyType signature and ecryption key type
     */
    public JwtService(
        @Value("${backbase.usercontext.signatureAndEncryption.key.value}") String signatureAndEncryptionKey,
        @Value("${backbase.usercontext.signatureAndEncryption.key.type}") String signatureAndEncryptionKeyType
    ) {
        System.setProperty("SIG_SECRET_KEY", "JWTSecretKeyDontUseInProduction!");
        System.setProperty("USERCTX_KEY", "JWTSecretKeyDontUseInProduction!");
        properties = getJsonWebTokenProperties(signatureAndEncryptionKey, signatureAndEncryptionKeyType);
        consumerType = createTokenConsumerType();
    }


    private JsonWebTokenProperties getJsonWebTokenProperties(String userContextSignatureAndEncryptionKey,
        String signatureAndEncryptionKeyType) {
        JsonWebTokenProperties jsonProperties = new JwtService.JsonWebTokenEncryptedProperties(
            JsonWebTokenTypeFactory.SIGNED_AND_ENCRYPTED_TOKEN_TYPE);
        jsonProperties.getSignature().getKey().setType(TokenKeyType.valueOf(signatureAndEncryptionKeyType));
        jsonProperties.getSignature().getKey().setValue(userContextSignatureAndEncryptionKey);
        jsonProperties.getEncryption().getKey().setType(TokenKeyType.valueOf(signatureAndEncryptionKeyType));
        jsonProperties.getEncryption().getKey().setValue(userContextSignatureAndEncryptionKey);
        return jsonProperties;
    }


    private JsonWebTokenConsumerType createTokenConsumerType() {
        try {
            return JsonWebTokenTypeFactory.getConsumer(properties);
        } catch (JsonWebTokenException e) {
            throw new InvalidInvocationException("JsonWebTokenProducerType initialization process failed." + e);
        }
    }

    /**
     * Return an object of {@link JsonWebTokenProducerType}.
     *
     * @return {@link JsonWebTokenProducerType}
     */
    public JsonWebTokenConsumerType getJsonWebTokenConsumerType() {
        return consumerType;
    }

    private class JsonWebTokenEncryptedProperties extends JsonWebTokenProperties {

        public JsonWebTokenEncryptedProperties(String type) {
            this.setType(type);
        }
    }
}
