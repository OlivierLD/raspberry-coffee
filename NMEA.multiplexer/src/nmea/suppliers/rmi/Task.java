package nmea.suppliers.rmi;

public interface Task<T> {
	T execute();
}