package oliv.events;

import java.net.Socket;

public interface ServerInterface {
    void onMessage(byte[] message, Socket sender);
    void close();
}
