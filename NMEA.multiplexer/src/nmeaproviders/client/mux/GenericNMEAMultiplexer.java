package nmeaproviders.client.mux;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import computers.Computer;
import computers.TrueWindComputer;
import context.ApplicationContext;
import gnu.io.CommPortIdentifier;
import http.HTTPServer;
import http.HTTPServerInterface;
import nmea.api.Multiplexer;
import nmea.api.NMEAClient;
import nmeaproviders.client.BME280Client;
import nmeaproviders.client.DataFileClient;
import nmeaproviders.client.HTU21DFClient;
import nmeaproviders.client.RandomClient;
import nmeaproviders.client.SerialClient;
import nmeaproviders.client.TCPClient;
import nmeaproviders.client.WebSocketClient;
import nmeaproviders.reader.BME280Reader;
import nmeaproviders.reader.DataFileReader;
import nmeaproviders.reader.HTU21DFReader;
import nmeaproviders.reader.RandomReader;
import nmeaproviders.reader.SerialReader;
import nmeaproviders.reader.TCPReader;
import nmeaproviders.reader.WebSocketReader;
import servers.ConsoleWriter;
import servers.DataFileWriter;
import servers.Forwarder;
import servers.TCPWriter;
import servers.WebSocketWriter;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

public class GenericNMEAMultiplexer implements Multiplexer, HTTPServerInterface {
	private HTTPServer adminServer = null;

	private List<NMEAClient> nmeaDataProviders  = new ArrayList<>();
	private List<Forwarder>  nmeaDataForwarders = new ArrayList<>();
	private List<Computer>   nmeaDataComputers  = new ArrayList<>();

	// TODO Operation List

