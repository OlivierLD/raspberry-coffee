package scim;

import java.io.File;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Using SCIM syntax on a JSON file (curationFunctions.json)
 * As it is now, just an exercise...
 * <p>
 * SCIM "spec": https://ldapwiki.com/wiki/SCIM%20Filtering
 * <p>
 * Parsing an expression: https://unnikked.ga/how-to-build-a-boolean-expression-evaluator-518e9e068a65,
 * and https://github.com/unnikkedga/BooleanExpressionEvaluator
 */
public class ScimParser {

    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * @param value quoted - or un-quoted - string. With simple or double quote, or none.
     * @return The string without the quotes, whatever they were.
     */
    private static String dropQuotes(String value) {
        if ((value.startsWith("\"") && value.endsWith("\"")) ||
                (value.startsWith("'") && value.endsWith("'"))) {
            value = value.substring(1, value.length() - 1);
        }
        return value;
    }


    public enum SupportedSuffix {
        NOT_EQUAL_TO("NotEqualTo", "string"),
        GE("GreaterThanOrEqualTo", "number"),
        GT("GreaterThan", "number"),
        LE("LowerThanOrEqualTo", "number"),
        LT("LowerThan", "number");

        private final String suffix;
        private final String applyToType;

        SupportedSuffix(String suffix, String type) {
            this.suffix = suffix;
            this.applyToType = type;
        }

        public String suffix() {
            return this.suffix;
        }

        public String applyToType() {
            return this.applyToType;
        }

        public static SupportedSuffix getFromSuffix(String suff) {
            for (SupportedSuffix suffix : SupportedSuffix.values()) {
                if (suffix.suffix().equals(suff)) {
                    return suffix;
                }
            }
            return null;
        }

        public static SupportedSuffix getSuffix(String colName) {
            for (SupportedSuffix suffix : SupportedSuffix.values()) {
                if (colName.endsWith(suffix.suffix())) {
                    return suffix;
                }
            }
            return null;
        }
    }

    /**
     * @param op the lowercase operator
     * @return the corresponding member of the {@link OneExpression.SCIMOperators} enum.
     * @Deprecated Use {@link OneExpression.SCIMOperators#getFromOp(String)}
     */
    private static OneExpression.SCIMOperators findOp(String op) {
        return Arrays.stream(OneExpression.SCIMOperators.values())
                .filter(scimOp -> scimOp.op().equals(op))
                .findFirst().orElse(null);
    }

    /**
     * @param expression like 'field co value' for field contains value
     * @return the expected bean, populated.
     */
    private static OneExpression parseOne(String expression) {
        String[] fieldOpValue = expression.split(" ");
        String operator = fieldOpValue[1].trim();
        String field = fieldOpValue[0].trim();
        String value = null;
        if (fieldOpValue.length > 2) {
            StringBuilder sb = new StringBuilder();
            sb.append(fieldOpValue[2]);
            // case of 'field co "This is a String"'
            for (int i = 3; i < fieldOpValue.length; i++) {
                sb.append(String.format(" %s", fieldOpValue[i]));
            }
            value = dropQuotes(sb.toString().trim());
        }
        return OneExpression
                .builder()
                .field(field)
                .op(operator)
                .value(value)
                .build();
    }

    /**
     * @param origin    the json object, represented as a Map
     * @param fieldPath dot-separated path, like levelOne.levelTwo.levelThree
     * @return the target, if found. null otherwise.
     */
    private static Object drillToValue(Map<String, Object> origin, String fieldPath) {
        String[] path = fieldPath.split("\\.");
        Object nextMap = origin;
        Object result = null;
        for (String next : path) {
            if (nextMap instanceof Map) {
                result = ((Map<String, Object>) nextMap).get(next);
                if (result != null) {
                    nextMap = result; // Keep moving!
                } else {
                    break;
                }
            } else {
                break;
            }
        }
        return result;
    }

