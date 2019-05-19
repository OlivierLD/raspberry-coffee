package adc.sample.log;


import adc.sample.log.LogAnalysis.LogData;

import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

public class LogAnalysisPanel
		extends JPanel
		implements MouseListener, MouseMotionListener {
	@SuppressWarnings("compatibility:5644286187611665244")
	public final static long serialVersionUID = 1L;

	private transient Map<Date, LogData> logdata = null;
	private Date minDate = null, maxDate = null;
	private float minVolt = Float.MAX_VALUE, maxVolt = Float.MIN_VALUE;
	private final static NumberFormat VOLT_FMT = new DecimalFormat("#0.00");
	private final static DateFormat DATE_FMT = new SimpleDateFormat("dd-MMM-yy HH:mm");
	protected transient Stroke thick = new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
	private boolean withSmoothing = "true".equals(System.getProperty("with.smoothing", "true"));

	public LogAnalysisPanel() {
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

		this.addMouseListener(this);
		this.addMouseMotionListener(this);
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		if (logdata != null) {
			// Smooth Voltage
			Map<Date, Float> smoothVoltage = new HashMap<Date, Float>();
			final int SMOOTH_WIDTH = 1_000;
			SortedSet<Date> keys = new TreeSet<Date>(logdata.keySet());
			if (withSmoothing) {
				List<LogData> ld = new ArrayList<LogData>();
				for (Date d : keys)
					ld.add(logdata.get(d));

				for (int i = 0; i < ld.size(); i++) {
					float yAccu = 0f;
					for (int acc = i - (SMOOTH_WIDTH / 2); acc < i + (SMOOTH_WIDTH / 2); acc++) {
						double y;
						if (acc < 0)
							y = ld.get(0).getVoltage();
						else if (acc > (ld.size() - 1))
							y = ld.get(ld.size() - 1).getVoltage();
						else
							y = ld.get(acc).getVoltage();
						yAccu += y;
					}
					yAccu = yAccu / SMOOTH_WIDTH;
					//      System.out.println("Smooth Voltage:" + yAccu);
					smoothVoltage.put(ld.get(i).getDate(), yAccu);
				}
			}

			// Sort, mini maxi.
			boolean narrow = "y".equals(System.getProperty("narrow", "n"));
			/* SortedSet<Date> */
			keys = new TreeSet<Date>(logdata.keySet());
			for (Date key : keys) {
				LogData value = logdata.get(key);
//      System.out.println(value.getDate() + ": " + value.getVoltage()  + " V");
				if (minDate == null)
					minDate = value.getDate();
				else {
					if (value.getDate().before(minDate))
						minDate = value.getDate();
				}
				if (maxDate == null)
					maxDate = value.getDate();
				else {
					if (value.getDate().after(maxDate))
						maxDate = value.getDate();
				}
				if (narrow) {
					minVolt = Math.min(minVolt, value.getVoltage());
					maxVolt = Math.max(maxVolt, value.getVoltage());
				}
			}
			if (!narrow) {
				minVolt = 0; // Math.min(minVolt, value.getVoltage());
				maxVolt = 15; // Math.max(maxVolt, value.getVoltage());
			}
			long timespan = maxDate.getTime() - minDate.getTime();
			float voltspan = maxVolt - minVolt;

			g2d.setColor(Color.white);
			g2d.fillRect(0, 0, this.getWidth(), this.getHeight());
			// Volt grid
			int minVoltGrid = (int) Math.floor(minVolt);
			int maxVoltGrid = (int) Math.ceil(maxVolt);
			g2d.setColor(Color.lightGray);
			for (int v = minVoltGrid; v < maxVoltGrid; v++) {
				float voltoffset = v - minVolt;
				int y = this.getHeight() - (int) (this.getHeight() * ((float) voltoffset / (float) voltspan));
				g2d.drawLine(0, y, this.getWidth(), y);
			}

			g2d.setColor(Color.red);
			Point previous = null;
			// Raw Data
			for (Date key : keys) {
				LogData value = logdata.get(key);
				Date date = key;
				float volt = value.getVoltage();
				long timeoffset = date.getTime() - minDate.getTime();
				float voltoffset = volt - minVolt;
				int x = (int) (this.getWidth() * ((float) timeoffset / (float) timespan));
				int y = this.getHeight() - (int) (this.getHeight() * ((float) voltoffset / (float) voltspan));
				Point current = new Point(x, y);
//      System.out.println("x:" + x + ", y:" + y);
				if (previous != null)
					g2d.drawLine(previous.x, previous.y, current.x, current.y);
				previous = current;
			}
			if (withSmoothing) {
				// Smooth Data
				g2d.setColor(Color.blue);
				Stroke orig = g2d.getStroke();
				g2d.setStroke(thick);
				previous = null;
				for (Date key : keys) {
					float volt = smoothVoltage.get(key).floatValue();
					long timeoffset = key.getTime() - minDate.getTime();
					float voltoffset = volt - minVolt;
					int x = (int) (this.getWidth() * ((float) timeoffset / (float) timespan));
					int y = this.getHeight() - (int) (this.getHeight() * ((float) voltoffset / (float) voltspan));
					Point current = new Point(x, y);
					//System.out.println("x:" + x + ", y:" + y);
					if (previous != null)
						g2d.drawLine(previous.x, previous.y, current.x, current.y);
					previous = current;
				}
				g2d.setStroke(orig);
			}
		}
	}

	public void setLogData(Map<Date, LogData> logdata) {
		this.logdata = logdata;
		this.repaint();
	}

	@Override
	public void mouseClicked(MouseEvent mouseEvent) {
		// TODO Implement this method
	}

	@Override
	public void mousePressed(MouseEvent mouseEvent) {
		// TODO Implement this method
	}

	@Override
	public void mouseReleased(MouseEvent mouseEvent) {
		// TODO Implement this method
	}

	@Override
	public void mouseEntered(MouseEvent mouseEvent) {
		// TODO Implement this method
	}

	@Override
	public void mouseExited(MouseEvent mouseEvent) {
		// TODO Implement this method
	}

	@Override
	public void mouseDragged(MouseEvent mouseEvent) {
		// TODO Implement this method
	}

	@Override
	public void mouseMoved(MouseEvent mouseEvent) {
		int x = mouseEvent.getPoint().x;
		int y = mouseEvent.getPoint().y;
		// Voltage
		try {
			float voltspan = maxVolt - minVolt;
			long timespan = maxDate.getTime() - minDate.getTime();
			float voltage = minVolt + (voltspan * (float) (this.getHeight() - y) / (float) this.getHeight());
			long minTime = minDate.getTime();
			long time = minTime + (long) (timespan * ((float) x / (float) this.getWidth()));
			Date date = new Date(time);
			String mess = "<html><center><b>" + VOLT_FMT.format(voltage) + " V</b><br>" +
					DATE_FMT.format(date) + "</center></html>";
			//  System.out.println(mess);
			this.setToolTipText(mess);
		} catch (NullPointerException npe) {

		}
	}
}
