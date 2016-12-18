package nmea.mux;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import computers.Computer;
import computers.ExtraDataComputer;
import context.ApplicationContext;
import gnu.io.CommPortIdentifier;
import http.HTTPServer;
import http.HTTPServerInterface;
import http.RESTProcessorUtil;
import http.utils.DumpUtil;
import nmea.api.Multiplexer;
import nmea.api.NMEAClient;
import nmea.api.NMEAParser;
import nmea.providers.client.BME280Client;
import nmea.providers.client.DataFileClient;
import nmea.providers.client.HTU21DFClient;
import nmea.providers.client.RandomClient;
import nmea.providers.client.SerialClient;
import nmea.providers.client.TCPClient;
import nmea.providers.client.WebSocketClient;
import nmea.providers.reader.BME280Reader;
import nmea.providers.reader.DataFileReader;
import nmea.providers.reader.HTU21DFReader;
import nmea.providers.reader.RandomReader;
import nmea.providers.reader.SerialReader;
import nmea.providers.reader.TCPReader;
import nmea.providers.reader.WebSocketReader;
import nmea.forwarders.ConsoleWriter;
import nmea.forwarders.DataFileWriter;
import nmea.forwarders.Forwarder;
import nmea.forwarders.TCPWriter;
import nmea.forwarders.WebSocketWriter;
import nmea.forwarders.rmi.RMIServer;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Function;
import java.util.stream.Collectors;

public class GenericNMEAMultiplexer implements Multiplexer, HTTPServerInterface {
	private HTTPServer adminServer = null;

	private List<NMEAClient> nmeaDataProviders = new ArrayList<>();
	private List<Forwarder> nmeaDataForwarders = new ArrayList<>();
	private List<Computer> nmeaDataComputers = new ArrayList<>();

	private static class Operation {
		String verb;
		String path;
		String description;
		Function<HTTPServer.Request, HTTPServer.Response> fn;

		public Operation(String verb, String path, Function<HTTPServer.Request, HTTPServer.Response> fn, String description) {
			this.verb = verb;
			this.path = path;
			this.description = description;
			this.fn = fn;
		}

		public String getVerb() {
			return verb;
		}

		public String getPath() {
			return path;
		}

		public String getDescription() {
			return description;
		}

		public Function<HTTPServer.Request, HTTPServer.Response> getFn() {
			return fn;
		}
	}

	/**
	 * Define all the REST operations to be managed
	 * by the HTTP server.
	 * <p>
	 * See {@link #processRequest(HTTPServer.Request, HTTPServer.Response)}
	 * See {@link HTTPServer}
	 */
	List<Operation> operations = Arrays.asList(
					new Operation(
									"GET",
									"/oplist",
									this::getOperationList,
									"List of all available operations."),
					new Operation(
									"GET",
									"/serial-ports",
									this::getSerialPorts,
									"Get the list of the available serial ports."),
					new Operation(
									"GET",
									"/channels",
									this::getChannels,
									"Get the list of the input channels"),
					new Operation(
									"GET",
									"/forwarders",
									this::getForwarders,
									"Get the list of the output channels"),
					new Operation(
									"GET",
									"/computers",
									this::getComputers,
									"Get the list of the computers"),
					new Operation(
									"DELETE",
									"/forwarders/{id}",
									this::deleteForwarder,
									"Delete an output channel"),
					new Operation(
									"DELETE",
									"/channels/{id}",
									this::deleteChannel,
									"Delete an input channel"),
					new Operation(
									"DELETE",
									"/computers/{id}",
									this::deleteComputer,
									"Delete a computer"),
					new Operation(
									"POST",
									"/forwarders",
									this::postForwarder,
									"Creates an output channel"),
					new Operation(
									"POST",
									"/channels",
									this::postChannel,
									"Creates an input channel"),
					new Operation(
									"POST",
									"/computers",
									this::postComputer,
									"Creates computer"),
					new Operation(
									"PUT",
									"/channels",
									this::putChannel,
									"Update channel"),
					new Operation(
									"PUT",
									"/forwarders",
									this::putForwarder,
									"Update forwarder"),
					new Operation(
									"PUT",
									"/computers",
									this::putComputer,
									"Update computer"),
					new Operation(
									"PUT",
									"/mux-verbose/{pos}",
									this::putMuxVerbose,
									"Update Multiplexer verbose"));

	public HTTPServer.Response processRequest(HTTPServer.Request request, HTTPServer.Response defaultResponse) {
		Optional<Operation> opOp = operations
						.stream()
						.filter(op -> op.getVerb().equals(request.getVerb()) && RESTProcessorUtil.pathMatches(op.getPath(), request.getPath()))
						.findFirst();
		if (opOp.isPresent()) {
			Operation op = opOp.get();
			request.setRequestPattern(op.getPath()); // To get the prms later on.
			HTTPServer.Response processed = op.getFn().apply(request);
			return processed;
		}
		return defaultResponse;
	}

	private HTTPServer.Response getSerialPorts(HTTPServer.Request request) {
		HTTPServer.Response response = new HTTPServer.Response(request.getProtocol(), 200);

		List<String> portList = getSerialPortList();
		Object[] portArray = portList.toArray(new Object[portList.size()]);
		String content = new Gson().toJson(portArray).toString();
		RESTProcessorUtil.generateHappyResponseHeaders(response, content.length());
		response.setPayload(content.getBytes());

		return response;
	}

	private HTTPServer.Response getChannels(HTTPServer.Request request) {
		HTTPServer.Response response = new HTTPServer.Response(request.getProtocol(), 200);

		List<Object> channelList = getInputChannelList();
		Object[] channelArray = channelList.stream()
						.collect(Collectors.toList())
						.toArray(new Object[channelList.size()]);

		String content = new Gson().toJson(channelArray);
		RESTProcessorUtil.generateHappyResponseHeaders(response, content.length());
		response.setPayload(content.getBytes());

		return response;
	}

	private HTTPServer.Response getForwarders(HTTPServer.Request request) {
		HTTPServer.Response response = new HTTPServer.Response(request.getProtocol(), 200);
		List<Object> forwarderList = getForwarderList();
		Object[] forwarderArray = forwarderList.stream()
						.collect(Collectors.toList())
						.toArray(new Object[forwarderList.size()]);

		String content = new Gson().toJson(forwarderArray);
		RESTProcessorUtil.generateHappyResponseHeaders(response, content.length());
		response.setPayload(content.getBytes());

		return response;
	}

	private HTTPServer.Response getComputers(HTTPServer.Request request) {
		HTTPServer.Response response = new HTTPServer.Response(request.getProtocol(), 200);
		List<Object> computerList = getComputerList();
		Object[] forwarderArray = computerList.stream()
						.collect(Collectors.toList())
						.toArray(new Object[computerList.size()]);

		String content = new Gson().toJson(forwarderArray);
		RESTProcessorUtil.generateHappyResponseHeaders(response, content.length());
		response.setPayload(content.getBytes());

		return response;
	}

