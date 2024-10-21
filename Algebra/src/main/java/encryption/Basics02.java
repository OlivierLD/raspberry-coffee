package encryption;

import primes.PrimeNumbers;
import primes.Primes;

import java.io.*;
import java.math.BigInteger;
import java.text.NumberFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static primes.PGCD.pgcd;
import static primes.Primes.primeFactors;
import static primes.Primes.spitMapOut;

/**
 * Based on the book "The Code Book", by Simon Singh, 1999, Fourth Estate Limited.
 * French edition "Histoire des codes secrets" (Le Livre de Poche), pages 232 and after.
 *
 * ...and more.
 * Mostly used as a playground for the related Jupyter notebooks
 *
 * RSA basics, and beyond.
 */
public class Basics02 {

    /**
     * This is a "one way function" (fonction a sens unique)
     * This is a POC. Not to use in the real world.
     *
     * @param base
     * @param mod
     * @param x
     * @return
     */
    public static long powXmodY(long base, long mod, long x) {
        return (long)(Math.pow(base, x) % mod);
    }

    /**
     * Check if
     * (e * D) % ((p - 1) * (q - 1)) = 1
     *              => (e * D) % phi = 1
     * where D is the private key
     *
     * phi & D are coprimes
     *
     * @param p
     * @param q
     * @param e
     * @return true or false
     */
    private static boolean checkPQE(long p, long q, long e) {
        long D = findD(p, q, e);
        long phi = (p - 1) * (q - 1);
        return ((e * D) % phi) == 1;
    }

    private static boolean checkPhiDCoprime(long p, long q, long e) {
        long D = findD(p, q, e);
        long phi = (p - 1) * (q - 1);

        long pgcdPhiD = pgcd((int)D, (int)phi);
        if (pgcdPhiD != 1) {
            System.out.printf("PGCD D, \u03D5 (%d, %d) : %d => %s\n", D, phi, pgcdPhiD, (pgcdPhiD == 1) ? "good" :
                    String.format("not good, \u03D5 => %s, D => %s", spitMapOut((int)phi, primeFactors((int)phi)), spitMapOut((int)D, primeFactors((int)D))));
        }
        return (pgcdPhiD == 1);
    }

    /**
     * Calculate the Private Key
     *
     * See https://www.geeksforgeeks.org/how-to-solve-rsa-algorithm-problems/#
     *
     * @param p P
     * @param q Q
     * @param e E, aka public key
     * @return the private key for p, q, e
     */
    public static long findD(long p, long q, long e) {
        long phi = (p - 1) * (q - 1);
        // To solve => (e * d) % ((p - 1) * (q - 1)) = 1
        //          => (e * d) % phi = 1
        // pgcd(phi, d) = 1
        boolean found = false;
        long D = 1;
        // More than one D ? See findDList method.
        while (!found) { // There must be a better way... This one can loop forever.
            long x = (e * D) % phi;
            if (x == 1) {
                found = true;
            } else {
                D++;
                if (D == Long.MAX_VALUE) {
                    // Not found... Exit loop.
                    D = -1;
                    break;
                }
            }
        }
        long d = D;

        return d;
    }

    /**
     * Takes time...
     * @param p
     * @param q
     * @param e
     * @return
     */
    public static List<Long> findDList(long p, long q, long e) {
        List<Long> dList = new ArrayList<>();

        long phi = (p - 1) * (q - 1);
        // To solve => (e * d) % ((p - 1) * (q - 1)) = 1
        //          => (e * d) % phi = 1
        // pgcd(phi, d) = 1
        long D = 1;
        while (true) {
            long x = (e * D) % phi;
            if (x == 1) {
                dList.add(D);
            }
            D++;
            // if (D == Long.MAX_VALUE) {
            if (D == Integer.MAX_VALUE) { // Could go further...
                // Top limit... Exit loop.
                break;
            }
        }
        return dList;
    }

