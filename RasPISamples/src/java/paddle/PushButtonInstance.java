package paddle;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.RaspiPin;
import java.util.function.Consumer;
import paddle.JoyBonnet.ButtonEvent;
import pushbutton.PushButtonObserver;

/**
 * This shows how to hide the "complexity" of the PushButtonMaster
 * Just implement the onButtonPressed method, and you're good.
 *
 * Scrolls through an array of options every time the button is pressed
 */
public class PushButtonInstance implements PushButtonObserver {

	private static PushButtonMaster rgm = null;
	private Consumer<ButtonEvent> eventConsumer;

	public PushButtonInstance(GpioController gpio, Pin pin, Consumer<ButtonEvent> eventConsumer) {
		rgm = new PushButtonMaster(this);
		this.eventConsumer = eventConsumer;
		rgm.initCtx(gpio, pin);  // Can override default pin
	}

	@Override
	public void onButtonPressed() {
		this.eventConsumer.accept(new ButtonEvent());
	}

}
