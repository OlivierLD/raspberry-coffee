package hanoitower;

import hanoitower.events.HanoiContext;

public class BackendAlgorithm {

	public static void move(int n, String from, String to, String using) {
		if (n == 0) {
			return;
		}
		move(n - 1, from, using, to);
		if ("true".equals(System.getProperty("backend.verbose", "false"))) {
			System.out.println("Moving from " + from + " to " + to);
		}
		HanoiContext.getInstance().fireMoveRequired(from, to); // Tell the UI (whatever it is)
		move(n - 1, using, to, from);
	}

	public static void main(String... args) throws Exception {
		int nbDisc = 4;
		if (args.length > 0) {
			try {
				nbDisc = Integer.parseInt(args[0]);
			} catch (NumberFormatException nfe) {
				nfe.printStackTrace();
			}
		}
		System.setProperty("backend.verbose", "true");
		move(nbDisc, "A", "C", "B"); // Moving A to C using B
	}

}
