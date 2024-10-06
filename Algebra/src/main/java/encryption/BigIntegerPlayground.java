package encryption;

import java.math.BigInteger;

public class BigIntegerPlayground {
    static String sample = "AAAAB3NzaC1yc2EAAAADAQABAAACAQDL2mOfC+qBkEKOWPWvQQ+hEhzkklXxAZxqfXclP79omAekR1VGsqnhdmrSK9oKwdjy0M+Tuwi" +
            "0g//RxkvokVy+EnneQPXtp7k551vHhdTGCL5OitjfMGqyUxroEf7Ki6iERqIFfCbir5uI34EvKZVctKxjqqMb6TYpzqYwxfBSkmYAy2r+5t7n5Jkhoko9T" +
            "wQhK3On2YRLAZVZK22KtR3VkeRDE7E+j7OYal5BKCpZjJasm+EiUfLrD0/gCmneiIK8CRyQPviD+6oIi77uHCHzyjlbCrW7iC8rmK7/4JeHVwsRfMSgxGh" +
            "bZWkqyZEFasa1PPkQjAgGLOPvWQb6RdCW1Ud+0sAlKfw6db/Vw6euPjXFgQfbO5pIk9A7XGdeMZ3A7bKGnbjelOrR/8Al6U2sq8ouNN0vad3U4aqJn+BC1" +
            "NwIWqgfOc3RORSk58bctSA2lr8kzfM2Oi0w1Q7b5oSIUTO3UpP55kTqbitgg4wcMiuUVo+lvHJniqTZwpFNNBayyJWJeu7W85WGolWXrjZTWsjO5olRpwa" +
            "JYoVfhtnmo8Otox7ICJOxXsS/0yU+1rsxkzfd0liUrtbbw7Xa8DwfbhfRCFgbk+w+ASTEehN3iEvTTQGUNq6bJaLwtTp/2d0jufjQm8s1f8cn37edlJFxL" +
            "coZb/DN6R/Vzl1poMRkxw";

    public static void main(String[] args) {
        byte[] byteSample = sample.getBytes();

        BigInteger bigStuff = new BigInteger(byteSample);
        System.out.printf("Big number %s\n: => %d digits, bit count: %d, bit length: %d, probable prime: %b\n",
                bigStuff.toString(), bigStuff.toString().length(), bigStuff.bitCount(), bigStuff.bitLength(), bigStuff.isProbablePrime(1));

        // Find the next prime...
        BigInteger bigOne = BigInteger.valueOf(1);
        boolean isPrime = false;
        long nbTest = 0;
        while (!isPrime) {
            bigStuff = bigStuff.add(bigOne);

            // String bigString = bigStuff.toString().substring(bigStuff.toString().length() - 10);
            // System.out.printf("...%s\n", bigString);

            isPrime = bigStuff.isProbablePrime(1);
            nbTest++;
            if (nbTest % 1_000 == 0) {
                System.out.printf("Test #%d...\n", nbTest);
            }
        }
        System.out.printf("Probably Prime: %s\n", bigStuff.toString());
    }
}
