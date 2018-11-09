package polarmaker.polars.smooth.gui.components.dotmatrix;

import polarmaker.Constants;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StreamTokenizer;
import java.util.Enumeration;
import java.util.Vector;

/**
 * A set of classes to parse, represent and display 3D wireframe models
 * represented in Wavefront .obj format.
 * Adapted from ThreeD.java, delivered with the jdk 1.2.2
 */
public class ThreeDPanel
		extends JPanel
		implements Runnable,
		MouseListener,
		MouseMotionListener {
	private Color bgColor = Color.black;
	private Color lineColor = Color.green;
	private Color textColor = Color.red;
	private Color pointColor = Color.yellow;

	private String panelLabel = "";

	public static final short DOT_OPT = 0;
	public static final short CIRC_OPT = 1;
	public static final short DRAW_OPT = 2;

	private short drawingOption = DOT_OPT;

	private Vector speedPts = null;

	Model3D md = null;
	Model3D pts = null;
	boolean painted = true;
	float xfac;
	int prevx, prevy;
	float xtheta, ytheta;
	float scalefudge = 1.0F;
	Matrix3D amat = new Matrix3D(),
			tmat = new Matrix3D();
	String mdname = null;
	String message = null;

	boolean stbd_port = false;

	public ThreeDPanel(String model,
	                   Color bg,
	                   Color line,
	                   Color text,
	                   Color pts) {
		this(model, 2.0f, bg, line, text, pts);
	}

	public ThreeDPanel(String model,
	                   float scale,
	                   Color bg,
	                   Color line,
	                   Color text,
	                   Color pts) {
		if (bg != null) {
			bgColor = bg;
		}
		if (line != null) {
			lineColor = line;
		}
		if (text != null) {
			textColor = text;
		}
		if (pts != null) {
			pointColor = pts;
		}
		init(model, scale);
	}

	public void setScale(float s) {
		this.scalefudge = s;
	}

	public void setModel(String m) {
		if (!m.equals(mdname)) {
			this.mdname = m;
			init(mdname, scalefudge);
		} else {
			System.out.println("(Same model...)");
		}
	}

	public void setSpeedPts(Vector v) {
		this.speedPts = v;
	}

	public Vector getSpeedPts() {
		return this.speedPts;
	}

	public void setDrawingOption(short s) {
		this.drawingOption = s;
	}

	public void init(String model, float scale) {
		mdname = model;
		scalefudge = scale;

//    amat.yrot(20);
//    amat.xrot(20);
		amat.yrot(10);
		amat.xrot(10);
		if (mdname == null)
			mdname = "obj" + File.separator + Constants.OBJ_FILE_NAME;

		addMouseListener(this);
		addMouseMotionListener(this);

		this.setBackground(bgColor);
	}

	public void setBGColor(Color c) {
		bgColor = c;
	}

	public void setPointColor(Color c) {
		pointColor = c;
	}

	public void setTextColor(Color c) {
		textColor = c;
	}

	public void setLineColor(Color c) {
		lineColor = c;
	}

	public void setPanelLabel(String s) {
		panelLabel = s;
	}

	public void run() {
		InputStream is = null;
		try {
			Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
//    System.out.println("Running 3D for " + mdname);
			is = new FileInputStream(mdname);
			Model3D m = new Model3D(is);
			md = m;
			m.findBB();
			m.compress();
			float xw = m.xmax - m.xmin;
			float yw = m.ymax - m.ymin;
			float zw = m.zmax - m.zmin;
			if (yw > xw) {
				xw = yw;
			}
			if (zw > xw) {
				xw = zw;
			}
			float f1 = getSize().width / xw;
			float f2 = getSize().height / xw;
			xfac = 0.7f * (f1 < f2 ? f1 : f2) * scalefudge;

			if (this.speedPts != null) {
				Model3D ptModel = new Model3D(speedPts);
				pts = ptModel;
				ptModel.findBB();
				ptModel.compress();
			}
		} catch (Exception e) {
			md = null;
			message = e.toString();
			System.out.println("ThreeDPanel:" + message);
		}
		try {
			if (is != null) {
				is.close();
			}
		} catch (Exception e) {
		}
		repaint();
	}

	public void mouseClicked(MouseEvent e) {
	}

	public void mousePressed(MouseEvent e) {
		super.setCursor(new Cursor(Cursor.MOVE_CURSOR));
		prevx = e.getX();
		prevy = e.getY();
		e.consume();
	}

	public void mouseReleased(MouseEvent e) {
		super.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	public void mouseDragged(MouseEvent e) {
		int x = e.getX();
		int y = e.getY();

		tmat.unit();
		float xtheta = (prevy - y) * 360.0f / getSize().width;
		float ytheta = (x - prevx) * 360.0f / getSize().height;
		tmat.xrot(xtheta);
		tmat.yrot(ytheta);
		amat.mult(tmat);
		if (painted) {
			painted = false;
			repaint();
		}
		prevx = x;
		prevy = y;
		e.consume();

//    System.out.println("xtheta:" + xtheta);
//    System.out.println("ytheta:" + ytheta);
	}

	public void mouseMoved(MouseEvent e) {
	}

	public void paintComponent(Graphics g) {
		((Graphics2D) g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		init(mdname, scalefudge);
	}

	public void paint(Graphics g) {
//  System.out.println("Painting for " + mdname);
		Rectangle r = this.getBounds();
		// Background
		g.setColor(bgColor);
		g.fillRect(0, 0, r.width, r.height);

		g.setColor(textColor);
		if (panelLabel.trim().length() > 0) {
			g.drawString(panelLabel, 3, 12);
		}
		g.setColor(bgColor);
		// Axis
    /*
    g.setColor(fs);
    g.drawLine(0, r.height / 2, r.width, r.height / 2);
    g.drawLine(r.width / 2, 0, r.width / 2, r.height);
    */

		if (md == null) {
			run(); // Always reset
		}
		if (md != null) {
			md.mat.unit();
			md.mat.translate(-(md.xmin + md.xmax) / 2,
					-(md.ymin + md.ymax) / 2,
					-(md.zmin + md.zmax) / 2);
			md.mat.mult(amat);
			md.mat.scale(xfac, -xfac, 16 * xfac / getSize().width);
			md.mat.translate(getSize().width / 2, getSize().height / 2, 8);
			md.transformed = false;
			md.paint(g);
		}
		if (pts != null) {
			pts.mat.unit();
			pts.mat.translate(-(md.xmin + md.xmax) / 2,
					-(md.ymin + md.ymax) / 2,
					-(md.zmin + md.zmax) / 2);
			pts.mat.mult(amat);
			pts.mat.scale(xfac, -xfac, 16 * xfac / getSize().width);
			pts.mat.translate(getSize().width / 2, getSize().height / 2, 8);
			pts.transformed = false;
			pts.paint(g, ThreeDPanel.Model3D.PLOT);
		} else if (message != null) {
			g.drawString("Error in model:", 3, 20);
			g.drawString(message, 10, 40);
			System.err.println("Error in model : " + message);
		}
		setPainted();
	}

	private synchronized void setPainted() {
		painted = true;
		notifyAll();
	}

	/**
	 * The representation of a 3D model
	 */
	final class Model3D {
		float vert[];
		int tvert[];
		int nvert, maxvert;
		int con[];
		int ncon, maxcon;
		boolean transformed;
		Matrix3D mat;

		public static final short PLOT = 0;
		public static final short DRAW = 1;

		float xmin, xmax, ymin, ymax, zmin, zmax;

		Model3D() {
			mat = new Matrix3D();
			mat.xrot(20);
			mat.yrot(30);
		}

		/**
		 * Create a 3D model by parsing an input stream
		 */
		Model3D(InputStream is) throws IOException,
				FileFormatException {
			this();
			StreamTokenizer st = new StreamTokenizer(new BufferedReader(new InputStreamReader(is)));
			st.eolIsSignificant(true);
			st.commentChar('#');
			scan: // Ah c'est joli!
			while (true) {
				switch (st.nextToken()) {
					default:
						break scan;
					case StreamTokenizer.TT_EOL:
						break;
					case StreamTokenizer.TT_WORD:
						if ("v".equals(st.sval)) {
							double x = 0, y = 0, z = 0;
							if (st.nextToken() == StreamTokenizer.TT_NUMBER) {
								x = st.nval;
								if (st.nextToken() == StreamTokenizer.TT_NUMBER) {
									y = st.nval;
									if (st.nextToken() == StreamTokenizer.TT_NUMBER)
										z = st.nval;
								}
							}
							addVert((float) x, (float) y, (float) z);
							while (st.ttype != StreamTokenizer.TT_EOL && st.ttype != StreamTokenizer.TT_EOF) {
								st.nextToken();
							}
						} else if ("f".equals(st.sval) || "fo".equals(st.sval) || "l".equals(st.sval)) {
							int start = -1;
							int prev = -1;
							int n = -1;
							while (true) {
								if (st.nextToken() == StreamTokenizer.TT_NUMBER) {
									n = (int) st.nval;
									if (prev >= 0)
										add(prev - 1, n - 1);
									if (start < 0)
										start = n;
									prev = n;
								} else if (st.ttype == '/') {
									st.nextToken();
								} else {
									break;
								}
							}
							if (start >= 0) {
								add(start - 1, prev - 1);
							}
							if (st.ttype != StreamTokenizer.TT_EOL) {
								break scan;
							}
						} else {
							while (st.nextToken() != StreamTokenizer.TT_EOL && st.ttype != StreamTokenizer.TT_EOF) ;
						}
				}
			}
			is.close();
			if (st.ttype != StreamTokenizer.TT_EOF) {
				throw new FileFormatException(st.toString());
			}
		}

		Model3D(Vector v) throws Exception {
			this();
			// v
			int nb = 0;
			Enumeration pts = v.elements();
			while (pts.hasMoreElements()) {
				Object pt = pts.nextElement();
				if (pt instanceof ThreeDPoint) {
					nb++;
					ThreeDPoint pt3d = (ThreeDPoint) pt;
					addVert((float) pt3d.getX(), (float) pt3d.getY(), (float) pt3d.getZ());
				} else {
					throw new Exception("Invalid object in vector");
				}
			}
			// f
			for (int i = 0; i < nb; i++) {
				add(i, i + 1);
			}
		}

		/**
		 * Add a vertex to this model
		 */
		private int addVert(float x, float y, float z) {
			int i = nvert;
			if (i >= maxvert) {
				if (vert == null) {
					maxvert = 100;
					vert = new float[maxvert * 3];
				} else {
					maxvert *= 2;
					float nv[] = new float[maxvert * 3];
					System.arraycopy(vert, 0, nv, 0, vert.length);
					vert = nv;
				}
			}
			i *= 3;
			vert[i] = x;
			vert[i + 1] = y;
			vert[i + 2] = z;
			return nvert++;
		}

		/**
		 * Add a line from vertex p1 to vertex p2
		 */
		private void add(int p1, int p2) {
			int i = ncon;
			if (p1 >= nvert || p2 >= nvert) {
				return;
			}
			if (i >= maxcon) {
				if (con == null) {
					maxcon = 100;
					con = new int[maxcon];
				} else {
					maxcon *= 2;
					int nv[] = new int[maxcon];
					System.arraycopy(con, 0, nv, 0, con.length);
					con = nv;
				}
			}
			if (p1 > p2) {
				int t = p1;
				p1 = p2;
				p2 = t;
			}
			con[i] = (p1 << 16) | p2;
			ncon = i + 1;
		}

		/**
		 * Transform all the points in this model
		 */
		private void transform() {
			if (transformed || nvert <= 0) {
				return;
			}
			if (tvert == null || tvert.length < nvert * 3) {
				tvert = new int[nvert * 3];
			}
			mat.transform(vert, tvert, nvert);
			transformed = true;
		}

		/* Quick Sort implementation
		 */
		private void quickSort(int a[], int left, int right) {
			int leftIndex = left;
			int rightIndex = right;
			int partionElement;
			if (right > left) {

				/* Arbitrarily establishing partition element as the midpoint of
				 * the array.
				 */
				partionElement = a[(left + right) / 2];

				// loop through the array until indices cross
				while (leftIndex <= rightIndex) {
					/* find the first element that is greater than or equal to
					 * the partionElement starting from the leftIndex.
					 */
					while ((leftIndex < right) && (a[leftIndex] < partionElement)) {
						++leftIndex;
					}

					/* find an element that is smaller than or equal to
					 * the partionElement starting from the rightIndex.
					 */
					while ((rightIndex > left) && (a[rightIndex] > partionElement)) {
						--rightIndex;
					}
					// if the indexes have not crossed, swap
					if (leftIndex <= rightIndex) {
						swap(a, leftIndex, rightIndex);
						++leftIndex;
						--rightIndex;
					}
				}

				/* If the right index has not reached the left side of array
				 * must now sort the left partition.
				 */
				if (left < rightIndex) {
					quickSort(a, left, rightIndex);
				}

				/* If the left index has not reached the right side of array
				 * must now sort the right partition.
				 */
				if (leftIndex < right) {
					quickSort(a, leftIndex, right);
				}
			}
		}

		private void swap(int a[], int i, int j) {
			int T;
			T = a[i];
			a[i] = a[j];
			a[j] = T;
		}


		/**
		 * eliminate duplicate lines
		 */
		private void compress() {
			int limit = ncon;
			int c[] = con;
			quickSort(con, 0, ncon - 1);
			int d = 0;
			int pp1 = -1;
			for (int i = 0; i < limit; i++) {
				int p1 = c[i];
				if (pp1 != p1) {
					c[d] = p1;
					d++;
				}
				pp1 = p1;
			}
			ncon = d;
		}

		private Color gr[];

		/**
		 * Paint this model to a graphics context.  It uses the matrix associated
		 * with this model to map from model space to screen space.
		 * The next version of the browser should have double buffering,
		 * which will make this *much* nicer
		 */
		private void paint(Graphics g) {
			paint(g, DRAW);
		}

		private void paint(Graphics g, short opt) {
			if (vert == null || nvert <= 0) {
				return;
			}
			transform();
			if (opt == DRAW) {
				if (gr == null) {
					gr = new Color[16];
					// Green from 255 (light) to 60 (dark)
					int FROM = 225;
					int TO = 60;
					int AMPLITUDE = FROM - TO;
					int ORIGIN = 255 - AMPLITUDE;
					for (int i = 0; i < 16; i++) {
//          int grey = (int) (170*(1-Math.pow(i/15.0, 2.3)));
//          gr[15 - i] = new Color(grey, grey, grey);
						int green = ORIGIN + ((int) ((double) (15 - i) * ((double) AMPLITUDE / 15.0)));
//          gr[15 - i] = new Color(0, green, 0);
						gr[15 - i] = lineColor;
					}
				}
			}
			int lg = 0;
			int lim = ncon;
			int c[] = con;
			int v[] = tvert;
			if (lim <= 0 || nvert <= 0) {
				return;
			}
			int p1 = 0, p2 = 0;
			for (int i = 0; i < lim; i++) {
				int T = c[i];
				p1 = ((T >> 16) & 0xFFFF) * 3;
				p2 = (T & 0xFFFF) * 3;
				int grey = v[p1 + 2] + v[p2 + 2];
				if (grey < 0) {
					grey = 0;
				}
				if (grey > 15) {
					grey = 15;
				}
				if (grey != lg) {
					lg = grey;
					g.setColor(opt == DRAW ? gr[grey] : pointColor);
				}


				if (opt == DRAW) {
					if (i == 0) { // Axes
//          g.setColor(Color.black);
						g.setColor(lineColor);
					}
					if (i == 1 && stbd_port) {
						Color cc = g.getColor();
						g.setColor(textColor);
						Font f = g.getFont();
						Font f2 = new Font(f.getName(), Font.BOLD, f.getSize());
						g.setFont(f2);
						String str = "STBD";
						int l = g.getFontMetrics(f2).stringWidth(str);
						g.drawString(str, v[p1] - (l / 2), v[p1 + 1]);
						g.setFont(f);
						g.setColor(cc);
					}
					if (i == 36 && stbd_port) {
						Color cc = g.getColor();
						g.setColor(textColor);
						Font f = g.getFont();
						Font f2 = new Font(f.getName(), Font.BOLD, f.getSize());
						g.setFont(f2);
						String str = "PORT";
						int l = g.getFontMetrics(f2).stringWidth(str);
						g.drawString(str, v[p1] - (l / 2), v[p1 + 1]);
						g.setFont(f);
						g.setColor(cc);
					}
				} else {
					g.setColor(pointColor);
				}
				if (opt == DRAW) {
					// Draw
					g.drawLine(v[p1], v[p1 + 1], v[p2], v[p2 + 1]);
				} else {
					// Plot
					if (drawingOption == CIRC_OPT) {
						g.drawOval(v[p1] - 2, v[p1 + 1] - 2, 4, 4);
					} else if (drawingOption == DOT_OPT) {
						g.drawLine(v[p1], v[p1 + 1], v[p1], v[p1 + 1]);
					} else if (drawingOption == DRAW_OPT) {
						g.drawLine(v[p1], v[p1 + 1], v[p2], v[p2 + 1]);
					}
				}
			}
			if (opt == PLOT && drawingOption != DRAW_OPT) { // Last point
				g.drawLine(v[p2], v[p2 + 1], v[p2], v[p2 + 1]);
			}
		}

		/**
		 * Find the bounding box of this model
		 */
		void findBB() {
			if (nvert <= 0) {
				return;
			}
			float v[] = vert;
			float xmin = v[0], xmax = xmin;
			float ymin = v[1], ymax = ymin;
			float zmin = v[2], zmax = zmin;
			for (int i = nvert * 3; (i -= 3) > 0; ) {
				float x = v[i];
				if (x < xmin) {
					xmin = x;
				}
				if (x > xmax) {
					xmax = x;
				}
				float y = v[i + 1];
				if (y < ymin) {
					ymin = y;
				}
				if (y > ymax) {
					ymax = y;
				}
				float z = v[i + 2];
				if (z < zmin) {
					zmin = z;
				}
				if (z > zmax) {
					zmax = z;
				}
			}
			this.xmax = xmax;
			this.xmin = xmin;
			this.ymax = ymax;
			this.ymin = ymin;
			this.zmax = zmax;
			this.zmin = zmin;
		}
	}

	class FileFormatException extends Exception {
		public FileFormatException(String s) {
			super(s);
		}
	}
}

