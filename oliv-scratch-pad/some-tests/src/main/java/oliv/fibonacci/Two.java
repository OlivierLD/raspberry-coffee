package oliv.fibonacci;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Two {

    private static class Context {
        long a = 0;
        long b = 0;
    }

    private static Context ctx = new Context();

    private static long fib(int n) {
        if (n == 0) {
            ctx.a = 0;
            return n;
        } else if (n == 1) {
            ctx.b = 1;
            return n;
        } else {
            long val = ctx.a + ctx.b;
            ctx.a = ctx.b;
            ctx.b = val;
            return val;
        }
    }

    private static List<Long> fibonacci(int n) {
        List<Long> suite = new ArrayList<>();
        for (int i=0; i<n; i++) {
            long val = fib(i);
            suite.add(val);
        }
        return suite;
    }

    public static void main(String... args) {
        int length = 30;
        if (args.length == 1) {
            length = Integer.parseInt(args[0]);
        }
        long before = System.currentTimeMillis();
        List<Long> suite = fibonacci(length);
        String result = suite.stream()
                .map(NumberFormat.getInstance()::format)
                .collect(Collectors.joining(", "));
        long after = System.currentTimeMillis();
        System.out.printf("Computation done in %s ms:%n", NumberFormat.getInstance().format(after - before));
        System.out.printf("[%s]%n", result);
    }
}
