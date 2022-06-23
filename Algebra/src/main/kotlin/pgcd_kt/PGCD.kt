package pgcd_kt

import pgcd_kt.PGCD

object PGCD {
    fun pgcd(n1: Int, n2: Int): Int {
        var n1 = n1
        var n2 = n2
        while (n1 != n2) {
            if (n1 > n2) {
                n1 -= n2
            } else {
                n2 -= n1
            }
        }
        return n2
    }

    @JvmStatic
    fun main(args: Array<String>) {
        var n1 = 60
        var n2 = 36
        System.out.printf("PGCD(%d, %d) = %d\n", n1, n2, pgcd(n1, n2))
        n1 = 355
        n2 = 113
        System.out.printf("PGCD(%d, %d) = %d\n", n1, n2, pgcd(n1, n2))
    }
}