package oliv.sorting;

import java.io.*;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * A quick test
 *
 * Filtering and sorting CSV files data
 */
public class SortFilterCSV {

    public SortFilterCSV() {
    }

    private final static String DATA_FILE_NAME = "house-prices-datasets" + File.separator + "train.csv";
    private final static String CSV_SEPARATOR = ",";

    static class CustomFilter<E> implements Predicate<List<String>> {
        @Override
        public boolean test(List<String> columns) {
            boolean keepIt = // columns.get(2).matches(".*[BCD|xyz].*") &&
//                    !columns.get(9).equals("AllPub") &&
                    ! columns.get(10).equals("Inside") &&
                    Double.parseDouble(columns.get(0)) > 300;
            return keepIt;
        }
    }

    /**
     * That one streams from the file system
     */
    public static List<List<String>> buildDataList() throws IOException {
        String line;
        boolean keepReading = true;
        List<List<String>> csvList = new ArrayList<>();
        try (BufferedReader dataStream = new BufferedReader(new FileReader(DATA_FILE_NAME))) {
            while (keepReading) {
                line = dataStream.readLine();
                if (line == null) {
                    keepReading = false;
                } else {
                    csvList.add(Arrays.asList(line.split(CSV_SEPARATOR)));
                }
            }
            return csvList;
        }
    }

    public static void main(String... args) throws Exception {

        List<List<String>> csvDataset = buildDataList();

        int limit = 10;
        System.out.println(String.format("-- RAW (size %d limit %d) --", csvDataset.size(), limit));
        csvDataset.stream().limit(10).forEach(line -> {
            System.out.println(line
                    .stream()
                    .collect(Collectors.joining(" - ")));
        });

        // For fun find distinct values of a given column
        int col = 10;
        List<String> distinctCol = csvDataset
                .stream()
                .map(line -> line.get(col))
                .distinct()
                .collect(Collectors.toList());
        System.out.println(String.format("-- Distinct col %d --", col));
        distinctCol.forEach(System.out::println);
        System.out.println("---------------------");

        // Count line with given value in given column
        final String lookFor = "Inside";
        long count = csvDataset.stream()
                .map(line -> line.get(col))
                .filter(column -> lookFor.equals(column))
                .count();
        System.out.println(String.format("%d lines with column %d with value \"%s\"", count, col, lookFor));
        System.out.println("---------------------");

        Predicate<List<String>> predicate = new CustomFilter<>();
        csvDataset = csvDataset
                .stream()
                .skip(1) // skip descriptors
                .filter(predicate) // Custom filter
                .collect(Collectors.toList());

        System.out.println(String.format("-- List of Lists (filtered, size %d, limit %d) --", csvDataset.size(), limit));
        csvDataset.stream().limit(limit).forEach(line -> {
            System.out.println(line.stream().collect(Collectors.joining(" - ")));
        });

        // Sorting on column 2 (3rd one)
        Comparator<List<String>> comp = Comparator.comparing(csvLine -> csvLine.get(2));
        Collections.sort(csvDataset, comp);
        System.out.println(String.format("-- Sorted List of Lists, on 3rd col (size %d, limit %d) --", csvDataset.size(), limit));
        csvDataset.stream()
                .skip(1) // skip descriptors
                .limit(limit)
                .forEach(line -> {
                    System.out.println(line
                            .stream()
                            .collect(Collectors.joining(" - ")));
                });

        // Sorting, numerical
        Comparator<List<String>> comp2 = Comparator.comparing(csvLine -> Integer.valueOf(csvLine.get(0)));
        Collections.sort(csvDataset, comp2);
        System.out.println(String.format("-- Sorted List of Lists, on 1st col, as int (size %d, limit %d) --", csvDataset.size(), limit));
        csvDataset.stream()
                .skip(1) // skip descriptors
                .limit(limit)
                .forEach(line -> System.out.println(line
                        .stream()
                        .collect(Collectors.joining(" - "))));

    }
}
