package client;

import compute.Task;
import java.io.Serializable;
import java.math.BigDecimal;

public class Speak implements Task<Boolean>, Serializable {

	private static final long serialVersionUID = 227L;

	private final String textToSpeak;
	/**
	 * Construct a task to speak.
	 */
	public Speak(String text) {
		this.textToSpeak = text;
	}

	/**
	 * Speak!
	 *
	 */
	public Boolean execute() {
		System.out.println("Server speaking " + this.textToSpeak);
		try
		{
			SpeechTools.speak(this.textToSpeak);
		} catch (Exception ex) {
			return false;
		}
		return true;
	}
}
