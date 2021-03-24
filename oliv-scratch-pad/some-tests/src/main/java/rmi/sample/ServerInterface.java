package rmi.sample;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ServerInterface extends Remote {
	String execute(String x) throws RemoteException;
}
