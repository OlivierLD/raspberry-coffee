package section.one;

import chords.ChordList;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.PrintStream;
import java.util.List;
import javax.swing.JPanel;
import ukulele.Chord;
import ukulele.ChordPanel;


public class ThreeChordPanel
				extends JPanel {
	private GridBagLayout gridBagLayout1 = new GridBagLayout();
	private String[] chords = {"A", "B", "C"};
	private ChordPanel chordPanelI = new ChordPanel();
	private ChordPanel chordPanelIV = new ChordPanel();
	private ChordPanel chordPanelV = new ChordPanel();

	public ThreeChordPanel() {
		this("A B C");
	}

	public ThreeChordPanel(String chords) {
		this(chords.split(" "));
	}


	public ThreeChordPanel(String[] sa) {
		this(sa[0], sa[1], sa[2]);
	}

	public ThreeChordPanel(String I, String IV, String V) {
		setData(I, IV, V);

		try {
			jbInit();
		} catch (Exception e) {
			e.printStackTrace();
		}
		setChordData();
	}

	public void setData(String s) {
		setData(s.split("\\s"));
	}

	public void setData(String[] sa) {
		setData(sa[0], sa[1], sa[2]);
	}

	public void setData(String I, String IV, String V) {
		this.chords = new String[3];
		this.chords[0] = I;
		this.chords[1] = IV;
		this.chords[2] = V;

		setChordData();
		repaint();
	}

	private void setChordData() {
		this.chordPanelI.setChord((Chord) ChordList.findChord(this.chords[0]).get(0));
		this.chordPanelIV.setChord((Chord) ChordList.findChord(this.chords[1]).get(0));
		this.chordPanelV.setChord((Chord) ChordList.findChord(this.chords[2]).get(0));
	}

	private void jbInit()
					throws Exception {
		setSize(new Dimension(295, 140));
		setPreferredSize(new Dimension(295, 140));
		setLayout(this.gridBagLayout1);
		add(this.chordPanelI, new GridBagConstraints(0, 0, 1, 1, 0.0D, 0.0D, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		add(this.chordPanelIV, new GridBagConstraints(1, 0, 1, 1, 0.0D, 0.0D, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		add(this.chordPanelV, new GridBagConstraints(2, 0, 1, 1, 0.0D, 0.0D, GridBagConstraints.EAST, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
	}


	public static void main(String... args) {
		String s = "Db Gb Ab7";
		String[] sa = s.split(" ");
		for (String str : sa)
			System.out.println("[" + str + "]");
		System.out.println();
		sa = s.split("\\s");
		for (String str : sa) {
			System.out.println("[" + str + "]");
		}
	}
}
