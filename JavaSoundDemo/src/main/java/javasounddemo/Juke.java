/*
 * @(#)Juke.java	1.19	00/01/31
 *
 * Copyright (c) 1999 Sun Microsystems, Inc. All Rights Reserved.
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
import java.awt.font.*;
import java.awt.geom.*;
import java.awt.event.*;
import java.text.AttributedString;
import java.text.AttributedCharacterIterator;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import javax.swing.event.*;
import javax.sound.midi.*;
import javax.sound.sampled.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.util.Vector;
import java.net.URL;


/**
 * A JukeBox for sampled and midi sound files.  Features duration progress,
 * seek slider, pan and volume controls.
 *
 * @author Brian Lichtenwalter
 * @version @(#)Juke.java	1.19 00/01/31
 */
public class Juke extends JPanel implements Runnable, LineListener, MetaEventListener, ControlContext {

	private final int bufSize = 16384;
	private PlaybackMonitor playbackMonitor = new PlaybackMonitor();

	private Vector sounds = new Vector();
	private Thread thread;
	private Sequencer sequencer;
	private boolean midiEOM, audioEOM;
	private Synthesizer synthesizer;
	private MidiChannel channels[];
	private Object currentSound;
	private String currentName;
	private double duration;
	private int num;
	private boolean bump;
	private boolean paused = false;
	private JButton startB, pauseB, loopB, prevB, nextB;
	private JTable table;
	private JSlider panSlider, gainSlider;
	private JSlider seekSlider;
	private JukeTable jukeTable;
	private Loading loading;
	private Credits credits;
	private String errStr;
	private JukeControls controls;

