package utils;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;

/**
 * Generate sample binary data file, like the joystick output.
 * Useful when not on a Pi, or when there is no joystick.
 */
public class Utilities {
	public static void main(String... args) {
		byte[] ba = new byte[] {
				(byte)0xE6, (byte)0x7F, (byte)0x71, (byte)0x00, (byte)0x01, (byte)0x80, (byte)0x02, (byte)0x01,
				(byte)0x06, (byte)0x88, (byte)0x71, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x02, (byte)0x01,
				(byte)0xB0, (byte)0xCE, (byte)0x71, (byte)0x00, (byte)0xFF, (byte)0x7F, (byte)0x02, (byte)0x01,
				(byte)0xD8, (byte)0xD8, (byte)0x71, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x02, (byte)0x01,
				(byte)0x78, (byte)0xF7, (byte)0x71, (byte)0x00, (byte)0xFF, (byte)0x7F, (byte)0x02, (byte)0x00,
				(byte)0xDC, (byte)0x06, (byte)0x72, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x02, (byte)0x00
		};

		try {
			File file = new File("sample.data.dat");
			DataOutputStream dos = new DataOutputStream(new FileOutputStream(file));
			dos.write(ba, 0, ba.length);
			dos.flush();
			dos.close();
			System.out.println("Created " + file.getAbsolutePath());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
