package com.backbase.accesscontrol.util;

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
 * Decrypt functionality.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AesDecrypt {

    public static final String AES_CBC_PKCS_5_PADDING = "AES/CBC/PKCS5PADDING";

    /**
     * Decrypt value with given key and initVector.
     *
     * @param key        key
     * @param initVector init vector
     * @param encrypted  encrypted value
     * @return decrypted value
     */
    public static String decrypt(String key, String initVector, String encrypted) {

        try {
            IvParameterSpec iv = new IvParameterSpec(initVector.getBytes(StandardCharsets.UTF_8));
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "AES");

            @SuppressWarnings("squid:S5542")
            Cipher cipher = Cipher.getInstance(AES_CBC_PKCS_5_PADDING);
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, iv);

            UUID uuid = UUID.fromString(encrypted);
            byte[] result = asBytes(uuid);
            byte[] original = cipher.doFinal(result);

            return new String(original);
        } catch (NoSuchAlgorithmException
            | NoSuchPaddingException
            | InvalidKeyException
            | InvalidAlgorithmParameterException
            | IllegalBlockSizeException
            | BadPaddingException e) {
            return null;

        }
    }

    private static byte[] asBytes(UUID uuid) {
        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());
        return bb.array();
    }

}
