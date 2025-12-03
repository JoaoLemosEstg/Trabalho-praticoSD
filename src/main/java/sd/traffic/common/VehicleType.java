package sd.traffic.common;

public enum VehicleType {
    MOTO,
    CARRO,
    CAMIAO;

    public double travelTimeFactor() {
        // tmoto = 0.5 * tcarro ; tcaminhão = 4 * tmoto = 2 * tcarro
        return switch (this) {
            case MOTO -> 0.5;
            case CARRO -> 1.0;
            case CAMIAO -> 2.0;
        };
    }
}
