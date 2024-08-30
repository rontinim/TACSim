public class ShortestPaths { //ora non usata!!!!!!!!!!

    public double calculateDistanceDijkstra(GPSCoordinate v1, GPSCoordinate v2) {
        Dijkstra dijkstra = new Dijkstra();
        double totalDistance = dijkstra.calculateDistance(v1, v2);
        
        if (totalDistance != 0.1) { // Assumi che 0.1 sia il valore di default per "percorso non trovato"
            System.out.println("total: " + totalDistance + "km");
        } else {
            System.out.println("Nessun percorso trovato.");
        }
        
        return totalDistance;
    }
}
