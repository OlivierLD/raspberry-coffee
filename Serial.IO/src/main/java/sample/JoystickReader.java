package sample;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Not Serial, ByteArrayInputStream
 */
public class JoystickReader {
	private final static String JOYSTICK_INPUT = "/dev/input/js0";
	private final static int BUFFER_SIZE = 16_384;
	private final static int MAX_DISPLAY_LEN = 32;

	public static void main(String... args) {
		try (DataInputStream joystick = new DataInputStream(new FileInputStream(JOYSTICK_INPUT))) {
			byte[] data = new byte[BUFFER_SIZE];
			List<Byte> byteStream = new ArrayList<>();
			int nb;
			while ((nb = joystick.read(data, 0, data.length)) != -1) {
				for (int i=0; i<nb; i++) {
					byteStream.add(data[i]);
					while (byteStream.size() > MAX_DISPLAY_LEN) {
						byteStream.remove(0);
					}
					String faisVoir = byteStream.stream()
							.map(b -> String.format("%02X", (b & 0xFF)))
							.collect(Collectors.joining(" "));
					System.out.println(faisVoir);
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