    /**
     * Find a candidate public key E
     * Careful... Might loop forever (findD)...
     * @param p
     * @param q
     * @param tentativeD
     * @return
     */
    public static List<Long> findCandidateE(long p, long q, long tentativeD) {

        List<Long> suggestions = new ArrayList<>();
        final int NB_KEYS = 10;

        // phi & d, coprime
        long phi = (p - 1) * (q - 1);

        long D = tentativeD; // The private key. Can be any int.

        long pgcdPhiD = pgcd((int)D, (int)phi);
        if (pgcdPhiD != 1) {
            throw new RuntimeException(String.format("Bad combination, GCD = %d, \u03D5 => %s, D => %s", pgcdPhiD,
                    spitMapOut((int)phi, primeFactors((int)phi)), spitMapOut((int)D, primeFactors((int)D))));
//            System.out.printf("==> Bad combination: PGCD D, \u03D5 (%d, %d) : %d => %s\n", D, phi, pgcdPhiD, (pgcdPhiD == 1) ? "good" :
//                    String.format("not good, \u03D5 => %s, D => %s", spitMapOut((int)phi, primeFactors((int)phi)), spitMapOut((int)D, primeFactors((int)D))));
//            return suggestions;
        }
        // (E × D) mod Ø(n) = 1
        // Loop on E...
        long E = 0;
        boolean ok; // = ((E * D) % phi) == 1;
        while (suggestions.size() < NB_KEYS) {
            E += 1;
            ok = ((E * D) % phi) == 1;
            if (E == Long.MAX_VALUE) {
                System.out.printf("E reached value %d, Exiting.", Long.MAX_VALUE);
                break;
            }
            if (ok) {
                suggestions.add(E);
                // We need (E × D) mod Ø(n) = 1
                long modulo = (E * D) % phi;
                System.out.printf("\tFor E = %d, modulo = %d\n", E, modulo);

                long key = findD(p, q, E); // Find Private Key D, with Public Key E
                System.out.printf("\tp=%d, q=%d, E=%d, make a D key: %d\n", p, q, E, key);
            }
        }
        System.out.printf("Exit the loop with E = %d, and size = %d\n", E, suggestions.size());

        return suggestions; // E;
    }

    private static void page328() {
    // Page 328
    long base = 3;
    long mod = 7;
    for (int i=1; i<=6; i++) {
        System.out.printf("%d ^ %d mod %d: %d\n", base, i, mod, powXmodY(base, mod, i));
    }
}

    private static void page329() {
        // Page 329
        System.out.println("-- Page 329 - Diffie-Hellman exchange --");
        // See https://en.wikipedia.org/wiki/Diffie%E2%80%93Hellman_key_exchange
        long aliceNumber = 3;
        long bernardNumber = 6;

        long base = 7;
        long mod = 11;

        long alpha = powXmodY(base, mod, aliceNumber);
        long beta  = powXmodY(base, mod, bernardNumber);

        System.out.printf("Step 2 : Alice %d, Bernard %d\n", alpha, beta);

        // Alice sends alpha to Bernard.
        // Bernard sends beta to Alice.

        // What Alice and Bernard have to agree on is the couple (base, mod), and the way to use it (powXmodY)

        long aliceCalcStep4 = powXmodY(beta, mod, aliceNumber);
        long bernardCalcStep4 = powXmodY(alpha, mod, bernardNumber);

        System.out.printf("Key should be (the same), for Alice %d, for Bernard %d.\n", aliceCalcStep4, bernardCalcStep4);
    }

    private static int encodeWithPublicKey(long pkE, long pkN, int toEncode) {
        // Produce (toEncode ^ pkE) % pkN
        if (false) {
            int encoded = /* Encrypted: M^e (mod N) */ (int) powXmodY(toEncode, pkN, pkE);
            return encoded;
        } else {// (Math.pow(base, x) % mod)
            BigInteger bigE = BigInteger.valueOf(pkE);
            BigInteger bigN = BigInteger.valueOf(pkN);
            if (false) {
                System.out.printf("E %d, bit count: %d, bit length: %d\n", pkE, bigE.bitCount(), bigE.bitLength());
                System.out.printf("N %d, bit count: %d, bit length: %d\n", pkN, bigN.bitCount(), bigN.bitLength());
            }
            int encoded = (int)BigInteger.valueOf(toEncode).modPow(bigE, bigN).longValue();
            // int encoded = (int)BigInteger.valueOf(toEncode).modPow(BigInteger.valueOf(pkE), BigInteger.valueOf(pkN)).longValue();
            return encoded;
        }
    }

