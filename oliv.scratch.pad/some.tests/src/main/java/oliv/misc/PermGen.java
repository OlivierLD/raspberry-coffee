package oliv.misc;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class PermGen {

	private List<List<String>> permutationList;

	private List<List<String>> generate(List<String> listOfN) {
		return generate(listOfN, listOfN.size());
	}

	private List<List<String>> generate(List<String> listOfN, int maxPermSize) {
		permutationList = new ArrayList<>(); // Reset
		for (int i = 2; i <= Math.min(maxPermSize, listOfN.size()); i++) {
			this.generateForSize(listOfN, i);
		}
		return permutationList;
	}

	private void generateForSize(List<String> listOfN, int permSize) {
		int limit = listOfN.size() - (permSize - 1);
		for (int i = 0; i < limit; i++) {
			List<Integer> idx = new ArrayList<>();
			idx.add(i);
			recurse(listOfN, idx, 1, permSize);
		}
	}

	private void recurse(List<String> listOfN, List<Integer> idx, int rnk, int permSize) {
		if (rnk < permSize) {
			int limit = listOfN.size() - (permSize - 1) + rnk;
			for (int i = idx.get(idx.size() - 1) + 1; i < limit; i++) {
				List<Integer> indexes = new ArrayList<>();
				indexes.addAll(idx);
				indexes.add(i);
				recurse(listOfN, indexes, rnk + 1, permSize);
			}
		} else {
			List<List<Integer>> perms = generatePerm(idx);
			perms.stream().forEach(list -> {
				List<String> onePerm = new ArrayList<>();
				list.stream().forEach(i -> onePerm.add(listOfN.get(i)));
				permutationList.add(onePerm);
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

		String[] listOfN = new String[]{
				"Segment",
				"Social Data Source",
				"Social Data Source and Unit",
				"Segment Groups",
				"Segment Groups Hidden",
				"Timeperiod with Reporting Date",
				"Timeperiod (Analytics)",
				"Timeperiod with Analytics Only",
				"Timeperiod (Hidden)",
				"Timeperiod",
				"Timeperiod (without Reporting Date)",
				"Timeperiod (No Custom Timeperiods)",
				"Timeperiod (Invitations)",
				"Reporting Date",
				"Reporting Date (Hidden)",
				"Reporting Date (with Label)",
				"Segment Filter",
				"Segment Filter 2",
				"Segment Filter (Invitations)",
				"Segment Filter 2 (Invitations)",
				"Custom Filter",
				"Custom Query",
				"Search",
				"Search (Response Investigatior)",
				"Question",
				"Question",
				"Question (No Social)",
				"Question (Social Only)",
				"Questions",
				"Questions (Ranker Snapshot)",
				"Questions (Score Table)",
				"Questions (Analytics)",
				"Questions (Social)",
				"Score",
				"Score (No Social)",
				"Alert",
				"Columns",
				"Columns (Profiler)",
				"Benchmark",
				"Calculation",
				"Calculation (Text Analytics)",
				"Crosstab Columns",
				"Crosstab Rows",
				"Comment Field",
				"Comment Field (Tag and Field Strict)",
				"Comment Field (Tag and Field)",
				"Comment Field (Strict)",
				"Category Filter",
				"Category Filter 2",
				"Comment Filter (Topic Investigator 2)",
				"Topic Rank",
				"Topic Group",
				"Segment Ranker",
				"Segment Ranker",
				"Social Media Filter",
				"Social Media Filter (Surveys and Social)",
				"Social Media Filter (Social Only)",
				"Customer Filter",
				"Action Planner Outcome",
				"Action Planner Display",
				"Ask Now",
				"Category Filter",
				"Category Filter 2",
				"Category Filter (Strict)",
				"Category Filter (No Sentiment)",
				"Category Filter (No Sentiment, Strict)",
				"Topic Rank",
				"Topic Group"
		}; // No duplicates

		int maxPermSize = 5;

		for (int i = 8; i < listOfN.length; i++) {
			List<String> elmtList = Arrays.asList(listOfN).subList(0, i);
//		System.out.println("Generating permutations for " + elmtList.stream().collect(Collectors.joining(", ")));
			PermGen dp = new PermGen();

			long before = System.currentTimeMillis();
			List<List<String>> permutations = dp.generate(elmtList, maxPermSize);
			long after = System.currentTimeMillis();

			System.out.println(String.format("On %d objects, %s permutations, calculated in %s ms.",
					elmtList.size(),
					NumberFormat.getInstance().format(permutations.size()),
					NumberFormat.getInstance().format(after - before)));
//		permutations.stream().forEach(permutation -> System.out.println(permutation.stream().collect(Collectors.joining(", "))));
		}
		System.out.println("Done.");
	}
}
