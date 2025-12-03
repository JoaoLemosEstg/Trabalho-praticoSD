package traffic.net;

import java.io.ObjectOutputStream;
import java.net.Socket;

public class MessageSender {

    public static void send(Message msg) throws Exception {
        int port = PortConfig.getPort(msg.getTo());
        try (Socket socket = new Socket("localhost", port);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())) {

            out.writeObject(msg);
            out.flush();
        }
    }
}
