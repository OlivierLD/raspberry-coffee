package oliv.sorting;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * There is one selector (aka filter) per column (dummy ones, possibly overridden)
 * There is only one sort possible for now. (ASC or DESC)
 */
public class SortCSVDynOnFile {

    enum DataType {
        NUMBER,
        STRING,
        BOOLEAN
    }

    enum SortType {
        ASC,
        DESC
    }

    enum FilterType {
        EQUALS,
        EQUALS_IC,
        MATCHES,
        EQ,
        GT,
        LT,
        GE,
        LE,
        BETWEEN
    }

    private static class FilterDescriptor {
        int columnIdx;
        FilterType filter;
        String prmOne;
        String prmTwo;
        boolean negate = false;
        public FilterDescriptor(int col, FilterType ft, String prmOne) {
            this(col, ft, prmOne, null, false);
        }
        public FilterDescriptor(int col, FilterType ft, String prmOne, boolean neg) {
            this(col, ft, prmOne, null, neg);
        }
        public FilterDescriptor(int col, FilterType ft, String prmOne, String prmTwo) {
            this(col, ft, prmOne, prmTwo, false);
        }
        public FilterDescriptor(int col, FilterType ft, String prmOne, String prmTwo, boolean neg) {
            this.columnIdx = col;
            this.filter = ft;
            this.prmOne = prmOne;
            this.prmTwo = prmTwo;
            this.negate = neg;
        }
    }

    private static class SortDescriptor {
        int columnIdx;
        SortType sort;
        public SortDescriptor(int col, SortType type) {
            this.columnIdx = col;
            this.sort = type;
        }
    }

    private List<List<String>> targetData = null;

    private List<Predicate<List<String>>> customFilters = new ArrayList<>();
    private List<Comparator<List<String>>> customSorters = new ArrayList<>();

    private DataType[] lineDescriptors;

    public SortCSVDynOnFile(DataType[] descriptors) {
        this.lineDescriptors = descriptors;
        this.resetFilters();
    }

    public void resetFilters() {
        while (this.customFilters.size() > 0) {
            this.customFilters.remove(0);
        }
        for (int i=0; i<this.lineDescriptors.length; i++) {
            Predicate<List<String>> columnFilter = strings -> true;
            customFilters.add(columnFilter);
        }
    }

    public void resetSorters() {
        while (this.customSorters.size() > 0) {
            this.customSorters.remove(0);
        }
        for (int i=0; i<this.lineDescriptors.length; i++) {
            Predicate<List<String>> columnFilter = strings -> true;
            customFilters.add(columnFilter);
        }
    }

    public void setFilter(int colNum, FilterType type, String prm1, String prm2, boolean neg) {
        Predicate<List<String>> columnFilter = columns -> {
            String col = columns.get(colNum);
            boolean keepIt = true;
            if (lineDescriptors[colNum].equals(DataType.NUMBER)) {
                try {
                    Double curVal = Double.parseDouble(col);
                    if (type.equals(FilterType.EQ)) {
                        keepIt = neg ? !curVal.equals(Double.parseDouble(prm1)) : curVal.equals(Double.parseDouble(prm1));
                    } else if (type.equals(FilterType.GT)) {
                        keepIt = neg ? !(curVal > Double.parseDouble(prm1)) : curVal > Double.parseDouble(prm1);
                    } else if (type.equals(FilterType.LT)) {
                        keepIt = neg ? !(curVal < Double.parseDouble(prm1)) : curVal < Double.parseDouble(prm1);
                    } else if (type.equals(FilterType.GE)) {
                        keepIt = neg ? !(curVal >= Double.parseDouble(prm1)) : curVal >= Double.parseDouble(prm1);
                    } else if (type.equals(FilterType.LE)) {
                        keepIt = neg ? !(curVal <= Double.parseDouble(prm1)) : curVal <= Double.parseDouble(prm1);
                    } else if (type.equals(FilterType.BETWEEN)) {
                        keepIt = neg ? !(curVal >= Double.parseDouble(prm1) && curVal <= Double.parseDouble(prm2)) :
                                curVal >= Double.parseDouble(prm1) && curVal <= Double.parseDouble(prm2);
                    }
                } catch (NumberFormatException nfe) {
                    // Absorb
                    keepIt = false;
                }
            } else if (lineDescriptors[colNum].equals(DataType.STRING) || lineDescriptors[colNum].equals(DataType.BOOLEAN)) {
                String curVal = col;
                if (type.equals(FilterType.EQUALS)) {
                    keepIt = neg ? !curVal.equals(prm1) : curVal.equals(prm1);
                } else if (type.equals(FilterType.EQUALS_IC)) {
                    keepIt = neg ? !curVal.equalsIgnoreCase(prm1) : curVal.equalsIgnoreCase(prm1);
                } else if (type.equals(FilterType.MATCHES)) {
                    keepIt = neg ? !curVal.matches(prm1) : curVal.matches(prm1);
                }
            }
            return keepIt;
        };
        customFilters.add(colNum, columnFilter);
    }

