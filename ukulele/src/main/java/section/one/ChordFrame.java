package section.one;

import chordfinder.AllChordFrame_AboutBoxPanel1;

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
	private JMenuItem menuFileSave = new JMenuItem();
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

	private void jbInit() throws Exception {
		setJMenuBar(this.menuBar);
		getContentPane().setLayout(null);
		setSize(new Dimension(614, 688));
		setTitle("Ukulele Chord Finder - Tuned G C E A");
		this.menuFile.setText("File");
		this.menuFileSave.setText("Create image from content");
		this.menuFileExit.setText("Exit");
		this.menuFileSave.addActionListener(ae -> ChordFrame.this.fileSave_ActionPerformed(ae));
		this.menuFileExit.addActionListener(ae -> ChordFrame.this.fileExit_ActionPerformed(ae));
		this.menuHelp.setText("Help");
		this.menuHelpAbout.setText("About");
		this.menuHelpAbout.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				ChordFrame.this.helpAbout_ActionPerformed(ae);
			}
		});
		this.menuFile.add(this.menuFileSave);
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
	void fileSave_ActionPerformed(ActionEvent e) {
		Component selectedPane = this.tabbedPane.getSelectedComponent();
		System.out.printf("Selected Element: a %s\n", selectedPane.getClass().getName());
		if (selectedPane instanceof PanelWithContent) {
			createImageFromContent(((PanelWithContent)selectedPane).getJTable(), new File("./ChordPane.png"), "png");
		} else {
			System.out.println("Not that one...");
		}
	}

	void helpAbout_ActionPerformed(ActionEvent e) {
		JOptionPane.showMessageDialog(this, new AllChordFrame_AboutBoxPanel1(), "About", JOptionPane.INFORMATION_MESSAGE);
	}

	/**
	 * Save the current FULL component view to a file.
	 * @param component the component to create the image from
	 * @param f the file to create
	 * @param ext the image extension (jpg, png, etc), used by ImageIO, no dot in this value.
	 */
	public void createImageFromContent(Component component, File f, String ext) {

		int width = component.getWidth();
		int height = component.getHeight();

		// Create a buffered image in which to draw
		final BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Thread imageGenerator = new Thread(() -> {
			try {
				SwingUtilities.invokeAndWait(() -> {
					// Create a graphics contents on the buffered image
					Graphics2D g2d = bufferedImage.createGraphics();
					// instance.paintComponent(g2d);
					component.paint(g2d);
					// Write generated image to a file
					try {
						OutputStream os = new FileOutputStream(f);
						ImageIO.write(bufferedImage, ext, os);
						os.flush();
						os.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
					// Graphics context no longer needed so dispose it
					g2d.dispose();
				});
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			System.out.printf(">> End of image generator, for %s\n", f.getAbsolutePath());
		});
		imageGenerator.start();
	}

}
