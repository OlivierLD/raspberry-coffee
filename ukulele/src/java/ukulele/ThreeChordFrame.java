package ukulele;

import chords.ChordList;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import section.one.ThreeChordPanel;

public class ThreeChordFrame
				extends JFrame {
	private int imgIndex = 0;
	private static final DecimalFormat DF = new DecimalFormat("000");

	private int chordIndex = -1;

	private ThreeChordPanel threeChordPanel = new ThreeChordPanel("C F G7");
	private BorderLayout borderLayout1 = new BorderLayout();
	private JPanel bottomPanel = new JPanel();
	private JButton backButton = new JButton();
	private JButton forwardButton = new JButton();

	public ThreeChordFrame() {
		try {
			jbInit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void jbInit()
					throws Exception {
		getContentPane().setLayout(this.borderLayout1);
		setSize(new Dimension(310, 180));
		setTitle("3 Chords");
		this.threeChordPanel.setBackground(Color.white);
		this.backButton.setText("<");
		this.backButton.setActionCommand("back");
		this.backButton.setMaximumSize(new Dimension(21, 21));
		this.backButton.setMinimumSize(new Dimension(21, 21));
		this.backButton.setPreferredSize(new Dimension(21, 21));
		this.backButton.setMargin(new Insets(1, 1, 1, 1));
		this.backButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ThreeChordFrame.this.backButton_actionPerformed(e);
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
				ThreeChordFrame.this.forwardButton_actionPerformed(e);
			}
		});
		this.threeChordPanel.setSize(110, 180);
		getContentPane().add(this.threeChordPanel, BorderLayout.CENTER);
		this.bottomPanel.add(this.backButton, null);
		this.bottomPanel.add(this.forwardButton, null);
		getContentPane().add(this.bottomPanel, "South");
	}

	private void backButton_actionPerformed(ActionEvent e) {
		this.chordIndex -= 1;
		if (this.chordIndex < 0) {
			this.chordIndex = (ChordList.getChords().length - 1);
		}
	}

	private void forwardButton_actionPerformed(ActionEvent e) {
		this.chordIndex += 1;
		if (this.chordIndex > ChordList.getChords().length - 1) {
			this.chordIndex = 0;
		}

		this.threeChordPanel.setData("A B C");
	}
}
