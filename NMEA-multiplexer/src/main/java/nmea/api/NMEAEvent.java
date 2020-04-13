package nmea.api;

import java.util.EventObject;

/**
 * Manages the kind of events that happen during the reading process
 *
 * @author Olivier Le Diouris
 * @version 1.0
 */
public class NMEAEvent extends EventObject {
	private String content = "";

	/*
	 * To be used for the stop statement
	 */
	public NMEAEvent(Object source) {
		super(source);
	}

	/*
	 * To be used for the DataRead and DataDetected events
	 */
	public NMEAEvent(Object source, String s) {
		super(source);
		content = s;
	}

	/*
	 * Once DataRead or DataDetected has been trapped, use this method to
	 * get the concerned data String.
	 *
	 * @return the concerned data, as a String
	 */
	public String getContent() {
		return this.content;
	}
}