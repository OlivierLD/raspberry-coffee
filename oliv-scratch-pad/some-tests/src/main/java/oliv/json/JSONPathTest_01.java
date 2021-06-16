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
 * See this https://www.baeldung.com/guide-to-jayway-jsonpath
 */
public class JSONPathTest_01 {

    private final static String ORIGINAL_JSON_FILEPATH = "json/json.path.test.01.json";
    private final static String SECOND_JSON_FILEPATH = "json/json.path.test.02.json";

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

        System.out.println("\nDone");
    }

}
