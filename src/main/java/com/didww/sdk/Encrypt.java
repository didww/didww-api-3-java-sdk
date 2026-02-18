package com.didww.sdk;

import com.didww.sdk.exception.DidwwClientException;
import com.didww.sdk.resource.PublicKey;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;
import javax.crypto.spec.SecretKeySpec;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.List;

public class Encrypt {

    private final DidwwClient client;
    private String[] publicKeys;
    private String fingerprint;

    public Encrypt(DidwwClient client) {
        this.client = client;
        reset();
    }

    public static byte[] encryptWithKeys(byte[] data, String[] publicKeys) {
        try {
            SecureRandom random = new SecureRandom();

            // Generate AES-256-CBC key (32 bytes) and IV (16 bytes)
            byte[] aesKey = new byte[32];
            random.nextBytes(aesKey);
            byte[] aesIv = new byte[16];
            random.nextBytes(aesIv);

            // Encrypt data with AES-256-CBC
            Cipher aesCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            aesCipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(aesKey, "AES"), new IvParameterSpec(aesIv));
            byte[] encryptedAes = aesCipher.doFinal(data);

            // Concatenate AES key + IV
            byte[] aesCredentials = new byte[aesKey.length + aesIv.length];
            System.arraycopy(aesKey, 0, aesCredentials, 0, aesKey.length);
            System.arraycopy(aesIv, 0, aesCredentials, aesKey.length, aesIv.length);

            // RSA-OAEP encrypt aesCredentials with each public key
            byte[] encryptedRsaA = encryptRsaOaep(publicKeys[0], aesCredentials);
            byte[] encryptedRsaB = encryptRsaOaep(publicKeys[1], aesCredentials);

            // Concatenate: rsa_a + rsa_b + aes_encrypted
            byte[] result = new byte[encryptedRsaA.length + encryptedRsaB.length + encryptedAes.length];
            System.arraycopy(encryptedRsaA, 0, result, 0, encryptedRsaA.length);
            System.arraycopy(encryptedRsaB, 0, result, encryptedRsaA.length, encryptedRsaB.length);
            System.arraycopy(encryptedAes, 0, result, encryptedRsaA.length + encryptedRsaB.length, encryptedAes.length);

            return result;
        } catch (Exception e) {
            throw new DidwwClientException("Encryption failed", e);
        }
    }

    public static String calculateFingerprint(String[] publicKeys) {
        return fingerprintFor(publicKeys[0]) + ":::" + fingerprintFor(publicKeys[1]);
    }

    public byte[] encrypt(byte[] data) {
        return encryptWithKeys(data, publicKeys);
    }

    public String[] getPublicKeys() {
        return publicKeys;
    }

    public String getFingerprint() {
        return fingerprint;
    }

    public void reset() {
        List<PublicKey> keys = client.publicKeys().list(null).getData();
        this.publicKeys = new String[]{
                keys.get(0).getKey(),
                keys.get(1).getKey()
        };
        this.fingerprint = calculateFingerprint(this.publicKeys);
    }

    private static String fingerprintFor(String publicKeyPem) {
        try {
            String base64 = normalizePublicKey(publicKeyPem);
            byte[] publicKeyBin = Base64.getDecoder().decode(base64);
            MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
            byte[] digest = sha1.digest(publicKeyBin);
            return bytesToHex(digest);
        } catch (Exception e) {
            throw new DidwwClientException("Failed to calculate fingerprint", e);
        }
    }

    private static byte[] encryptRsaOaep(String publicKeyPem, byte[] data) throws Exception {
        String base64 = normalizePublicKey(publicKeyPem);
        byte[] keyBytes = Base64.getDecoder().decode(base64);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        java.security.PublicKey rsaKey = KeyFactory.getInstance("RSA").generatePublic(keySpec);

        OAEPParameterSpec oaepSpec = new OAEPParameterSpec(
                "SHA-256",
                "MGF1",
                MGF1ParameterSpec.SHA256,
                PSource.PSpecified.DEFAULT
        );

        Cipher rsaCipher = Cipher.getInstance("RSA/ECB/OAEPPadding");
        rsaCipher.init(Cipher.ENCRYPT_MODE, rsaKey, oaepSpec);
        return rsaCipher.doFinal(data);
    }

    private static String normalizePublicKey(String publicKeyPem) {
        return publicKeyPem
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
