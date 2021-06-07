package oliv.regexp;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParseURL2 {

    private final static String URL_PATTERN = "(http|ftp|https):\\/\\/([\\w_-]+(?:(?:\\.[\\w_-]+)+))([\\w.,@?^=%&:\\/~+#-]*[\\w@?^=%&\\/~+#-])?";
    public static void main(String... args) throws Exception {

        String urlString = "http://100.102.90.104:80/docs/books/tutorial/index.html?name=networking#DOWNLOADING";

        Pattern pattern = Pattern.compile(URL_PATTERN);
        Matcher matcher = pattern.matcher(urlString);
        System.out.printf("Match:%b (%d group(s))\n\n", matcher.matches(), matcher.groupCount());
        String protocol = matcher.group(1);
        String machine = matcher.group(2);
        System.out.println("protocol:" + protocol);
        System.out.println("machine:" + machine);

    }
}