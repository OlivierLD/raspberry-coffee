package paddle.buttons;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.Pin;

import java.util.function.Consumer;

/**
 * This shows how to hide the "complexity" of the PushButtonMaster
 * Just implement the onButtonPressed method, and you're good.
 *
 * Scrolls through an array of options every time the button is pressed
 */
public class PushButtonInstance implements PushButtonObserver {

	private static PushButtonMaster buttonMaster = null;
	private String name = "ButtonName";
	private Consumer<ButtonEvent> eventConsumer;

	public PushButtonInstance(GpioController gpio, Pin pin, Consumer<ButtonEvent> eventConsumer) {
		this(gpio, pin, null, eventConsumer);
	}
	public PushButtonInstance(GpioController gpio, Pin pin, String name, Consumer<ButtonEvent> eventConsumer) {
		if (name != null) {
			this.name = name;
		}
		buttonMaster = new PushButtonMaster(this);
		this.eventConsumer = eventConsumer;
		buttonMaster.initCtx(gpio, pin);  // Can override default pin
	}

	@Override
	public void onButtonPressed() {
		this.eventConsumer.accept(new ButtonEvent(ButtonEvent.EventType.PUSHED, String.format("Button [%s] down", this.name)));
	}

	@Override
	public void onButtonReleased() {
		this.eventConsumer.accept(new ButtonEvent(ButtonEvent.EventType.RELEASED, String.format("Button [%s] released", this.name)));
	}

	public static class ButtonEvent {
		private String payload;
		private EventType type;

		public enum EventType {
			PUSHED,
			RELEASED
		};
		public ButtonEvent(EventType eventType, String payload) {
			this.type = eventType;
			this.payload = payload;
		}
		public String getPayload() {
			return this.payload;
		}
		public EventType getEventType() {
			return this.type;
		}
	}
}
