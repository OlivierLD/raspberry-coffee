package polarmaker.polars.smooth.gui.components.polars;

import polarmaker.Constants;
import polarmaker.polars.PolarPoint;
import polarmaker.polars.main.PolarSmoother;
import polarmaker.polars.smooth.gui.components.MainPanelInterface;
import polarmaker.polars.smooth.gui.components.tree.PolarTreeNode;

import javax.swing.JPanel;
import javax.swing.tree.DefaultTreeModel;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class PolarPanel
		extends JPanel
		implements CurvePanelInterface,
		MouseListener,
		MouseMotionListener {
	MainPanelInterface parent;

	private transient List<CoeffForSection> coeffList = null;

	int draggedFromX = -1;
	int draggedFromY = -1;
	PolarTreeNode draggedPolarPoint = null;
	boolean dragged = false;
	private boolean mouseDraggedEnabled = true;
	private boolean plotBulkData = true;

	PolarTreeNode[] selectedNode = null;
	Color bgColor = Color.black;
	Color gridColor = Color.green;
	Color lineColor1 = Color.green;
	Color lineColor2 = Color.red;

	Color[] smoothedPolarColors = new Color[]
			{
					Color.red,
					Color.black,
					Color.blue,
					Color.green,
					Color.orange,
					Color.magenta,
					Color.cyan,
					Color.darkGray,
					Color.pink
			};

	//double[][] coeffDeg = null;
	/* Next one is an array, in case
	 * there are several curves to display
	 * (multiple select in the JTree) */
	double[][] coeff = null;

	public PolarPanel(MainPanelInterface mpi) {
		this(mpi, null, null, null, null);
	}

	public PolarPanel(MainPanelInterface mpi,
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

	public void setCoeff(double[][] da) {
		coeff = da;
	}

	public void resetCoeffDeg() {
		coeffList = null;
	}

	public void addCoeffDeg(String name, double[][] daa) {
		if (coeffList == null) {
			coeffList = new ArrayList<CoeffForSection>();
		}
		coeffList.add(new CoeffForSection(name, daa));
	}

	private void jbInit() throws Exception {
		this.setLayout(null);
		addMouseMotionListener(this);
		addMouseListener(this);
	}

	public void setSelectedNode(PolarTreeNode[] ptn) {
		selectedNode = ptn;
	}

	public void setSelectedNodeUp(PolarTreeNode[] ptn) {
	}

	public void paintComponent(Graphics g) {
		((Graphics2D) g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		Rectangle rect = this.getBounds();
		int w = rect.width;
		int h = rect.height;
		g.setColor(bgColor);
		g.fillRect(0, 0, w, h);
		g.setColor(gridColor);
		// Painting BSP circles
		for (int i = 0; i < Constants.getMaxBoatSpeedForPolars(); i++) {
			int radius = (i + 1) * Math.min(w, h / 2) / Constants.getMaxBoatSpeedForPolars();
			g.drawOval(-radius, (h / 2) - radius, 2 * radius, 2 * radius);
			g.drawString(Integer.toString(i + 1),2, (h / 2) - radius);
		}
		// Painting radii
		for (double d = 0.0; d <= 180.0; d += 30.0) {
			int small = Math.min(w, h / 2) / Constants.getMaxBoatSpeedForPolars();
			int big = Math.min(w, h / 2);
			g.drawLine((int) (small * Math.sin(Math.toRadians(d))),
					(int) ((h / 2) - (small * Math.cos(Math.toRadians(d)))),
					(int) (big * Math.sin(Math.toRadians(d))),
					(int) ((h / 2) - (big * Math.cos(Math.toRadians(d)))));
			g.drawString(Integer.toString((int) d),
					(int) (big * Math.sin(Math.toRadians(d))),
					(int) ((h / 2) - (big * Math.cos(Math.toRadians(d)))));
		}
		// Paint smooth data
		if (selectedNode != null && (coeff != null || coeffList != null)) {
			Stroke originalStroke = null;
			if (g instanceof Graphics2D) {
				originalStroke = ((Graphics2D) g).getStroke();
				Stroke stroke = new BasicStroke(2,
						BasicStroke.CAP_BUTT,
						BasicStroke.JOIN_BEVEL);
				((Graphics2D) g).setStroke(stroke);
			}
//    g.setColor(lineColor1);
			for (int i = 0; i < selectedNode.length; i++) {
				g.setColor(smoothedPolarColors[i % smoothedPolarColors.length]);
				if (selectedNode[i].getType() == PolarTreeNode.ROOT_TYPE) {
					System.out.println("Men at work..."); // TODO Draw everything
				}
				if (selectedNode[i].getType() == PolarTreeNode.SECTION_TYPE) {
					// Draw from-to TWA
					Color c = g.getColor();
					g.setColor(Color.red);
					float[] dashPattern = {5, 5, 5, 5};
					Stroke origStroke = ((Graphics2D) g).getStroke();
					((Graphics2D) g).setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10, dashPattern, 0));
					int small = Math.min(w, h / 2) / Constants.getMaxBoatSpeedForPolars();
					int big = Math.min(w, h / 2);
					g.drawLine((int) (small * Math.sin(Math.toRadians(selectedNode[i].getFromTwa()))),
							(int) ((h / 2) - (small * Math.cos(Math.toRadians(selectedNode[i].getFromTwa())))),
							(int) (big * Math.sin(Math.toRadians(selectedNode[i].getFromTwa()))),
							(int) ((h / 2) - (big * Math.cos(Math.toRadians(selectedNode[i].getFromTwa())))));
					g.drawLine((int) (small * Math.sin(Math.toRadians(selectedNode[i].getToTwa()))),
							(int) ((h / 2) - (small * Math.cos(Math.toRadians(selectedNode[i].getToTwa())))),
							(int) (big * Math.sin(Math.toRadians(selectedNode[i].getToTwa()))),
							(int) ((h / 2) - (big * Math.cos(Math.toRadians(selectedNode[i].getToTwa())))));
					g.setColor(c);
					((Graphics2D) g).setStroke(origStroke);
					// Now draw
					double[] actualCoeff = new double[selectedNode[i].getPolarDegree() + 1];
					for (int ws = Constants.getMinWindSpeed(); ws < Constants.getMaxWindSpeed(); ws++) {
						double[][] coeffDeg = null;
						for (CoeffForSection cfs : coeffList) {
							if (cfs.getSectionName().equals(selectedNode[i].getModel())) {
								coeffDeg = cfs.getCoeff();
								break;
							}
						}
						for (int j = 0; j < (selectedNode[i].getPolarDegree() + 1); j++) {
							actualCoeff[j] = PolarSmoother.f((double) ws, coeffDeg[j]);
						}
//          System.out.println("Plotting Smoothed data for " + selectedNode[i].getModel() + ", from " + selectedNode[i].getFromTwa() + " to " + selectedNode[i].getToTwa());
						plotSmoothTws(g, actualCoeff, selectedNode[i].getFromTwa(), selectedNode[i].getToTwa());
					}
				} else if (selectedNode[i].getType() == PolarTreeNode.TWS_TYPE) {
					plotSmoothTws(g, coeff[i], ((PolarTreeNode) selectedNode[i].getParent()).getFromTwa(), ((PolarTreeNode) selectedNode[i].getParent()).getToTwa());
				} else if (selectedNode[i].getType() == PolarTreeNode.TWA_TYPE) {
					// Nothing for now...
				}
			}
			if (g instanceof Graphics2D) {
				((Graphics2D) g).setStroke(originalStroke);
			}
		}
		// Paint original data
		g.setColor(lineColor2);
		if (selectedNode != null && plotBulkData) {
			for (int i = 0; i < selectedNode.length; i++) {
				if (selectedNode[i].getType() == PolarTreeNode.ROOT_TYPE) {
					Enumeration enumeration = selectedNode[i].children();
					while (enumeration.hasMoreElements()) {
						PolarTreeNode child = (PolarTreeNode) enumeration.nextElement();
						if (child.getType() == PolarTreeNode.SECTION_TYPE) {
							Enumeration subEnumeration = child.children();
							while (subEnumeration.hasMoreElements()) {
								PolarTreeNode grandChild = (PolarTreeNode) subEnumeration.nextElement();
								if (grandChild.getType() == PolarTreeNode.TWS_TYPE) {
									plotBulkTws(g, grandChild);
								} else {
									System.out.println("Invalid children under Section (in PolarPanel.paintComponent)");
								}
							}
						} else {
							System.out.println("Invalid children under Root (in PolarPanel.paintComponent)");
						}
					}
				}
				if (selectedNode[i].getType() == PolarTreeNode.SECTION_TYPE) {
					// Draw from-to TWA
					Color c = g.getColor();
					g.setColor(Color.red);
					float[] dashPattern = {5, 5, 5, 5};
					Stroke origStroke = ((Graphics2D) g).getStroke();
					((Graphics2D) g).setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10, dashPattern, 0));
					int small = Math.min(w, h / 2) / Constants.getMaxBoatSpeedForPolars();
					int big = Math.min(w, h / 2);
					g.drawLine((int) (small * Math.sin(Math.toRadians(selectedNode[i].getFromTwa()))),
							(int) ((h / 2) - (small * Math.cos(Math.toRadians(selectedNode[i].getFromTwa())))),
							(int) (big * Math.sin(Math.toRadians(selectedNode[i].getFromTwa()))),
							(int) ((h / 2) - (big * Math.cos(Math.toRadians(selectedNode[i].getFromTwa())))));
					g.drawLine((int) (small * Math.sin(Math.toRadians(selectedNode[i].getToTwa()))),
							(int) ((h / 2) - (small * Math.cos(Math.toRadians(selectedNode[i].getToTwa())))),
							(int) (big * Math.sin(Math.toRadians(selectedNode[i].getToTwa()))),
							(int) ((h / 2) - (big * Math.cos(Math.toRadians(selectedNode[i].getToTwa())))));

					g.setColor(gridColor);
					((Graphics2D) g).setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
					// Sector, 0 - From
					if (selectedNode[i].getFromTwa() > 0 && selectedNode.length == 1) {
						int x = -Math.min(w, h / 2);
						int y = h - (2 * big);
						g.fillArc(x, y, 2 * Math.min(w, h / 2), 2 * Math.min(w, h / 2), 90, -selectedNode[i].getFromTwa());
					}
					// Sector, To - 180
					if (selectedNode[i].getToTwa() < 180 && selectedNode.length == 1) {
						int x = -Math.min(w, h / 2);
						int y = h - (2 * big);
						g.fillArc(x, y, 2 * Math.min(w, h / 2), 2 * Math.min(w, h / 2), 90 - selectedNode[i].getToTwa(), selectedNode[i].getToTwa() - 180);
					}
					((Graphics2D) g).setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
					((Graphics2D) g).setStroke(origStroke);
					g.setColor(c); // Reset color
					Enumeration enumeration = selectedNode[i].children();
					while (enumeration.hasMoreElements()) {
						PolarTreeNode child = (PolarTreeNode) enumeration.nextElement();
						if (child.getType() == PolarTreeNode.TWS_TYPE) {
							plotBulkTws(g, child);
						} else {
							System.out.println("Invalid children under Section (in PolarPanel.paintComponent)");
						}
					}
				} else if (selectedNode[i].getType() == PolarTreeNode.TWS_TYPE) {
					plotBulkTws(g, selectedNode[i]);
				} else if (selectedNode[i].getType() == PolarTreeNode.TWA_TYPE) {
					// Nothing for now...
				}
			}
		}
	}

	public void plotBulkTws(Graphics g, PolarTreeNode ptn) {
		Rectangle rect = this.getBounds();
		int w = rect.width;
		int h = rect.height;

		int prevX = Integer.MIN_VALUE;
		int prevY = Integer.MIN_VALUE;
		// Walk through the child-nodes
		Enumeration enumeration = ptn.children();
		double ratio = (double) Math.min(w, h / 2) / (double) Constants.getMaxBoatSpeedForPolars();
		while (enumeration.hasMoreElements()) {
			PolarTreeNode child = (PolarTreeNode) enumeration.nextElement();
			if (child.getType() == PolarTreeNode.TWA_TYPE) {
				int twa = child.getTwa();
				double bsp = child.getBsp();
				if (twa > 0) {
//            System.out.println("Twa:" + twa + ", bsp:" + bsp);
					int x = (int) (ratio * bsp * Math.sin(Math.toRadians((double) twa)));
					int y = (int) ((h / 2) - (ratio * bsp * Math.cos(Math.toRadians((double) twa))));
					g.drawOval(x - 2, y - 2, 4, 4);
					if (prevX != Integer.MIN_VALUE && prevY != Integer.MIN_VALUE) {
						g.drawLine(prevX, prevY, x, y);
					}
					prevX = x;
					prevY = y;
				}
			} else
				System.out.println("Invalid children under TWS");
		}
		// VMG Data
		Color save = g.getColor();
		g.setColor(lineColor1);
		int twa = (int) Math.round(ptn.getUpwindTwa());
		double bsp = ptn.getUpwindSpeed();
		int x = (int) (ratio * bsp * Math.sin(Math.toRadians((double) twa)));
		int y = (int) ((h / 2) - (ratio * bsp * Math.cos(Math.toRadians((double) twa))));
		g.drawOval(x - 2, y - 2, 4, 4);

		twa = (int) Math.round(ptn.getDownwindTwa());
		bsp = ptn.getDownwindSpeed();
		x = (int) (ratio * bsp * Math.sin(Math.toRadians((double) twa)));
		y = (int) ((h / 2) - (ratio * bsp * Math.cos(Math.toRadians((double) twa))));
		g.drawOval(x - 2, y - 2, 4, 4);
		g.setColor(save);
	}

	private void plotSmoothTws(Graphics g, double[] coeff, double fromTwa, double toTwa) {
		Rectangle rect = this.getBounds();
		int w = rect.width;
		int h = rect.height;

		int prevX = Integer.MIN_VALUE;
		int prevY = Integer.MIN_VALUE;

		double ratio = (double) Math.min(w, h / 2) / (double) Constants.getMaxBoatSpeedForPolars();
//  for (int twa=20; twa<=175; twa++)
		for (int twa = (int) fromTwa; twa <= (int) toTwa; twa++) {
			double bsp = PolarSmoother.f((double) twa, coeff);
			int x = (int) (ratio * bsp * Math.sin(Math.toRadians((double) twa)));
			int y = (int) ((h / 2) - (ratio * bsp * Math.cos(Math.toRadians((double) twa))));
			if (prevX != Integer.MIN_VALUE && prevY != Integer.MIN_VALUE) {
				g.drawLine(prevX, prevY, x, y);
			}
			prevX = x;
			prevY = y;
		}
	}

	private PolarPoint panelToPolar(MouseEvent e) {
		e.consume();
//  this.setToolTipText("Mouse " + e.getX() + ", " + e.getY());
		Rectangle r = this.getBounds();
		final int origX = 0, // r.width / 2,
				origY = r.height / 2;
//  int unit = (int)Math.round((double)getHeight() / (double)MAX_WIND_SPEED_FOR_POLAR);
		double unit = (double) Math.min((double) r.height / 2.0, (double) r.width) / (double) Constants.getMaxBoatSpeedForPolars();
		double dist2center = Math.sqrt(Math.pow(origX - e.getX(), 2) + Math.pow(origY - e.getY(), 2));
//  System.out.println("h:" + r.height + ", w:" + r.width + ", unit=" + unit);
		double angle = Double.MIN_VALUE;
		try {
			double x = origX - e.getX();
			double y = origY - e.getY();
			angle = Math.atan(x / y);
			//  angle *= (180.0 / Math.PI);
			angle = Math.toDegrees(angle);
			if (angle < 0.0) {
				angle = -angle;
			} else {
				angle = 180.0 - angle;
			}
		} catch (Exception ignore) { // Divided by zero
			System.out.println("Ooops");
		}
//  double bs = 2 * (double)dist2center / (double)unit;
		double bs = (double) dist2center / unit;
		bs = (double) Math.round(bs * 100.0) / 100.0;

		return new PolarPoint(bs, angle);
	}

	// Tooltip
	public void mouseMoved(MouseEvent e) {
		PolarPoint pp = panelToPolar(e);
		if (mouseDraggedEnabled && plotBulkData) {
			PolarTreeNode closest = findClosest(selectedNode, pp);
			if (closest != null) {
				this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			} else {
				this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			}
		}
		this.setToolTipText((int) Math.round(pp.getTwa()) + "�, " + Double.toString(pp.getBsp()) + " knts");
	}

	public void mouseClicked(MouseEvent e) {
		e.consume();
//  int x = e.getX();
//  int y = e.getY();
//  String mess = "Pos:" + Integer.toString(x) + ", " + Integer.toString(y);
//  System.out.println(mess);
	}

	public void mousePressed(MouseEvent e) {
		if (mouseDraggedEnabled && plotBulkData) {
			PolarTreeNode treeRoot = (PolarTreeNode) parent.getTreeRoot();
			if (treeRoot != null) {
				draggedFromX = e.getX();
				draggedFromY = e.getY();
				PolarPoint pp = panelToPolar(e);
				// Find closest point, must be visible! -> refer to selectedNode
				draggedPolarPoint = findClosest(selectedNode, pp);
				if (draggedPolarPoint != null) {
					// Change the component's cursor to another shape
					this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
//        System.out.println("Found:" + draggedPolarPoint.getBsp() + " knts " + draggedPolarPoint.getTwa());
				}
			}
		}
	}

	private PolarTreeNode findClosest(PolarTreeNode[] selected,
	                                  PolarPoint toBeFound) {
		PolarTreeNode ret = null;
		if (selected == null || selected.length == 0) {
			return null;
		}
		for (int i = 0; i < selected.length; i++) {
			if (selected[i].getType() == PolarTreeNode.ROOT_TYPE ||
					selected[i].getType() == PolarTreeNode.SECTION_TYPE ||
					selected[i].getType() == PolarTreeNode.TWS_TYPE) {
				Enumeration enumeration = selected[i].children();
				while (enumeration.hasMoreElements()) {
					PolarTreeNode n = (PolarTreeNode) enumeration.nextElement();
					ret = findClosest(new PolarTreeNode[]{n}, toBeFound);
					if (ret != null) {
						break;
					}
				}
			} else if (selected[i].getType() == PolarTreeNode.TWA_TYPE) {
				// This is what we're interested in
				double bsp = selected[i].getBsp();
				int twa = selected[i].getTwa();
				if ((int) Math.round(toBeFound.getTwa()) == twa && Math.abs(toBeFound.getBsp() - bsp) < 0.5) {
					// Found!
					ret = selected[i];
					break;
				}
			}
			if (ret != null) {
				break;
			}
		}
		return ret;
	}

	public void setMouseDraggedEnabled(boolean b) {
		mouseDraggedEnabled = b;
	}

	public boolean getMouseDraggedEnabled() {
		return mouseDraggedEnabled;
	}

	public void setPlotBulkData(boolean b) {
		plotBulkData = b;
	}

	public boolean getPlotBulkData() {
		return plotBulkData;
	}

	public void mouseDragged(MouseEvent e) {
		if (mouseDraggedEnabled && plotBulkData) {
			dragged = true;
			PolarPoint pp = panelToPolar(e);
			if (draggedPolarPoint != null) {
				draggedPolarPoint.setBsp(pp.getBsp());
				draggedPolarPoint.setTwa((int) Math.round(pp.getTwa()));
				// Recalculate and set coeffs
				parent.setSelectedNode(selectedNode);
				parent.setSelectedNodeUp(selectedNode);
			}
			this.repaint(); // the polar panel
			((DefaultTreeModel) parent.getJTree().getModel()).reload(draggedPolarPoint);
		}
	}

	public void mouseReleased(MouseEvent e) {
//  System.out.println("released");
		if (dragged) {
			// Move the point
//    PolarPoint pp = panelToPolar(e);
			dragged = false;
//    System.out.println("Moved from " + draggedFromX + "/" + draggedFromY +
//                       " to " + e.getX() + "/" + e.getY() +
//                       " -> " + pp.getTwa() + "/" + pp.getBsp());
			this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		}
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}
}
