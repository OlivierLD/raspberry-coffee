package adc.gui;

import adc.ADCObserver;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

public class AnalogDisplayFrame
		extends JFrame {
	private JMenuBar menuBar = new JMenuBar();
	private JMenu menuFile = new JMenu();
	private JMenuItem menuFileExit = new JMenuItem();

	private AnalogDisplayPanel displayPanel = null;
	private transient AnalogDisplayApp caller;

	public AnalogDisplayFrame(ADCObserver.MCP3008_input_channels channel, AnalogDisplayApp parent) {
		this.caller = parent;
		displayPanel = new AnalogDisplayPanel(channel, 100);
		try {
			jbInit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void jbInit()
			throws Exception {
		this.setJMenuBar(menuBar);
		this.getContentPane().setLayout(new BorderLayout());
		this.setSize(new Dimension(400, 275));
		this.setTitle("Volume");
		menuFile.setText("File");
		menuFileExit.setText("Exit");
		menuFileExit.addActionListener(ae -> {
			fileExit_ActionPerformed(ae);
		});
//		menuFileExit.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent ae) {
//				fileExit_ActionPerformed(ae);
//			}
//		});

		menuFile.add(menuFileExit);
		menuBar.add(menuFile);

		this.getContentPane().add(displayPanel, BorderLayout.CENTER);
	}

	void fileExit_ActionPerformed(ActionEvent e) {
		System.out.println(e.getActionCommand());
		this.caller.close();
		System.exit(0);
	}
}
