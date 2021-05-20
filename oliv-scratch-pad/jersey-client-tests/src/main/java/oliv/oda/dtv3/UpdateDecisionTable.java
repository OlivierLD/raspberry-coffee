package oliv.oda.dtv3;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class UpdateDecisionTable {

    private final static String DT_JSON = "approval.strategy.dt.json"; // In the resource folder

    private static String txStatement;
    private static String userContext;

    private final static String DT_DOCUMENT_PREFIX = "--decision-table:";
    private final static String USER_CONTEXT_PREFIX = "--context-file:";
    private final static String TX_FILE_PREFIX = "--transformation-file:";

    public static void main(String... args) throws Exception {

        String cliDT = null;
        URL resource = null;

        for (String arg : args) {
            if (arg.startsWith(DT_DOCUMENT_PREFIX)) {
                cliDT = arg.substring(DT_DOCUMENT_PREFIX.length());
                resource = new File(cliDT).toURI().toURL();
            } else if (arg.startsWith(USER_CONTEXT_PREFIX)) {
                String userContextFileName = arg.substring(USER_CONTEXT_PREFIX.length());
                try (BufferedReader br = new BufferedReader(new FileReader(userContextFileName))) {
                    StringBuffer sb = new StringBuffer();
                    String line;
                    while ((line = br.readLine()) != null) {
                        sb.append(line);
                    }
                    userContext = sb.toString();
                }
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
            ClassLoader classLoader = UpdateDecisionTable.class.getClassLoader();
            resource = classLoader.getResource(DT_JSON); // At the root of the resources folder.
        }
        System.out.println("Resource: " + resource);
        System.out.println("------- Context -------");
        System.out.println(userContext);
        System.out.println("-----------------------");
        System.out.println("---- TX Statement -----");
        System.out.println(txStatement);
        System.out.println("-----------------------");

        Map<String, Object> upsertResponseMap = new HashMap<>(); // That one is NOT immutable.
        // Default
        upsertResponseMap.put("upsertType", "update");
        upsertResponseMap.put("utterance", "{ }");
        upsertResponseMap.put("originalUtterance", "This is the utterance");

        String updated = DecisionTableStaticUtils.processUpdate(resource.openStream(), userContext, txStatement, upsertResponseMap);

        System.out.println("Updated:\n" + updated);

        System.out.println("Done");
    }
}
