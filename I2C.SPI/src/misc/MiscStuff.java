package misc;

import utils.StringUtils;

public class MiscStuff {
	public static void main(String... args) {
		int i = (0x32 >> 1);
		System.out.println("(0x32 >> 1) i:" + Integer.toString(i) + " 0#" + Integer.toBinaryString(i) + " 0x" + Integer.toHexString(i) + " 0" + Integer.toOctalString(i));
		i = (0x3C >> 1);
		System.out.println("(0x3C >> 1) i:" + Integer.toString(i) + " 0#" + Integer.toBinaryString(i) + " 0x" + Integer.toHexString(i) + " 0" + Integer.toOctalString(i));
		//
		int on = 4_095;
		System.out.println(Integer.toString(on) + " = " +
				Integer.toString((on >> 8) & 0xFFFF) + " " + Integer.toString((on & 0xFF) & 0xFFFF) + " .. " +
				"0x" + StringUtils.lpad(Integer.toHexString((on >> 8) & 0xFFFF), 2, "0") + " " + StringUtils.lpad(Integer.toHexString((on & 0xFF) & 0xFFFF), 2, "0"));
		on = 3_024;
		System.out.println(Integer.toString(on) + " = " +
				Integer.toString((on >> 8) & 0xFFFF) + " " + Integer.toString((on & 0xFF) & 0xFFFF) + " .. " +
				"0x" + StringUtils.lpad(Integer.toHexString((on >> 8) & 0xFFFF), 2, "0") + " " + StringUtils.lpad(Integer.toHexString((on & 0xFF) & 0xFFFF), 2, "0"));
		on = 256;
		System.out.println(Integer.toString(on) + " = " +
				Integer.toString((on >> 8) & 0xFFFF) + " " + Integer.toString((on & 0xFF) & 0xFFFF) + " .. " +
				"0x" + StringUtils.lpad(Integer.toHexString((on >> 8) & 0xFFFF), 2, "0") + " " + StringUtils.lpad(Integer.toHexString((on & 0xFF) & 0xFFFF), 2, "0"));
		on = 255;
		System.out.println(Integer.toString(on) + " = " +
				Integer.toString((on >> 8) & 0xFFFF) + " " + Integer.toString((on & 0xFF) & 0xFFFF) + " .. " +
				"0x" + StringUtils.lpad(Integer.toHexString((on >> 8) & 0xFFFF), 2, "0") + " " + StringUtils.lpad(Integer.toHexString((on & 0xFF) & 0xFFFF), 2, "0"));
	}
}
