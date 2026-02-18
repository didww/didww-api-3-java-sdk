package com.didww.sdk;

import org.junit.jupiter.api.Test;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.security.spec.MGF1ParameterSpec;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

class EncryptTest {

    // Test RSA key pair (2048-bit) for unit testing
    private static final KeyPair KEY_PAIR_A;
    private static final KeyPair KEY_PAIR_B;

    static {
        try {
            KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
            gen.initialize(2048);
            KEY_PAIR_A = gen.generateKeyPair();
            KEY_PAIR_B = gen.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private static String toPem(PublicKey key) {
        String base64 = Base64.getMimeEncoder(64, "\n".getBytes()).encodeToString(key.getEncoded());
        return "-----BEGIN PUBLIC KEY-----\n" + base64 + "\n-----END PUBLIC KEY-----\n";
    }

    @Test
    void testEncryptWithKeysRoundTrip() throws Exception {
        String pemA = toPem(KEY_PAIR_A.getPublic());
        String pemB = toPem(KEY_PAIR_B.getPublic());
        String[] publicKeys = {pemA, pemB};

        byte[] plaintext = "Hello, DIDWW encryption!".getBytes();
        byte[] encrypted = Encrypt.encryptWithKeys(plaintext, publicKeys);

        // RSA-OAEP with 2048-bit key produces 256-byte output per key
        int rsaBlockSize = 256;
        assertThat(encrypted.length).isGreaterThan(rsaBlockSize * 2);

        // Extract the three parts
        byte[] encryptedRsaA = new byte[rsaBlockSize];
        byte[] encryptedRsaB = new byte[rsaBlockSize];
        byte[] encryptedAes = new byte[encrypted.length - rsaBlockSize * 2];

        System.arraycopy(encrypted, 0, encryptedRsaA, 0, rsaBlockSize);
        System.arraycopy(encrypted, rsaBlockSize, encryptedRsaB, 0, rsaBlockSize);
        System.arraycopy(encrypted, rsaBlockSize * 2, encryptedAes, 0, encryptedAes.length);

        // Decrypt AES credentials with private key A
        OAEPParameterSpec oaepSpec = new OAEPParameterSpec(
                "SHA-256", "MGF1", MGF1ParameterSpec.SHA256, PSource.PSpecified.DEFAULT);
        Cipher rsaCipher = Cipher.getInstance("RSA/ECB/OAEPPadding");
        rsaCipher.init(Cipher.DECRYPT_MODE, KEY_PAIR_A.getPrivate(), oaepSpec);
        byte[] aesCredentials = rsaCipher.doFinal(encryptedRsaA);

        // AES credentials = 32-byte key + 16-byte IV = 48 bytes
        assertThat(aesCredentials).hasSize(48);

        byte[] aesKey = new byte[32];
        byte[] aesIv = new byte[16];
        System.arraycopy(aesCredentials, 0, aesKey, 0, 32);
        System.arraycopy(aesCredentials, 32, aesIv, 0, 16);

        // Decrypt AES data
        Cipher aesCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        aesCipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(aesKey, "AES"), new IvParameterSpec(aesIv));
        byte[] decrypted = aesCipher.doFinal(encryptedAes);

        assertThat(decrypted).isEqualTo(plaintext);

        // Also verify key B has the same AES credentials
        rsaCipher.init(Cipher.DECRYPT_MODE, KEY_PAIR_B.getPrivate(), oaepSpec);
        byte[] aesCredentialsB = rsaCipher.doFinal(encryptedRsaB);
        assertThat(aesCredentialsB).isEqualTo(aesCredentials);
    }

    @Test
    void testCalculateFingerprint() {
        String pemA = toPem(KEY_PAIR_A.getPublic());
        String pemB = toPem(KEY_PAIR_B.getPublic());
        String[] publicKeys = {pemA, pemB};

        String fingerprint = Encrypt.calculateFingerprint(publicKeys);

        // Fingerprint format: hex_sha1_a:::hex_sha1_b
        assertThat(fingerprint).contains(":::");
        String[] parts = fingerprint.split(":::");
        assertThat(parts).hasSize(2);
        // Each SHA-1 hex digest is 40 characters
        assertThat(parts[0]).hasSize(40).matches("[0-9a-f]+");
        assertThat(parts[1]).hasSize(40).matches("[0-9a-f]+");
        // Two different keys should have different fingerprints
        assertThat(parts[0]).isNotEqualTo(parts[1]);
    }

    @Test
    void testFingerprintIsConsistent() {
        String pemA = toPem(KEY_PAIR_A.getPublic());
        String pemB = toPem(KEY_PAIR_B.getPublic());
        String[] publicKeys = {pemA, pemB};

        String fp1 = Encrypt.calculateFingerprint(publicKeys);
        String fp2 = Encrypt.calculateFingerprint(publicKeys);

        assertThat(fp1).isEqualTo(fp2);
    }

    @Test
    void testEncryptWithKeysProducesUniqueOutput() {
        String pemA = toPem(KEY_PAIR_A.getPublic());
        String pemB = toPem(KEY_PAIR_B.getPublic());
        String[] publicKeys = {pemA, pemB};

        byte[] plaintext = "Same input".getBytes();
        byte[] enc1 = Encrypt.encryptWithKeys(plaintext, publicKeys);
        byte[] enc2 = Encrypt.encryptWithKeys(plaintext, publicKeys);

        // Each encryption uses random AES key + IV, so outputs differ
        assertThat(enc1).isNotEqualTo(enc2);
    }
}
