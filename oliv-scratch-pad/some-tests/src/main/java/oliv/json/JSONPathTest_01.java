package oliv.json;

import com.jayway.jsonpath.Criteria;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.Filter;
import com.jayway.jsonpath.JsonPath;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * See this
 * https://www.baeldung.com/guide-to-jayway-jsonpath
 * https://support.smartbear.com/alertsite/docs/monitors/api/endpoint/jsonpath.html
 * https://jsonpath.com/
 *
 */
public class JSONPathTest_01 {

    private final static String ORIGINAL_JSON_FILEPATH = "json/json.path.test.01.json";
    private final static String SECOND_JSON_FILEPATH = "json/json.path.test.02.json";
    private final static String THIRD_JSON_FILEPATH = "json/json.path.test.03.json";

    private final static String FULL_JSON = "{\"formattedProblems\":\"- Error: Cannot convert abc to Number, [input entry[2,5]]\\n- Warning: Expected number for condition <abc, [input entry[2,5]]\\n\\n[First], Expense.Approver, Expense.Approvee, Expense.Employee Level, Expense.Remaining Budget, Expense.Amount, Expense.Type, Manager Justified, Strategy, Reason\\n1, johnny.gau@oracle.com, sam.thorpe@acme.com, -, -, {\\\"operation1\\\":\\\"<\\\",\\\"endpoint1\\\":\\\"120\\\"}, -, -, Approve, set my auto approval limit for Sam to $120\\n2, alex.smith@acme.com, sam.thorpe@acme.com, -, -, {\\\"operation1\\\":\\\"<\\\",\\\"endpoint1\\\":\\\"abc\\\"}, -, -, Approve, set my auto approval limit for sam to abc\\n3, alex.smith@acme.com, -, -, -, {\\\"operation1\\\":\\\"<\\\",\\\"endpoint1\\\":\\\"100\\\"}, -, -, Approve, sure, set my auto approval limit to $100\\n4, ernie.johnson@acme.com, -, -, -, {\\\"operation1\\\":\\\"<\\\",\\\"endpoint1\\\":\\\"2000\\\"}, -, -, Review, Ernie has recently had some expenses questioned\\n5, -, -, -, -, -, -, -, Review, No applicable manager preference\",\"hitPolicy\":\"First\",\"upsertType\":\"update\",\"newDecisionTableRules\":[{\"Expense.Approver\":\"johnny.gau@oracle.com\",\"Expense.Remaining Budget\":\"-\",\"Expense.Approvee\":\"sam.thorpe@acme.com\",\"Manager Justified\":\"-\",\"Expense.Amount\":{\"endpoint1\":\"120\",\"operation1\":\"<\"},\"Strategy\":\"Approve\",\"Expense.Type\":\"-\",\"Reason\":\"set my auto approval limit for Sam to $120\",\"Expense.Employee Level\":\"-\"},{\"Expense.Approver\":\"alex.smith@acme.com\",\"Expense.Remaining Budget\":\"-\",\"Expense.Approvee\":\"sam.thorpe@acme.com\",\"Manager Justified\":\"-\",\"Expense.Amount\":{\"endpoint1\":\"abc\",\"operation1\":\"<\"},\"Strategy\":\"Approve\",\"Expense.Type\":\"-\",\"Reason\":\"set my auto approval limit for sam to abc\",\"Expense.Employee Level\":\"-\"},{\"Expense.Approver\":\"alex.smith@acme.com\",\"Expense.Remaining Budget\":\"-\",\"Expense.Approvee\":\"-\",\"Manager Justified\":\"-\",\"Expense.Amount\":{\"endpoint1\":\"100\",\"operation1\":\"<\"},\"Strategy\":\"Approve\",\"Expense.Type\":\"-\",\"Reason\":\"sure, set my auto approval limit to $100\",\"Expense.Employee Level\":\"-\"},{\"Expense.Approver\":\"ernie.johnson@acme.com\",\"Expense.Remaining Budget\":\"-\",\"Expense.Approvee\":\"-\",\"Manager Justified\":\"-\",\"Expense.Amount\":{\"endpoint1\":\"2000\",\"operation1\":\"<\"},\"Strategy\":\"Review\",\"Expense.Type\":\"-\",\"Reason\":\"Ernie has recently had some expenses questioned\",\"Expense.Employee Level\":\"-\"},{\"Expense.Approver\":\"-\",\"Expense.Remaining Budget\":\"-\",\"Expense.Approvee\":\"-\",\"Manager Justified\":\"-\",\"Expense.Amount\":\"-\",\"Strategy\":\"Review\",\"Expense.Type\":\"-\",\"Reason\":\"No applicable manager preference\",\"Expense.Employee Level\":\"-\"}],\"originalRulesValues\":[{\"outputEntries\":[{\"mode\":\"Text\",\"allowedModes\":[\"Text\",\"Advanced\"],\"@class\":\".Builder\",\"role\":\"Rule Output\",\"suggestions\":[{\"mode\":\"Text\",\"value\":\"Reject\"},{\"mode\":\"Text\",\"value\":\"Review\"},{\"mode\":\"Text\",\"value\":\"Approve\"}],\"suggest\":true,\"value\":\"Approve\"},{\"mode\":\"Text\",\"allowedModes\":[\"Text\",\"Advanced\"],\"@class\":\".Builder\",\"role\":\"Rule Output\",\"suggest\":true,\"value\":\"set my auto approval limit for Sam to $120\"}],\"annotationEntries\":[\"\",\"\"],\"inputEntries\":[{\"mode\":\"Text\",\"allowedModes\":[\"Text\",\"Any\",\"Advanced\"],\"not\":false,\"@class\":\".Builder\",\"role\":\"Rule Test\",\"suggest\":true,\"value\":\"johnny.gau@oracle.com\"},{\"mode\":\"Text\",\"allowedModes\":[\"Text\",\"Any\",\"Advanced\"],\"not\":false,\"@class\":\".Builder\",\"role\":\"Rule Test\",\"suggest\":true,\"value\":\"sam.thorpe@acme.com\"},{\"mode\":\"Any\",\"allowedModes\":[\"Number\",\"Any\",\"Advanced\"],\"not\":false,\"@class\":\".Builder\",\"role\":\"Rule Test\",\"suggest\":true,\"value\":\"-\"},{\"mode\":\"Any\",\"allowedModes\":[\"Number\",\"Any\",\"Advanced\"],\"not\":false,\"@class\":\".Builder\",\"role\":\"Rule Test\",\"suggest\":true,\"value\":\"-\"},{\"mode\":\"Number\",\"allowedModes\":[\"Number\",\"Any\",\"Advanced\"],\"not\":false,\"@class\":\".Builder\",\"role\":\"Rule Test\",\"range\":{\"endpoint1\":\"120\",\"operation1\":\"<\"},\"suggest\":true},{\"mode\":\"Any\",\"allowedModes\":[\"Text\",\"Any\",\"Advanced\"],\"not\":false,\"@class\":\".Builder\",\"role\":\"Rule Test\",\"suggestions\":[{\"mode\":\"Text\",\"value\":\"Travel\"},{\"mode\":\"Text\",\"value\":\"Procurement\"},{\"mode\":\"Text\",\"value\":\"Onboarding\"}],\"suggest\":true,\"value\":\"-\"},{\"mode\":\"Any\",\"allowedModes\":[\"True or False\",\"Any\",\"Advanced\"],\"not\":false,\"@class\":\".Builder\",\"role\":\"Rule Test\",\"suggest\":true,\"value\":\"-\"}]},{\"outputEntries\":[{\"mode\":\"Text\",\"allowedModes\":[\"Text\",\"Advanced\"],\"@class\":\".Builder\",\"role\":\"Rule Output\",\"suggestions\":[{\"mode\":\"Text\",\"value\":\"Reject\"},{\"mode\":\"Text\",\"value\":\"Review\"},{\"mode\":\"Text\",\"value\":\"Approve\"}],\"suggest\":true,\"value\":\"Approve\"},{\"mode\":\"Text\",\"allowedModes\":[\"Text\",\"Advanced\"],\"@class\":\".Builder\",\"role\":\"Rule Output\",\"suggest\":true,\"value\":\"set my auto approval limit for sam to abc\"}],\"annotationEntries\":[\"\",\"\"],\"inputEntries\":[{\"mode\":\"Text\",\"allowedModes\":[\"Text\",\"Any\",\"Advanced\"],\"not\":false,\"@class\":\".Builder\",\"role\":\"Rule Test\",\"suggest\":true,\"value\":\"alex.smith@acme.com\"},{\"mode\":\"Text\",\"allowedModes\":[\"Text\",\"Any\",\"Advanced\"],\"not\":false,\"@class\":\".Builder\",\"role\":\"Rule Test\",\"suggest\":true,\"value\":\"sam.thorpe@acme.com\"},{\"mode\":\"Any\",\"allowedModes\":[\"Number\",\"Any\",\"Advanced\"],\"not\":false,\"@class\":\".Builder\",\"role\":\"Rule Test\",\"suggest\":true,\"value\":\"-\"},{\"mode\":\"Any\",\"allowedModes\":[\"Number\",\"Any\",\"Advanced\"],\"not\":false,\"@class\":\".Builder\",\"role\":\"Rule Test\",\"suggest\":true,\"value\":\"-\"},{\"mode\":\"Number\",\"allowedModes\":[\"Number\",\"Any\",\"Advanced\"],\"not\":false,\"@class\":\".Builder\",\"role\":\"Rule Test\",\"range\":{\"endpoint1\":\"abc\",\"operation1\":\"<\"},\"suggest\":true,\"problems\":[{\"severity\":\"Error\",\"message\":\"Cannot convert abc to Number\"},{\"severity\":\"Warning\",\"message\":\"Expected number for condition <abc\"}]},{\"mode\":\"Any\",\"allowedModes\":[\"Text\",\"Any\",\"Advanced\"],\"not\":false,\"@class\":\".Builder\",\"role\":\"Rule Test\",\"suggestions\":[{\"mode\":\"Text\",\"value\":\"Travel\"},{\"mode\":\"Text\",\"value\":\"Procurement\"},{\"mode\":\"Text\",\"value\":\"Onboarding\"}],\"suggest\":true,\"value\":\"-\"},{\"mode\":\"Any\",\"allowedModes\":[\"True or False\",\"Any\",\"Advanced\"],\"not\":false,\"@class\":\".Builder\",\"role\":\"Rule Test\",\"suggest\":true,\"value\":\"-\"}]},{\"outputEntries\":[{\"mode\":\"Text\",\"allowedModes\":[\"Text\",\"Advanced\"],\"@class\":\".Builder\",\"role\":\"Rule Output\",\"suggestions\":[{\"mode\":\"Text\",\"value\":\"Reject\"},{\"mode\":\"Text\",\"value\":\"Review\"},{\"mode\":\"Text\",\"value\":\"Approve\"}],\"suggest\":true,\"value\":\"Approve\"},{\"mode\":\"Text\",\"allowedModes\":[\"Text\",\"Advanced\"],\"@class\":\".Builder\",\"role\":\"Rule Output\",\"suggest\":true,\"value\":\"sure, set my auto approval limit to $100\"}],\"annotationEntries\":[\"\",\"\"],\"inputEntries\":[{\"mode\":\"Text\",\"allowedModes\":[\"Text\",\"Any\",\"Advanced\"],\"not\":false,\"@class\":\".Builder\",\"role\":\"Rule Test\",\"suggest\":true,\"value\":\"alex.smith@acme.com\"},{\"mode\":\"Any\",\"allowedModes\":[\"Text\",\"Any\",\"Advanced\"],\"not\":false,\"@class\":\".Builder\",\"role\":\"Rule Test\",\"suggest\":true,\"value\":\"-\"},{\"mode\":\"Any\",\"allowedModes\":[\"Number\",\"Any\",\"Advanced\"],\"not\":false,\"@class\":\".Builder\",\"role\":\"Rule Test\",\"suggest\":true,\"value\":\"-\"},{\"mode\":\"Any\",\"allowedModes\":[\"Number\",\"Any\",\"Advanced\"],\"not\":false,\"@class\":\".Builder\",\"role\":\"Rule Test\",\"suggest\":true,\"value\":\"-\"},{\"mode\":\"Number\",\"allowedModes\":[\"Number\",\"Any\",\"Advanced\"],\"not\":false,\"@class\":\".Builder\",\"role\":\"Rule Test\",\"range\":{\"endpoint1\":\"100\",\"operation1\":\"<\"},\"suggest\":true},{\"mode\":\"Any\",\"allowedModes\":[\"Text\",\"Any\",\"Advanced\"],\"not\":false,\"@class\":\".Builder\",\"role\":\"Rule Test\",\"suggestions\":[{\"mode\":\"Text\",\"value\":\"Travel\"},{\"mode\":\"Text\",\"value\":\"Procurement\"},{\"mode\":\"Text\",\"value\":\"Onboarding\"}],\"suggest\":true,\"value\":\"-\"},{\"mode\":\"Any\",\"allowedModes\":[\"True or False\",\"Any\",\"Advanced\"],\"not\":false,\"@class\":\".Builder\",\"role\":\"Rule Test\",\"suggest\":true,\"value\":\"-\"}]},{\"outputEntries\":[{\"mode\":\"Text\",\"allowedModes\":[\"Text\",\"Advanced\"],\"@class\":\".Builder\",\"role\":\"Rule Output\",\"suggestions\":[{\"mode\":\"Text\",\"value\":\"Reject\"},{\"mode\":\"Text\",\"value\":\"Review\"},{\"mode\":\"Text\",\"value\":\"Approve\"}],\"suggest\":true,\"value\":\"Review\"},{\"mode\":\"Text\",\"allowedModes\":[\"Text\",\"Advanced\"],\"@class\":\".Builder\",\"role\":\"Rule Output\",\"suggest\":true,\"value\":\"Ernie has recently had some expenses questioned\"}],\"annotationEntries\":[\"\",\"\"],\"inputEntries\":[{\"mode\":\"Text\",\"allowedModes\":[\"Text\",\"Any\",\"Advanced\"],\"not\":false,\"@class\":\".Builder\",\"role\":\"Rule Test\",\"suggest\":true,\"value\":\"ernie.johnson@acme.com\"},{\"mode\":\"Any\",\"allowedModes\":[\"Text\",\"Any\",\"Advanced\"],\"not\":false,\"@class\":\".Builder\",\"role\":\"Rule Test\",\"suggest\":true,\"value\":\"-\"},{\"mode\":\"Any\",\"allowedModes\":[\"Number\",\"Any\",\"Advanced\"],\"not\":false,\"@class\":\".Builder\",\"role\":\"Rule Test\",\"suggest\":true,\"value\":\"-\"},{\"mode\":\"Any\",\"allowedModes\":[\"Number\",\"Any\",\"Advanced\"],\"not\":false,\"@class\":\".Builder\",\"role\":\"Rule Test\",\"suggest\":true,\"value\":\"-\"},{\"mode\":\"Number\",\"allowedModes\":[\"Number\",\"Any\",\"Advanced\"],\"not\":false,\"@class\":\".Builder\",\"role\":\"Rule Test\",\"range\":{\"endpoint1\":\"2000\",\"operation1\":\"<\"},\"suggest\":true},{\"mode\":\"Any\",\"allowedModes\":[\"Text\",\"Any\",\"Advanced\"],\"not\":false,\"@class\":\".Builder\",\"role\":\"Rule Test\",\"suggestions\":[{\"mode\":\"Text\",\"value\":\"Travel\"},{\"mode\":\"Text\",\"value\":\"Procurement\"},{\"mode\":\"Text\",\"value\":\"Onboarding\"}],\"suggest\":true,\"value\":\"-\"},{\"mode\":\"Any\",\"allowedModes\":[\"True or False\",\"Any\",\"Advanced\"],\"not\":false,\"@class\":\".Builder\",\"role\":\"Rule Test\",\"suggest\":true,\"value\":\"-\"}]},{\"outputEntries\":[{\"mode\":\"Text\",\"allowedModes\":[\"Text\",\"Advanced\"],\"@class\":\".Builder\",\"role\":\"Rule Output\",\"suggestions\":[{\"mode\":\"Text\",\"value\":\"Reject\"},{\"mode\":\"Text\",\"value\":\"Review\"},{\"mode\":\"Text\",\"value\":\"Approve\"}],\"suggest\":true,\"value\":\"Review\"},{\"mode\":\"Text\",\"allowedModes\":[\"Text\",\"Advanced\"],\"@class\":\".Builder\",\"role\":\"Rule Output\",\"suggest\":true,\"value\":\"No applicable manager preference\"}],\"annotationEntries\":[\"\",\"\"],\"inputEntries\":[{\"mode\":\"Any\",\"allowedModes\":[\"Text\",\"Any\",\"Advanced\"],\"not\":false,\"@class\":\".Builder\",\"role\":\"Rule Test\",\"suggest\":true,\"value\":\"-\"},{\"mode\":\"Any\",\"allowedModes\":[\"Text\",\"Any\",\"Advanced\"],\"not\":false,\"@class\":\".Builder\",\"role\":\"Rule Test\",\"suggest\":true,\"value\":\"-\"},{\"mode\":\"Any\",\"allowedModes\":[\"Number\",\"Any\",\"Advanced\"],\"not\":false,\"@class\":\".Builder\",\"role\":\"Rule Test\",\"suggest\":true,\"value\":\"-\"},{\"mode\":\"Any\",\"allowedModes\":[\"Number\",\"Any\",\"Advanced\"],\"not\":false,\"@class\":\".Builder\",\"role\":\"Rule Test\",\"suggest\":true,\"value\":\"-\"},{\"mode\":\"Any\",\"allowedModes\":[\"Number\",\"Any\",\"Advanced\"],\"not\":false,\"@class\":\".Builder\",\"role\":\"Rule Test\",\"suggest\":true,\"value\":\"-\"},{\"mode\":\"Any\",\"allowedModes\":[\"Text\",\"Any\",\"Advanced\"],\"not\":false,\"@class\":\".Builder\",\"role\":\"Rule Test\",\"suggestions\":[{\"mode\":\"Text\",\"value\":\"Travel\"},{\"mode\":\"Text\",\"value\":\"Procurement\"},{\"mode\":\"Text\",\"value\":\"Onboarding\"}],\"suggest\":true,\"value\":\"-\"},{\"mode\":\"Any\",\"allowedModes\":[\"True or False\",\"Any\",\"Advanced\"],\"not\":false,\"@class\":\".Builder\",\"role\":\"Rule Test\",\"suggest\":true,\"value\":\"-\"}]}],\"originalUtterance\":\"set my auto approval limit for sam to abc\",\"problems\":[{\"severity\":\"Error\",\"path\":[\"input entry[2,5]\"],\"message\":\"Cannot convert abc to Number\"},{\"severity\":\"Warning\",\"path\":[\"input entry[2,5]\"],\"message\":\"Expected number for condition <abc\"}]}";

