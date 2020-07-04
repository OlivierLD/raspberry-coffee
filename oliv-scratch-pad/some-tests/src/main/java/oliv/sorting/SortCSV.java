package oliv.sorting;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SortCSV {

    private final static String[] csvLines = {
            "123;ZZZ;ABC;",
            "234;AAA;XYZ;",
            "456;MMM;BCD;",
            "345;ABC;mno;",
            "345;ABC;pqr;",
            "345;ABC;vwx;",
            "345;ABC;wxy;"
    };

    public SortCSV() {
    }

    static class CustomFilter<E> implements Predicate<List<String>> {
        @Override
        public boolean test(List<String> columns) {
            boolean keepIt = columns.get(2).matches(".*[BCD|xyz].*") &&
                    Double.parseDouble(columns.get(0)) > 300;
            return keepIt;
        }
    }

    public static void main(String... args) {
        List<List<String>> csvList;
        System.out.println("-- RAW --");
        Arrays.asList(csvLines).stream().forEach(System.out::println);

        if (false) {
            csvList = Arrays.asList(csvLines)
                    .stream()
                    .map(line -> Arrays.asList(line.split(";")))
                    .filter(columns -> columns.get(2).matches(".*[BCD|xyz].*")) // Regex filter on 3nd column
                    .filter(columns -> Double.parseDouble(columns.get(0)) > 300) // Numerical filter on 1st column
                    .collect(Collectors.toList());
        } else {
            Predicate<List<String>> predicate = new CustomFilter<>();
            csvList = Arrays.asList(csvLines)
                    .stream()
                    .map(line -> Arrays.asList(line.split(";")))
                    .filter(cols -> predicate.test(cols)) // Custom filter
                    .collect(Collectors.toList());
        }
        System.out.println("-- List of Lists (filtered) --");
        csvList.stream().forEach(line -> {
            System.out.println(line.stream().collect(Collectors.joining(" - ")));
        });

        // Sorting on column 2 (3rd one)
        Comparator<List<String>> comp = Comparator.comparing(csvLine -> csvLine.get(2));
        Collections.sort(csvList, comp);
        System.out.println("-- Sorted List of Lists, on 3rd col --");
        csvList.stream().forEach(line -> {
            System.out.println(line
                    .stream()
                    .collect(Collectors.joining(" - ")));
        });

        Comparator<List<String>> comp2 = Comparator.comparing(csvLine -> Integer.valueOf(csvLine.get(0)));
        Collections.sort(csvList, comp2);
        System.out.println("-- Sorted List of Lists, on 1st col (as int) --");
        csvList.stream().forEach(line -> {
            System.out.println(line
                    .stream()
                    .collect(Collectors.joining(" - ")));
        });

    }
}
