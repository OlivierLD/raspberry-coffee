package polarmaker.polars.util;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

public class StaticUtil {
	public static double parseDouble(String str) throws Exception {
		double result = Double.MIN_VALUE;
		try {
			result = Double.parseDouble(str);
		} catch (NumberFormatException nfe) {
			try {
				Number number = NumberFormat.getNumberInstance(Locale.getDefault()).parse(str);
				result = number.doubleValue();
			} catch (ParseException e) {
				throw e;
			}
		}
		return result;
	}

	public static void main(String... args) throws Exception {
		String str = "123,45";
		if (args.length > 0)
			str = args[0];

		System.out.println(parseDouble(str));
	}
}
