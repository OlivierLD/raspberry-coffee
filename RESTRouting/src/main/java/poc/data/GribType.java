package poc.data;

public class GribType
		implements Comparable {
	private String type;
	private String desc;
	private String unit;

	private float min, max;

	public GribType(String t, String d, String u, float min, float max) {
		type = t;
		desc = d;
		unit = u;
		this.min = min;
		this.max = max;
	}

	public String toString() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getType() {
		return type;
	}

	public int compareTo(Object o) {
		return this.type.compareTo(o.toString());
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public String getDesc() {
		return desc;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	public String getUnit() {
		return unit;
	}

	public void setMin(float min) {
		this.min = min;
	}

	public float getMin() {
		return min;
	}

	public void setMax(float max) {
		this.max = max;
	}

	public float getMax() {
		return max;
	}
}

