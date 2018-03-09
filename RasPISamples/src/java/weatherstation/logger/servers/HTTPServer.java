package weatherstation.logger.servers;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class HTTPServer {
	private boolean verbose = "true".equals(System.getProperty("http.verbose", "false"));
	private String data;
	private int _port = 0;

	public HTTPServer() throws Exception {
		// Bind the server
		String machineName = "localhost";
		String port = "6666";

		machineName = System.getProperty("http.host", machineName);
		port = System.getProperty("http.port", port);

		System.out.println("HTTP Host:" + machineName);
		System.out.println("HTTP Port:" + port);

		JSONObject dummyPayload = new JSONObject( // Free sample
				"{ \"dir\": 350.0, \n" +
						"  \"avgdir\": 345.67,\n" +
						"  \"volts\": 3.4567,\n" +
						"  \"speed\": 12.345,\n" +
						"  \"gust\": 13.456,\n" +
						"  \"rain\": 0.1,\n" +
						"  \"press\": 101300.00,\n" +
						"  \"temp\": 18.34,\n" +
						"  \"hum\": 58.5,\n" +
						"  \"cputemp\": 34.56 }");
		this.data = dummyPayload.toString();

		try {
			_port = Integer.parseInt(port);
		} catch (NumberFormatException nfe) {
			throw nfe;
		}

		// Infinite loop, waiting for requests
		Thread httpListenerThread = new Thread("HTTPListener") {
			public void run() {
				boolean go = true;
				try {
					Map<String, String> header = new HashMap<>();
					ServerSocket ss = new ServerSocket(_port);
					System.out.println("Port " + _port + " opened successfully.");
					while (go) {
						Socket client = ss.accept();
						BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
						PrintWriter out = new PrintWriter(new OutputStreamWriter(client.getOutputStream()));
						String line;
						while ((line = in.readLine()) != null) {
							if (verbose) {
								System.out.println(">>> HTTP Request:" + line);
							}
							if (line.length() == 0) {
								break;
							} else if (line.startsWith("POST /exit") || line.startsWith("GET /exit")) {
								System.out.println("Received an exit signal");
								go = false;
							} else if (line.startsWith("POST / ") || line.startsWith("GET / ") || line.startsWith("POST /all") || line.startsWith("GET /all")) {
								// Usual case, reply
								System.out.println("Received data request");
							}
							//          System.out.println("Read:[" + line + "]");
							if (line.indexOf(":") > -1) { // Header?
								String headerKey = line.substring(0, line.indexOf(":"));
								String headerValue = line.substring(line.indexOf(":") + 1);
								header.put(headerKey, headerValue);
							}
						}
						String contentType = "text/plain";
						String content = "exit";
						if (go) {
							contentType = "application/json";
							content = (generateContent());
						}
						if (content.length() > 0) {
							// Headers?
							out.print("HTTP/1.1 200 \r\n");
							out.print("Content-Type: " + contentType + "\r\n");
							out.print("Content-Length: " + content.length() + "\r\n");
							out.print("Access-Control-Allow-Origin: *\r\n");
							out.print("\r\n"); // End Of Header
							//
							out.println(content);
							if (verbose) {
								System.out.println(">>> HTTP Server, content [" + content + "]");
							}
						}
						out.flush();
						out.close();
						in.close();
						client.close();
						if (verbose) {
							System.out.println(">>> HTTP Server, loop bottom");
						}
					}
					ss.close();
				} catch (Exception e) {
					System.err.println(">>> Port " + _port + ", " + e.toString() + " >>>");
					e.printStackTrace();
					System.err.println("<<< Port " + _port + " <<<");
				} finally {
					System.out.println("HTTP Server is done.");
				}
			}
		};
		httpListenerThread.start();
	}

	public void setData(String str) {
		if (verbose) {
			System.out.println("HTTP Logger, setData:" + str);
		}
		this.data = str;
	}

	private String generateContent() {
		String str = this.data;
		if (verbose) {
			System.out.println(str);
		}
		return str;
	}

	//  For dev tests
	public static void main(String... args) throws Exception {
		//System.setProperty("http.port", "9999");
		new HTTPServer();
		Thread t = new Thread("HTTPWaiter") {
			public void run() {
				synchronized (this) {
					try {
						this.wait();
					} catch (Exception ex) {
					}
				}
			}
		};
		t.start();
	}
}
