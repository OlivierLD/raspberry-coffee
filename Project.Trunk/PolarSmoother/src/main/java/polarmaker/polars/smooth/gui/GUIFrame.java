package polarmaker.polars.smooth.gui;

import polarmaker.Constants;
import polarmaker.polars.smooth.gui.components.MainPanel;
import polarmaker.polars.smooth.gui.components.PolarUtilities;
import polarmaker.polars.smooth.gui.components.polars.CoeffForPolars;
import polarmaker.polars.smooth.gui.components.tree.PolarTreeNode;
import polarmaker.polars.smooth.gui.components.widgets.ExtrapolationPanel;
import polarmaker.polars.smooth.gui.components.widgets.SmoothPrmPanel;
import polarmaker.smooth.PolarsResourceBundle;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JToolBar;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

public class GUIFrame
		extends JFrame {
	private DecimalFormat fmt = new DecimalFormat("##0.0");
	private boolean fileIsOpen = false;

	private SmoothPrmPanel spp = null;

	private String fName = null;

	private ImageIcon imageHelp = new ImageIcon(GUIFrame.class.getResource("help.png"));
	private ImageIcon imageClose = new ImageIcon(GUIFrame.class.getResource("save.png"));
	private ImageIcon imageOpen = new ImageIcon(GUIFrame.class.getResource("open.png"));
	private JButton buttonHelp = new JButton();
	private JButton buttonClose = new JButton();
	private JButton buttonOpen = new JButton();
	private JToolBar toolBar = new JToolBar();
	private JLabel statusBar = new JLabel();
	private JMenuItem menuHelpAbout = new JMenuItem();
	private JMenuItem menuSmoothPrm = new JMenuItem();
	private JMenu menuHelp = new JMenu();
	private JMenuItem menuFileNew = new JMenuItem();
	private JMenuItem menuFileOpen = new JMenuItem();
	private JMenuItem menuFileImportMaxSea = new JMenuItem();

	private JMenu menuFileReopen = new JMenu();

	private JMenuItem menuFileSave = new JMenuItem();
	private JMenuItem menuFileSaveAs = new JMenuItem();
	private JMenuItem menuFilePrint = new JMenuItem();
	private JMenuItem menuFileGenCoeff = new JMenuItem();
	private JMenuItem menuFileExportMaxSea = new JMenuItem();

	private JMenuItem menuFileExtrapolate = new JMenuItem();

	private JCheckBoxMenuItem plotBulkOnSmooth = new JCheckBoxMenuItem();
	private JMenuItem menuFileExit = new JMenuItem();
	private JMenu menuFile = new JMenu();
	private JMenu menuConfig = new JMenu();

	private JMenuBar menuBar = new JMenuBar();
	private JPanel panelCenter = new JPanel();
	private BorderLayout layoutMain = new BorderLayout();
	private MainPanel mainPanel1 = new MainPanel();
	private BorderLayout borderLayout1 = new BorderLayout();

	private boolean ok2exit = true;

	public GUIFrame() {
		this(true);
	}

	public GUIFrame(boolean canClose) {
		ok2exit = canClose;
		try {
			jbInit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private String lastFile = "";

	private void jbInit()
			throws Exception {
		Constants.readConstants();

		this.setIconImage(new ImageIcon(this.getClass().getResource("paperboat_32x32.png")).getImage());
		this.setJMenuBar(menuBar);
		this.getContentPane().setLayout(layoutMain);
		panelCenter.setLayout(borderLayout1);
		this.setSize(new Dimension(400, 600));

		this.setTitle(PolarsResourceBundle.getPolarsResourceBundle().getString("frame-title"));
		menuFile.setText(PolarsResourceBundle.getPolarsResourceBundle().getString("file"));
		menuFileNew.setText(PolarsResourceBundle.getPolarsResourceBundle().getString("new"));
		menuFileNew.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				fileNew_ActionPerformed(ae);
			}
		});
		menuFileOpen.setText(PolarsResourceBundle.getPolarsResourceBundle().getString("open"));
		menuFileOpen.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				fileOpen_ActionPerformed(ae);
			}
		});
		menuFileReopen.setText(PolarsResourceBundle.getPolarsResourceBundle().getString("re-open"));
		if (Constants.getLastOpenFile().length() > 0) {
			lastFile = Constants.getLastOpenFile().substring(Constants.getLastOpenFile().lastIndexOf(File.separator) + 1);
			JMenuItem fileToReopen = new JMenuItem(lastFile);
			fileToReopen.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					fName = Constants.getLastOpenFile();
					reOpen(fName);
				}
			});
			menuFileReopen.add(fileToReopen);
		}

		menuFileImportMaxSea.setText(PolarsResourceBundle.getPolarsResourceBundle().getString("open-maxsea"));
		menuFileImportMaxSea.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				fileImportMaxSea_ActionPerformed(ae);
			}
		});

		menuFileSave.setText(PolarsResourceBundle.getPolarsResourceBundle().getString("save"));
		menuFileSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				fileSave_ActionPerformed(ae);
			}
		});

		menuFileSaveAs.setText(PolarsResourceBundle.getPolarsResourceBundle().getString("save-as"));
		menuFileSaveAs.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				fileSaveAs_ActionPerformed(ae);
			}
		});

		menuFilePrint.setText(PolarsResourceBundle.getPolarsResourceBundle().getString("print"));
		menuFilePrint.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				filePrint_ActionPerformed(ae);
			}
		});

		menuFileGenCoeff.setText(PolarsResourceBundle.getPolarsResourceBundle().getString("gen-coeff"));
		menuFileGenCoeff.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				fileGen_ActionPerformed(ae);
			}
		});

		menuFileExportMaxSea.setText(PolarsResourceBundle.getPolarsResourceBundle().getString("gen-max-sea"));
		menuFileExportMaxSea.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				fileExportMaxSea_ActionPerformed(ae);
			}
		});

		menuFileExtrapolate.setText(PolarsResourceBundle.getPolarsResourceBundle().getString("extrapolate"));
		menuFileExtrapolate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				fileExtrapolate_ActionPerformed(ae);
			}
		});

		menuFileExit.setText(PolarsResourceBundle.getPolarsResourceBundle().getString("exit"));
		menuFileExit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				fileExit_ActionPerformed(ae);
			}
		});
		menuFileExit.setEnabled(ok2exit);
		menuConfig.setText(PolarsResourceBundle.getPolarsResourceBundle().getString("config"));
		menuSmoothPrm.setText(PolarsResourceBundle.getPolarsResourceBundle().getString("smooth-prm"));
		menuSmoothPrm.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				smoothPrm_ActionPerformed(ae);
			}
		});
		plotBulkOnSmooth.setText(PolarsResourceBundle.getPolarsResourceBundle().getString("plot-bulk"));
		plotBulkOnSmooth.setSelected(true);
		plotBulkOnSmooth.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				plotBulk_ActionPerformed(ae);
			}
		});

		menuHelp.setText(PolarsResourceBundle.getPolarsResourceBundle().getString("help"));
		menuHelpAbout.setText(PolarsResourceBundle.getPolarsResourceBundle().getString("about"));
		menuHelpAbout.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				helpAbout_ActionPerformed(ae);
			}
		});
		statusBar.setText(PolarsResourceBundle.getPolarsResourceBundle().getString("no-file-open"));
		buttonOpen.setToolTipText(PolarsResourceBundle.getPolarsResourceBundle().getString("open-file"));
		buttonOpen.setIcon(imageOpen);
		buttonOpen.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				buttonOpen_actionPerformed(e);
			}
		});
		buttonClose.setToolTipText(PolarsResourceBundle.getPolarsResourceBundle().getString("save-modif"));
		buttonClose.setIcon(imageClose);
		buttonClose.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				buttonSave_actionPerformed(e);
			}
		});
		buttonHelp.setToolTipText(PolarsResourceBundle.getPolarsResourceBundle().getString("get-help"));
		buttonHelp.setIcon(imageHelp);
		buttonHelp.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				helpAbout_ActionPerformed(e);
			}
		});

		menuFile.add(menuFileNew);
		menuFile.add(menuFileOpen);
		menuFile.add(menuFileReopen);
		menuFile.add(menuFileImportMaxSea);
		menuFile.add(menuFileSave);
		menuFile.add(menuFileSaveAs);
		menuFile.add(menuFilePrint);
		menuFile.add(new JSeparator());
		menuFile.add(menuFileGenCoeff);
		menuFile.add(menuFileExportMaxSea);
		menuFile.add(new JSeparator());
		menuFile.add(menuFileExtrapolate);
		menuFile.add(new JSeparator());
		menuFile.add(menuFileExit);
		menuBar.add(menuFile);
		menuConfig.add(menuSmoothPrm);
		menuConfig.add(plotBulkOnSmooth);
		menuBar.add(menuConfig);
		menuHelp.add(menuHelpAbout);
		menuBar.add(menuHelp);
		enableMenus(false);

		this.getContentPane().add(statusBar, BorderLayout.SOUTH);
		toolBar.add(buttonOpen);
		toolBar.add(buttonClose);
		toolBar.add(buttonHelp);
		this.getContentPane().add(toolBar, BorderLayout.NORTH);
		panelCenter.add(mainPanel1, BorderLayout.CENTER);
		this.getContentPane().add(panelCenter, BorderLayout.CENTER);
	}

	void enableMenus(boolean b) {
		fileIsOpen = b;
		menuFileSave.setEnabled(b);
		menuFileSaveAs.setEnabled(b);
		menuFilePrint.setEnabled(b);
		menuFileGenCoeff.setEnabled(b);
		menuFileExportMaxSea.setEnabled(b);
		menuFileExtrapolate.setEnabled(b);
	}

	void fileNew_ActionPerformed(ActionEvent e) {
		System.out.println("Creating new Data");
		// Choose
		String newFName = PolarUtilities.chooseFile(JFileChooser.FILES_AND_DIRECTORIES, "polar-data",
				"Polar Data",
				"Polar Data",
				"Create");
		if (newFName != null && newFName.trim().length() > 0) {
			try {
				File f = new File(newFName);
				boolean go = true;
				if (f.exists()) {
					int resp =
							JOptionPane.showConfirmDialog(this,
									newFName + " " + PolarsResourceBundle.getPolarsResourceBundle().getString("already-exists"), PolarsResourceBundle.getPolarsResourceBundle().getString("saving-data"),
									JOptionPane.YES_NO_OPTION,
									JOptionPane.QUESTION_MESSAGE);
					if (resp == JOptionPane.NO_OPTION) {
						go = false;
					}
				}
				if (go) {
					FileWriter fw = new FileWriter(f);

					fw.write("<polar-data xmlns=\"" + Constants.DATA_NAMESPACE_URI + "\"\n" +
							"            model=\"" + "new-model" + "\">\n");
					fw.write("  <polar-section name=\"one\"\n" +
							"                 polar-degree=\"" + Constants.DEFAULT_POLAR_DEGREE + "\"\n" +
							"                 coeff-degree=\"" + Constants.DEFAULT_COEFF_DEGREE + "\"\n" +
							"                 from-twa=\"20\"\n" +
							"                 to-twa=\"80\">\n");
					fw.write("    <tws value=\"0.0\"\n" +
							"         upwind-speed=\"0.0\"\n" +
							"         upwind-twa=\"0.0\"\n" +
							"         upwind-vmg=\"0.0\"\n" +
							"         downwind-speed=\"0.0\"\n" +
							"         downwind-twa=\"0.0\"\n" +
							"         downwind-vmg=\"0.0\">\n");
					fw.write("      <twa value=\"0\" bsp=\"0.0\"/>\n");
					fw.write("    </tws>\n");
					fw.write("  </polar-section>\n");
					fw.write("</polar-data>\n");
					fw.flush();
					fw.close();
					fName = newFName;
					if (mainPanel1 != null) {
						mainPanel1.setDataFile(fName);
					}
					statusBar.setText(fName);
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	private void fileOpen_ActionPerformed(ActionEvent e) {
		setDataFile();
	}

	public void reOpen(String fName) {
		if (fName != null && fName.trim().length() > 0) {
			if (mainPanel1 != null) {
				mainPanel1.setDataFile(fName);
			}
			statusBar.setText(fName);
			enableMenus(true);
		}
	}

	private void fileImportMaxSea_ActionPerformed(ActionEvent e) {
		importFromMaxSeaFile();
	}

	private void fileSave_ActionPerformed(ActionEvent e) {
		save(fName);
	}

	private void fileSaveAs_ActionPerformed(ActionEvent e) {
		// Choose
		String newFName = PolarUtilities.chooseFile(JFileChooser.FILES_AND_DIRECTORIES,
				"polar-data",
				"Polar Data",
				"Polar Data",
				"Save As");
		if (newFName != null && newFName.trim().length() > 0) {
			save(newFName);
		}
	}

	private void filePrint_ActionPerformed(ActionEvent e) {
		JOptionPane.showMessageDialog(this, "Soon...", "Print", JOptionPane.PLAIN_MESSAGE);

	}

	private void fileGen_ActionPerformed(ActionEvent e) {
		genCoeff();
	}

	private void fileExportMaxSea_ActionPerformed(ActionEvent e) {
		exportToMaxSea();
	}

	private ExtrapolationPanel ep = null;

	private void fileExtrapolate_ActionPerformed(ActionEvent ae) {
		if (ep == null) {
			ep = new ExtrapolationPanel();
		}
		int resp = JOptionPane.showConfirmDialog(this, ep, "Extrapolation", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		if (resp == JOptionPane.OK_OPTION) {
			double factor = ep.getFactor();
			mainPanel1.extrapolateSpeed(factor);
		}
	}

	private void fileExit_ActionPerformed(ActionEvent e) {
		System.exit(0);
	}

	private void buttonSave_actionPerformed(ActionEvent e) {
		save(fName);
	}

	private void save(String fn) {
		System.out.println("Saving XML Data");
		fn = PolarUtilities.makeSureExtensionIsOK(fn, ".polar-data");
		Object o = mainPanel1.getTreeRoot();
		if (o == null) {
			JOptionPane.showMessageDialog(this, PolarsResourceBundle.getPolarsResourceBundle().getString("no-data-loaded"), PolarsResourceBundle.getPolarsResourceBundle().getString("saving-data"),
					JOptionPane.WARNING_MESSAGE);
			System.out.println("Save what?");
		} else if (o instanceof PolarTreeNode) {
			PolarTreeNode root = (PolarTreeNode) o;
			if (root.getType() == PolarTreeNode.ROOT_TYPE) {
				System.out.println("Saving... [" + fn + "]");
				try {
					File f = new File(fn);
					boolean go = true;
					if (f.exists()) {
						int resp =
								JOptionPane.showConfirmDialog(this,
										fn + " " + PolarsResourceBundle.getPolarsResourceBundle().getString("already-exists"), PolarsResourceBundle.getPolarsResourceBundle().getString("saving-data"),
										JOptionPane.YES_NO_OPTION,
										JOptionPane.QUESTION_MESSAGE);
						if (resp == JOptionPane.NO_OPTION) {
							go = false;
						}
					}
					if (go) {
						FileWriter fw = new FileWriter(f);

						fw.write("<polar-data xmlns=\"" + Constants.DATA_NAMESPACE_URI + "\"\n" +
								"            model=\"" + root.getModel() + "\">\n");
						recurseTreeNode(root, fw);
						fw.write("</polar-data>\n");

						fw.flush();
						fw.close();
					} else {
						System.out.println("Operation canceled.");
					}
				} catch (IOException ioe) {
					JOptionPane.showMessageDialog(this, PolarsResourceBundle.getPolarsResourceBundle().getString("file-error") +
									ioe.getMessage(), PolarsResourceBundle.getPolarsResourceBundle().getString("saving-data"),
							JOptionPane.ERROR_MESSAGE);
				}
			} else {
				System.out.println("We should start from the root...");
			}
		} else {
			System.out.println("I cannot deal with a " + o.getClass().getName());
		}
	}

	private void recurseTreeNode(PolarTreeNode ptn, FileWriter fw)
			throws IOException {
		Enumeration children = ptn.children();
		if (children != null) {
			while (children.hasMoreElements()) {
				PolarTreeNode child = (PolarTreeNode) children.nextElement();
				if (child.getType() == PolarTreeNode.SECTION_TYPE) {
					fw.write("  <polar-section name=\"" + child.getModel() + "\"\n" +
							"                 polar-degree=\"" + child.getPolarDegree() + "\"\n" +
							"                 coeff-degree=\"" + child.getCoeffDegree() + "\"\n" +
							"                 from-twa=\"" + child.getFromTwa() + "\"\n" +
							"                 to-twa=\"" + child.getToTwa() + "\">\n");
					recurseTreeNode(child, fw);
					fw.write("  </polar-section>\n");
				} else if (child.getType() == PolarTreeNode.TWS_TYPE) {
					fw.write("    <tws value=\"" + child.getTws());
					if (child.getUpwindSpeed() != Double.MIN_VALUE) {
						fw.write("\"\n         upwind-speed=\"" + child.getUpwindSpeed());
					}
					if (child.getUpwindTwa() != Double.MIN_VALUE) {
						fw.write("\"\n         upwind-twa=\"" + child.getUpwindTwa());
					}
					if (child.getUpwindVmg() != Double.MIN_VALUE) {
						fw.write("\"\n         upwind-vmg=\"" + child.getUpwindVmg());
					}
					if (child.getDownwindSpeed() != Double.MIN_VALUE) {
						fw.write("\"\n         downwind-speed=\"" + child.getDownwindSpeed());
					}
					if (child.getDownwindTwa() != Double.MIN_VALUE) {
						fw.write("\"\n         downwind-twa=\"" + child.getDownwindTwa());
					}
					if (child.getDownwindVmg() != Double.MIN_VALUE) {
						fw.write("\"\n         downwind-vmg=\"" + child.getDownwindVmg());
					}
					fw.write("\">\n");
					recurseTreeNode(child, fw);
					fw.write("    </tws>\n");
				} else if (child.getType() == PolarTreeNode.TWA_TYPE) {
					fw.write("      <twa value=\"" + child.getTwa() + "\" bsp=\"" + child.getBsp() + "\"/>\n");
				} else {
					System.out.println("Unknown Tree Node Type:" + child.getType());
				}
			}
		}
	}

	private void genCoeff() {
		System.out.println("Generate coefficients");
		if (mainPanel1.getTreeRoot() != null) {
			String fName = PolarUtilities.chooseFile(JFileChooser.FILES_ONLY, "polar-coeff", "Coefficients", "Save Coefficient File", "Save As");
			if (fName.trim().length() > 0) {
				fName = PolarUtilities.makeSureExtensionIsOK(fName, ".polar-coeff");
				// Loop on the different sections. Assume we have a tree of sections, with all the degrees.
				PolarTreeNode treeRoot = (PolarTreeNode) mainPanel1.getTreeRoot();
				int nbSections = treeRoot.getChildCount();
				System.out.println("We have [" + nbSections + "] section(s).");
				assert (treeRoot.getType() == PolarTreeNode.ROOT_TYPE);

				try {
					FileWriter fw = new FileWriter(fName);
					fw.write("<?xml version='1.0'?>\n");
					fw.write("<all-section-root xmlns=\"" + Constants.POLAR_FUNCTION_NS_URI + "\">\n");

					Enumeration enumeration = treeRoot.children();
					while (enumeration.hasMoreElements()) {
						PolarTreeNode child = (PolarTreeNode) enumeration.nextElement();

						int polarDegree = child.getPolarDegree();
						int coeffDegree = child.getCoeffDegree();
						int fromTwa = child.getFromTwa();
						int toTwa = child.getToTwa();

						double[][] coeffDeg = MainPanel.generateCoefficients(child, polarDegree, coeffDegree);

						fw.write("  <polar-coeff-function from-twa=\"" + Integer.toString(fromTwa) + "\"\n" +
								"                        to-twa=\"" + Integer.toString(toTwa) + "\"\n" +
								"                        polar-degree=\"" + Integer.toString(polarDegree) + "\"\n" +
								"                        polar-coeff-degree=\"" + Integer.toString(coeffDegree) + "\">\n");
						for (int k = 0; k < (polarDegree + 1); k++) {
							fw.write("    <polar-coeff degree=\"" + Integer.toString(polarDegree - k) + "\">\n");
							for (int j = 0; j < coeffDeg[k].length; j++) {
								fw.write("      <coeff degree=\"" + Integer.toString(coeffDeg[k].length - j - 1) + "\">" + coeffDeg[k][j] + "</coeff>\n");
							}
							fw.write("    </polar-coeff>\n");
						}
						fw.write("  </polar-coeff-function>\n");
					}
					fw.write("</all-section-root>\n");
					fw.flush();
					fw.close();
					JOptionPane.showMessageDialog(this, PolarsResourceBundle.getPolarsResourceBundle().getString("coeff-ok") + fName, PolarsResourceBundle.getPolarsResourceBundle().getString("coeff-generation"),
							JOptionPane.INFORMATION_MESSAGE);
				} catch (Exception e) {
					e.printStackTrace();
					JOptionPane.showMessageDialog(this, PolarsResourceBundle.getPolarsResourceBundle().getString("coeff-error") + "\n" + e.getMessage(), PolarsResourceBundle.getPolarsResourceBundle().getString("coeff-generation"),
							JOptionPane.ERROR_MESSAGE);
				}
			}
		} else {
			System.out.println("Load a Data Tree first...");
			JOptionPane.showMessageDialog(this, PolarsResourceBundle.getPolarsResourceBundle().getString("no-data-loaded"), PolarsResourceBundle.getPolarsResourceBundle().getString("coeff-generation"),
					JOptionPane.WARNING_MESSAGE);
		}
	}

	void smoothPrm_ActionPerformed(ActionEvent e) {
		PolarTreeNode[] sn = mainPanel1.getSelectedNode();
		if (sn == null || (sn != null && sn.length != 1)) {
			JOptionPane.showMessageDialog(this, "<html>Select <b>one</b> Section Node</html>", "Smoothing", JOptionPane.WARNING_MESSAGE);
			return;
		}

		PolarTreeNode ptn = sn[0];
		if (ptn.getType() != PolarTreeNode.SECTION_TYPE) {
			JOptionPane.showMessageDialog(this, "<html>Select one <b>Section Node</b></html>", "Smoothing", JOptionPane.WARNING_MESSAGE);
			return;
		}

		if (spp == null) {
			spp = new SmoothPrmPanel();
		}
		spp.setMaxBSP(Constants.getMaxBoatSpeedForPolars());
		spp.setMinTWS(Constants.getMinWindSpeed());
		spp.setMaxTWS(Constants.getMaxWindSpeed());

		int resp =
				JOptionPane.showConfirmDialog(this,
						spp, PolarsResourceBundle.getPolarsResourceBundle().getString("smoothing-parameters"),
						JOptionPane.OK_CANCEL_OPTION,
						JOptionPane.QUESTION_MESSAGE);
		if (resp == JOptionPane.OK_OPTION) {
			try {
				Constants.setMaxBoatSpeedForPolars(spp.getMaxBSP());
				Constants.setMinWindSpeed(spp.getMinTWS());
				Constants.setMaxWindSpeed(spp.getMaxTWS());
				Constants.storeConstants();
				this.repaint();
			} catch (Exception ex) {
				JOptionPane.showMessageDialog(this, PolarsResourceBundle.getPolarsResourceBundle().getString("invalid-parameters-values") +
								ex.getMessage(), PolarsResourceBundle.getPolarsResourceBundle().getString("bad-values"),
						JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	void plotBulk_ActionPerformed(ActionEvent e) {
		mainPanel1.setPlotBulkOnSmoothPanel(plotBulkOnSmooth.isSelected());
		mainPanel1.repaint();
	}

	void helpAbout_ActionPerformed(ActionEvent e) {
		JOptionPane.showMessageDialog(this,
				new AboutPolarSmoother(), PolarsResourceBundle.getPolarsResourceBundle().getString("about"),
				JOptionPane.PLAIN_MESSAGE);
	}

	private void buttonOpen_actionPerformed(ActionEvent e) {
		setDataFile();
	}

	private void setDataFile() {
		fName = PolarUtilities.chooseFile(JFileChooser.FILES_AND_DIRECTORIES, "polar-data", "Polar Data", "Polar Data", "Open");
		reOpen(fName);
	}

	private void importFromMaxSeaFile() {
		fName = PolarUtilities.chooseFile(JFileChooser.FILES_AND_DIRECTORIES, "pol", "MaxSea Polars", "MaxSea", "Open");
		if (fName != null && fName.trim().length() > 0) {
			openMaxSeaPolarFile(fName);
		}
	}

	public void openMaxSeaPolarFile(String fName) {
		try {
			// Generate a temp XML File matching the data
			File tmp = File.createTempFile("FromMaxSea", ".polar-data");
			String f = tmp.getPath();
			System.out.println("Created " + f);
			BufferedWriter bw = new BufferedWriter(new FileWriter(tmp));
			BufferedReader br = new BufferedReader(new FileReader(fName));
			String line = "";
			int ln = 0;
			Vector<Vector<Object>> matrix = new Vector<Vector<Object>>();
			while ((line = br.readLine()) != null) {
				ln++;
				Vector<Object> thisLine = new Vector<Object>();
				StringTokenizer parser = new StringTokenizer(line);
				while (parser.hasMoreTokens()) {
					String data = parser.nextToken();
					//          System.out.println("Read:" + data);
					if (ln == 1) { // TWS array
						if (data.equals("TWA"))
							thisLine.add("NoData");
						else {
							Integer ws = new Integer(data);
							thisLine.add(ws);
						}
					} else {
						Double d = new Double(data);
						thisLine.add(d);
					}
				}
				matrix.add(thisLine);
			}
			// Now we have a square matrix
			int nbLines = matrix.size();
			String defaultName = fName.substring(fName.lastIndexOf(File.separator) + 1, fName.indexOf(".pol"));
			String nsURI = Constants.DATA_NAMESPACE_URI;
			bw.write("<polar-data xmlns=\"" + nsURI + "\"\n" +
					"            model=\"" + defaultName + "\">\n");

			bw.write("  <polar-section name='from MaxSea' \n");
			bw.write("                 from-twa='0'\n");
			bw.write("                 to-twa='180'\n");
			bw.write("                 polar-degree='" + Integer.toString(Constants.DEFAULT_POLAR_DEGREE) + "'\n");
			bw.write("                 coeff-degree='" + Integer.toString(Constants.DEFAULT_COEFF_DEGREE) + "'>\n");
			Vector tws = (Vector) matrix.elementAt(0);
			Enumeration enumeration = tws.elements();
			int twsIndex = 0;
			while (enumeration.hasMoreElements()) {
				Object o = enumeration.nextElement();
				if (o instanceof String) { // First Element, "TWA"
					continue;
				} else {
					// Must be an Integer
					Integer ws = (Integer) o;
					bw.write("    <tws value=\"" + ws.toString() + "\">\n");
					// We add a 0,0
					bw.write("      <twa value=\"0\" bsp=\"0.0\"/>\n");
					for (int idx = 1; idx < nbLines; idx++) {
						Double twa = (Double) ((Vector) matrix.elementAt(idx)).elementAt(0);
						Double bsp = (Double) ((Vector) matrix.elementAt(idx)).elementAt(twsIndex + 1);
						bw.write("      <twa value=\"" + Integer.toString(twa.intValue()) + "\" bsp=\"" + bsp + "\"/>\n");
					}
					bw.write("    </tws>\n");
				}
				twsIndex++;
			}
			bw.write("  </polar-section>\n");
			bw.write("</polar-data>\n");
			// Done
			br.close();
			bw.flush();
			bw.close();
			if (mainPanel1 != null) {
				mainPanel1.setDataFile(f);
			}
			statusBar.setText(f);
			enableMenus(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void exportToMaxSea() {
		System.out.println("Exporting coefficients in MaxSea format");
		// degrees are in Constants.POLAR_DEGREE and Constants.COEFF_DEGREE
		Constants.setLastOpenFile(fName);
		Constants.storeConstants();
		if (mainPanel1.getTreeRoot() != null) {
			// 1 - Re-generating all coeffs
			Enumeration sections = ((PolarTreeNode) mainPanel1.getTreeRoot()).children();
			List<CoeffForPolars> coefflist = new ArrayList<CoeffForPolars>();
			while (sections.hasMoreElements()) {
				PolarTreeNode section = (PolarTreeNode) sections.nextElement();
				double[][] coeffDeg = MainPanel.generateCoefficients(section, section.getPolarDegree(), section.getCoeffDegree());
				coefflist.add(new CoeffForPolars(coeffDeg, section.getPolarDegree(), section.getFromTwa(), section.getToTwa()));
			}
			try {
				// Choose a file name
				String fName = PolarUtilities.chooseFile(JFileChooser.FILES_AND_DIRECTORIES, "pol", "MaxSea Polar Data", "MaxSea", "Save As");
				fName = PolarUtilities.makeSureExtensionIsOK(fName, ".pol");

				if (fName != null && fName.trim().length() > 0) {
					FileWriter fw = new FileWriter(fName);
					fw.write("TWA\t");
					for (int i = Constants.getMinWindSpeed(); i <= Constants.getMaxWindSpeed(); i++) {
						fw.write(Integer.toString(i) + (i == Constants.getMaxWindSpeed() ? "" : "\t"));
					}
					fw.write("\n");

					for (int twa = Constants.MIN_WIND_ANGLE; twa <= Constants.MAX_WIND_ANGLE; twa += 2) {
						fw.write(Integer.toString(twa) + "\t");
						for (int tws = Constants.getMinWindSpeed(); tws <= Constants.getMaxWindSpeed(); tws++) {
							double bsp = PolarUtilities.getBSP(coefflist, tws, twa);
							fw.write(fmt.format(bsp) + (tws == Constants.getMaxWindSpeed() ? "" : "\t"));
						}
						fw.write("\n");
					}
					fw.flush();
					fw.close();
					JOptionPane.showMessageDialog(this,
							PolarsResourceBundle.getPolarsResourceBundle().getString("maxsea-ok") + fName,
							PolarsResourceBundle.getPolarsResourceBundle().getString("coeff-generation"),
							JOptionPane.INFORMATION_MESSAGE);
				}
			} catch (Exception e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(this,
						PolarsResourceBundle.getPolarsResourceBundle().getString("coeff-error") + "\n" + e.getMessage(),
						PolarsResourceBundle.getPolarsResourceBundle().getString("coeff-generation"),
						JOptionPane.ERROR_MESSAGE);
			}
		} else {
			System.out.println("Load a Data Tree first...");
			JOptionPane.showMessageDialog(this,
					PolarsResourceBundle.getPolarsResourceBundle().getString("no-data-loaded"),
					PolarsResourceBundle.getPolarsResourceBundle().getString("coeff-generation"),
					JOptionPane.WARNING_MESSAGE);
		}
	}
}
