package polarmaker.polars.smooth.gui.components.tree;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * In case something special is to be done
 * at the top definition of the node
 */
public class DefaultDataTreeNode
		extends DefaultMutableTreeNode {
	public DefaultDataTreeNode() {
		super();
	}

	public DefaultDataTreeNode(String s) {
		super(s);
	}

	public void removeFromParent() {
		super.removeFromParent();
	}
}
