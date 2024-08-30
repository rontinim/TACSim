import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Graph_Dijkstra {
    Map<Long, Node_Dijkstra> nodes = new HashMap<>();
    Map<Long, List<Edges_Dijkstra>> adjacencyList = new HashMap<>();
    Map<String, Long> coordinatesToNodeId = new HashMap<>();

    void addNode(Node_Dijkstra node) {
        nodes.put(node.id, node);
        adjacencyList.put(node.id, new ArrayList<>());
        String coordinateKey = createCoordinateKey(node.lat, node.lon);
        coordinatesToNodeId.put(coordinateKey, node.id);
    }

    void addEdge(long sourceId, long destinationId) {
        Node_Dijkstra source = nodes.get(sourceId);
        Node_Dijkstra destination = nodes.get(destinationId);
        Edges_Dijkstra edge = new Edges_Dijkstra(source, destination);
        adjacencyList.get(sourceId).add(edge);
    }

    public Map<Long, Node_Dijkstra> getNodesMap() {
        return nodes;
    }

    public Node_Dijkstra getNode(Long nodeId) {
        return nodes.get(nodeId);
    }

    public int getNodeCount() {
        return nodes.size();
    }
    
    public int getEdgeCount() {
        int edgeCount = 0;
        for (List<Edges_Dijkstra> edgeList : adjacencyList.values()) {
            edgeCount += edgeList.size();
        }
        return edgeCount;
    }    

    public Long findNodeIdByCoordinates(double lat, double lon) {
        String coordinateKey = createCoordinateKey(lat, lon);
        return coordinatesToNodeId.getOrDefault(coordinateKey, 32339714L); // Usa il valore di default se non trovato
    }

    private String createCoordinateKey(double lat, double lon) {
        return lat + "," + lon;
    }

    public List<Node_Dijkstra> getNeighbors(Node_Dijkstra node) { //per nodi adiacenti
        List<Node_Dijkstra> neighbors = new ArrayList<>();
        List<Edges_Dijkstra> edges = adjacencyList.get(node.getId());
        if (edges != null) {
            for (Edges_Dijkstra edge : edges) {
                neighbors.add(edge.destination);
            }
        }
        return neighbors;
    }
    
}




