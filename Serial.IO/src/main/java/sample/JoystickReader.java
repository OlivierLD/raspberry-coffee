package sample;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Not Serial, ByteArrayOutputStream
 */
public class JoystickReader {
	private final static String JOYSTICK_INPUT = "/dev/input/js0";
	private final static int BUFFER_SIZE = 16_384;

	public static void main(String... args) {
		try (DataInputStream joystick = new DataInputStream(new FileInputStream(JOYSTICK_INPUT))) {
			byte[] data = new byte[BUFFER_SIZE];
			int nb;
			while ((nb = joystick.read(data, 0, data.length)) != -1) {
				for (int i=0; i<nb; i++) {
					System.out.println(String.format("0x%02X", data[i] & 0xFF));
				}
			}
			System.out.println("Done!");
		} catch (FileNotFoundException fnfe) {
			System.err.println("Ooops!");
			fnfe.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}
}
