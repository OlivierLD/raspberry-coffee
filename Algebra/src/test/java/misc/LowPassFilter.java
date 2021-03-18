package misc;

import lowpass.Filter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class LowPassFilter {

    public static void main(String... args) {
        List<Double> data = new ArrayList<>();
        for (int i=0; i<1_000; i++) {
            data.add(Math.random() * 100);
        }
        // Display min & max
        double minValue = data.stream()
                .min(Comparator.comparing(Double::doubleValue))
                .get()
                .doubleValue();
        double maxValue = data.stream()
                .max(Comparator.comparing(Double::doubleValue))
                .get()
                .doubleValue();
        System.out.println(String.format("Data from %.03f to %.03f", minValue, maxValue));

        final List<Double> filteredValues = new ArrayList<>();

        final AtomicReference<Double> acc = new AtomicReference<>(0d);
        data.stream().forEach(value -> {
            acc.set(Filter.lowPass(Filter.ALPHA, value, acc.get()));
            filteredValues.add(acc.get());
        });
        System.out.println(String.format("Filtered list has %d entries.", filteredValues.size()));

        boolean again = true;
        if (again) {
            filteredValues.clear();
            acc.set(0d);
            data.stream().forEach(value -> {
                acc.set((value * Filter.ALPHA) + (acc.get() * (1 - Filter.ALPHA)));
                filteredValues.add(acc.get());
            });
            System.out.println(String.format("Filtered list has %d entries.", filteredValues.size()));
        }

        double minFData = filteredValues.stream()
                .map(g -> g > 0 ? g : 0)
                .min(Comparator.comparing(Double::doubleValue))
                .get()
                .doubleValue();
        double maxFData = filteredValues.stream()
                .max(Comparator.comparing(Double::doubleValue))
                .get()
                .doubleValue();
        System.out.println(String.format("Filtered Data from %.03f to %.03f", minFData, maxFData));


    }
}
