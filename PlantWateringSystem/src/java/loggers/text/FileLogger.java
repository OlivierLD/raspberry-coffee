package loggers.text;

import com.google.gson.JsonObject;
import http.client.HTTPClient;
import loggers.DataLoggerInterface;
import loggers.LogData;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import static utils.StaticUtil.userInput;

/**
 * Generate a log file on the file system. CSV
 * Provide file name in -Dlogger.file.name
 * Default is "logger.log"
 */
public class FileLogger implements DataLoggerInterface {

	private static boolean DEBUG = "true".equals(System.getProperty("file.logger.verbose"));

	private String fileName = "";
	private BufferedWriter logFile = null;

	Map<String, Double> dataMap = null;

	@Override
	public void accept(LogData feedData) {
		String data = feedData.feed().value();
		double value = feedData.value();

		if (DEBUG) {
			System.out.println(String.format("Received %s: %.02f", data, value));
		}
		try {
			if (dataMap == null) {
				dataMap = new HashMap<>(2);
			}
			dataMap.put(data, value);
			if (dataMap.size() == 2) {
				String line = String.format("%d;%s;%s",
						System.currentTimeMillis(),
						String.valueOf(dataMap.get(LogData.FEEDS.HUM.value())),
						String.valueOf(dataMap.get(LogData.FEEDS.AIR.value())));
				if (logFile != null) {
					logFile.write(line + "\n");
					logFile.flush();
				}
				dataMap = null; // Reset
			}
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	@Override
	public void close() {
		if (DEBUG) {
			System.out.println(String.format("Closing logger [%s]", this.getClass().getName()));
		}
		if (logFile != null) {
			try {
				logFile.close();
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
	}

	public FileLogger() {
		if (DEBUG) {
			System.out.println(String.format("Creating logger [%s]", this.getClass().getName()));
		}
		this.fileName = System.getProperty("logger.file.name", "logger.log");
		try {
			logFile = new BufferedWriter(new FileWriter(this.fileName));
			logFile.write("epoch;hum;temp\n"); // Header
			logFile.flush();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

}
