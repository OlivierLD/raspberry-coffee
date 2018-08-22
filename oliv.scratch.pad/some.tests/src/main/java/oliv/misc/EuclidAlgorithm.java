package oliv.misc;

public class EuclidAlgorithm {
	private static int greatestCommonDivisor(int u, int v) { // PGCD
		int t;
		while (u > 0) {
			if (u < v) {
				t = u;
				u = v;
				v = t;
			}
			u -= v;
		}
		return v;
	}

	public static void main(String... args) {
		int x, y;
		x = 200;
		y = 300;
		int gcd = greatestCommonDivisor(x, y);
		System.out.println(String.format("GCD(%d, %d) = %d", x, y, gcd));
		System.out.println(String.format("=> %d / %d = %d / %d", x, y, (x / gcd), (y / gcd)));
	}
}
