package polarmaker.polars.smooth.gui.components.polars;

import polarmaker.polars.smooth.gui.components.MainPanelInterface;
import polarmaker.polars.smooth.gui.components.tree.PolarTreeNode;
import polarmaker.smooth.PolarsResourceBundle;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTree;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;

public class CurveTabPanel
		extends JPanel
		implements CurvePanelInterface,
		MainPanelInterface {
	private CartesianPanel cartesianPanel = null;
	private PolarPanel polarPanel = null;

	private BorderLayout borderLayout1 = new BorderLayout();
	private JTabbedPane jTabbedPane1 = new JTabbedPane();

	private Color bgColor = Color.black;
	private Color gridColor = Color.green;
	private Color lineColor1 = Color.green;
	private Color lineColor2 = Color.red;

	private transient MainPanelInterface parent = null;
	private boolean mouseDraggedEnabled = true;
	private boolean plotBulkData = true;

	private PolarTreeNode[] selectedNode = null;
	private double[][] coeffDeg = null;
	/* Next one is an array, in case
	 * there are several curves to display
	 * (multiple select in the JTree) */
	private double[][] coeff = null;

	public CurveTabPanel(MainPanelInterface mpi) {
		this(mpi, null, null, null, null);
	}

	public CurveTabPanel(MainPanelInterface mpi,
	                     Color c1,
	                     Color c2,
	                     Color c3,
	                     Color c4) {
		parent = mpi;
		if (c1 != null) bgColor = c1;
		if (c2 != null) gridColor = c2;
		if (c3 != null) lineColor1 = c3;
		if (c4 != null) lineColor2 = c4;
		try {
			jbInit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public MainPanelInterface getParentPanel() {
		return parent;
	}

	private void jbInit() throws Exception {
		cartesianPanel = new CartesianPanel(this, bgColor, gridColor, lineColor1, lineColor2);
		polarPanel = new PolarPanel(this, bgColor, gridColor, lineColor1, lineColor2);
		this.setLayout(borderLayout1);
		this.add(jTabbedPane1, BorderLayout.CENTER);
		jTabbedPane1.setTabPlacement(JTabbedPane.BOTTOM);
		jTabbedPane1.add(PolarsResourceBundle.getPolarsResourceBundle().getString("polar"), polarPanel);
		jTabbedPane1.add(PolarsResourceBundle.getPolarsResourceBundle().getString("cartesian"), cartesianPanel);
	}

	public boolean getMouseDraggedEnabled() {
		return mouseDraggedEnabled;
	}

	public boolean getPlotBulkData() {
		return plotBulkData;
	}

	public void plotBulkTws(Graphics g, PolarTreeNode ptn) {
		polarPanel.plotBulkTws(g, ptn);
		cartesianPanel.plotBulkTws(g, ptn);
	}

	public void setCoeff(double[][] da) {
		polarPanel.setCoeff(da);
		cartesianPanel.setCoeff(da);
	}

	public void resetCoeffDeg() {
		polarPanel.resetCoeffDeg();
		cartesianPanel.resetCoeffDeg();
	}

	public void addCoeffDeg(String sectionName, double[][] daa) {
		polarPanel.addCoeffDeg(sectionName, daa);
		cartesianPanel.addCoeffDeg(sectionName, daa);
	}

	public void setMouseDraggedEnabled(boolean b) {
		polarPanel.setMouseDraggedEnabled(b);
		cartesianPanel.setMouseDraggedEnabled(b);
	}

	public void setPlotBulkData(boolean b) {
		polarPanel.setPlotBulkData(b);
		cartesianPanel.setPlotBulkData(b);
	}

	public void setSelectedNode(PolarTreeNode[] ptn) {
		polarPanel.setSelectedNode(ptn);
		cartesianPanel.setSelectedNode(ptn);
	}

	public void setSelectedNodeUp(PolarTreeNode[] ptn) {
		parent.setSelectedNode(ptn);
	}

	public Object getTreeRoot() {
		return parent.getTreeRoot();
	}

	public JTree getJTree() {
		return parent.getJTree();
	}
}
