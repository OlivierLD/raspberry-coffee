package oliv.oda.dtv2;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DecisionTablesStaticUtils {

    private static List<String> getItemList(Map<String, Object> decisionMap) {
        List<String> inputParams = new ArrayList<>();
        // Find required input data
        Object requiredInputData = decisionMap.get("requiredInputData"); // Also available in logic.references
        Object items = ((Map)requiredInputData).get("items");
        List<Map<String, Object>> itemList = (List)items;
        itemList.forEach(item -> {
            String value = (String)((Map)item).get("name");
            inputParams.add(value);
        });
        return inputParams;
    }

    private static String stripQuotes(String in) {
        String out = in;
        if ((out.startsWith("\"") && out.endsWith("\"")) ||
                (out.startsWith("'") && out.endsWith("'"))) {
            out = out.substring(1, out.length() - 1);
        }
        return out;
    }

    private static DecisionContext setUpdateContext(Map<String, Object> decisionMap,
                                                    Map<String, Object> txMap) throws Exception {
        DecisionContext context = new DecisionContext();
        List<Map<String, Object>> update = (List)txMap.get("update-operation");
        List<Map<String, Object>> where = (List)txMap.get("line-locator");

        if (where != null) {
            where.forEach(oneCondition -> {
                context.addWhereColumnId((String)oneCondition.get("item-name"));
                context.addWhereColumnValue((String)oneCondition.get("item-value"));
            });
        }

        if (update != null) {
            update.forEach(oneUpdate -> { // TODO There is only ONE for now
                String itemName = (String)oneUpdate.get("item-name");
                String updateTo = (String)oneUpdate.get("update-to");
                context.setTargetColumnId(itemName);
                context.setTargetColumnValue(updateTo);
                context.setRawTargetColumnValue(updateTo);
            });
        }

        List<String> items = getItemList(decisionMap);
        if (true) {
            System.out.println("--- ITEMS ---");
            items.forEach(System.out::println);
            System.out.println("-------------");
        }

        String decisionName = (String)decisionMap.get("name");
        if (where != null) {
            context.getWhereColumnId().forEach(columnId -> {
                if (!items.contains(columnId) && !columnId.equals(decisionName)) {
                    throw new InvalidParameterException(String.format("WHERE [%s] Not Found in item list", columnId));
                }
            });
        }
        if (!context.getTargetColumnId().equals(decisionName) && !items.contains(context.getTargetColumnId())) {
            throw new InvalidParameterException(String.format("%s [%s] Not Found in item list", "UPDATE", context.getTargetColumnId()));
        }
        // Find indexes
        if (where != null) {
            context.getWhereColumnId().forEach(columnId -> {
                if (columnId.equals(decisionName)) {
                    context.addWhereColumnIndex(-1);
                } else {
                    context.addWhereColumnIndex(items.indexOf(columnId));
                }
            });
//            context.setWhereColumnIndex(items.indexOf(context.getWhereColumnId()));
        }
        context.setTargetColumnIndex(context.getTargetColumnId().equals(decisionName) ? -1 : items.indexOf(context.getTargetColumnId()));

        // Operation, on the value to update
        DecisionContext.Operation op = DecisionContext.detectOperation(context.getTargetColumnValue());
        if (op != null) {
            // Extract value
            String extracted = DecisionContext.extractFunctionParameter(context.getTargetColumnValue(), op);
            context.setTargetColumnValue(stripQuotes(extracted));
        }
        context.setOperation(op);

        return context;
    }

    private static DecisionContext setQueryContext(Map<String, Object> decisionMap,
                                                   Map<String, Object> txMap) throws Exception {
        DecisionContext context = new DecisionContext();
        List<Map<String, Object>> query = (List)txMap.get("select-operation");
        List<Map<String, Object>> where = (List)txMap.get("line-locator");

        if (where != null) {
            where.forEach(oneCondition -> {
                context.addWhereColumnId((String)oneCondition.get("item-name"));
                context.addWhereColumnValue((String)oneCondition.get("item-value"));
            });
        }

        if (query != null) {
            // TODO Make sure there is a 'where' ?
            context.setTargetColumnId((String)query.get(0).get("item-name")); // TODO There is a limit to 1 element
            DecisionContext.Operation op = DecisionContext.detectOperation(context.getTargetColumnId());
            if (op != null) {
                // Extract value
                String extracted = DecisionContext.extractFunctionParameter(context.getTargetColumnId(), op);
                context.setTargetColumnId(stripQuotes(extracted));
            }
            context.setOperation(op);
//            context.setTargetColumnValue(query);
            context.setRawTargetColumnValue((String)query.get(0).get("item-name"));
        }

        List<String> items = getItemList(decisionMap);
        if (true) {
            System.out.println("--- ITEMS ---");
            items.forEach(System.out::println);
            System.out.println("-------------");
        }

        String decisionName = (String)decisionMap.get("name");
        if (where != null) {
            context.getWhereColumnId().forEach(columnId -> {
                if (!items.contains(columnId) && !columnId.equals(decisionName)) {
                    throw new InvalidParameterException(String.format("WHERE [%s] Not Found in item list", columnId));
                }
            });
        }
        if (!context.getTargetColumnId().equals(decisionName) && !items.contains(context.getTargetColumnId())) {
            throw new InvalidParameterException(String.format("%s [%s] Not Found in item list", "QUERY", context.getTargetColumnId()));
        }
        // Find indexes
        if (where != null) {
            context.getWhereColumnId().forEach(columnId -> {
                if (columnId.equals(decisionName)) {
                    context.addWhereColumnIndex(-1);
                } else {
                    context.addWhereColumnIndex(items.indexOf(columnId));
                }
            });
        }
        context.setTargetColumnIndex(context.getTargetColumnId().equals(decisionName) ? -1 : items.indexOf(context.getTargetColumnId()));

        return context;
    }

    private static List<Object> getValues(Map<String, Object> entryNode) {
        List<Object> values = new ArrayList<>();
        if (entryNode.get("value") != null) {
            values.add(entryNode.get("value"));
        } else if (entryNode.get("values") != null) {
            values.addAll((List)entryNode.get("values"));
        }
        return values;
    }

    static String processUpdate(InputStream original, String txSyntax) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        // THE Decision Table Object
        Map<String, Object> jsonMap = mapper.readValue(original, Map.class);
        // The transformation directive
        Map<String, Object> tx = mapper.readValue(txSyntax, Map.class);
        // Set Update Context
        DecisionContext decisionUpdateContext = setUpdateContext(jsonMap, tx);

        final List<String> columnId = decisionUpdateContext.getWhereColumnId();
        final List<String> columnValue = decisionUpdateContext.getWhereColumnValue();

        final String targetColumnId = decisionUpdateContext.getTargetColumnId();
        final String targetColumnNewValue = decisionUpdateContext.getTargetColumnValue();

        final List<Integer> columnIndex = decisionUpdateContext.getWhereColumnIndex();
        final int targetColumnIndex = decisionUpdateContext.getTargetColumnIndex();

//        if (columnId != null) { // Mean there IS a where
//            System.out.printf("[%s] found at index %d\n", columnId, columnIndex);
//        }
//        System.out.printf("[%s] found at index %d\n", targetColumnId, targetColumnIndex);

        final List<Object> queryResult = new ArrayList<>();

        // Find the rules
        List<Map<String, Object>> rules = (List)((Map)jsonMap.get("logic")).get("rules");
        rules.forEach(rule -> {
            List<Map<String, Object>> inputEntries = (List)rule.get("inputEntries");
            List<Map<String, Object>> outputEntries = (List)rule.get("outputEntries");
            List<Map<String, Object>> annotationEntries = (List)rule.get("annotationEntries");

            boolean conditionMet = true;
            for (int ii=0; ii<columnIndex.size(); ii++) {
                int colIdx = columnIndex.get(ii);
                List<Object> values = (colIdx == -1 ? Arrays.asList((String)outputEntries.get(0).get("value")) : getValues(inputEntries.get(colIdx)));
                System.out.println("Comparing " + values + " and " + columnValue.get(ii));
                boolean met = (columnValue.get(ii) == null || values.contains(columnValue.get(ii)));
                conditionMet = conditionMet && met;
            }

            if (conditionMet) {
                // Column targetColumnId
                if (decisionUpdateContext.getOperation() != null) {
                    if (decisionUpdateContext.getOperation().equals(DecisionContext.Operation.RANGE)) {
                        Map<String, Object> range = (Map) inputEntries.get(targetColumnIndex).get("range");
                        if (range != null) {
                            Object endpoint1 = range.get("endpoint1");
                            System.out.printf(">> Value for %s: currently %s, moving to %s\n", (columnValue != null ? columnValue : "this line"), endpoint1, targetColumnNewValue);
                            range.put("endpoint1", targetColumnNewValue);
                            queryResult.add(endpoint1);
                        } else {
                            throw new RuntimeException("No 'range' found where expected");
                        }
                    } else if (decisionUpdateContext.getOperation().equals(DecisionContext.Operation.APPEND_TO_LIST)) {
                        List<String> values = (List) inputEntries.get(targetColumnIndex).get("values");
                        if (values == null) {
                            String oneValue = (String)inputEntries.get(targetColumnIndex).get("value");
                            if (oneValue != null) {
                                values = new ArrayList();
                                values.add(oneValue);
                                inputEntries.get(targetColumnIndex).put("values", values);
                                inputEntries.get(targetColumnIndex).remove("value");
                            }
                        }
                        if (values != null) {
                            // Check value validity
                            List<Map<String, Object>> suggestions = (List) inputEntries.get(targetColumnIndex).get("suggestions");
                            AtomicBoolean found = new AtomicBoolean(false);
                            suggestions.forEach(suggestion -> {
                                if (targetColumnNewValue.equals(suggestion.get("value"))) {
                                    found.set(true);
                                }
                            });
                            if (found.get()) {
                                AtomicBoolean alreadyThere = new AtomicBoolean(false);
                                values.forEach(val -> {
                                    if (targetColumnNewValue.equals(val)) {
                                        alreadyThere.set(true);
                                    }
                                });
                                if (!alreadyThere.get()) {
                                    values.add(targetColumnNewValue);
                                } else {
                                    throw new RuntimeException(String.format("[%s] already there.", targetColumnNewValue));
                                }
                            } else {
                                throw new RuntimeException(String.format("[%s] not a valid option.", targetColumnNewValue));
                            }
                        } else {
                            throw new RuntimeException("No 'values' or 'value' found where expected");
                        }
                    } else if (decisionUpdateContext.getOperation().equals(DecisionContext.Operation.DELETE_FROM_LIST)) {
                        // Leave at least one element...
                        List<String> values = (List) inputEntries.get(targetColumnIndex).get("values");
                        if (values == null) {
                            String oneValue = (String)inputEntries.get(targetColumnIndex).get("value");
                            if (oneValue != null) {
                                if (targetColumnNewValue.equals(oneValue)) {
                                    throw new RuntimeException(String.format("[%s] is the last value in the list. Leaving it.", targetColumnNewValue));
                                } else {
                                    throw new RuntimeException(String.format("[%s] is not in the list", targetColumnNewValue));
                                }
                            }
                        }
                        if (values != null) {
                            if (values.contains(targetColumnNewValue)) {
                                values.remove(targetColumnNewValue);
                            } else {
                                throw new RuntimeException(String.format("[%s] is not in the list", targetColumnNewValue));
                            }
                        } else {
                            throw new RuntimeException("No 'values' or 'value' found where expected");
                        }
                    }
                } else {
                    // TODO Create a value node?
                    if (targetColumnIndex != -1) {
                        throw new RuntimeException(String.format("Unmanaged operation [%s]", decisionUpdateContext.getRawTargetColumnValue()));
                    } else {
                        String oldValue = (String)outputEntries.get(0).get("value");
                        outputEntries.get(0).put("value", targetColumnNewValue);
                        queryResult.add(oldValue);
                    }
                }
            }
//            System.out.printf("%d input(s), %d output, %d annotations\n",
//                    inputEntries.size(),
//                    outputEntries.size(),
//                    annotationEntries.size());
        });

        String jsonInString;
        if (queryResult.size() == 0) {
            System.out.println(">> Warning: >> No update was done!");
            // Insert?
        }
        jsonInString = mapper.writeValueAsString(jsonMap);
        return jsonInString;
    }

    static String processQuery(InputStream original, String txSyntax) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        // THE Decision Table Object
        Map<String, Object> jsonMap = mapper.readValue(original, Map.class);
        // The transformation directive
        Map<String, Object> tx = mapper.readValue(txSyntax, Map.class);
        // Set Update Context
        DecisionContext decisionUpdateContext = setQueryContext(jsonMap, tx);

        final List<String> columnId = decisionUpdateContext.getWhereColumnId();
        final List<String> columnValue = decisionUpdateContext.getWhereColumnValue();

        final String targetColumnId = decisionUpdateContext.getTargetColumnId();
        final String targetColumnNewValue = decisionUpdateContext.getTargetColumnValue();

        final List<Integer> columnIndex = decisionUpdateContext.getWhereColumnIndex();
        final int targetColumnIndex = decisionUpdateContext.getTargetColumnIndex();

