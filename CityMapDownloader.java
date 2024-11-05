import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;


public class CityMapDownloader {
    public static int numTasks=100;
    private static long maxRamUsed = 0; 
    public static String cityName;
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Si prega di fornire il nome della città.");
            return;
        }
        cityName = args[0];

        String query = createOverpassQuery(cityName);
        String osmData = fetchOSMData(query);

        if (osmData != null) {
            convertToCSV(osmData, "Nodes.csv", false); // False per i nodi
            convertToCSV(osmData, "Edges.csv", true); // True per gli archi
        }
    }
    public void alternativeMain(String citta){
        if (citta == null) {
            System.out.println("Si prega di fornire il nome della città.");
            return;
        }
        cityName = citta;
        String query = createOverpassQuery(cityName);
        String osmData = fetchOSMData(query);
        if (osmData != null) {
            convertToCSV(osmData, "Nodes.csv", false); // False per i nodi
            convertToCSV(osmData, "Edges.csv", true); // True per gli archi
        }
    }
    private static String createOverpassQuery(String cityName) {
        return "[out:json];"
               + "area[name=\"" + cityName + "\"]->.searchArea;"
               + "(way(area.searchArea)[\"highway\"];);"
               + "(._;>;);"
               + "out body;";
    }

    private static String fetchOSMData(String query) {
        try {
            String apiUrl = "https://lz4.overpass-api.de/api/interpreter";
            URL url = new URL(apiUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = query.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
                StringBuilder content = new StringBuilder();
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                }
                in.close();

                long memoryUsed = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
                updateMaxMemoryUsage(memoryUsed);

                return content.toString();
            } else {
                System.out.println("Errore nella chiamata API OSM: " + responseCode);
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void convertToCSV(String osmData, String outputFileName, boolean forEdges) {
    try {
        File file = new File(outputFileName);
        // Usa FileWriter per sovrascrivere il file esistente
        FileWriter fileWriter = new FileWriter(file, false);
        PrintWriter writer = new PrintWriter(fileWriter);

        JSONObject jsonData = new JSONObject(osmData);
        JSONArray elements = jsonData.getJSONArray("elements");

        if (forEdges) {
            writer.println("way_id,node_id");
            for (int i = 0; i < elements.length(); i++) {
                JSONObject element = elements.getJSONObject(i);
                if (element.getString("type").equals("way")) {
                    long wayId = element.getLong("id");
                    JSONArray nodes = element.getJSONArray("nodes");
                    for (int j = 0; j < nodes.length(); j++) {
                        writer.println(wayId + "," + nodes.getLong(j));
                    }
                }
            }
        } else {
            writer.println("node_id,lat,lon");
            for (int i = 0; i < elements.length(); i++) {
                JSONObject element = elements.getJSONObject(i);
                if (element.getString("type").equals("node")) {
                    long nodeId = element.getLong("id");
                    double lat = element.getDouble("lat");
                    double lon = element.getDouble("lon");
                    writer.println(nodeId + "," + lat + "," + lon);
                }
            }
        }

            writer.close();
            System.out.println("File CSV " + outputFileName + " generato con successo.");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("Errore durante la scrittura del file CSV.");
            e.printStackTrace();
        }
    }

    public void setTask(int tatks){
        this.numTasks = tatks;
    }

    public void createTxt() {
        String directoryPath = "D:/tacSim/TACSim/taskBlock";
        String inputFileName = directoryPath + "/" + cityName + numTasks; // Nome del file completo
        String outputFileName = "la_clean_new.txt";
    
        try {
            File inputFile = new File(inputFileName);
            if (!inputFile.exists()) {
                System.out.println("Il file " + inputFileName + " non esiste.");
                return;
            }
    
            BufferedReader reader = new BufferedReader(new FileReader(inputFile));
            PrintWriter writer = new PrintWriter(new FileWriter(outputFileName, false)); // False per sovrascrivere il file
    
            String line;
            while ((line = reader.readLine()) != null) {
                writer.println(line);
            }
    
            reader.close();
            writer.close();
    
            System.out.println("File " + outputFileName + " aggiornato con successo con i task da " + inputFileName);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("Errore durante la lettura o scrittura dei file.");
            e.printStackTrace();
        }
    }
/*    



    public static void createTxt() {
        try {
            Graph_Dijkstra graph = GraphPersistanSingleton.getInstance();
            String outputFileName = "la_clean_new.txt";
            File file = new File(outputFileName);
            PrintWriter writer = new PrintWriter(file);

            // Ottiene tutti i nodi dal grafo dalla mappa
            List<Node_Dijkstra> allNodes = new ArrayList<>(graph.getNodesMap().values());

            // Seleziona casualmente 500 nodi se il grafo è più grande di questo numero
            List<Node_Dijkstra> selectedNodes = new ArrayList<>();
            Random random = new Random();
            while (selectedNodes.size() < numTasks && !allNodes.isEmpty()) {
                int index = random.nextInt(allNodes.size());
                selectedNodes.add(allNodes.get(index));
                allNodes.remove(index);
            }

            // Mischia l'ordine dei nodi selezionati casualmente
            Collections.shuffle(selectedNodes);

            long baseTimestamp = 1333476000000L; // Timestamp di partenza
            long lastTimestamp = baseTimestamp;

            // Assegna i timestamp in ordine crescente ai nodi mischiati
            for (Node_Dijkstra node : selectedNodes) {
                double lat = node.lat;
                double lon = node.lon;
                long increment = (1000L * (random.nextInt(50) + 5)); // Incrementi di 5-55 secondi
                lastTimestamp += increment;

                // Scrivi la riga nel file con il timestamp incrementato
                writer.println(lat + "\t" + lon + "\t" + lastTimestamp);
            }

            writer.close();
            System.out.println("File " + outputFileName + " generato con successo.");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
*/
    private static void updateMaxMemoryUsage(long currentMemoryUsage) {
        if (currentMemoryUsage > maxRamUsed) {
            maxRamUsed = currentMemoryUsage;
        }
    }
    public long getMaxMemoryUsedForDownload(){
        return maxRamUsed;
    }
    public void clearMaxRam(){
		maxRamUsed = 0;
	}
}
