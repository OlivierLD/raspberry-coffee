package nmea.forwarders;

import java.util.Properties;

public class ConsoleWriter implements Forwarder {
	private Properties props = null;

	public ConsoleWriter() throws Exception {
	}

	@Override
	public void write(byte[] message) {
		String mess = new String(message);
		if (!mess.isEmpty()) {
			System.out.println(mess.trim()); // That is what this Forwarder does.
		}
	}

	@Override
	public void close() {
		System.out.println("- Stop writing to the console. (" + this.getClass().getName() + ")");
	}

	private static class ConsoleBean {
		private String cls;
		private String type = "console";

		public ConsoleBean(ConsoleWriter instance) {
			cls = instance.getClass().getName();
		}
	}

	@Override
	public Object getBean() {
		return new ConsoleBean(this);
	}

	@Override
	public void setProperties(Properties props) {
		this.props = props;
	}
}
