package traffic.intersection;

import traffic.model.NodeId;

public class Cr4Main {
    public static void main(String[] args) throws Exception {
        new IntersectionProcess(NodeId.CR4).start();
    }
}
