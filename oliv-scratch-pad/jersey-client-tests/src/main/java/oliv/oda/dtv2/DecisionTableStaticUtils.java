package oliv.oda.dtv2;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.security.InvalidParameterException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DecisionTableStaticUtils {

    private static List<String> getInputItemList(Map<String, Object> decisionMap) {
        List<String> inputParams = new ArrayList<>();
        // Find required input data
//        Object requiredInputData = decisionMap.get("requiredInputData"); // Also available in logic.references
//        Object items = ((Map)requiredInputData).get("items");
//        List<Map<String, Object>> itemList = (List)items;
//        itemList.forEach(item -> {
//            String value = (String)((Map)item).get("name");
//            inputParams.add(value);
//        });

        List<Map<String, Object>> itemList = (List)((Map)decisionMap.get("logic")).get("inputs");
        itemList.forEach(item -> {
            String value = (String)((Map)item.get("inputExpression")).get("value");
            inputParams.add(value);
        });

        return inputParams;
    }

    private static List<String> getOutputItemList(Map<String, Object> decisionMap) {
        List<String> outputParams = new ArrayList<>();
        List<Map<String, Object>> itemList = (List)((Map)decisionMap.get("logic")).get("outputs");
        itemList.forEach(item -> {
            String value = (String)((Map)item).get("name");
            outputParams.add(value);
        });

        return outputParams;
    }

    private static String stripQuotes(String in) {
        String out = in;
        if (out != null) {
            out = out.trim();
            if ((out.startsWith("\"") && out.endsWith("\"")) ||
                    (out.startsWith("'") && out.endsWith("'"))) {
                out = out.substring(1, out.length() - 1);
            }
        }
        return out;
    }

    private static DecisionContext setUpdateContext(Map<String, Object> decisionMap,
                                                    Map<String, Object> txMap) throws Exception {
        DecisionContext context = new DecisionContext();
        List<Map<String, Object>> update = (List)txMap.get("update-operation");
        List<Map<String, Object>> where = (List)txMap.get("line-locator");

//        String decisionName = (String)decisionMap.get("name");
        List<String> inputItems = getInputItemList(decisionMap);
        List<String> outputItems = getOutputItemList(decisionMap);
        if (true) {
            System.out.println("--- INPUT ITEMS ---");
            inputItems.forEach(System.out::println);
            System.out.println("-------------------");
            System.out.println("-- OUTPUT ITEMS ---");
            outputItems.forEach(System.out::println);
            System.out.println("-------------------");
        }

        if (where != null) {
            where.forEach(oneCondition -> {
                context.addWhereColumnId((String)oneCondition.get("item-name"));
                context.addWhereColumnValue((String)oneCondition.get("item-value"));
            });
            context.getWhereColumnId().forEach(columnId -> {
                if (!inputItems.contains(columnId) && !outputItems.contains(columnId)) {
                    throw new InvalidParameterException(String.format("WHERE [%s] Not Found in item list", columnId));
                }
                // Find indexes
                DecisionContext.TargetColumnIndex idx = outputItems.contains(columnId) ?
                        new DecisionContext.TargetColumnIndex(outputItems.indexOf(columnId), DecisionContext.TargetColumnIndex.InOrOut.OUPUT) :
                        new DecisionContext.TargetColumnIndex(inputItems.indexOf(columnId));
                context.addWhereColumnIndex(idx);
            });
        }

        if (update != null) {
            update.forEach(oneUpdate -> {
                String itemName = (String)oneUpdate.get("item-name");
                String updateTo = (String)oneUpdate.get("update-to");
                context.addTargetColumnId(itemName);
                // Operation, on the value to update
                DecisionContext.Operation op = DecisionContext.detectOperation(updateTo);
                String extracted = null;
                if (op != null) { // Extract value
                    extracted = DecisionContext.extractFunctionParameter(updateTo, op);
                }
                context.addTargetColumnValue(extracted == null ? updateTo : stripQuotes(extracted));
                context.addOperation(op);
                context.addRawTargetColumnValue(updateTo);
                context.addTargetColumnIndex(outputItems.contains(itemName) ?
                        new DecisionContext.TargetColumnIndex(outputItems.indexOf(itemName), DecisionContext.TargetColumnIndex.InOrOut.OUPUT) :
                        new DecisionContext.TargetColumnIndex(inputItems.indexOf(itemName)));

                if (!outputItems.contains(itemName) && !inputItems.contains(itemName)) {
                    throw new InvalidParameterException(String.format("%s [%s] Not Found in item list", "UPDATE", itemName));
                }
            });
        }
        return context;
    }

    private static DecisionContext setQueryContext(Map<String, Object> decisionMap,
                                                   Map<String, Object> txMap) throws Exception {
        DecisionContext context = new DecisionContext();
        List<Map<String, Object>> query = (List)txMap.get("select-operation");
        List<Map<String, Object>> where = (List)txMap.get("line-locator");

//        String decisionName = (String)decisionMap.get("name");

        List<String> inputItems = getInputItemList(decisionMap);
        List<String> outputItems = getOutputItemList(decisionMap);
        if (true) {
            System.out.println("--- INPUT ITEMS ---");
            inputItems.forEach(System.out::println);
            System.out.println("-------------------");
            System.out.println("-- OUTPUT ITEMS ---");
            outputItems.forEach(System.out::println);
            System.out.println("-------------------");
        }

        if (where != null) {
            where.forEach(oneCondition -> {
                context.addWhereColumnId((String)oneCondition.get("item-name"));
                context.addWhereColumnValue((String)oneCondition.get("item-value"));
            });
            context.getWhereColumnId().forEach(columnId -> {
                if (!inputItems.contains(columnId) && !outputItems.contains(columnId)) {
                    throw new InvalidParameterException(String.format("WHERE [%s] Not Found in item list", columnId));
                }
                // Find indexes.
                DecisionContext.TargetColumnIndex idx = outputItems.contains(columnId) ?
                                new DecisionContext.TargetColumnIndex(outputItems.indexOf(columnId), DecisionContext.TargetColumnIndex.InOrOut.OUPUT) :
                                new DecisionContext.TargetColumnIndex(inputItems.indexOf(columnId));
                context.addWhereColumnIndex(idx);
            });
        }

        if (query != null) {
            query.forEach(oneQuery -> {
                String itemName = (String)oneQuery.get("item-name");
                context.addTargetColumnId(itemName);
                DecisionContext.Operation op = DecisionContext.detectOperation(itemName);
                String extracted = null;
                if (op != null) { // Extract value
                    extracted = DecisionContext.extractFunctionParameter(itemName, op);
                }
                if (extracted != null) {
                    context.addTargetColumnId(extracted);
                }
                context.addOperation(op);
                context.addRawTargetColumnValue(itemName);

                String itemToUse = (extracted != null ? stripQuotes(extracted) : itemName);
                if (!outputItems.contains(itemToUse) && !inputItems.contains(itemToUse)) {
                    throw new InvalidParameterException(String.format("%s [%s] Not Found in item list", "QUERY", itemToUse));
                }
                context.addTargetColumnIndex(outputItems.contains(itemToUse) ?
                        new DecisionContext.TargetColumnIndex(outputItems.indexOf(itemToUse), DecisionContext.TargetColumnIndex.InOrOut.OUPUT) :
                        new DecisionContext.TargetColumnIndex(inputItems.indexOf(itemToUse)));
            });
        }
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

        final List<String> targetColumnId = decisionUpdateContext.getTargetColumnId();
        final List<String> targetColumnNewValue = decisionUpdateContext.getTargetColumnValue();

        final List<DecisionContext.TargetColumnIndex> columnIndex = decisionUpdateContext.getWhereColumnIndex();
        final List<DecisionContext.TargetColumnIndex> targetColumnIndex = decisionUpdateContext.getTargetColumnIndex();

//        if (columnId != null) { // Mean there IS a where
//            System.out.printf("[%s] found at index %d\n", columnId, columnIndex);
//        }
//        System.out.printf("[%s] found at index %d\n", targetColumnId, targetColumnIndex);

        final List<Object> queryResult = new ArrayList<>();

        // Find the rules (line in the decision table)
        List<Map<String, Object>> rules = (List)((Map)jsonMap.get("logic")).get("rules");
        rules.forEach(rule -> {
            List<Map<String, Object>> inputEntries = (List)rule.get("inputEntries");
            List<Map<String, Object>> outputEntries = (List)rule.get("outputEntries");
            List<Map<String, Object>> annotationEntries = (List)rule.get("annotationEntries");

            boolean conditionMet = true;
            for (int ii=0; ii<columnIndex.size(); ii++) {
                DecisionContext.TargetColumnIndex colIdx = columnIndex.get(ii);
                List<Object> values = (colIdx.getWhere() == DecisionContext.TargetColumnIndex.InOrOut.OUPUT ?
                        Arrays.asList((String)outputEntries.get(colIdx.getIndex()).get("value")) :
                        getValues(inputEntries.get(colIdx.getIndex())));
                System.out.println("Comparing " + values + " and " + columnValue.get(ii));
                boolean met = (columnValue.get(ii) == null || values.contains(columnValue.get(ii)));
                conditionMet = conditionMet && met;
            }

            if (conditionMet) {
                // Loop on the output columns
                for (int outIndex = 0; outIndex < decisionUpdateContext.targetColumnIndex.size(); outIndex++) {
                    Map<String, Object> oneRowResult = new HashMap<>();
                    oneRowResult.put("item", decisionUpdateContext.targetColumnId.get(outIndex));
                    if (decisionUpdateContext.getOperation().get(outIndex) != null) {
                        if (decisionUpdateContext.getOperation().get(outIndex).equals(DecisionContext.Operation.RANGE)) {
                            Map<String, Object> range = (Map) inputEntries.get(targetColumnIndex.get(outIndex).getIndex()).get("range");
                            if (range != null) {
                                Object endpoint1 = range.get("endpoint1");
                                System.out.printf(">> Value for [%s]: currently [%s], moving to [%s]\n",
                                        (targetColumnId.get(outIndex) != null ? targetColumnId.get(outIndex) : "this line"),
                                        endpoint1,
                                        targetColumnNewValue.get(outIndex));
                                range.put("endpoint1", targetColumnNewValue.get(outIndex));
                                oneRowResult.put("from", endpoint1);
                                oneRowResult.put("to", targetColumnNewValue.get(outIndex));
                            } else {
                                oneRowResult.put("from", null);
                                oneRowResult.put("to", null);
                                System.out.println("No 'range' found where expected");
                                // throw new RuntimeException("No 'range' found where expected");
                            }
                        } else if (decisionUpdateContext.getOperation().get(outIndex).equals(DecisionContext.Operation.APPEND_TO_LIST)) {
                            List<String> values = (List) inputEntries.get(targetColumnIndex.get(outIndex).getIndex()).get("values");
                            if (values == null) {
                                String oneValue = (String) inputEntries.get(targetColumnIndex.get(outIndex).getIndex()).get("value");
                                if (oneValue != null) {
                                    values = new ArrayList();
                                    values.add(oneValue);
                                    inputEntries.get(targetColumnIndex.get(outIndex).getIndex()).put("values", values);
                                    inputEntries.get(targetColumnIndex.get(outIndex).getIndex()).remove("value");
                                }
                            }
                            if (values != null) {
                                // Check value validity
                                List<Map<String, Object>> suggestions = (List) inputEntries.get(targetColumnIndex.get(outIndex).getIndex()).get("suggestions");
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
                                        values.add(targetColumnNewValue.get(outIndex));
                                    } else {
                                        throw new RuntimeException(String.format("[%s] already there.", targetColumnNewValue));
                                    }
                                } else {
                                    throw new RuntimeException(String.format("[%s] not a valid option.", targetColumnNewValue));
                                }
                            } else {
                                throw new RuntimeException("No 'values' or 'value' found where expected");
                            }
                        } else if (decisionUpdateContext.getOperation().get(outIndex).equals(DecisionContext.Operation.DELETE_FROM_LIST)) {
                            // Leave at least one element...
                            List<String> values = (List) inputEntries.get(targetColumnIndex.get(outIndex).getIndex()).get("values");
                            if (values == null) {
                                String oneValue = (String) inputEntries.get(targetColumnIndex.get(outIndex).getIndex()).get("value");
                                if (oneValue != null) {
                                    if (targetColumnNewValue.get(outIndex).equals(oneValue)) {
                                        throw new RuntimeException(String.format("[%s] is the last value in the list. Leaving it.", targetColumnNewValue.get(outIndex)));
                                    } else {
                                        throw new RuntimeException(String.format("[%s] is not in the list", targetColumnNewValue.get(outIndex)));
                                    }
                                }
                            }
                            if (values != null) {
                                if (values.contains(targetColumnNewValue.get(outIndex))) {
                                    values.remove(targetColumnNewValue.get(outIndex));
                                } else {
                                    throw new RuntimeException(String.format("[%s] is not in the list", targetColumnNewValue.get(outIndex)));
                                }
                            } else {
                                throw new RuntimeException("No 'values' or 'value' found where expected");
                            }
                        }
                    } else {
                        // TODO Create a value node?
                        if (targetColumnIndex.get(outIndex).getWhere() != DecisionContext.TargetColumnIndex.InOrOut.OUPUT) {
                            throw new RuntimeException(String.format("Unmanaged operation [%s]", decisionUpdateContext.getRawTargetColumnValue().get(outIndex)));
                        } else {
                            String oldValue = (String) outputEntries.get(targetColumnIndex.get(outIndex).getIndex()).get("value");
                            outputEntries.get(targetColumnIndex.get(outIndex).getIndex()).put("value", targetColumnNewValue.get(outIndex));
                            oneRowResult.put("from", oldValue);
                            oneRowResult.put("to", targetColumnNewValue.get(outIndex));
                        }
                    }
                    queryResult.add(oneRowResult);
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
        } else {
            System.out.println("Update happened:" + mapper.writeValueAsString(queryResult));
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

        final List<String> targetColumnId = decisionUpdateContext.getTargetColumnId();
        final List<String> targetColumnNewValue = decisionUpdateContext.getTargetColumnValue();

        final List<DecisionContext.TargetColumnIndex> columnIndex = decisionUpdateContext.getWhereColumnIndex();
        final List<DecisionContext.TargetColumnIndex> targetColumnIndex = decisionUpdateContext.getTargetColumnIndex();

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
            for (int ii=0; ii<columnIndex.size(); ii++) { // Loop on the where clause elements
                DecisionContext.TargetColumnIndex colIdx = columnIndex.get(ii);
                List<Object> values = (colIdx.getWhere() == DecisionContext.TargetColumnIndex.InOrOut.OUPUT ?
                        Arrays.asList((String)outputEntries.get(colIdx.getIndex()).get("value")) :
                        getValues(inputEntries.get(colIdx.getIndex())));
                System.out.println("Comparing " + values + " and " + columnValue.get(ii));
                boolean met = (columnValue.get(ii) == null || values.contains(columnValue.get(ii)));
                conditionMet = conditionMet && met;
            }

            if (conditionMet) {
                List<Object> oneRowResult = new ArrayList<>();
                // Loop on the items to retrieve
                for (int outIndex = 0; outIndex < decisionUpdateContext.targetColumnIndex.size(); outIndex++) {
                    // Column targetColumnId
                    if (decisionUpdateContext.getOperation().get(outIndex) != null) {
                        if (decisionUpdateContext.getOperation().get(outIndex).equals(DecisionContext.Operation.RANGE)) {
                            Map<String, Object> range = (Map) inputEntries.get(targetColumnIndex.get(outIndex).getIndex()).get("range");
                            if (range != null) {
                                Object endpoint1 = range.get("endpoint1");
                                System.out.printf(">> Value for %s: currently %s\n", (columnValue.get(outIndex) != null ? columnValue.get(outIndex) : "this line"), endpoint1);
                                oneRowResult.add(endpoint1);
                            } else {
                                oneRowResult.add("Range NotFound");
//                                throw new RuntimeException("No 'range' found where expected");
                            }
                        }
                    } else {
                        // System.out.println("Query with no function");
                        if (targetColumnIndex.get(outIndex).getWhere() == DecisionContext.TargetColumnIndex.InOrOut.OUPUT) {
                            oneRowResult.add(outputEntries.get(targetColumnIndex.get(outIndex).getIndex()).get("value"));
                        } else {
                            Map<String, Object> itemObjectMap = inputEntries.get(targetColumnIndex.get(outIndex).getIndex());
                            if (itemObjectMap.get("range") != null) {
                                oneRowResult.add(itemObjectMap.get("range"));
                            } else if (itemObjectMap.get("values") != null) {
                                oneRowResult.add(itemObjectMap.get("values"));
                            } else if (itemObjectMap.get("value") != null) {
                                oneRowResult.add(itemObjectMap.get("value"));
                            } else {
                                oneRowResult.add(itemObjectMap);
                            }
                        }
                    }
                }
                queryResult.add(oneRowResult);
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

        public final static class TargetColumnIndex {
            enum InOrOut {
                INPUT,
                OUPUT
            }
            int index;
            InOrOut where = InOrOut.INPUT; // Default

            public TargetColumnIndex(int idx) {
                this(idx, InOrOut.INPUT);
            }

            public TargetColumnIndex(int idx, InOrOut where) {
                this.index = idx;
                this.where = where;
            }

            public int getIndex() {
                return this.index;
            }

            public InOrOut getWhere() {
                return where;
            }
        }

        // From the where clause
        List<String> whereColumnId = new ArrayList<>();
        List<TargetColumnIndex> whereColumnIndex = new ArrayList<>();
        List<String> whereColumnValue = new ArrayList<>();
        // From the query or update clause
        List<String> targetColumnId = new ArrayList<>();
        List<TargetColumnIndex> targetColumnIndex = new ArrayList<>();
        List<String> targetColumnValue = new ArrayList<>();
        List<String> rawTargetColumnValue = new ArrayList<>();
        List<Operation> operation = new ArrayList<>();

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

        public List<TargetColumnIndex> getWhereColumnIndex() {
            return whereColumnIndex;
        }

        public void setWhereColumnIndex(List<TargetColumnIndex> whereColumnIndex) {
            this.whereColumnIndex = whereColumnIndex;
        }
        public void addWhereColumnIndex(TargetColumnIndex whereColumnIndex) {
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

        public List<String> getTargetColumnId() {
            return targetColumnId;
        }

        public void setTargetColumnId(List<String> targetColumnId) {
            this.targetColumnId = targetColumnId;
        }

        public void addTargetColumnId(String targetColumnId) {
            this.targetColumnId.add(targetColumnId);
        }

        public List<TargetColumnIndex> getTargetColumnIndex() {
            return targetColumnIndex;
        }

        public void setTargetColumnIndex(List<TargetColumnIndex> targetColumnIndex) {
            this.targetColumnIndex = targetColumnIndex;
        }

        public void addTargetColumnIndex(TargetColumnIndex targetColumnIndex) {
            this.targetColumnIndex.add(targetColumnIndex);
        }

        public List<String> getTargetColumnValue() {
            return targetColumnValue;
        }

        public void setTargetColumnValue(List<String> targetColumnValue) {
            this.targetColumnValue = targetColumnValue;
        }

        public void addTargetColumnValue(String targetColumnValue) {
            this.targetColumnValue.add(targetColumnValue);
        }

        public List<Operation> getOperation() {
            return operation;
        }

        public void setOperation(List<Operation> operation) {
            this.operation = operation;
        }

        public void addOperation(Operation operation) {
            this.operation.add(operation);
        }

        public List<String> getRawTargetColumnValue() {
            return rawTargetColumnValue;
        }

        public void setRawTargetColumnValue(List<String> rawTargetColumnValue) {
            this.rawTargetColumnValue = rawTargetColumnValue;
        }

        public void addRawTargetColumnValue(String rawTargetColumnValue) {
            this.rawTargetColumnValue.add(rawTargetColumnValue);
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
