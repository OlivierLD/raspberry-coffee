package encryption;

/**
 * Histoire des codes secrets, pages 232 and after.
 * Brute force.
 */
public class Basics {
    // Suitable for lowercase, not accented characters, no punctuation, just blanks.
    private final static String NOT_ENCRYPTED = "abcdefghijklmnopqrstuvwxyz";
    private final static String ALICE_KEY     = "HFSUGTAKVDEOYJBPNXWCQRIMZL";
    private final static String BERNARD_KEY   = "CPMGATNOJEFWIQBURYHXSDZKLV";

    /**
     * Encrypt a message with a key.
     *
     * @param original Original message, in lowercase. No accent, no punctuation. Just blanks allowed.
     * @param key The encryption key
     * @return The encrypted message.
     */
    private static String encrypt(String original, String key) {
        StringBuilder encrypted = new StringBuilder();
        char[] charArray = original.toCharArray();
        for (char c : charArray) {
            boolean found = false;
            if (c == ' ') {
                encrypted.append(' ');
            } else {
                if (NOT_ENCRYPTED.contains(new StringBuffer(c))) {
                    int idx = 0;
                    while (!found) {
                        try {
                            if (NOT_ENCRYPTED.charAt(idx) == c) {
                                found = true;
                            } else {
                                idx++;
                            }
                        } catch (StringIndexOutOfBoundsException siobe) {
                            System.err.println("--- Cough that ---");
                            siobe.printStackTrace();
                            System.err.println("------------------");
                            break;
                        }
                    }
                    if (found) {
                        encrypted.append(key.charAt(idx));
                    }
                } else {
                    System.out.println("Oops...");
                }
            }
        }
        return encrypted.toString();
    }

    /**
     * Decrypt a message, with the key it has been encrypted with.
     * @param encrypted Encrypted message
     * @param key The key, used for encryption
     * @return The decrypted message
     */
    private static String decrypt(String encrypted, String key) {
        StringBuilder decrypted = new StringBuilder();
        char[] charArray = encrypted.toCharArray();
        for (char c : charArray) {
            boolean found = false;
            if (c == ' ') {
                decrypted.append(' ');
            } else {
                if (key.contains(new StringBuffer(c))) {
                    int idx = 0;
                    while (!found) {
                        if (key.charAt(idx) == c) {
                            found = true;
                        } else {
                            idx++;
                        }
                    }
                    decrypted.append(NOT_ENCRYPTED.charAt(idx));
                } else {
                    System.out.println("Oops...");
                }
            }
        }
        return decrypted.toString();
    }

    private static void checkTheKeys() {
        if (NOT_ENCRYPTED.length() != 26) {
            System.out.printf("Bad length for NOT_ENCRYPTED: %d\n", NOT_ENCRYPTED.length());
        }
        if (ALICE_KEY.length() != 26) {
            System.out.printf("Bad length for ALICE_KEY: %d\n", ALICE_KEY.length());
        }
        if (BERNARD_KEY.length() != 26) {
            System.out.printf("Bad length for BERNARD_KEY: %d\n", BERNARD_KEY.length());
        }
    }

    public static void main(String... args) {

        boolean checkKeys = false;
        for (String arg : args) {
            if ("--check-keys".equals(arg)) {
                checkKeys = true;
            }
        }
        if (checkKeys) {
            checkTheKeys();
        }

        String originalMessage = "vois moi a midi";
        String encrypted = encrypt(originalMessage, ALICE_KEY);

        System.out.printf("Encrypted with Alice's key [%s]\n", encrypted);

        String decrypted = decrypt(encrypted, ALICE_KEY);
        System.out.printf("[%s] => Back to original [%s]\n", originalMessage, decrypted);

        // --------------------
        originalMessage = "Vois moi à midi, Ducon !";

        // String normalized = StringUtils.stripAccents(originalMessage); // TODO Fix that...
        encrypted = encrypt(originalMessage, ALICE_KEY);

        System.out.println("-----------------");
        System.out.printf("Accented message: Encrypted with Alice's key [%s]\n", encrypted);

        System.out.println("----- Done! -----");
    }
}
