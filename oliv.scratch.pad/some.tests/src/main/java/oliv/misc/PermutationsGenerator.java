package oliv.misc;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class PermutationsGenerator {

	private List<List<Integer>> permutationList;
	private BufferedWriter output;

	public enum GenerationOption {
		GENERATE_IN_MEMORY_LIST,
		WRITE_TO_FILE
	}

	private long nbPerm = 0L;

	private long getNbPerm() {
		return this.nbPerm;
	}

	private List<List<Integer>> generate(List<Integer> listOfN, GenerationOption option) {
		return generate(listOfN, option, listOfN.size());
	}

	private List<List<Integer>> generate(List<Integer> listOfN, GenerationOption option, int maxPermSize) {
		permutationList = new ArrayList<>(); // Reset
		nbPerm = 0L;
		if (GenerationOption.WRITE_TO_FILE.equals(option)) {
			try {
				output = new BufferedWriter(new FileWriter("out.txt"));
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
		for (int i = 2; i <= Math.min(maxPermSize, listOfN.size()); i++) {
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

	private void generateForSize(GenerationOption option, List<Integer> listOfN, int permSize) {
		int limit = listOfN.size() - (permSize - 1);
		for (int i = 0; i < limit; i++) {
			List<Integer> idx = new ArrayList<>();
			idx.add(i);
			recurse(option, listOfN, idx, 1, permSize);
		}
	}

	private void recurse(GenerationOption option, List<Integer> listOfN, List<Integer> idx, int rnk, int permSize) {
		if (rnk < permSize) {
			int limit = listOfN.size() - (permSize - 1) + rnk;
			for (int i = idx.get(idx.size() - 1) + 1; i < limit; i++) {
				List<Integer> indexes = new ArrayList<>();
				indexes.addAll(idx);
				indexes.add(i);
				recurse(option, listOfN, indexes, rnk + 1, permSize);
			}
		} else {
			List<List<Integer>> perms = generatePermutations(idx);
			perms.stream().forEach(list -> {
				nbPerm++;
				List<Integer> onePerm = new ArrayList<>();
				list.stream().forEach(i -> onePerm.add(listOfN.get(i)));
				if (GenerationOption.WRITE_TO_FILE.equals(option)) {
					try {
						String line = onePerm.stream().map(String::valueOf).collect(Collectors.joining(", ")) + "\n";
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

	private static <E> List<List<E>> generatePermutations(List<E> original) {
		if (original.size() == 0) {
			List<List<E>> result = new ArrayList<List<E>>();
			result.add(new ArrayList<E>());
			return result;
		}
		E firstElement = original.remove(0);
		List<List<E>> listValue = new ArrayList<List<E>>();
		List<List<E>> permutations = generatePermutations(original);
		for (List<E> smallerPermutated : permutations) {
			for (int i = 0; i <= smallerPermutated.size(); i++) {
				List<E> temp = new ArrayList<E>(smallerPermutated);
				temp.add(i, firstElement);
				listValue.add(temp);
			}
		}
		return listValue;
	}

	public static List<List<List<Integer>>> generateAllPermutations(int valueListSize, int maxPermSize, GenerationOption genOpt) {
		if (maxPermSize > valueListSize) {
			throw new RuntimeException(String.format("Max %d > size %d", maxPermSize, valueListSize));
		}

		List<List<List<Integer>>> allPermutations = new ArrayList<>();

		List<Integer> listOfIndexes = new ArrayList<>();
		for (int i = 0; i < valueListSize; i++) {
			listOfIndexes.add(i);
		}

		for (int i = 2; i <= listOfIndexes.size(); i++) {
			List<Integer> elmtList = listOfIndexes.subList(0, i);
			PermutationsGenerator generator = new PermutationsGenerator();
			List<List<Integer>> permutations = generator.generate(elmtList, genOpt, maxPermSize);
			allPermutations.add(permutations);
		}
		return allPermutations;
	}

//	public static void main1(String... args) {
//
//		boolean csv = true;
//
//		if (csv) {
//			System.out.println("Max Perm length;List Size;Nb Perm;Elapsed ms");
//		}
//
//		List<Integer> listOfIndexes = new ArrayList<>();
//		for (int i=0; i<10; i++) {
//			listOfIndexes.add(i);
//		}
//		int maxPermSize = listOfIndexes.size(); // 5;
//
//		for (int i=2; i<=listOfIndexes.size(); i++) {
//			List<Integer> elmtList = listOfIndexes.subList(0, i);
////		System.out.println("Generating permutations for " + elmtList.stream().collect(Collectors.joining(", ")));
//			PermutationsGenerator generator = new PermutationsGenerator();
//
//			GenerationOption genOpt = GenerationOption.GENERATE_IN_MEMORY_LIST;
//
//			long before = System.currentTimeMillis();
//			List<List<Integer>> permutations = generator.generate(elmtList, genOpt, maxPermSize);
//			long after = System.currentTimeMillis();
//
//			long nbPerm = (genOpt == GenerationOption.GENERATE_IN_MEMORY_LIST ? permutations.size() : generator.getNbPerm());
//			if (csv) {
//				System.out.println(String.format("%d;%d;%d;%d", maxPermSize, elmtList.size(), nbPerm, (after - before)));
//			} else {
//				System.out.println(String.format("On %d objects, %s permutations, calculated in %s ms.",
//								elmtList.size(),
//								NumberFormat.getInstance().format(nbPerm),
//								NumberFormat.getInstance().format(after - before)));
//			}
//		}
//		if (!csv) {
//			System.out.println("Done.");
//		}
//	}

	public static void main(String... args) {
		List<List<List<Integer>>> allPermutations = generateAllPermutations(3, 3, GenerationOption.GENERATE_IN_MEMORY_LIST);
		final AtomicInteger upTo = new AtomicInteger(2);
		allPermutations.stream().forEach(one -> {
			System.out.println(String.format("=== Up to %d elements ===", upTo.getAndIncrement()));
			one.stream().forEach(two -> {
				String perm = two.stream().map(String::valueOf).collect(Collectors.joining(", "));
				System.out.println(perm);
			});
		});
	}
}
