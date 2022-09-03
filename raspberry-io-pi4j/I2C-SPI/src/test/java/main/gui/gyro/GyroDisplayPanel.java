package main.gui.gyro;


import main.gui.utils.Point3D;

import i2c.sensor.listener.L3GD20Listener;
import i2c.sensor.listener.SensorL3GD20Context;

import main.SampleL3GD20RealReader;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;

import java.lang.reflect.InvocationTargetException;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

// This class listens to the gyroscope
public class GyroDisplayPanel
				extends JPanel {
	@SuppressWarnings("compatibility:5286281276243161150")
	public final static long serialVersionUID = 1L;

	protected transient Stroke thick = new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
	protected transient Stroke dotted = new BasicStroke(1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1f, new float[]{2f}, 0f);
	protected transient Stroke origStroke = null;

	private transient Point3D[] vertices = null;
	private transient int[][] faces;
	private transient List<Point3D> rotated = null;

	private final static boolean DEMO = "true".equals(System.getProperty("demo", "true"));
	private transient SampleL3GD20RealReader sensorReader = null;

	private double angleX = 0d, angleY = 0d, angleZ = 0d;
	private final double DELTA_T = 0.05;

	public GyroDisplayPanel() {
		try {
			jbInit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void jbInit()
					throws Exception {
		System.out.println("-- Demo Mode is " + (DEMO ? "ON" : "OFF"));
		System.out.println("-- Check it in " + this.getClass().getName());
		this.setLayout(null);
		this.setOpaque(false);
		this.setBackground(new Color(0, 0, 0, 0));
		// Create the model here
		vertices = new Point3D[]
						{
										new Point3D(-2, 0.5, -1), // 0
										new Point3D(2, 0.5, -1), // 1
										new Point3D(2, -0.5, -1), // 2
										new Point3D(-2, -0.5, -1), // 3
										new Point3D(-2, 0.5, 1), // 4
										new Point3D(2, 0.5, 1), // 5
										new Point3D(2, -0.5, 1), // 6
										new Point3D(-2, -0.5, 1)  // 7
						};
		faces = new int[][]
						{
										new int[]{0, 1, 2, 3},
										new int[]{1, 5, 6, 2},
										new int[]{5, 4, 7, 6},
										new int[]{4, 0, 3, 7},
										new int[]{0, 4, 5, 1},
										new int[]{3, 2, 6, 7}
						};

		rotateFigure(0, 0, 0);
		if (DEMO)
			startMoving(); // This would be replaced by the listener interaction, in non-demo mode.
		else {
			Thread sensorListener = new Thread() {
				public void run() {
					try {
						sensorReader = new SampleL3GD20RealReader();
						System.out.println("...Adding listener");
						SensorL3GD20Context.getInstance().addReaderListener(new L3GD20Listener() {
							public void motionDetected(double x, double y, double z) {
								angleX += (x * DELTA_T);
								angleY += (y * DELTA_T);
								angleZ += (z * DELTA_T);
								try {
									rotateFigure(angleX, angleY, angleZ);
								} catch (Exception ex) {
								}
							}

							public void close() {
								sensorReader.stop();
							}
						});
						System.out.println("Starting listening...");
						sensorReader.start();
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			};
			sensorListener.start();
		}
	}

	@Override
	protected void paintComponent(Graphics gr) {
		super.paintComponent(gr);

		Graphics2D g2d = (Graphics2D) gr;
		g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//  origStroke = g2d.getStroke();

		if (rotated != null) {
			synchronized (rotated) {
				for (int[] f : faces) {
					gr.drawLine((int) rotated.get(f[0]).getX(), (int) rotated.get(f[0]).getY(), (int) rotated.get(f[1]).getX(), (int) rotated.get(f[1]).getY());
					gr.drawLine((int) rotated.get(f[1]).getX(), (int) rotated.get(f[1]).getY(), (int) rotated.get(f[2]).getX(), (int) rotated.get(f[2]).getY());
					gr.drawLine((int) rotated.get(f[2]).getX(), (int) rotated.get(f[2]).getY(), (int) rotated.get(f[3]).getX(), (int) rotated.get(f[3]).getY());
					gr.drawLine((int) rotated.get(f[3]).getX(), (int) rotated.get(f[3]).getY(), (int) rotated.get(f[0]).getX(), (int) rotated.get(f[0]).getY());
				}
			}
		}

//  g2d.setStroke(origStroke);
	}

	private void rotateFigure(double x, double y, double z) throws InvocationTargetException, InterruptedException {
		rotated = new ArrayList<Point3D>();
		synchronized (rotated) {
			for (Point3D p : vertices) {
				Point3D r = p.rotateX(x).rotateY(y).rotateZ(z);
				Point3D proj = r.project(this.getWidth(), this.getHeight(), 256, 4);
				rotated.add(proj);
			}
		}
//  repaint();
		SwingUtilities.invokeAndWait(new Runnable() {
			public void run() {
				repaint();
			}
		});

	}

	// For demo
	private void startMoving() {
		Thread movingThread = new Thread() {
			public void run() {
				for (int x = 0, y = 0, z = 0; x < 360; x++, y++, z++) {
					try {
						rotateFigure(x, y, z);
					} catch (Exception ex) {
					}
					try {
						Thread.sleep(10L);
					} catch (InterruptedException ie) {
					}
				}
			}
		};
		movingThread.start();
	}
}
