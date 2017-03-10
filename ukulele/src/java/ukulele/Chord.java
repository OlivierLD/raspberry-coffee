package ukulele;

public class Chord {
	private String title = "";
	private int[] finger = null;


	public Chord(String title, int[] finger) {
		this.title = title;
		this.finger = finger;
	}

	public String getTitle() {
		return this.title;
	}

	public int[] getFinger() {
		return this.finger;
	}

	public String toString() {
		String s = getTitle() + " (";
		for (int i = 0; i < getFinger().length; i++) {
			s = s + (getFinger()[i] == -1 ? "X" : Integer.toString(getFinger()[i])) + " ";
		}
		return s.trim() + ")";
	}

	public Chord clone(String newTitle) {
		return new Chord(newTitle, this.finger);
	}
}