//        if (columnId != null) { // Mean there IS a where
//            System.out.printf("[%s] found at index %d\n", columnId, columnIndex);
//        }
//        System.out.printf("[%s] found at index %d\n", targetColumnId, targetColumnIndex);

        final List<Object> queryResult = new ArrayList<>();

        // Find the rules
        List<Map<String, Object>> rules = (List)((Map)jsonMap.get("logic")).get("rules");
        rules.forEach(rule -> {
            List<Map<String, Object>> inputEntries = (List)rule.get("inputEntries");
            List<Map<String, Object>> outputEntries = (List)rule.get("outputEntries");
            List<Map<String, Object>> annotationEntries = (List)rule.get("annotationEntries");

            boolean conditionMet = true;
            for (int ii=0; ii<columnIndex.size(); ii++) {
                int colIdx = columnIndex.get(ii);
                List<Object> values = (colIdx == -1 ? Arrays.asList((String)outputEntries.get(0).get("value")) : getValues(inputEntries.get(colIdx)));
                System.out.println("Comparing " + values + " and " + columnValue.get(ii));
                boolean met = (columnValue.get(ii) == null || values.contains(columnValue.get(ii)));
                conditionMet = conditionMet && met;
            }

            if (conditionMet) {
                // Column targetColumnId
                if (decisionUpdateContext.getOperation() != null) {
                    if (decisionUpdateContext.getOperation().equals(DecisionContext.Operation.RANGE)) {
                        Map<String, Object> range = (Map) inputEntries.get(targetColumnIndex).get("range");
                        if (range != null) {
                            Object endpoint1 = range.get("endpoint1");
                            System.out.printf(">> Value for %s: currently %s\n", (columnValue != null ? columnValue : "this line"), endpoint1);
                            queryResult.add(endpoint1);
                        } else {
                            throw new RuntimeException("No 'range' found where expected");
                        }
                   }
                } else {
                    // System.out.println("Query with no function");
                    if (targetColumnIndex == -1) {
                        queryResult.add(outputEntries.get(0).get("value"));
                    } else {
                        Map<String, Object> itemObjectMap = inputEntries.get(targetColumnIndex);
                        if (itemObjectMap.get("range") != null) {
                            queryResult.add(itemObjectMap.get("range"));
                        } else if (itemObjectMap.get("values") != null) {
                            queryResult.add(itemObjectMap.get("values"));
                        } else if (itemObjectMap.get("value") != null) {
                            queryResult.add(itemObjectMap.get("value"));
                        } else {
                            queryResult.add(itemObjectMap);
                        }
                    }
                }
            }
//            System.out.printf("%d input(s), %d output, %d annotations\n",
//                    inputEntries.size(),
//                    outputEntries.size(),
//                    annotationEntries.size());
        });

        String jsonInString;
        if (queryResult.size() > 0) {
            jsonInString = mapper.writeValueAsString(queryResult); // .get(0));
        } else {
            jsonInString = "NOT_FOUND";
        }
        return jsonInString;
    }

    public static class DecisionContext {

        public enum Operation {
            RANGE("range"),
            APPEND_TO_LIST("appendToList"),      // Not for QUERY
            DELETE_FROM_LIST("deleteFromList");  // Not for QUERY

            private final String functionName;

            Operation(String functionName) {
                this.functionName = functionName;
            }
            public String functionName() {
                return this.functionName;
            }
        }

        List<String> whereColumnId = new ArrayList<>();
        List<Integer> whereColumnIndex = new ArrayList<>();
        List<String> whereColumnValue = new ArrayList<>();
        String targetColumnId;
        int targetColumnIndex;
        String targetColumnValue;
        String rawTargetColumnValue;
        Operation operation;

        public DecisionContext() {}

        public List<String> getWhereColumnId() {
            return whereColumnId;
        }

        public void setWhereColumnId(List<String> whereColumnId) {
            this.whereColumnId = whereColumnId;
        }
        public void addWhereColumnId(String columnId) {
            this.whereColumnId.add(columnId);
        }

        public List<Integer> getWhereColumnIndex() {
            return whereColumnIndex;
        }

        public void setWhereColumnIndex(List<Integer> whereColumnIndex) {
            this.whereColumnIndex = whereColumnIndex;
        }
        public void addWhereColumnIndex(int whereColumnIndex) {
            this.whereColumnIndex.add(whereColumnIndex);
        }

        public List<String> getWhereColumnValue() {
            return whereColumnValue;
        }

        public void setWhereColumnValue(List<String> whereColumnValue) {
            this.whereColumnValue = whereColumnValue;
        }
        public void addWhereColumnValue(String whereColumnValue) {
            this.whereColumnValue.add(whereColumnValue);
        }

        public String getTargetColumnId() {
            return targetColumnId;
        }

        public void setTargetColumnId(String targetColumnId) {
            this.targetColumnId = targetColumnId;
        }

        public int getTargetColumnIndex() {
            return targetColumnIndex;
        }

        public void setTargetColumnIndex(int targetColumnIndex) {
            this.targetColumnIndex = targetColumnIndex;
        }

        public String getTargetColumnValue() {
            return targetColumnValue;
        }

        public void setTargetColumnValue(String targetColumnValue) {
            this.targetColumnValue = targetColumnValue;
        }

        public Operation getOperation() {
            return operation;
        }

        public void setOperation(Operation operation) {
            this.operation = operation;
        }

        public String getRawTargetColumnValue() {
            return rawTargetColumnValue;
        }

        public void setRawTargetColumnValue(String rawTargetColumnValue) {
            this.rawTargetColumnValue = rawTargetColumnValue;
        }

        protected static Operation detectOperation(String str) {
            Operation operation = null;

            for (Operation op : Operation.values()) {
                String patternStr = op.functionName() + "\\(.*\\)";
                Pattern pattern = Pattern.compile(patternStr);
                Matcher matcher = pattern.matcher(str);
                if (matcher.matches()) {
                    operation = op;
                    break;
                }
            }
            return operation;
        }

        /**
         * From 'range(350)', extract '350'
         * @param str
         * @param op
         * @return
         */
        protected static String extractFunctionParameter(String str, Operation op) {
            String extracted;
            String patternStr = op.functionName() + "\\(.*\\)";
            Pattern pattern = Pattern.compile(patternStr);
            Matcher matcher = pattern.matcher(str);
            if (matcher.matches()) {
                extracted = str.substring(op.functionName().length() + 1, str.trim().length() - 1);
            } else {
                throw new RuntimeException(String.format("[%s] does not match [%s]", str, op.functionName()));
            }
            return extracted;
        }
    }
}
