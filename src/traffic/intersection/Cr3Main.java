package traffic.intersection;

import traffic.model.NodeId;

public class Cr3Main {
    public static void main(String[] args) throws Exception {
        new IntersectionProcess(NodeId.CR3).start();
    }
}