    public void setSorter(int colNum, SortType type) {
        DataType dataType = this.lineDescriptors[colNum];
        Comparator<List<String>> comp = (o1, o2) -> {
            if (dataType.equals(DataType.NUMBER)) {
                double d1 = Double.parseDouble(o1.get(colNum));
                double d2 = Double.parseDouble(o2.get(colNum));
                int result;
                if (d1 == d2) {
                    result = 0;
                } else if (d1 > d2) {
                    result = 1;
                } else {
                    result = -1;
                }
                return type.equals(SortType.ASC) ? result : -result;
            } else {
                return type.equals(SortType.ASC) ? o1.get(colNum).compareTo(o2.get(colNum)) : o2.get(colNum).compareTo(o1.get(colNum));
            }
        };
        this.customSorters.add(comp);
    }

    public void setTargetData(List<List<String>> data) {
        this.targetData = data;
    }

    public List<List<String>> selectAndSort(FilterDescriptor[] filters, SortDescriptor[] sorters) {

        this.resetFilters();
        this.resetSorters();

        for (int i=0; i<filters.length; i++) {
            this.setFilter(filters[i].columnIdx, filters[i].filter, filters[i].prmOne, filters[i].prmTwo, filters[i].negate);
        }
        for (int i=0; i<sorters.length; i++) {
            this.setSorter(sorters[i].columnIdx, sorters[i].sort);
        }

        List<List<String>> filtered = this.targetData;

        // TODO - ONE filter with all conditions
        for (int i=0; i<this.customFilters.size(); i++) {
            filtered = filtered.stream()
                    .filter(this.customFilters.get(i))
                    .collect(Collectors.toList());
        }

        final List<List<String>> fFiltered = filtered;
        // TODO - Nested Sorts
        this.customSorters.forEach(sorter -> Collections.sort(fFiltered, sorter));

        return fFiltered;
    }

    /**
     * That one streams from the file system
     */
    public static List<List<String>> buildDataList(String fileName, String columnSeparator) throws IOException {
        String line;
        boolean keepReading = true;
        List<List<String>> csvList = new ArrayList<>();
        // try with resources
        try (BufferedReader dataStream = new BufferedReader(new FileReader(fileName))) {
            while (keepReading) {
                line = dataStream.readLine();
                if (line == null) {
                    keepReading = false;
                } else {
                    csvList.add(Arrays.asList(line.split(columnSeparator)));
                }
            }
            return csvList;
        }
    }

    /**
     * Below, the data and parameters for this test.
     */
    static DataType[] lineDesc = {
            DataType.NUMBER, // 0 - Id
            DataType.STRING, // 1 - MSZoning
            DataType.STRING, // 2 - Street
            DataType.STRING, // 3 - Alley
            DataType.STRING, // 4 - LandContour
            DataType.STRING, // 5 - Utilities
            DataType.STRING, // 6 - Neighborhood
            DataType.STRING, // 7 - Condition1
            DataType.STRING, // 8 - HouseStyle
            DataType.NUMBER  // 9 - SalePrice
    };

    static FilterDescriptor[] filters = {
            new FilterDescriptor(9, FilterType.BETWEEN, String.valueOf(80_000), String.valueOf(1_000_000)) ,
//            new FilterDescriptor(3, FilterType.EQUALS_IC, "NA", true),
            new FilterDescriptor(8, FilterType.EQUALS_IC, "2Story"),
            new FilterDescriptor(7, FilterType.EQUALS_IC, "Norm", true) /*,
            new FilterDescriptor(2, FilterType.MATCHES, ".*[BCD|xyz].*") */
    };

    static SortDescriptor[] sorters = {
            new SortDescriptor(9, SortType.ASC)
//                new SortDescriptor(0, SortType.ASC)
    };

    static String DATA_FILE_NAME = "house-prices-datasets/result.csv";
    static String COL_SEPARATOR  = ",";

    public static void main(String... args) throws IOException {

        SortCSVDynOnFile sorter = new SortCSVDynOnFile(lineDesc);

        List<List<String>> csvList = buildDataList(DATA_FILE_NAME, COL_SEPARATOR);
        sorter.setTargetData(csvList);

        System.out.println(String.format("-- Raw Data, limited (%d entries) --", csvList.size()));
        csvList.stream().limit(100).forEach(System.out::println);

        List<List<String>> filtered = sorter.selectAndSort(filters, sorters);
        System.out.println(String.format("\n-- Filtered and Sorted (%d entries) --", filtered.size()));
        filtered.stream()
                .forEach(strings -> System.out.println(strings
                        .stream()
                        .collect(Collectors.joining(" - "))));

        int offset = 10;
        int pageLen = 12;
        System.out.println(String.format("\n-- Paginated (entries %d to %d), Filtered and Sorted (%d entries) --", offset, offset + pageLen, filtered.size()));
        if (offset > filtered.size()) {
            System.out.println(String.format("Offset %s beyond size %s, nothing to show.", offset, filtered.size()));
        } else {
            filtered.stream()
                    .skip(offset)
                    .limit(pageLen)
                    .forEach(strings -> System.out.println(strings
                            .stream()
                            .collect(Collectors.joining(" - "))));
        }
        System.out.println("\nDone!");
    }
}
