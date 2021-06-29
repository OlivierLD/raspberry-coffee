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

    private final static String PATH_ELEMENT = "path";
    private final static String MESSAGE_ELEMENT = "message";
    private final static String SEVERITY_ELEMENT = "severity";

    // Overlap
    private final static String OVERLAPPING_PATTERN_STR = "^Overlapping rules ([0-9]*), ([0-9]*)$";
    //                                                                         |         |
    //                                                                         |         Group 2
    //                                                                         Group 1
    private final static Pattern OVERLAPPING_PATTERN = Pattern.compile(OVERLAPPING_PATTERN_STR);
    // Path
    private final static String PATH_PATTERN_STR = "^(input|output)[ ]entry\\[([0-9]+),([0-9]+)\\]$";
    //                                                |                        |        |
    //                                                |                        |        Group 3
    //                                                |                        Group 2
    //                                                Group 1
    private final static Pattern PATH_PATTERN = Pattern.compile(PATH_PATTERN_STR);
    // Missing rule
    private final static String MISSING_RULE_PATTERN_STR = "^Missing rule with conditions: (.*)";
    //                                                                                      |
    //                                                                                      Group 1
    private final static Pattern MISSING_RULE_PATTERN = Pattern.compile(MISSING_RULE_PATTERN_STR);
    // ...and what else?

    public static class ProblemRepresentation {
        public enum ProblemType {
            ONE_LINE_PROBLEM,
            OVERLAP_PROBLEM,
            MISSING_RULE_PROBLEM
        }
        private Map<String, Object> originalMap;
        private ProblemType problemType;
        private String errorMessage; // TODO Something better

        public ProblemRepresentation originalMap(Map<String, Object> originalMap) {
            this.originalMap = originalMap;
            return this;
        }
        public ProblemRepresentation problemType(ProblemType problemType) {
            this.problemType = problemType;
            return this;
        }
        public ProblemRepresentation errorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }

        public Map<String, Object> getOriginalMap() {
            return originalMap;
        }

        public ProblemType getProblemType() {
            return problemType;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }

    /**
     * Parse ONE Problem
     * @param problem
     */
    public static ProblemRepresentation problemParser(Map<String, Object> problem) {
//        System.out.println("Parsing One problem");

        String message = (String)problem.get(MESSAGE_ELEMENT);
        String severity = (String)problem.get(SEVERITY_ELEMENT);
        List<String> path = (List)problem.get(PATH_ELEMENT);

        ProblemRepresentation problemRepresentation = new ProblemRepresentation().originalMap(problem);

        if (path == null) {
            Matcher matcher = OVERLAPPING_PATTERN.matcher(message);
            if (matcher.matches()) {
//                System.out.println("It is an overlap");
                String one = matcher.group(1);
                String two = matcher.group(2);
                int ruleOne = Integer.parseInt(one);
                int ruleTwo = Integer.parseInt(two);
                problemRepresentation.problemType(ProblemRepresentation.ProblemType.OVERLAP_PROBLEM);
                String associatedMessage = String.format("%s - Overlap between rules %d and %d", severity, ruleOne, ruleTwo);
                problemRepresentation.errorMessage(associatedMessage);
//                System.out.println(associatedMessage);
            } else {
                Matcher missingMatcher = MISSING_RULE_PATTERN.matcher(message);
                if (missingMatcher.matches()) {
                    String missingRuleMessage = missingMatcher.group(1);
                    problemRepresentation.problemType(ProblemRepresentation.ProblemType.MISSING_RULE_PROBLEM);
                    String associatedMessage = String.format("%s - Missing rule: %s", severity, message);
                    problemRepresentation.errorMessage(associatedMessage);
//                System.out.println(associatedMessage);
                } else {
                    System.out.println("Duh...");
                }
            }
        } else {
//            System.out.println("Path is here");
            path.forEach(pth -> {
                Matcher matcher = PATH_PATTERN.matcher(pth);
                if (matcher.matches()) {
                    String type = matcher.group(1).toUpperCase();
                    int line = Integer.parseInt(matcher.group(2));
                    int column = Integer.parseInt(matcher.group(3));
                    problemRepresentation.problemType(ProblemRepresentation.ProblemType.ONE_LINE_PROBLEM);
                    String associatedMessage = String.format("%s - one-line problem: %s, in %s columns, line #%d, col #%d", severity, message, type, line, column);
                    problemRepresentation.errorMessage(associatedMessage);
//                    System.out.println(associatedMessage);
                } else {
                    System.out.println("No path matching.");
                }
            });
        }
        return problemRepresentation;
    }

    public static void main(String... args) throws Exception {

        String PROBLEMS_JSON = "./json.v3/problems.json";

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
            ProblemRepresentation problemRepresentation = problemParser(pb);
            System.out.println(String.format("A [%s]: %s", problemRepresentation.getProblemType(), problemRepresentation.getErrorMessage()));
        });
        System.out.println("\nDone!");
    }
}
