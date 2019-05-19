package chords;

//import org.jfugue.player.Player;
import org.jfugue.Player;
import ukulele.Chord;

public class ChordUtil {
	private static final String[] CHROMATIC = {"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"};


	private static final String[] ORIGINAL_TUNING = {"G", "C", "E", "A"};

	private static final int ORIGINAL_OCTAVE = 5;

	private static final String STRING_PREFIX = "T240 I[Guitar] ";

	protected static final String WHOLE = "w";
	protected static final String HALF = "h";
	protected static final String QUARTER = "q";
	protected static final String EIGHTH = "i";
	protected static final String SIXTEENTH = "s";
	protected static final String THIRTY_SECOND = "t";
	protected static final String SIXTY_FOURTH = "x";
	protected static final String ONE_TWENTY_EIGHTH = "o";

	private static final int ARPEGE = 0;
	private static final int CHORD = 1;
	private static final int PLAY_OPTION = ARPEGE;
	private static final String DEFAULT_DURATION = EIGHTH;
	private static final String FINAL_DURATION = WHOLE;

	public static final int LOWEST = 0;
	public static final int HIGHEST = 1;

	public static final int[] findLowestFinger(Chord chord) {
		int lowest = Integer.MAX_VALUE;
		int highest = Integer.MIN_VALUE;

		for (int i : chord.getFinger()) {
			if (i > 0) {
				lowest = Math.min(i, lowest);
				highest = Math.max(i, highest);
			}
		}
		return new int[]{lowest == Integer.MAX_VALUE ? LOWEST : lowest, highest};
	}

	public static final String generateMusicString(Chord chord) {
		String pattern = STRING_PREFIX;
		Chord toPlay = chord;
		if (toPlay == null) {
			toPlay = new Chord("", new int[]{0, 0, 0, 0});
		}
		for (int f = 0; f < toPlay.getFinger().length; f++) {
			if (toPlay.getFinger()[f] != -1) {
				String note = getNote(ORIGINAL_TUNING[f], toPlay.getFinger()[f]);
				pattern = pattern + note;
				if (f < toPlay.getFinger().length - 1) {
					pattern = pattern + DEFAULT_DURATION + " ";
				} else
					pattern = pattern + FINAL_DURATION;
			}
		}
		pattern = pattern.trim();
		if (!pattern.endsWith(WHOLE)) {
			pattern = pattern.substring(0, pattern.length() - 1) + WHOLE;
		}
		return pattern.trim();
	}

	private static final String getNote(String base, int nbFret) {
		int octave = ORIGINAL_OCTAVE;
		int baseOffset = findOffset(base);
		int newIndex = baseOffset;

		for (int i = 0; i < nbFret; i++) {
			newIndex++;
			if (newIndex >= CHROMATIC.length) {
				newIndex = 0;
				octave++;
			}
		}
		String note = CHROMATIC[newIndex];
		return note + Integer.toString(octave);
	}

	private static final int findOffset(String s) {
		int offset = 0;
		for (int i = 0; i < CHROMATIC.length; i++) {
			if (CHROMATIC[i].equals(s)) {
				offset = i;
				break;
			}
		}
		return offset;
	}

	public static final void playChord(Chord chord) {
		Player player = new Player();
		String str = generateMusicString(chord);
		System.out.println(chord.toString() + " -> [" + str + "]");
		player.play(str);
		// 4.0.3 and earlier
		player.close();
	}
}
