package utils.proxyguisample;

import http.HTTPServer;
import http.client.HTTPClient;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.util.Map;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * Minimal Swing GUI for a proxy
 */
public class ProxyGUI extends JFrame{

	TrafficRawPanel requestPanel = null;
	TrafficRawPanel responsePanel = null;

	public static void main(String... args) throws Exception {
		ProxyGUI proxyGui = new ProxyGUI();

		HTTPServer httpServer = new HTTPServer(9999);

		httpServer.setProxyFunction(proxyGui::proxyImpl);

		httpServer.startServer();
		System.out.println("Proxy Started");
	}

	public ProxyGUI(){
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setSize(600, 500);
		this.setTitle("HTTP Traffic");
		this.setLayout(new BorderLayout());

		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension frameSize = this.getSize();
		if (frameSize.height > screenSize.height) {
			frameSize.height = screenSize.height;
		}
		if (frameSize.width > screenSize.width) {
			frameSize.width = screenSize.width;
		}
		this.setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);
		this.setVisible(true);

		JPanel container = new JPanel();
		requestPanel = new TrafficRawPanel("Request");
		responsePanel = new TrafficRawPanel("Response");

//		requestPanel.addData("Requests");
//		responsePanel.addData("Responses");

		container.setLayout(new GridLayout(1,2));
		container.add(requestPanel);
		container.add(responsePanel);

		this.add(container);
		this.pack();
	}

	private HTTPServer.Response proxyImpl(HTTPServer.Request request) {

		// Dump the request in its frame
		// headers
		if (request.getHeaders() != null) {
			Map<String, String> respHeaders = request.getHeaders();
			respHeaders.keySet().forEach(k -> requestPanel.addData(String.format("%s: %s", k, respHeaders.get(k))));
		}
		// content?
		if (request.getContent() != null && request.getContent().length > 0) {
			if (request.getHeaders() != null && request.getHeaders().get("Content-Type") != null && HTTPServer.isText(request.getHeaders().get("Content-Type"))) {
				String requestPayload = new String(request.getContent());
				requestPanel.addData(String.format("%s", requestPayload));
			}
		}
		// An HTTPClient makes the received request, and returns the response
		HTTPServer.Response response = null;
		try {
			response = HTTPClient.doRequest(request);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
//	String rCode = String.format("Response code: %d", response.getStatus());
//	responsePanel.addData(rCode);
		if (response.getHeaders() != null) {
			Map<String, String> respHeaders = response.getHeaders();
			respHeaders.keySet().forEach(k -> responsePanel.addData(String.format("%s: %s", k, respHeaders.get(k))));
		}
		if (response.getPayload() != null) {
			if (response.getHeaders() != null && response.getHeaders().get("Content-Type") != null && HTTPServer.isText(response.getHeaders().get("Content-Type"))) {
				String responsePayload = new String(response.getPayload());
				responsePanel.addData(String.format("%s", responsePayload));
			}
		}
		responsePanel.addData("");
		return response;
	}
}
