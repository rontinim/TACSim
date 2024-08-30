/*import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.Edge;
import org.graphstream.graph.implementations.SingleGraph;
import javax.swing.SwingUtilities;

public class GraphVisualizer {

    public void visualize(Graph_Dijkstra myGraph) {
        SwingUtilities.invokeLater(() -> {
            System.setProperty("org.graphstream.ui", "org.graphstream.ui.swing");
            Graph graph = new SingleGraph("Graph Visualization");
            graph.setAttribute("ui.stylesheet", styleSheet());

            for (Node_Dijkstra node : myGraph.getNodesMap().values()) {
                Node graphNode = graph.addNode(Long.toString(node.id));
                graphNode.setAttribute("ui.label", Long.toString(node.id));
            }

            for (Long nodeId : myGraph.adjacencyList.keySet()) {
                for (Edges_Dijkstra edge : myGraph.adjacencyList.get(nodeId)) {
                    String edgeId = edge.source.id + "_" + edge.destination.id;
                    if (graph.getEdge(edgeId) == null) {
                        graph.addEdge(edgeId, Long.toString(edge.source.id), Long.toString(edge.destination.id), true);
                    }
                }
            }

            graph.setAttribute("ui.quality");
            graph.setAttribute("ui.antialias");
            graph.addAttribute("ui.stylesheet", styleSheet());
            graph.display(true);
            System.setProperty("org.graphstream.ui", "org.graphstream.ui.swing.util.Display");
            System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
            Viewer viewer = graph.display(true);
            viewer.disableAutoLayout();

        });
    }

    private String styleSheet() {
        return "node { fill-color: black; size: mapData(20, 50, 10px, 20px); text-alignment: above; text-size: 16px; text-color: blue; }" +
               "edge { fill-color: grey; size: mapData(1, 5, 1px, 2px); }";
    }
    

    public static void main(String[] args) {
        Graph_Dijkstra graph = GraphPersistanSingleton.getInstance();
        GraphVisualizer visualizer = new GraphVisualizer();
        visualizer.visualize(graph);
    }
}
*/