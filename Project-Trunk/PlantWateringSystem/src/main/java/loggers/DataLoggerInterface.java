package loggers;

import java.util.function.Consumer;

public interface DataLoggerInterface extends Consumer<LogData> {
	void close();
}
