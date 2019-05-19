package ukulele;

import chords.ChordList;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;


public class ChordFrame
				extends JFrame {
	private int imgIndex = 0;
	private static final DecimalFormat DF = new DecimalFormat("000");

	private int chordIndex = -1;

	private ChordPanel chordPanel = new ChordPanel();
	private BorderLayout borderLayout1 = new BorderLayout();
	private JPanel bottomPanel = new JPanel();
	private JButton backButton = new JButton();
	private JButton forwardButton = new JButton();

	public ChordFrame() {
		try {
			jbInit();
		} catch (Exception e) {
			e.printStackTrace();
		}

		Thread loopThread = new Thread() {
			public void run() {
				int i = 0;
				for (; ; ) {
					if (i >= ChordList.getChords().length) i = 0;
					ChordFrame.this.imgIndex = i;
					ChordFrame.this.setChord(ChordList.getChords()[i]);
					try {
						Thread.sleep(1_000L);
					} catch (Exception ignore) {
					}
					i++;
				}
			}


		};
		setChord(null);
	}

	private void jbInit()
					throws Exception {
		getContentPane().setLayout(this.borderLayout1);
		setSize(new Dimension(110, 180));
		setTitle("Tablature");
		this.chordPanel.setBackground(Color.white);
		this.backButton.setText("<");
		this.backButton.setActionCommand("back");
		this.backButton.setMaximumSize(new Dimension(21, 21));
		this.backButton.setMinimumSize(new Dimension(21, 21));
		this.backButton.setPreferredSize(new Dimension(21, 21));
		this.backButton.setMargin(new Insets(1, 1, 1, 1));
		this.backButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ChordFrame.this.backButton_actionPerformed(e);
			}
		});
		this.forwardButton.setText(">");
		this.forwardButton.setActionCommand("forward");
		this.forwardButton.setMaximumSize(new Dimension(21, 21));
		this.forwardButton.setMinimumSize(new Dimension(21, 21));
		this.forwardButton.setPreferredSize(new Dimension(21, 21));
		this.forwardButton.setMargin(new Insets(1, 1, 1, 1));
		this.forwardButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ChordFrame.this.forwardButton_actionPerformed(e);
			}
		});
		this.chordPanel.setSize(110, 180);
		getContentPane().add(this.chordPanel, BorderLayout.CENTER);
		this.bottomPanel.add(this.backButton, null);
		this.bottomPanel.add(this.forwardButton, null);
		getContentPane().add(this.bottomPanel, "South");
	}

	public void setChord(Chord chord) {
		this.chordPanel.setChord(chord);
		repaint();

		BufferedImage img = this.chordPanel.makeImage(this.chordPanel.getWidth(), this.chordPanel.getHeight());
		String fileName = "img" + DF.format(this.imgIndex) + ".png";
		try {
			this.chordPanel.saveImage(img, "png", fileName);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void backButton_actionPerformed(ActionEvent e) {
		this.chordIndex -= 1;
		if (this.chordIndex < 0) this.chordIndex = (ChordList.getChords().length - 1);
		setChord(ChordList.getChords()[this.chordIndex]);
	}

	private void forwardButton_actionPerformed(ActionEvent e) {
		this.chordIndex += 1;
		if (this.chordIndex > ChordList.getChords().length - 1) this.chordIndex = 0;
		setChord(ChordList.getChords()[this.chordIndex]);
	}
}
