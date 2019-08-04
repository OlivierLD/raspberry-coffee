package section.one;

import chordfinder.AllChordFrame_AboutBoxPanel1;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import ukulele.ChordPanel;
import static ukulele.ChordPanel.IDENTIFIER_MODE;

public class ChordFrame
				extends JFrame {
	private BorderLayout borderLayout = new BorderLayout();
	private JPanel keyChordPanel = new KeyChordPanel();
	private JPanel vampChordPanel = new VampChordPanel();
	private JPanel principalChordPanel = new PrincipalChordPanel();
	private JPanel tonalChordPanel = new TonalRegionChordPanel();

	private ChordPanel chordIdentifierPanel = new ChordPanel();


	private JTabbedPane tabbedPane = new JTabbedPane();

	private JMenuBar menuBar = new JMenuBar();
	private JMenu menuFile = new JMenu();
	private JMenuItem menuFileExit = new JMenuItem();
	private JMenu menuHelp = new JMenu();
	private JMenuItem menuHelpAbout = new JMenuItem();

	public ChordFrame() {
		try {
			jbInit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void jbInit()
					throws Exception {
		setJMenuBar(this.menuBar);
		getContentPane().setLayout(null);
		setSize(new Dimension(614, 688));
		setTitle("Ukulele Chord Finder - Tuned G C E A");
		this.menuFile.setText("File");
		this.menuFileExit.setText("Exit");
		this.menuFileExit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				ChordFrame.this.fileExit_ActionPerformed(ae);
			}
		});
		this.menuHelp.setText("Help");
		this.menuHelpAbout.setText("About");
		this.menuHelpAbout.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				ChordFrame.this.helpAbout_ActionPerformed(ae);
			}
		});
		this.menuFile.add(this.menuFileExit);
		this.menuBar.add(this.menuFile);
		this.menuHelp.add(this.menuHelpAbout);
		this.menuBar.add(this.menuHelp);
		setLayout(this.borderLayout);

		add(this.tabbedPane, BorderLayout.CENTER);
		this.tabbedPane.add("Keys", this.keyChordPanel);
		this.tabbedPane.add("Vamp Chords", this.vampChordPanel);
		this.tabbedPane.add("Principal Chords", this.principalChordPanel);
		this.tabbedPane.add("Tonal Regions Chart", this.tonalChordPanel);
		this.chordIdentifierPanel.setChordMode(IDENTIFIER_MODE);
		this.tabbedPane.add("Chord Identifier", this.chordIdentifierPanel);
	}

	void fileExit_ActionPerformed(ActionEvent e) {
		System.exit(0);
	}

	void helpAbout_ActionPerformed(ActionEvent e) {
		JOptionPane.showMessageDialog(this, new AllChordFrame_AboutBoxPanel1(), "About", -1);
	}
}
