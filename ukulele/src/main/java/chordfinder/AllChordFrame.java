package chordfinder;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;


public class AllChordFrame
				extends JFrame {
	private BorderLayout borderLayout = new BorderLayout();
	private JPanel tablePane = new AllChordPanel();

	private JMenuBar menuBar = new JMenuBar();
	private JMenu menuFile = new JMenu();
	private JMenuItem menuFileExit = new JMenuItem();
	private JMenu menuHelp = new JMenu();
	private JMenuItem menuHelpAbout = new JMenuItem();

	public AllChordFrame() {
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
		setSize(new Dimension(931, 688));
		setTitle("Ukulele Chord Finder - Tuned G C E A");
		this.menuFile.setText("File");
		this.menuFileExit.setText("Exit");
		this.menuFileExit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				AllChordFrame.this.fileExit_ActionPerformed(ae);
			}
		});
		this.menuHelp.setText("Help");
		this.menuHelpAbout.setText("About");
		this.menuHelpAbout.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				AllChordFrame.this.helpAbout_ActionPerformed(ae);
			}
		});
		this.menuFile.add(this.menuFileExit);
		this.menuBar.add(this.menuFile);
		this.menuHelp.add(this.menuHelpAbout);
		this.menuBar.add(this.menuHelp);
		setLayout(this.borderLayout);
		add(this.tablePane, BorderLayout.CENTER);
	}

	void fileExit_ActionPerformed(ActionEvent e) {
		System.exit(0);
	}

	void helpAbout_ActionPerformed(ActionEvent e) {
		JOptionPane.showMessageDialog(this, new AllChordFrame_AboutBoxPanel1(), "About", -1);
	}
}
