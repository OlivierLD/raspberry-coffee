package hanoitower;

import hanoitower.events.HanoiContext;

public class BackendAlgorithm {

	public static void move(int n, String from, String to, String using) {
		if (n == 0) {
			return;
		}
		move(n - 1, from, using, to);
		if ("true".equals(System.getProperty("backend.verbose", "false"))) {
			System.out.println("Move disc from " + from + " to " + to);
		}
//		System.out.println(">>>> Before fireMoveRequired.");
//		long before = System.currentTimeMillis();
		HanoiContext.getInstance().fireMoveRequired(from, to); // Tell the UI (whatever it is)
//		System.out.println(String.format(">>>> After fireMoveRequired (%d ms).", (System.currentTimeMillis() - before)));
		move(n - 1, using, to, from);
	}

	// Extra
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
