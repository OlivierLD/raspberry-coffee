package oliv.regexp;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class NonAplha {
	private final static String NON_ALPHA_CHARACTERS_PATTERN = "[^a-zA-Z0-9_]";
	private final static Pattern NA_PATTERN = Pattern.compile(NON_ALPHA_CHARACTERS_PATTERN);

	public static void main(String... args) {
		String val = "Akeu coucou, et #%$ -^+=! Ca rigole.";
		String original = val;
		Matcher matcher = NA_PATTERN.matcher(val);
		Set<String> matches = new HashSet<>();
		while (matcher.find()) {
			String match = matcher.group();
			System.out.println(String.format("Found [%s] matches in [%s]", match, val));
			matches.add(match);
		}
		for (String m : matches) {
			System.out.println(String.format("Replacing [%s]", m));
			int ascii = (int) m.charAt(0);
			String replace = Integer.toHexString(ascii).toUpperCase();
			try {
				CharSequence cs = new String(new char[]{m.charAt(0)}); // this way, 1st prm is NOT a regexp.
				val = val.replace(cs, replace);
			} catch (PatternSyntaxException pse) {
				pse.printStackTrace();
			}
			System.out.println(String.format("Replaced [%s] with [%s] => [%s]", m, replace, val));
		}
		System.out.println(String.format("Old string [%s]", original));
		System.out.println(String.format("New string [%s]", val));
	}
}
