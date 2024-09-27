package encryption;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

/**
 * Histoire des codes secrets, pages 232 and after.
 * Brute force.
 */
public class Basics {
    private final static String NOT_ENCRYPTED = "abcdefghijklmnopqrstuvwxyz";

    public enum Key {
       ALICE_KEY("HFSUGTAKVDEOYJBPNXWCQRIMZL", "Alice"),
       BERNARD_KEY("CPMGATNOJEFWIQBURYHXSDZKLV", "Bernard");

       private final String key;
       private final String name;
       Key(String key, String name) {
        this.key = key;
        this.name = name;
       }
       public String getKey() {
           return this.key;
       }
       public String getName() {
        return this.name;
       }
    }

    /* Punctuation characters, unchanged here. */
    private final static Character[] UNCHANGED = new Character[] {
            ' ', ',', ';' , ':', '!', '?', '\'', '.'
    };

    public static String lpad(String s, int len) {
        return lpad(s, len, " ");
    }

    public static String lpad(String s, int len, String pad) {
        String str;
        for(str = s; str.length() < len; str = pad + str) {
        }

        return str;
    }

    /**
     * Encrypt a message with a key.
     *
     * @param original Original message, in lowercase. No accent, no punctuation. Also see UNCHANGED characters.
     * @param key The encryption key to use
     * @return The encrypted message.
     */
    private static String encrypt(String original, Key key) {
        StringBuilder encrypted = new StringBuilder();
        char[] charArray = original.toCharArray();
        for (char c : charArray) {
            boolean found = false;
            if (Arrays.stream(UNCHANGED).anyMatch(character -> (character == c))) {
                encrypted.append(c);
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
                            System.err.printf("--- Cough that for [%s] ---\n", c);
                            siobe.printStackTrace();
                            System.err.println("------------------");
                            break;
                        }
                    }
                    if (found) {
                        encrypted.append(key.getKey().charAt(idx));
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
    private static String decrypt(String encrypted, Key key) {
        StringBuilder decrypted = new StringBuilder();
        char[] charArray = encrypted.toCharArray();
        for (char c : charArray) {
            boolean found = false;
            if (Arrays.stream(UNCHANGED).anyMatch(character -> (character == c))) {
                decrypted.append(c);
            } else {
                if (key.getKey().contains(new StringBuffer(c))) {
                    int idx = 0;
                    while (!found) {
                        if (key.getKey().charAt(idx) == c) {
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
        if (Key.ALICE_KEY.getKey().length() != 26) {
            System.out.printf("Bad length for ALICE_KEY: %d\n", Key.ALICE_KEY.getKey().length());
        }
        if (Key.BERNARD_KEY.getKey().length() != 26) {
            System.out.printf("Bad length for BERNARD_KEY: %d\n", Key.BERNARD_KEY.getKey().length());
        }
    }

    /**
     * One complete cycle, encryption decryption
     * @param original Original message
     * @param key The key to use
     */
    private static void oneCycle(String original, Key key) {
        String encrypted = encrypt(original.toLowerCase(), key);

        System.out.printf("Encrypted with %s's key [%s]\n", key.getName(), encrypted);

        String decrypted = decrypt(encrypted, key);
        System.out.printf("[%s] => Back to original [%s]\n", original, decrypted);
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
        oneCycle(originalMessage, Key.ALICE_KEY);
        System.out.println("-----------------");

        originalMessage = "Vois moi à midi, Ducon !";
        System.out.printf("Managing [%s]...\n", originalMessage);

        String normalized = StringUtils.stripAccents(originalMessage); // Needs its maven repo
        normalized = normalized.toLowerCase();
        oneCycle(normalized, Key.ALICE_KEY);
        System.out.println("-----------------");
        originalMessage = "The quick brown fox jumps over the lazy dog.";
        oneCycle(originalMessage, Key.ALICE_KEY);
        System.out.println("-----------------");
        originalMessage = "C'est chez le vieux forgeron que j'ai bu le meilleur whisky.";
        oneCycle(originalMessage, Key.ALICE_KEY);
        oneCycle(originalMessage, Key.BERNARD_KEY);
        System.out.println("-----------------");

        System.out.println("----- Done! -----");
        /*
         Step 2

        - Alice defines message
        - Encrypt with Alice's key
        - Send encrypted message to Bernard
        - Bernard encrypts the encrypted message (2 layers)
        - Sends to Alice
        - Alice decrypts, sends decrypted message to Bernard
        - Bernard decrypts
         */

        originalMessage = "akeu coucou";
        String encrypted01 = encrypt(originalMessage.toLowerCase(), Key.ALICE_KEY);
        // => Bernard
        System.out.printf("Sent to Bernard: [%s]\n", encrypted01);
        String encrypted02 = encrypt(encrypted01.toLowerCase(), Key.BERNARD_KEY);
        // => Alice
        System.out.printf("Send back to Alice: [%s]\n", encrypted02);
        String decrypted01 = decrypt(encrypted02, Key.ALICE_KEY);
        // => Bernard
        System.out.printf("Decrypted by Alice: [%s]\n", decrypted01);
        String decrypted02 = decrypt(decrypted01.toUpperCase(), Key.BERNARD_KEY);

        System.out.printf("Finally: [%s], wrong as expected.\n", decrypted02);

        // Display characters as binary number
        byte letterA = (byte)'A';
        String binaryString = Integer.toBinaryString(letterA);
        System.out.printf("%s in binary: &#%s\n", letterA, lpad(binaryString, 8, "0"));

        String hello = "HELLO"; // Page 309
        byte[] helloBytes = hello.getBytes();
        StringBuilder binaryHello = new StringBuilder();
        for (byte b : helloBytes) {
            binaryHello.append(String.format("%s ", lpad(Integer.toBinaryString(b), 8, "0")));
        }
        System.out.printf("Binary Hello (lpad) : [%s]\n", binaryHello.toString().trim());

        binaryHello = new StringBuilder();
        for (byte b : helloBytes) {
            binaryHello.append(String.format("%s ", Integer.toBinaryString(b)));
        }
        System.out.printf("Binary Hello (NO lpad, as used in the book) : [%s]\n", binaryHello.toString().trim());
    }
}