	public Juke(String dirName) {
		setLayout(new BorderLayout());
		setBorder(new EmptyBorder(5, 5, 5, 5));

		if (dirName != null) {
			loadJuke(dirName);
		}

		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
						jukeTable = new JukeTable(), controls = new JukeControls());
		splitPane.setContinuousLayout(true);
		add(splitPane);
	}

	public void open() {
		try {
			sequencer = MidiSystem.getSequencer();

			if (sequencer instanceof Synthesizer) {
				synthesizer = (Synthesizer) sequencer;
				channels = synthesizer.getChannels();
			}

		} catch (Exception ex) {
			ex.printStackTrace();
			return;
		}
		sequencer.addMetaEventListener(this);
		(credits = new Credits()).start();
	}

	public void close() {
		if (credits != null && credits.isAlive()) {
			credits.interrupt();
		}
		if (thread != null && startB != null) {
			startB.doClick(0);
		}
		if (jukeTable != null && jukeTable.frame != null) {
			jukeTable.frame.dispose();
			jukeTable.frame = null;
		}
		if (sequencer != null) {
			sequencer.close();
		}
	}

	public void loadJuke(String name) {
		try {
			File file = new File(name);
			if (file != null && file.isDirectory()) {
				String files[] = file.list();
				for (int i = 0; i < files.length; i++) {
					File leafFile = new File(file.getAbsolutePath(), files[i]);
					if (leafFile.isDirectory()) {
						loadJuke(leafFile.getAbsolutePath());
					} else {
						addSound(leafFile);
					}
				}
			} else if (file != null && file.exists()) {
				addSound(file);
			}
		} catch (SecurityException ex) {
			reportStatus(ex.toString());
			JavaSound.showInfoDialog();
		} catch (Exception ex) {
			reportStatus(ex.toString());
		}
	}

	private void addSound(File file) {
		String s = file.getName();
		if (s.endsWith(".au") || s.endsWith(".rmf") ||
						s.endsWith(".mid") || s.endsWith(".wav") ||
						s.endsWith(".aif") || s.endsWith(".aiff")) {
			sounds.add(file);
		}
	}

	public boolean loadSound(Object object) {
		duration = 0.0;
		(loading = new Loading()).start();

		if (object instanceof URL) {
			currentName = ((URL) object).getFile();
			playbackMonitor.repaint();
			try {
				currentSound = AudioSystem.getAudioInputStream((URL) object);
			} catch (Exception e) {
				try {
					currentSound = MidiSystem.getSequence((URL) object);
				} catch (InvalidMidiDataException imde) {
					System.out.println("Unsupported audio file.");
					return false;
				} catch (Exception ex) {
					ex.printStackTrace();
					currentSound = null;
					return false;
				}
			}
		} else if (object instanceof File) {
			currentName = ((File) object).getName();
			playbackMonitor.repaint();
			try {
				currentSound = AudioSystem.getAudioInputStream((File) object);
			} catch (Exception e1) {
				// load midi & rmf as inputstreams for now
				//try {
				//currentSound = MidiSystem.getSequence((File) object);
				//} catch (Exception e2) {
				try {
					FileInputStream is = new FileInputStream((File) object);
					currentSound = new BufferedInputStream(is, 1024);
				} catch (Exception e3) {
					e3.printStackTrace();
					currentSound = null;
					return false;
				}
				//}
			}
		}

		loading.interrupt();

		// user pressed stop or changed tabs while loading
		if (sequencer == null) {
			currentSound = null;
			return false;
		}

		if (currentSound instanceof AudioInputStream) {
			try {
				AudioInputStream stream = (AudioInputStream) currentSound;
				AudioFormat format = stream.getFormat();

				/**
				 * we can't yet open the device for ALAW/ULAW playback,
				 * convert ALAW/ULAW to PCM
				 */
				if ((format.getEncoding() == AudioFormat.Encoding.ULAW) ||
								(format.getEncoding() == AudioFormat.Encoding.ALAW)) {
					AudioFormat tmp = new AudioFormat(
									AudioFormat.Encoding.PCM_SIGNED,
									format.getSampleRate(),
									format.getSampleSizeInBits() * 2,
									format.getChannels(),
									format.getFrameSize() * 2,
									format.getFrameRate(),
									true);
					stream = AudioSystem.getAudioInputStream(tmp, stream);
					format = tmp;
				}
				DataLine.Info info = new DataLine.Info(
								Clip.class,
								stream.getFormat(),
								((int) stream.getFrameLength() *
												format.getFrameSize()));

				Clip clip = (Clip) AudioSystem.getLine(info);
				clip.addLineListener(this);
				clip.open(stream);
				currentSound = clip;
				seekSlider.setMaximum((int) stream.getFrameLength());
			} catch (Exception ex) {
				ex.printStackTrace();
				currentSound = null;
				return false;
			}
		} else if (currentSound instanceof Sequence || currentSound instanceof BufferedInputStream) {
			try {
				sequencer.open();
				if (currentSound instanceof Sequence) {
					sequencer.setSequence((Sequence) currentSound);
				} else {
					sequencer.setSequence((BufferedInputStream) currentSound);
				}
				seekSlider.setMaximum((int) (sequencer.getMicrosecondLength() / 1000));

			} catch (InvalidMidiDataException imde) {
				System.out.println("Unsupported audio file.");
				currentSound = null;
				return false;
			} catch (Exception ex) {
				ex.printStackTrace();
				currentSound = null;
				return false;
			}
		}

		seekSlider.setValue(0);

		// enable seek, pan, and gain sliders for sequences as well as clips
		seekSlider.setEnabled(true);
		panSlider.setEnabled(true);
		gainSlider.setEnabled(true);

		duration = getDuration();

		return true;
	}

	public void playSound() {
		playbackMonitor.start();
		setGain();
		setPan();
		midiEOM = audioEOM = bump = false;
		if (currentSound instanceof Sequence || currentSound instanceof BufferedInputStream && thread != null) {
			sequencer.start();
			while (!midiEOM && thread != null && !bump) {
				try {
					thread.sleep(99);
				} catch (Exception e) {
					break;
				}
			}
			sequencer.stop();
			sequencer.close();
		} else if (currentSound instanceof Clip && thread != null) {
			Clip clip = (Clip) currentSound;
			clip.start();
			try {
				thread.sleep(99);
			} catch (Exception e) {
			}
			while ((paused || clip.isActive()) && thread != null && !bump) {
				try {
					thread.sleep(99);
				} catch (Exception e) {
					break;
				}
			}
			clip.stop();
			clip.close();
		}
		currentSound = null;
		playbackMonitor.stop();
	}


	public double getDuration() {
		double duration = 0.0;
		if (currentSound instanceof Sequence) {
			duration = ((Sequence) currentSound).getMicrosecondLength() / 1_000_000.0;
		} else if (currentSound instanceof BufferedInputStream) {
			duration = sequencer.getMicrosecondLength() / 1_000_000.0;
		} else if (currentSound instanceof Clip) {
			Clip clip = (Clip) currentSound;
			duration = clip.getBufferSize() /
							(clip.getFormat().getFrameSize() * clip.getFormat().getFrameRate());
		}
		return duration;
	}


	public double getSeconds() {
		double seconds = 0.0;
		if (currentSound instanceof Clip) {
			Clip clip = (Clip) currentSound;
			seconds = clip.getFramePosition() / clip.getFormat().getFrameRate();
		} else if ((currentSound instanceof Sequence) || (currentSound instanceof BufferedInputStream)) {
			try {
				seconds = sequencer.getMicrosecondPosition() / 1_000_000.0;
			} catch (IllegalStateException e) {
				System.out.println("TEMP: IllegalStateException " +
								"on sequencer.getMicrosecondPosition(): " + e);
			}
		}
		return seconds;
	}


	public void update(LineEvent event) {
		if (event.getType() == LineEvent.Type.STOP && !paused) {
			audioEOM = true;
		}
	}


	public void meta(MetaMessage message) {
		if (message.getType() == 47) {  // 47 is end of track
			midiEOM = true;
		}
	}


	private void reportStatus(String msg) {
		if ((errStr = msg) != null) {
			System.out.println(errStr);
			playbackMonitor.repaint();
		}
		if (credits != null && credits.isAlive()) {
			credits.interrupt();
		}
	}


	public Thread getThread() {
		return thread;
	}


	public void start() {
		thread = new Thread(this);
		thread.setName("Juke");
		thread.start();
	}


	public void stop() {
		if (thread != null) {
			thread.interrupt();
		}
		thread = null;
	}


	public void run() {
		do {
			table.scrollRectToVisible(new Rectangle(0, 0, 1, 1));
			for (; num < sounds.size() && thread != null; num++) {
				table.scrollRectToVisible(new Rectangle(0, (num + 2) * (table.getRowHeight() + table.getRowMargin()), 1, 1));
				table.setRowSelectionInterval(num, num);
				if (loadSound(sounds.get(num)) == true) {
					playSound();
				}
				// take a little break between sounds
				try {
					thread.sleep(222);
				} catch (Exception e) {
					break;
				}
			}
			num = 0;
		} while (loopB.isSelected() && thread != null);

		if (thread != null) {
			startB.doClick();
		}
		thread = null;
		currentName = null;
		currentSound = null;
		playbackMonitor.repaint();
	}


	public void setPan() {

		int value = panSlider.getValue();

		if (currentSound instanceof Clip) {
			try {
				Clip clip = (Clip) currentSound;
				FloatControl panControl =
								(FloatControl) clip.getControl(FloatControl.Type.PAN);
				panControl.setValue(value / 100.0f);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		} else if (currentSound instanceof Sequence || currentSound instanceof BufferedInputStream) {
			for (int i = 0; i < channels.length; i++) {
				channels[i].controlChange(10, (int) (((double) value + 100.0) / 200.0 * 127.0));
			}
		}
	}


	public void setGain() {
		double value = gainSlider.getValue() / 100.0;

		if (currentSound instanceof Clip) {
			try {
				Clip clip = (Clip) currentSound;
				FloatControl gainControl =
								(FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
				float dB = (float)
								(Math.log(value == 0.0 ? 0.0001 : value) / Math.log(10.0) * 20.0);
				gainControl.setValue(dB);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		} else if (currentSound instanceof Sequence || currentSound instanceof BufferedInputStream) {
			for (int i = 0; i < channels.length; i++) {
				channels[i].controlChange(7, (int) (value * 127.0));
			}
		}
	}


	/**
	 * GUI controls for start, stop, previous, next, pan and gain.
	 */
	class JukeControls extends JPanel implements ActionListener, ChangeListener {

		public JukeControls() {
			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

			JPanel p1 = new JPanel();
			p1.setLayout(new BoxLayout(p1, BoxLayout.Y_AXIS));
			p1.setBorder(new EmptyBorder(10, 0, 5, 0));
			JPanel p2 = new JPanel();
			startB = addButton("Start", p2, sounds.size() != 0);
			pauseB = addButton("Pause", p2, false);
			p1.add(p2);
			JPanel p3 = new JPanel();
			prevB = addButton("<<", p3, false);
			nextB = addButton(">>", p3, false);
			p1.add(p3);
			add(p1);

			JPanel p4 = new JPanel(new BorderLayout());
			EmptyBorder eb = new EmptyBorder(5, 20, 10, 20);
			BevelBorder bb = new BevelBorder(BevelBorder.LOWERED);
			p4.setBorder(new CompoundBorder(eb, bb));
			p4.add(playbackMonitor);
			seekSlider = new JSlider(JSlider.HORIZONTAL, 0, 100, 0);
			seekSlider.setEnabled(false);
			seekSlider.addChangeListener(this);
			p4.add("South", seekSlider);
			add(p4);

			JPanel p5 = new JPanel();
			p5.setLayout(new BoxLayout(p5, BoxLayout.X_AXIS));
			p5.setBorder(new EmptyBorder(5, 5, 10, 5));
			panSlider = new JSlider(-100, 100, 0);
			panSlider.addChangeListener(this);
			TitledBorder tb = new TitledBorder(new EtchedBorder());
			tb.setTitle("Pan = 0.0");
			panSlider.setBorder(tb);
			p5.add(panSlider);
			gainSlider = new JSlider(0, 100, 80);
			gainSlider.addChangeListener(this);
			tb = new TitledBorder(new EtchedBorder());
			tb.setTitle("Gain = 80");
			gainSlider.setBorder(tb);
			p5.add(gainSlider);
			add(p5);
		}

		private JButton addButton(String name, JPanel panel, boolean state) {
			JButton b = new JButton(name);
			b.addActionListener(this);
			b.setEnabled(state);
			panel.add(b);
			return b;
		}

		public void stateChanged(ChangeEvent e) {
			JSlider slider = (JSlider) e.getSource();
			int value = slider.getValue();
			if (slider.equals(seekSlider)) {
				if (currentSound instanceof Clip) {
					((Clip) currentSound).setFramePosition(value);
				} else if (currentSound instanceof Sequence) {
					long dur = ((Sequence) currentSound).getMicrosecondLength();
					sequencer.setMicrosecondPosition(value * 1_000);
				} else if (currentSound instanceof BufferedInputStream) {
					long dur = sequencer.getMicrosecondLength();
					sequencer.setMicrosecondPosition(value * 1_000);
				}
				playbackMonitor.repaint();
				return;
			}
			TitledBorder tb = (TitledBorder) slider.getBorder();
			String s = tb.getTitle();
			if (s.startsWith("Pan")) {
				s = s.substring(0, s.indexOf('=') + 1) + s.valueOf(value / 100.0);
				if (currentSound != null) {
					setPan();
				}
			} else if (s.startsWith("Gain")) {
				s = s.substring(0, s.indexOf('=') + 1) + s.valueOf(value);
				if (currentSound != null) {
					setGain();
				}
			}
			tb.setTitle(s);
			slider.repaint();
		}


		public void setComponentsEnabled(boolean state) {
			seekSlider.setEnabled(state);
			pauseB.setEnabled(state);
			prevB.setEnabled(state);
			nextB.setEnabled(state);
		}


		public void actionPerformed(ActionEvent e) {
			JButton button = (JButton) e.getSource();
			if (button.getText().equals("Start")) {
				if (credits != null) {
					credits.interrupt();
				}
				paused = false;
				num = table.getSelectedRow();
				num = num == -1 ? 0 : num;
				start();
				button.setText("Stop");
				setComponentsEnabled(true);
			} else if (button.getText().equals("Stop")) {
				credits = new Credits();
				credits.start();
				paused = false;
				stop();
				button.setText("Start");
				pauseB.setText("Pause");
				setComponentsEnabled(false);
			} else if (button.getText().equals("Pause")) {
				paused = true;
				if (currentSound instanceof Clip) {
					((Clip) currentSound).stop();
				} else if (currentSound instanceof Sequence || currentSound instanceof BufferedInputStream) {
					sequencer.stop();
				}
				playbackMonitor.stop();
				pauseB.setText("Resume");
			} else if (button.getText().equals("Resume")) {
				paused = false;
				if (currentSound instanceof Clip) {
					((Clip) currentSound).start();
				} else if (currentSound instanceof Sequence || currentSound instanceof BufferedInputStream) {
					sequencer.start();
				}
				playbackMonitor.start();
				pauseB.setText("Pause");
			} else if (button.getText().equals("<<")) {
				paused = false;
				pauseB.setText("Pause");
				num = num - 1 < 0 ? sounds.size() - 1 : num - 2;
				bump = true;
			} else if (button.getText().equals(">>")) {
				paused = false;
				pauseB.setText("Pause");
				num = num + 1 == sounds.size() ? -1 : num;
				bump = true;
			}
		}
	}  // End JukeControls


	/**
	 * Displays current sound and time elapsed.
	 */
	public class PlaybackMonitor extends JPanel implements Runnable {

		String welcomeStr = "Welcome to Java Sound";
		Thread pbThread;
		Color black = new Color(20, 20, 20);
		Color jfcBlue = new Color(204, 204, 255);
		Color jfcDarkBlue = jfcBlue.darker();
		Font font24 = new Font("serif", Font.BOLD, 24);
		Font font28 = new Font("serif", Font.BOLD, 28);
		Font font42 = new Font("serif", Font.BOLD, 42);
		FontMetrics fm28, fm42;

		public PlaybackMonitor() {
			fm28 = getFontMetrics(font28);
			fm42 = getFontMetrics(font42);
		}

		public void paint(Graphics g) {
			Graphics2D g2 = (Graphics2D) g;
			Dimension d = getSize();
			g2.setBackground(black);
			g2.clearRect(0, 0, d.width, d.height);
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setColor(jfcBlue);

			if (errStr != null) {
				g2.setFont(new Font("serif", Font.BOLD, 18));
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
				g2.drawString("ERROR", 5, 20);
				AttributedString as = new AttributedString(errStr);
				Font font12 = new Font("serif", Font.PLAIN, 12);
				as.addAttribute(TextAttribute.FONT, font12, 0, errStr.length());
				AttributedCharacterIterator aci = as.getIterator();
				FontRenderContext frc = g2.getFontRenderContext();
				LineBreakMeasurer lbm = new LineBreakMeasurer(aci, frc);
				float x = 5, y = 25;
				lbm.setPosition(0);
				while (lbm.getPosition() < errStr.length()) {
					TextLayout tl = lbm.nextLayout(d.width - x - 5);
					if (!tl.isLeftToRight()) {
						x = d.width - tl.getAdvance();
					}
					tl.draw(g2, x, y += tl.getAscent());
					y += tl.getDescent() + tl.getLeading();
				}
			} else if (currentName == null) {
				FontRenderContext frc = g2.getFontRenderContext();
				TextLayout tl = new TextLayout(welcomeStr, font28, frc);
				float x = (float) (d.width / 2 - tl.getBounds().getWidth() / 2);
				tl.draw(g2, x, d.height / 2);
				if (credits != null) {
					credits.render(d, g2);
				}
			} else {
				g2.setFont(font24);
				g2.drawString(currentName, 5, fm28.getHeight() - 5);
				if (duration <= 0.0) {
					loading.render(d, g2);
				} else {
					double seconds = getSeconds();
					if (midiEOM || audioEOM) {
						seconds = duration;
					}
					if (seconds > 0.0) {
						g2.setFont(font42);
						String s = String.valueOf(seconds);
						s = s.substring(0, s.indexOf('.') + 2);
						int strW = (int) fm42.getStringBounds(s, g2).getWidth();
						g2.drawString(s, d.width - strW - 9, fm42.getAscent());

						int num = 30;
						int progress = (int) (seconds / duration * num);
						double ww = ((double) (d.width - 10) / (double) num);
						double hh = (int) (d.height * 0.25);
						double x = 0.0;
						for (; x < progress; x += 1.0) {
							g2.fill(new Rectangle2D.Double(x * ww + 5, d.height - hh - 5, ww - 1, hh));
						}
						g2.setColor(jfcDarkBlue);
						for (; x < num; x += 1.0) {
							g2.fill(new Rectangle2D.Double(x * ww + 5, d.height - hh - 5, ww - 1, hh));
						}
					}
				}
			}
		}

		public void start() {
			pbThread = new Thread(this);
			pbThread.setName("PlaybackMonitor");
			pbThread.start();
		}

		public void stop() {
			if (pbThread != null) {
				pbThread.interrupt();
			}
			pbThread = null;
		}

		public void run() {
			while (pbThread != null) {
				try {
					pbThread.sleep(99);
				} catch (Exception e) {
					break;
				}
				repaint();
			}
			pbThread = null;
		}
	} // End PlaybackMonitor


	/**
	 * Table to display the name of the sound.
	 */
	class JukeTable extends JPanel implements ActionListener {

		TableModel dataModel;
		JFrame frame;
		JTextField textField;
		JButton applyB;

		public JukeTable() {
			setLayout(new BorderLayout());
			setPreferredSize(new Dimension(260, 300));

			final String[] names = {"#", "Name"};

			dataModel = new AbstractTableModel() {
				public int getColumnCount() {
					return names.length;
				}

				public int getRowCount() {
					return sounds.size();
				}

				public Object getValueAt(int row, int col) {
					if (col == 0) {
						return new Integer(row);
					} else if (col == 1) {
						Object object = sounds.get(row);
						if (object instanceof File) {
							return ((File) object).getName();
						} else if (object instanceof URL) {
							return ((URL) object).getFile();
						}
					}
					return null;
				}

				public String getColumnName(int col) {
					return names[col];
				}

				public Class getColumnClass(int c) {
					return getValueAt(0, c).getClass();
				}

				public boolean isCellEditable(int row, int col) {
					return false;
				}

				public void setValueAt(Object aValue, int row, int col) {
				}
			};

			table = new JTable(dataModel);
			TableColumn col = table.getColumn("#");
			col.setMaxWidth(20);
			table.sizeColumnsToFit(0);

			JScrollPane scrollPane = new JScrollPane(table);
			EmptyBorder eb = new EmptyBorder(5, 5, 2, 5);
			scrollPane.setBorder(new CompoundBorder(eb, new EtchedBorder()));
			add(scrollPane);

			JPanel p1 = new JPanel();
			JMenuBar menuBar = new JMenuBar();
			menuBar.setBorder(new BevelBorder(BevelBorder.RAISED));
			JMenu menu = (JMenu) menuBar.add(new JMenu("Add"));
			String items[] = {"File or Directory of Files", "URL"};
			for (int i = 0; i < items.length; i++) {
				JMenuItem item = menu.add(new JMenuItem(items[i]));
				item.addActionListener(this);
			}
			p1.add(menuBar);

			menuBar = new JMenuBar();
			menuBar.setBorder(new BevelBorder(BevelBorder.RAISED));
			menu = (JMenu) menuBar.add(new JMenu("Remove"));
			JMenuItem item = menu.add(new JMenuItem("Selected"));
			item.addActionListener(this);
			item = menu.add(new JMenuItem("All"));
			item.addActionListener(this);
			p1.add(menuBar);

			loopB = addButton("loop", p1);
			loopB.setBackground(Color.gray);
			loopB.setSelected(true);

			add("South", p1);
		}


		private JButton addButton(String name, JPanel p) {
			JButton b = new JButton(name);
			b.addActionListener(this);
			p.add(b);
			return b;
		}


		private void doFrame(String titleName) {
			int w = 500;
			int h = 130;
			JPanel panel = new JPanel(new BorderLayout());
			JPanel p1 = new JPanel();
			if (titleName.endsWith("URL")) {
				p1.add(new JLabel("URL :"));
				textField = new JTextField("http://foo.bar.com/foo.wav");
				textField.addActionListener(this);
			} else {
				p1.add(new JLabel("File or Dir :"));
				String sep = String.valueOf(System.getProperty("file.separator").toCharArray()[0]);
				String text = null;
				try {
					text = System.getProperty("user.dir") + sep;
				} catch (SecurityException ex) {
					reportStatus(ex.toString());
					JavaSound.showInfoDialog();
					return;
				}
				textField = new JTextField(text);
				textField.setPreferredSize(new Dimension(w - 100, 30));
				textField.addActionListener(this);
			}
			p1.add(textField);
			panel.add(p1);
			JPanel p2 = new JPanel();
			applyB = addButton("Apply", p2);
			addButton("Cancel", p2);
			panel.add("South", p2);
			frame = new JFrame(titleName);
			frame.getContentPane().add("Center", panel);
			frame.pack();
			Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
			frame.setLocation(d.width / 2 - w / 2, d.height / 2 - h / 2);
			frame.setSize(w, h);
			frame.setVisible(true);
		}


		public void actionPerformed(ActionEvent e) {
			Object object = e.getSource();
			if (object instanceof JTextField) {
				applyB.doClick();
			} else if (object instanceof JMenuItem) {
				JMenuItem mi = (JMenuItem) object;
				if (mi.getText().startsWith("File")) {
					doFrame("Add File or Directory");
				} else if (mi.getText().equals("URL")) {
					doFrame("Add URL");
				} else if (mi.getText().equals("Selected")) {
					int rows[] = table.getSelectedRows();
					Vector tmp = new Vector();
					for (int i = 0; i < rows.length; i++) {
						tmp.add(sounds.get(rows[i]));
					}
					sounds.removeAll(tmp);
					tableChanged();
				} else if (mi.getText().equals("All")) {
					sounds.clear();
					tableChanged();
				}
			} else if (object instanceof JButton) {
				JButton button = (JButton) e.getSource();
				if (button.getText().equals("Apply")) {
					String name = textField.getText().trim();
					if (name.startsWith("http") || name.startsWith("file")) {
						try {
							sounds.add(new URL(name));
						} catch (Exception ex) {
							ex.printStackTrace();
						}
						;
					} else {
						loadJuke(name);
					}
					tableChanged();
				} else if (button.getText().equals("Cancel")) {
					frame.dispose();
					frame = null;
					errStr = null;
					playbackMonitor.repaint();
				} else if (button.getText().equals("loop")) {
					loopB.setSelected(!loopB.isSelected());
					loopB.setBackground(loopB.isSelected() ? Color.gray : Color.lightGray);
				}
				startB.setEnabled(sounds.size() != 0);
			}
		}

		public void tableChanged() {
			table.tableChanged(new TableModelEvent(dataModel));
		}
	}  // End JukeTable


	/**
	 * Animation thread for when an audio file loads.
	 */
	class Loading extends Thread {

		double extent;
		int incr;

		public void run() {
			extent = 360.0;
			incr = 10;
			while (true) {
				try {
					sleep(99);
				} catch (Exception ex) {
					break;
				}
				playbackMonitor.repaint();
			}
		}

		public void render(Dimension d, Graphics2D g2) {
			if (isAlive()) {
				FontRenderContext frc = g2.getFontRenderContext();
				TextLayout tl = new TextLayout("Loading", g2.getFont(), frc);
				float sw = (float) tl.getBounds().getWidth();
				tl.draw(g2, d.width - sw - 45, d.height - 10);
				double x = d.width - 33, y = d.height - 30, ew = 25, eh = 25;
				g2.draw(new Ellipse2D.Double(x, y, ew, eh));
				g2.fill(new Arc2D.Double(x, y, ew, eh, 90, extent, Arc2D.PIE));
				if ((extent -= incr) < 0) {
					extent = 350.0;
				}
			}
		}
	}


	/**
	 * Animation thread for the contributors of Java Sound.
	 */
	class Credits extends Thread {

		int x;
		Font font16 = new Font("serif", Font.PLAIN, 16);
		String contributors = "Contributors : Kara Kytle, " +
						"Jan Borgersen, " + "Brian Lichtenwalter";
		int strWidth = getFontMetrics(font16).stringWidth(contributors);

		public void run() {
			x = -999;
			while (!playbackMonitor.isShowing()) {
				try {
					sleep(999);
				} catch (Exception e) {
					return;
				}
			}
			for (int i = 0; i < 100; i++) {
				try {
					sleep(99);
				} catch (Exception e) {
					return;
				}
			}
			while (true) {
				if (--x < -strWidth) {
					x = playbackMonitor.getSize().width;
				}
				playbackMonitor.repaint();
				try {
					sleep(99);
				} catch (Exception ex) {
					break;
				}
			}
		}

		public void render(Dimension d, Graphics2D g2) {
			if (isAlive()) {
				g2.setFont(font16);
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
								RenderingHints.VALUE_ANTIALIAS_OFF);
				g2.drawString(contributors, x, d.height - 5);
			}
		}
	}

	public static void main(String... args) {
		String media = "./audio";
		final Juke juke = new Juke(args.length == 0 ? media : args[0]);
		juke.open();
		JFrame f = new JFrame("Juke Box");
		f.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}

			public void windowIconified(WindowEvent e) {
				juke.credits.interrupt();
			}
		});
		f.getContentPane().add("Center", juke);
		f.pack();
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int w = 750;
		int h = 340;
		f.setLocation(screenSize.width / 2 - w / 2, screenSize.height / 2 - h / 2);
		f.setSize(w, h);
		f.setVisible(true);
		if (args.length > 0) {
			File file = new File(args[0]);
			if (file == null && !file.isDirectory()) {
				System.out.println("usage: java Juke audioDirectory");
			}
		}
	}
}
