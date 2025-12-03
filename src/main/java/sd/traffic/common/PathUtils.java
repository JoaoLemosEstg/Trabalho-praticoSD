package sd.traffic.common;

import java.util.List;
import java.util.Random;

public class PathUtils {

    private static final Random RNG = new Random();

    public static List<String> randomPathForEntry(String entry) {
        double p = RNG.nextDouble();

        return switch (entry) {
            case "E1" -> {
                if (p < 0.34)
                    yield List.of("E1", "Cr1", "Cr4", "Cr5", "S");
                else if (p < 0.34 + 0.33)
                    yield List.of("E1", "Cr1", "Cr2", "Cr5", "S");
                else
                    yield List.of("E1", "Cr1", "Cr2", "Cr3", "S");
            }
            case "E2" -> {
                if (p < 0.34)
                    yield List.of("E2", "Cr2", "Cr5", "S");
                else if (p < 0.34 + 0.33)
                    yield List.of("E2", "Cr2", "Cr3", "S");
                else
                    yield List.of("E2", "Cr2", "Cr1", "Cr4", "Cr5", "S");
            }
            case "E3" -> {
                if (p < 0.34)
                    yield List.of("E3", "Cr3", "S");
                else if (p < 0.34 + 0.33)
                    yield List.of("E3", "Cr3", "Cr2", "Cr5", "S");
                else
                    yield List.of("E3", "Cr3", "Cr2", "Cr1", "Cr4", "Cr5", "S");
            }
            default -> throw new IllegalArgumentException("Entry inválida: " + entry);
        };
    }
}
