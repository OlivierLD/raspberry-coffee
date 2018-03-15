package breadboard.button;

import pushbutton.PushButtonObserver;

import static utils.StaticUtil.userInput;

/**
 * This shows how to hide the "complexity" of the PushButtonMaster
 * Just implement the onButtonPressed method, and you're good.
 *
 * Scrolls through an array of options every time the button is pressed
 */
public class PushButtonInstance implements PushButtonObserver {

	private static PushButtonMaster rgm = null;

	private final static String[] OPTION_ARRAY = {
					"Option One",
					"Option Two",
					"Option Three"
	};

	public static void main(String... args) {
		System.out.println("Enter Q (followed by [Return]) in the console to quit.");
		PushButtonInstance instance = new PushButtonInstance();
		rgm = new PushButtonMaster(instance);
		rgm.initCtx();                  // Initialize
//  rgm.initCtx(RaspiPin.GPIO_01);  // Can override default pin
		System.out.println("Ready!");
		stateOption();
		// while keep working...
		boolean keepWorking = true;
		while (keepWorking) {
			String what = userInput("");
			if ("Q".equalsIgnoreCase(what.trim())) {
				keepWorking = false;
				System.out.println("Bye...");
			}
		}
		rgm.freeResources();                             // Free and exit
	}

	private static int currentOption = 0;
	@Override
	public void onButtonPressed() {
		currentOption++;
		if (currentOption >= OPTION_ARRAY.length) {
			currentOption = 0;
		}
		stateOption();
	}

	@Override
	public void onButtonReleased() {
	}

	private static void stateOption() {
		System.out.println(String.format("Current Option is %s", OPTION_ARRAY[currentOption]));
	}
}
