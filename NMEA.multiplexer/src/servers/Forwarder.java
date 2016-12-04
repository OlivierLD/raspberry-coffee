package servers;

public interface Forwarder {
	void write(byte[] mess);

	void close();

	Object getBean();
}
