public class DataPoint {
    private double startLatitude;
    private double startLongitude;
    private double endLatitude;
    private double endLongitude;
    private long processTime;
    private long processRam;
    private double processCPU;
    private int distance;

    public DataPoint(double startLatitude, double startLongitude, double endLatitude, double endLongitude,
                     long processTime, long processRam, int distance) {
        this.startLatitude = startLatitude;
        this.startLongitude = startLongitude;
        this.endLatitude = endLatitude;
        this.endLongitude = endLongitude;
        this.processTime = processTime;
        this.processRam = processRam;
        this.distance = distance;
    }

    // Getter methods for each variable
    public double getStartLatitude() { return startLatitude; }
    public double getStartLongitude() { return startLongitude; }
    public double getEndLatitude() { return endLatitude; }
    public double getEndLongitude() { return endLongitude; }
    public long getProcessTime() { return processTime; }
    public long getProcessRam() { return processRam; }
    public double getProcessCPU() { return processCPU; }
    public double getDistance() { return distance; }

    @Override
    public String toString() {
        return "DataPoint{" +
               "startLatitude=" + startLatitude +
               ", startLongitude=" + startLongitude +
               ", endLatitude=" + endLatitude +
               ", endLongitude=" + endLongitude +
               ", processTime=" + processTime +
               ", processRam=" + processRam +
               ", processCPU=" + processCPU +
               ", distance=" + distance +
               '}';
    }
}