	private HTTPServer.Response deleteForwarder(HTTPServer.Request request) {
		Optional<Forwarder> opFwd = null;
		Gson gson = null;
		HTTPServer.Response response = new HTTPServer.Response(request.getProtocol(), 204);
		List<String> prmValues = RESTProcessorUtil.getPrmValues(request.getRequestPattern(), request.getPath());
		if (prmValues.size() == 1) {
			String id = prmValues.get(0);
			switch (id) {
				case "console":
					opFwd = nmeaDataForwarders.stream()
									.filter(fwd -> fwd instanceof ConsoleWriter)
									.findFirst();
					response = removeForwarderIfPresent(request, opFwd);
					break;
				case "file":
					gson = new GsonBuilder().create();
					if (request.getContent() != null) {
						StringReader stringReader = new StringReader(new String(request.getContent()));
						DataFileWriter.DataFileBean dataFileBean = gson.fromJson(stringReader, DataFileWriter.DataFileBean.class);
						opFwd = nmeaDataForwarders.stream()
										.filter(fwd -> fwd instanceof DataFileWriter &&
														((DataFileWriter) fwd).getLog().equals(dataFileBean.getLog()))
										.findFirst();
						response = removeForwarderIfPresent(request, opFwd);
					} else {
						response.setStatus(400); 
						// Add message payload
						String content = new Gson().toJson(new RESTProcessorUtil.ErrorMessage("missing payload")).toString();
						RESTProcessorUtil.generateHappyResponseHeaders(response, content.length());
						response.setPayload(content.getBytes());
					}
					break;
				case "tcp":
					gson = new GsonBuilder().create();
					if (request.getContent() != null) {
						StringReader stringReader = new StringReader(new String(request.getContent()));
						TCPWriter.TCPBean tcpBean = gson.fromJson(stringReader, TCPWriter.TCPBean.class);
						opFwd = nmeaDataForwarders.stream()
										.filter(fwd -> fwd instanceof TCPWriter &&
														((TCPWriter) fwd).getTcpPort() == tcpBean.getPort())
										.findFirst();
						response = removeForwarderIfPresent(request, opFwd);
					} else {
						response.setStatus(400); 
						// Add message payload
						String content = new Gson().toJson(new RESTProcessorUtil.ErrorMessage("missing payload")).toString();
						RESTProcessorUtil.generateHappyResponseHeaders(response, content.length());
						response.setPayload(content.getBytes());
					}
					break;
				case "rmi":
					gson = new GsonBuilder().create();
					if (request.getContent() != null) {
						StringReader stringReader = new StringReader(new String(request.getContent()));
						RMIServer.RMIBean rmiBean = gson.fromJson(stringReader, RMIServer.RMIBean.class);
						opFwd = nmeaDataForwarders.stream()
										.filter(fwd -> fwd instanceof RMIServer &&
														((RMIServer) fwd).getRegistryPort() == rmiBean.getPort())
										.findFirst();
						response = removeForwarderIfPresent(request, opFwd);
					} else {
						response.setStatus(400); 
						// Add message payload
						String content = new Gson().toJson(new RESTProcessorUtil.ErrorMessage("missing payload")).toString();
						RESTProcessorUtil.generateHappyResponseHeaders(response, content.length());
						response.setPayload(content.getBytes());
					}
					break;
				case "ws":
					gson = new GsonBuilder().create();
					if (request.getContent() != null) {
						StringReader stringReader = new StringReader(new String(request.getContent()));
						WebSocketWriter.WSBean wsBean = gson.fromJson(stringReader, WebSocketWriter.WSBean.class);
						opFwd = nmeaDataForwarders.stream()
										.filter(fwd -> fwd instanceof WebSocketWriter &&
														((WebSocketWriter) fwd).getWsUri().equals(wsBean.getWsUri()))
										.findFirst();
						response = removeForwarderIfPresent(request, opFwd);
					} else {
						response.setStatus(400); 
						// Add message payload
						String content = new Gson().toJson(new RESTProcessorUtil.ErrorMessage("missing payload")).toString();
						RESTProcessorUtil.generateHappyResponseHeaders(response, content.length());
						response.setPayload(content.getBytes());
					}
					break;
				case "udp":
					response.setStatus(501); // Not implemented
					break;
				default:
					break;
			}
		} else {
			response.setStatus(400); // Bad request
			// Add message payload
			String content = new Gson().toJson(new RESTProcessorUtil.ErrorMessage("missing path parameter")).toString();
			RESTProcessorUtil.generateHappyResponseHeaders(response, content.length());
			response.setPayload(content.getBytes());
		}
		return response;
	}

	private HTTPServer.Response deleteChannel(HTTPServer.Request request) {
		Optional<NMEAClient> opClient = null;
		Gson gson = null;
		HTTPServer.Response response = new HTTPServer.Response(request.getProtocol(), 204);
		List<String> prmValues = RESTProcessorUtil.getPrmValues(request.getRequestPattern(), request.getPath());
		if (prmValues.size() == 1) {
			String id = prmValues.get(0);
			switch (id) {
				case "file":
					gson = new GsonBuilder().create();
					if (request.getContent() != null) {
						StringReader stringReader = new StringReader(new String(request.getContent()));
						DataFileClient.DataFileBean dataFileBean = gson.fromJson(stringReader, DataFileClient.DataFileBean.class);
						opClient = nmeaDataProviders.stream()
										.filter(channel -> channel instanceof DataFileClient &&
														((DataFileClient.DataFileBean) ((DataFileClient) channel).getBean()).getFile().equals(dataFileBean.getFile()))
										.findFirst();
						response = removeChannelIfPresent(request, opClient);
					} else {
						response.setStatus(400);
						// Add message payload
						String content = new Gson().toJson(new RESTProcessorUtil.ErrorMessage("missing payload")).toString();
						RESTProcessorUtil.generateHappyResponseHeaders(response, content.length());
						response.setPayload(content.getBytes());
					}
					break;
				case "serial":
					gson = new GsonBuilder().create();
					if (request.getContent() != null) {
						StringReader stringReader = new StringReader(new String(request.getContent()));
						SerialClient.SerialBean serialBean = gson.fromJson(stringReader, SerialClient.SerialBean.class);
						opClient = nmeaDataProviders.stream()
										.filter(channel -> channel instanceof SerialClient &&
														((SerialClient.SerialBean) ((SerialClient) channel).getBean()).getPort().equals(serialBean.getPort())) // No need for BaudRate
										.findFirst();
						response = removeChannelIfPresent(request, opClient);
					} else {
						response.setStatus(400);
						// Add message payload
						String content = new Gson().toJson(new RESTProcessorUtil.ErrorMessage("missing payload")).toString();
						RESTProcessorUtil.generateHappyResponseHeaders(response, content.length());
						response.setPayload(content.getBytes());
					}
					break;
				case "tcp":
					gson = new GsonBuilder().create();
					if (request.getContent() != null) {
						StringReader stringReader = new StringReader(new String(request.getContent()));
						TCPClient.TCPBean tcpBean = gson.fromJson(stringReader, TCPClient.TCPBean.class);
						opClient = nmeaDataProviders.stream()
										.filter(channel -> channel instanceof TCPClient &&
														((TCPClient.TCPBean) ((TCPClient) channel).getBean()).getPort() == tcpBean.getPort())
										.findFirst();
						response = removeChannelIfPresent(request, opClient);
					} else {
						response.setStatus(400);
						// Add message payload
						String content = new Gson().toJson(new RESTProcessorUtil.ErrorMessage("missing payload")).toString();
						RESTProcessorUtil.generateHappyResponseHeaders(response, content.length());
						response.setPayload(content.getBytes());
					}
					break;
				case "ws":
					gson = new GsonBuilder().create();
					if (request.getContent() != null) {
						StringReader stringReader = new StringReader(new String(request.getContent()));
						WebSocketClient.WSBean wsBean = gson.fromJson(stringReader, WebSocketClient.WSBean.class);
						opClient = nmeaDataProviders.stream()
										.filter(channel -> channel instanceof WebSocketClient &&
														((WebSocketClient.WSBean) ((WebSocketClient) channel).getBean()).getWsUri().equals(wsBean.getWsUri()))
										.findFirst();
						response = removeChannelIfPresent(request, opClient);
					} else {
						response.setStatus(400);
						// Add message payload
						String content = new Gson().toJson(new RESTProcessorUtil.ErrorMessage("missing payload")).toString();
						RESTProcessorUtil.generateHappyResponseHeaders(response, content.length());
						response.setPayload(content.getBytes());
					}
					break;
				case "bme280":
					opClient = nmeaDataProviders.stream()
									.filter(channel -> channel instanceof BME280Client)
									.findFirst();
					response = removeChannelIfPresent(request, opClient);
					break;
				case "htu21df":
					opClient = nmeaDataProviders.stream()
									.filter(channel -> channel instanceof HTU21DFClient)
									.findFirst();
					response = removeChannelIfPresent(request, opClient);
					break;
				case "rnd":
					opClient = nmeaDataProviders.stream()
									.filter(channel -> channel instanceof RandomClient)
									.findFirst();
					response = removeChannelIfPresent(request, opClient);
					break;
				default:
					response.setStatus(501);
					break;
			}
		} else {
			response.setStatus(400); // Bad request
			// Add message payload
			String content = new Gson().toJson(new RESTProcessorUtil.ErrorMessage("missing path parameter")).toString();
			RESTProcessorUtil.generateHappyResponseHeaders(response, content.length());
			response.setPayload(content.getBytes());
		}
		return response;
	}

