package encryption;

import primes.PrimeNumbers;

import java.math.BigInteger;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import static primes.PGCD.pgcd;
import static primes.Primes.primeFactors;
import static primes.Primes.spitMapOut;

public class Basics02 {

    /**
     * This is a "one way function" (fonction a sens unique)
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
    /**
     * Calculate the Private Key
     * TODO Needs tuning...
     *
     * See https://www.geeksforgeeks.org/how-to-solve-rsa-algorithm-problems/#
     *
     * @param p
     * @param q
     * @param e
     * @return the private key for p, q, e
     */
    public static long findD(long p, long q, long e) {
        long phi = (p - 1) * (q - 1);
        // To solve => (e * d) % ((p - 1) * (q - 1)) = 1
        //          => (e * d) % phi = 1
        boolean found = false;
        long D = 1;
        while (!found) { // TODO there must be a better way...
            long x = (e * D) % phi;
            if (x == 1) {
                found = true;
            } else {
                D++;
            }
        }
        long d = D;
        // long d = (phi + 1) / e;
        // long d = (e - 1) % phi;
        return d;
    }

    public static long findCandidateE(long p, long q) {
        // (e + phi) must be coprime of p and q
        long phi = (p - 1) * (q - 1);
        long alpha = 1; // Can be any int...
        long D = 37; // The private key. Can be any int. prime ?
        long E = ((alpha + 1) * phi) / D;

        long coprime = E + phi;
        long pgcdEQ = pgcd((int)coprime, (int)q);
        long pgcdEP = pgcd((int)coprime, (int)p);
        if (pgcdEQ != 1 || pgcdEP != 1) {
            System.out.printf("PGCD E, P (%d, %d) : %d => %s\n", E, p, pgcdEP, (pgcdEP == 1) ? "good" :
                    String.format("not good, E => %s, P => %s", spitMapOut((int)coprime, primeFactors((int)coprime)), spitMapOut((int)p, primeFactors((int)p))));
            System.out.printf("PGCD E, Q (%d, %d) : %d => %s\n", E, q, pgcdEQ, (pgcdEQ == 1) ? "good" :
                    String.format("not good, E => %s, Q => %s", spitMapOut((int)coprime, primeFactors((int)coprime)), spitMapOut((int)q, primeFactors((int)q))));
            return 0;
        }

        long key = findD(p, q, E);
        System.out.printf("p=%d, q=%d, E=%d, make a D key: %d\n", p, q, E, key);

        return E;
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
        long aliceNumber = 3;
        long bernardNumber = 6;

        long base = 7;
        long mod = 11;

        long alpha = powXmodY(base, mod, aliceNumber);
        long beta  = powXmodY(base, mod, bernardNumber);

        System.out.printf("Step 2 : Alice %d, Bernard %d\n", alpha, beta);

        // Alice sends alpha to Bernard
        // Bernard sends beta to Alice

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
        } else {
            int encoded = (int)BigInteger.valueOf(toEncode).modPow(BigInteger.valueOf(pkE), BigInteger.valueOf(pkN)).longValue();
            return encoded;
        }
    }
    private static int decodeWithPrivateKey(long pkP, long pkQ, long pkE, int privateKey, int toDecode) {
        // int privateKey = (int)findD(pkP, pkQ, pkE); // Private key
        long N = pkP * pkQ;

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
    private static void page472() { // RSA

        System.out.println("--- RSA ---");
        final List<Long> primes = PrimeNumbers.getPrimes(500);
        System.out.println("Got the 500 first primes...");

        // (p: 17, q: 11, e: 7) => OK
        // (p: 17, q: 11, e: 13) => OK
        // (p: 127, q: 11, e: 13) => OK
        // (p: 17, q: 499, e: 13) => OK
        // (p: 127, q: 499, e: 13) => OK
        long aliceP = 127;
        long aliceQ = 499;
        // (e * d) % ((p - 1) * (q - 1)) = 1
        //              => (e * d) % phi = 1
        long aliceE = 13; // 7; // Does it have to be a prime ?

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
        boolean okCombo = checkPQE(aliceP, aliceQ, aliceE);
        System.out.printf("(e * D) mod phi = 1 ? %b\n", okCombo);

        // For the fun:
        long alicePxQ = aliceP * aliceQ; // aka N
        System.out.printf("Alice P x Q = %d: %s\n", alicePxQ, spitMapOut((int)alicePxQ, primeFactors((int)alicePxQ)));

        // We need e + ((p-1) x (q-1)) to be relative primes (aka coprimes)
        long coprime = aliceE + ((aliceP - 1) * (aliceQ - 1));
        long pgcdEP = pgcd((int)coprime, (int)aliceP);
        System.out.printf("PGCD E, P (%d, %d) : %d => %s\n", aliceE, aliceP, pgcdEP, (pgcdEP == 1) ? "good" :
                String.format("not good, E => %s, P => %s", spitMapOut((int)coprime, primeFactors((int)coprime)), spitMapOut((int)aliceP, primeFactors((int)aliceP))));
        long pgcdEQ = pgcd((int)coprime, (int)aliceQ);
        System.out.printf("PGCD E, Q (%d, %d) : %d => %s\n", aliceE, aliceQ, pgcdEQ, (pgcdEQ == 1) ? "good" :
                String.format("not good, E => %s, Q => %s", spitMapOut((int)coprime, primeFactors((int)coprime)), spitMapOut((int)aliceQ, primeFactors((int)aliceQ))));

        if (pgcdEP != 1 || pgcdEQ != 1) {
            System.err.printf("Bad value for E (%d)... Bye.\n", aliceE);
            return;
        }
        long aliceN = alicePxQ;
        // Alice can diffuse E & N, her public key
        System.out.printf("Alice is diffusing public key E & N (%d, %d)\n", aliceE, aliceN);

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
            int decryptionKeyD = (int)findD(aliceP, aliceQ, aliceE); // Private key
            int decryptedWithPrivateKey = decodeWithPrivateKey(aliceP, aliceQ, aliceE, decryptionKeyD, encryptedWithPublicKey);
            System.out.printf("Encrypted: %s, decrypted: %s (%c)\n",
                    NumberFormat.getInstance().format(encryptedWithPublicKey),
                    NumberFormat.getInstance().format(decryptedWithPrivateKey),
                    decryptedWithPrivateKey);
        }

        // Test on a full message
        String message = "Hello RSA World!";
        final byte[] bytes = message.getBytes();
        List<Integer> encoded = new ArrayList<>(); // Warning !! Those are ints ! [0..&xFF]
        System.out.println("-- Encoding --");
        for (byte b : bytes) {
            int encodedByte = encodeWithPublicKey(aliceE, aliceN, b);
            System.out.printf("Encoding (%c) %d -> %s\n",
                    b, b,
                    NumberFormat.getInstance().format(encodedByte));
            encoded.add(encodedByte);
        }
        System.out.println("-- Decoding --");
        int decryptionKeyD = (int)findD(aliceP, aliceQ, aliceE); // Private key
        List<Byte> decoded = new ArrayList<>();
        encoded.stream().forEach(enc -> {
            byte decodedByte = (byte)decodeWithPrivateKey(aliceP, aliceQ, aliceE, decryptionKeyD, enc);
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

    public static void main(String[] args) {

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

        if (false) {
            page328();
            System.out.println("---");
            page329();
            System.out.println("---");
        }

        boolean testFindD = false;
        boolean findE = false;
        if (testFindD) {

            long d = findD(17L, 11L, 7L);
            System.out.printf("For P: %d, Q: %d, E: %d, D: %d\n", 17L, 11L, 7L, d); // expected 23

            d = findD(7L, 11L, 13L);
            System.out.printf("For P: %d, Q: %d, E: %d, D: %d\n", 7L, 11L, 13L, d); // expected 37
        } else if (findE) {
            long testE = findCandidateE(17, 11);
            System.out.printf("For P: %d, Q: %d, Candidate E: %d\n", 17, 11, testE);
        } else {
            page472();
        }
    }
}