    public static void main(String... args) {
        // Load the JSON file

        String jsonpathCreatorNamePath = "$['tool']['jsonpath']['creator']['name']";
        String jsonpathCreatorLocationPath = "$['tool']['jsonpath']['creator']['location'][*]";

        String originalJSON;
        try (BufferedReader br = new BufferedReader(new FileReader(ORIGINAL_JSON_FILEPATH))) {
            StringBuffer sb = new StringBuffer();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            originalJSON = sb.toString();
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }

        // Let's go
        DocumentContext jsonContext = JsonPath.parse(originalJSON);
        String jsonpathCreatorName = jsonContext.read(jsonpathCreatorNamePath);
        List<String> jsonpathCreatorLocation = jsonContext.read(jsonpathCreatorLocationPath);

        System.out.println(String.format(">> One: %s", jsonpathCreatorName));
        System.out.println(">> Two:");
        System.out.println(jsonpathCreatorLocation.stream()
                .map(loc -> "- " + loc)
                .collect(Collectors.joining("\n")));

        // Java Filters
        Filter expensiveFilter = Filter.filter(Criteria.where("price").gt(20.00));
        List<Map<String, Object>> expensive = JsonPath.parse(originalJSON)
                .read("$['book'][?]", expensiveFilter);

        System.out.println(">> Java Filtered:");
        expensive.stream()
                .forEach(result -> {
                    System.out.printf("%s -> %s\n", result.get("title"), result.get("price"));
                });

        // In-line filters
//        Object dataObject = JsonPath.parse(originalJSON).read("$[?(@.price > 20)]");
        Object dataObject = JsonPath.parse(originalJSON).read("$['book'][?(@.price > 20)]");
//        String dataString = dataObject.toString();
        if (dataObject != null) {
            System.out.println(">> In-Line Filtered:");
            if (dataObject instanceof List) {
                ((List<Map<String, Object>>)dataObject).stream()
                        .forEach(result -> {
                            System.out.printf("%s -> %s\n", result.get("title"), result.get("price"));
                        });
            } else {
                System.out.println(dataObject.toString());
            }
        } else {
            System.out.println("Nope.");
        }

        // With the filename as prm
        try {
            Object expensiveBooks = JsonPath.parse(new File(SECOND_JSON_FILEPATH)).read("$[?(@.id > 2)]");
            if (expensiveBooks instanceof List) {
                ((List<Map<String, Object>>)expensiveBooks)
                        .stream()
                        .forEach(result -> System.out.printf("%s, released [%s]\n", result.get("title"), new Date((long)result.get("release date"))));
            } else {
                System.out.println(expensiveBooks.toString());
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        try {
            DocumentContext jsonContext_2 = JsonPath.parse(new File(THIRD_JSON_FILEPATH));
            Object problems = jsonContext_2.read("$.problems");
            System.out.printf("'problems' is a %s\n", problems.getClass().getName());
            if (problems instanceof List) {
                System.out.println("'problems' is a List");
                ((List)problems).forEach(System.out::println);
            } else {
                System.out.println("No. Not a List");
            }

            Object messages = jsonContext_2.read("$['problems'][*]['message']");
            System.out.printf("'messages' is a %s\n", messages.getClass().getName());
            if (messages instanceof List) {
                ((List<?>) messages).forEach(message -> System.out.printf("--> (%s) %s\n", message.getClass().getName(), message));
            }

            Object interpretation = jsonContext_2.read("$.interpretation");
            System.out.printf("'interpretation' is a %s\n", interpretation.getClass().getName());

            Object question = jsonContext_2.read("$.question");
            System.out.printf("'question' is a %s\n", question.getClass().getName());

        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        try {
            DocumentContext jsonContext_ = JsonPath.parse(FULL_JSON);
            Object problems = jsonContext_.read("$.problems");
            System.out.println("Bam.");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        System.out.println("\nDone");
    }

}
