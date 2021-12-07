package nmea.parser;

import java.util.ArrayList;
import java.util.List;

public class GSA {
	public enum ModeOne {Manual, Auto}

	public enum ModeTwo {NoFix, TwoD, ThreeD}

	private ModeOne mode1;
	private ModeTwo mode2;
	private List<Integer> svArray = new ArrayList<>();
	private float pDOP = -1f;
	private float hDOP = -1f;
	private float vDOP = -1f;

	public void setMode1(GSA.ModeOne mode1) {
		this.mode1 = mode1;
	}

	public GSA.ModeOne getMode1() {
		return mode1;
	}

	public void setMode2(GSA.ModeTwo mode2) {
		this.mode2 = mode2;
	}

	public GSA.ModeTwo getMode2() {
		return mode2;
	}

	public List<Integer> getSvArray() {
		return svArray;
	}

	public void setPDOP(float pDOP) {
		this.pDOP = pDOP;
	}

	public float getPDOP() {
		return pDOP;
	}

	public void setHDOP(float hDOP) {
		this.hDOP = hDOP;
	}

	public float getHDOP() {
		return hDOP;
	}

	public void setVDOP(float vDOP) {
		this.vDOP = vDOP;
	}

	public float getVDOP() {
		return vDOP;
	}
}
