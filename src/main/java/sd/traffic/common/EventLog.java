package sd.traffic.common;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Logger simples de eventos da simulação.
 * Escreve cada evento numa linha do ficheiro "events.log".
 */
public class EventLog {

    private static final String LOG_FILE = "events.log";
    private static final Object LOCK = new Object();
    private static final SimpleDateFormat FMT =
            new SimpleDateFormat("HH:mm:ss.SSS");

    /**
     * Regista um evento.
     *
     * @param nodeId    ID do nó (E1, Cr1, S, ...)
     * @param vehicleId ID do veículo (pode ser null para eventos sem veículo)
     * @param type      tipo do evento (ex: ARRIVAL_SYSTEM, QUEUE_ENTER, ...)
     * @param details   texto livre com mais informação
     */
    public static void log(String nodeId, String vehicleId,
                           String type, String details) {
        long ts = System.currentTimeMillis();
        String timeStr = FMT.format(new Date(ts));

        String line = String.format(
                "%s | node=%s | veh=%s | %-16s | %s",
                timeStr,
                nodeId,
                vehicleId == null ? "-" : vehicleId,
                type,
                details == null ? "" : details
        );

        synchronized (LOCK) {
            try (PrintWriter out = new PrintWriter(new FileWriter(LOG_FILE, true))) {
                out.println(line);
            } catch (IOException e) {
                System.err.println("Erro ao escrever no log de eventos: " + e.getMessage());
            }
        }
    }
}
