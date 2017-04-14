package raspisamples;

import java.net.URI;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONObject;

/*
 * Driven by WesbSocket server
 * See in node/server.js
 * 
 * 2 Servos (UP/LR)
 * 
 * Web interface available, see in node/servo.pilot.html
 * 
 * Start the WebSocket node server,
 * Start the script named pantilt.ws
 */
public class PanTiltWebSocketClient
{
  private static WebSocketClient webSocketClient = null;  
  
  public static void main(String[] args) throws Exception
  {
    String wsUri = System.getProperty("ws.uri", "ws://localhost:9876/");     
    float yaw = 0f;
    if (args.length == 0)
    {
      throw new IllegalArgumentException("Need the yaw value as parameter");
    }
    else
    {
      try
      {
        yaw = Float.parseFloat(args[0]);
      }
      catch (NumberFormatException nfe)
      {
        nfe.printStackTrace();
        System.exit(1);
      }
    }
    initWebSocketConnectionAndSend(wsUri, yaw);
    /*
    boolean sent = false;
    try { Thread.sleep(500L); } catch (Exception e) {}
    JSONObject mess = new JSONObject();
    mess.put("yaw", yaw);
    System.out.println("Sending message:" + mess.toString());
    while (!sent)
    {
      try
      {
        webSocketClient.send(mess.toString());
        System.out.println("... sent!");
        sent = true;
      }
      catch (NotYetConnectedException nyce)
      {
        System.out.println("   Waiting...");
        try { Thread.sleep(500L); } catch (Exception e) {}
      }
    }
    // And bye
    System.out.println("Closing.");
    webSocketClient.close(); */
    System.out.println("Done, bye");
  }

  private static void initWebSocketConnectionAndSend(String serverURI, final float yaw)
  {
    try
    {
      webSocketClient = new WebSocketClient(new URI(serverURI))
      {
        @Override
        public void onOpen(ServerHandshake serverHandshake)
        {
          System.out.println("WS On Open");
          // Sending message
          JSONObject mess = new JSONObject();
          mess.put("yaw", yaw);
          System.out.println("Sending...");
          send(mess.toString());
          System.out.println("Closing...");
          close();
        }

        @Override
        public void onMessage(String string)
        {
        }

        @Override
        public void onClose(int i, String string, boolean b)
        {
          System.out.println("WS On Close");
        }

        @Override
        public void onError(Exception exception)
        {
          System.out.println("WS On Error");
          exception.printStackTrace();
        }
      }; 
      webSocketClient.connect();
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }    
  }

  public static void close()
  {
    System.out.println("\nExiting...");
    webSocketClient.close();
    System.out.println("Bye");
  }
  
}