	private HTTPServer.Response deleteComputer(HTTPServer.Request request) {
		Optional<Computer> opComputer = null;
		Gson gson = null;
		HTTPServer.Response response = new HTTPServer.Response(request.getProtocol(), 204);
		List<String> prmValues = RESTProcessorUtil.getPrmValues(request.getRequestPattern(), request.getPath());
		if (prmValues.size() == 1) {
			String id = prmValues.get(0);
			switch (id) {
				case "tw-current":
					gson = new GsonBuilder().create();
					if (request.getContent() != null) {  // Really? Need that?
						opComputer = nmeaDataComputers.stream()
										.filter(channel -> channel instanceof ExtraDataComputer)
										.findFirst();
						response = removeComputerIfPresent(request, opComputer);
					} else {
						response.setStatus(400);
						// Add message payload
						String content = new Gson().toJson(new RESTProcessorUtil.ErrorMessage("'tw-current' not found")).toString();
						RESTProcessorUtil.generateHappyResponseHeaders(response, content.length());
						response.setPayload(content.getBytes());
					}
					break;
				default:
					break;
			}
		} else {
			response.setStatus(400); // Bad request
			// Add message payload
			String content = new Gson().toJson(new RESTProcessorUtil.ErrorMessage("missing path parameter")).toString();
			RESTProcessorUtil.generateHappyResponseHeaders(response, content.length());
			response.setPayload(content.getBytes());
		}
		return response;
	}

	private HTTPServer.Response postForwarder(HTTPServer.Request request) {
		HTTPServer.Response response = new HTTPServer.Response(request.getProtocol(), 200);
		Optional<Forwarder> opFwd = null;
		String type = "";
		if (request.getContent() == null || request.getContent().length == 0) {
			response.setStatus(400); // No Content
			// Add message payload
			String content = new Gson().toJson(new RESTProcessorUtil.ErrorMessage("missing payload")).toString();
			RESTProcessorUtil.generateHappyResponseHeaders(response, content.length());
			response.setPayload(content.getBytes());
			return response;
		} else {
			Object bean = new GsonBuilder().create().fromJson(new String(request.getContent()), Object.class);
			if (bean instanceof Map) {
				type = ((Map<String, String>) bean).get("type");
			}
		}
		switch (type) {
			case "console":
				// Check existence
				opFwd = nmeaDataForwarders.stream()
								.filter(fwd -> fwd instanceof ConsoleWriter)
								.findFirst();
				if (!opFwd.isPresent()) {
					try {
						Forwarder consoleForwarder = new ConsoleWriter();
						nmeaDataForwarders.add(consoleForwarder);
						response = new HTTPServer.Response(request.getProtocol(), 200);
						String content = new Gson().toJson(consoleForwarder.getBean());
						RESTProcessorUtil.generateHappyResponseHeaders(response, content.length());
						response.setPayload(content.getBytes());
					} catch (Exception ex) {
						response.setStatus(400); 
						// Add message payload
						String content = new Gson().toJson(new RESTProcessorUtil.ErrorMessage(ex.toString())).toString();
						RESTProcessorUtil.generateHappyResponseHeaders(response, content.length());
						response.setPayload(content.getBytes());
						ex.printStackTrace();
					}
				} else {
					// Already there
					response.setStatus(400); 
					// Add message payload
					String content = new Gson().toJson(new RESTProcessorUtil.ErrorMessage("'console' already exists.")).toString();
					RESTProcessorUtil.generateHappyResponseHeaders(response, content.length());
					response.setPayload(content.getBytes());
				}
				break;
			case "tcp":
				TCPWriter.TCPBean tcpJson = new Gson().fromJson(new String(request.getContent()), TCPWriter.TCPBean.class);
				// Check if not there yet.
				opFwd = nmeaDataForwarders.stream()
								.filter(fwd -> fwd instanceof TCPWriter &&
												((TCPWriter) fwd).getTcpPort() == tcpJson.getPort())
								.findFirst();
				if (!opFwd.isPresent()) {
					try {
						Forwarder tcpForwarder = new TCPWriter(tcpJson.getPort());
						nmeaDataForwarders.add(tcpForwarder);
						String content = new Gson().toJson(tcpForwarder.getBean());
						RESTProcessorUtil.generateHappyResponseHeaders(response, content.length());
						response.setPayload(content.getBytes());
					} catch (Exception ex) {
						response.setStatus(400); 
						// Add message payload
						String content = new Gson().toJson(new RESTProcessorUtil.ErrorMessage(ex.toString())).toString();
						RESTProcessorUtil.generateHappyResponseHeaders(response, content.length());
						response.setPayload(content.getBytes());
						ex.printStackTrace();
					}
				} else {
					// Already there
					response.setStatus(400); 
					// Add message payload
					String content = new Gson().toJson(new RESTProcessorUtil.ErrorMessage("this 'tcp' already exists.")).toString();
					RESTProcessorUtil.generateHappyResponseHeaders(response, content.length());
					response.setPayload(content.getBytes());
				}
				break;
			case "rmi":
				RMIServer.RMIBean rmiJson = new Gson().fromJson(new String(request.getContent()), RMIServer.RMIBean.class);
				// Check if not there yet.
				opFwd = nmeaDataForwarders.stream()
								.filter(fwd -> fwd instanceof RMIServer &&
												((RMIServer) fwd).getRegistryPort() == rmiJson.getPort())
								.findFirst();
				if (!opFwd.isPresent()) {
					try {
						Forwarder rmiForwarder = new RMIServer(rmiJson.getPort(), rmiJson.getBindingName());
						nmeaDataForwarders.add(rmiForwarder);
						String content = new Gson().toJson(rmiForwarder.getBean());
						RESTProcessorUtil.generateHappyResponseHeaders(response, content.length());
						response.setPayload(content.getBytes());
					} catch (Exception ex) {
						response.setStatus(400); 
						// Add message payload
						String content = new Gson().toJson(new RESTProcessorUtil.ErrorMessage(ex.toString())).toString();
						RESTProcessorUtil.generateHappyResponseHeaders(response, content.length());
						response.setPayload(content.getBytes());
						ex.printStackTrace();
					}
				} else {
					// Already there
					response.setStatus(400); 
					// Add message payload
					String content = new Gson().toJson(new RESTProcessorUtil.ErrorMessage("this 'rmi' already exists.")).toString();
					RESTProcessorUtil.generateHappyResponseHeaders(response, content.length());
					response.setPayload(content.getBytes());
				}
				break;
			case "file":
				DataFileWriter.DataFileBean fileJson = new Gson().fromJson(new String(request.getContent()), DataFileWriter.DataFileBean.class);
				// Check if not there yet.
				opFwd = nmeaDataForwarders.stream()
								.filter(fwd -> fwd instanceof DataFileWriter &&
												((DataFileWriter) fwd).getLog().equals(fileJson.getLog()))
								.findFirst();
				if (!opFwd.isPresent()) {
					try {
						Forwarder fileForwarder = new DataFileWriter(fileJson.getLog());
						nmeaDataForwarders.add(fileForwarder);
						String content = new Gson().toJson(fileForwarder.getBean());
						RESTProcessorUtil.generateHappyResponseHeaders(response, content.length());
						response.setPayload(content.getBytes());
					} catch (Exception ex) {
						response.setStatus(400); 
						// Add message payload
						String content = new Gson().toJson(new RESTProcessorUtil.ErrorMessage(ex.toString())).toString();
						RESTProcessorUtil.generateHappyResponseHeaders(response, content.length());
						response.setPayload(content.getBytes());
						ex.printStackTrace();
					}
				} else {
					response.setStatus(400); 
					// Add message payload
					String content = new Gson().toJson(new RESTProcessorUtil.ErrorMessage("this 'file' already exists.")).toString();
					RESTProcessorUtil.generateHappyResponseHeaders(response, content.length());
					response.setPayload(content.getBytes());
				}
				break;
			case "ws":
				WebSocketWriter.WSBean wsJson = new Gson().fromJson(new String(request.getContent()), WebSocketWriter.WSBean.class);
				// Check if not there yet.
				opFwd = nmeaDataForwarders.stream()
								.filter(fwd -> fwd instanceof WebSocketWriter &&
												((WebSocketWriter) fwd).getWsUri() == wsJson.getWsUri())
								.findFirst();
				if (!opFwd.isPresent()) {
					try {
						Forwarder wsForwarder = new WebSocketWriter(wsJson.getWsUri());
						nmeaDataForwarders.add(wsForwarder);
						String content = new Gson().toJson(wsForwarder.getBean());
						RESTProcessorUtil.generateHappyResponseHeaders(response, content.length());
						response.setPayload(content.getBytes());
					} catch (Exception ex) {
						response.setStatus(400); 
						// Add message payload
						String content = new Gson().toJson(new RESTProcessorUtil.ErrorMessage(ex.toString())).toString();
						RESTProcessorUtil.generateHappyResponseHeaders(response, content.length());
						response.setPayload(content.getBytes());
						ex.printStackTrace();
					}
				} else {
					// Already there
					response.setStatus(400); 
					// Add message payload
					String content = new Gson().toJson(new RESTProcessorUtil.ErrorMessage("this 'ws' already exists.")).toString();
					RESTProcessorUtil.generateHappyResponseHeaders(response, content.length());
					response.setPayload(content.getBytes());
				}
				break;
			default:
				response.setStatus(501); // Not implemented
				// Add message payload
				String content = new Gson().toJson(new RESTProcessorUtil.ErrorMessage("'" + type + "' not implemented.")).toString();
				RESTProcessorUtil.generateHappyResponseHeaders(response, content.length());
				response.setPayload(content.getBytes());
				break;
		}
		return response;
	}

