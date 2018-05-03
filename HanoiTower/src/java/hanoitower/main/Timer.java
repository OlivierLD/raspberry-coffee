package hanoitower.main;

import hanoitower.BackendAlgorithm;

import java.text.NumberFormat;

public class Timer {

	public static void main(String... args) {
		int nbDisc = 1;
		BackendAlgorithm.move(nbDisc, "B", "A", "C");
		for (int i = 2; i <= 25; i++) {
			nbDisc = i;
			long before = System.currentTimeMillis();
			BackendAlgorithm.move(nbDisc, "B", "A", "C");
			long after = System.currentTimeMillis();
			System.out.println((new StringBuilder())
					.append("Processing for ")
					.append(nbDisc)
					.append(" discs (")
					.append(NumberFormat.getInstance().format((int)(Math.pow(2, nbDisc) - 1)))
					.append(" moves) completed in ")
					.append(Long.toString(after - before))
					.append(" ms.").toString());
		}
	}
}
