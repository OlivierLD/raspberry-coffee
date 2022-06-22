/*
 * @(#)Groove.java	1.16	99/11/03
 *
 * Copyright (c) 1998, 1999 by Sun Microsystems, Inc. All Rights Reserved.
 *
 * Sun grants you ("Licensee") a non-exclusive, royalty free, license to use,
 * modify and redistribute this software in source and binary code form,
 * provided that i) this copyright notice and license appear on all copies of
 * the software; and ii) Licensee does not utilize the software in a manner
 * which is disparaging to Sun.
 *
 * This software is provided "AS IS," without a warranty of any kind. ALL
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING ANY
 * IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 * NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN AND ITS LICENSORS SHALL NOT BE
 * LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING
 * OR DISTRIBUTING THE SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL SUN OR ITS
 * LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT,
 * INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER
 * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF
 * OR INABILITY TO USE SOFTWARE, EVEN IF SUN HAS BEEN ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGES.
 *
 * This software is not designed or intended for use in on-line control of
 * aircraft, air traffic, aircraft navigation or aircraft communications; or in
 * the design, construction, operation or maintenance of any nuclear
 * facility. Licensee represents and warrants that it will not use or
 * redistribute the Software for such purposes.
 */
package javasounddemo;


import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import javax.swing.event.*;
import javax.sound.midi.*;
import java.util.Vector;

/**
 * Rhythm Groove Box.  Program any beat you like, click on a cell.
 * Channel 10 (the rhythm channel) supports the 47 instrument sounds.
 * These sounds are the result of a program change to instrument 1.
 *
 * Beat Pattern 1
 *
 *     |  1 sec         | 2 sec
 *     1 e + a  2 e + a   3 e + a  4 e + a
 * hh  x   x    x   x     x   x    x   x
 * sn           x                  x
 * kk  x     x      x     x
 *     0 1 2 3  4 5 6 7   8 9 1011 12131415
 *
 * Hi-hat
 * on-off : 0-1, 2-3, 4-5, 6-7, 8-9, 10-11, 12-13, 14-15
 *
 * snare :
 * on-off : 4-5, 12-13
 *
 * bass :
 * on-off : 0-1, 3-4, 6-7, 8-9
 *
 * @version @(#)Groove.java	1.16 99/11/03
 * @author Brian Lichtenwalter
 */
public class Groove extends JPanel implements ActionListener, ControlContext, MetaEventListener {

	private final int PROGRAM = 192;
	private final int NOTEON = 144;
	private final int NOTEOFF = 128;
	private int velocity = 100;
	private TempoDial tempoDial = new TempoDial();
	private Sequencer sequencer;
	private Track track;
	private TableModel dataModel;
	private JTable table;
	private int row, col;
	private JButton loopB, startB;
	private JComboBox combo;
	private String instruments[] =
					{"Acoustic bass drum", "Bass drum 1", "Side stick", "Acoustic snare",
									"Hand clap", "Electric snare", "Low floor tom", "Closed hi-hat",
									"High floor tom", "Pedal hi-hat", "Low tom", "Open hi-hat",
									"Low-mid tom", "Hi-mid tom", "Crash cymbal 1", "High tom",
									"Ride cymbal 1", "Chinese cymbal", "Ride bell", "Tambourine",
									"Splash cymbal", "Cowbell", "Crash cymbal 2", "Vibraslap",
									"Ride cymbal 2", "Hi bongo", "Low bongo", "Mute hi conga",
									"Open hi conga", "Low conga", "High timbale", "Low timbale",
									"High agogo", "Low agogo", "Cabasa", "Maracas",
									"Short whistle", "Long whistle", "Short guiro", "Long guiro",
									"Claves", "Hi wood block", "Low wood block", "Mute cuica",
									"Open cuica", "Mute triangle", "Open triangle"};
	private Vector data = new Vector(instruments.length);

	@SuppressWarnings("unchecked")
	public Groove() {
		setLayout(new BorderLayout(5, 0));
		EmptyBorder eb = new EmptyBorder(5, 5, 5, 5);
		setBorder(eb);

		for (int i = 0, id = 35; i < instruments.length; i++, id++) {
			data.add(new Data(instruments[i], id));
		}

		final String[] names = {"Instrument",
						"1", "e", "+", "a",
						"2", "e", "+", "a",
						"3", "e", "+", "a",
						"4", "e", "+", "a"};

		dataModel = new AbstractTableModel() {
			public int getColumnCount() {
				return names.length;
			}

			public int getRowCount() {
				return data.size();
			}

			public Object getValueAt(int row, int col) {
				if (col == 0) {
					return ((Data) data.get(row)).name;
				} else {
					return ((Data) data.get(row)).staff[col - 1];
				}
			}

			public String getColumnName(int col) {
				return names[col];
			}

			public Class getColumnClass(int c) {
				return getValueAt(0, c).getClass();
			}

			public boolean isCellEditable(int row, int col) {
				return col == 0 ? false : true;
			}

			public void setValueAt(Object aValue, int row, int col) {
				if (col == 0) {
					((Data) data.get(row)).name = (String) aValue;
				} else {
					((Data) data.get(row)).staff[col - 1] = (Color) aValue;
				}
			}
		};

		DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
			public void setValue(Object value) {
				setBackground((Color) value);
			}
		};

