package i2c.gui.acc;

import i2c.sensor.LSM303;
import i2c.sensor.listener.LSM303Listener;

import i2c.sensor.listener.SensorLSM303Context;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Stroke;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

// This class listens to the  LSM303 (acc + mag)
public class AccelerometerDisplayPanel
				extends JPanel {

	public final static long serialVersionUID = 1L;

	protected transient Stroke thick = new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
	protected transient Stroke dotted = new BasicStroke(1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1f, new float[]{2f}, 0f);
	protected transient Stroke origStroke = null;

	private transient LSM303 sensor = null;

	private List<Double> accXList = new ArrayList<>();
	private List<Double> accYList = new ArrayList<>();
	private List<Double> accZList = new ArrayList<>();
	private List<Double> magXList = new ArrayList<>();
	private List<Double> magYList = new ArrayList<>();
	private List<Double> magZList = new ArrayList<>();
	private List<Double> headingList = new ArrayList<>();

	private double headingDegrees = 0f, pitchDegrees = 0f, rollDegrees = 0f;

	private double minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
	private double minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;
	private double minZ = Integer.MAX_VALUE, maxZ = Integer.MIN_VALUE;

	private final double DELTA_T = 0.05;

	public AccelerometerDisplayPanel() {
		try {
			jbInit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void jbInit()
					throws Exception {
		this.setLayout(null);
		this.setOpaque(false);
		this.setBackground(new Color(0, 0, 0, 0));

		Thread sensorListener = new Thread(() -> {
			try {
				sensor = new LSM303();
				System.out.println("...Adding listener");
				LSM303Listener dataListener = new LSM303Listener() {
					@Override
					public void dataDetected(double accX, double accY, double accZ, double magX, double magY, double magZ, double heading, double pitch, double roll) {
						maxX = Math.max(maxX, accX);
						minX = Math.min(minX, accX);
						maxY = Math.max(maxY, accX);
						minY = Math.min(minY, accX);
						maxZ = Math.max(maxZ, accX);
						minZ = Math.min(minZ, accX);
						synchronized (accXList) {
							accXList.add(accX);
							while (accXList.size() > 1_000) {
								accXList.remove(0);
							}
						}
						synchronized (accYList) {
							accYList.add(accY);
							while (accYList.size() > 1_000) {
								accYList.remove(0);
							}
						}
						synchronized (accZList) {
							accZList.add(accZ);
							while (accZList.size() > 1_000) {
								accZList.remove(0);
							}
						}
						synchronized (magXList) {
							magXList.add(magX);
							while (magXList.size() > 1_000) {
								magXList.remove(0);
							}
						}
						synchronized (magYList) {
							magYList.add(magY);
							while (magYList.size() > 1_000) {
								magYList.remove(0);
							}
						}
						synchronized (magZList) {
							magZList.add(magZ);
							while (magZList.size() > 1_000) {
								magZList.remove(0);
							}
						}
						synchronized (headingList) {
							headingList.add(heading);
							while (headingList.size() > 1_000) {
								headingList.remove(0);
							}
						}
						headingDegrees = heading;
						pitchDegrees   = pitch;
						rollDegrees    = roll;

						repaint();
					}

					public void close() {
						sensor.setKeepReading(false);
					}
				};
				SensorLSM303Context.getInstance().addReaderListener(dataListener);
				sensor.setDataListener(dataListener);
				sensor.setWait(250L);
				System.out.println("Starting listening...");
				sensor.startReading();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		});
		sensorListener.start();
	}

	private final static NumberFormat Z_FMT = new DecimalFormat("000");

	@Override
	protected void paintComponent(Graphics gr) {
		super.paintComponent(gr);

		Graphics2D g2d = (Graphics2D) gr;
		g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		String str = String.format("Heading: %s\272, Pitch: %s\272, Roll: %s\272", Z_FMT.format(headingDegrees), Z_FMT.format(pitchDegrees), Z_FMT.format(rollDegrees));

//  origStroke = g2d.getStroke();
//  g2d.setStroke(origStroke);
//  System.out.println("X data:" + accXList.size() + " point(s) min:" + minX + ", max:" + maxX);
		gr.setColor(Color.white);
		gr.fillRect(0, 0, this.getWidth(), this.getHeight());
		gr.setColor(Color.green);
		synchronized (accXList) {
			drawData(0, gr, accXList, minX, maxX);
		}
		gr.setColor(Color.red);
		synchronized (accYList) {
			drawData(1, gr, accYList, minY, maxY);
		}
		gr.setColor(Color.blue);
		synchronized (accZList) {
			drawData(2, gr, accZList, minZ, maxZ);
		}
		gr.setColor(Color.black);
		gr.drawString(str, 10, 20);
	}

	private void drawData(int idx, Graphics gr, List<Double> data, double min, double max) {
		double xRatio = (double) this.getWidth() / (double) data.size();
		double yRatio = (double) (this.getHeight() / 3) / ((double) (max - min));
		int _x = 0;
		Point previous = null;
		for (Double x : data) {
			int xPt = (int) (_x * xRatio);
			int yPt = (idx * (this.getHeight() / 3)) + (int) ((x.intValue() - min) * yRatio);
			_x++;
			Point pt = new Point(xPt, this.getHeight() - yPt);
			if (previous != null)
				gr.drawLine(previous.x, previous.y, pt.x, pt.y);
			previous = pt;
		}
	}
}
