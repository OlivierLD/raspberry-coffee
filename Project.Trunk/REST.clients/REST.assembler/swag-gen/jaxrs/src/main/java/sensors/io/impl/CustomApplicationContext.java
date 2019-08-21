package sensors.io.impl;

public class CustomApplicationContext {

	private CustomApplicationContext() {
	}

	private static CustomApplicationContext instance;
	public static CustomApplicationContext getInstance() {
		if (instance == null) {
			System.out.println("Instantiating the CustomApplicationContext.");
			instance = new CustomApplicationContext();
		}
		return instance;
	}

	private int nbAccess = 0;

	public int getNbAccess() {
		return nbAccess;
	}

	public void setNbAccess(int nbAccess) {
		this.nbAccess = nbAccess;
	}
}
