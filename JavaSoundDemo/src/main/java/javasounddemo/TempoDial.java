/*
 * @(#)TempoDial.java	1.8	99/11/03
 *
 * Copyright (c) 1998, 1999 by Sun Microsystems, Inc. All Rights Reserved.
 * 
 * Sun grants you ("Licensee") a non-exclusive, royalty free, license to use,
 * modify and redistribute this software in source and binary code form,
 * provided that i) this copyright notice and license appear on all copies of
 * the software; and ii) Licensee does not utilize the software in a manner
 * which is disparaging to Sun.
 * 
 * This software is provided "AS IS," without a warranty of any kind. ALL
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING ANY
 * IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 * NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN AND ITS LICENSORS SHALL NOT BE
 * LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING
 * OR DISTRIBUTING THE SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL SUN OR ITS
 * LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT,
 * INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER
 * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF
 * OR INABILITY TO USE SOFTWARE, EVEN IF SUN HAS BEEN ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGES.
 * 
 * This software is not designed or intended for use in on-line control of
 * aircraft, air traffic, aircraft navigation or aircraft communications; or in
 * the design, construction, operation or maintenance of any nuclear
 * facility. Licensee represents and warrants that it will not use or
 * redistribute the Software for such purposes.
 */
package javasounddemo;

import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.Vector;
import javax.sound.midi.Sequencer;

/**
 * Midi tempo dial in beats per minute.
 *
 * @author Brian Lichtenwalter
 * @version @(#)TempoDial.java	1.8 99/11/03
 */
public class TempoDial extends JPanel {

	private int dotSize = 6;
	private Ellipse2D ellipse;
	private Vector data;
	private Data currentData;
	private Sequencer sequencer;

	@SuppressWarnings("unchecked")
	public TempoDial() {
		setBackground(new Color(20, 20, 20));
		ellipse = new Ellipse2D.Float(2, 20, 92, 120);
		Vector dots = new Vector();
		PathIterator pi = ellipse.getPathIterator(null, 0.9);
		while (!pi.isDone()) {
			float[] pt = new float[6];
			switch (pi.currentSegment(pt)) {
				case FlatteningPathIterator.SEG_MOVETO:
				case FlatteningPathIterator.SEG_LINETO:
					dots.add(new Ellipse2D.Float(pt[0], pt[1], dotSize, dotSize));
			}
			pi.next();
		}
		Vector tmp = new Vector();
		for (int i = 0; i < dots.size(); i++) {
			if (((Ellipse2D) dots.get(i)).getY() >= ellipse.getHeight() / 2) {
				tmp.add(dots.get(i));
			}
		}
		dots.removeAll(tmp);

		float x = (float) (ellipse.getX() + ellipse.getWidth() / 2);
		float y = (float) (ellipse.getY() + (ellipse.getHeight() / 2));
		Vector paths = new Vector(dots.size());
		for (int i = 0; i < dots.size(); i++) {
			GeneralPath gp = new GeneralPath(GeneralPath.WIND_NON_ZERO);
			gp.moveTo(x, y);
			Ellipse2D e1 = (Ellipse2D) dots.get(i);
			gp.lineTo((float) e1.getX(), (float) e1.getY());
			if (i + 1 < dots.size()) {
				Ellipse2D e2 = (Ellipse2D) dots.get(i + 1);
				gp.lineTo((float) e2.getX(), (float) e2.getY());
			}
			gp.closePath();
			paths.add(gp);
		}

		data = new Vector(paths.size());
		for (int i = 0, tempo = 40; i < paths.size(); i++, tempo += 10) {
			data.add(new Data(tempo, dots.get(i), paths.get(i)));
			if (tempo == 120) {
				currentData = (Data) data.lastElement();
			}
		}

		addMouseMotionListener(new MouseMotionAdapter() {
			public void mouseDragged(MouseEvent e) {
				processMouse(e);
			}
		});
		addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				processMouse(e);
			}
		});
	}


	private void processMouse(MouseEvent e) {
		if (ellipse.contains(e.getPoint())) {
			for (int i = 0; i < data.size(); i++) {
				currentData = (Data) data.get(i);
				if (currentData.path.contains(e.getPoint())) {
					break;
				}
			}
			repaint();
			if (sequencer != null) {
				sequencer.setTempoInBPM((float) getTempo());
			}
		}
	}


	public void setSequencer(Sequencer sequencer) {
		this.sequencer = sequencer;
	}


	public float getTempo() {
		return ((float) currentData.tempo);
	}


	/**
	 * Tempo value must match one found in data vector.
	 * Acceptable tempo values start at 40 increment by 10 until 160.
	 */
	public void setTempo(float tempo) {
		for (int i = 0; i < data.size(); i++) {
			currentData = (Data) data.get(i);
			if (currentData.tempo == tempo) {
				break;
			}
		}
		repaint();
	}


	public void paint(Graphics g) {
		Dimension d = getSize();
		Graphics2D g2 = (Graphics2D) g;
		g2.setBackground(getBackground());
		g2.clearRect(0, 0, d.width, d.height);
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
						RenderingHints.VALUE_ANTIALIAS_ON);

		double x = ellipse.getWidth() / 2 + ellipse.getX() + dotSize / 2;
		double y = ellipse.getHeight() / 2;
		double x2 = currentData.dot.getX() + dotSize / 2;
		double y2 = currentData.dot.getY() + dotSize / 2;
		Ellipse2D e = new Ellipse2D.Double(x - 5, y - 5, 10, 10);

		Color jfcBlue = new Color(204, 204, 255);
		g2.setColor(jfcBlue);
		g2.setStroke(new BasicStroke(3));
		g2.draw(new Line2D.Double(e.getX() + 5, e.getY() + 5, x2, y2));
		g2.fill(e);
		g2.setFont(new Font("serif", Font.BOLD, 12));
		g2.drawString(String.valueOf(currentData.tempo) + " bpm", 2, 12);

		g2.fill(currentData.dot);
		g2.setStroke(new BasicStroke(1.5f));
		g2.setColor(jfcBlue.darker());
		for (int i = 0; i < data.size(); i++) {
			g2.draw(((Data) data.get(i)).dot);
		}
	}

	public Dimension getPreferredSize() {
		return new Dimension(105, 70);
	}

	public Dimension getMaximumSize() {
		return getPreferredSize();
	}

	/**
	 * Convenience storage class for our tempo dial data.
	 */
	class Data extends Object {
		int tempo;
		Ellipse2D dot;
		GeneralPath path;

		public Data(int tempo, Object dot, Object path) {
			this.tempo = tempo;
			this.dot = (Ellipse2D) dot;
			this.path = (GeneralPath) path;
		}
	}

	public static void main(String argv[]) {
		JFrame f = new JFrame("Tempo Dial");
		f.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
		f.getContentPane().add("Center", new TempoDial());
		f.pack();
		f.setSize(new Dimension(200, 140));
		f.setVisible(true);
	}
}
