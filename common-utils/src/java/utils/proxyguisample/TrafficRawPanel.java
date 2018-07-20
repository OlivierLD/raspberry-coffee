package utils.proxyguisample;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.SystemColor;

public class TrafficRawPanel
		extends JPanel {
	private BorderLayout borderLayout1 = new BorderLayout();
	private JScrollPane jScrollPane = new JScrollPane();
	private JTextArea textArea = new JTextArea();

	public TrafficRawPanel() {
		try {
			jbInit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void jbInit() {
		this.setLayout(borderLayout1);
		this.setSize(new Dimension(300, 500));
		this.setPreferredSize(new Dimension(300, 500));
		this.setMinimumSize(new Dimension(70, 70));

		textArea.setFont(new Font("Source Code Pro", 0, 11));
		textArea.setForeground(Color.green);
		textArea.setBackground(SystemColor.windowText);
		jScrollPane.getViewport().add(textArea, null);
		this.add(jScrollPane, BorderLayout.CENTER);
	}

	public void addData(String line) {
		textArea.setText(textArea.getText() + "\n" + line);
		textArea.setCaretPosition(textArea.getDocument().getLength());
	}

	public void paintComponent(Graphics gr) {
		super.paintComponent(gr);
	}
}
