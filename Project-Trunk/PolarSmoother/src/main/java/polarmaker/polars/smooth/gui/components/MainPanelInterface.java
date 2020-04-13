package polarmaker.polars.smooth.gui.components;

import polarmaker.polars.smooth.gui.components.tree.PolarTreeNode;

import javax.swing.JTree;

public interface MainPanelInterface {
	public void setSelectedNode(PolarTreeNode[] ptn);

	public void setSelectedNodeUp(PolarTreeNode[] ptn);

	public Object getTreeRoot();

	public JTree getJTree();
}
