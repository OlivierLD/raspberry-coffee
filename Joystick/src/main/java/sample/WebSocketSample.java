package sample;

import com.google.gson.Gson;
// import com.google.gson.GsonBuilder;

import joystick.JoystickReaderV2;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import static joystick.JoystickReaderV2.JOYSTICK_DOWN;
import static joystick.JoystickReaderV2.JOYSTICK_INPUT_0;
import static joystick.JoystickReaderV2.JOYSTICK_LEFT;
import static joystick.JoystickReaderV2.JOYSTICK_RIGHT;
import static joystick.JoystickReaderV2.JOYSTICK_UP;

public class WebSocketSample {

	private static class JoystickMessage {
		boolean up = false;
		boolean down = false;
		boolean left = false;
		boolean right = false;
	}

	public static void main(String... args) throws Exception {

		final WebSocketFeeder wsFeeder = new WebSocketFeeder();

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
			JoystickMessage joystickMessage = new JoystickMessage();
			// Synthesis
			String status = "";
			if (up.get()) {
				status += "Up ";
				joystickMessage.up = true;
			}
			if (down.get()) {
				status += "Down ";
				joystickMessage.down = true;
			}
			if (left.get()) {
				status += "Left ";
				joystickMessage.left = true;
			}
			if (right.get()) {
				status += "Right ";
				joystickMessage.right = true;
			}
			if (status.length() == 0) {
				status = "Center";
			}
			System.out.println(String.format("Joystick status: %s", status));
			String content = new Gson().toJson(joystickMessage);
			wsFeeder.pushMessage(content);
		};

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			wsFeeder.shutdown();
		}, "Shutdown Hook"));

		/* JoystickReaderV2 joystickReader = */ new JoystickReaderV2(JOYSTICK_INPUT_0, callback);
	}

}