	private HTTPServer.Response postChannel(HTTPServer.Request request) {
		HTTPServer.Response response = new HTTPServer.Response(request.getProtocol(), 200);
		Optional<NMEAClient> opClient = null;
		String type = "";
		if (request.getContent() == null || request.getContent().length == 0) {
			response.setStatus(400); // No Content
			// Add message payload
			String content = new Gson().toJson(new RESTProcessorUtil.ErrorMessage("missing payload")).toString();
			RESTProcessorUtil.generateHappyResponseHeaders(response, content.length());
			response.setPayload(content.getBytes());
			return response;
		} else {
			Object bean = new GsonBuilder().create().fromJson(new String(request.getContent()), Object.class);
			if (bean instanceof Map) {
				type = ((Map<String, String>) bean).get("type");
			}
		}
		switch (type) {
			case "tcp":
				TCPClient.TCPBean tcpJson = new Gson().fromJson(new String(request.getContent()), TCPClient.TCPBean.class);
				opClient = nmeaDataProviders.stream()
								.filter(channel -> channel instanceof SerialClient &&
												((TCPClient.TCPBean) ((TCPClient) channel).getBean()).getPort() == tcpJson.getPort() &&
												((TCPClient.TCPBean) ((TCPClient) channel).getBean()).getHostname().equals(tcpJson.getHostname()))
								.findFirst();
				if (!opClient.isPresent()) {
					try {
						NMEAClient tcpClient = new TCPClient(this);
						tcpClient.initClient();
						tcpClient.setReader(new TCPReader(tcpClient.getListeners(), tcpJson.getHostname(), tcpJson.getPort()));
						nmeaDataProviders.add(tcpClient);
						tcpClient.startWorking();
						String content = new Gson().toJson(tcpClient.getBean());
						RESTProcessorUtil.generateHappyResponseHeaders(response, content.length());
						response.setPayload(content.getBytes());
					} catch (Exception ex) {
						response.setStatus(400); 
						// Add message payload
						String content = new Gson().toJson(new RESTProcessorUtil.ErrorMessage(ex.toString())).toString();
						RESTProcessorUtil.generateHappyResponseHeaders(response, content.length());
						response.setPayload(content.getBytes());
						ex.printStackTrace();
					}
				} else {
					// Already there
					response.setStatus(400); 
					// Add message payload
					String content = new Gson().toJson(new RESTProcessorUtil.ErrorMessage("this 'tcp' already exists")).toString();
					RESTProcessorUtil.generateHappyResponseHeaders(response, content.length());
					response.setPayload(content.getBytes());
				}
				break;
			case "serial":
				SerialClient.SerialBean serialJson = new Gson().fromJson(new String(request.getContent()), SerialClient.SerialBean.class);
				opClient = nmeaDataProviders.stream()
								.filter(channel -> channel instanceof SerialClient &&
												((SerialClient.SerialBean) ((SerialClient) channel).getBean()).getPort().equals(serialJson.getPort()))
								.findFirst();
				if (!opClient.isPresent()) {
					try {
						NMEAClient serialClient = new SerialClient(this);
						serialClient.initClient();
						serialClient.setReader(new SerialReader(serialClient.getListeners(), serialJson.getPort(), serialJson.getBr()));
						nmeaDataProviders.add(serialClient);
						serialClient.startWorking();
						String content = new Gson().toJson(serialClient.getBean());
						RESTProcessorUtil.generateHappyResponseHeaders(response, content.length());
						response.setPayload(content.getBytes());
					} catch (Exception ex) {
						response.setStatus(400); 
						// Add message payload
						String content = new Gson().toJson(new RESTProcessorUtil.ErrorMessage(ex.toString())).toString();
						RESTProcessorUtil.generateHappyResponseHeaders(response, content.length());
						response.setPayload(content.getBytes());
						ex.printStackTrace();
					}
				} else {
					// Already there
					response.setStatus(400); 
					// Add message payload
					String content = new Gson().toJson(new RESTProcessorUtil.ErrorMessage("this 'serial' already exists")).toString();
					RESTProcessorUtil.generateHappyResponseHeaders(response, content.length());
					response.setPayload(content.getBytes());
				}
				break;
			case "ws":
				WebSocketClient.WSBean wsJson = new Gson().fromJson(new String(request.getContent()), WebSocketClient.WSBean.class);
				// Check if not there yet.
				opClient = nmeaDataProviders.stream()
								.filter(channel -> channel instanceof SerialClient &&
												((WebSocketClient.WSBean) ((WebSocketClient) channel).getBean()).getWsUri().equals(wsJson.getWsUri()))
								.findFirst();
				if (!opClient.isPresent()) {
					try {
						NMEAClient wsClient = new WebSocketClient(this);
						wsClient.initClient();
						wsClient.setReader(new WebSocketReader(wsClient.getListeners(), wsJson.getWsUri()));
						nmeaDataProviders.add(wsClient);
						wsClient.startWorking();
						String content = new Gson().toJson(wsClient.getBean());
						RESTProcessorUtil.generateHappyResponseHeaders(response, content.length());
						response.setPayload(content.getBytes());
					} catch (Exception ex) {
						response.setStatus(400); 
						// Add message payload
						String content = new Gson().toJson(new RESTProcessorUtil.ErrorMessage(ex.toString())).toString();
						RESTProcessorUtil.generateHappyResponseHeaders(response, content.length());
						response.setPayload(content.getBytes());
						ex.printStackTrace();
					}
				} else {
					// Already there
					response.setStatus(400); 
					// Add message payload
					String content = new Gson().toJson(new RESTProcessorUtil.ErrorMessage("this 'ws' already exists")).toString();
					RESTProcessorUtil.generateHappyResponseHeaders(response, content.length());
					response.setPayload(content.getBytes());
				}
				break;
			case "file":
				DataFileClient.DataFileBean fileJson = new Gson().fromJson(new String(request.getContent()), DataFileClient.DataFileBean.class);
				// Check if not there yet.
				opClient = nmeaDataProviders.stream()
								.filter(channel -> channel instanceof DataFileClient &&
												((DataFileClient.DataFileBean) ((DataFileClient) channel).getBean()).getFile().equals(fileJson.getFile()))
								.findFirst();
				if (!opClient.isPresent()) {
					try {
						NMEAClient fileClient = new DataFileClient(this);
						fileClient.initClient();
						fileClient.setReader(new DataFileReader(fileClient.getListeners(), fileJson.getFile()));
						nmeaDataProviders.add(fileClient);
						fileClient.startWorking();
						String content = new Gson().toJson(fileClient.getBean());
						RESTProcessorUtil.generateHappyResponseHeaders(response, content.length());
						response.setPayload(content.getBytes());
					} catch (Exception ex) {
						response.setStatus(400); 
						// Add message payload
						String content = new Gson().toJson(new RESTProcessorUtil.ErrorMessage(ex.toString())).toString();
						RESTProcessorUtil.generateHappyResponseHeaders(response, content.length());
						response.setPayload(content.getBytes());
						ex.printStackTrace();
					}
				} else {
					// Already there
					response.setStatus(400); 
					// Add message payload
					String content = new Gson().toJson(new RESTProcessorUtil.ErrorMessage("this 'file' already exists")).toString();
					RESTProcessorUtil.generateHappyResponseHeaders(response, content.length());
					response.setPayload(content.getBytes());
				}
				break;
			case "bme280":
				opClient = nmeaDataProviders.stream()
								.filter(channel -> channel instanceof BME280Client)
								.findFirst();
				if (!opClient.isPresent()) {
					try {
						NMEAClient bme280Client = new BME280Client(this);
						bme280Client.initClient();
						bme280Client.setReader(new BME280Reader(bme280Client.getListeners()));
						nmeaDataProviders.add(bme280Client);
						bme280Client.startWorking();
						String content = new Gson().toJson(bme280Client.getBean());
						RESTProcessorUtil.generateHappyResponseHeaders(response, content.length());
						response.setPayload(content.getBytes());
					} catch (Exception ex) {
						response.setStatus(400); 
						// Add message payload
						String content = new Gson().toJson(new RESTProcessorUtil.ErrorMessage(ex.toString())).toString();
						RESTProcessorUtil.generateHappyResponseHeaders(response, content.length());
						response.setPayload(content.getBytes());
						ex.printStackTrace();
					}
				} else {
					// Already there
					response.setStatus(400); 
					// Add message payload
					String content = new Gson().toJson(new RESTProcessorUtil.ErrorMessage("this 'bme280' already exists")).toString();
					RESTProcessorUtil.generateHappyResponseHeaders(response, content.length());
					response.setPayload(content.getBytes());
				}
				break;
			case "htu21df":
				opClient = nmeaDataProviders.stream()
								.filter(channel -> channel instanceof HTU21DFClient)
								.findFirst();
				if (!opClient.isPresent()) {
					try {
						NMEAClient htu21dfClient = new HTU21DFClient(this);
						htu21dfClient.initClient();
						htu21dfClient.setReader(new HTU21DFReader(htu21dfClient.getListeners()));
						nmeaDataProviders.add(htu21dfClient);
						htu21dfClient.startWorking();
						String content = new Gson().toJson(htu21dfClient.getBean());
						RESTProcessorUtil.generateHappyResponseHeaders(response, content.length());
						response.setPayload(content.getBytes());
					} catch (Exception ex) {
						response.setStatus(400); 
						// Add message payload
						String content = new Gson().toJson(new RESTProcessorUtil.ErrorMessage(ex.toString())).toString();
						RESTProcessorUtil.generateHappyResponseHeaders(response, content.length());
						response.setPayload(content.getBytes());
						ex.printStackTrace();
					}
				} else {
					// Already there
					response.setStatus(400); 
					// Add message payload
					String content = new Gson().toJson(new RESTProcessorUtil.ErrorMessage("this 'htu21df' already exists")).toString();
					RESTProcessorUtil.generateHappyResponseHeaders(response, content.length());
					response.setPayload(content.getBytes());
				}
				break;
			case "rnd":
				opClient = nmeaDataProviders.stream()
								.filter(channel -> channel instanceof RandomClient)
								.findFirst();
				if (!opClient.isPresent()) {
					try {
						NMEAClient rndClient = new RandomClient(this);
						rndClient.initClient();
						rndClient.setReader(new RandomReader(rndClient.getListeners()));
						nmeaDataProviders.add(rndClient);
						rndClient.startWorking();
						String content = new Gson().toJson(rndClient.getBean());
						RESTProcessorUtil.generateHappyResponseHeaders(response, content.length());
						response.setPayload(content.getBytes());
					} catch (Exception ex) {
						response.setStatus(400); 
						// Add message payload
						String content = new Gson().toJson(new RESTProcessorUtil.ErrorMessage(ex.toString())).toString();
						RESTProcessorUtil.generateHappyResponseHeaders(response, content.length());
						response.setPayload(content.getBytes());
						ex.printStackTrace();
					}
				} else {
					// Already there
					response.setStatus(400); 
					// Add message payload
					String content = new Gson().toJson(new RESTProcessorUtil.ErrorMessage("this 'rnd' already exists")).toString();
					RESTProcessorUtil.generateHappyResponseHeaders(response, content.length());
					response.setPayload(content.getBytes());
				}
				break;
			default:
				response.setStatus(501); // Not implemented
				break;
		}
		return response;
	}

