package polarmaker.polars.smooth.gui.components;

import polarmaker.Constants;
import polarmaker.polars.PolarPoint;
import polarmaker.polars.main.PolarSmoother;
import polarmaker.polars.smooth.gui.components.dotmatrix.ThreeDPanel;
import polarmaker.polars.smooth.gui.components.dotmatrix.ThreeDPoint;
import polarmaker.polars.smooth.gui.components.polars.CoeffForPolars;
import polarmaker.polars.smooth.gui.components.polars.CurveTabPanel;
import polarmaker.polars.smooth.gui.components.tree.PolarTreeNode;
import polarmaker.polars.util.DataComputer;
import polarmaker.smooth.PolarsResourceBundle;
import wireframe.wiremaker.ObjMaker;

import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTree;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.io.File;
import java.io.FileWriter;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class MainPanel
		extends JPanel
		implements MainPanelInterface {
	private JSplitPane jSplitPane1 = new JSplitPane();
	private TreePanel treePanel = new TreePanel(this);
	private BorderLayout borderLayout1 = new BorderLayout();

	private PolarTreeNode[] selectedNode = null;

	private CurveTabPanel bulkPanel = new CurveTabPanel(this, Color.black, Color.green, Color.red, Color.yellow);
	private CurveTabPanel smoothPanel = new CurveTabPanel(this, Color.white, Color.gray, Color.red, Color.blue);
	private ThreeDPanel threeDPanel = new ThreeDPanel(Constants.OBJ_FILE_NAME, Color.black, Color.green, null, null);

	private JTabbedPane tabbedPane = new JTabbedPane();

	private static Map<String, CoeffForPolars> coeffMap = null;

	public MainPanel() {
		try {
			jbInit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void jbInit() throws Exception {
		this.setLayout(borderLayout1);
//  treePanel.setDataFile("src\\Sydney32PolarData.xml");
		jSplitPane1.setLeftComponent(treePanel);
		tabbedPane.add(PolarsResourceBundle.getPolarsResourceBundle().getString("bulk"), bulkPanel);
		tabbedPane.add(PolarsResourceBundle.getPolarsResourceBundle().getString("smoothed"), smoothPanel);
		tabbedPane.add(PolarsResourceBundle.getPolarsResourceBundle().getString("threed"), threeDPanel);
		jSplitPane1.setRightComponent(tabbedPane);
		jSplitPane1.setDividerLocation(125);
		jSplitPane1.setResizeWeight(0.35D);
		this.add(jSplitPane1, BorderLayout.CENTER);
//  treePanel.repaint();

		bulkPanel.setMouseDraggedEnabled(true);
		smoothPanel.setMouseDraggedEnabled(true);
	}

	public void setPlotBulkOnSmoothPanel(boolean b) {
		if (smoothPanel != null)
			smoothPanel.setPlotBulkData(b);
	}

	public void setDataFile(String s) {
		treePanel.setDataFile(s);
		treePanel.repaint();
		bulkPanel.repaint();
		smoothPanel.repaint();
	}

	public void extrapolateSpeed(double factor) {
		treePanel.extrapolate(factor);
		treePanel.repaint();
		bulkPanel.repaint();
		smoothPanel.repaint();
	}

	public static double[][] generateCoefficients(PolarTreeNode startFrom, int polarDegree, int coeffDegree) {
		// Smoothing the whole stuff
		int ii = 0;
		Enumeration enumeration = startFrom.children();
		// Ugly!
		int size = 0;
		while (enumeration.hasMoreElements()) {
			enumeration.nextElement();
			size++;
		}
		// Reset
		enumeration = startFrom.children();

		double[][] bigArray = new double[size][];
		double[] windSpeed = new double[size];
		while (enumeration.hasMoreElements()) {
			PolarTreeNode child = (PolarTreeNode) enumeration.nextElement();
//    System.out.println("Child Type=" + child.getType());
			if (child.getType() == PolarTreeNode.TWS_TYPE) {
				double[] coeff = smoothOneCurve(child, polarDegree);
				if ("true".equals(System.getProperty("verbose", "false"))) {
					// One curve smooting
					System.out.println("-- Smoothing for TWS:" + child.getTws() + " --");
					System.out.println("- Original points:");
					Enumeration twa = child.children();
					while (twa.hasMoreElements()) {
						PolarTreeNode twaNode = (PolarTreeNode) twa.nextElement();
						System.out.println("TWA:" + twaNode.getTwa() + ", STW:" + twaNode.getBsp());
					}
					System.out.println("-----------------------------------");
					System.out.println("- Calculated:");
					twa = child.children();
					while (twa.hasMoreElements()) {
						PolarTreeNode twaNode = (PolarTreeNode) twa.nextElement();
						double bsp = PolarSmoother.f(twaNode.getTwa(), coeff);
						if (bsp < 0) bsp = 0;
						System.out.println(" - TWA:" + twaNode.getTwa() + ", STW:" + bsp);
					}
					// End of verbose crap
				}
				bigArray[ii] = coeff;
				windSpeed[ii] = child.getTws();
//      System.out.println("Coeff for TWS:" + windSpeed[ii]);
//      for (int i=0; i<coeff.length; i++)
//        System.out.println("i=" + i + ":" + coeff[i]);
//      System.out.println("-------------------");
				ii++;
			} else {
				System.out.println("Invalid children under Root, type " + child.getType());
				return (double[][]) null;
			}
		}
		// Smooth coefficients
		double[][] coeffDeg = new double[polarDegree + 1][];
		for (int k = 0; k < (polarDegree + 1); k++) {
			Vector<PolarPoint> v = new Vector<>();
			for (int j = 0; j < bigArray.length; j++) { // all the tws
				v.add(new PolarPoint(bigArray[j][k], windSpeed[j])); // Inverted!!
			}
			coeffDeg[k] = DataComputer.smooth(v, coeffDegree);
		}
		return coeffDeg;
	}

	public void setSelectedNode(PolarTreeNode[] ptn) {
		// Go!
		selectedNode = ptn;
		bulkPanel.setSelectedNode(ptn);
		smoothPanel.setSelectedNode(ptn);
		// Smooth data
		smoothPanel.resetCoeffDeg();
		if (selectedNode != null) {
			coeffMap = null;
//    System.out.println("Smoothing Data");
			double[][] coeffArray = new double[selectedNode.length][];
			for (int i = 0; i < selectedNode.length; i++) {
				if (selectedNode[i].getType() == PolarTreeNode.ROOT_TYPE) {
					PolarTreeNode[] sectionNodes = new PolarTreeNode[selectedNode[i].getChildCount()];
					Enumeration children = selectedNode[i].children();
					int idx = 0;
					while (children.hasMoreElements()) {
						sectionNodes[idx++] = (PolarTreeNode) children.nextElement();
					}
					setSelectedNode(sectionNodes);
					// 3D, full view
					if (coeffMap != null) {
						try {
							PolarSmoother.generateXMLforObj("polars",
									new FileWriter(new File("polars.xml")),
									coeffMap);
							// Transform
							ObjMaker.generate("polars.xml");
							// Replace the panel
							threeDPanel = new ThreeDPanel("polars.obj", Color.black, Color.green, null, null);
							tabbedPane.setComponentAt(2, threeDPanel);
							threeDPanel.setPanelLabel("");
							threeDPanel.setDrawingOption(ThreeDPanel.CIRC_OPT);
						} catch (Exception ex) {
							ex.printStackTrace();
						}
					}
				} else if (selectedNode[i].getType() == PolarTreeNode.SECTION_TYPE) {
					final PolarTreeNode theSection = selectedNode[i];
					double[][] coeffDeg = null;
					this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

					coeffDeg = generateCoefficients(theSection, theSection.getPolarDegree(), theSection.getCoeffDegree());
					// Done
					smoothPanel.addCoeffDeg(theSection.getModel(), coeffDeg);
					if (coeffMap == null) {
						coeffMap = new HashMap<>();
					}
					coeffMap.put(theSection.getModel(), new CoeffForPolars(coeffDeg,
							theSection.getPolarDegree(),
							theSection.getFromTwa(),
							theSection.getToTwa()));
					try {
						if (selectedNode.length == 1) {
							PolarSmoother.generateXMLforObj("polars",
									new FileWriter(new File("polars.xml")),
									coeffDeg,
									theSection.getPolarDegree(),
									theSection.getFromTwa(),
									theSection.getToTwa());
							// Transform
							ObjMaker.generate("polars.xml");
							// Replace the panel
							threeDPanel = new ThreeDPanel("polars.obj", Color.black, Color.green, null, null);
							tabbedPane.setComponentAt(2, threeDPanel);
							//        threeDPanel.setModel("polars.obj");
							threeDPanel.setPanelLabel("Polar Deg. " + theSection.getPolarDegree() +
									", Coeff Deg. " + theSection.getCoeffDegree());
							//          Vector data = buildSpeedDataVector((PolarTreeNode)getTreeRoot());
							Vector data = buildSpeedDataVector(theSection);
							threeDPanel.setSpeedPts(data);
							threeDPanel.setDrawingOption(ThreeDPanel.CIRC_OPT);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
					this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				} else if (selectedNode[i].getType() == PolarTreeNode.TWS_TYPE) {
					double[] coeff = smoothOneCurve(selectedNode[i], ((PolarTreeNode) selectedNode[i].getParent()).getPolarDegree());
					coeffArray[i] = coeff;
				} else if (selectedNode[i].getType() == PolarTreeNode.TWA_TYPE) {
					// Nothing for now...
				}
			}
			smoothPanel.setCoeff(coeffArray);
		} else
			smoothPanel.setCoeff(null);
		bulkPanel.repaint();
		smoothPanel.repaint();
	}

	// builds a Vector of ThreeDPoint
	private Vector buildSpeedDataVector(PolarTreeNode section) {
		Vector<ThreeDPoint> v = new Vector<ThreeDPoint>();
		Enumeration enumeration = section.children();

		while (enumeration.hasMoreElements()) {
			PolarTreeNode twsNode = (PolarTreeNode) enumeration.nextElement();
			if (twsNode.getType() == PolarTreeNode.TWS_TYPE) {
				double tws = twsNode.getTws();
				Enumeration bspEnumeration = twsNode.children();
				while (bspEnumeration.hasMoreElements()) {
					PolarTreeNode twaNode = (PolarTreeNode) bspEnumeration.nextElement();
					double bsp = twaNode.getBsp();
					int twa = twaNode.getTwa();
					float x = (float) tws;
					float y = (float) (bsp * Math.sin(Math.toRadians((double) twa)));
					float z = (float) (bsp * Math.cos(Math.toRadians((double) twa)));
					ThreeDPoint tdp = new ThreeDPoint(x, y, z);
					v.add(tdp);
					tdp = new ThreeDPoint(x, -y, z);
					v.add(tdp);
				}
			} else {
				System.out.println("Invalid children under Section (in MainPanel.buildSpeedDataVector)");
			}
		}
		return v;
	}

	public void setSelectedNodeUp(PolarTreeNode[] ptn) {
	}

	private static double[] smoothOneCurve(PolarTreeNode ptn, int polarDegree) {
		Vector<PolarPoint> pointToSmooth = new Vector<PolarPoint>();
//  System.out.println("Smoothing for TWS:" + ptn.getTws());
		if (ptn.getUpwindSpeed() != Double.MIN_VALUE && ptn.getUpwindTwa() != Double.MIN_VALUE) {
			pointToSmooth.add(new PolarPoint(ptn.getUpwindSpeed(), ptn.getUpwindTwa()));
		}
		if (ptn.getDownwindSpeed() != Double.MIN_VALUE && ptn.getDownwindTwa() != Double.MIN_VALUE) {
			pointToSmooth.add(new PolarPoint(ptn.getDownwindSpeed(), ptn.getDownwindTwa()));
		}

//  System.out.println("adding->" + ptn.getUpwindSpeed() + "/" + ptn.getUpwindTwa());
//  System.out.println("adding->" + ptn.getDownwindSpeed() + "/" + ptn.getDownwindTwa());
		// Walk through the child-nodes
		Enumeration enumeration = ptn.children();
		while (enumeration.hasMoreElements()) {
			PolarTreeNode child = (PolarTreeNode) enumeration.nextElement();
			if (child.getType() == PolarTreeNode.TWA_TYPE) {
				int twa = child.getTwa();
				double bsp = child.getBsp();
				pointToSmooth.add(new PolarPoint(bsp, (double) twa));
//      System.out.println("adding->" + bsp + "/" + twa);
			}
		}
		return DataComputer.smooth(pointToSmooth, polarDegree);
	}

	public Object getTreeRoot() {
		return treePanel.getRoot();
	}

	public JTree getJTree() {
		if (treePanel != null)
			return treePanel.getJTree();
		else
			return null;
	}

	public PolarTreeNode[] getSelectedNode() {
		return selectedNode;
	}
}
