package traffic.generator;

import traffic.model.*;
import traffic.net.*;

import java.util.List;

public class VehicleGenerator {

    public static void main(String[] args) throws Exception {

        
        List<NodeId> path = List.of(
                NodeId.E1, NodeId.CR1, NodeId.CR2, NodeId.CR5, NodeId.S
        );

        Vehicle v = new Vehicle(
                VehicleType.CARRO,
                NodeId.E1,
                path,
                0.0
        );

        Message msg = new Message(
                MessageType.VEHICLE_ARRIVAL,
                NodeId.E1,
                NodeId.CR1,
                v
        );

        MessageSender.send(msg);

        System.out.println("Vehicle sent!");
    }
}
