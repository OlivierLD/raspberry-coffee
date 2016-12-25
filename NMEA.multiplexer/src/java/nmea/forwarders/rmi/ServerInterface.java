package nmea.forwarders.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ServerInterface extends Remote {
	<T> T executeTask(Task<T> t) throws RemoteException;
}
