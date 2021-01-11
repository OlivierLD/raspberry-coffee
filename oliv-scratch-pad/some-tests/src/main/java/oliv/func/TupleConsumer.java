package oliv.func;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class TupleConsumer {

    // Consumes one tuple of 3 elements
    private final static Consumer<List<Double>> tupleConsumer = tuple -> {
        assert(tuple.size() == 3);
        System.out.println(tuple.stream().map(dbl -> String.format("%.02f", dbl)).collect(Collectors.joining(", ")));
    };

    public static class Context {
        String whatever = "Akeu";
        int id = 123456;
    }

    private final static BiConsumer<Context, List<List<Double>>> tupleListConsumer = (ctx, tupleList) -> {
        System.out.println(String.format("With Context [%s, %d]:", ctx.whatever, ctx.id));
        tupleList.forEach(tuple -> {
            System.out.println(tuple.stream().map(dbl -> String.format("%.02f", dbl)).collect(Collectors.joining(", ")));
        });
    };
    
    private final static BiConsumer<Context, double[][]> tupleArrayConsumer = (ctx, tuples) -> {
        System.out.println(String.format("With Context [%s, %d]:", ctx.whatever, ctx.id));
        for (double[] tuple : tuples) {
            System.out.println(Arrays.stream(tuple)
                    .boxed()
                    .map(dbl -> String.format("%.02f", dbl))
                    .collect(Collectors.joining(", ")));
        }
    };

    public static void main(String... args) {
        tupleConsumer.accept(Arrays.asList(1d, 2d, 3d));
        System.out.println("------------------------------");
        Context ctx = new Context();
        tupleListConsumer.accept(ctx, Arrays.asList(
                Arrays.asList(1d, 2d, 3d),
                Arrays.asList(4d, 5d, 6d)));
        System.out.println("------------------------------");
        double matrix[][] = { {0d, 0d, 0d, 0d}, {0d, 1d, 2d, 3d}, {0d, 2d, 4d, 6d}, {0d, 3d, 6d, 9d} };
        tupleArrayConsumer.accept(ctx, matrix);
    }
}
