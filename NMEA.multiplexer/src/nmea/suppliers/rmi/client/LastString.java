package nmea.suppliers.rmi.client;

import nmea.suppliers.rmi.Task;

import java.io.Serializable;

/**
 * Invokable by the client
 */
public class LastString implements Task<String>, Serializable {

	private static final long serialVersionUID = 227L;


	public LastString() {
	}

	public String execute() {
		return "Duh";
	}
}
