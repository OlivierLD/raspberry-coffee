package oliv.misc;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class PermGen4Tests {

	private List<List<String>> permutationList;
	private BufferedWriter output;

	enum GenerationOption {
		GENERATE_IN_MEMORY_LIST,
		WRITE_TO_FILE
	}

	private long nbPerm = 0L;

	public long getNbPerm() {
		return this.nbPerm;
	}

	private List<List<String>> generate(List<String> listOfN, GenerationOption option) {
		return generate(listOfN, option, listOfN.size());
	}

	private List<List<String>> generate(List<String> listOfN, GenerationOption option, int maxPermSize) {
		permutationList = new ArrayList<>(); // Reset
		nbPerm = 0L;
		if (GenerationOption.WRITE_TO_FILE.equals(option)) {
			try {
				output = new BufferedWriter(new FileWriter("out.txt"));
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
		for (int i=2; i<=Math.min(maxPermSize, listOfN.size()); i++) {
			this.generateForSize(option, listOfN, i);
		}
		if (GenerationOption.WRITE_TO_FILE.equals(option)) {
			try {
				output.close();
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
		return permutationList;
	}

	private void generateForSize(GenerationOption option, List<String> listOfN, int permSize) {
		int limit = listOfN.size() - (permSize - 1);
		for (int i=0; i<limit; i++) {
			List<Integer> idx = new ArrayList<>();
			idx.add(i);
			recurse(option, listOfN, idx, 1, permSize);
		}
	}

	private void recurse(GenerationOption option, List<String> listOfN, List<Integer> idx, int rnk, int permSize) {
		if (rnk < permSize) {
			int limit = listOfN.size() - (permSize - 1) + rnk;
			for (int i=idx.get(idx.size() - 1) + 1; i<limit; i++) {
				List<Integer> indexes = new ArrayList<>();
				indexes.addAll(idx);
				indexes.add(i);
				recurse(option, listOfN, indexes, rnk + 1, permSize);
			}
		} else{
			List<List<Integer>> perms = generatePerm(idx);
			perms.stream().forEach(list -> {
				nbPerm++;
				List<String> onePerm = new ArrayList<>();
				list.stream().forEach(i -> onePerm.add(listOfN.get(i)));
				if (GenerationOption.WRITE_TO_FILE.equals(option)) {
					try {
						String line = onePerm.stream().collect(Collectors.joining(", ")) + "\n";
						output.write(line);
					} catch (IOException ioe) {
						ioe.printStackTrace();
					}
				} else if (GenerationOption.GENERATE_IN_MEMORY_LIST.equals(option)) {
					permutationList.add(onePerm);
				}
			});
		}
	}

	public static <E> List<List<E>> generatePerm(List<E> original) {
		if (original.size() == 0) {
			List<List<E>> result = new ArrayList<List<E>>();
			result.add(new ArrayList<E>());
			return result;
		}
		E firstElement = original.remove(0);
		List<List<E>> listValue = new ArrayList<List<E>>();
		List<List<E>> permutations = generatePerm(original);
		for (List<E> smallerPermutated : permutations) {
			for (int i=0; i <= smallerPermutated.size(); i++) {
				List<E> temp = new ArrayList<E>(smallerPermutated);
				temp.add(i, firstElement);
				listValue.add(temp);
			}
		}
		return listValue;
	}

	public static void main(String... args) {

		String[] listOfN = new String[] {
						"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M",
						"N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z",
						"a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m",
						"n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z",
						"1", "2", "3", "4", "5", "6", "7", "8", "9", "0"
		}; // No duplicates

		int maxPermSize = 5;
		boolean csv = true;

		if (csv) {
			System.out.println("Max Perm length;List Size;Nb Perm;Elapsed ms");
		}

		for (int i=8; i<=listOfN.length; i++) {
			List<String> elmtList = Arrays.asList(listOfN).subList(0, i);
//		System.out.println("Generating permutations for " + elmtList.stream().collect(Collectors.joining(", ")));
			PermGen4Tests dp = new PermGen4Tests();

			GenerationOption genOpt = GenerationOption.WRITE_TO_FILE;

			long before = System.currentTimeMillis();
			List<List<String>> permutations = dp.generate(elmtList, genOpt, maxPermSize);
			long after = System.currentTimeMillis();

			long nbPerm = (genOpt == GenerationOption.GENERATE_IN_MEMORY_LIST ? permutations.size() : dp.getNbPerm());
			if (csv) {
				System.out.println(String.format("%d;%d;%d;%d", maxPermSize, elmtList.size(), nbPerm, (after - before)));
			} else {
				System.out.println(String.format("On %d objects, %s permutations, calculated in %s ms.",
								elmtList.size(),
								NumberFormat.getInstance().format(nbPerm),
								NumberFormat.getInstance().format(after - before)));
			}
//		permutations.stream().forEach(permutation -> System.out.println(permutation.stream().collect(Collectors.joining(", "))));
		}
		if (!csv) {
			System.out.println("Done.");
		}
	}
}
