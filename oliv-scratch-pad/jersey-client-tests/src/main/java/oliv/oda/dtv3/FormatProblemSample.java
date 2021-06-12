package oliv.oda.dtv3;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class FormatProblemSample {

    private final static String ORIGINAL_JSON_DOCUMENT_PREFIX = "--original-json:";

    public static void main(String... args) {

        String originalJSONFile = null;

        for (int i=0; i<args.length; i++) {
            if (args[i].startsWith(ORIGINAL_JSON_DOCUMENT_PREFIX)) {
                originalJSONFile = args[i].substring(ORIGINAL_JSON_DOCUMENT_PREFIX.length());
            }
        }
        if (originalJSONFile == null ) {
            throw new RuntimeException(String.format("Provide CLI parameter %s.", ORIGINAL_JSON_DOCUMENT_PREFIX));
        }

        // Moving on.
        String originalJSON;
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

        try {
            String formattedProblems = DecisionTableStaticUtils.formatProblems(originalJSON);
            System.out.println("Problems:");
            System.out.println(formattedProblems);
        } catch (JsonProcessingException jpe) {
            throw new RuntimeException(jpe);
        }

    }

}
