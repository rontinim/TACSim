import java.util.Objects;

public class Node_Dijkstra {
        long id;
        double lat, lon, distance;
        /*long version; 

        public Node_Dijkstra(long id, double lat, double lon, double distance, long version) {
            this.id = id;
            this.lat = lat;
            this.lon = lon;
            this.distance = distance;
            this.version = version;
        }*/
        Node_Dijkstra(long id, double lat, double lon) {
            this.id = id;
            this.lat = lat;
            this.lon = lon;
            this.distance = 0;
        }

        public long getId() {
            return id;
        }
    
        public double getLatitudine() {
            return lat;
        }
    
        public double getLongitudine() {
            return lon;
        }
        public double getDistance(){
            return distance;
        }
        public double setDistance(double dist){
            this.distance = dist;
            return this.distance;
        }
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Node_Dijkstra that = (Node_Dijkstra) o;
            return id == that.id;
        }

        @Override
        public int hashCode() {
            return Objects.hash(id);
        }

}
