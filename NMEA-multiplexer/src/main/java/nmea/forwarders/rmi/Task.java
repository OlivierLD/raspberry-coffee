package nmea.forwarders.rmi;

public interface Task<T> {
	T execute();
}