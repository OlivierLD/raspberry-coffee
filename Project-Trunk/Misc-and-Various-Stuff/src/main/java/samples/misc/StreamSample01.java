package samples.misc;

import java.util.List;

public class StreamSample01 {

    public static void main(String... args) {
        List<String> sampleList = List.of("A", "B", "C", "D");
        final String connector = "UNION";
        StringBuffer sb = new StringBuffer();
        sb.append(String.format("(%s)", sampleList.get(0)));
        for (int i=1; i<sampleList.size(); i++) {
            sb.append(String.format(" %s (%s)", connector, sampleList.get(i)));
        }
        System.out.println(sb.toString());
    }
}
