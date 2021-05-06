package oliv.oda;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.net.URL;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PatchDecisionTable {

    private final static String DT_JSON = "approval.strategy.dt.json"; // In the resource folder

    private static String txStatement = "{" +
            "\"update\": \"'Approval Amount' = range(350)\"," +
            "\"where\": \"'Manager' = 'Alex'\"" +
            "}";

    public static class DecisionUpdateContext {

        public enum Operation {
            RANGE("range"),
            APPEND_TO_LIST("appendToList"),
            DELETE_FROM_LIST("deleteFromList");

            private final String functionName;

            Operation(String functionName) {
                this.functionName = functionName;
            }
            public String functionName() {
                return this.functionName;
            }
        }

        public enum StatementType {
            UPDATE,
            QUERY
        }

        StatementType statementType = StatementType.UPDATE;
        String whereColumnId;
        int whereColumnIndex;
        String whereColumnValue;
        String targetColumnId;
        int targetColumnIndex;
        String targetColumnValue;
        String rawTargetColumnValue;
        Operation operation;

        public DecisionUpdateContext() {}

        public String getWhereColumnId() {
            return whereColumnId;
        }

        public void setWhereColumnId(String whereColumnId) {
            this.whereColumnId = whereColumnId;
        }

        public int getWhereColumnIndex() {
            return whereColumnIndex;
        }

        public void setWhereColumnIndex(int whereColumnIndex) {
            this.whereColumnIndex = whereColumnIndex;
        }

        public String getWhereColumnValue() {
            return whereColumnValue;
        }

        public void setWhereColumnValue(String whereColumnValue) {
            this.whereColumnValue = whereColumnValue;
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

        public StatementType getStatementType() {
            return statementType;
        }

        public void setStatementType(StatementType statementType) {
            this.statementType = statementType;
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

    private static List<String> getItemList(Map<java.lang.String, Object> decisionMap) {
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

    private static DecisionUpdateContext setUpdateContext(Map<String, Object> decisionMap,
                                                          Map<String, Object> txMap) throws Exception {
        DecisionUpdateContext context = new DecisionUpdateContext();
        String update = (String)txMap.get("update");
        String query = (String)txMap.get("query");
        String where = (String)txMap.get("where");

        if (where != null) {
            String[] whereSplit = where.split("=");
            context.setWhereColumnId(stripQuotes(whereSplit[0].trim()));
            context.setWhereColumnValue(stripQuotes(whereSplit[1].trim()));
        }

        if (update != null) {
            context.setStatementType(DecisionUpdateContext.StatementType.UPDATE);
            String[] updateSplit = update.split("=");
            context.setTargetColumnId((stripQuotes(updateSplit[0].trim())));
            context.setTargetColumnValue(stripQuotes(updateSplit[1].trim()));
            context.setRawTargetColumnValue(stripQuotes(updateSplit[1].trim()));
        }

        if (query != null) {
            // TODO Make sure there is a 'where' ?
            context.setStatementType(DecisionUpdateContext.StatementType.QUERY);
            context.setTargetColumnId((stripQuotes(query)));
            DecisionUpdateContext.Operation op = DecisionUpdateContext.detectOperation(context.getTargetColumnId());
            if (op != null) {
                // Extract value
                String extracted = DecisionUpdateContext.extractFunctionParameter(context.getTargetColumnId(), op);
                context.setTargetColumnId(stripQuotes(extracted));
            }
            context.setOperation(op);
//            context.setTargetColumnValue(query);
            context.setRawTargetColumnValue(stripQuotes(query));
        }

        List<String> items = getItemList(decisionMap);
        if (verbose) {
            System.out.println("--- ITEMS ---");
            items.forEach(System.out::println);
            System.out.println("-------------");
        }

        if (where != null && !items.contains(context.getWhereColumnId())) {
            throw new InvalidParameterException(String.format("WHERE [%s] Not Found in item list", context.getWhereColumnId()));
        }
        if (!items.contains(context.getTargetColumnId())) {
            throw new InvalidParameterException(String.format("%s [%s] Not Found in item list", context.getStatementType(), context.getTargetColumnId()));
        }
        // Find indexes
        if (where != null) {
            context.setWhereColumnIndex(items.indexOf(context.getWhereColumnId()));
        }
        context.setTargetColumnIndex(items.indexOf(context.getTargetColumnId()));

        // Operation, on the value to update
        if (DecisionUpdateContext.StatementType.UPDATE.equals(context.getStatementType())) {
            DecisionUpdateContext.Operation op = DecisionUpdateContext.detectOperation(context.getTargetColumnValue());
            if (op != null) {
                // Extract value
                String extracted = DecisionUpdateContext.extractFunctionParameter(context.getTargetColumnValue(), op);
                context.setTargetColumnValue(stripQuotes(extracted));
            }
            context.setOperation(op);
        }
        return context;
    }

    private static String processUpdate(InputStream original, String txSyntax) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        // THE Decision Table Object
        Map<String, Object> jsonMap = mapper.readValue(original, Map.class);
        // The transformation directive
        Map<String, Object> tx = mapper.readValue(txSyntax, Map.class);
        // Set Update Context
        DecisionUpdateContext decisionUpdateContext = setUpdateContext(jsonMap, tx);

        final String columnId = decisionUpdateContext.getWhereColumnId();
        final String columnValue = decisionUpdateContext.getWhereColumnValue();

        final String targetColumnId = decisionUpdateContext.getTargetColumnId();
        final String targetColumnNewValue = decisionUpdateContext.getTargetColumnValue();

        final int columnIndex = decisionUpdateContext.getWhereColumnIndex();
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

            Object value = (inputEntries.get(columnIndex).get("value"));
            if (columnValue == null || columnValue.equals(value.toString())) {
                // Column targetColumnId
                if (decisionUpdateContext.getOperation() != null) {
                    if (decisionUpdateContext.getOperation().equals(DecisionUpdateContext.Operation.RANGE)) {
                        Map<String, Object> range = (Map) inputEntries.get(targetColumnIndex).get("range");
                        if (range != null) {
                            Object endpoint1 = range.get("endpoint1");
                            if (DecisionUpdateContext.StatementType.UPDATE.equals(decisionUpdateContext.getStatementType())) {
                                System.out.printf(">> Value for %s: currently %s, moving to %s\n", (columnValue != null ? columnValue : "this line"), endpoint1, targetColumnNewValue);
                                range.put("endpoint1", targetColumnNewValue);
                            } else {
                                System.out.printf(">> Value for %s: currently %s\n", (columnValue != null ? columnValue : "this line"), endpoint1);
                                queryResult.add(endpoint1);
                            }
                        } else {
                            throw new RuntimeException("No 'range' found where expected");
                        }
                    } else if (decisionUpdateContext.getOperation().equals(DecisionUpdateContext.Operation.APPEND_TO_LIST)) {
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
                    } else if (decisionUpdateContext.getOperation().equals(DecisionUpdateContext.Operation.DELETE_FROM_LIST)) {
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
                    if (DecisionUpdateContext.StatementType.UPDATE.equals(decisionUpdateContext.getStatementType())) {
                        // TODO Create a value node?
                        throw new RuntimeException(String.format("Unmanaged operation [%s]", decisionUpdateContext.getRawTargetColumnValue()));
                    } else {
                        // System.out.println("Query with no function");
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
        if (DecisionUpdateContext.StatementType.UPDATE.equals(decisionUpdateContext.getStatementType())) {
            jsonInString = mapper.writeValueAsString(jsonMap);
        } else {
            if (queryResult.size() > 0) {
                jsonInString = mapper.writeValueAsString(queryResult.get(0));
            } else {
                jsonInString = "NOT_FOUND";
            }
        }
        return jsonInString;
    }

    private final static String DT_DOCUMENT_PREFIX = "--decision-table:";
    private final static String TX_STATEMENT_PREFIX = "--transformation:";
    private final static String TX_FILE_PREFIX = "--transformation-file:";

    private final static boolean verbose = true;

    public static void main(String... args) throws Exception {

        String cliDT = null;
        URL resource = null;

        for (String arg : args) {
            if (arg.startsWith(DT_DOCUMENT_PREFIX)) {
                cliDT = arg.substring(DT_DOCUMENT_PREFIX.length());
                resource = new File(cliDT).toURI().toURL();
            } else if (arg.startsWith(TX_STATEMENT_PREFIX)) {
                txStatement = arg.substring(TX_STATEMENT_PREFIX.length());
            } else if (arg.startsWith(TX_FILE_PREFIX)) {
                String txFileName = arg.substring(TX_FILE_PREFIX.length());
                try (BufferedReader br = new BufferedReader(new FileReader(txFileName))) {
                    StringBuffer sb = new StringBuffer();
                    String line;
                    while ((line = br.readLine()) != null) {
                        sb.append(line);
                    }
                    txStatement = sb.toString();
                }
            }
        }

        if (cliDT == null) {
            ClassLoader classLoader = PatchDecisionTable.class.getClassLoader();
            resource = classLoader.getResource(DT_JSON); // At the root of the resources folder.
        }
        System.out.println("Resource: " + resource);
        System.out.println("---- TX Statement -----");
        System.out.println(txStatement);
        System.out.println("-----------------------");

        String updated = processUpdate(resource.openStream(), txStatement);

        System.out.println("Final Result:\n" + updated);

        System.out.println("Done");
    }
}
