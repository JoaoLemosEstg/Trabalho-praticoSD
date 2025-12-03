package traffic.intersection;

import traffic.model.NodeId;

public class Cr1Main {
    public static void main(String[] args) throws Exception {
        new IntersectionProcess(NodeId.CR1).start();
    }
}