	private HTTPServer.Response postComputer(HTTPServer.Request request) {
		HTTPServer.Response response = new HTTPServer.Response(request.getProtocol(), 200);
		Optional<Computer> opComputer = null;
		String type = "";
		if (request.getContent() == null || request.getContent().length == 0) {
			response.setStatus(400); // No Content
			// Add message payload
			String content = new Gson().toJson(new RESTProcessorUtil.ErrorMessage("missing payload")).toString();
			RESTProcessorUtil.generateHappyResponseHeaders(response, content.length());
			response.setPayload(content.getBytes());
			return response;
		} else {
			Object bean = new GsonBuilder().create().fromJson(new String(request.getContent()), Object.class);
			if (bean instanceof Map) {
				type = ((Map<String, String>) bean).get("type");
			}
		}
		switch (type) {
			case "tw-current":
				ExtraDataComputer.ComputerBean twJson = new Gson().fromJson(new String(request.getContent()), ExtraDataComputer.ComputerBean.class);
				// Check if not there yet.
				opComputer = nmeaDataComputers.stream()
								.filter(channel -> channel instanceof ExtraDataComputer)
								.findFirst();
				if (!opComputer.isPresent()) {
					try {
						Computer twsCurrentComputer = new ExtraDataComputer(this, twJson.getPrefix(), twJson.getTimeBufferLength());
						nmeaDataComputers.add(twsCurrentComputer);
						String content = new Gson().toJson(twsCurrentComputer.getBean());
						RESTProcessorUtil.generateHappyResponseHeaders(response, content.length());
						response.setPayload(content.getBytes());
					} catch (Exception ex) {
						response.setStatus(400); 
						// Add message payload
						String content = new Gson().toJson(new RESTProcessorUtil.ErrorMessage(ex.toString())).toString();
						RESTProcessorUtil.generateHappyResponseHeaders(response, content.length());
						response.setPayload(content.getBytes());
						ex.printStackTrace();
					}
				} else {
					// Already there
					response.setStatus(400); 
					// Add message payload
					String content = new Gson().toJson(new RESTProcessorUtil.ErrorMessage("this 'computer' already exists")).toString();
					RESTProcessorUtil.generateHappyResponseHeaders(response, content.length());
					response.setPayload(content.getBytes());
				}
				break;
			default:
				response.setStatus(501); // Not implemented
				break;
		}
		return response;
	}

