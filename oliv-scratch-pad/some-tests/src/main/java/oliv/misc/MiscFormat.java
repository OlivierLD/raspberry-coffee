package oliv.misc;

import java.text.DecimalFormat;
import java.text.NumberFormat;

public class MiscFormat {
	public static void main(String... args) {
		NumberFormat nf = new DecimalFormat("##.##");

		double d = 12.345678;
		System.out.println("---- For " + d + " ----");
		System.out.println(String.format("%+.2f, %+.02f, %f, %s", d, d, d, d));
		System.out.println(String.format("With sign: %s%s", (d > 0 ? "+" : ""), d));
		System.out.println(String.format("NumberFormat %s", nf.format(d)));
		System.out.println(String.format("String.valueOf %s", String.valueOf(d)));

		d = 12d;
		System.out.println("---- For " + d + " ----");
		System.out.println(String.format("%+.2f, %+.02f, %f, %s", d, d, d, d));
		System.out.println(String.format("With sign: %s%s", (d > 0 ? "+" : ""), d));
		System.out.println(String.format("NumberFormat %s", nf.format(d)));
		System.out.println(String.format("String.valueOf %s", String.valueOf(d)));

		d = -12.23;
		System.out.println("---- For " + d + " ----");
		System.out.println(String.format("%+.2f, %+.02f, %f, %s", d, d, d, d));
		System.out.println(String.format("With sign: %s%s", (d > 0 ? "+" : ""), d));
		System.out.println(String.format("NumberFormat %s", nf.format(d)));
		System.out.println(String.format("String.valueOf %s", String.valueOf(d)));

		d = 12.345678901234567890;
		System.out.println("---- For " + d + " ----");
		System.out.println(String.format("%+.2f, %+.02f, %f, %s", d, d, d, d));
		System.out.println(String.format("With sign: %s%s", (d > 0 ? "+" : ""), d));
		System.out.println(String.format("NumberFormat %s", nf.format(d)));
		System.out.println(String.format("String.valueOf %s", String.valueOf(d)));
	}
}