		table = new JTable(dataModel);
		table.getColumn(names[0]).setMinWidth(120);
		TableColumnModel tcm = table.getColumnModel();
		for (int i = 1; i < names.length; i++) {
			TableColumn col = tcm.getColumn(i);
			col.setCellRenderer(renderer);
		}

		// Listener for row changes
		ListSelectionModel lsm = table.getSelectionModel();
		lsm.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				ListSelectionModel sm = (ListSelectionModel) e.getSource();
				if (!sm.isSelectionEmpty()) {
					row = sm.getMinSelectionIndex();
				}
			}
		});

		// Listener for column changes
		lsm = table.getColumnModel().getSelectionModel();
		lsm.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				ListSelectionModel sm = (ListSelectionModel) e.getSource();
				if (!sm.isSelectionEmpty()) {
					col = sm.getMinSelectionIndex();
				}
				if (col != 0) {
					Color c = ((Data) data.get(row)).staff[col - 1];
					if (c.equals(Color.white)) {
						((Data) data.get(row)).staff[col - 1] = Color.black;
					} else {
						((Data) data.get(row)).staff[col - 1] = Color.white;
					}
					table.tableChanged(new TableModelEvent(dataModel));
				}
			}
		});

		JPanel p1 = new JPanel();
		p1.setLayout(new BoxLayout(p1, BoxLayout.Y_AXIS));
		SoftBevelBorder sbb = new SoftBevelBorder(BevelBorder.RAISED);
		p1.setBorder(new CompoundBorder(sbb, eb));
		p1.add(tempoDial);
		p1.add(Box.createVerticalStrut(10));

		JPanel p2 = new JPanel(new GridLayout(0, 1, 2, 10));
		p2.add(startB = makeButton("Start", getBackground()));
		p2.add(loopB = makeButton("Loop", getBackground()));
		p2.add(makeButton("Clear Table", getBackground()));

		combo = new JComboBox();
		combo.addActionListener(this);
		combo.addItem("Rock Beat 1");
		combo.addItem("Rock Beat 2");
		combo.addItem("Rock Beat 3");
		p2.add(combo);

		p1.add(p2);
		p1.add(Box.createVerticalStrut(120));
		add("West", p1);

		add("Center", new JScrollPane(table));
	}

	public void open() {
		try {
			sequencer = MidiSystem.getSequencer();
			sequencer.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
		tempoDial.setSequencer(sequencer);
		sequencer.addMetaEventListener(this);
	}

	public void close() {
		if (startB.getText().startsWith("Stop")) {
			startB.doClick(0);
		}
		if (sequencer != null) {
			sequencer.close();
		}
		sequencer = null;
	}


	private JButton makeButton(String bName, Color c) {
		JButton b = new JButton(bName);
		b.setBackground(c);
		b.addActionListener(this);
		return b;
	}

	private void buildTrackThenStartSequencer() {
		Sequence sequence = null;
		try {
			sequence = new Sequence(Sequence.PPQ, 4);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		track = sequence.createTrack();
		createEvent(PROGRAM, 9, 1, 0);
		for (int i = 0; i < data.size(); i++) {
			Data d = (Data) data.get(i);
			for (int j = 0; j < d.staff.length; j++) {
				if (d.staff[j].equals(Color.black)) {
					createEvent(NOTEON, 9, d.id, j);
					createEvent(NOTEOFF, 9, d.id, j + 1);
				}
			}
		}
		// so we always have a track from 0 to 15.
		createEvent(PROGRAM, 9, 1, 15);

		// set and start the sequencer.
		try {
			sequencer.setSequence(sequence);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		sequencer.start();
		sequencer.setTempoInBPM(tempoDial.getTempo());
	}


	private void presetTracks(int num) {

		final int ACOUSTIC_BASS = 35;
		final int ACOUSTIC_SNARE = 38;
		final int HAND_CLAP = 39;
		final int PEDAL_HIHAT = 44;
		final int LO_TOM = 45;
		final int CLOSED_HIHAT = 42;
		final int CRASH_CYMBAL1 = 49;
		final int HI_TOM = 50;
		final int RIDE_BELL = 53;

		clearTable();

		switch (num) {
			case 0:
				for (int i = 0; i < 16; i += 2) {
					setCell(CLOSED_HIHAT, i);
				}
				setCell(ACOUSTIC_SNARE, 4);
				setCell(ACOUSTIC_SNARE, 12);
				int bass1[] = {0, 3, 6, 8};
				for (int i = 0; i < bass1.length; i++) {
					setCell(ACOUSTIC_BASS, bass1[i]);
				}
				break;
			case 1:
				for (int i = 0; i < 16; i += 4) {
					setCell(CRASH_CYMBAL1, i);
				}
				for (int i = 0; i < 16; i += 2) {
					setCell(PEDAL_HIHAT, i);
				}
				setCell(ACOUSTIC_SNARE, 4);
				setCell(ACOUSTIC_SNARE, 12);
				int bass2[] = {0, 2, 3, 7, 9, 10, 15};
				for (int i = 0; i < bass2.length; i++) {
					setCell(ACOUSTIC_BASS, bass2[i]);
				}
				break;
			case 2:
				for (int i = 0; i < 16; i += 4) {
					setCell(RIDE_BELL, i);
				}
				for (int i = 2; i < 16; i += 4) {
					setCell(PEDAL_HIHAT, i);
				}
				setCell(HAND_CLAP, 4);
				setCell(HAND_CLAP, 12);
				setCell(HI_TOM, 13);
				setCell(LO_TOM, 14);
				int bass3[] = {0, 3, 6, 9, 15};
				for (int i = 0; i < bass3.length; i++) {
					setCell(ACOUSTIC_BASS + 1, bass3[i]);
				}
				break;
			default:
		}
		table.tableChanged(new TableModelEvent(dataModel));
	}


	private void setCell(int id, int tick) {
		for (int i = 0; i < data.size(); i++) {
			Data d = (Data) data.get(i);
			if (d.id == id) {
				d.staff[tick] = Color.black;
				break;
			}
		}
	}

	private void clearTable() {
		for (int i = 0; i < data.size(); i++) {
			Data d = (Data) data.get(i);
			for (int j = 0; j < d.staff.length; j++) {
				d.staff[j] = Color.white;
			}
		}
	}

	private void createEvent(int type, int chan, int num, long tick) {
		ShortMessage message = new ShortMessage();
		try {
			message.setMessage(type, chan, num, velocity);
			MidiEvent event = new MidiEvent(message, tick);
			track.add(event);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void meta(MetaMessage message) {
		if (message.getType() == 47) {  // 47 is end of track
			if (loopB.getBackground().equals(Color.gray)) {
				if (sequencer != null && sequencer.isOpen()) {
					sequencer.start();
					sequencer.setTempoInBPM(tempoDial.getTempo());
				}
			} else {
				startB.setText("Start");
			}
		}
	}

	public void actionPerformed(ActionEvent e) {
		Object object = e.getSource();
		if (object instanceof JComboBox) {
			presetTracks(((JComboBox) object).getSelectedIndex());
			if (startB.getText().startsWith("Stop")) {
				sequencer.stop();
				buildTrackThenStartSequencer();
			}
		} else if (object instanceof JButton) {
			JButton b = (JButton) object;
			if (b.equals(startB)) {
				if (b.getText().startsWith("Start")) {
					buildTrackThenStartSequencer();
					b.setText("Stop");
				} else {
					sequencer.stop();
					b.setText("Start");
				}
			} else if (b.equals(loopB)) {
				b.setSelected(!b.isSelected());
				if (loopB.getBackground().equals(Color.gray)) {
					loopB.setBackground(getBackground());
				} else {
					loopB.setBackground(Color.gray);
				}
			} else if (b.getText().startsWith("Clear")) {
				clearTable();
				table.tableChanged(new TableModelEvent(dataModel));
			}
		}
	}

	/**
	 * Storage class for instrument and musical staff represented by color.
	 */
	class Data extends Object {
		String name;
		int id;
		Color staff[] = new Color[16];

		public Data(String name, int id) {
			this.name = name;
			this.id = id;
			for (int i = 0; i < staff.length; i++) {
				staff[i] = Color.white;
			}
		}
	}

    @SuppressWarnings("deprecation")
	public static void main(String... args) {
		final Groove groove = new Groove();
		JFrame f = new JFrame("Rhythm Groove Box");
		f.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
		f.getContentPane().add("Center", groove);
		f.pack();
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int w = 640;
		int h = 440;
		f.setLocation(screenSize.width / 2 - w / 2, screenSize.height / 2 - h / 2);
		f.setSize(w, h);
		f.show();
		groove.open();
	}
}
