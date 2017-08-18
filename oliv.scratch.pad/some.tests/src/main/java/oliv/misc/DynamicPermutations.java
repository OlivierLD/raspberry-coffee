package oliv.misc;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DynamicPermutations {

	private static String[] listOfN = new String[]{"A", "B", "C", "D", "E", "F", "G", "H"}; // No duplicates
	private static int nbPerm = 0;

	private static Map<String, Integer> permMap = new HashMap<>();

	private static void generate(int permSize) {
		int limit = listOfN.length - (permSize - 1);
		for (int i = 0; i < limit; i++) {
			List<Integer> idx = new ArrayList<>();
			idx.add(i);
			recurse(idx, 1, permSize);
		}
	}

	private static void recurse(List<Integer> idx, int rnk, int permSize) {
		if (rnk < permSize) {
			int limit = listOfN.length - (permSize - 1) + rnk;
			for (int i = idx.get(idx.size() - 1) + 1; i < limit; i++) {
				List<Integer> indexes = new ArrayList<>();
				indexes.addAll(idx);
				indexes.add(i);
				recurse(indexes, rnk + 1, permSize);
			}
		} else {
			System.out.println("Rnk:" + rnk + ", list:" + idx.stream().map(i -> listOfN[i]).collect(Collectors.joining(",")));
			List<List<Integer>> perms = generatePerm(idx);
			perms.stream().forEach(list -> {
				nbPerm++;
				String perm = list.stream().map(i -> listOfN[i]).collect(Collectors.joining(","));
				putAndCount(permMap, perm);
				System.out.println("  -> permuted: " + perm);
			});
		}
	}

	private static void putAndCount(Map<String, Integer> map, String element) {
		Integer counter = map.get(element);
		if (counter == null) {
			map.put(element, 1);
		} else {
			map.put(element, counter + 1);
		}
	}

	public static <E> List<List<E>> generatePerm(List<E> original) {
		if (original.size() == 0) {
			List<List<E>> result = new ArrayList<List<E>>();
			result.add(new ArrayList<E>());
			return result;
		}
		E firstElement = original.remove(0);
		List<List<E>> returnValue = new ArrayList<List<E>>();
		List<List<E>> permutations = generatePerm(original);
		for (List<E> smallerPermutated : permutations) {
			for (int index = 0; index <= smallerPermutated.size(); index++) {
				List<E> temp = new ArrayList<E>(smallerPermutated);
				temp.add(index, firstElement);
				returnValue.add(temp);
			}
		}
		return returnValue;
	}

	public static void main(String... args) {
		System.out.println("Generating permutations for " + Arrays.asList(listOfN).stream().collect(Collectors.joining(",")));
		for (int i = 2; i <= listOfN.length; i++) {
			generate(i);
		}
		System.out.println(String.format("%s permutations.", NumberFormat.getInstance().format(nbPerm)));
		System.out.println(String.format("Map has %s entries.", NumberFormat.getInstance().format(permMap.size())));

		long moreThanOne = permMap.keySet().stream().filter(key -> permMap.get(key) > 1).count();
		System.out.println("More than 1: " + moreThanOne);
	}
}
