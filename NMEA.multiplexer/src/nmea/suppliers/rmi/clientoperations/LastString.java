package nmea.suppliers.rmi.clientoperations;

import nmea.suppliers.rmi.Task;

import java.io.Serializable;

/**
 * Task invokable by the client.
 * Runs (executes) on the server.
 */
public class LastString implements Task<String>, Serializable {

	private static final long serialVersionUID = 227L;
	private String lastString = "";

	public LastString() {
	}

	public void setLastString(String str) {
		this.lastString = str;
	}

	public String execute() {
		return this.lastString;
	}
}
