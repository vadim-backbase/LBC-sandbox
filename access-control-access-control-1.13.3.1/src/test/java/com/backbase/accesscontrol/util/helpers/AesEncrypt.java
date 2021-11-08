package com.backbase.accesscontrol.util.helpers;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Encrypt functionality.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AesEncrypt {

    public static final String AES_CBC_PKCS_5_PADDING = "AES/CBC/PKCS5PADDING";

    /**
     * Encrypt value with given key  and initVector.
     *
     * @return uuid representation
     */
    public static String encrypt(String key, String initVector, String value) {
        try {

            IvParameterSpec iv = new IvParameterSpec(initVector.getBytes(StandardCharsets.UTF_8));
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "AES");

            Cipher cipher = Cipher.getInstance(AES_CBC_PKCS_5_PADDING);
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, iv);

            byte[] encrypted = cipher.doFinal(value.getBytes());

            UUID uuid = asUuid(encrypted);

            return uuid.toString();

        } catch (NoSuchAlgorithmException
            | NoSuchPaddingException
            | InvalidKeyException
            | InvalidAlgorithmParameterException
            | IllegalBlockSizeException
            | BadPaddingException e) {
            return null;
        }
    }

    private static UUID asUuid(byte[] bytes) {
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        long firstLong = bb.getLong();
        long secondLong = bb.getLong();
        return new UUID(firstLong, secondLong);
    }

}
