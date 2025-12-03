package traffic;

import traffic.intersection.*;
import traffic.model.NodeId;
import traffic.sink.SinkProcess;
import traffic.generator.VehicleGenerator;

public class Main {

    public static void main(String[] args) {

        // Lançar o SinkProcess
        new Thread(() -> {
            try {
                System.out.println("Starting SinkProcess...");
                SinkProcess.main(null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        // Lançar os cruzamentos
        startIntersection("CR1", NodeId.CR1);
        startIntersection("CR2", NodeId.CR2);
        startIntersection("CR3", NodeId.CR3);
        startIntersection("CR4", NodeId.CR4);
        startIntersection("CR5", NodeId.CR5);

        // Esperar 1 segundo só para garantir que todos os processos arrancam
        try { Thread.sleep(1000); } catch (Exception ignored) {}

        // Lançar o Gerador de veículos
        new Thread(() -> {
            try {
                System.out.println("Starting VehicleGenerator...");
                VehicleGenerator.main(null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private static void startIntersection(String name, NodeId id) {
        new Thread(() -> {
            try {
                System.out.println("Starting " + name + "...");
                new IntersectionProcess(id).start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, name + "-Thread").start();
    }
}