    /**
     * @param obj  the object to evaluate the expression on
     * @param expr the expression to use, as parsed
     * @return true or false, if the condition expressed by the 'expr' is met or not.
     */
    private static boolean evaluate(Map<String, Object> obj, OneExpression expr) {
        if (expr.getOp().equals(OneExpression.SCIMOperators.CO)) {
            return String.valueOf(drillToValue(obj, expr.getField())).contains(expr.getValue());
        } else if (expr.getOp().equals(OneExpression.SCIMOperators.EQ)) {
            return String.valueOf(drillToValue(obj, expr.getField())).equals(expr.getValue());
        } else if (expr.getOp().equals(OneExpression.SCIMOperators.NE)) {
            return !String.valueOf(drillToValue(obj, expr.getField())).equals(expr.getValue());
        } else if (expr.getOp().equals(OneExpression.SCIMOperators.SW)) {
            return String.valueOf(drillToValue(obj, expr.getField())).startsWith(expr.getValue());
        } else if (expr.getOp().equals(OneExpression.SCIMOperators.PR)) {
            return drillToValue(obj, expr.getField()) != null;
        } else { // Other operators... on Dates or Numbers
            Object object = drillToValue(obj, expr.getField());
            if (object != null) {
                String field = String.valueOf(object);
                try {
                    double fieldValue = Double.parseDouble(field);
                    double refValue = Double.parseDouble(expr.getValue());
                    if (expr.getOp().equals(OneExpression.SCIMOperators.LT)) {
                        return fieldValue < refValue;
                    } else if (expr.getOp().equals(OneExpression.SCIMOperators.LE)) {
                        return fieldValue <= refValue;
                    } else if (expr.getOp().equals(OneExpression.SCIMOperators.GT)) {
                        return fieldValue > refValue;
                    } else if (expr.getOp().equals(OneExpression.SCIMOperators.GE)) {
                        return fieldValue >= refValue;
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    // TODO Is that a date?

                }
            }
            return false;
        }
    }

    /**
     * The high level method, the one to call.
     *
     * @param list   the list to filter
     * @param filter the list of expression to apply to filter.
     * @return The filtered list.
     */
    private static List<Map<String, Object>> filter(List<Map<String, Object>> list, String filter) {
        String[] expression = filter.split(" and "); // Only AND for now...
        List<OneExpression> filters = new ArrayList<>();

        Arrays.asList(expression).forEach(exp -> filters.add(parseOne(exp)));
        if (true) { // Verbose
            System.out.println("---- [Filter parsed expressions] ----");
            filters.forEach(exp -> System.out.println(String.format("[%s]", exp.toString())));
            System.out.println("-------------------------------------");
        }

        return list.stream().filter(oneItem -> {
            boolean ok = true;
            for (OneExpression expr : filters) {
                ok &= evaluate(oneItem, expr);  // AND
            }
            return ok;
        }).collect(Collectors.toList());
    }

    private final static Map<String, String> OIC_COL_TO_PATH = new HashMap<>();

    static {
        //                   name,  path
        // For the OCI option, curationFunctions.json
        OIC_COL_TO_PATH.put("category", "categories");
        OIC_COL_TO_PATH.put("title", "title");
        OIC_COL_TO_PATH.put("columnFunction", "columnFunction");
        OIC_COL_TO_PATH.put("left", "parameters.properties.left.type");
        OIC_COL_TO_PATH.put("right", "parameters.properties.right.type");
        OIC_COL_TO_PATH.put("additionalProperties", "parameters.schema.additionalProperties");
        // For the OCI Option, bagnoles.json. That one is a flat structure. TODO See the categories (Array)
        OIC_COL_TO_PATH.put("make", "make");
        OIC_COL_TO_PATH.put("model", "model");
        OIC_COL_TO_PATH.put("power", "power");
        OIC_COL_TO_PATH.put("categories", "categories");
        OIC_COL_TO_PATH.put("second-hand", "second-hand");
        OIC_COL_TO_PATH.put("price", "price");
    }

    private static List<Map<String, Object>> filter(List<Map<String, Object>> list, List<String> filters, Map<String, String> colToPath) {

        List<String> yesFilters = new ArrayList<>();
        List<String> noFilters = new ArrayList<>();
        if (filters != null) {
            filters.forEach(filter -> {
                String[] nv = filter.split("=");
                SupportedSuffix suffix = SupportedSuffix.getSuffix(nv[0]);
                if (suffix != null && suffix.equals(SupportedSuffix.NOT_EQUAL_TO)) {
                    noFilters.add(filter);
                } else {
                    yesFilters.add(filter);
                }
            });
        }

        Map<String, List<String>> yesFilterAndLists = new HashMap<>();
        Map<String, List<String>> noFilterAndLists = new HashMap<>();
        System.out.println("-------- F I L T E R S ---------");
        if (yesFilters != null) {
            yesFilters.forEach(filter -> {
                System.out.println(filter);
                String[] nv = filter.split("=");
                List<String> colMap = yesFilterAndLists.get(nv[0]);
                if (colMap == null) {
                    colMap = new ArrayList<>();
                }
                colMap.add(nv[1]);
                yesFilterAndLists.put(nv[0], colMap);
            });
        }
        // Same for NO (negative) filters
        if (noFilters != null) {
            noFilters.forEach(filter -> {
                System.out.println(filter);
                String[] nv = filter.split("=");

                SupportedSuffix suffix = SupportedSuffix.getSuffix(nv[0]); // Should be only NotEqualTo
                String colName = nv[0];
                if (suffix != null) {
                    colName = colName.substring(0, colName.indexOf(suffix.suffix()));
                }
//                String colName = nv[0].substring(0, nv[0].indexOf("NotEqualTo"));
                List<String> colMap = noFilterAndLists.get(colName);
                if (colMap == null) {
                    colMap = new ArrayList<>();
                }
                colMap.add(nv[1]);
                noFilterAndLists.put(colName, colMap);
            });
        }
        System.out.println("--------------------------------");
        return list.stream().filter(oneItem -> {
//            System.out.println("--------------------------------------------------");
            AtomicBoolean ok = new AtomicBoolean(true);
            yesFilterAndLists.forEach((colName, valueList) -> {
                AtomicBoolean colOk = new AtomicBoolean(false); // YesFilter on same column: OR, and start with false!
                valueList.forEach(value -> {
                    SupportedSuffix suffix = SupportedSuffix.getSuffix(colName);
                    String path;
                    if (suffix != null) {
                        path = colToPath.get(colName.substring(0, colName.indexOf(suffix.suffix())));
                    } else {
                        path = colToPath.get(colName);
                    }
                    Object objValue = drillToValue(oneItem, path);
                    System.out.println(String.format("With Path [%s] comparing expected [%s] to [%s]", path, value, String.valueOf(objValue)));
                    // Logical OR
                    if (objValue == null) {
                        colOk.set(colOk.get() | false);
                    } else {
                        if (suffix == null || (suffix != null && suffix.applyToType().equals("string"))) {
                            colOk.set(colOk.get() | value.equals(String.valueOf(objValue)));
                        } else {
                            String type = suffix.applyToType();
                            if (type.equals("number")) {
                                double numValue = Double.parseDouble(String.valueOf(objValue));
                                double refValue = Double.parseDouble(String.valueOf(value));
//                                System.out.println(String.format("Numeric comparison between %f and %f", numValue, refValue));
                                if (suffix.equals(SupportedSuffix.GE)) {
                                    colOk.set(colOk.get() | (numValue >= refValue));
                                } else if (suffix.equals(SupportedSuffix.GT)) {
                                    colOk.set(colOk.get() | (numValue > refValue));
                                } else if (suffix.equals(SupportedSuffix.LT)) {
                                    colOk.set(colOk.get() | (numValue < refValue));
                                } else if (suffix.equals(SupportedSuffix.LE)) {
                                    colOk.set(colOk.get() | (numValue <= refValue));
                                }
                            }
                        }
                    }
                });
                ok.set(ok.get() & colOk.get()); // Logical AND.
            });

            noFilterAndLists.forEach((colName, valueList) -> {
                AtomicBoolean colOk = new AtomicBoolean(true);
                valueList.forEach(value -> {
                    String path = colToPath.get(colName);
                    Object objValue = drillToValue(oneItem, path);
                    System.out.println(String.format("With Path [%s] comparing NOT expected [%s] to [%s]", path, value, String.valueOf(objValue)));
                    // Logical AND (it's a negation)
                    if (objValue == null) {
                        colOk.set(colOk.get() & true);
                    } else {
                        colOk.set(colOk.get() & !value.equals(String.valueOf(objValue)));
                    }
                });
//                System.out.println(String.format("\t>> Neg: %s", colOk.get()));
                ok.set(ok.get() & colOk.get()); // Logical AND.
            });

            return ok.get();
        }).collect(Collectors.toList());
    }

    private final static String FILE_NAME_1 = "./curationFunctions.json";
    private final static String ARRAY_FOR_FUNCTIONS = "curationFunctions";
    private final static String FILE_NAME_2 = "./bagnoles.json";
    private final static String ARRAY_FOR_BAGNOLES = "bagnoles";
    private static boolean USE_SCIM = true;

    private static String dataFileName = /* FILE_NAME_1 */ FILE_NAME_2;
    private static String arrayName = /* ARRAY_FOR_FUNCTIONS */ ARRAY_FOR_BAGNOLES;

    /**
     * For tests
     *
     * @param args Unused.
     */
    public static void main(String... args) {

        try {
            File jsonFile = new File(dataFileName);
            if (!jsonFile.exists()) {
                throw new RuntimeException(String.format("%s not found in %s", dataFileName, System.getProperty("user.dir")));
            }
            Map<String, Object> map = mapper.readValue(jsonFile.toURI().toURL(), Map.class);
            if (true) {
                System.out.println("Map was read");
            }
            List<Map<String, Object>> listToFilter = (List<Map<String, Object>>) map.get(arrayName);
            List<Map<String, Object>> filtered = null;
            if (USE_SCIM) {
                // A - SCIM-like filters
//                String filter = "title co \"W\"";
//                String filter = "columnFunction eq true";
//                String filter = "parameters pr";
//                String filter = "name co .math";
//                String filter = "name co .math and title co \"W\"";
//                String filter = "name co .math and title sw \"W\" and description co \"eturns the\" and parameters.schema.additionalProperties eq false ";
//                String filter = "parameters.schema.properties.left pr ";
//                String filter = "stuff pr and stuff ge 9";
                String filter = "make ne VolksWagen and extra-data.nb-seats ge 4";
//                String filter = "make pr";

                filtered = filter(listToFilter, filter);
            } else {
                // B - OCI-like filters, parameters.
                /*
                 * From the doc at https://confluence.oci.oraclecorp.com/pages/viewpage.action?spaceKey=DEX&title=API+Consistency+Guidelines#APIConsistencyGuidelines-Filtering
                 * Example: cars?make=foo&model=bar
                 *              matches all cars where make is "foo" AND model is "bar".
                 * Example: cars?make=foo&make=baz
                 *              matches all cars where make is "foo" OR make is "baz".
                 * Example: cars?make=foo&model=bar&make=baz
                 *              matches all cars where (make is "foo" OR make is "baz") AND model is "bar".
                 * Example: cars?makeNotEqualTo=toyota&makeNotEqualTo=honda
                 *              matches all cars where make is not "toyota" AND make is not "honda" (i.e. the make is neither "toyota" nor "honda").
                 */
                String makeFilter1 = "make=VolksWagen";
                String makeFilter2 = "make=Nissan";
                String makeFilter3 = "make=Citroen";
                String makeFilterNon1 = "makeNotEqualTo=VolksWagen";
                String makeFilterNon2 = "makeNotEqualTo=Nissan";
                String modelFilter1 = "model=One";
                String modelFilter2 = "model=Two";
                String priceFilter3 = "priceGreaterThanOrEqualTo=900";
                String modelFilterNon1 = "modelNotEqualTo=Two";
                String modelFilterNon2 = "modelNotEqualTo=Three";

                String columnFunctionFilter = "columnFunction=true";
                String additionalPropertiesFilter = "additionalProperties=false";
                String titleFilter = "title=SampleKurtosis";
                String titleFilterNon1 = "titleNotEqualTo=SampleKurtosis";
                String titleFilterNon2 = "titleNotEqualTo=SampleSkew";

                /*
                To try:
                cars?make=foo&make=bar&modelNotEqual=X&modelNotEqual=Y
                 */
//                filtered = filter(listToFilter,
//                        Arrays.asList(new String[] {
//                                makeFilter1,
//                                makeFilter2,
//                                modelFilter1 }),
//                        OIC_COL_TO_PATH);
                filtered = filter(listToFilter,
                        Arrays.asList(new String[]{
                                columnFunctionFilter,
                                // additionalPropertiesFilter,
                                // titleFilter,
                                titleFilterNon1,
                                titleFilterNon2}),
                        OIC_COL_TO_PATH);
//                filtered = filter(listToFilter,
//                        Arrays.asList(new String[] {
//                                makeFilter1,
//                                makeFilter2,
//                                makeFilter3,
//                                priceFilter3,
//                                modelFilterNon1,
//                                modelFilterNon2 }),
//                        OIC_COL_TO_PATH);
            }
            // Sorted ?
            if (true) {
                filtered.sort(Comparator.comparing(
                        oneMap -> (int) oneMap.get("price"),
                        Comparator.nullsLast(Comparator.reverseOrder()))); // naturalOrder() for ASC sort.
            }

            String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(filtered);
            if (true) {
                System.out.println(json);
            }
            System.out.println(String.format("Filtered: %d / %d elements.", filtered.size(), listToFilter.size()));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
