package joystick;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Warning: This is not serial port related!
 * Not Serial, ByteArrayInputStream
 */
public class JoystickReaderV2 {

	public final static String JOYSTICK_INPUT_0 = "/dev/input/js0";
	public final static String JOYSTICK_INPUT_1 = "/dev/input/js1";

	private static boolean DEBUG = "true".equals(System.getProperty("joystick.debug", "false"));

	private final static int BUFFER_SIZE = 16_384; // Should be big enough ;)
	private final static int MAX_DISPLAY_LEN = 32;

	public final static byte JOYSTICK_NONE  = 0x0;
	public final static byte JOYSTICK_LEFT  = 0x1;
	public final static byte JOYSTICK_RIGHT = 0x1 << 1;
	public final static byte JOYSTICK_UP    = 0x1 << 2;
	public final static byte JOYSTICK_DOWN  = 0x1 << 3;

	private Consumer<Byte> statusCallback;
	private String joystickInput;

	public JoystickReaderV2(String joystickInput) {
		this(joystickInput, null);
	}
	public JoystickReaderV2(String joystickInput, Consumer<Byte> statusCallback) {
		this.joystickInput = joystickInput;
		this.statusCallback = statusCallback;

		try (DataInputStream joystick = new DataInputStream(new FileInputStream(this.joystickInput))) { // Auto-close
			byte[] data = new byte[BUFFER_SIZE];
			List<Byte> byteStream = new ArrayList<>();
			int nb;
			long lastReadTime = System.currentTimeMillis();
			while ((nb = joystick.read(data, 0, data.length)) != -1) {
				long now = System.currentTimeMillis();
				if (now - lastReadTime > 1_000) { // New line, reset buffer
					byteStream.clear();
				}
				lastReadTime = now;
				for (int i=0; i<nb; i++) {
					byteStream.add(data[i]);
					while (byteStream.size() > MAX_DISPLAY_LEN) {
						byteStream.remove(0);
					}
					if (byteStream.size() != 0 && byteStream.size() % 8 == 0) {
						String dump = byteStream.stream() // The 8 bytes
								.map(b -> String.format("%02X", (b & 0xFF)))
								.collect(Collectors.joining(" "));

						byte status = JOYSTICK_NONE;
						if (byteStream.get(5) == (byte)0x80) {
							if (byteStream.get(7) == 0x00) {
								// pos = "Down";
								status = JOYSTICK_DOWN;
							} else if (byteStream.get(7) == 0x01) {
								// pos = "Left";
								status = JOYSTICK_LEFT;
							}
						} else if (byteStream.get(5) == (byte)0x7F) {
							if (byteStream.get(7) == 0x00) {
								// pos = "Up";
								status = JOYSTICK_UP;
							} else if (byteStream.get(7) == 0x01) {
								// pos = "Right";
								status = JOYSTICK_RIGHT;
							}
						}
						if (this.statusCallback != null) {
							this.statusCallback.accept(status);
						}
						if (this.statusCallback == null || DEBUG) {
							System.out.println(String.format("\t%s, [0x%02X 0x%02X] => 0b%s",
									dump,
									byteStream.get(5), byteStream.get(7), // 5 & 7
									String.format("%4s",
											Integer.toBinaryString(status & 0xFF)).replace(' ', '0')));
						}
						byteStream.clear();
					}
				}
			}
			System.out.println("Done!");
		} catch (FileNotFoundException fnfe) {
			System.err.println("Ooops!");
			fnfe.printStackTrace();
		} catch (IOException ioe) {
			System.err.println("Argh!");
			ioe.printStackTrace();
		}
	}

	public static void main(String... args) {
		final AtomicBoolean
				up = new AtomicBoolean(false),
				down = new AtomicBoolean(false),
				right = new AtomicBoolean(false),
				left = new AtomicBoolean(false);
		Consumer<Byte> callback = (b) -> {
			if (b == 0x0) {
				up.set(false);
				down.set(false);
				right.set(false);
				left.set(false);
			} else if ((b.byteValue() & JOYSTICK_LEFT) == JOYSTICK_LEFT) {
				left.set(true);
			} else if ((b.byteValue() & JOYSTICK_RIGHT) == JOYSTICK_RIGHT) {
				right.set(true);
			} else if ((b.byteValue() & JOYSTICK_UP) == JOYSTICK_UP) {
				up.set(true);
			} else if ((b.byteValue() & JOYSTICK_DOWN) == JOYSTICK_DOWN) {
				down.set(true);
			}
			// Synthesis
			String status = "";
			if (up.get()) {
				status += "Up ";
			}
			if (down.get()) {
				status += "Down ";
			}
			if (left.get()) {
				status += "Left ";
			}
			if (right.get()) {
				status += "Right ";
			}
			if (status.length() == 0) {
				status = "Center";
			}
			System.out.println(String.format("Joystick status: %s", status));
		};

		/* JoystickReaderV2 joystickReader = */ new JoystickReaderV2(JOYSTICK_INPUT_0, callback);
	}
}
