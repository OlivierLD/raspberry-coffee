package nmea.api;

/**
 * Anything that can happen during the NMEA Stream reading
 *
 * @author Olivier Le Diouris
 * @version 1.0
 */
public class NMEAException extends Exception {
	public NMEAException() {
		super();
	}

	public NMEAException(String s) {
		super(s);
	}
}