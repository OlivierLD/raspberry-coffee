package engine;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.URLEncoder;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import compute.Compute;
import compute.Task;

public class ComputeEngine implements Serializable, Compute {

	private Registry registry     = null;
	private int registryPort      = 1099;
	private Thread me = null;

	public ComputeEngine() {
		super();
	}

	@Override
	public <T> T executeTask(Task<T> t) {
		return t.execute();
	}

	public void startServer() {
		try {
			registry = LocateRegistry.createRegistry(registryPort);
			System.out.println("Registry started");
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		String name = "Compute";
//		try {
//			name = "rmi://" + InetAddress.getLocalHost().getHostName() /* "localhost" */ + ":" +
//							Integer.toString(registryPort) + "/" +
//							URLEncoder.encode("compute", "UTF-8"); // "compute", path in the url
//		} catch (Exception ex) {
//			ex.printStackTrace();
//		}
//  System.setSecurityManager(new RMISecurityManager());
		try {
			Compute engine = new ComputeEngine();
			System.out.println("ComputeEngine created...");
			System.out.println("Registering as [" + name + "] ...");
			Naming.rebind(name, engine);
			System.out.println("Registered as [" + name + "] !");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void shutdown() {
		System.out.println("Shutting down");
		if (me != null) {
			synchronized (me) {
				me.notify();
			}
		} else {
			System.out.println("What ???????");
		}
	}

	private void cleanup() {
		try {
			System.out.println("Stopping registry");
			UnicastRemoteObject.unexportObject(registry, true);
			System.out.println("Stopped");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void stopServer() {
		System.out.println("[Server] Shutting down...");
		try {
			this.shutdown();
		} catch (Exception ex) { ex.printStackTrace(); }
	}

	private void waitForSignal() {
	  me = Thread.currentThread();
		synchronized (me) {
			try {
				System.out.println("Server waiting...");
				me.wait();
				System.out.println("Server received notification.");
			} catch (InterruptedException ie) {
				ie.printStackTrace();
			}
		}
	}

	public static void main(String[] args) {
		final ComputeEngine ce = new ComputeEngine();

    Runtime.getRuntime().addShutdownHook(new Thread() {
	    public void run() {
		    System.out.println("Server received SIGINT signal");
		    ce.stopServer();
		    try { Thread.sleep(1000L); }
		    catch (InterruptedException ie) {}
	    }
    });

		ce.startServer();

		ce.waitForSignal();

		System.out.println("Server exiting.");
		ce.cleanup();
		System.out.println("Server done");
	}
}