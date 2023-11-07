package chordfinder;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import javax.imageio.ImageIO;
import javax.swing.*;


public class AllChordFrame
				extends JFrame {
	private BorderLayout borderLayout = new BorderLayout();
	private JPanel tablePane = new AllChordPanel();

	private JMenuBar menuBar = new JMenuBar();
	private JMenu menuFile = new JMenu();
	private JMenuItem menuFileExit = new JMenuItem();
	private JMenuItem menuSaveAsImage = new JMenuItem();
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
		this.menuSaveAsImage.setText("Save as image");
		this.menuFileExit.setText("Exit");
		this.menuFileExit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				AllChordFrame.this.fileExit_ActionPerformed(ae);
			}
		});
		this.menuSaveAsImage.addActionListener(ae -> AllChordFrame.this.fileSaveAsImage_ActionPerformed(ae));
		this.menuHelp.setText("Help");
		this.menuHelpAbout.setText("About");
		this.menuHelpAbout.addActionListener(ae -> AllChordFrame.this.helpAbout_ActionPerformed(ae));
		this.menuFile.add(this.menuSaveAsImage);
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

	void fileSaveAsImage_ActionPerformed(ActionEvent a) {
		System.out.println("Saving...");
		File f = new File("allchords.png");
		((AllChordPanel)tablePane).createImageFromContent(f, "png", this.getWidth(), this.getHeight());
	}
	void helpAbout_ActionPerformed(ActionEvent e) {
		JOptionPane.showMessageDialog(this, new AllChordFrame_AboutBoxPanel1(), "About", -1);
	}
}