	private HTTPServer.Response putChannel(HTTPServer.Request request) {
		HTTPServer.Response response = new HTTPServer.Response(request.getProtocol(), 200);
		Optional<NMEAClient> opClient = null;
		String type = "";
		if (request.getContent() == null || request.getContent().length == 0) {
			response.setStatus(400); // No Content
			// Add message payload
			String content = new Gson().toJson(new RESTProcessorUtil.ErrorMessage("missing payload")).toString();
			RESTProcessorUtil.generateHappyResponseHeaders(response, content.length());
			response.setPayload(content.getBytes());
			return response;
		} else {
			Object bean = new GsonBuilder().create().fromJson(new String(request.getContent()), Object.class);
			if (bean instanceof Map) {
				type = ((Map<String, String>) bean).get("type");
			}
		}
		switch (type) {
			case "serial":
				SerialClient.SerialBean serialJson = new Gson().fromJson(new String(request.getContent()), SerialClient.SerialBean.class);
				opClient = nmeaDataProviders.stream()
								.filter(channel -> channel instanceof SerialClient &&
												((SerialClient.SerialBean) ((SerialClient) channel).getBean()).getPort().equals(serialJson.getPort()))
								.findFirst();
				if (!opClient.isPresent()) {
					response.setStatus(404); // Not found
					// Add message payload
					String content = new Gson().toJson(new RESTProcessorUtil.ErrorMessage("this 'serial' was not found")).toString();
					RESTProcessorUtil.generateHappyResponseHeaders(response, content.length());
					response.setPayload(content.getBytes());
				} else { // Then update
					SerialClient serialClient = (SerialClient) opClient.get();
					serialClient.setVerbose(serialJson.getVerbose());
					String content = new Gson().toJson(serialClient.getBean());
					RESTProcessorUtil.generateHappyResponseHeaders(response, content.length());
					response.setPayload(content.getBytes());
				}
				break;
			case "file":
				DataFileClient.DataFileBean fileJson = new Gson().fromJson(new String(request.getContent()), DataFileClient.DataFileBean.class);
				opClient = nmeaDataProviders.stream()
								.filter(channel -> channel instanceof DataFileClient &&
												((DataFileClient.DataFileBean) ((DataFileClient) channel).getBean()).getFile().equals(fileJson.getFile()))
								.findFirst();
				if (!opClient.isPresent()) {
					response.setStatus(404); // Not found
					// Add message payload
					String content = new Gson().toJson(new RESTProcessorUtil.ErrorMessage("this 'file' was not found")).toString();
					RESTProcessorUtil.generateHappyResponseHeaders(response, content.length());
					response.setPayload(content.getBytes());
				} else { // Then update
					DataFileClient dataFileClient = (DataFileClient) opClient.get();
					dataFileClient.setVerbose(fileJson.getVerbose());
					String content = new Gson().toJson(dataFileClient.getBean());
					RESTProcessorUtil.generateHappyResponseHeaders(response, content.length());
					response.setPayload(content.getBytes());
				}
				break;
			case "tcp":
				TCPClient.TCPBean tcpJson = new Gson().fromJson(new String(request.getContent()), TCPClient.TCPBean.class);
				opClient = nmeaDataProviders.stream()
								.filter(channel -> channel instanceof TCPClient &&
												((TCPClient.TCPBean) ((TCPClient) channel).getBean()).getHostname().equals(tcpJson.getHostname()) &&
												((TCPClient.TCPBean) ((TCPClient) channel).getBean()).getPort() == tcpJson.getPort())
								.findFirst();
				if (!opClient.isPresent()) {
					response.setStatus(404); // Not found
					// Add message payload
					String content = new Gson().toJson(new RESTProcessorUtil.ErrorMessage("this 'tcp' was not found")).toString();
					RESTProcessorUtil.generateHappyResponseHeaders(response, content.length());
					response.setPayload(content.getBytes());
				} else { // Then update
					TCPClient tcpClient = (TCPClient) opClient.get();
					tcpClient.setVerbose(tcpJson.getVerbose());
					String content = new Gson().toJson(tcpClient.getBean());
					RESTProcessorUtil.generateHappyResponseHeaders(response, content.length());
					response.setPayload(content.getBytes());
				}
				break;
			case "ws":
				WebSocketClient.WSBean wsJson = new Gson().fromJson(new String(request.getContent()), WebSocketClient.WSBean.class);
				opClient = nmeaDataProviders.stream()
								.filter(channel -> channel instanceof WebSocketClient &&
												((WebSocketClient.WSBean) ((WebSocketClient) channel).getBean()).getWsUri().equals(wsJson.getWsUri()))
								.findFirst();
				if (!opClient.isPresent()) {
					response.setStatus(404); // Not found
					// Add message payload
					String content = new Gson().toJson(new RESTProcessorUtil.ErrorMessage("this 'ws' was not found")).toString();
					RESTProcessorUtil.generateHappyResponseHeaders(response, content.length());
					response.setPayload(content.getBytes());
				} else { // Then update
					WebSocketClient webSocketClient = (WebSocketClient) opClient.get();
					webSocketClient.setVerbose(wsJson.getVerbose());
					String content = new Gson().toJson(webSocketClient.getBean());
					RESTProcessorUtil.generateHappyResponseHeaders(response, content.length());
					response.setPayload(content.getBytes());
				}
				break;
			case "bme280":
				BME280Client.BME280Bean bme280Json = new Gson().fromJson(new String(request.getContent()), BME280Client.BME280Bean.class);
				opClient = nmeaDataProviders.stream()
								.filter(channel -> channel instanceof BME280Client)
								.findFirst();
				if (!opClient.isPresent()) {
					response.setStatus(404); // Not found
					// Add message payload
					String content = new Gson().toJson(new RESTProcessorUtil.ErrorMessage("this 'bme280' was not found")).toString();
					RESTProcessorUtil.generateHappyResponseHeaders(response, content.length());
					response.setPayload(content.getBytes());
				} else { // Then update
					BME280Client bme280Client = (BME280Client) opClient.get();
					bme280Client.setVerbose(bme280Json.getVerbose());
					String content = new Gson().toJson(bme280Client.getBean());
					RESTProcessorUtil.generateHappyResponseHeaders(response, content.length());
					response.setPayload(content.getBytes());
				}
				break;
			case "htu21df":
				HTU21DFClient.HTU21DFBean htu21dfJson = new Gson().fromJson(new String(request.getContent()), HTU21DFClient.HTU21DFBean.class);
				opClient = nmeaDataProviders.stream()
								.filter(channel -> channel instanceof HTU21DFClient)
								.findFirst();
				if (!opClient.isPresent()) {
					response.setStatus(404); // Not found
					// Add message payload
					String content = new Gson().toJson(new RESTProcessorUtil.ErrorMessage("this 'htu21df' was not found")).toString();
					RESTProcessorUtil.generateHappyResponseHeaders(response, content.length());
					response.setPayload(content.getBytes());
				} else { // Then update
					HTU21DFClient htu21DFClient = (HTU21DFClient) opClient.get();
					htu21DFClient.setVerbose(htu21dfJson.getVerbose());
					String content = new Gson().toJson(htu21DFClient.getBean());
					RESTProcessorUtil.generateHappyResponseHeaders(response, content.length());
					response.setPayload(content.getBytes());
				}
				break;
			case "rnd":
				RandomClient.RandomBean rndJson = new Gson().fromJson(new String(request.getContent()), RandomClient.RandomBean.class);
				opClient = nmeaDataProviders.stream()
								.filter(channel -> channel instanceof RandomClient)
								.findFirst();
				if (!opClient.isPresent()) {
					response.setStatus(404); // Not found
					// Add message payload
					String content = new Gson().toJson(new RESTProcessorUtil.ErrorMessage("this 'rnd' was not found")).toString();
					RESTProcessorUtil.generateHappyResponseHeaders(response, content.length());
					response.setPayload(content.getBytes());
				} else { // Then update
					RandomClient randomClient = (RandomClient) opClient.get();
					randomClient.setVerbose(rndJson.getVerbose());
					String content = new Gson().toJson(randomClient.getBean());
					RESTProcessorUtil.generateHappyResponseHeaders(response, content.length());
					response.setPayload(content.getBytes());
				}
				break;
			default:
				response.setStatus(501); // Not implemented
				break;
		}
		return response;
	}

	private HTTPServer.Response putForwarder(HTTPServer.Request request) {
		HTTPServer.Response response = new HTTPServer.Response(request.getProtocol(), 200);
		Optional<NMEAClient> opClient = null;
		String type = "";
		if (request.getContent() == null || request.getContent().length == 0) {
			response.setStatus(400); // No Content
			// Add message payload
			String content = new Gson().toJson(new RESTProcessorUtil.ErrorMessage("missing payload")).toString();
			RESTProcessorUtil.generateHappyResponseHeaders(response, content.length());
			response.setPayload(content.getBytes());
			return response;
		} else {
			Object bean = new GsonBuilder().create().fromJson(new String(request.getContent()), Object.class);
			if (bean instanceof Map) {
				type = ((Map<String, String>) bean).get("type");
			}
		}
		switch (type) {
			default:
				response.setStatus(501); // Not implemented
				break;
		}
		return response;
	}

	private HTTPServer.Response putComputer(HTTPServer.Request request) {
		HTTPServer.Response response = new HTTPServer.Response(request.getProtocol(), 200);
		Optional<Computer> opComputer = null;
		String type = "";
		if (request.getContent() == null || request.getContent().length == 0) {
			response.setStatus(400); // No Content
			// Add message payload
			String content = new Gson().toJson(new RESTProcessorUtil.ErrorMessage("missing payload")).toString();
			RESTProcessorUtil.generateHappyResponseHeaders(response, content.length());
			response.setPayload(content.getBytes());
			return response;
		} else {
			Object bean = new GsonBuilder().create().fromJson(new String(request.getContent()), Object.class);
			if (bean instanceof Map) {
				type = ((Map<String, String>) bean).get("type");
			}
		}
		switch (type) {
			case "tw-current":
				ExtraDataComputer.ComputerBean twJson = new Gson().fromJson(new String(request.getContent()), ExtraDataComputer.ComputerBean.class);
				opComputer = nmeaDataComputers.stream()
								.filter(cptr -> cptr instanceof ExtraDataComputer)
								.findFirst();
				if (!opComputer.isPresent()) {
					response.setStatus(404); // Not found
					// Add message payload
					String content = new Gson().toJson(new RESTProcessorUtil.ErrorMessage("'tw-current' not found")).toString();
					RESTProcessorUtil.generateHappyResponseHeaders(response, content.length());
					response.setPayload(content.getBytes());
				} else { // Then update
					ExtraDataComputer computer = (ExtraDataComputer) opComputer.get();
					computer.setVerbose(twJson.isVerbose());
					computer.setPrefix(twJson.getPrefix());
					computer.setTimeBufferLength(twJson.getTimeBufferLength());
					String content = new Gson().toJson(computer.getBean());
					RESTProcessorUtil.generateHappyResponseHeaders(response, content.length());
					response.setPayload(content.getBytes());
				}
				break;
			default:
				response.setStatus(501); // Not implemented
				break;
		}
		return response;
	}

