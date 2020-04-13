package rmi.sample;

import java.rmi.*;

public interface ServerInterface extends Remote {
	String execute(String x) throws RemoteException;
}
