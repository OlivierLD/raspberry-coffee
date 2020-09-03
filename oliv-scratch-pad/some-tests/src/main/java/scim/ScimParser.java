package scim;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Using SCIM syntax on a JSON file (curationFunctions.json)
 * As it is now, just an exercise...
 *
 * SCIM "spec": https://ldapwiki.com/wiki/SCIM%20Filtering
 *
 * Parsing an expression: https://unnikked.ga/how-to-build-a-boolean-expression-evaluator-518e9e068a65,
 *        and https://github.com/unnikkedga/BooleanExpressionEvaluator
 */
public class ScimParser {

    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     *
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

    public enum SCIMOperators {
        CO("co"), // Contains
        EQ("eq"), // Equals
        SW("sw"), // Starts With
        PR("pr"), // Present (exists)
        GT("gt"), // Greater than
        GE("ge"), // Greater or equal
        LT("lt"), // Lower than
        LE("le"); // Lower or equal

        private final String op;

        SCIMOperators(String op) {
            this.op = op;
        }
        public String op() {
            return this.op;
        }
    }

    /**
     *
     * @param op the lowercase operator
     * @return the corresponding member of the {@link SCIMOperators} enum.
     */
    private static SCIMOperators findOp(String op) {
        return Arrays.stream(SCIMOperators.values())
                .filter(scimOp -> scimOp.op().equals(op))
                .findFirst().orElse(null);
    }

    private static class OneExpression {
        String field;
        SCIMOperators op;
        String value;

        public OneExpression field(String field) {
            this.field = field;
            return this;
        }
        public OneExpression op(String op) {
            this.op = findOp(op);
            return this;
        }
        public OneExpression value(String value) {
            this.value = value;
            return this;
        }

        @Override
        public String toString() {
            return String.format("%s %s%s", field, op.op(), (value == null ? "" : (" " + String.format("'%s'", value))));
        }
    }

    /**
     *
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
            for (int i=3; i<fieldOpValue.length; i++) {
                sb.append(String.format(" %s", fieldOpValue[i]));
            }
            value = dropQuotes(sb.toString().trim());
        }
        return new OneExpression()
                .field(field)
                .op(operator)
                .value(value);
    }

    /**
     *
     * @param origin the json object, represented as a Map
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
     *
     * @param obj the object to evaluate the expression on
     * @param expr the expression to use, as parsed
     * @return true or false, if the condition expressed by the 'expr' is met or not.
     */
    private static boolean evaluate(Map<String, Object> obj, OneExpression expr) {
        if (expr.op.equals(SCIMOperators.CO)) {
            return String.valueOf(drillToValue(obj, expr.field)).contains(expr.value);
        } else if (expr.op.equals(SCIMOperators.EQ)) {
            return String.valueOf(drillToValue(obj, expr.field)).equals(expr.value);
        } else if (expr.op.equals(SCIMOperators.SW)) {
            return String.valueOf(drillToValue(obj, expr.field)).startsWith(expr.value);
        } else if (expr.op.equals(SCIMOperators.PR)) {
            return drillToValue(obj, expr.field) != null;
        } else { // Other operators... on Dates or Numbers
            Object object = drillToValue(obj, expr.field);
            if (object != null) {
                String field = String.valueOf(object);
                try {
                    double fieldValue = Double.parseDouble(field);
                    double refValue = Double.parseDouble(expr.value);
                    if (expr.op.equals(SCIMOperators.LT)) {
                        return fieldValue < refValue;
                    } else if (expr.op.equals(SCIMOperators.LE)) {
                        return fieldValue <= refValue;
                    } else if (expr.op.equals(SCIMOperators.GT)) {
                        return fieldValue > refValue;
                    } else if (expr.op.equals(SCIMOperators.GE)) {
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
     * @param list the list to filter
     * @param filter the list of expression to apply to filter.
     * @return The filtered list.
     */
    private static List<Map<String, Object>> filter(List<Map<String, Object>> list, String filter) {
        String[] expression = filter.split(" and "); // Only AND for now...
        List<OneExpression> filters = new ArrayList<>();
        Arrays.asList(expression).forEach(exp -> filters.add(parseOne(exp)));
        if (true) { // Verbose
            System.out.println("-- [Filter parsed expressions] --");
            filters.forEach(exp -> System.out.println(String.format("[%s]", exp.toString())));
            System.out.println("---------------------------------");
        }

        return list.stream().filter(oneItem -> {
            boolean ok = true;
            for (OneExpression expr : filters) {
                ok &= evaluate(oneItem, expr);  // AND
            }
            return ok;
        }).collect(Collectors.toList());
    }

    private final static String FILE_NAME = "./curationFunctions.json";

    /**
     * For tests
     * @param args Unused.
     */
    public static void main(String... args) {
        try {
            File jsonFile = new File(FILE_NAME);
            if (!jsonFile.exists()) {
                throw new RuntimeException(String.format("%s not found in %s", FILE_NAME, System.getProperty("user.dir")));
            }
            Map<String, Object> map = mapper.readValue(jsonFile.toURI().toURL(), Map.class);
            if (true) {
                System.out.println("Map was read");
            }
            List<Map<String, Object>> listToFilter = (List<Map<String, Object>>) map.get("curationFunctions");
//            String filter = "title co \"W\"";
//            String filter = "columnFunction eq true";
//            String filter = "parameters pr";
//            String filter = "name co .math";
//            String filter = "name co .math and title co \"W\"";
//            String filter = "name co .math and title sw \"W\" and description co \"eturns the\" and parameters.schema.additionalProperties eq false ";
//            String filter = "parameters.schema.properties.left pr ";
            String filter = "stuff pr and stuff ge 9";
            List<Map<String, Object>> filtered = filter(listToFilter, filter);
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
