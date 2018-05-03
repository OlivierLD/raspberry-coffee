package mearm.hanoitower;

public class BackendAlgorithm {

	public static void move(int n, String from, String to, String using) {
		if (n == 0) {
			return;
		}
		move(n - 1, from, using, to);
		System.out.println("Moving from " + from + " to " + to);
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
		move(nbDisc, "A", "C", "B"); // Moving A to C using B
	}

}