	/**
	 * Implements the management of the REST requests.
	 *
	 * @param request
	 * @return
	 */
	@Override
	public HTTPServer.Response onRequest(HTTPServer.Request request) {
		HTTPServer.Response response = new HTTPServer.Response(request.getProtocol(), 400); // Default, not found
		switch (request.getVerb()) {
			case "GET":
				// GET /serial-ports
				if (request.getPath().equals("/serial-ports")) {
					response = new HTTPServer.Response(request.getProtocol(), 200);

					List<String> portList = getSerialPortList();
					Object[] portArray = portList.toArray(new Object[portList.size()]);
					String content = new Gson().toJson(portArray).toString();
					generateHappyResponseHeaders(response, content.length());
					response.setPayload(content.getBytes());
				}
				// GET /channels
				else if (request.getPath().equals("/channels")) {
					response = new HTTPServer.Response(request.getProtocol(), 200);

					List<Object> channelList = getInputChannelList();
					Object[] channelArray = channelList.stream()
									.collect(Collectors.toList())
									.toArray(new Object[channelList.size()]);

					String content = new Gson().toJson(channelArray);
					generateHappyResponseHeaders(response, content.length());
					response.setPayload(content.getBytes());
				}
				// GET /forwarders
				else if (request.getPath().equals("/forwarders")) {
					response = new HTTPServer.Response(request.getProtocol(), 200);

					List<Object> forwarderList = getForwarderList();
					Object[] forwarderArray = forwarderList.stream()
									.collect(Collectors.toList())
									.toArray(new Object[forwarderList.size()]);

					String content = new Gson().toJson(forwarderArray);
					generateHappyResponseHeaders(response, content.length());
					response.setPayload(content.getBytes());
				} else {
					response = new HTTPServer.Response(request.getProtocol(), 404); // Default, not found
				}
				break;
			case "DELETE": // DELETE channel, forwarder
				String[] deletePathElem = request.getPath().split("/");
			  // DELETE /forwarders/:type. Details in the payload
				if (deletePathElem != null && deletePathElem.length >= 3 && deletePathElem[1].equals("forwarders")) {
					if (deletePathElem[2].equals("console")) {                       // console
						Optional<Forwarder> opFwd = nmeaDataForwarders.stream()
										.filter(fwd -> fwd instanceof ConsoleWriter)
										.findFirst();
						response = removeForwarderIfPresent(request, opFwd);
					} else if (deletePathElem[2].equals("file")) {                   // file
						Gson gson = new GsonBuilder().create();
						if (request.getContent() != null) {
							StringReader stringReader = new StringReader(new String(request.getContent()));
							DataFileWriter.DataFileBean dataFileBean = gson.fromJson(stringReader, DataFileWriter.DataFileBean.class);
							Optional<Forwarder> opFwd = nmeaDataForwarders.stream()
											.filter(fwd -> fwd instanceof DataFileWriter &&
															((DataFileWriter) fwd).getLog().equals(dataFileBean.getLog()))
											.findFirst();
							response = removeForwarderIfPresent(request, opFwd);
						} else {
							response = new HTTPServer.Response(request.getProtocol(), 400); // Bad request (no payload)
						}
					} else if (deletePathElem[2].equals("tcp")) {                   // tcp
						Gson gson = new GsonBuilder().create();
						if (request.getContent() != null) {
							StringReader stringReader = new StringReader(new String(request.getContent()));
							TCPWriter.TCPBean tcpBean = gson.fromJson(stringReader, TCPWriter.TCPBean.class);
							Optional<Forwarder> opFwd = nmeaDataForwarders.stream()
											.filter(fwd -> fwd instanceof TCPWriter &&
															((TCPWriter)fwd).getTcpPort() == tcpBean.getPort())
											.findFirst();
							response = removeForwarderIfPresent(request, opFwd);
						} else {
							response = new HTTPServer.Response(request.getProtocol(), 400); // Bad request (no payload)
						}
					} else if (deletePathElem[2].equals("ws")) {                    // ws
						Gson gson = new GsonBuilder().create();
						if (request.getContent() != null) {
							StringReader stringReader = new StringReader(new String(request.getContent()));
							WebSocketWriter.WSBean wsBean = gson.fromJson(stringReader, WebSocketWriter.WSBean.class);
							Optional<Forwarder> opFwd = nmeaDataForwarders.stream()
											.filter(fwd -> fwd instanceof WebSocketWriter &&
															((WebSocketWriter)fwd).getWsUri().equals(wsBean.getWsUri()))
											.findFirst();
							response = removeForwarderIfPresent(request, opFwd);
						} else {
							response = new HTTPServer.Response(request.getProtocol(), 400); // Bad request (no payload)
						}
					} else if (deletePathElem[2].equals("udp")) {                   // udp
						response = new HTTPServer.Response(request.getProtocol(), 404); // Not implemented
					}
				}
				// DELETE /channels/:type. Details in the payload
				else if (deletePathElem != null && deletePathElem.length >= 3 && deletePathElem[1].equals("channels")) {
				  if (deletePathElem[2].equals("file")) {                   // file
					  Gson gson = new GsonBuilder().create();
					  if (request.getContent() != null) {
						  StringReader stringReader = new StringReader(new String(request.getContent()));
						  DataFileClient.DataFileBean dataFileBean = gson.fromJson(stringReader, DataFileClient.DataFileBean.class);
						  Optional<NMEAClient> opClient = nmeaDataProviders.stream()
										  .filter(channel -> channel instanceof DataFileClient &&
														  ((DataFileClient.DataFileBean) ((DataFileClient) channel).getBean()).getFile().equals(dataFileBean.getFile()))
										  .findFirst();
						  response = removeChannelIfPresent(request, opClient);
					  }
				  } else if (deletePathElem[2].equals("serial")) {        // serial
					  Gson gson = new GsonBuilder().create();
					  if (request.getContent() != null) {
						  StringReader stringReader = new StringReader(new String(request.getContent()));
						  SerialClient.SerialBean serialBean = gson.fromJson(stringReader, SerialClient.SerialBean.class);
						  Optional<NMEAClient> opClient = nmeaDataProviders.stream()
										  .filter(channel -> channel instanceof SerialClient &&
														  ((SerialClient.SerialBean) ((SerialClient) channel).getBean()).getPort().equals(serialBean.getPort())) // No need for BaudRate
										  .findFirst();
						  response = removeChannelIfPresent(request, opClient);
					  }
				  } else if (deletePathElem[2].equals("tcp")) {           // tcp
					  Gson gson = new GsonBuilder().create();
					  if (request.getContent() != null) {
						  StringReader stringReader = new StringReader(new String(request.getContent()));
						  TCPClient.TCPBean tcpBean = gson.fromJson(stringReader, TCPClient.TCPBean.class);
						  Optional<NMEAClient> opClient = nmeaDataProviders.stream()
										  .filter(channel -> channel instanceof TCPClient &&
														  ((TCPClient.TCPBean) ((TCPClient) channel).getBean()).getPort() == tcpBean.getPort())
										  .findFirst();
						  response = removeChannelIfPresent(request, opClient);
					  }
				  } else if (deletePathElem[2].equals("ws")) {            // ws
					  Gson gson = new GsonBuilder().create();
					  if (request.getContent() != null) {
						  StringReader stringReader = new StringReader(new String(request.getContent()));
						  WebSocketClient.WSBean wsBean = gson.fromJson(stringReader, WebSocketClient.WSBean.class);
						  Optional<NMEAClient> opClient = nmeaDataProviders.stream()
										  .filter(channel -> channel instanceof WebSocketClient &&
														  ((WebSocketClient.WSBean) ((WebSocketClient) channel).getBean()).getWsUri().equals(wsBean.getWsUri()))
										  .findFirst();
						  response = removeChannelIfPresent(request, opClient);
					  }
				  } else if (deletePathElem[2].equals("bme280")) {        // bme280
					  Optional<NMEAClient> opClient = nmeaDataProviders.stream()
									  .filter(channel -> channel instanceof BME280Client)
									  .findFirst();
					  response = removeChannelIfPresent(request, opClient);
				  } else if (deletePathElem[2].equals("htu21df")) {       // htu21df
					  Optional<NMEAClient> opClient = nmeaDataProviders.stream()
									  .filter(channel -> channel instanceof HTU21DFClient)
									  .findFirst();
					  response = removeChannelIfPresent(request, opClient);
				  } else if (deletePathElem[2].equals("rnd")) {           // rnd
					  Optional<NMEAClient> opClient = nmeaDataProviders.stream()
									  .filter(channel -> channel instanceof RandomClient)
									  .findFirst();
					  response = removeChannelIfPresent(request, opClient);
				  } else {
					  // Not implemented
					  response = new HTTPServer.Response(request.getProtocol(), 404); // Not implemented
				  }
				}
				break;
			case "POST": // POST channel, forwarder
				String[] postPathElem = request.getPath().split("/");
				String type = "";
				if (request.getContent() == null) {
					response = new HTTPServer.Response(request.getProtocol(), 400); // No Content
				} else {
					Object bean = new GsonBuilder().create().fromJson(new String(request.getContent()), Object.class);
					if (bean instanceof Map) {
						type = ((Map<String, String>)bean).get("type");
					}
				}
				// POST /forwarders
				if (postPathElem != null && postPathElem.length >= 2 && postPathElem[1].equals("forwarders")) {
					if (type.equals("console")) {
						// Check existence
						Optional<Forwarder> opFwd = nmeaDataForwarders.stream()
										.filter(fwd -> fwd instanceof ConsoleWriter)
										.findFirst();
						if (!opFwd.isPresent()) {
							try {
								Forwarder consoleForwarder = new ConsoleWriter();
								nmeaDataForwarders.add(consoleForwarder);
								response = new HTTPServer.Response(request.getProtocol(), 200);
								String content = new Gson().toJson(consoleForwarder.getBean());
								generateHappyResponseHeaders(response, content.length());
								response.setPayload(content.getBytes());
							} catch (Exception ex) {
								response = new HTTPServer.Response(request.getProtocol(), 400); // Default, Bad Request
								ex.printStackTrace();
							}
						} else {
							// Already there
							response = new HTTPServer.Response(request.getProtocol(), 400); // Default, Bad Request
						}
					} else if (type.equals("tcp")) {
						if (request.getContent() != null && request.getContent().length > 0) {
							TCPWriter.TCPBean json = new Gson().fromJson(new String(request.getContent()), TCPWriter.TCPBean.class);
							// Check if not there yet.
							Optional<Forwarder> opFwd = nmeaDataForwarders.stream()
											.filter(fwd -> fwd instanceof TCPWriter &&
															((TCPWriter)fwd).getTcpPort() == json.getPort())
											.findFirst();
							if (!opFwd.isPresent()) {
								try {
									Forwarder tcpForwarder = new TCPWriter(json.getPort());
									nmeaDataForwarders.add(tcpForwarder);
									response = new HTTPServer.Response(request.getProtocol(), 200);
									String content = new Gson().toJson(tcpForwarder.getBean());
									generateHappyResponseHeaders(response, content.length());
									response.setPayload(content.getBytes());
								} catch (Exception ex) {
									response = new HTTPServer.Response(request.getProtocol(), 400); // Default, Bad Request
									ex.printStackTrace();
								}
							} else {
								// Already there
								response = new HTTPServer.Response(request.getProtocol(), 400); // Default, Bad Request
							}
						} else {
							response = new HTTPServer.Response(request.getProtocol(), 400); // Default, Bad Request
						}
					} else if (type.equals("file")) {
						if (request.getContent() != null && request.getContent().length > 0) {
							DataFileWriter.DataFileBean json = new Gson().fromJson(new String(request.getContent()), DataFileWriter.DataFileBean.class);
							// Check if not there yet.
							Optional<Forwarder> opFwd = nmeaDataForwarders.stream()
											.filter(fwd -> fwd instanceof DataFileWriter &&
															((DataFileWriter)fwd).getLog().equals(json.getLog()))
											.findFirst();
							if (!opFwd.isPresent()) {
								try {
									Forwarder fileForwarder = new DataFileWriter(json.getLog());
									nmeaDataForwarders.add(fileForwarder);
									response = new HTTPServer.Response(request.getProtocol(), 200);
									String content = new Gson().toJson(fileForwarder.getBean());
									generateHappyResponseHeaders(response, content.length());
									response.setPayload(content.getBytes());
								} catch (Exception e) {
									response = new HTTPServer.Response(request.getProtocol(), 400); // Default, Bad Request
									e.printStackTrace();
								}
							}
						} else {
							response = new HTTPServer.Response(request.getProtocol(), 400); // Default, Bad Request
						}
					} else if (type.equals("ws")) {
						if (request.getContent() != null && request.getContent().length > 0) {
							WebSocketWriter.WSBean json = new Gson().fromJson(new String(request.getContent()), WebSocketWriter.WSBean.class);
							// Check if not there yet.
							Optional<Forwarder> opFwd = nmeaDataForwarders.stream()
											.filter(fwd -> fwd instanceof WebSocketWriter &&
															((WebSocketWriter)fwd).getWsUri() == json.getWsUri())
											.findFirst();
							if (!opFwd.isPresent()) {
								try {
									Forwarder wsForwarder = new WebSocketWriter(json.getWsUri());
									nmeaDataForwarders.add(wsForwarder);
									response = new HTTPServer.Response(request.getProtocol(), 200);
									String content = new Gson().toJson(wsForwarder.getBean());
									generateHappyResponseHeaders(response, content.length());
									response.setPayload(content.getBytes());
								} catch (Exception ex) {
									response = new HTTPServer.Response(request.getProtocol(), 400); // Default, Bad Request
									ex.printStackTrace();
								}
							} else {
								// Already there
								response = new HTTPServer.Response(request.getProtocol(), 400); // Default, Bad Request
							}
						} else {
							response = new HTTPServer.Response(request.getProtocol(), 400); // Default, Bad Request
						}
					}
				}
				// POST /channels
				else if (postPathElem != null && postPathElem.length >= 2 && postPathElem[1].equals("channels")) {
					if (type.equals("tcp")) {                                                               // tcp
						if (request.getContent() != null && request.getContent().length > 0) {
							TCPClient.TCPBean json = new Gson().fromJson(new String(request.getContent()), TCPClient.TCPBean.class);
							// Check if not there yet.
							Optional<NMEAClient> opClient = nmeaDataProviders.stream()
											.filter(channel -> channel instanceof SerialClient &&
															((TCPClient.TCPBean) ((TCPClient) channel).getBean()).getPort() == json.getPort() &&
															((TCPClient.TCPBean) ((TCPClient) channel).getBean()).getHostname().equals(json.getHostname()))
											.findFirst();
							if (!opClient.isPresent()) {
								try {
									NMEAClient tcpClient = new TCPClient(this);
									tcpClient.initClient();
									tcpClient.setReader(new TCPReader(tcpClient.getListeners(), json.getHostname(), json.getPort()));
									nmeaDataProviders.add(tcpClient);
									tcpClient.startWorking();
									response = new HTTPServer.Response(request.getProtocol(), 200);
									String content = new Gson().toJson(tcpClient.getBean());
									generateHappyResponseHeaders(response, content.length());
									response.setPayload(content.getBytes());
								} catch (Exception ex) {
									response = new HTTPServer.Response(request.getProtocol(), 400); // Default, Bad Request
									ex.printStackTrace();
								}
							} else {
								// Already there
								response = new HTTPServer.Response(request.getProtocol(), 400); // Default, Bad Request
							}
						} else {
							response = new HTTPServer.Response(request.getProtocol(), 400); // Default, Bad Request
						}
					} else if (type.equals("serial")) {                                                           // serial
						if (request.getContent() != null && request.getContent().length > 0) {
							SerialClient.SerialBean json = new Gson().fromJson(new String(request.getContent()), SerialClient.SerialBean.class);
							// Check if not there yet.
							Optional<NMEAClient> opClient = nmeaDataProviders.stream()
											.filter(channel -> channel instanceof SerialClient &&
															((SerialClient.SerialBean) ((SerialClient) channel).getBean()).getPort().equals(json.getPort()))
											.findFirst();
							if (!opClient.isPresent()) {
								try {
									NMEAClient serialClient = new SerialClient(this);
									serialClient.initClient();
									serialClient.setReader(new SerialReader(serialClient.getListeners(), json.getPort(), json.getBr()));
									nmeaDataProviders.add(serialClient);
									serialClient.startWorking();
									response = new HTTPServer.Response(request.getProtocol(), 200);
									String content = new Gson().toJson(serialClient.getBean());
									generateHappyResponseHeaders(response, content.length());
									response.setPayload(content.getBytes());
								} catch (Exception ex) {
									response = new HTTPServer.Response(request.getProtocol(), 400); // Default, Bad Request
									ex.printStackTrace();
								}
							} else {
								// Already there
								response = new HTTPServer.Response(request.getProtocol(), 400); // Default, Bad Request
							}
						} else {
							response = new HTTPServer.Response(request.getProtocol(), 400); // Default, Bad Request
						}
					} else if (type.equals("ws")) {                                                               // ws
						if (request.getContent() != null && request.getContent().length > 0) {
							WebSocketClient.WSBean json = new Gson().fromJson(new String(request.getContent()), WebSocketClient.WSBean.class);
							// Check if not there yet.
							Optional<NMEAClient> opClient = nmeaDataProviders.stream()
											.filter(channel -> channel instanceof SerialClient &&
															((WebSocketClient.WSBean) ((WebSocketClient) channel).getBean()).getWsUri().equals(json.getWsUri()))
											.findFirst();
							if (!opClient.isPresent()) {
								try {
									NMEAClient wsClient = new WebSocketClient(this);
									wsClient.initClient();
									wsClient.setReader(new WebSocketReader(wsClient.getListeners(), json.getWsUri()));
									nmeaDataProviders.add(wsClient);
									wsClient.startWorking();
									response = new HTTPServer.Response(request.getProtocol(), 200);
									String content = new Gson().toJson(wsClient.getBean());
									generateHappyResponseHeaders(response, content.length());
									response.setPayload(content.getBytes());
								} catch (Exception ex) {
									response = new HTTPServer.Response(request.getProtocol(), 400); // Default, Bad Request
									ex.printStackTrace();
								}
							} else {
								// Already there
								response = new HTTPServer.Response(request.getProtocol(), 400); // Default, Bad Request
							}
						} else {
							response = new HTTPServer.Response(request.getProtocol(), 400); // Default, Bad Request
						}
					} else if (type.equals("file")) {                                                             // data file
						if (request.getContent() != null && request.getContent().length > 0) {
							DataFileClient.DataFileBean json = new Gson().fromJson(new String(request.getContent()), DataFileClient.DataFileBean.class);
							// Check if not there yet.
							Optional<NMEAClient> opClient = nmeaDataProviders.stream()
											.filter(channel -> channel instanceof DataFileClient &&
															((DataFileClient.DataFileBean) ((DataFileClient) channel).getBean()).getFile().equals(json.getFile()))
											.findFirst();
							if (!opClient.isPresent()) {
								try {
									NMEAClient fileClient = new DataFileClient(this);
									fileClient.initClient();
									fileClient.setReader(new DataFileReader(fileClient.getListeners(), json.getFile()));
									nmeaDataProviders.add(fileClient);
									fileClient.startWorking();
									response = new HTTPServer.Response(request.getProtocol(), 200);
									String content = new Gson().toJson(fileClient.getBean());
									generateHappyResponseHeaders(response, content.length());
									response.setPayload(content.getBytes());
								} catch (Exception ex) {
									response = new HTTPServer.Response(request.getProtocol(), 400); // Default, Bad Request
									ex.printStackTrace();
								}
							} else {
								// Already there
								response = new HTTPServer.Response(request.getProtocol(), 400); // Default, Bad Request
							}
						} else {
							response = new HTTPServer.Response(request.getProtocol(), 400); // Default, Bad Request
						}
					} else if (type.equals("bme280")) {                                                           // bme280
						// Check if not there yet.
						Optional<NMEAClient> opClient = nmeaDataProviders.stream()
										.filter(channel -> channel instanceof BME280Client)
										.findFirst();
						if (!opClient.isPresent()) {
							try {
								NMEAClient bme280Client = new BME280Client(this);
								bme280Client.initClient();
								bme280Client.setReader(new BME280Reader(bme280Client.getListeners()));
								nmeaDataProviders.add(bme280Client);
								bme280Client.startWorking();
								response = new HTTPServer.Response(request.getProtocol(), 200);
								String content = new Gson().toJson(bme280Client.getBean());
								generateHappyResponseHeaders(response, content.length());
								response.setPayload(content.getBytes());
							} catch (Exception ex) {
								response = new HTTPServer.Response(request.getProtocol(), 400); // Default, Bad Request
								ex.printStackTrace();
							}
						} else {
							// Already there
							response = new HTTPServer.Response(request.getProtocol(), 400); // Default, Bad Request
						}
					} else if (type.equals("htu21df")) {                                                          // htu21df
						// Check if not there yet.
						Optional<NMEAClient> opClient = nmeaDataProviders.stream()
										.filter(channel -> channel instanceof HTU21DFClient)
										.findFirst();
						if (!opClient.isPresent()) {
							try {
								NMEAClient htu21dfClient = new HTU21DFClient(this);
								htu21dfClient.initClient();
								htu21dfClient.setReader(new HTU21DFReader(htu21dfClient.getListeners()));
								nmeaDataProviders.add(htu21dfClient);
								htu21dfClient.startWorking();
								response = new HTTPServer.Response(request.getProtocol(), 200);
								String content = new Gson().toJson(htu21dfClient.getBean());
								generateHappyResponseHeaders(response, content.length());
								response.setPayload(content.getBytes());
							} catch (Exception ex) {
								response = new HTTPServer.Response(request.getProtocol(), 400); // Default, Bad Request
								ex.printStackTrace();
							}
						} else {
							// Already there
							response = new HTTPServer.Response(request.getProtocol(), 400); // Default, Bad Request
						}
					} else if (type.equals("rnd")) {                                                              // rnd
						// Check if not there yet.
						Optional<NMEAClient> opClient = nmeaDataProviders.stream()
										.filter(channel -> channel instanceof RandomClient)
										.findFirst();
						if (!opClient.isPresent()) {
							try {
								NMEAClient rndClient = new RandomClient(this);
								rndClient.initClient();
								rndClient.setReader(new RandomReader(rndClient.getListeners()));
								nmeaDataProviders.add(rndClient);
								rndClient.startWorking();
								response = new HTTPServer.Response(request.getProtocol(), 200);
								String content = new Gson().toJson(rndClient.getBean());
								generateHappyResponseHeaders(response, content.length());
								response.setPayload(content.getBytes());
							} catch (Exception ex) {
								response = new HTTPServer.Response(request.getProtocol(), 400); // Default, Bad Request
								ex.printStackTrace();
							}
						} else {
							// Already there
							response = new HTTPServer.Response(request.getProtocol(), 400); // Default, Bad Request
						}
					} else {
						response = new HTTPServer.Response(request.getProtocol(), 404); // Default, Not implemented
					}
				}
				break;
			case "PUT": // Update on channels: verbose on/off
				postPathElem = request.getPath().split("/");
				type = "";
				if (request.getContent() == null) {
					response = new HTTPServer.Response(request.getProtocol(), 400); // No Content
				} else {
					Object bean = new GsonBuilder().create().fromJson(new String(request.getContent()), Object.class);
					if (bean instanceof Map) {
						type = ((Map<String, String>)bean).get("type");
					}
				}
				// PUT /channels, channel object in the payload
				if (postPathElem != null && postPathElem.length >= 2 && postPathElem[1].equals("channels")) {
					if (type.equals("serial")) {
						// Check existence
						if (request.getContent() != null && request.getContent().length > 0) {
							SerialClient.SerialBean json = new Gson().fromJson(new String(request.getContent()), SerialClient.SerialBean.class);
							Optional<NMEAClient> opClient = nmeaDataProviders.stream()
											.filter(channel -> channel instanceof SerialClient &&
															((SerialClient.SerialBean) ((SerialClient) channel).getBean()).getPort().equals(json.getPort()))
											.findFirst();
							if (!opClient.isPresent()) {
								response = new HTTPServer.Response(request.getProtocol(), 404); // Not found
							} else { // Then update
								response = new HTTPServer.Response(request.getProtocol(), 200);
								SerialClient serialClient = (SerialClient) opClient.get();
								serialClient.setVerbose(json.getVerbose());
								String content = new Gson().toJson(serialClient.getBean());
								generateHappyResponseHeaders(response, content.length());
								response.setPayload(content.getBytes());
							}
						}
					} else if (type.equals("file")) {
						// Check existence
						if (request.getContent() != null && request.getContent().length > 0) {
							DataFileClient.DataFileBean json = new Gson().fromJson(new String(request.getContent()), DataFileClient.DataFileBean.class);
							Optional<NMEAClient> opClient = nmeaDataProviders.stream()
											.filter(channel -> channel instanceof DataFileClient &&
															((DataFileClient.DataFileBean) ((DataFileClient) channel).getBean()).getFile().equals(json.getFile()))
											.findFirst();
							if (!opClient.isPresent()) {
								response = new HTTPServer.Response(request.getProtocol(), 404); // Not found
							} else { // Then update
								response = new HTTPServer.Response(request.getProtocol(), 200);
								DataFileClient dataFileClient = (DataFileClient) opClient.get();
								dataFileClient.setVerbose(json.getVerbose());
								String content = new Gson().toJson(dataFileClient.getBean());
								generateHappyResponseHeaders(response, content.length());
								response.setPayload(content.getBytes());
							}
						}
					} else if (type.equals("tcp")) {
						// Check existence
						if (request.getContent() != null && request.getContent().length > 0) {
							TCPClient.TCPBean json = new Gson().fromJson(new String(request.getContent()), TCPClient.TCPBean.class);
							Optional<NMEAClient> opClient = nmeaDataProviders.stream()
											.filter(channel -> channel instanceof TCPClient &&
															((TCPClient.TCPBean) ((TCPClient) channel).getBean()).getHostname().equals(json.getHostname()) &&
															((TCPClient.TCPBean) ((TCPClient) channel).getBean()).getPort() == json.getPort())
											.findFirst();
							if (!opClient.isPresent()) {
								response = new HTTPServer.Response(request.getProtocol(), 404); // Not found
							} else { // Then update
								response = new HTTPServer.Response(request.getProtocol(), 200);
								TCPClient tcpClient = (TCPClient) opClient.get();
								tcpClient.setVerbose(json.getVerbose());
								String content = new Gson().toJson(tcpClient.getBean());
								generateHappyResponseHeaders(response, content.length());
								response.setPayload(content.getBytes());
							}
						}
					} else if (type.equals("ws")) {
						// Check existence
						if (request.getContent() != null && request.getContent().length > 0) {
							WebSocketClient.WSBean json = new Gson().fromJson(new String(request.getContent()), WebSocketClient.WSBean.class);
							Optional<NMEAClient> opClient = nmeaDataProviders.stream()
											.filter(channel -> channel instanceof WebSocketClient &&
															((WebSocketClient.WSBean) ((WebSocketClient) channel).getBean()).getWsUri().equals(json.getWsUri()))
											.findFirst();
							if (!opClient.isPresent()) {
								response = new HTTPServer.Response(request.getProtocol(), 404); // Not found
							} else { // Then update
								response = new HTTPServer.Response(request.getProtocol(), 200);
								WebSocketClient webSocketClient = (WebSocketClient) opClient.get();
								webSocketClient.setVerbose(json.getVerbose());
								String content = new Gson().toJson(webSocketClient.getBean());
								generateHappyResponseHeaders(response, content.length());
								response.setPayload(content.getBytes());
							}
						}
					} else if (type.equals("bme280")) {
						// Check existence
						if (request.getContent() != null && request.getContent().length > 0) {
							BME280Client.BME280Bean json = new Gson().fromJson(new String(request.getContent()), BME280Client.BME280Bean.class);
							Optional<NMEAClient> opClient = nmeaDataProviders.stream()
											.filter(channel -> channel instanceof BME280Client)
											.findFirst();
							if (!opClient.isPresent()) {
								response = new HTTPServer.Response(request.getProtocol(), 404); // Not found
							} else { // Then update
								response = new HTTPServer.Response(request.getProtocol(), 200);
								BME280Client bme280Client = (BME280Client) opClient.get();
								bme280Client.setVerbose(json.getVerbose());
								String content = new Gson().toJson(bme280Client.getBean());
								generateHappyResponseHeaders(response, content.length());
								response.setPayload(content.getBytes());
							}
						}
					} else if (type.equals("htu21df")) {
						// Check existence
						if (request.getContent() != null && request.getContent().length > 0) {
							HTU21DFClient.HTU21DFBean json = new Gson().fromJson(new String(request.getContent()), HTU21DFClient.HTU21DFBean.class);
							Optional<NMEAClient> opClient = nmeaDataProviders.stream()
											.filter(channel -> channel instanceof HTU21DFClient)
											.findFirst();
							if (!opClient.isPresent()) {
								response = new HTTPServer.Response(request.getProtocol(), 404); // Not found
							} else { // Then update
								response = new HTTPServer.Response(request.getProtocol(), 200);
								HTU21DFClient htu21DFClient = (HTU21DFClient) opClient.get();
								htu21DFClient.setVerbose(json.getVerbose());
								String content = new Gson().toJson(htu21DFClient.getBean());
								generateHappyResponseHeaders(response, content.length());
								response.setPayload(content.getBytes());
							}
						}
					} else if (type.equals("rnd")) {
						// Check existence
						if (request.getContent() != null && request.getContent().length > 0) {
							RandomClient.RandomBean json = new Gson().fromJson(new String(request.getContent()), RandomClient.RandomBean.class);
							Optional<NMEAClient> opClient = nmeaDataProviders.stream()
											.filter(channel -> channel instanceof RandomClient)
											.findFirst();
							if (!opClient.isPresent()) {
								response = new HTTPServer.Response(request.getProtocol(), 404); // Not found
							} else { // Then update
								response = new HTTPServer.Response(request.getProtocol(), 200);
								RandomClient randomClient = (RandomClient) opClient.get();
								randomClient.setVerbose(json.getVerbose());
								String content = new Gson().toJson(randomClient.getBean());
								generateHappyResponseHeaders(response, content.length());
								response.setPayload(content.getBytes());
							}
						}
					}
				} else if (postPathElem != null && postPathElem.length >= 2 && postPathElem[1].equals("forwarders")) {
					// Anything to update here?

				} else if (postPathElem != null && postPathElem.length >= 4 && postPathElem[1].equals("mux") && postPathElem[2].equals("verbose")) {
					// PUT /mux/verbose/:val where val is 'on' or 'off', no payload.
					boolean newValue = "on".equals(postPathElem[3]);
					this.verbose = newValue;
					response = new HTTPServer.Response(request.getProtocol(), 200);
					String content = "";
					generateHappyResponseHeaders(response, content.length());
					response.setPayload(content.getBytes());
				}
				break;
			case "PATCH":
			default:
				break;
		}
		if (this.verbose) {
			System.out.println("======================================");
			System.out.println("Request :\n" + request.toString());
			System.out.println("Response :\n" + response.toString());
			System.out.println("======================================");
		}
		return response;
	}

