package oliv.misc;

public class Permutations {

	private static String[] listOfN = new String[]{"A", "B", "C", "D", "E", "F", "G", "H"}; // No duplicates

	private static void printCombinations() {
		int i, j, k;
		int len = listOfN.length;

		for (i = 0; i < len - 2; i++) {
			for (j = i + 1; j < len - 1; j++) {
				for (k = j + 1; k < len; k++) {
					System.out.println(String.format("%s%s%s", listOfN[i], listOfN[j], listOfN[k]));
				}
			}
		}
	}

	public static void main(String... args) {
		printCombinations();
	}
}
