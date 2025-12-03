package traffic.sink;

import traffic.model.NodeId;
import traffic.model.Vehicle;
import traffic.net.Message;
import traffic.net.MessageType;
import traffic.net.PortConfig;

import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class SinkProcess {

    public static void main(String[] args) throws Exception {
        int port = PortConfig.getPort(NodeId.S);

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("[S] listening on port " + port);

            while (true) {
                Socket client = serverSocket.accept();
                new Thread(() -> handle(client)).start();
            }
        }
    }

    private static void handle(Socket client) {
        try (ObjectInputStream in = new ObjectInputStream(client.getInputStream())) {

            Message msg = (Message) in.readObject();
            if (msg.getType() == MessageType.VEHICLE_EXIT) {
                Vehicle v = msg.getVehicle();
                System.out.println("[S] Vehicle arrived at sink: " + v.getId());
            } else {
                System.out.println("[S] Received non-exit message: " + msg);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
