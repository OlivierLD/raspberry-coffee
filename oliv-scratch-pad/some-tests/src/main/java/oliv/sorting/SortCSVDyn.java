package oliv.sorting;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * There is one selector (aka filter) per column (dummy ones, possibly overridden)
 * There is only one sort possible for now. (ASC or DESC)
 */
public class SortCSVDyn {

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
        public FilterDescriptor(int col, FilterType ft, String prmOne) {
            this(col, ft, prmOne, null);
        }
        public FilterDescriptor(int col, FilterType ft, String prmOne, String prmTwo) {
            this.columnIdx = col;
            this.filter = ft;
            this.prmOne = prmOne;
            this.prmTwo = prmTwo;
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

    public SortCSVDyn(DataType[] descriptors) {
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

    public void setFilter(int colNum, FilterType type, String prm1, String prm2) {
        Predicate<List<String>> columnFilter = columns -> {
            String col = columns.get(colNum);
            boolean keepIt = true;
            if (lineDescriptors[colNum].equals(DataType.NUMBER)) {
                Double curVal = Double.parseDouble(col);
                if (type.equals(FilterType.EQ)) {
                    keepIt = curVal.equals(Double.parseDouble(prm1));
                } else if (type.equals(FilterType.GT)) {
                    keepIt = curVal > Double.parseDouble(prm1);
                } else if (type.equals(FilterType.LT)) {
                    keepIt = curVal < Double.parseDouble(prm1);
                } else if (type.equals(FilterType.GE)) {
                    keepIt = curVal >= Double.parseDouble(prm1);
                } else if (type.equals(FilterType.LE)) {
                    keepIt = curVal <= Double.parseDouble(prm1);
                } else if (type.equals(FilterType.BETWEEN)) {
                    keepIt = curVal >= Double.parseDouble(prm1) && curVal <= Double.parseDouble(prm2);
                }
            } else if (lineDescriptors[colNum].equals(DataType.STRING) || lineDescriptors[colNum].equals(DataType.BOOLEAN)) {
                String curVal = col;
                if (type.equals(FilterType.EQUALS)) {
                    keepIt = curVal.equals(prm1);
                } else if (type.equals(FilterType.EQUALS_IC)) {
                    keepIt = curVal.equalsIgnoreCase(prm1);
                } else if (type.equals(FilterType.MATCHES)) {
                    keepIt = curVal.matches(prm1);
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
            this.setFilter(filters[i].columnIdx, filters[i].filter, filters[i].prmOne, filters[i].prmTwo);
        }
        for (int i=0; i<sorters.length; i++) {
            this.setSorter(sorters[i].columnIdx, sorters[i].sort);
        }

        List<List<String>> filtered = this.targetData;

        for (int i=0; i<this.customFilters.size(); i++) {
            filtered = filtered.stream()
                    .filter(this.customFilters.get(i))
                    .collect(Collectors.toList());
        }

//        List<List<String>> filtered = this.targetData
//                .stream()
//                .filter(this.customFilters.get(0))   // TODO add filters dynamically, or one filter with all conditions?
//                .filter(this.customFilters.get(1))
//                .filter(this.customFilters.get(2))
//                .filter(this.customFilters.get(3))
//                .filter(this.customFilters.get(4))
//                .collect(Collectors.toList());

        final List<List<String>> fFiltered = filtered;
        this.customSorters.forEach(sorter -> Collections.sort(fFiltered, sorter));

        return fFiltered;
    }

    private final static String[] csvLines = {
            "123;ZZZ;ABC;0.00001;true;",
            "234;AAA;XYZ;0.00002;true;",
            "456;MMM;BCD;- 0.00001;false;",
            "345;ABC;mno;0.00023;false;",
            "345;ABC;pqr;0.00001;true;",
            "345;ABC;abc;0.00010;true;",
            "260;ABC;bcd;0.00001;false;",
            "250;ABC;vwx;0.00123;true;",
            "345;ABC;wxy;0.00001;true;"
    };

    static FilterDescriptor[] filters = {
            new FilterDescriptor(0, FilterType.BETWEEN, String.valueOf(234), String.valueOf(345)) ,
            new FilterDescriptor(4, FilterType.EQUALS_IC, String.valueOf(true)),
            new FilterDescriptor(2, FilterType.MATCHES, ".*[BCD|xyz].*")
    };

    static SortDescriptor[] sorters = {
            new SortDescriptor(3, SortType.ASC)
//                new SortDescriptor(0, SortType.ASC)
    };

    static DataType[] lineDesc = {
            DataType.NUMBER,       // 1st column
            DataType.STRING,       // 2nd column
            DataType.STRING,       // 3rd column
            DataType.NUMBER,       // 4th column
            DataType.BOOLEAN       // 5th column
    };

    public static void main(String... args) {

        SortCSVDyn sorter = new SortCSVDyn(lineDesc);

        List<List<String>> csvList = Arrays.asList(csvLines)
                .stream()
                .map(line -> Arrays.asList(line.split(";")))
                .collect(Collectors.toList());

        sorter.setTargetData(csvList);

        System.out.println("-- Raw Data --");
        Arrays.asList(csvLines).stream().forEach(System.out::println);

        List<List<String>> filtered = sorter.selectAndSort(filters, sorters);
        System.out.println("-- Filtered and Sorted --");
        filtered.stream()
                .forEach(strings -> System.out.println(strings
                        .stream()
                        .collect(Collectors.joining(" - "))));

        System.out.println("\nDone!");
    }
}
