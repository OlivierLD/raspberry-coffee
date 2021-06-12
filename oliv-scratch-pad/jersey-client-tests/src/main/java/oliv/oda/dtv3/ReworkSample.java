package oliv.oda.dtv3;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class ReworkSample {

    private final static String ORIGINAL_JSON_DOCUMENT_PREFIX = "--original-json:";
    private final static String REWORK_STMT_PREFIX = "--rework-stmt:";

    public static void main(String... args) {

        String originalJSONFile = null;
        String reworkStmtFile = null;

        for (int i=0; i<args.length; i++) {
            if (args[i].startsWith(ORIGINAL_JSON_DOCUMENT_PREFIX)) {
                originalJSONFile = args[i].substring(ORIGINAL_JSON_DOCUMENT_PREFIX.length());
            } else if (args[i].startsWith(REWORK_STMT_PREFIX)) {
                reworkStmtFile = args[i].substring(REWORK_STMT_PREFIX.length());
            }
        }
        if (originalJSONFile == null || reworkStmtFile == null) {
            throw new RuntimeException(String.format("Provide both CLI parameters %s and %s.", ORIGINAL_JSON_DOCUMENT_PREFIX, REWORK_STMT_PREFIX));
        }

        // Moving on.
        String originalJSON;
        String reworkStmt;
        try (BufferedReader br = new BufferedReader(new FileReader(originalJSONFile))) {
            StringBuffer sb = new StringBuffer();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            originalJSON = sb.toString();
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
        try (BufferedReader br = new BufferedReader(new FileReader(reworkStmtFile))) {
            StringBuffer sb = new StringBuffer();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            reworkStmt = sb.toString();
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }

        try {
            String reworked = DecisionTableStaticUtils.rework(originalJSON, reworkStmt);
            System.out.println("Reworked:");
            System.out.println(reworked);
        } catch (JsonProcessingException jpe) {
            throw new RuntimeException(jpe);
        }

    }

}
