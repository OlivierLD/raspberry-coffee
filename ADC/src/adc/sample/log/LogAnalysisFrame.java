package adc.sample.log;

import adc.sample.log.LogAnalysis.LogData;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;
import java.util.Map;

public class LogAnalysisFrame
		extends JFrame {
	private JMenuBar menuBar = new JMenuBar();
	private JMenu menuFile = new JMenu();
	private JMenuItem menuFileExit = new JMenuItem();

	private LogAnalysisPanel displayPanel = null;
	private transient LogAnalysis caller;
	private JScrollPane jScrollPane = null;

	public LogAnalysisFrame(LogAnalysis parent) {
		this.caller = parent;
		displayPanel = new LogAnalysisPanel();
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
		this.setSize(new Dimension(1_000, 275));
		this.setTitle("Battery Data");
		menuFile.setText("File");
		menuFileExit.setText("Exit");
		menuFileExit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				fileExit_ActionPerformed(ae);
			}
		});
		menuFile.add(menuFileExit);
		menuBar.add(menuFile);

		displayPanel.setPreferredSize(new Dimension(1_400, 275));
		jScrollPane = new JScrollPane(displayPanel);
//  this.getContentPane().add(displayPanel, BorderLayout.CENTER);
		this.getContentPane().add(jScrollPane, BorderLayout.CENTER);

	}

	void fileExit_ActionPerformed(ActionEvent e) {
		System.out.println(e.getActionCommand());
//  this.caller.close();
		System.exit(0);
	}

	public void setLogData(Map<Date, LogData> logdata) {
		displayPanel.setLogData(logdata);
	}
}
