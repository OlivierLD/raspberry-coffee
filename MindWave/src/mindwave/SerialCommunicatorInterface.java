package mindwave;

public interface SerialCommunicatorInterface {
	boolean isSerialOpen();
	void writeSerial(byte b);
	void flushSerial();
}
