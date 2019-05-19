/*
 * @(#)JavaSound.java	1.15	00/01/31
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
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;
import java.io.File;
import java.util.Vector;
import javax.sound.sampled.*;
import javax.sound.midi.*;

/**
 * The Java Sound Samples : MidiSynth, Juke, CapturePlayback, Groove.
 *
 * @author Brian Lichtenwalter
 * @version @(#)JavaSound.java	1.15 00/01/31
 */
public class JavaSound extends JPanel implements ChangeListener, Runnable {

	private Vector demos = new Vector(4);
	private JTabbedPane tabPane = new JTabbedPane();
	private int width = 760, height = 500;
	private int index;


	public JavaSound(String audioDirectory) {

		setLayout(new BorderLayout());

		JMenuBar menuBar = new JMenuBar();

		if (JavaSoundApplet.applet == null) {
			JMenu fileMenu = (JMenu) menuBar.add(new JMenu("File"));
			JMenuItem item = (JMenuItem) fileMenu.add(new JMenuItem("Exit"));
			item.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					System.exit(0);
				}
			});
		}
		JMenu options = (JMenu) menuBar.add(new JMenu("Options"));
		JMenuItem item = (JMenuItem) options.add(new JMenuItem("Applet Info"));
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showInfoDialog();
			}
		});
		add(menuBar, BorderLayout.NORTH);

		tabPane.addChangeListener(this);

		EmptyBorder eb = new EmptyBorder(5, 5, 5, 5);
		BevelBorder bb = new BevelBorder(BevelBorder.LOWERED);
		CompoundBorder cb = new CompoundBorder(eb, bb);
		JPanel p = new JPanel(new BorderLayout());
		p.setBorder(new CompoundBorder(cb, new EmptyBorder(0, 0, 90, 0)));
		final Juke juke = new Juke(audioDirectory);
		p.add(juke);
		demos.add(juke);
		tabPane.addTab("Juke Box", p);

		new Thread(this).start();

		add(tabPane, BorderLayout.CENTER);
	}

	public void stateChanged(ChangeEvent e) {
		close();
		System.gc();
		index = tabPane.getSelectedIndex();
		open();
	}

	public void close() {
		((ControlContext) demos.get(index)).close();
	}

	public void open() {
		((ControlContext) demos.get(index)).open();
	}

	public Dimension getPreferredSize() {
		return new Dimension(width, height);
	}

	public static void showInfoDialog() {
		final String msg =
						"When running the Java Sound demo as an applet these permissions\n" +
										"are necessary in order to load/save files and record audio :  \n\n" +
										"grant { \n" +
										"  permission java.io.FilePermission \"<<ALL FILES>>\", \"read, write\";\n" +
										"  permission javax.sound.sampled.AudioPermission \"record\"; \n" +
										"  permission java.util.PropertyPermission \"user.dir\", \"read\";\n" +
										"}; \n\n" +
										"The permissions need to be added to the .java.policy file.";
		new Thread(new Runnable() {
			public void run() {
				JOptionPane.showMessageDialog(null, msg, "Applet Info", JOptionPane.INFORMATION_MESSAGE);
			}
		}).start();
	}

	/**
	 * Lazy load the tabbed pane with CapturePlayback, MidiSynth and Groove.
	 */
	public void run() {
		EmptyBorder eb = new EmptyBorder(5, 5, 5, 5);
		BevelBorder bb = new BevelBorder(BevelBorder.LOWERED);
		CompoundBorder cb = new CompoundBorder(eb, bb);
		JPanel p = new JPanel(new BorderLayout());
		p.setBorder(new CompoundBorder(cb, new EmptyBorder(0, 0, 90, 0)));
		CapturePlayback capturePlayback = new CapturePlayback();
		demos.add(capturePlayback);
		p.add(capturePlayback);
		tabPane.addTab("Capture/Playback", p);

		MidiSynth midiSynth = new MidiSynth();
		demos.add(midiSynth);
		tabPane.addTab("Midi Synthesizer", midiSynth);

		p = new JPanel(new BorderLayout());
		p.setBorder(new CompoundBorder(cb, new EmptyBorder(0, 0, 5, 20)));
		Groove groove = new Groove();
		demos.add(groove);
		p.add(groove);
		tabPane.addTab("Groove Box", p);
	}

	public static void main(String... args) {

		try {
			if (MidiSystem.getSequencer() == null) {
				System.err.println("MidiSystem Sequencer Unavailable, exiting!");
				System.exit(1);
			} else if (AudioSystem.getMixer(null) == null) {
				System.err.println("AudioSystem Unavailable, exiting!");
				System.exit(1);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			System.exit(1);
		}

		String media = "./audio";
		if (args.length > 0) {
			File file = new File(args[0]);
			if (file == null && !file.isDirectory()) {
				System.out.println("usage: java JavaSound audioDirectory");
			} else {
				media = args[0];
			}
		}

		final JavaSound demo = new JavaSound(media);
		JFrame f = new JFrame("Java Sound Demo");
		f.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}

			public void windowDeiconified(WindowEvent e) {
				demo.open();
			}

			public void windowIconified(WindowEvent e) {
				demo.close();
			}
		});
		f.getContentPane().add("Center", demo);
		f.pack();
		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		f.setLocation(d.width / 2 - demo.width / 2, d.height / 2 - demo.height / 2);
		f.setSize(new Dimension(demo.width, demo.height));
		f.setVisible(true);
	}
}
