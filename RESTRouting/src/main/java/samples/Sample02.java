package samples;

import com.google.gson.Gson;
import jgrib.GribFile;
import jgrib.GribRecord;
import jgrib.GribRecordBDS;
import jgrib.GribRecordGDS;
import jgrib.GribRecordPDS;
import jgrib.NoValidGribException;
import jgrib.NotSupportedException;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.SortedSet;
import java.util.TreeSet;

public class Sample02 {

	public static void main(String... args) {
		String fName = "GRIB_2017_10_16_07_31_47_PDT.grb";
		try {
			GribFile gribFile = new GribFile(fName);

			HashMap<GribDate, HashMap<Type, Float[][]>> bigmap = new HashMap<>();

			for (int i = 0; i < gribFile.getLightRecords().length; i++) {
				try {
					GribRecord gr = new GribRecord(gribFile.getLightRecords()[i]);
					GribRecordPDS grpds = gr.getPDS(); // Headers and Data
					GribRecordGDS grgds = gr.getGDS(); // Boundaries and Steps
					GribRecordBDS grbds = gr.getBDS(); // TASK get min and max from this one.

					Date date = grpds.getGMTForecastTime().getTime();
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

					GribDate gDate = new GribDate(date, height, width, stepX, stepY, top, bottom, left, right);

					Float[][] data = new Float[height][width];
					float val = 0F;
					for (int col = 0; col < width; col++) {
						for (int row = 0; row < height; row++) {
							try {
								val = gr.getValue(col, row);
								data[row][col] = val;
							} catch (Exception ex) {
								ex.printStackTrace();
							}
						}
					}
					HashMap<Type, Float[][]> subMap = bigmap.get(gDate);
					if (subMap == null)
						subMap = new HashMap<Type, Float[][]>();
					subMap.put(new Type(type, description, unit, grbds.getMinValue(), grbds.getMaxValue()), data);
					bigmap.put(gDate, subMap);
				} catch (NoValidGribException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (NotSupportedException e) {
					e.printStackTrace();
				}
			}

			SortedSet<GribDate> sortedSet = new TreeSet<>(bigmap.keySet());

			String content = new Gson().toJson(gribFile);
			System.out.println(content);

			System.out.println("Done");
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NotSupportedException e) {
			e.printStackTrace();
		} catch (NoValidGribException e) {
			e.printStackTrace();
		}
	}

	static class Type implements Comparable
	{
		private String type;
		private String desc;
		private String unit;

		private float min, max;

		public Type(String t, String d, String u, float min, float max)
		{
			type = t;
			desc = d;
			unit = u;
			this.min = min;
			this.max = max;
		}

		public String toString() { return type; }

		public void setType(String type)
		{
			this.type = type;
		}

		public String getType()
		{
			return type;
		}

		public int compareTo(Object o)
		{
			return this.type.compareTo(o.toString());
		}

		public void setDesc(String desc)
		{
			this.desc = desc;
		}

		public String getDesc()
		{
			return desc;
		}

		public void setUnit(String unit)
		{
			this.unit = unit;
		}

		public String getUnit()
		{
			return unit;
		}

		public void setMin(float min)
		{
			this.min = min;
		}

		public float getMin()
		{
			return min;
		}

		public void setMax(float max)
		{
			this.max = max;
		}

		public float getMax()
		{
			return max;
		}
	}

	static class GribDate extends Date
	{
		private Date date;
		private int height;
		private int width;
		private double stepx;
		private double stepy;
		private double top, bottom, left, right;

		public GribDate(Date d, int h, int w, double x, double y, double t, double b, double l, double r)
		{
			super(d.getTime());
			this.date = d;
			this.height = h;
			this.width = w;
			this.stepx = x;
			this.stepy = y;
			this.left = l;
			this.right = r;
			this.top = t;
			this.bottom = b;
		}

		public void setGDate(Date date)
		{
			this.date = date;
		}

		public Date getGDate()
		{
			return date;
		}

		public void setHeight(int height)
		{
			this.height = height;
		}

		public int getHeight()
		{
			return height;
		}

		public void setWidth(int width)
		{
			this.width = width;
		}

		public int getWidth()
		{
			return width;
		}

		public void setStepx(double stepx)
		{
			this.stepx = stepx;
		}

		public double getStepx()
		{
			return stepx;
		}

		public void setStepy(double stepy)
		{
			this.stepy = stepy;
		}

		public double getStepy()
		{
			return stepy;
		}

		public void setTop(double top)
		{
			this.top = top;
		}

		public double getTop()
		{
			return top;
		}

		public void setBottom(double bottom)
		{
			this.bottom = bottom;
		}

		public double getBottom()
		{
			return bottom;
		}

		public void setLeft(double left)
		{
			this.left = left;
		}

		public double getLeft()
		{
			return left;
		}

		public void setRight(double right)
		{
			this.right = right;
		}

		public double getRight()
		{
			return right;
		}
	}
}