    private static List<Integer> encodeMessageWithPrivateKey(String message, long pkE, long pkN) {
        return encodeMessageWithPrivateKey(message, pkE, pkN, false);
    }
    private static List<Integer> encodeMessageWithPrivateKey(String message, long pkE, long pkN, boolean verbose) {
        final byte[] bytes = message.getBytes();

        // Encoding process
        List<Integer> encoded = new ArrayList<>(); // Warning !! Those are ints ! [0..&xFF]
        if (verbose) {
            System.out.println("-- Encoding --");
        }
        for (byte b : bytes) {
            int encodedByte = encodeWithPublicKey(pkE, pkN, b);
            if (verbose) {
                System.out.printf("Encoding (%c) %d -> %s\n",
                        b, b,
                        NumberFormat.getInstance().format(encodedByte));
            }
            encoded.add(encodedByte);
        }
        return encoded;
    }

    private static int decodeWithPrivateKey(long pkP, long pkQ, int privateKey, int toDecode) {
        // int privateKey = (int)findD(pkP, pkQ, pkE); // Private key
        long N = pkP * pkQ;
        return decodeWithPrivateKey(N, privateKey, toDecode);
    }
    private static int decodeWithPrivateKey(long N, int privateKey, int toDecode) {
        // System.out.printf("Found D: %d\n", decryptionKeyD);
        // System.out.printf("%d: %s\n", decryptionKeyD, spitMapOut((int)decryptionKeyD, primeFactors((int)decryptionKeyD)));

        if (false) {
            double pow = Math.pow(toDecode, privateKey);
            if (pow > Long.MAX_VALUE) {
                // System.out.println("Oops... Too big!"); // Argh
                // TODO This section needs improvements... (198 ^ 36) % 209 = 88, and this is not what's found. BigInteger ?
                // find the first under the limit
                int maxPow = 1;
                boolean keepLooping = true;
                while (keepLooping) {
                    if (Math.pow(toDecode, maxPow) >= Long.MAX_VALUE) {
                        keepLooping = false;
                    } else {
                        maxPow += 1;
                    }
                }
                maxPow = (int) Math.round(maxPow / 2); // Divide by 2, to make sure...
                // System.out.printf("\tMax Pow: %d\n", maxPow);
                // Splitting, THE loop
                int remainder = privateKey;
                int smallPow = maxPow;
                long newBase = 1;
                while (remainder > 0) {
                    newBase *= ((Math.pow(toDecode, smallPow)) % N);
                    // System.out.printf("\tLooping on new base: (%d ^ %d) %% %d : %d\n", toDecode, smallPow, N, newBase);
                    remainder -= smallPow;
                    if (remainder > 0) {
                        smallPow = Math.min(smallPow, remainder);
                    }
                }
                pow = newBase;
            }
            int decryptedCharacter = (int) (pow % N);
            return decryptedCharacter;
        } else {
            int decryptedCharacter = (int)BigInteger.valueOf(toDecode).modPow(BigInteger.valueOf(privateKey), BigInteger.valueOf(N)).longValue();
            // int decryptedCharacter = (int)BigInteger.valueOf(toDecode).modPow(BigInteger.valueOf(pkE), BigInteger.valueOf(N)).longValue();
            return decryptedCharacter;
        }
    }

