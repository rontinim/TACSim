import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class GraphPersistanSingleton {

    private static volatile Graph_Dijkstra instance;
    private GraphPersistanSingleton() {}

    public static Graph_Dijkstra getInstance() {
        if (instance == null) {
            synchronized (GraphPersistanSingleton.class) {
                if (instance == null) {
                    instance = createGraph();
                }
            }
        }
        return instance;
    }
    public static void resetInstance() {
        synchronized (GraphPersistanSingleton.class) {
            instance = null;
        }
    }

    private static Graph_Dijkstra createGraph() {
        Graph_Dijkstra graph = new Graph_Dijkstra();

        // Carica nodi dal file CSV
        try (BufferedReader brNode = new BufferedReader(new FileReader("Nodes.csv"))) {
            String line = brNode.readLine();
            while ((line = brNode.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                } else {
                    String[] values = line.split(",");
                    long id = Long.parseLong(values[0]);
                    double lat = Double.parseDouble(values[1]);
                    double lon = Double.parseDouble(values[2]);
                    graph.addNode(new Node_Dijkstra(id, lat, lon));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Carica archi dal file CSV
        try (BufferedReader brEdge = new BufferedReader(new FileReader("Edges.csv"))) {
            String line = brEdge.readLine();
            Long prevNodeId = null;
            Long firstNodeId = null;
            long currentWayId = -1;

            while ((line = brEdge.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                String[] values = line.split(",");
                long wayId = Long.parseLong(values[0]);
                long nodeId = Long.parseLong(values[1]);

                if (wayId != currentWayId) {
                    prevNodeId = null;
                    firstNodeId = nodeId;
                    currentWayId = wayId;
                }

                if (prevNodeId != null) {
                    graph.addEdge(prevNodeId, nodeId);
                }

                prevNodeId = nodeId;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return graph;
    }
    
}
