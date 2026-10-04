package ai.docucraft.mobile;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import java.nio.charset.StandardCharsets;
import java.security.KeyStore;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public final class ApiKeyStore {
    private static final String PREFS = "docucraft_secure";
    private static final String KEY_NAME = "DocuCraftGroqKey";
    private static final String VALUE_NAME = "groq_api_key";

    private ApiKeyStore() {}

    public static void save(Context context, String apiKey) throws Exception {
        SecretKey key = getOrCreateKey();
        byte[] iv = new byte[12];
        new java.security.SecureRandom().nextBytes(iv);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(128, iv));
        byte[] encrypted = cipher.doFinal(apiKey.getBytes(StandardCharsets.UTF_8));

        String value = Base64.encodeToString(iv, Base64.NO_WRAP) + "." +
                Base64.encodeToString(encrypted, Base64.NO_WRAP);
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit().putString(VALUE_NAME, value).apply();
    }

    public static String load(Context context) {
        try {
            String value = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                    .getString(VALUE_NAME, null);
            if (value == null || !value.contains(".")) return null;

            String[] parts = value.split("\.", 2);
            byte[] iv = Base64.decode(parts[0], Base64.NO_WRAP);
            byte[] encrypted = Base64.decode(parts[1], Base64.NO_WRAP);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, getOrCreateKey(),
                    new GCMParameterSpec(128, iv));
            return new String(cipher.doFinal(encrypted), StandardCharsets.UTF_8);
        } catch (Exception e) {
            return null;
        }
    }

    public static void clear(Context context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit().remove(VALUE_NAME).apply();
    }

    private static SecretKey getOrCreateKey() throws Exception {
        KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
        keyStore.load(null);

        if (keyStore.containsAlias(KEY_NAME)) {
            return ((KeyStore.SecretKeyEntry) keyStore.getEntry(KEY_NAME, null)).getSecretKey();
        }

        KeyGenerator generator = KeyGenerator.getInstance(
                android.security.keystore.KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
        generator.init(new android.security.keystore.KeyGenParameterSpec.Builder(
                KEY_NAME,
                android.security.keystore.KeyProperties.PURPOSE_ENCRYPT |
                        android.security.keystore.KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(android.security.keystore.KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(android.security.keystore.KeyProperties.ENCRYPTION_PADDING_NONE)
                .build());
        return generator.generateKey();
    }
}
