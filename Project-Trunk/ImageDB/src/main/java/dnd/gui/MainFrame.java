package dnd.gui;

import dnd.gui.ctx.AppContext;
import dnd.gui.utils.ImageDBUtils;
import dnd.gui.utils.ImageDefinition;
import dnd.gui.utils.ImagePanel;
import dnd.gui.utils.ImageTable;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;

public class MainFrame
		extends JFrame {
	private static final long serialVersionUID = 1864622636330896530L;
	private BorderLayout layoutMain = new BorderLayout();
	private JPanel panelCenter = new JPanel();
	private JMenuBar menuBar = new JMenuBar();
	private JMenu menuFile = new JMenu();
	private JMenuItem menuFileExit = new JMenuItem();
	private JMenu menuHelp = new JMenu();
	private JMenuItem menuHelpAbout = new JMenuItem();
	private JLabel statusBar = new JLabel();
	private JToolBar toolBar = new JToolBar();

	private JButton buttonHelp = new JButton();

	private ImageIcon imageHelp = new ImageIcon(MainFrame.class.getResource("help.gif"));
	private JSplitPane jSplitPane1 = new JSplitPane();
	private BorderLayout borderLayout1 = new BorderLayout();

	private ImageTable imageTable = null;
	private ImagePanel imagePanel = new ImagePanel();

	private MainFrame instance = this;

	public MainFrame() {
		try {
			jbInit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void jbInit() throws Exception {
		setJMenuBar(this.menuBar);
		getContentPane().setLayout(this.layoutMain);
		this.panelCenter.setLayout(this.borderLayout1);
		setSize(new Dimension(1162, 742));
		setTitle("Image DB");
		this.menuFile.setText("File");
		this.menuFileExit.setText("Exit");
		this.menuFileExit.addActionListener(ae -> fileExit_ActionPerformed(ae));
		this.menuHelp.setText("Help");
		this.menuHelpAbout.setText("About");
		this.menuHelpAbout.addActionListener(ae -> helpAbout_ActionPerformed(ae));
		this.statusBar.setText("");

		this.buttonHelp.setToolTipText("Help");
		this.buttonHelp.setIcon(this.imageHelp);
		this.buttonHelp.addActionListener(e ->
				JOptionPane
						.showMessageDialog(
								MainFrame.this.instance,
								"<html><b>Drag & drop</b> images on the right pane to insert them in the database.<br>Then update the <b>tags</b>, separated with a comma.<br>The <b>filter</b> field searches the tags. As well, use comma to separated them.<br>Like \"<i>ugly, horrible, disgusting</i>\".<br>Joker character is '%'<br>The <b>and</b> radio button combines the search criteria (tags), the <b>or</b> decombines them.</html>",
								"Help!", JOptionPane.INFORMATION_MESSAGE));
		this.menuFile.add(this.menuFileExit);
		this.menuBar.add(this.menuFile);
		this.menuHelp.add(this.menuHelpAbout);
		this.menuBar.add(this.menuHelp);
		getContentPane().add(this.statusBar, BorderLayout.SOUTH);

		this.toolBar.add(this.buttonHelp);
		getContentPane().add(this.toolBar, BorderLayout.NORTH);

		this.jSplitPane1.setOneTouchExpandable(true);
		this.jSplitPane1.setContinuousLayout(true);
		this.panelCenter.add(this.jSplitPane1, BorderLayout.CENTER);

		List<ImageDefinition> imageList = ImageDBUtils.populateImageList(AppContext.getInstance().getConn());
		this.imageTable = new ImageTable(imageList);
		this.jSplitPane1.setLeftComponent(this.imageTable);
		this.jSplitPane1.setRightComponent(this.imagePanel);

		getContentPane().add(this.panelCenter, BorderLayout.CENTER);
	}

	private void fileExit_ActionPerformed(ActionEvent e) {
		AppContext.getInstance().closeConnection();
		System.exit(0);
	}

	private void helpAbout_ActionPerformed(ActionEvent e) {
		JOptionPane.showMessageDialog(this, new MainFrame_AboutBoxPanel(), "About", JOptionPane.PLAIN_MESSAGE);
	}
}
