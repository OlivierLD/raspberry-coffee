package oliv.misc;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class LowPassFilter {

	private static final String DATA_FILE = "../../Project.Trunk/REST.clients/TCP.Watch.01/max.gust.csv";
	private final static int[] COLUMN_LENGTHS = new int[] { 19, 4, 5, 5, 4, 7, 5, 6, 5 };

	public final static double ALPHA = 0.015D;
	public static double lowPass(double alpha, double value, double acc) {
		return (value * alpha) + (acc * (1 - alpha));
	}

	private static void displayLine(String line) {
		String[] array = line.split(",");
		StringBuffer fmtLine = new StringBuffer();
		fmtLine.append("|");
		final AtomicInteger idx = new AtomicInteger(0);
		Arrays.asList(array).stream().forEach(cell -> {
			String fmtCell = String.format(" %" + String.valueOf(COLUMN_LENGTHS[idx.get()]) + "s |", cell);
			fmtLine.append(fmtCell);
			idx.set(idx.get() + 1);
		});
		System.out.println(fmtLine.toString());
	}

	public static void main(String... args) throws Exception {
		System.out.println(String.format("Running from %s", System.getProperty("user.dir")));

		List<Double> gusts = new ArrayList<>();
		BufferedReader br = new BufferedReader(new FileReader(DATA_FILE));
		// Display top of the file
		String line = "";
		long lineNo = 0;
		while (line != null) {
			line = br.readLine();
			if (line != null) {
				if (lineNo < 10) {
					displayLine(line);
				}
				lineNo += 1;
				if (lineNo > 1) {
					String[] items = line.split(",");
					if (items.length > 2) {
						String gust = items[2];
						gusts.add(Double.parseDouble(gust));
					}
				}
			}
		}
		br.close();

		System.out.println(String.format("We read %d line(s), %d entry(ies) in the buffer.", lineNo, gusts.size()));
		double minGust = gusts.stream()
				.min(Comparator.comparing(Double::doubleValue))
				.get()
				.doubleValue();
		double maxGust = gusts.stream()
				.max(Comparator.comparing(Double::doubleValue))
				.get()
				.doubleValue();
		System.out.println(String.format("Gusts from %.03f kts to %.03f kts", minGust, maxGust));

		final List<Double> filteredGusts = new ArrayList<>();

		final AtomicReference<Double> acc = new AtomicReference<>(0d);
		gusts.stream().forEach(gust -> {
			acc.set(lowPass(ALPHA, gust, acc.get()));
			filteredGusts.add(acc.get());
		});
		System.out.println(String.format("Filtered list has %d entries.", filteredGusts.size()));

		boolean again = true;
		if (again) {
			filteredGusts.clear();
			acc.set(0d);
			gusts.stream().forEach(gust -> {
				acc.set((gust * ALPHA) + (acc.get() * (1 - ALPHA)));
				filteredGusts.add(acc.get());
			});
			System.out.println(String.format("Filtered list has %d entries.", filteredGusts.size()));
		}

		double minFGust = filteredGusts.stream()
				.map(g -> g > 0 ? g : 0)
				.min(Comparator.comparing(Double::doubleValue))
				.get()
				.doubleValue();
		double maxFGust = filteredGusts.stream()
				.max(Comparator.comparing(Double::doubleValue))
				.get()
				.doubleValue();
		System.out.println(String.format("Filtered Gusts from %.03f kts to %.03f kts", minFGust, maxFGust));

	}
}
