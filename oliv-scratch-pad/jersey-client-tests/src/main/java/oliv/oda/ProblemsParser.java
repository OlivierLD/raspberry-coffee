package oliv.oda;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProblemsParser {

    private static ObjectMapper mapper = new ObjectMapper();

    private final static String PROBLEMS_JSON = "./json.v3/problems.json";

    // Overlap
    private final static String OVERLAPPING_PATTERN_STR = "^Overlapping rules ([0-9]*), ([0-9]*)$";
    private final static Pattern OVERLAPPING_PATTERN = Pattern.compile(OVERLAPPING_PATTERN_STR);
    // Path
    private final static String PATH_PATTERN_STR = "^(input|output)[ ]entry\\[([0-9]+),([0-9]+)\\]$";
    private final static Pattern PATH_PATTERN = Pattern.compile(PATH_PATTERN_STR);
    // ...and what else?

    private static void problemParser(Map<String, Object> problem) {
//        System.out.println("Parsing One problem");

        String message = (String)problem.get("message");
        String severity = (String)problem.get("severity");
        List<String> path = (List)problem.get("path");

        if (path == null) {
            Matcher matcher = OVERLAPPING_PATTERN.matcher(message);
            if (matcher.matches()) {
//                System.out.println("It is an overlap");
                String one = matcher.group(1);
                String two = matcher.group(2);
                int ruleOne = Integer.parseInt(one);
                int ruleTwo = Integer.parseInt(two);
                System.out.println(String.format("%s - Overlap between rules %d and %d", severity, ruleOne, ruleTwo));
            } else {
                System.out.println(String.format("%s - Not an overlap: %s", severity, message));
            }

        } else {
//            System.out.println("Path is here");
            path.forEach(pth -> {
                Matcher matcher = PATH_PATTERN.matcher(pth);
                if (matcher.matches()) {
                    String type = matcher.group(1).toUpperCase();
                    int line = Integer.parseInt(matcher.group(2));
                    int column = Integer.parseInt(matcher.group(3));
                    System.out.println(String.format("%s - one-line problem: %s, in %s columns, line #%d, col #%d", severity, message, type, line, column));
                } else {
                    System.out.println("No path matching.");
                }
            });
        }
    }

    public static void main(String... args) throws Exception {
        String originalJSON;
        try (BufferedReader br = new BufferedReader(new FileReader(PROBLEMS_JSON))) {
            StringBuffer sb = new StringBuffer();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            originalJSON = sb.toString();
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }

        Map<String, Object> map = mapper.readValue(originalJSON, Map.class);

//        System.out.println("Parsed.");
        List<Map<String, Object>> problems = (List)map.get("problems");
        problems.forEach(pb -> {
            problemParser(pb);
        });
        System.out.println("\nDone!");
    }
}
