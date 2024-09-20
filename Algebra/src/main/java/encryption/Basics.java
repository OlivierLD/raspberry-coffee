package encryption;

public class Basics {

    /**
     * Histoire des codes secrets, pages 232 and after.
     * Brute force.
     */
    // Suitable for lowercase, not accented characters, no punctuation, just blanks.
    private final static String NOT_ENCRYPTED = "abcdefghijklmnopqrstuvwxyz";
    private final static String ALICE_KEY     = "HFSUGTAKVDEOYJBPNXWCQRIMZL";
    private final static String BERNARD_KEY   = "CPMGATNOJEFWIQBURYHXSDZKLV";

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
                        if (NOT_ENCRYPTED.charAt(idx) == c) {
                            found = true;
                        } else {
                            idx++;
                        }
                    }
                    encrypted.append(key.charAt(idx));
                } else {
                    System.out.println("Oops...");
                }
            }
        }
        return encrypted.toString();
    }

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
    public static void main(String... args) {
        if (NOT_ENCRYPTED.length() != 26) {
            System.out.printf("Bad length for NOT_ENCRYPTED: %d\n", NOT_ENCRYPTED.length());
        }
        if (ALICE_KEY.length() != 26) {
            System.out.printf("Bad length for ALICE_KEY: %d\n", ALICE_KEY.length());
        }
        if (BERNARD_KEY.length() != 26) {
            System.out.printf("Bad length for BERNARD_KEY: %d\n", BERNARD_KEY.length());
        }

        String originalMessage = "vois moi a midi";
        String encrypted = encrypt(originalMessage, ALICE_KEY);
        System.out.printf("Encrypted with Alice's key [%s]\n", encrypted);
        String decrypted = decrypt(encrypted, ALICE_KEY);
        System.out.printf("Back to original [%s]\n", decrypted);
    }
}
