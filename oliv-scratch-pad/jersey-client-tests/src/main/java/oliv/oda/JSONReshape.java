package oliv.oda;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class JSONReshape {

    private final static String inputJson = "{\"interpretation\":{\"Strategy\":{\"Strategy\":\"Reject\"},\"Reasons\":{\"Corporate\":\"MSDN is prohibited\",\"ManagerPref\":\"Set my auto approval limit to $100\"}},\"problems\":[{\"message\":\"No rules match\",\"path\":[\"Justification Keywords\"],\"severity\":\"Warning\"}]}";

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

    private static String extractFromMap(Map<String, Object> map, String path) {
        String extracted = null;
        String[] pathElements = path.split("\\.");
        for (String elem : pathElements) {
//                              System.out.println("Elem:" + elem);
            Object subMap = map.get(elem);
            if (subMap != null) {
                if (subMap instanceof Map) {
                    map = (Map<String, Object>) subMap;
                } else {
                    extracted = subMap.toString();
                }
            } else {
                break;
            }
        }
        return extracted;
    }

    private final static String REWORK_FUNC_NAME = "rework(";
    private static String rework(String json, String stmt) throws JsonProcessingException {

        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> finalMap = new HashMap<>();

        String txStmt = stmt.trim();
        try {
            final Map<String, Object> jsonMap = mapper.readValue(json, Map.class);
            if (txStmt.startsWith(REWORK_FUNC_NAME) && txStmt.endsWith(")")) {

                String stmtContent = txStmt.substring(REWORK_FUNC_NAME.length(), txStmt.length() - 1);

                String[] txElement = stmtContent.split(",");

                Arrays.asList(txElement).forEach(onePath -> {
                    String[] nv = onePath.trim().split(":");
                    if (nv != null && nv.length == 2) {
                        String name = stripQuotes(nv[0].trim());
                        String path = stripQuotes(nv[1].trim());
                        String extracted = extractFromMap(jsonMap, path);
                        finalMap.put(name, extracted);
                    }
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return mapper.writeValueAsString(finalMap);
    }

    public static void main(String... args) {

        String reworkStmt = "rework(\'strategy\': \'interpretation.Strategy.Strategy\', \'manager\': \'interpretation.Reasons.ManagerPref\')";

        try {
            String reworked = rework(inputJson, reworkStmt);
            System.out.println(reworked);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }
}
