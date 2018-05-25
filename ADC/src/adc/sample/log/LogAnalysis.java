package adc.sample.log;

import javax.swing.JOptionPane;
import javax.swing.UIManager;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class LogAnalysis {
	private static float voltageCoeff = 1.0f; // MULTIPLYING VOLTAGE by this one !
	private static int hourOffset = 0;
	private final static SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
	private final Map<Date, LogData> logdata = new HashMap<Date, LogData>();

	public LogAnalysis(String fName) throws IOException, ParseException {
		LogAnalysisFrame frame = new LogAnalysisFrame(this);
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension frameSize = frame.getSize();
		if (frameSize.height > screenSize.height) {
			frameSize.height = screenSize.height;
		}
		if (frameSize.width > screenSize.width) {
			frameSize.width = screenSize.width;
		}
		frame.setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);
		//  frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});

		frame.setVisible(true);

		BufferedReader br = new BufferedReader(new FileReader(fName));
		String line = "";
		boolean keepReading = true;
		Date minDate = null, maxDate = null;
		long smallestTimeInterval = Long.MAX_VALUE;
		long previousDate = -1L;
		int prevAdc = 0;
		int prevVolume = 0;
		float prevVoltage = 0;
		float minVolt = Float.MAX_VALUE, maxVolt = Float.MIN_VALUE;
		int nbl = 0;
		boolean withSmoothing = "true".equals(System.getProperty("with.smoothing", "true"));
		while (keepReading) {
			line = br.readLine();
			if (line == null)
				keepReading = false;
			else {
				nbl++;
				if (nbl % 100 == 0)
					System.out.println("Read " + nbl + " lines");
				String[] data = line.split(";");
				try {
					Date logDate = SDF.parse(data[0]);
					logDate = new Date(logDate.getTime() + (hourOffset * 3_600_000)); // TODO Make sure the gap is not too big (like > 1 day)
					int adc = Integer.parseInt(data[1]);
					int volume = Integer.parseInt(data[2]);
					float voltage = Float.parseFloat(data[3]) * voltageCoeff;
					if (withSmoothing && previousDate != -1) {
						// Smooth...
						long deltaT = (logDate.getTime() - previousDate) / 1_000; // in seconds
						int deltaADC = (adc - prevAdc);
						int deltaVolume = (volume - prevVolume);
						float deltaVolt = (voltage - prevVoltage);
						for (int i = 0; i < deltaT; i++) {
							Date smoothDate = new Date(previousDate + (i * 1_000));
							int smoothADC = prevAdc + (int) ((double) deltaADC * ((double) i / (double) deltaT));
							int smoothVolume = prevVolume + (int) ((double) deltaVolume * ((double) i / (double) deltaT));
							float smoothVolt = prevVoltage + (float) ((double) deltaVolt * ((double) i / (double) deltaT));
							logdata.put(smoothDate, new LogData(smoothDate, smoothADC, smoothVolume, smoothVolt));
						}
					} else {
						logdata.put(logDate, new LogData(logDate, adc, volume, voltage));
					}
					if (minDate == null)
						minDate = logDate;
					else {
						long interval = logDate.getTime() - previousDate;
						smallestTimeInterval = Math.min(smallestTimeInterval, interval);
						if (logDate.before(minDate))
							minDate = logDate;
					}
					prevAdc = adc;
					prevVolume = volume;
					prevVoltage = voltage;
					previousDate = logDate.getTime();
					if (maxDate == null)
						maxDate = logDate;
					else {
						if (logDate.after(maxDate))
							maxDate = logDate;
					}
					minVolt = Math.min(minVolt, voltage);
					maxVolt = Math.max(maxVolt, voltage);
				} catch (NumberFormatException nfe) {
					System.err.println("For line [" + line + "], (" + nbl + ") " + nfe.toString());
				} catch (ParseException pe) {
					System.err.println("For line [" + line + "], (" + nbl + ") " + pe.toString());
				} catch (Exception ex) {
					System.err.println("For line [" + line + "], (" + nbl + ")");
					ex.printStackTrace();
				}
			}
		}
		br.close();
		System.out.println("Read " + nbl + " lines.");
		// Sort
//    SortedSet<Date> keys = new TreeSet<Date>(logdata.keySet());
//    for (Date key : keys)
//    {
//       LogData value = logdata.get(key);
//       // do something
//      System.out.println(value.getDate() + ": " + value.getVoltage()  + " V");
//    }
		if (nbl == 0) {
			JOptionPane.showMessageDialog(null, "LogFile [" + fName + "] is empty, aborting.", "Battery Log", JOptionPane.WARNING_MESSAGE);
			System.exit(1);
		} else {
			System.out.println("From  [" + minDate + "] to [" + maxDate + "] (" + Long.toString((maxDate.getTime() - minDate.getTime()) / 1_000) + " s)");
			System.out.println("Volts [" + minVolt + ", " + maxVolt + "]");
			System.out.println("Smallest interval:" + (smallestTimeInterval / 1_000) + " s.");
			System.out.println("LogData has " + logdata.size() + " element(s)");
			frame.setLogData(logdata);
		}
	}

	public static void main(String... args) throws Exception {
		try {
			if (System.getProperty("swing.defaultlaf") == null)
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			voltageCoeff = Float.parseFloat(System.getProperty("voltage.coeff", Float.toString(voltageCoeff)));
		} catch (NumberFormatException nfe) {
			nfe.printStackTrace();
		}
		try {
			hourOffset = Integer.parseInt(System.getProperty("hour.offset", Integer.toString(hourOffset)));
		} catch (NumberFormatException nfe) {
			nfe.printStackTrace();
		}
		System.setProperty("hour.offset", Integer.toString(hourOffset));
		String dataFName = "battery.log";
		if (args.length > 0)
			dataFName = args[0];
		new LogAnalysis(dataFName);
	}

	public static class LogData {
		private Date date;
		private int adc;
		private int volume;

		public Date getDate() {
			return date;
		}

		public int getAdc() {
			return adc;
		}

		public int getVolume() {
			return volume;
		}

		public float getVoltage() {
			return voltage;
		}

		private float voltage;

		public LogData(Date d, int a, int v, float volt) {
			this.date = d;
			this.adc = a;
			this.volume = v;
			this.voltage = volt;
		}
	}
}