	private HTTPServer.Response putMuxVerbose(HTTPServer.Request request) {
		HTTPServer.Response response = new HTTPServer.Response(request.getProtocol(), 200);
		List<String> prmValues = RESTProcessorUtil.getPrmValues(request.getRequestPattern(), request.getPath());
		if (prmValues.size() != 1) {
			response.setStatus(400);
			// Add message payload
			String content = new Gson().toJson(new RESTProcessorUtil.ErrorMessage("missing path parameter")).toString();
			RESTProcessorUtil.generateHappyResponseHeaders(response, content.length());
			response.setPayload(content.getBytes());
			return response;
		}
		boolean newValue = "on".equals(prmValues.get(0));
		this.verbose = newValue;
		String content = "";
		RESTProcessorUtil.generateHappyResponseHeaders(response, content.length());
		response.setPayload(content.getBytes());

		return response;
	}

	private HTTPServer.Response getOperationList(HTTPServer.Request request) {
		HTTPServer.Response response = new HTTPServer.Response(request.getProtocol(), 200);
		List<Object> channelList = getInputChannelList();
		Operation[] channelArray = operations.stream()
						.collect(Collectors.toList())
						.toArray(new Operation[operations.size()]);
		String content = new Gson().toJson(channelArray);
		RESTProcessorUtil.generateHappyResponseHeaders(response, content.length());
		response.setPayload(content.getBytes());
		return response;
	}

	private HTTPServer.Response emptyOperation(HTTPServer.Request request) {
		HTTPServer.Response response = new HTTPServer.Response(request.getProtocol(), 200);

		return response;
	}

	/**
	 * Implements the management of the REST requests.
	 * Dedicated Admin Server.
	 *
	 * @param request the parsed request.
	 * @return the response, along with its HTTP status code.
	 */
	@Override
	public HTTPServer.Response onRequest(HTTPServer.Request request) {
		HTTPServer.Response response = new HTTPServer.Response(request.getProtocol(), 501); // Default, Not implemented
		response = processRequest(request, response);
		if (this.verbose) {
			System.out.println("======================================");
			System.out.println("Request :\n" + request.toString());
			System.out.println("Response :\n" + response.toString());
			System.out.println("======================================");
		}
		return response;
	}

	private HTTPServer.Response removeForwarderIfPresent(HTTPServer.Request request, Optional<Forwarder> opFwd) {
		HTTPServer.Response response;
		if (opFwd.isPresent()) {
			Forwarder forwarder = opFwd.get();
			forwarder.close();
			nmeaDataForwarders.remove(forwarder);
			response = new HTTPServer.Response(request.getProtocol(), 204);
		} else {
			response = new HTTPServer.Response(request.getProtocol(), 404);
			// Add message payload
			String content = new Gson().toJson(new RESTProcessorUtil.ErrorMessage("forwarder not found")).toString();
			RESTProcessorUtil.generateHappyResponseHeaders(response, content.length());
			response.setPayload(content.getBytes());
		}
		return response;
	}

	private HTTPServer.Response removeChannelIfPresent(HTTPServer.Request request, Optional<NMEAClient> nmeaClient) {
		HTTPServer.Response response;
		if (nmeaClient.isPresent()) {
			NMEAClient client = nmeaClient.get();
			client.stopDataRead();
			nmeaDataProviders.remove(client);
			response = new HTTPServer.Response(request.getProtocol(), 204);
		} else {
			response = new HTTPServer.Response(request.getProtocol(), 404);
			// Add message payload
			String content = new Gson().toJson(new RESTProcessorUtil.ErrorMessage("channel not found")).toString();
			RESTProcessorUtil.generateHappyResponseHeaders(response, content.length());
			response.setPayload(content.getBytes());
		}
		return response;
	}

	private HTTPServer.Response removeComputerIfPresent(HTTPServer.Request request, Optional<Computer> nmeaComputer) {
		HTTPServer.Response response;
		if (nmeaComputer.isPresent()) {
			Computer computer = nmeaComputer.get();
			computer.close();
			nmeaDataComputers.remove(computer);
			response = new HTTPServer.Response(request.getProtocol(), 204);
		} else {
			response = new HTTPServer.Response(request.getProtocol(), 404);
			// Add message payload
			String content = new Gson().toJson(new RESTProcessorUtil.ErrorMessage("computer not found")).toString();
			RESTProcessorUtil.generateHappyResponseHeaders(response, content.length());
			response.setPayload(content.getBytes());
		}
		return response;
	}

	private static List<String> getSerialPortList() {
		List<String> portList = new ArrayList<>();
		// Opening Serial port
		Enumeration enumeration = CommPortIdentifier.getPortIdentifiers();
		while (enumeration.hasMoreElements()) {
			CommPortIdentifier cpi = (CommPortIdentifier) enumeration.nextElement();
			portList.add(cpi.getName());
		}
		return portList;
	}

	private List<Object> getInputChannelList() {
		return nmeaDataProviders.stream().map(nmea -> nmea.getBean()).collect(Collectors.toList());
	}

	private List<Object> getForwarderList() {
		return nmeaDataForwarders.stream().map(fwd -> fwd.getBean()).collect(Collectors.toList());
	}

	private List<Object> getComputerList() {
		return nmeaDataComputers.stream().map(cptr -> cptr.getBean()).collect(Collectors.toList());
	}

	@Override
	public synchronized void onData(String mess) {
		if (verbose) {
			System.out.println("==== From MUX: " + mess);
			DumpUtil.displayDualDump(mess);
			System.out.println("==== End Mux =============");
		}
		// Computers. Must go first, as a computer may refeed the present onData method.
		nmeaDataComputers.stream()
						.forEach(computer -> {
							computer.write(mess.getBytes());
						});
		// Forwarders
		nmeaDataForwarders.stream()
						.forEach(fwd -> {
							try {
								fwd.write((mess.trim() + NMEAParser.STANDARD_NMEA_EOS).getBytes());
							} catch (Exception e) {
								e.printStackTrace();
							}
						});
	}

	private final static NumberFormat MUX_IDX_FMT = new DecimalFormat("00");
	private boolean verbose = false;

