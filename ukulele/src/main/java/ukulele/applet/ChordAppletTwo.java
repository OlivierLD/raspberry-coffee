package ukulele.applet;

import chordfinder.UkuleleChordFinder;
import ctx.AppContext;
import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JApplet;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import section.one.KeyChordPanel;
import section.one.PrincipalChordPanel;
import section.one.TonalRegionChordPanel;
import section.one.VampChordPanel;
import ukulele.ChordPanel;
import static ukulele.ChordPanel.IDENTIFIER_MODE;

@SuppressWarnings("deprecation")
public class ChordAppletTwo
				extends JApplet {
	private BorderLayout borderLayout1 = new BorderLayout();

	private JPanel keyChordPanel = new KeyChordPanel();
	private JPanel vampChordPanel = new VampChordPanel();
	private JPanel principalChordPanel = new PrincipalChordPanel();
	private JPanel tonalChordPanel = new TonalRegionChordPanel();
	private ChordPanel chordIdentifierPanel = new ChordPanel();

	private JTabbedPane tabbedPane = new JTabbedPane();

	private void jbInit()
					throws Exception {
		getContentPane().setLayout(this.borderLayout1);
		setSize(new Dimension(820, 525));
		getContentPane().add(this.tabbedPane, BorderLayout.CENTER);
		this.tabbedPane.add("Keys", this.keyChordPanel);
		this.tabbedPane.add("Vamp Chords", this.vampChordPanel);
		this.tabbedPane.add("Principal Chords", this.principalChordPanel);
		this.tabbedPane.add("Tonal Regions Chart", this.tonalChordPanel);
		this.chordIdentifierPanel.setChordMode(IDENTIFIER_MODE);
		this.tabbedPane.add("Chord Identifier", this.chordIdentifierPanel);
	}

	public void init() {
		String lang;
		try {
			lang = getParameter("lang");
		} catch (Exception ex) {
			lang = "EN";
		}
		AppContext.getInstance().fireUserLanguage(lang);
		try {
			jbInit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void jButton1_actionPerformed(ActionEvent e) {
		UkuleleChordFinder.main((String[])null);
	}

	public static void main(String... args) {
		System.out.println("You can also run section.one.KeyChordFinder");
		JFrame frame = new JFrame("Main for Applet");
		frame.setSize(820, 525);

		final Applet applet = new ChordAppletTwo();

		frame.getContentPane().add(applet);
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent we) {
				applet.stop();
				applet.destroy();
				System.exit(0);
			}
		});

		frame.setVisible(true);
		applet.init();
		applet.start();
	}
}
