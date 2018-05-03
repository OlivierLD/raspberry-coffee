package hanoitower.gui;

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Dimension;

public class HanoiFrame extends JFrame {

	private static final long serialVersionUID = 1L;
	private BorderLayout layoutMain;
	private JPanel hanoiPanel;

	public HanoiFrame() {
		layoutMain = new BorderLayout();
		hanoiPanel = new PanelHolder();
		try {
			jbInit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void jbInit() throws Exception {
		getContentPane().setLayout(layoutMain);
		setSize(new Dimension(1_124, 426));
		String title = System.getProperty("frame.title", "Hanoi Tower");
		setTitle(title);
		getContentPane().add(hanoiPanel, BorderLayout.CENTER);
	}

}
