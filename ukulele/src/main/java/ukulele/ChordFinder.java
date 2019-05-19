package ukulele;

import chords.ChordList;
import java.io.PrintStream;


public class ChordFinder {
	public static void main(String... args)
					throws Exception {
		if (args.length != 4)
			throw new IllegalArgumentException("Need 4 int.");
		int[] chord = new int[4];
		for (int i = 0; i < args.length; i++)
			chord[i] = Integer.parseInt(args[i]);
		boolean found = false;
		for (Chord c : ChordList.getChords()) {
			if ((c.getFinger()[0] == chord[0]) && (c.getFinger()[1] == chord[1]) && (c.getFinger()[2] == chord[2]) && (c.getFinger()[3] == chord[3])) {

				System.out.println(c.toString());
				found = true;
				break;
			}
		}
		if (!found) {
			System.out.println("... not found");
		}
	}
}
