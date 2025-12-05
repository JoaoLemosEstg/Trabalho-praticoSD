package sd.traffic.dashboard;

import sd.traffic.common.StatsSnapshot;

import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DashboardServer {

    private final int port;
    private final Map<String, StatsSnapshot> lastStats = new ConcurrentHashMap<>();

    public DashboardServer(int port) {
        this.port = port;
    }

    public void start() throws Exception {
        ServerSocket server = new ServerSocket(port);
        System.out.println("Dashboard server a ouvir na porta " + port);

        Thread printer = new Thread(this::printLoop);
        printer.setDaemon(true);
        printer.start();

        while (true) {
            Socket s = server.accept();
            new Thread(() -> handleConnection(s)).start();
        }
    }

    private void handleConnection(Socket s) {
        try (ObjectInputStream in = new ObjectInputStream(s.getInputStream())) {
            StatsSnapshot snap = (StatsSnapshot) in.readObject();
            lastStats.put(snap.nodeId, snap);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void printLoop() {
        while (true) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException ignored) {}

            System.out.println("===== DASHBOARD =====");

            for (StatsSnapshot ss : lastStats.values()) {
                if ("S".equals(ss.nodeId)) {
                    // Nó S → estatísticas globais da simulação
                    System.out.println(">> Nó S (estatísticas globais)");

                    printGlobalLine(
                            "CARRO",
                            ss.processedCarro,
                            ss.MinCarro,
                            ss.AvgCarro,
                            ss.MaxCarro
                    );

                    printGlobalLine(
                            "MOTO ",
                            ss.processedMoto,
                            ss.MinMoto,
                            ss.AvgMoto,
                            ss.MaxMoto
                    );

                    printGlobalLine(
                            "CAMIAO",
                            ss.processedCamiao,
                            ss.MinCamiao,
                            ss.AvgCamiao,
                            ss.MaxCamiao
                    );

                } else {
                    // Nó de cruzamento normal
                    System.out.printf(
                            "Nó %-3s | filaAtual=%4d  maxFila=%4d  avgFila=%6.2f  totalProc=%6d  |  MOTO=%4d  CARRO=%4d  CAMIAO=%4d%n",
                            ss.nodeId,
                            ss.currentQueue,
                            ss.maxQueue,
                            ss.avgQueue,
                            ss.totalVehiclesProcessed,
                            ss.processedMoto,
                            ss.processedCarro,
                            ss.processedCamiao
                    );
                }
            }

            System.out.println("=====================");
        }
    }

    // ajuda a formatar a linha de estatísticas globais do nó S
    private void printGlobalLine(String label, long count, long min, long avg, long max) {
        if (count == 0) {
            System.out.printf("  %-6s | count=0  dwell(ms)= - / - / - %n", label);
        } else {
            System.out.printf(
                    "  %-6s | count=%4d  dwell(ms)= min=%6d  avg=%6d  max=%6d%n",
                    label, count, min, avg, max
            );
        }
    }

    public static void main(String[] args) throws Exception {
        int port = 6000;
        new DashboardServer(port).start();
    }
}
