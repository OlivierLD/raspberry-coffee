package compute;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Compute Engine
 */
public interface Compute extends Remote {
	<T> T executeTask(Task<T> t) throws RemoteException;
}