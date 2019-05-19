package polarmaker.polars.smooth.gui.components;

import polarmaker.Constants;
import polarmaker.polars.main.PolarSmoother;
import polarmaker.polars.smooth.gui.components.tree.JTreeUtil;
import polarmaker.polars.smooth.gui.components.tree.PolarTreeNode;
import polarmaker.polars.smooth.gui.components.widgets.EditRootPanel;
import polarmaker.polars.smooth.gui.components.widgets.EditSectionPanel;
import polarmaker.polars.smooth.gui.components.widgets.EditTWAPanel;
import polarmaker.polars.smooth.gui.components.widgets.TWSPanel;
import polarmaker.polars.util.sort.ObjectToSort;
import polarmaker.polars.util.sort.QSortAlgorithm;
import polarmaker.smooth.PolarsResourceBundle;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.ToolTipManager;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Enumeration;
import java.util.Vector;

//import javax.swing.tree.TreeModel;

public class TreePanel
		extends JPanel {
	private BorderLayout borderLayout1 = new BorderLayout();
	private JScrollPane scrollPane = null;
	private final transient TreeSelectionListener treeMonitor = new TreeMonitor();
//private final TreeModel emptyTreeModel = new DefaultTreeModel(new PolarTreeNode("Polars"));

	private transient Object root = null;

	private PolarTreeNode[] currentlySelectedNode = null;
	private JTree dataTree = new JTree();

	private String dataFile = null;
	private transient MainPanelInterface caller = null;

	private TreePopup treePopup = new TreePopup(this);

	public TreePanel(MainPanelInterface mpi) {
		caller = mpi;
		try {
			jbInit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void setDataFile(String s) {
		dataFile = s;
		boolean ok = true;
		try {
			PolarSmoother.validatePolarDocument(s);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, "Invalid Input Document Format:\n" + e.getMessage(), "Schema Validation", JOptionPane.ERROR_MESSAGE);
			ok = false;
		}
		if (ok) {
			root = null; // Reset!
			resetDataTree();
			((DefaultTreeModel) dataTree.getModel()).reload((DefaultMutableTreeNode) root);
			caller.setSelectedNode(null);
			sortTree((PolarTreeNode) root);
		}
	}

	public void extrapolate(double d) {
		if (root != null) {
			PolarTreeNode ptn = (PolarTreeNode) root;
			loopAndExtraolate(ptn, d);
		}
	}

	private void loopAndExtraolate(PolarTreeNode node, double d) {
		int c = node.getChildCount();
		if (c == 0) { // Leaf
			node.setBsp(d * node.getBsp());
		} else {
			if (node.getType() == PolarTreeNode.TWS_TYPE) {
				node.setDownwindSpeed(node.getDownwindSpeed() * d);
				node.setUpwindSpeed(node.getUpwindSpeed() * d);
				node.setDownwindVmg(node.getDownwindVmg() * d);
				node.setUpwindVmg(node.getUpwindVmg() * d);
			}
			for (int i = 0; i < c; i++) {
				loopAndExtraolate((PolarTreeNode) node.getChildAt(i), d);
			}
		}
	}

	private void resetDataTree() {
		// Build the tree after the XML File if found
		root = JTreeUtil.buildTree((PolarTreeNode) root, dataFile);
		if (root != null) {
			if ("true".equals(System.getProperty("verbose", "false"))) {
				System.out.println("Appending the tree for " + dataFile);
			}
			dataTree.setModel(new DefaultTreeModel((PolarTreeNode) root));
		}
	}

	private void jbInit() throws Exception {
		this.setLayout(borderLayout1);
		this.setMinimumSize(new Dimension(100, 100));
//  Dimension d = new Dimension(100, 100);
		if (dataTree != null) {
			dataTree.addTreeSelectionListener(treeMonitor);
			dataTree.addMouseListener(new MouseListener() {
				public void mouseClicked(MouseEvent e) {
					boolean right = ((e.getModifiers() & InputEvent.BUTTON3_MASK) == InputEvent.BUTTON3_MASK);
					if (dataTree.getPathForLocation(e.getX(), e.getY()) != null) {
						// fireDefaultAction(e);
						PolarTreeNode ptn = (PolarTreeNode) dataTree.getLastSelectedPathComponent();
						if (ptn != null && ptn.getType() == PolarTreeNode.ROOT_TYPE) {
							if (right) {
								treePopup.show(dataTree, e.getX(), e.getY(), ptn);
							}
						} else if (ptn != null && ptn.getType() == PolarTreeNode.TWS_TYPE) {
							if (right) {
								treePopup.show(dataTree, e.getX(), e.getY(), ptn);
							}
						} else if (ptn != null && ptn.getType() == PolarTreeNode.TWA_TYPE) {
							if (right) {
								treePopup.show(dataTree, e.getX(), e.getY(), ptn);
							}
						} else if (ptn != null && ptn.getType() == PolarTreeNode.SECTION_TYPE) {
							if (right) {
//                System.out.println("Right");
								treePopup.show(dataTree, e.getX(), e.getY(), ptn);
							}
//              else
//                System.out.println("Left");
						}
					} else {
						if ("true".equals(System.getProperty("verbose", "false")))
							System.out.println("MouseClicked");
					}
				}

				public void mousePressed(MouseEvent e) {
					tryPopup(e);
				}

				public void mouseReleased(MouseEvent e) {
					if (e.getClickCount() == 2) {
						dblClicked(e);
					} else {
//            System.out.println("Mouse Released");
						tryPopup(e);
					}
				}

				public void mouseEntered(MouseEvent e) {
				}

				public void mouseExited(MouseEvent e) {
				}

				private void dblClicked(MouseEvent e) {
					System.out.println("Double-Click");
					if (e.isConsumed()) {
						return;
					}
					// Let's make sure we only invoke double click action when
					// we have a treepath. For example; This avoids opening an editor on a
					// selected node when the user double clicks on the expand/collapse icon.
					if (e.getClickCount() == 2) {
						if (dataTree.getPathForLocation(e.getX(), e.getY()) != null) {
							// fireDefaultAction(e);
							PolarTreeNode ptn = (PolarTreeNode) dataTree.getLastSelectedPathComponent();
							if (ptn != null && ptn.getType() == PolarTreeNode.ROOT_TYPE) {
								System.out.println("DoubleClick on Root");
							} else if (ptn != null && ptn.getType() == PolarTreeNode.TWS_TYPE) {
								System.out.println("DoubleClick on TWS " + ptn.getTws());
							} else if (ptn != null && ptn.getType() == PolarTreeNode.TWA_TYPE) {
								System.out.println("DoubleClick on TWA");
								// Edit node
								editTWA(ptn);
								sortTree((PolarTreeNode) ptn.getParent());
							} else if (ptn != null && ptn.getType() == PolarTreeNode.SECTION_TYPE) {
								System.out.println("Double Click on section " + ptn.toString());
							}
						}
					} else if (e.getClickCount() > 2) {
						// Fix triple-click wanna-be drag events...
						e.consume();
					}
				}

				private void tryPopup(MouseEvent e) {
					if (e.isPopupTrigger()) {
						TreePath current = dataTree.getPathForLocation(e.getX(), e.getY());
						if (current == null) {
							return;
						}
						TreePath[] paths = dataTree.getSelectionPaths();
						boolean isSelected = false;
						if (paths != null) {
							for (int i = 0; i < paths.length; i++) {
								if (paths[i] == current) {
									isSelected = true;
									break;
								}
							}
						}
						if (!isSelected) {
							dataTree.setSelectionPath(current);
						}
					}
				}
			});
			dataTree.setCellRenderer(new polarmaker.polars.smooth.gui.components.tree.DataTreeCellRenderer());
			// Enable Tooltips
			ToolTipManager.sharedInstance().registerComponent(dataTree);
			if (scrollPane == null) {
				scrollPane = new JScrollPane();
				scrollPane.getViewport().add(dataTree, null);
				this.add(scrollPane, BorderLayout.CENTER);
			}
			dataTree.setModel(new DefaultTreeModel(null)); //Nothing by default
		}
	}

	public Object getRoot() {
		return root;
	}

	public JTree getJTree() {
		return dataTree;
	}

	public PolarTreeNode[] getSelectedNode() {
		return currentlySelectedNode;
	}

	public void paintComponent(Graphics g) {
		((Graphics2D) g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		try {
			this.jbInit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	class TreeMonitor implements TreeSelectionListener {
		JTextField feedback = null;

		public TreeMonitor() {
			this(null);
		}

		public TreeMonitor(JTextField fld) {
			feedback = fld;
		}

		public void valueChanged(TreeSelectionEvent tse) {
			TreePath[] tp = dataTree.getSelectionPaths();
			currentlySelectedNode = null;
			if (tp == null) {
				return;
			}
			currentlySelectedNode = new PolarTreeNode[tp.length];
			for (int i = 0; i < tp.length; i++) {
				PolarTreeNode toSelect = null;
				PolarTreeNode selected = (PolarTreeNode) tp[i].getLastPathComponent();
				if (selected.getType() == PolarTreeNode.TWA_TYPE) { // Take the TWS
					toSelect = (PolarTreeNode) selected.getParent();
				} else {
					toSelect = selected;
				}
				currentlySelectedNode[i] = toSelect;
			}
			caller.setSelectedNode(currentlySelectedNode);
		}
	}

	protected void editTWA(PolarTreeNode ptn) {
		EditTWAPanel etp = new EditTWAPanel();
		etp.setBsp(ptn.getBsp());
		etp.setTwa(ptn.getTwa());
		int resp = JOptionPane.showConfirmDialog(this,
				etp, PolarsResourceBundle.getPolarsResourceBundle().getString("edit-twa"),
				JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.QUESTION_MESSAGE);
		if (resp == JOptionPane.OK_OPTION) {
			try {
				ptn.setBsp(etp.getBsp());
				ptn.setTwa(etp.getTwa());
			} catch (Exception ex) {
				JOptionPane.showMessageDialog(this, PolarsResourceBundle.getPolarsResourceBundle().getString("bad-value") + ex.getMessage(), PolarsResourceBundle.getPolarsResourceBundle().getString("twa-node"),
						JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	protected void sortTree(PolarTreeNode ptn) {
		if (ptn == null) {
			return;
		}
		QSortAlgorithm qsa = new QSortAlgorithm();
		Enumeration children = ptn.children();
		Vector<NodeObject> v = new Vector<NodeObject>();
		while (children.hasMoreElements()) {
			PolarTreeNode n = (PolarTreeNode) children.nextElement();
			v.addElement(new NodeObject(n));
		}
		NodeObject[] nodeArray = new NodeObject[v.size()];
		Enumeration enum2 = v.elements();
		int rnk = 0;
		while (enum2.hasMoreElements()) {
			NodeObject no = (NodeObject) enum2.nextElement();
			nodeArray[rnk++] = no;
			((PolarTreeNode) no.getSortedObject()).removeFromParent();
		}
		try {
			qsa.sort(nodeArray);
		} catch (Exception sortException) {
			JOptionPane.showMessageDialog(this,
					sortException.toString(),
					PolarsResourceBundle.getPolarsResourceBundle().getString("sort"),
					JOptionPane.ERROR_MESSAGE);
		}
		for (int i = 0; i < nodeArray.length; i++) {
			PolarTreeNode sorted = (PolarTreeNode) nodeArray[i].getSortedObject();
			ptn.add(sorted);
		}
		// refresh the tree
		((DefaultTreeModel) this.dataTree.getModel()).reload(ptn);
	}

	/**
	 * This is the popup menu for the JTree
	 * Is is contextual.
	 */
	class TreePopup extends JPopupMenu
			implements ActionListener,
			PopupMenuListener {
		TreePanel parent;

		JMenuItem edit;
		JMenuItem insert;
		JMenuItem createChild;
		JMenuItem delete;
		JMenuItem duplicate;

		private final String EDIT = PolarsResourceBundle.getPolarsResourceBundle().getString("edit-node");
		private String INSERT = "Insert";
		private String CHILD = "Create Child Node";
		private final String DELETE = PolarsResourceBundle.getPolarsResourceBundle().getString("delete-node");
		private final String DUPLICATE = "Duplicate this node";

		PolarTreeNode ptn = null;
		int _x = 0, _y = 0;

		public TreePopup(TreePanel c) {
			super();
			this.parent = c;
			this.add(edit = new JMenuItem(EDIT));
			edit.addActionListener(this);
			this.add(insert = new JMenuItem(INSERT));
			insert.addActionListener(this);
			this.add(createChild = new JMenuItem(CHILD));
			createChild.addActionListener(this);
			this.add(delete = new JMenuItem(DELETE));
			delete.addActionListener(this);
			this.add(duplicate = new JMenuItem(DUPLICATE));
			duplicate.addActionListener(this);
		}

		public void actionPerformed(ActionEvent event) {
			if (event.getActionCommand().equals(EDIT)) {
				PolarTreeNode parentNode = (PolarTreeNode) ptn.getParent();
				if (ptn.getType() == PolarTreeNode.ROOT_TYPE) {
					EditRootPanel erp = new EditRootPanel();
					erp.setName(ptn.getModel());
					int resp = JOptionPane.showConfirmDialog(this,
							erp,
							PolarsResourceBundle.getPolarsResourceBundle().getString("edit-root"),
							JOptionPane.OK_CANCEL_OPTION,
							JOptionPane.QUESTION_MESSAGE);
					if (resp == JOptionPane.OK_OPTION) {
						ptn.setModel(erp.getName());
					}
				} else if (ptn.getType() == PolarTreeNode.SECTION_TYPE) {
					EditSectionPanel esp = new EditSectionPanel();
					esp.setName(ptn.getModel());
					esp.setPolarDegree(ptn.getPolarDegree());
					esp.setCoeffDegree(ptn.getCoeffDegree());
					esp.setFromTWA(ptn.getFromTwa());
					esp.setToTWA(ptn.getToTwa());
					int resp = JOptionPane.showConfirmDialog(this,
							esp,
							"Edit Section",
							JOptionPane.OK_CANCEL_OPTION,
							JOptionPane.QUESTION_MESSAGE);
					if (resp == JOptionPane.OK_OPTION) {
						ptn.setModel(esp.getName()); // TODO Make sure the name is unique.
						ptn.setPolarDegree(esp.getPolarDegree());
						ptn.setCoeffDegree(esp.getCoeffDegree());
						ptn.setFromTwa(esp.getFromTWA());
						ptn.setToTwa(esp.getToTWA());
					}
				} else if (ptn.getType() == PolarTreeNode.TWS_TYPE) {
					TWSPanel etp = new TWSPanel();
					etp.setTWS(ptn.getTws());
					etp.setBspVmgUp(ptn.getUpwindSpeed());
					etp.setTwaVmgUp(ptn.getUpwindTwa());
					etp.setVmgUp(ptn.getUpwindVmg());
					etp.setBspVmgDown(ptn.getDownwindSpeed());
					etp.setTwaVmgDown(ptn.getDownwindTwa());
					etp.setVmgDown(ptn.getDownwindVmg());
					// Data
					Object[][] data = new Object[ptn.getChildCount()][2];
					for (int i = 0; i < ptn.getChildCount(); i++) {
						data[i][0] = new Integer(((PolarTreeNode) ptn.getChildAt(i)).getTwa());
						data[i][1] = new Double(((PolarTreeNode) ptn.getChildAt(i)).getBsp());
					}
					etp.setTwaData(data);
					int resp = JOptionPane.showConfirmDialog(this,
							etp, PolarsResourceBundle.getPolarsResourceBundle().getString("edit-tws"),
							JOptionPane.OK_CANCEL_OPTION,
							JOptionPane.QUESTION_MESSAGE);
					if (resp == JOptionPane.OK_OPTION) {
						try {
							// Then create a TWS and related TWAs
							double bspvmgu = Double.MIN_VALUE;
							try {
								bspvmgu = etp.getBspVmgUp();
							} catch (Exception ignore) {
							}
							double twavmgu = Double.MIN_VALUE;
							try {
								twavmgu = etp.getTwaVmgUp();
							} catch (Exception ignore) {
							}
							double vmgu = Double.MIN_VALUE;
							try {
								vmgu = etp.getVmgUp();
							} catch (Exception ignore) {
							}
							double bspvmgd = Double.MIN_VALUE;
							try {
								bspvmgd = etp.getBspVmgDown();
							} catch (Exception ignore) {
							}
							double twavmgd = Double.MIN_VALUE;
							try {
								twavmgd = etp.getTwaVmgDown();
							} catch (Exception ignore) {
							}
							double vmgd = Double.MIN_VALUE;
							try {
								vmgd = etp.getVmgDown();
							} catch (Exception ignore) {
							}

							PolarTreeNode newNode = new PolarTreeNode(etp.getTWS(),
									bspvmgu,
									twavmgu,
									vmgu,
									bspvmgd,
									twavmgd,
									vmgd);
							Object[][] tableData = etp.getTwaData();
							for (int i = 0; i < tableData.length; i++)
								newNode.add(new PolarTreeNode(((Integer) tableData[i][0]).intValue(),
										((Double) tableData[i][1]).doubleValue()));
							// ptn = newNode;
							parentNode.remove(ptn);
							parentNode.add(newNode);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				} else if (ptn.getType() == PolarTreeNode.TWA_TYPE) {
					editTWA(ptn);
				}
				sortTree(parentNode);
			} else if (event.getActionCommand().equals(INSERT)) {
				int idx = ptn.getParent().getIndex(ptn);
				System.out.println("Inserting after:" + idx);
				PolarTreeNode newNode = null;
				if (ptn.getType() == PolarTreeNode.TWS_TYPE) {
					newNode = createTWS();
				}
				if (ptn.getType() == PolarTreeNode.TWA_TYPE) {
					// Then create a TWA
					newNode = new PolarTreeNode(0, 0.0);
				}
				if (newNode != null) {
					// Add in position idx + 1
					((DefaultTreeModel) parent.dataTree.getModel()).insertNodeInto((DefaultMutableTreeNode) newNode, (DefaultMutableTreeNode) ptn.getParent(), (idx + 1));
					sortTree((PolarTreeNode) ptn.getParent());
				}
			} else if (event.getActionCommand().equals(CHILD)) {
				PolarTreeNode newNode = null;
				if (ptn.getType() == PolarTreeNode.ROOT_TYPE) {
					newNode = createSection();
				}
				if (ptn.getType() == PolarTreeNode.SECTION_TYPE) {
					newNode = createTWS();
				}
				if (ptn.getType() == PolarTreeNode.TWS_TYPE) {
					// Then create a TWA
					newNode = new PolarTreeNode(0, 0.0);
				}
				if (newNode != null) {
					ptn.add(newNode);
					sortTree(ptn);
				}
			} else if (event.getActionCommand().equals(DELETE)) {
				((DefaultTreeModel) parent.dataTree.getModel()).removeNodeFromParent(ptn);
				((DefaultTreeModel) parent.dataTree.getModel()).reload(ptn);
			} else if (event.getActionCommand().equals(DUPLICATE)) {
				int existing = ((PolarTreeNode) ptn.getParent()).getChildCount();
				PolarTreeNode newNode = new PolarTreeNode(Integer.toString(existing + 1) + " - Duplicated", ptn.getPolarDegree(), ptn.getCoeffDegree(), ptn.getFromTwa(), ptn.getToTwa());
				newNode = duplicate(ptn, newNode);

				((PolarTreeNode) ptn.getParent()).add(newNode);
				sortTree((PolarTreeNode) ptn.getParent());
				((DefaultTreeModel) parent.dataTree.getModel()).reload(ptn.getParent());
			}
		}

		private PolarTreeNode duplicate(PolarTreeNode origin, PolarTreeNode into) {
			Enumeration children = origin.children();
			while (children.hasMoreElements()) {
				PolarTreeNode child = (PolarTreeNode) children.nextElement();
				PolarTreeNode clone = (PolarTreeNode) child.clone();
				clone = duplicate(child, clone);
				into.add(clone);
			}
			return into;
		}

		private PolarTreeNode createSection() {
			PolarTreeNode newNode = null;
			EditSectionPanel esp = new EditSectionPanel();
			esp.setCoeffDegree(Constants.DEFAULT_COEFF_DEGREE);
			esp.setPolarDegree(Constants.DEFAULT_POLAR_DEGREE);
			esp.setFromTWA(0);
			esp.setToTWA(180);
			esp.setName("New Section");
			int resp = JOptionPane.showConfirmDialog(this,
					esp,
					PolarsResourceBundle.getPolarsResourceBundle().getString("create-section"),
					JOptionPane.OK_CANCEL_OPTION,
					JOptionPane.PLAIN_MESSAGE);
			if (resp == JOptionPane.OK_OPTION) {
				try {

					newNode = new PolarTreeNode(esp.getName(),
							esp.getPolarDegree(),
							esp.getCoeffDegree(),
							esp.getFromTWA(),
							esp.getToTWA());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			return newNode;
		}

		private PolarTreeNode createTWS() {
			PolarTreeNode newNode = null;
			TWSPanel etp = new TWSPanel();
			etp.setTWSTopLabel(PolarsResourceBundle.getPolarsResourceBundle().getString("create-tws-top-text"));
			int resp = JOptionPane.showConfirmDialog(this,
					etp,
					PolarsResourceBundle.getPolarsResourceBundle().getString("create-tws"),
					JOptionPane.OK_CANCEL_OPTION,
					JOptionPane.PLAIN_MESSAGE);
			if (resp == JOptionPane.OK_OPTION) {
				try {
					double bspvmgu = Double.MIN_VALUE;
					try {
						bspvmgu = etp.getBspVmgUp();
					} catch (Exception ignore) {
					}
					double twavmgu = Double.MIN_VALUE;
					try {
						twavmgu = etp.getTwaVmgUp();
					} catch (Exception ignore) {
					}
					double vmgu = Double.MIN_VALUE;
					try {
						vmgu = etp.getVmgUp();
					} catch (Exception ignore) {
					}
					double bspvmgd = Double.MIN_VALUE;
					try {
						bspvmgd = etp.getBspVmgDown();
					} catch (Exception ignore) {
					}
					double twavmgd = Double.MIN_VALUE;
					try {
						twavmgd = etp.getTwaVmgDown();
					} catch (Exception ignore) {
					}
					double vmgd = Double.MIN_VALUE;
					try {
						vmgd = etp.getVmgDown();
					} catch (Exception ignore) {
					}

					newNode = new PolarTreeNode(etp.getTWS(),
							bspvmgu,
							twavmgu,
							vmgu,
							bspvmgd,
							twavmgd,
							vmgd);
					Object[][] tableData = etp.getTwaData();
					for (int i = 0; i < tableData.length; i++)
						newNode.add(new PolarTreeNode(((Integer) tableData[i][0]).intValue(),
								((Double) tableData[i][1]).doubleValue()));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			return newNode;
		}

		public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
		}

		public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
		}

		public void popupMenuCanceled(PopupMenuEvent e) {
		}

		public void show(Component c, int x, int y, PolarTreeNode ptn) {
			super.show(c, x, y);
			this.ptn = ptn;
			if (ptn.getType() == PolarTreeNode.ROOT_TYPE) {
				edit.setVisible(true);
				insert.setVisible(false);
				CHILD = PolarsResourceBundle.getPolarsResourceBundle().getString("create-section");
				createChild.setText(CHILD);
				createChild.setVisible(true);
				delete.setVisible(false);
				duplicate.setVisible(false);
			} else if (ptn.getType() == PolarTreeNode.SECTION_TYPE) {
				edit.setVisible(true);
				insert.setVisible(false);
				CHILD = PolarsResourceBundle.getPolarsResourceBundle().getString("create-tws");
				createChild.setText(CHILD);
				createChild.setVisible(true);
				delete.setVisible(false);
				duplicate.setVisible(true);
			} else if (ptn.getType() == PolarTreeNode.TWS_TYPE) {
				edit.setVisible(true);
				INSERT = PolarsResourceBundle.getPolarsResourceBundle().getString("create-tws");
				insert.setText(INSERT);
				insert.setVisible(true);
				CHILD = PolarsResourceBundle.getPolarsResourceBundle().getString("create-twa");
				createChild.setText(CHILD);
				createChild.setVisible(true);
				delete.setVisible(true);
				duplicate.setVisible(false);
			} else if (ptn.getType() == PolarTreeNode.TWA_TYPE) {
				edit.setVisible(true);
				INSERT = PolarsResourceBundle.getPolarsResourceBundle().getString("create-twa");
				insert.setText(INSERT);
				insert.setVisible(true);
				createChild.setVisible(false);
				delete.setVisible(true);
				duplicate.setVisible(false);
			}
			this.pack();
			_x = x;
			_y = y;
		}
	}

	class NodeObject extends ObjectToSort {
		PolarTreeNode body;

		public NodeObject(Object o) {
			super(o);
			if (o instanceof PolarTreeNode)
				body = (PolarTreeNode) o;
		}

		public double getValue() {
			double val = -Double.MAX_VALUE;
			if (body.getType() == PolarTreeNode.TWS_TYPE) {
				val = body.getTws();
			} else if (body.getType() == PolarTreeNode.TWA_TYPE) {
				val = body.getTwa();
			}
			return val;
		}

		public Object getSortedObject() {
			return body;
		}
	}
}
