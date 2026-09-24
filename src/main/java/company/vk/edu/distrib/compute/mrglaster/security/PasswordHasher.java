package company.vk.edu.distrib.compute.mrglaster.security;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;


public class PasswordHasher {
    // Тут бы Argon2id использовать, но пока работаем с тем, что есть
    // https://cheatsheetseries.owasp.org/cheatsheets/Password_Storage_Cheat_Sheet.html#pbkdf2
    private static final int ITERATIONS = 6; // Рекомендовано 600_000, но тогда мы по таймауту в тестах вываливаемся
    private static final int KEY_LENGTH = 256;
    private static final int SALT_LENGTH = 16;
    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";


    public static String hash(String password) {
        byte[] salt = new byte[SALT_LENGTH];
        new SecureRandom().nextBytes(salt);

        byte[] hash = pbkdf2(password, salt, ITERATIONS);

        return ITERATIONS + ":" +
                Base64.getEncoder().encodeToString(salt) + ":" +
                Base64.getEncoder().encodeToString(hash);
    }

    public static boolean verify(String password, String storedHashString) {
        if (password == null || storedHashString == null) {
            return false;
        }

        String[] parts = storedHashString.split(":");
        if (parts.length != 3) {
            return false;
        }

        try {
            int iterations = Integer.parseInt(parts[0]);
            byte[] salt = Base64.getDecoder().decode(parts[1]);
            byte[] originalHash = Base64.getDecoder().decode(parts[2]);

            byte[] testHash = pbkdf2(password, salt, iterations);

            return MessageDigest.isEqual(originalHash, testHash);

        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static byte[] pbkdf2(String password, byte[] salt, int iterations) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, KEY_LENGTH);
            SecretKeyFactory skf = SecretKeyFactory.getInstance(ALGORITHM);
            return skf.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException("Password hashing error", e);
        }
    }
}