	private void generateHappyResponseHeaders(HTTPServer.Response response, int contentLength) {
		Map<String, String> responseHeaders = new HashMap<>();
		responseHeaders.put("Content-Type", "application/json");
		responseHeaders.put("Content-Length", String.valueOf(contentLength));
		responseHeaders.put("Access-Control-Allow-Origin", "*");
		response.setHeaders(responseHeaders);
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

	@Override
	public synchronized void onData(String mess) {
		if (verbose) {
			System.out.println(">> From MUX: " + mess);
		}
		// Computers
		nmeaDataComputers.stream()
						.forEach(computer -> {
							computer.write(mess.getBytes());
						});
		// Forwarders
		nmeaDataForwarders.stream()
						.forEach(fwd -> {
							try {
								fwd.write(mess.getBytes());
							} catch (Exception e) {
								e.printStackTrace();
							}
						});
	}

	private final static NumberFormat MUX_IDX_FMT = new DecimalFormat("00");
	private boolean verbose = false;

	public GenericNMEAMultiplexer(Properties muxProps) {
		// Read initial config from the properties file.
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
					default:
						throw new RuntimeException(String.format("forward type [%s] not supported yet.", type));
				}
			}
			fwdIdx++;
		}
		// Init cache?
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
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		// Computers
		nmeaDataComputers.add(new TrueWindComputer(this)); // For tests for now

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
