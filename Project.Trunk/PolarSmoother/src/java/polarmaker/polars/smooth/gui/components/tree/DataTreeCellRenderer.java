package polarmaker.polars.smooth.gui.components.tree;

import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.Component;

public class DataTreeCellRenderer
		extends DefaultTreeCellRenderer {
	public DataTreeCellRenderer() {
		super();
	}

	public Component getTreeCellRendererComponent(JTree tree,
	                                              Object value,
	                                              boolean sel,
	                                              boolean expanded,
	                                              boolean leaf,
	                                              int row,
	                                              boolean hasFocus) {
		super.getTreeCellRendererComponent(tree,
				value,
				sel,
				expanded,
				leaf,
				row,
				hasFocus);
		if (value instanceof PolarTreeNode) {
			PolarTreeNode ptn = (PolarTreeNode) value;
			switch (ptn.getType()) {
				case PolarTreeNode.ROOT_TYPE:
					setIcon(new ImageIcon(this.getClass().getResource("paperboat.png")));
					setToolTipText("All the data...");
					break;
				case PolarTreeNode.SECTION_TYPE:
					setIcon(new ImageIcon(this.getClass().getResource("label.png")));
					setToolTipText("Polar Section");
					break;
				case PolarTreeNode.TWS_TYPE:
					setIcon(new ImageIcon(this.getClass().getResource("bullet_ball_glass_red.png")));
					setToolTipText("True Wind Speed");
					break;
				case PolarTreeNode.TWA_TYPE:
					setIcon(new ImageIcon(this.getClass().getResource("bullet_ball_glass_blue.png")));
					setToolTipText("True Wind Angle & Boat SPeed");
					break;
				default:
					break;
			}
		}
		return this;
	}
}
