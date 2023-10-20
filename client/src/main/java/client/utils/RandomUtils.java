package client.utils;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;
import java.util.random.RandomGenerator;

/**
 * Utility class for generating random data using a secure random algorithm if available.
 * Otherwise, it falls back to using the default random number generator.
 */
@SuppressWarnings({"checkstyle:NoWhitespaceBefore", "reused"})
public enum RandomUtils {
    ;
    public static final RandomGenerator RAND;

    static {
        RandomGenerator randVerify;
        try {
            randVerify = SecureRandom.getInstanceStrong();
        } catch (final NoSuchAlgorithmException ignored) {
            randVerify = new Random();
        }
        RAND = randVerify;
    }
}
