package com.backbase.accesscontrol.configuration;

import com.backbase.buildingblocks.jwt.core.JsonWebTokenConsumerType;
import com.backbase.buildingblocks.jwt.core.JsonWebTokenProducerType;
import com.backbase.buildingblocks.jwt.core.exception.JsonWebTokenException;
import com.backbase.buildingblocks.jwt.core.properties.JsonWebTokenProperties;
import com.backbase.buildingblocks.jwt.core.properties.TokenKeyType;
import com.backbase.buildingblocks.jwt.core.token.JsonWebTokenClaimsSet;
import com.backbase.buildingblocks.jwt.core.type.JsonWebTokenTypeFactory;
import com.backbase.buildingblocks.jwt.internal.InternalJwtConsumer;
import com.backbase.buildingblocks.jwt.internal.impl.InternalJwtConsumerImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jmx.access.InvalidInvocationException;
import org.springframework.stereotype.Component;


@Component
public class UserContextConfiguration {

    private JsonWebTokenProperties properties;

    private JsonWebTokenProducerType<JsonWebTokenClaimsSet, String> producerType;

    private InternalJwtConsumer consumer;

    /**
     * Constructor for {@link UserContextConfiguration} class.
     *
     * @param signatureAndEncryptionKey     signature and encryption key
     * @param signatureAndEncryptionKeyType signature and ecryption key type
     */
    public UserContextConfiguration(
        @Value("${backbase.usercontext.signatureAndEncryption.key.value}") String signatureAndEncryptionKey,
        @Value("${backbase.usercontext.signatureAndEncryption.key.type}") String signatureAndEncryptionKeyType
    ) {
        properties = getJsonWebTokenProperties(signatureAndEncryptionKey, signatureAndEncryptionKeyType);
        producerType = createTokenProducerType();
        consumer = createJwtConsumer();
    }


    private JsonWebTokenProperties getJsonWebTokenProperties(String userContextSignatureAndEncryptionKey,
        String signatureAndEncryptionKeyType) {
        JsonWebTokenProperties jsonProperties = new JsonWebTokenEncryptedProperties(
            JsonWebTokenTypeFactory.SIGNED_AND_ENCRYPTED_TOKEN_TYPE);
        jsonProperties.getSignature().getKey().setType(TokenKeyType.valueOf(signatureAndEncryptionKeyType));
        jsonProperties.getSignature().getKey().setValue(userContextSignatureAndEncryptionKey);
        jsonProperties.getEncryption().getKey().setType(TokenKeyType.valueOf(signatureAndEncryptionKeyType));
        jsonProperties.getEncryption().getKey().setValue(userContextSignatureAndEncryptionKey);
        return jsonProperties;
    }


    private JsonWebTokenProducerType<JsonWebTokenClaimsSet, String> createTokenProducerType() {
        try {
            return JsonWebTokenTypeFactory.getProducer(properties);
        } catch (JsonWebTokenException e) {
            throw new InvalidInvocationException("JsonWebTokenProducerType initialization process failed." + e);
        }
    }


    private InternalJwtConsumer createJwtConsumer() {
        JsonWebTokenConsumerType<JsonWebTokenClaimsSet, String> consumerType;
        try {
            consumerType = JsonWebTokenTypeFactory.getConsumer(properties);
        } catch (JsonWebTokenException e) {
            throw new InvalidInvocationException("JsonWebTokenConsumerType initialization process failed." + e);
        }
        return new InternalJwtConsumerImpl(consumerType);
    }

    /**
     * Return an object of {@link JsonWebTokenProducerType}.
     *
     * @return {@link JsonWebTokenProducerType}
     */
    public JsonWebTokenProducerType<JsonWebTokenClaimsSet, String> getJsonWebTokenProducerType() {
        return producerType;
    }

    /**
     * Return an object of {@link InternalJwtConsumer}.
     *
     * @return {@link InternalJwtConsumer}
     */
    public InternalJwtConsumer getInternalJwtConsumer() {
        return consumer;
    }

    private class JsonWebTokenEncryptedProperties extends JsonWebTokenProperties {

        public JsonWebTokenEncryptedProperties(String type) {
            this.setType(type);
        }
    }


}
