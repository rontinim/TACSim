public class PathResult {
    private long sourceId;
    private long destinationId;
    private double distance;

    public PathResult(long sourceId, long destinationId, double distance) {
        this.sourceId = sourceId;
        this.destinationId = destinationId;
        this.distance = distance;
    }

    public long getSourceId() {
        return sourceId;
    }

    public long getDestinationId() {
        return destinationId;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    // Metodo per verificare se il risultato corrisponde a una data coppia di nodi
    public boolean matches(long sourceId, long destinationId) {
        return this.sourceId == sourceId && this.destinationId == destinationId;
    }
}
