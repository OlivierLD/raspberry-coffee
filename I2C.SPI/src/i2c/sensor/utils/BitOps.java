package i2c.sensor.utils;

public class BitOps {
	public static boolean checkBit(int value, int position) {
		int mask = 1 << position;
		return ((value & mask) == mask);
	}

	public static int setBit(int value, int position) {
		return (value | (1 << position));
	}

	public static int clearBit(int value, int position) {
		return (value & ~(1 << position));
	}

	public static int flipBit(int value, int position) {
		return (value ^ (1 << position));
	}

	public static boolean checkBits(int value, int mask) {
		return ((value & mask) == mask);
	}

	public static int setBits(int value, int mask) {
		return (value | mask);
	}

	public static int clearBits(int value, int mask) {
		return (value & (~mask));
	}

	public static int flipBits(int value, int mask) {
		return value ^ mask;
	}

	public static int setValueUnderMask(int valueToSet, int currentValue, int mask) {
		int currentValueCleared = clearBits(currentValue, mask);
		int i = 0;
		while (mask % 2 == 0 && mask != 0x00) {
			mask >>= 1;
			i++;
		}
		return setBits(valueToSet << i, currentValueCleared);
	}

	public static int getValueUnderMask(int currentValue, int mask) {
		int currentValueCleared = clearBits(currentValue, ~mask); // clear bits not under mask
		int i = 0;
		while (mask % 2 == 0 && mask != 0x00) {
			mask >>= 1;
			i++;
		}
		return currentValueCleared >> i;
	}

	public static int twosComplementToByte(int value) {
		if (value >= 0 && value <= 0x7f)
			return value;
		else
			return value - 0x100;
	}

	public static int twosComplementToCustom(int value, int signBitPosition) {
		if (value >= 0 && value <= (1 << signBitPosition) - 1)
			return value;
		else
			return value - (2 << signBitPosition);
	}
}
