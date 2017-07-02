package nmea.forwarders;

import context.ApplicationContext;
import context.NMEADataCache;

import java.util.Properties;

/**
 * This is a skeleton for a {@link Forwarder}, or for a Transformer
 * <br>
 * It can be loaded dynamically. As such, it can be set only from the properties file
 * used at startup. It - for now - cannot be managed from the Web UI.
 * The REST api is not aware of it.
 * <br>
 * To load it, use the properties file at startup:
 * <pre>
 *   forward.XX.cls=nmea.forwarders.ProcessorSkeleton
 * </pre>
 * A jar containing this class and its dependencies must be available in the classpath.
 */
public class ProcessorSkeleton implements Forwarder {
	private boolean keepWorking = true;

	/*
	 * @throws Exception
	 */
	public ProcessorSkeleton() throws Exception {
		// Make sure the cache has been initialized.
		if (ApplicationContext.getInstance().getDataCache() == null) {
			throw new RuntimeException("Init the Cache first. See the properties file used at startup."); // Oops
		}
		// This part is not mandatory, but can be useful
		Thread cacheThread = new Thread("ProcessorSkeleton CacheThread") {
			public void run() {
				while (keepWorking) { // Ping the cache every second.
					NMEADataCache cache = ApplicationContext.getInstance().getDataCache();
					try { Thread.sleep(1_000L); } catch (Exception ex) {}
				}
			}
		};
		cacheThread.start();
	}

	@Override
	public void write(byte[] message) {
		// TODO: Something smart here
	}

	@Override
	public void close() {
		System.out.println("- Stop writing to " + this.getClass().getName());
		try {
			// Stop Cache thread
			keepWorking = false;
			try { Thread.sleep(2_000L); } catch (Exception ex) {}
			// TODO: Shutdown whatever has to be shut down here.
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	public static class SkeletonBean {
		private String cls;
		private String type = "skeleton";

		public SkeletonBean(ProcessorSkeleton instance) {
			cls = instance.getClass().getName();
		}
	}

	@Override
	public Object getBean() {
		return new SkeletonBean(this);
	}

	@Override
	public void setProperties(Properties props) {
		// Optional Properties file mention in the forwarder definition would be read here
		// forward.XX.properties=my-forwarder.properties
	}
}
