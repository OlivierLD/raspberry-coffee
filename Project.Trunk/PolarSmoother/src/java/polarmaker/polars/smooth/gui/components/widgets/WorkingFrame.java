package polarmaker.polars.smooth.gui.components.widgets;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import java.awt.Dimension;
import java.awt.Rectangle;

public class WorkingFrame extends JFrame {
	private JProgressBar workingProgressBar = new JProgressBar();
	private JLabel label = new JLabel();

	public WorkingFrame() {
		try {
			jbInit();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public JProgressBar getProgressBar() {
		return workingProgressBar;
	}

	public void setText(String str) {
		label.setText(str);
	}

	private void jbInit() throws Exception {
		this.getContentPane().setLayout(null);
		this.setSize(new Dimension(400, 128));
		this.setTitle("Please Wait...");
		workingProgressBar.setBounds(new Rectangle(25, 55, 345, 20));
		label.setText("Working, please wait...");
		label.setBounds(new Rectangle(25, 30, 345, 15));
		this.getContentPane().add(label, null);
		this.getContentPane().add(workingProgressBar, null);
	}
}
