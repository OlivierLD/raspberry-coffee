package oliv.oda.dtv2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.URL;

public class QueryDecisionTable {

    private final static String DT_JSON = "approval.strategy.dt.json"; // In the resource folder

    private static String jsonStatement;

    private final static String DT_DOCUMENT_PREFIX = "--decision-table:";
    private final static String TX_STATEMENT_PREFIX = "--transformation:";
    private final static String TX_FILE_PREFIX = "--transformation-file:";

    public static void main(String... args) throws Exception {

        String cliDT = null;
        URL resource = null;

        for (String arg : args) {
            if (arg.startsWith(DT_DOCUMENT_PREFIX)) {
                cliDT = arg.substring(DT_DOCUMENT_PREFIX.length());
                resource = new File(cliDT).toURI().toURL();
            } else if (arg.startsWith(TX_STATEMENT_PREFIX)) {
                jsonStatement = arg.substring(TX_STATEMENT_PREFIX.length());
            } else if (arg.startsWith(TX_FILE_PREFIX)) {
                String txFileName = arg.substring(TX_FILE_PREFIX.length());
                try (BufferedReader br = new BufferedReader(new FileReader(txFileName))) {
                    StringBuffer sb = new StringBuffer();
                    String line;
                    while ((line = br.readLine()) != null) {
                        sb.append(line);
                    }
                    jsonStatement = sb.toString();
                }
            }
        }

        if (cliDT == null) {
            ClassLoader classLoader = QueryDecisionTable.class.getClassLoader();
            resource = classLoader.getResource(DT_JSON); // At the root of the resources folder.
        }
        System.out.println("Resource: " + resource);
        System.out.println("---- Query Statement -----");
        System.out.println(jsonStatement);
        System.out.println("-----------------------");

        String queryResult = DecisionTablesStaticUtils.processQuery(resource.openStream(), jsonStatement);

        System.out.println("Query Result:\n" + queryResult);

        System.out.println("Done");
    }
}
