package primes;

import java.math.BigInteger;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PrimeNumbers {

    private final static long SEC  = 1_000L;
    private final static long MIN  = 60 * SEC;
    private final static long HOUR = 60 * MIN;
    private final static long DAY  = 24 * HOUR;

    public static int[] msToHMS(long ms) {
        long remainder = ms;
        int days = (int) (remainder / DAY);
        remainder -= (days * DAY);
        int hours = (int) (remainder / HOUR);
        remainder -= (hours * HOUR);
        int minutes = (int) (remainder / MIN);
        remainder -= (minutes * MIN);
        int seconds = (int) (remainder / SEC);
        remainder -= (seconds * SEC);
        int millis = (int)remainder;

        return new int[] { days, hours, minutes, seconds, millis };
    }

    public static String fmtDHMS(int[] date) {
        String str = "";
        if (date[0] > 0)
            str = String.format("%d day%s ", date[0], (date[0] > 1 ? "s" : ""));
        if (date[1] > 0 || !str.trim().isEmpty())
            str += String.format("%d hour%s ", date[1], (date[1] > 1 ? "s" : ""));
        if (date[2] > 0 || !str.trim().isEmpty())
            str += (String.format("%d minute%s", date[2], (date[2] > 1 ? "s" : "")));
        if (date[3] > 0 || date[4] > 0) {
            str += (String.format("%s%d.%03d sec%s", (!str.trim().isEmpty() ? " " : ""), date[3], date[4], (date[3] > 1 ? "s" : "")));
        }
        return str;
    }


    /**
     * Check if a long is a prime number
     * @param num the number to check
     * @return true or false
     */
    public static boolean isPrime(long num) {
        boolean isPrime = false;
        int count = 0;
        // Check for divisibility from 2 up to i/2
        for (int j = 2; j <= num / 2; j++) {
            if (num % j == 0) {
                count++;  // Increment if 'num' is divisible by 'j'
                break;    // Exit loop if a divisor is found. We don't need more than one.
            }
        }
        // If the count is 0, 'i' is prime
        if (count == 0) {
            isPrime = true;
        }
        return isPrime;
    }

    /**
     * Same as above, with BigInteger
     * @param num
     * @return
     */
    public static boolean isPrime(BigInteger num) {
        boolean isPrime = false;
        int count = 0;
        // Check for divisibility from 2 up to i/2
        BigInteger bigJ = BigInteger.TWO;
        while (bigJ.compareTo(num.divide(BigInteger.TWO)) <= 0) {
            if (num.mod(bigJ).compareTo(BigInteger.ZERO) == 0) {
                count++;
                break;
            }
            bigJ = bigJ.add(BigInteger.ONE); // j++
            if (bigJ.mod(BigInteger.valueOf(1_000_000)).compareTo(BigInteger.ZERO) == 0) {
                System.out.printf("... BigJ is now %s\n", bigJ.toString());
            }
        }
        // If the count is 0, 'i' is prime
        if (count == 0) {
            isPrime = true;
        }
        return isPrime;
    }

    /**
     * Get the first nb prime numbers
     * @param nb The number of elements in the returned list
     * @return The list of the first n prime numbers.
     */
    public static List<Long> getPrimes(int nb) {
        List<Long> primeList = new ArrayList<>();
        int howMany = 0;
        int i = 1;
        while (howMany < nb) {
            int count = 0;  // Reset counter for each 'i'
            // Check for divisibility from 2 up to i/2
            for (int j = 2; j <= i / 2; j++) {
                if (i % j == 0) {
                    count++;  // Increment if 'i' is divisible by 'j'
                    break;    // Exit loop if a divisor is found
                }
            }
            // If the count is 0, 'i' is prime
            if (count == 0) {
                howMany++;
                primeList.add((long)i);  // Add the prime number to the list
            }
            i++;
        }
        return primeList;
    }

    /**
     * Same as above, but with BigIntegers
     * @param nb
     * @return
     */
    public static List<BigInteger> getPrimes(BigInteger nb) {
        List<BigInteger> primeList = new ArrayList<>();
        BigInteger howMany = BigInteger.ZERO;
        BigInteger bigI = BigInteger.ONE;
        while (howMany.compareTo(nb) < 0) {
            int count = 0;
            BigInteger bigJ = BigInteger.TWO;
            while (bigJ.compareTo(bigI.divide(BigInteger.TWO)) <= 0) {
                if (bigI.mod(bigJ).compareTo(BigInteger.ZERO) == 0) {
                    count++;
                    break;
                }
                bigJ = bigJ.add(BigInteger.ONE);
            }
            if (count == 0) {
                howMany = howMany.add(BigInteger.ONE);
                primeList.add(bigI);
            }
            bigI = bigI.add(BigInteger.ONE);
        }
        return primeList;
    }
    /*
     * Spits out all the prime numbers, below 'n'.
     */
    public static void main(String... args) {

        if (false) {
            long ns = 5_946_324_528_320L;
            System.out.printf("%d ns = %s\n", ns, fmtDHMS(msToHMS(ns / 1_000_000)));
            System.exit(0);
        }

        int num = 3_000;  // Define the upper limit (n)
        int count;        // Initialize counter for divisibility checks
        int nbPrime = 0;

        // Iterate from 1 up to 'num' to identify prime numbers
        for (int i = 1; i <= num; i++) {
            count = 0;  // Reset counter for each 'i'
            // Check for divisibility from 2 up to i/2
            for (int j = 2; j <= i / 2; j++) {
                if (i % j == 0) {
                    count++;  // Increment if 'i' is divisible by 'j'
                    break;    // Exit loop if a divisor is found
                }
            }
            // If the count is 0, 'i' is prime
            if (count == 0) {
                nbPrime++;
                System.out.printf("#%d . %d\n", nbPrime, i);  // Output the prime number
            }
        }
        System.out.printf("Found %d prime numbers below %d\n", nbPrime, num);

        // Get the prime numbers list, as Longs.
        final List<Long> primes = getPrimes(500);
        System.out.printf("500 first primes: %s\n", primes.stream().map(l -> l.toString()).collect(Collectors.joining(", ")));

        System.out.printf("Is 499 prime ? %b\n", isPrime(499));
        System.out.printf("Is 1789 prime ? %b\n", isPrime(1789));

        List<Long> list = List.of(499L, 1789L, 437L);
        list.forEach(n -> System.out.printf("Is %d prime ? %b\n", n, isPrime(n)));

        // Get the prime numbers list, as BigIntegers
        final List<BigInteger> bigPrimes = getPrimes(BigInteger.valueOf(500));
        System.out.printf("500 first (big) primes: %s\n", primes.stream().map(l -> l.toString()).collect(Collectors.joining(", ")));

        List<BigInteger> bigList = List.of(BigInteger.valueOf(499L), BigInteger.valueOf(1789L), BigInteger.valueOf(437L));
        bigList.forEach(n -> System.out.printf("Is Big %s prime ? %b\n", n.toString(), isPrime(n)));

        String bigSample = "AAAAB3NzaC1yc2EAAAADAQABAAACAQDL2mOfC+qBkEKOWPWvQQ+hEhzkklXxAZxqfXclP79omAekR1VGsqnhdmrSK9oKwdjy0M+Tuwi" +
                "0g//RxkvokVy+EnneQPXtp7k551vHhdTGCL5OitjfMGqyUxroEf7Ki6iERqIFfCbir5uI34EvKZVctKxjqqMb6TYpzqYwxfBSkmYAy2r+5t7n5Jkhoko9T" +
                "wQhK3On2YRLAZVZK22KtR3VkeRDE7E+j7OYal5BKCpZjJasm+EiUfLrD0/gCmneiIK8CRyQPviD+6oIi77uHCHzyjlbCrW7iC8rmK7/4JeHVwsRfMSgxGh" +
                "bZWkqyZEFasa1PPkQjAgGLOPvWQb6RdCW1Ud+0sAlKfw6db/Vw6euPjXFgQfbO5pIk9A7XGdeMZ3A7bKGnbjelOrR/8Al6U2sq8ouNN0vad3U4aqJn+BC1" +
                "NwIWqgfOc3RORSk58bctSA2lr8kzfM2Oi0w1Q7b5oSIUTO3UpP55kTqbitgg4wcMiuUVo+lvHJniqTZwpFNNBayyJWJeu7W85WGolWXrjZTWsjO5olRpwa" +
                "JYoVfhtnmo8Otox7ICJOxXsS/0yU+1rsxkzfd0liUrtbbw7Xa8DwfbhfRCFgbk+w+ASTEehN3iEvTTQGUNq6bJaLwtTp/2d0jufjQm8s1f8cn37edlJFxLcoZb/DN6R/Vzl1poMRkxw";
        byte[] byteSample = bigSample.getBytes();
        BigInteger bigStuff = new BigInteger(byteSample);
        long before = System.nanoTime();
        System.out.printf("-> Prime ? %b\n", isPrime(bigStuff));
        long after = System.nanoTime();
        System.out.printf("Processed in %s \u00e5Sec\n", NumberFormat.getInstance().format(after - before));

        String bigBigStuff = "7757261088483434418914714320070393839829142425002032208212945699153839390763109004817" +
                "26303745510397167132647209504746332167156645006916802067662588185307820465670603094012152135578835" +
                "02024479642274690750473975217366118323219882368706934443420372862670143613975149367011713703446016" +
                "55875052811532843318999443897296217632082622564961277629140577288402666095962864746296520335751004" +
                "68548622569291561214086277327872007749265885618454743386377939436224025602634049062631452573056538" +
                "96162741962485113180979703425450356204501804093928390780085117508173708739458124524985817728307954" +
                "16254934613487622301636970094124693090008422222320282901971971046043452570664956635385434414254440" +
                "50583042296479846862127931436014950030062760551639353663703312026336019542441329833114484875183298" +
                "34953280935771019385138188706143428630598022813440931444005935025075408969965731956052690540951300" +
                "383723611409394217676635543621580565025456323899418949866048280942136741415200462675504759483686543" +
                "301626481522812452633057241536321705023665965492449308903358029160668956098497472957906558497784768" +
                "272010191851837968106361246206495563636193004929751488763914625172168012842213203773516747363658616" +
                "545931554505001754906577024887844000748540982319678838929777228318036642571633332296309520964797090" +
                "858360287886700029928856041241480818328238194123421455418569395429450407717929423452675316387776973" +
                "000643536960712393331206562106841733820995781488788139986852731987321813060563804431392836301556356" +
                "877104876035670012567228496101843615022806855174701278369591251714597301769287780091437608782774570" +
                "500542448935239203450262829938207769341136868436038842950124766234366555183945327480808885704050608" +
                "1988830310381504571891795794488520480182162926737302470193";
        System.out.printf("%d digits\n", bigBigStuff.length());
        BigInteger anotherBigOne = new BigInteger(bigBigStuff);
        before = System.nanoTime();
        System.out.printf("==> Prime ? %b\n", anotherBigOne.isProbablePrime(1));
        after = System.nanoTime();
        System.out.printf("isProbablePrime, processed in %s \u00e5Sec\n", NumberFormat.getInstance().format(after - before));

        before = System.nanoTime();
        System.out.printf("==> Prime ? %b\n", isPrime(anotherBigOne));
        after = System.nanoTime();
        long elapsed = after - before;
        System.out.println(String.format("=> Elapsed: %s", fmtDHMS(msToHMS(elapsed / 1_000_000))));
        System.out.printf("isPrime, processed in %s \u00e5Sec\n", NumberFormat.getInstance().format(elapsed));
    }
}
