package sd.traffic.node;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;

public class NodeMain {

    public static void main(String[] args) throws Exception {

        if (args.length != 1) {
            System.err.println("Uso: java ... NodeMain <config-file.json>");
            System.exit(1);
        }

        ObjectMapper mapper = new ObjectMapper();
        CrossroadConfig config = mapper.readValue(new File(args[0]), CrossroadConfig.class);

        // ============================
        //        IDENTIFICAR O NÓ
        // ============================

        String id = config.nodeId;

        // ------------------------------------
        // Caso 1 → Nó de Entrada (E1/E2/E3)
        // ------------------------------------
        if ("ENTRY_NODE".equals(id)) {

            System.out.println(">>> Iniciando nó de entradas...");


            ArrivalGenerator g1 = new ArrivalGenerator("E1", 0.3,
                    "Cr1", "localhost", 5001);
            ArrivalGenerator g2 = new ArrivalGenerator("E2", 0.3,
                    "Cr2", "localhost", 5002);
            ArrivalGenerator g3 = new ArrivalGenerator("E3", 0.3,
                    "Cr3", "localhost", 5003);

            g1.start();
            g2.start();
            g3.start();

            return;
        }

        // ------------------------------------
        // Caso 2 → Nó final S (sink)
        // ------------------------------------
        if ("S".equals(id)) {

            System.out.println(">>> Iniciando nó S (sumidouro / saída)...");

            SinkProcess sink = new SinkProcess(config);
            sink.start();
            return;
        }

        // ------------------------------------
        // Caso 3 → Nó normal (Cr1, Cr2, Cr3, Cr4, Cr5)
        // ------------------------------------

        System.out.println(">>> Iniciando cruzamento " + id + "...");

        CrossroadProcess process = new CrossroadProcess(config);
        process.start();
    }
}
