package i2c.sensor.utils;

import com.pi4j.io.i2c.I2CDevice;

import java.io.IOException;

public class EndianReaders {

	public enum Endianness {
		LITTLE_ENDIAN,
		BIG_ENDIAN
	}

	/**
	 * Read an unsigned byte from the I2C device
	 */
	public static int readU8(I2CDevice device, int i2caddr, int reg, boolean verbose) throws IOException {
		int result = 0;

		result = device.read(reg);
		if (verbose) {
			System.out.println("I2C: Device " + i2caddr + " (0x" + Integer.toHexString(i2caddr) +
					") returned " + result + " (0x" + Integer.toHexString(result) +
					") from reg " + reg + " (0x" + Integer.toHexString(reg) + ")");
		}

		return result; // & 0xFF;
	}

	/**
	 * Read a signed byte from the I2C device
	 */
	public static int readS8(I2CDevice device, int i2caddr, int reg, boolean verbose) throws IOException {
		int result = 0;

		result = device.read(reg); // & 0x7F;
		if (result > 127)
			result -= 256;
		if (verbose) {
			System.out.println("I2C: Device " + i2caddr + " returned " + result + " from reg " + reg);
		}
		return result; // & 0xFF;
	}

	public static int readU16LE(I2CDevice device, int i2caddr, int register, boolean verbose) throws IOException {
		return EndianReaders.readU16(device, i2caddr, register, Endianness.LITTLE_ENDIAN, verbose);
	}

	public static int readU16BE(I2CDevice device, int i2caddr, int register, boolean verbose) throws IOException {
		return EndianReaders.readU16(device, i2caddr, register, Endianness.BIG_ENDIAN, verbose);
	}

	public static int readU16(I2CDevice device, int i2caddr, int register, Endianness endianness, boolean verbose) throws IOException {
		int hi = EndianReaders.readU8(device, i2caddr, register, verbose);
		int lo = EndianReaders.readU8(device, i2caddr, register + 1, verbose);
		return ((endianness == Endianness.BIG_ENDIAN) ? (hi << 8) + lo : (lo << 8) + hi); // & 0xFFFF;
	}

	public static int readS16(I2CDevice device, int i2caddr, int register, Endianness endianness, boolean verbose) throws IOException {
		int hi = 0, lo = 0;
		if (endianness == Endianness.BIG_ENDIAN) {
			hi = EndianReaders.readS8(device, i2caddr, register, verbose);
			lo = EndianReaders.readU8(device, i2caddr, register + 1, verbose);
		} else {
			lo = EndianReaders.readU8(device, i2caddr, register, verbose);
			hi = EndianReaders.readS8(device, i2caddr, register + 1, verbose);
		}
		return ((hi << 8) + lo); // & 0xFFFF;
	}

	public static int readS16LE(I2CDevice device, int i2caddr, int register, boolean verbose) throws IOException {
		return EndianReaders.readS16(device, i2caddr, register, Endianness.LITTLE_ENDIAN, verbose);
	}

	public static int readS16BE(I2CDevice device, int i2caddr, int register, boolean verbose) throws IOException {
		return EndianReaders.readS16(device, i2caddr, register, Endianness.BIG_ENDIAN, verbose);
	}
}