	/**
	 * Constructor.
	 * @param muxProps Initial config. See {@link #main(String...)} method.
	 */
	public GenericNMEAMultiplexer(Properties muxProps) {

		// Check duplicates if operation list. Barfs if duplicate is found.
		for (int i = 0; i < operations.size(); i++) {
			for (int j = i + 1; j < operations.size(); j++) {
				if (operations.get(i).getVerb().equals(operations.get(j).getVerb()) &&
								RESTProcessorUtil.pathsAreIndentical(operations.get(i).getPath(), operations.get(j).getPath())) {
					throw new RuntimeException(String.format("Duplicate entry in operations list %s %s", operations.get(i).getVerb(), operations.get(i).getPath()));
				}
			}
		}

		// Read initial config from the properties file. See the main method.
		verbose = "true".equals(System.getProperty("mux.data.verbose", "false")); // Initial verbose.
		int muxIdx = 1;
		boolean thereIsMore = true;
		// 1 - Input channels
		while (thereIsMore) {
			String typeProp = String.format("mux.%s.type", MUX_IDX_FMT.format(muxIdx));
			String type = muxProps.getProperty(typeProp);
			if (type == null) {
				thereIsMore = false;
			} else {
				switch (type) {
					case "serial":
						try {
							String serialPort = muxProps.getProperty(String.format("mux.%s.port", MUX_IDX_FMT.format(muxIdx)));
							String br = muxProps.getProperty(String.format("mux.%s.baudrate", MUX_IDX_FMT.format(muxIdx)));
							NMEAClient serialClient = new SerialClient(this);
//					  serialClient.setEOS("\n");
							serialClient.initClient();
							serialClient.setReader(new SerialReader(serialClient.getListeners(), serialPort, Integer.parseInt(br)));
							nmeaDataProviders.add(serialClient);
						} catch (Exception e) {
							e.printStackTrace();
						}
						break;
					case "tcp":
						try {
							String tcpPort = muxProps.getProperty(String.format("mux.%s.port", MUX_IDX_FMT.format(muxIdx)));
							String tcpServer = muxProps.getProperty(String.format("mux.%s.server", MUX_IDX_FMT.format(muxIdx)));
							NMEAClient tcpClient = new TCPClient(this);
							tcpClient.initClient();
							tcpClient.setReader(new TCPReader(tcpClient.getListeners(), tcpServer, Integer.parseInt(tcpPort)));
							nmeaDataProviders.add(tcpClient);
						} catch (Exception e) {
							e.printStackTrace();
						}
						break;
					case "file":
						try {
							String filename = muxProps.getProperty(String.format("mux.%s.filename", MUX_IDX_FMT.format(muxIdx)));
							NMEAClient fileClient = new DataFileClient(this);
							fileClient.initClient();
							fileClient.setReader(new DataFileReader(fileClient.getListeners(), filename));
							nmeaDataProviders.add(fileClient);
						} catch (Exception e) {
							e.printStackTrace();
						}
						break;
					case "ws":
						try {
							String wsUri = muxProps.getProperty(String.format("mux.%s.wsuri", MUX_IDX_FMT.format(muxIdx)));
							NMEAClient wsClient = new WebSocketClient(this);
							wsClient.initClient();
							wsClient.setReader(new WebSocketReader(wsClient.getListeners(), wsUri));
							nmeaDataProviders.add(wsClient);
						} catch (Exception e) {
							e.printStackTrace();
						}
						break;
					case "htu21df": // Humidity & Temperature sensor
						try {
							NMEAClient htu21dfClient = new HTU21DFClient(this);
							htu21dfClient.initClient();
							htu21dfClient.setReader(new HTU21DFReader(htu21dfClient.getListeners()));
							nmeaDataProviders.add(htu21dfClient);
						} catch (Exception e) {
							e.printStackTrace();
						} catch (Error err) {
							err.printStackTrace();
						}
						break;
					case "rnd": // Random generator, for debugging
						try {
							NMEAClient rndClient = new RandomClient(this);
							rndClient.initClient();
							rndClient.setReader(new RandomReader(rndClient.getListeners()));
							nmeaDataProviders.add(rndClient);
						} catch (Exception e) {
							e.printStackTrace();
						} catch (Error err) {
							err.printStackTrace();
						}
						break;
					case "bme280": // Humidity, Temperature, Pressure
						try {
							NMEAClient bme280Client = new BME280Client(this);
							bme280Client.initClient();
							bme280Client.setReader(new BME280Reader(bme280Client.getListeners()));
							nmeaDataProviders.add(bme280Client);
						} catch (Exception e) {
							e.printStackTrace();
						} catch (Error err) {
							err.printStackTrace();
						}
						break;
					case "bmp180": // Temperature, Pressure
					case "lsm303": // 3D magnetometer
					case "batt":   // Battery Voltage, use XDR
					default:
						throw new RuntimeException(String.format("mux type [%s] not supported yet.", type));
				}
			}
			muxIdx++;
		}
		thereIsMore = true;
		int fwdIdx = 1;
		// 2 - Output channels
		while (thereIsMore) {
			String typeProp = String.format("forward.%s.type", MUX_IDX_FMT.format(fwdIdx));
			String type = muxProps.getProperty(typeProp);
			if (type == null) {
				thereIsMore = false;
			} else {
				switch (type) {
					case "tcp":
						String tcpPort = muxProps.getProperty(String.format("forward.%s.port", MUX_IDX_FMT.format(fwdIdx)));
						try {
							Forwarder tcpForwarder = new TCPWriter(Integer.parseInt(tcpPort));
							nmeaDataForwarders.add(tcpForwarder);
						} catch (Exception ex) {
							ex.printStackTrace();
						}
						break;
					case "file":
						String fName = muxProps.getProperty(String.format("forward.%s.filename", MUX_IDX_FMT.format(fwdIdx)));
						try {
							Forwarder fileForwarder = new DataFileWriter(fName);
							nmeaDataForwarders.add(fileForwarder);
						} catch (Exception ex) {
							ex.printStackTrace();
						}
						break;
					case "ws":
						String wsUri = muxProps.getProperty(String.format("forward.%s.wsuri", MUX_IDX_FMT.format(fwdIdx)));
						try {
							Forwarder wsForwarder = new WebSocketWriter(wsUri);
							nmeaDataForwarders.add(wsForwarder);
						} catch (Exception ex) {
							ex.printStackTrace();
						}
						break;
					case "console":
						try {
							Forwarder consoleForwarder = new ConsoleWriter();
							nmeaDataForwarders.add(consoleForwarder);
						} catch (Exception ex) {
							ex.printStackTrace();
						}
						break;
					case "rmi":
						String rmiPort = muxProps.getProperty(String.format("forward.%s.port", MUX_IDX_FMT.format(fwdIdx)));
						String rmiName = muxProps.getProperty(String.format("forward.%s.name", MUX_IDX_FMT.format(fwdIdx)));
						try {
							Forwarder rmiServerForwarder;
							if (rmiName != null && rmiName.trim().length() > 0) {
								rmiServerForwarder = new RMIServer(Integer.parseInt(rmiPort), rmiName);
							} else {
								rmiServerForwarder = new RMIServer(Integer.parseInt(rmiPort));
							}
							nmeaDataForwarders.add(rmiServerForwarder);
						} catch (Exception ex) {
							ex.printStackTrace();
						}
						break;
					default:
						throw new RuntimeException(String.format("forward type [%s] not supported yet.", type));
				}
			}
			fwdIdx++;
		}
		// Init cache (for Computers).
		if ("true".equals(muxProps.getProperty("init.cache", "false"))) {
			try {
				String deviationFile = muxProps.getProperty("deviation.file.name", "zero-deviation.csv");
				double maxLeeway = Double.parseDouble(muxProps.getProperty("max.leeway", "0"));
				double bspFactor = Double.parseDouble(muxProps.getProperty("bsp.factor", "1"));
				double awsFactor = Double.parseDouble(muxProps.getProperty("aws.factor", "1"));
				double awaOffset = Double.parseDouble(muxProps.getProperty("awa.offset", "0"));
				double hdgOffset = Double.parseDouble(muxProps.getProperty("hdg.offset", "0"));
				double defaultDeclination = Double.parseDouble(muxProps.getProperty("default.declination", "0"));
				int damping = Integer.parseInt(muxProps.getProperty("damping", "1"));
				ApplicationContext.getInstance().initCache(deviationFile, maxLeeway, bspFactor, awsFactor, awaOffset, hdgOffset, defaultDeclination, damping);

				// If there is a cache, then let's see what computers to start.
				thereIsMore = true;
				int cptrIdx = 1;
				// 3 - Computers
				while (thereIsMore) {
					String typeProp = String.format("computer.%s.type", MUX_IDX_FMT.format(cptrIdx));
					String type = muxProps.getProperty(typeProp);
					if (type == null) {
						thereIsMore = false;
					} else {
						switch (type) {
							case "tw-current":
								String prefix = muxProps.getProperty(String.format("computer.%s.prefix", MUX_IDX_FMT.format(cptrIdx)), "OS");
								long timeBufferLength = Long.parseLong(muxProps.getProperty(String.format("computer.%s.time.buffer.length", MUX_IDX_FMT.format(cptrIdx)), "600000"));
								try {
									Computer twCurrentComputer = new ExtraDataComputer(this, prefix, timeBufferLength);
									nmeaDataComputers.add(twCurrentComputer);
								} catch (Exception ex) {
									ex.printStackTrace();
								}
								break;
							default:
								break;
						}
					}
					cptrIdx++;
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				System.out.println("Shutting down multiplexer nicely.");
				nmeaDataProviders.stream()
								.forEach(client -> client.stopDataRead());
				nmeaDataForwarders.stream()
								.forEach(fwd -> fwd.close());
				nmeaDataComputers.stream()
								.forEach(comp -> comp.close());
				if (adminServer != null) {
					adminServer.stopRunning();
				}
			}
		});

		nmeaDataProviders.stream()
						.forEach(client -> {
							try {
								client.startWorking();
							} catch (Exception ex) {
								ex.printStackTrace();
							}
						});
	}

	public void startAdminServer(int port) {
		try {
			this.adminServer = new HTTPServer(port, this);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String... args) {

		String propertiesFile = System.getProperty("mux.properties", "nmea.mux.properties");

		Properties definitions = new Properties();
		File propFile = new File(propertiesFile);
		if (!propFile.exists()) {
			throw new RuntimeException(String.format("File [%s] not found", propertiesFile));
		} else {
			try {
				definitions.load(new java.io.FileReader(propFile));
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}

		GenericNMEAMultiplexer mux = new GenericNMEAMultiplexer(definitions);

		// with.http.server=yes
		// http.port=9999
		if ("yes".equals(definitions.getProperty("with.http.server", "false"))) {
			mux.startAdminServer(Integer.parseInt(definitions.getProperty("http.port", "9999")));
		}
	}
}
