package traffic.model;

public enum VehicleType {
    MOTO(0.5),   
    CARRO(1.0),  
    CAMIAO(2.0); 

    private final double timeFactor;

    VehicleType(double timeFactor) {
        this.timeFactor = timeFactor;
    }

    public double getTimeFactor() {
        return timeFactor;
    }
}
