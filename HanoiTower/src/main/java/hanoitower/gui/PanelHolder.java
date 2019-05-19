package hanoitower.gui;

import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Dimension;

public class PanelHolder extends JPanel {

	private static final long serialVersionUID = 1L;
	private HanoiPanel hanoiPanel;
	private ControlPanel controlPanel;

	public PanelHolder() {
		hanoiPanel = new HanoiPanel();
		controlPanel = new ControlPanel();
		try {
			jbInit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void jbInit() throws Exception {
		setLayout(new BorderLayout());
		setSize(new Dimension(630, 300));
		add(hanoiPanel, BorderLayout.CENTER);
		if ("true".equals(System.getProperty("with.control", "true"))) {
			add(controlPanel, BorderLayout.SOUTH);
		}
	}
}
