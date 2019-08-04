package poc;

import jgrib.GribFile;
import jgrib.GribRecord;
import jgrib.GribRecordBDS;
import jgrib.GribRecordGDS;
import jgrib.GribRecordPDS;
import jgrib.NoValidGribException;
import jgrib.NotSupportedException;
import poc.bulkpanel.BulkGribPanel;
import poc.bulkpanel.GribDatePanel;
import poc.bulkpanel.OneGRIBTablePanel;
import poc.data.GribDate;
import poc.data.GribType;

import javax.swing.JFrame;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TimeZone;
import java.util.TreeSet;

public class GRIBBulk {
	public static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss_S_z");
	public static final SimpleDateFormat FORMATTED = new SimpleDateFormat("yyyy MMM dd HH:mm:ss.S z");

	private final static boolean verbose = "true".equals(System.getProperty("grib.verbose", "false"));

	private Map<GribDate, Map<GribType, Float[][]>> gribDataMap = null;
	private List<String> feedback = null;

	public GRIBBulk() {
		super();
	}

	private final void showBulk(final Thread parent, GribFile gribFile) {
		feedback = new ArrayList<>(1);
		try {
//    GribPDSParamTable.turnOffJGRIBLogging();

			TimeZone tz = TimeZone.getTimeZone("Etc/UTC"); // ("127"); // "GMT + 0"
			//  TimeZone.setDefault(tz);
			SDF.setTimeZone(tz);
			FORMATTED.setTimeZone(tz);

			gribDataMap = new HashMap<>();

			for (int i = 0; i < gribFile.getLightRecords().length; i++) {
				try {
					GribRecord gr = new GribRecord(gribFile.getLightRecords()[i]);
					GribRecordPDS grpds = gr.getPDS(); // Headers and Data
					GribRecordGDS grgds = gr.getGDS(); // Boundaries and Steps
					GribRecordBDS grbds = gr.getBDS(); // TASK get min and max from this one.

					Calendar cal = grpds.getGMTForecastTime();
					cal.setTimeZone(tz);
					Date date = cal.getTime();

					int width = grgds.getGridNX();
					int height = grgds.getGridNY();
					double stepX = grgds.getGridDX();
					double stepY = grgds.getGridDY();
					double top = Math.max(grgds.getGridLat1(), grgds.getGridLat2());
					double bottom = Math.min(grgds.getGridLat1(), grgds.getGridLat2());
					double left = Math.min(grgds.getGridLon1(), grgds.getGridLon2());
					double right = Math.max(grgds.getGridLon1(), grgds.getGridLon2());

					String type = grpds.getType();
					String description = grpds.getDescription();
					String unit = grpds.getUnit();

					if (right - left > 180) { // then swap. like left=-110, right=130
						double tmp = right;
						right = left;
						left = tmp;
					}

					GribDate gDate = new GribDate(date, height, width, stepX, stepY, top, bottom, left, right);

					Float[][] data = new Float[height][width];
					float val = 0F;
					for (int col = 0; col < width; col++) {
						for (int row = 0; row < height; row++) {
							try {
								val = gr.getValue(col, row);
								if (val > grbds.getMaxValue() || val < grbds.getMinValue()) {
									if (verbose) {
										System.out.println("type:" + type + " val:" + val + " is out of [" + grbds.getMinValue() + ", " + grbds.getMaxValue() + "]");
									}
									val = grbds.getMinValue(); // TODO Make sure that's right...
								}
								data[row][col] = val;
							} catch (Exception ex) {
								ex.printStackTrace();
							}
						}
					}
					Map<GribType, Float[][]> subMap = gribDataMap.get(gDate);
					if (subMap == null) {
						subMap = new HashMap<>();
					}
					subMap.put(new GribType(type, description, unit, grbds.getMinValue(), grbds.getMaxValue()), data);
					gribDataMap.put(gDate, subMap);
				} catch (NoValidGribException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (NotSupportedException e) {
					e.printStackTrace();
				}
			}

			BulkGribPanel bgp = new BulkGribPanel(this);

			SortedSet<GribDate> ss = new TreeSet<>(gribDataMap.keySet());
			for (GribDate d : ss) {
				// New Date Tab
				GribDatePanel datePanel = new GribDatePanel();
				bgp.getMainTabbedPane().add(FORMATTED.format(d.getGDate()), datePanel);
				// datePanel : more info
				datePanel.setWidth(d.getWidth());
				datePanel.setHeight(d.getHeight());
				datePanel.setStepX(d.getStepx());
				datePanel.setStepY(d.getStepy());
				datePanel.setTop(d.getTop());
				datePanel.setBottom(d.getBottom());
				datePanel.setLeft(d.getLeft());
				datePanel.setRight(d.getRight());

				Map<GribType, Float[][]> dMap = gribDataMap.get(d);
				SortedSet<GribType> type4date = new TreeSet<>(dMap.keySet());
				//    System.out.println(d.getGDate().toString() + " : " + type4date.size() + " type(s)");
				for (GribType t : type4date) {
					// New type tab in this date tab
					//      System.out.print("  For " + d + " and type " + t + " (" + t.getDesc() + ", " + t.getUnit() + ")");
					Float[][] data = dMap.get(t);
					//      System.out.println(data.length + "x" + data[0].length);
					OneGRIBTablePanel ogtp = new OneGRIBTablePanel();
					ogtp.setData(data);
					ogtp.setMin(t.getMin());
					ogtp.setMax(t.getMax());

					ogtp.setText(t.getDesc() + ", " + t.getUnit() + ". min:" + Float.toString(t.getMin()) + ", max:" + Float.toString(t.getMax()));
					datePanel.getDateTabbedPane().add(t.getType(), ogtp);
				}
			}

			JFrame frame = new JFrame("Bulk GRIB Data");
			frame.getContentPane().add(bgp);
			frame.setSize(new Dimension(800, 600));
			Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
			Dimension frameSize = frame.getSize();
			if (frameSize.height > screenSize.height) frameSize.height = screenSize.height;
			if (frameSize.width > screenSize.width) frameSize.width = screenSize.width;
			frame.setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);
			frame.addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent e) {
					e.getComponent().setVisible(false);
//          System.out.println("Notifying...");
					feedback.add("Success!");
					synchronized (parent) {
						parent.notify();
					}
				}
			});
			frame.setVisible(true);
		} catch (Exception ex) {
			ex.printStackTrace();
			feedback.add(ex.toString());
		}
	}

	private void dumpFeedback() {
		if (feedback != null) {
			feedback.stream().forEach(System.out::println);
		}
	}
	// For standalone tests
	public static void main(String... args) throws Exception {
		GRIBBulk gb = new GRIBBulk();
//	"GRIB_2017_10_16_07_31_47_PDT.grb", "GRIB_2009_02_25_Sample.grb";
//	String gribFileName = "GRIB_2009_02_25_Sample.grb";
		String gribFileName = "GRIB_2017_10_16_07_31_47_PDT.grb";
		if (args.length > 0) {
			gribFileName = args[0];
		}
		URL gribURL = new File(gribFileName).toURI().toURL();
		GribFile gf = new GribFile(gribURL.openStream());
		gb.showBulk(Thread.currentThread(), gf);
		Thread me = Thread.currentThread();
		synchronized (me) {
			me.wait();
		}
		System.out.println("Done:");
		gb.dumpFeedback();
		System.exit(0);
	}
}
