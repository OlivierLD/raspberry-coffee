package oliv.tcp.chat;

import java.io.Closeable;
import java.net.Socket;

public interface ServerInterface extends Closeable {
    void onMessage(byte[] message, Socket sender);
    void close();
}
