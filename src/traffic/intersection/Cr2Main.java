package traffic.intersection;

import traffic.model.NodeId;

public class Cr2Main {
    public static void main(String[] args) throws Exception {
        new IntersectionProcess(NodeId.CR2).start();
    }
}
