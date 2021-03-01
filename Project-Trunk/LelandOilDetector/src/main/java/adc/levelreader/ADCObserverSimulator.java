package adc.levelreader;

import adc.ADCContext;
import adc.ADCObserver;
import adc.levelreader.main.LelandPrototype;
import analogdigitalconverter.mcp.MCPReader;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONObject;

import java.net.URI;

/**
 * Used to simulate the behavior of the ADC reader.
 * Listens to w WebSocket, feed through another channel (see feeder.html & al)
 */
public class ADCObserverSimulator
        extends ADCObserver {
    private static MCPReader.MCP3008InputChannels[] adcChannel;
    private static WebSocketClient webSocketClient = null;

    private static int currentWaterLevel = -1,
            currentOilLevel = -1;

    public ADCObserverSimulator() {
        super();
    }

    public ADCObserverSimulator(MCPReader.MCP3008InputChannels[] mcp3008InputChannels) {
        super(mcp3008InputChannels);
        adcChannel = mcp3008InputChannels;
        initWebSocketConnection(LelandPrototype.getAppProperties().getProperty("ws.uri", "ws://localhost:9876/"));
    }

    private static void broadcastLevels(int wl, int ol) {
//  LelandPrototype.displayAppMess("           >>> Sim >>>  WL: " + wl + ", OL: " + ol);
        currentWaterLevel = wl;
        currentOilLevel = ol;
    }

    private void simulateADCRead() {
        int wl = currentWaterLevel;
        int ol = currentOilLevel;

        for (int chan = 0; chan <= wl; chan++) {
//    System.out.println("   -- Channel " + chan + " : Water");
            ADCContext.getInstance().fireValueChanged(adcChannel[chan], (int) Math.round(1023 * 0.80)); // Water
            //   try { Thread.sleep(100); } catch (InterruptedException ie) {}
        }
        for (int chan = wl + 1; chan <= ol; chan++) {
//    System.out.println("   -- Channel " + chan + " : Oil");
            ADCContext.getInstance().fireValueChanged(adcChannel[chan], (int) Math.round(1023 * 0.40)); // Oil
            //   try { Thread.sleep(100); } catch (InterruptedException ie) {}
        }
        for (int chan = ol + 1; chan <= 6; chan++) {
//    System.out.println("   -- Channel " + chan + " : Air");
            ADCContext.getInstance().fireValueChanged(adcChannel[chan], 0);                           // Air
            //   try { Thread.sleep(100); } catch (InterruptedException ie) {}
        }
    }

    private static void initWebSocketConnection(String serverURI) {
        try {
            webSocketClient = new WebSocketClient(new URI(serverURI)) {
                @Override
                public void onOpen(ServerHandshake serverHandshake) {
                    LelandPrototype.displayAppMess("WS On Open");
                }

                @Override
                public void onMessage(String content) {
//        LelandPrototype.displayAppMess("WS On Message:" + content);
                    try {
                        JSONObject json = new JSONObject(content);
                        if (json.getString("type").equals("message")) {
                            JSONObject data = json.getJSONObject("data");
                            String text = data.getString("text").replaceAll("&quot;", "\"");
                            try {
                                JSONObject payload = new JSONObject(text);
                                JSONObject feed = payload.getJSONObject("feed");
                                if (feed != null) {
                                    int wl = feed.getInt("waterlevel");
                                    int ol = feed.getInt("oillevel");
                                    broadcastLevels(wl, ol);
                                }
                            } catch (Exception ignore) {
                            }
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }

                @Override
                public void onClose(int i, String string, boolean b) {
                    LelandPrototype.displayAppMess("WS On Close");
                }

                @Override
                public void onError(Exception exception) {
                    LelandPrototype.displayAppMess("WS On Error");
                    //      exception.printStackTrace();
                }
            };
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void start(int tol, long pause) {
        // super.start(i, l);
        webSocketClient.connect();
        Thread simADC = new Thread() {
            public void run() {
                while (true) {
                    simulateADCRead();
                    try {
                        Thread.sleep(1_000);
                    } catch (InterruptedException ie) {
                    }
                }
            }
        };
        simADC.start();
    }
}
