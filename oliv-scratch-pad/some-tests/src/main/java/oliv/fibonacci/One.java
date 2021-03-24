package oliv.fibonacci;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class One {

    private static long fibonacci(int n) {
        if (n <= 1) {
            return n;
        }
        return fibonacci(n - 1) + fibonacci(n - 2);
    }

    public static void main(String... args) {
        List<Long> suite = new ArrayList<>();
        int length = 30;
        if (args.length == 1) {
            length = Integer.parseInt(args[0]);
        }
        for (int i=0; i<length; i++) {
            suite.add(fibonacci(i));
        }
        String result = suite.stream()
                .map(NumberFormat.getInstance()::format)
                .collect(Collectors.joining(", "));
        System.out.printf("[%s]%n", result);

    }
}
