package polarmaker.polars.smooth.gui.components.polars;

import polarmaker.polars.smooth.gui.components.tree.PolarTreeNode;

import java.awt.Graphics;

public interface CurvePanelInterface {
	boolean getMouseDraggedEnabled();

	boolean getPlotBulkData();

	void plotBulkTws(Graphics g, PolarTreeNode ptn);

	void setCoeff(double[][] da);

	public void resetCoeffDeg();

	public void addCoeffDeg(String sectionName, double[][] daa);

	void setMouseDraggedEnabled(boolean b);

	void setPlotBulkData(boolean b);

	void setSelectedNode(PolarTreeNode[] ptn);

	void setSelectedNodeUp(PolarTreeNode[] ptn);
}