    private static byte[] decodeMessageWithPrivateKey(List<Integer> encoded, int privateKeyD, long N) {
        return decodeMessageWithPrivateKey(encoded, privateKeyD, N,false);
    }
    private static byte[] decodeMessageWithPrivateKey(List<Integer> encoded, int privateKeyD, long N, boolean verbose) {
        List<Byte> decoded = new ArrayList<>();
        AtomicInteger atomicKey = new AtomicInteger(privateKeyD);
        encoded.stream().forEach(enc -> {
            byte decodedByte = (byte)decodeWithPrivateKey(N, atomicKey.get(), enc);
            if (verbose) {
                System.out.printf("Decoded: %s => %d (%c)\n",
                        NumberFormat.getInstance().format(enc),
                        decodedByte,
                        decodedByte);
            }
            decoded.add(decodedByte);
        });
        byte[] decodedBA = new byte[decoded.size()];
        for (int i=0; i<decoded.size(); i++) {
            decodedBA[i] = decoded.get(i).byteValue();
        }
        if (verbose) {
            System.out.printf("\nFinal message, decoded: [%s]\n", new String(decodedBA));
        }
        return decodedBA;
    }

    private static void page472() { // RSA

        System.out.println("--- RSA ---");

        // (p: 17, q: 11, e: 7) => OK
        // (p: 17, q: 11, e: 13) => OK
        // (p: 127, q: 11, e: 13) => OK
        // (p: 17, q: 499, e: 13) => OK
        // (p: 127, q: 499, e: 13) => OK
        // (p: 127, q: 499, e: 46379) => OK
        long aliceP = 127; // _456_987;
        long aliceQ = 499;
        // p: 9_419, q: 1_933

        // (e * d) % ((p - 1) * (q - 1)) = 1
        //              => (e * d) % phi = 1
        long aliceE = 23_801; // 46_379; // 7; // 13; // 7; // Does it have to be a prime ?

        long aliceD = 29;

        if (false) {
            final List<Long> primes = PrimeNumbers.getPrimes(500);
            System.out.println("Got the 500 first primes...");

            System.out.printf("P is prime ? %b\n", primes.stream().filter(p -> p == aliceP).findFirst().isPresent());
            System.out.printf("Q is prime ? %b\n", primes.stream().filter(p -> p == aliceQ).findFirst().isPresent());
            System.out.printf("E is prime ? %b\n", primes.stream().filter(p -> p == aliceE).findFirst().isPresent());

            if (!primes.stream().filter(p -> p == aliceP).findFirst().isPresent()) {
                System.out.printf("%d is not prime. Bye\n", aliceP);
                return;
            }
            if (!primes.stream().filter(p -> p == aliceQ).findFirst().isPresent()) {
                System.out.printf("%d is not prime. Bye\n", aliceQ);
                return;
            }
        } else {
            if (PrimeNumbers.isPrime(aliceP)) {
                System.out.printf("P (%d) is prime\n", aliceP);
            } else {
                System.out.printf("P (%d) is not prime. Aborting.\n", aliceP);
                return;
            }
            if (PrimeNumbers.isPrime(aliceQ)) {
                System.out.printf("Q (%d) is prime\n", aliceQ);
            } else {
                System.out.printf("Q (%d) is not prime. Aborting.\n", aliceQ);
                return;
            }
            System.out.printf("E is%s prime.\n", PrimeNumbers.isPrime(aliceE) ? "" : " not");
        }
        // We need D and ((p-1) x (q-1)) to be relative primes (aka coprimes)
        boolean okCombo = checkPQE(aliceP, aliceQ, aliceE);
        System.out.printf("(e * D) mod \u03D5 = 1 ? %b\n", okCombo);
        okCombo = checkPhiDCoprime(aliceP, aliceQ, aliceE);
        System.out.printf("\u03D5 & D coprimes ? %b\n", okCombo);

        // For the fun:
        long alicePxQ = aliceP * aliceQ; // aka N
        System.out.printf("Alice P x Q = %d: %s\n", alicePxQ, spitMapOut((int)alicePxQ, primeFactors((int)alicePxQ)));

        long aliceN = alicePxQ;
        // Alice can diffuse E & N, her public key
        System.out.printf("Alice is diffusing public key E & N (%d, %d)\n", aliceE, aliceN);
        System.out.printf(" --> (Private Key: %d)\n", findD(aliceP, aliceQ, aliceE));

        // Now, let's encrypt...
        int characterToEncrypt = 88; // aka M, Ascii 88: 1011000, 'X'.

        if (false) {
            int encryptedC /* Encrypted: M^e (mod N) */ = (int) powXmodY(characterToEncrypt, aliceN, aliceE); // Encrypted with what Alice has diffused.
            System.out.printf("Used by Bernard, with N and E (%d, %d), %d is encrypted into %d\n", aliceN, aliceE, characterToEncrypt, encryptedC);
            System.out.printf("%d is what Alice is receiving from Bernard.\n", encryptedC);

            // pow (aka exponential) and mod are one way functions.
            // Now, decryption..., on Alice's side
            System.out.printf("Alice is now decrypting Bernard's message (%d), using P, Q, and E (%d, %d, %d)\n", encryptedC, aliceP, aliceQ, aliceE);
            int receivedCharacter = encryptedC;
            int decryptionKeyD = (int) findD(aliceP, aliceQ, aliceE); // aka Private Key

            System.out.printf("Found D: %d\n", decryptionKeyD);
            System.out.printf("%d: %s\n", decryptionKeyD, spitMapOut((int) decryptionKeyD, primeFactors((int) decryptionKeyD)));

            long testBigInt = powXmodY(receivedCharacter, decryptionKeyD, aliceN);
            System.out.printf("Regular - Found : %d\n", testBigInt);
            testBigInt = BigInteger.valueOf(receivedCharacter).modPow(BigInteger.valueOf(decryptionKeyD), BigInteger.valueOf(aliceN)).longValue(); // Seems to work
            System.out.printf("BigInteger - Found : %d\n", testBigInt);

            double pow = Math.pow(receivedCharacter, decryptionKeyD);
            if (pow > Long.MAX_VALUE) {
                System.out.println("Oops... Too big!"); // Argh
                // TODO This section needs improvements... (198 ^ 36) % 209 = 88, and this is not what's found. BigInteger ?
                // find the first under the limit
                int maxPow = 1;
                boolean keepLooping = true;
                while (keepLooping) {
                    if (Math.pow(receivedCharacter, maxPow) >= Long.MAX_VALUE) {
                        keepLooping = false;
                    } else {
                        maxPow += 1;
                    }
                }
                maxPow = (int) Math.round(maxPow / 2); // Divide by 2, to make sure...
                System.out.printf("\tMax Pow: %d\n", maxPow);
                // Splitting, THE loop
                int remainder = decryptionKeyD;
                int smallPow = maxPow;
                long newBase = 1;
                while (remainder > 0) {
                    newBase *= ((Math.pow(receivedCharacter, smallPow)) % aliceN);
                    System.out.printf("\tLooping on new base: (%d ^ %d) %% %d : %d\n", receivedCharacter, smallPow, aliceN, newBase);
                    remainder -= smallPow;
                    if (remainder > 0) {
                        smallPow = Math.min(smallPow, remainder);
                    }
                }
                pow = newBase;
            }
            long decryptedCharacter = ((long) pow) % aliceN;
            System.out.printf("Decrypted: (%d ^ %d) mod %d => %d ...\n", receivedCharacter, decryptionKeyD, aliceN, decryptedCharacter);

            if (characterToEncrypt == decryptedCharacter) {
                System.out.println("Perfect!");
            } else {
                System.out.printf("Error: Expected %d, received %d\n", characterToEncrypt, decryptedCharacter);
            }
        } else {
            System.out.println("-- With external methods (and BigIntegers) --");
            int encryptedWithPublicKey = encodeWithPublicKey(aliceE, aliceN, characterToEncrypt);
            System.out.printf("Encryption : %s (%c) => %s\n",
                    NumberFormat.getInstance().format(characterToEncrypt),
                    characterToEncrypt,
                    NumberFormat.getInstance().format(encryptedWithPublicKey));

            // TODO A test: decode with public key...

            int privateKeyD = (int)findD(aliceP, aliceQ, aliceE); // Private key

            if (true) { // Can take time...
                final List<Long> dList = findDList(aliceP, aliceQ, aliceE); // Limited to Integer.MAX_VALUE !!
                System.out.printf("Possible Ds (private keys), %d entries.\n", dList.size());
                dList.stream()
                        .limit(100) // A limit !!
                        .forEach(k -> System.out.printf("\t-> %d\n", k));
                // Try this: use the second one
                privateKeyD = (int)dList.get(1).longValue();
            }

            int decryptedWithPrivateKey = decodeWithPrivateKey(aliceP, aliceQ, privateKeyD, encryptedWithPublicKey);
            System.out.printf("Encrypted: %s, decrypted: %s (%c)\n",
                    NumberFormat.getInstance().format(encryptedWithPublicKey),
                    NumberFormat.getInstance().format(decryptedWithPrivateKey),
                    decryptedWithPrivateKey);
        }

        // Test on a full message
        String message = "Hello RSA World!";
        final byte[] bytes = message.getBytes();

        // Encoding process
        List<Integer> encoded = new ArrayList<>(); // Warning !! Those are ints ! [0..&xFF]
        System.out.println("-- Encoding --");
        for (byte b : bytes) {
            int encodedByte = encodeWithPublicKey(aliceE, aliceN, b);
            System.out.printf("Encoding (%c) %d -> %s\n",
                    b, b,
                    NumberFormat.getInstance().format(encodedByte));
            encoded.add(encodedByte);
        }
        // Write the encoded result into a file ?
        try {
            File file = new File("." + File.separator + "encoded.bin");
            FileWriter fos = new FileWriter(file);
            // fos.write(encoded. ...getBytes());
            encoded.stream().forEach(b -> {
                try {
                    fos.write(b); // writes an int
                    // System.out.printf("\tWritten %d\n", b);
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            });
            fos.close();
            System.out.println("Created " + file.getAbsolutePath());
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // Decoding process
        int privateKeyD = (int)findD(aliceP, aliceQ, aliceE); // Private key
        if (false) {
            final List<Long> dList = findDList(aliceP, aliceQ, aliceE); // Limited to Integer.MAX_VALUE !!
            System.out.printf("Possible Ds (private keys), %d entries.\n", dList.size());
//            dList.stream()
//                    .limit(100) // A limit !!
//                    .forEach(k -> System.out.printf("\t-> %d\n", k));
            // Try this: use the second one
            privateKeyD = (int) dList.get(1).longValue();
        }

        // Read the encoded from a file ?
        try {
            FileReader encodedData = new FileReader(new File("." + File.separator + "encoded.bin"));
            encoded.clear();
            int read;
            while ((read = encodedData.read()) != -1) {
                encoded.add(read);
                // System.out.printf("\tRead: %d\n", read);
            }
            encodedData.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        System.out.printf("-- Decoding with private key %d --\n", privateKeyD);
        List<Byte> decoded = new ArrayList<>();
        AtomicInteger atomicKey = new AtomicInteger(privateKeyD);
        encoded.stream().forEach(enc -> {
            byte decodedByte = (byte)decodeWithPrivateKey(aliceP, aliceQ, atomicKey.get(), enc);
            System.out.printf("Decoded: %s => %d (%c)\n",
                    NumberFormat.getInstance().format(enc),
                    decodedByte,
                    decodedByte);
            decoded.add(decodedByte);
        });
        byte[] decodedBA = new byte[decoded.size()];
        for (int i=0; i<decoded.size(); i++) {
            decodedBA[i] = decoded.get(i).byteValue();
        }
        System.out.printf("\nFinal message, decoded: [%s]\n", new String(decodedBA));
    }

    public static void main__(String[] args) {

        if (false) {
            BigInteger b1, b2;

            b1 = new BigInteger("321456");
            b2 = new BigInteger("31711");

            // apply mod() method
            BigInteger result = b1.mod(b2);

            // print result
            System.out.println("Result of mod operation between " + b1
                    + " and " + b2 +
                    " equal to " + result); // Expect 4346

            // BigInteger test. (198 ^ 36) % 209 = 88
            BigInteger a = BigInteger.valueOf(198);
            BigInteger b = BigInteger.valueOf(36);
            BigInteger c = BigInteger.valueOf(209);

            BigInteger pow = a.pow(b.intValue());
            System.out.printf("Pow: " + pow.toString());
            /*BigInteger*/
            result = pow.mod(c);
            System.out.printf("With BigIntegers, result: %d\n", result.longValue());

            // System.exit(0);
        }

        if (true) {
            page328();
            System.out.println("---");
            page329();
            System.out.println("---");
        }

        boolean testFindD = false;
        boolean findE = false;
        if (testFindD) {
            // long p = 9_419, q = 1_933;  // TODO Try this one.
            long d = findD(17L, 11L, 7L);
            System.out.printf("For P: %d, Q: %d, E: %d, D: %d\n", 17L, 11L, 7L, d); // expected 23
            // Again
            d = findD(7L, 11L, 13L);
            System.out.printf("For P: %d, Q: %d, E: %d, D: %d\n", 7L, 11L, 13L, d); // expected 37
        } else if (findE) {
            // long p = 17, q = 11, d = 374; // To see the Exception
            // long p = 17, q = 11, d = 23;
            long p = 127, q = 499, d = 29;
            try {
                List<Long> testE = findCandidateE(p, q, d);
                System.out.printf("For P: %d, Q: %d, D: %d\n", p, q, d);
                testE.stream().forEach(e -> {
                    System.out.printf("Candidate E: %d (prime: %b)\n", e, PrimeNumbers.isPrime(e));
                });
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else {
            page472();
        }

        if (false) {
            String big = BigInteger.valueOf(113).pow((int) 299).toString();
            System.out.printf("%d ^ %d = %s\n%d characters long.]\n", 113, 299, big, big.length());

            long P = 127;
            long Q = 499;
            long E = 46_379;

            long D = 29;

            long PHI = ((P - 1) * (Q - 1));
            long modulo = BigInteger.valueOf(E).multiply(BigInteger.valueOf(D)).mod(BigInteger.valueOf(PHI)).longValue();
            System.out.printf("Modulo: %d, %s\n", modulo, (modulo == 1) ? "good!" : "Oops...");

            long enc = 44_021;
            long N = P * Q;
            long x = BigInteger.valueOf(enc).modPow(BigInteger.valueOf(D), BigInteger.valueOf(N)).longValue();
            System.out.printf("Encrypted: %d, Decrypted: %d\n", enc, x);

            // long N = 63_373;
            System.out.printf("N %d => %s\n", N, spitMapOut((int)N, primeFactors((int)N)));

            final long privateK = findD(P, Q, E);
            System.out.printf("With P %d, Q %d, E %d, found private key %d\n", P, Q, E, privateK);

            N = 187;
            System.out.printf("N %d => %s\n", N, spitMapOut((int)N, primeFactors((int)N)));
            P = 17;
            Q = 11;
            E = 7;
            final long otherPrivateK = findD(P, Q, E);
            System.out.printf("With P %d, Q %d, E %d, found private key %d\n", P, Q, E, otherPrivateK); // That seems to work...
        }
    }

    /**
     * A Test from a file
     * @param args args[0] is optional file name
     */
    public static void main_(String... args) {

        // TODO Find real world samples. ssh ? ~/.ssh/id_rsa.pub & ~/.ssh/id_rsa Where is N, where is E, where is D ?
        // See https://crypto.stackexchange.com/questions/52688/see-the-rsas-n-p-q-e-text-and-d-from-ssh-keygen

        String messageFileName = "." + File.separator + "encoded.bin";

        if (args.length > 0) {
            messageFileName = args[0];
        }

        // Hack. Read the data from a file, without having the private key, decode it.

        // Public Key, from Alice's phone book.
        // long aliceP = 127;
        // long aliceQ = 499;
        long aliceN = 63_373; // Should be P*Q, P and Q being prime numbers
        long aliceE = 23_801;

        final Map<Integer, Integer> primeFactors = primeFactors((int)aliceN);
        final Set<Integer> keys = primeFactors.keySet();
        long aliceP = ((Integer)keys.toArray()[0]).longValue();
        long aliceQ = ((Integer)keys.toArray()[1]).longValue();

        // Read the encoded from a file ?
        List<Integer> encoded = new ArrayList<>();
        try {
            FileReader encodedData = new FileReader(new File(messageFileName));
            encoded.clear();
            int read;
            while ((read = encodedData.read()) != -1) {
                encoded.add(read);
                // System.out.printf("\tRead: %d\n", read);
            }
            encodedData.close();
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }

        // This is where we HACK the private key.
        int privateKeyD = (int)findD(aliceP, aliceQ, aliceE); // Private key

        System.out.printf("-- Decoding with private key %d --\n", privateKeyD);
        List<Byte> decoded = new ArrayList<>();
        AtomicInteger atomicKey = new AtomicInteger(privateKeyD);
        encoded.stream().forEach(enc -> {
            byte decodedByte = (byte)decodeWithPrivateKey(aliceP, aliceQ, atomicKey.get(), enc);
            System.out.printf("Decoded: %s => %d (%c)\n",
                    NumberFormat.getInstance().format(enc),
                    decodedByte,
                    decodedByte);
            decoded.add(decodedByte);
        });
        byte[] decodedBA = new byte[decoded.size()];
        for (int i=0; i<decoded.size(); i++) {
            decodedBA[i] = decoded.get(i).byteValue();
        }
        System.out.printf("\nFinal message, decoded: [%s]\n", new String(decodedBA));
    }

    /**
     * Another crack test
     * @param args
     */
    public static void main(String... args) {
        // Try to crack the key, with "big." - or so - numbers
        long aliceP = 9_419, aliceQ = 1_933;  // Try this one.

        // Public Key, from Alice's phone book.
        long aliceN = aliceP * aliceQ; // 63_373; // Should be P*Q, P and Q being prime numbers
        long aliceE = 23_801;

        System.out.printf("P: %s is prime: %b, Q: %s is prime: %b, E: %s is prime: %b\n",
                NumberFormat.getInstance().format(aliceP), PrimeNumbers.isPrime(aliceP),
                NumberFormat.getInstance().format(aliceQ), PrimeNumbers.isPrime(aliceQ),
                NumberFormat.getInstance().format(aliceE), PrimeNumbers.isPrime(aliceE));

        int characterToEncrypt = 88; // X
        int encryptedWithPublicKey = encodeWithPublicKey(aliceE, aliceN, characterToEncrypt);
        System.out.printf("Encryption : %s (%c) => %s\n",
                NumberFormat.getInstance().format(characterToEncrypt),
                characterToEncrypt,
                NumberFormat.getInstance().format(encryptedWithPublicKey));

        int privateKeyD; // = (int)findD(aliceP, aliceQ, aliceE); // Private key

        // THE crack
        long before = System.currentTimeMillis();
        final List<Long> dList = findDList(aliceP, aliceQ, aliceE);
        long after = System.currentTimeMillis();

        System.out.printf("Possible Ds (private keys), %d entries, found in %s ms.\n",
                dList.size(),
                NumberFormat.getInstance().format(after - before));
        if (false) {
            dList.stream()
                    .limit(100) // A limit !!
                    .forEach(k -> System.out.printf("\t-> %d\n", k));
        }
        // Try this: use the SECOND one
        // This is where we HACK the private key.
        privateKeyD = (int)dList.get(1).longValue();
        System.out.printf("D: %s is prime: %b\n",
                NumberFormat.getInstance().format(privateKeyD), PrimeNumbers.isPrime(privateKeyD));

        int decryptedWithPrivateKey = decodeWithPrivateKey(/*aliceP, aliceQ*/ aliceN, privateKeyD, encryptedWithPublicKey);
        System.out.printf("Encrypted: %s, decrypted: %s (%c)\n",
                NumberFormat.getInstance().format(encryptedWithPublicKey),
                NumberFormat.getInstance().format(decryptedWithPrivateKey),
                decryptedWithPrivateKey);

        // Now, on a message
        String message = "Let's do it again...";
        final List<Integer> encodedMessage = encodeMessageWithPrivateKey(message, aliceE, aliceN);

        final byte[] decodedMessage = decodeMessageWithPrivateKey(encodedMessage, privateKeyD, aliceN);
        System.out.printf("Decoded: [%s]\n", new String(decodedMessage));

        System.out.println("Done.");
    }
}
