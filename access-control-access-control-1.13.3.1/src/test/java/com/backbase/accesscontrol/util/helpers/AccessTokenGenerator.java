package com.backbase.accesscontrol.util.helpers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AccessTokenGenerator {


    private String key;
    private String initPhrase;
    private long expiration;

    public AccessTokenGenerator(
        @Value("${backbase.accesscontrol.token.key:empty}") String key,
        @Value("${backbase.accesscontrol.token.initPhrase:empty}") String initPhrase,
        @Value("${backbase.accesscontrol.token.expiration:empty}") String expiration
    ) {
        this.initPhrase = initPhrase;
        this.key = key;
        this.expiration = Long.parseLong(expiration);
    }

    private String encrypt(long time) {
        return AesEncrypt.encrypt(key, initPhrase, Long.toString(time));
    }

    public String generateValidToken() {
        return encrypt(System.currentTimeMillis());
    }

    public String generateExpiredToken() {
        return encrypt(System.currentTimeMillis() - (expiration + 100) * 1000);
    }


    public String generateInFutureToken() {
        return encrypt(System.currentTimeMillis() + (expiration + 100) * 1000);
    }
}
