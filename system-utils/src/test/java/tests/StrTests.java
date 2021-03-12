package tests;

public class StrTests {
	public static void main(String... args) {
		String payload = "Akeu Coucou\r\n";
		payload = payload.trim();
		while (payload.endsWith("\n") || payload.endsWith("\r")) {
			payload = payload.substring(payload.length() - 1);
		}
		System.out.println(payload);

		payload = "Akeu | Coucou | Ta mere";
		String[] sa = payload.split("\\|");
		for (String s : sa) {
			System.out.println(s.trim());
		}
	}
}
