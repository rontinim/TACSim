import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DataStore {
    private static Map<Integer, List<List<DataPoint>>> dataPointsList1 = new HashMap<>();
    private static Map<Integer, List<List<DataPoint>>> dataPointsList2 = new HashMap<>();
    private static Map<Integer, List<List<DataPoint>>> dataPointsList4 = new HashMap<>();

    private static int currentRepetition = 1;

    public static void setCurrentRepetition(int repetition) {
        currentRepetition = repetition;
    }

    public static void addDataIfAbsent(DataPoint newDataPoint, int countExperiment, int listSelector) {
        if (newDataPoint.getDistance() == 0 || newDataPoint.getDistance() == -1) {
            return;
        }
        
        String newKey = createKey(newDataPoint);
        Map<Integer, List<List<DataPoint>>> targetMap = getTargetMap(listSelector);

        // Inizializza la mappa per countExperiment se necessario
        targetMap.putIfAbsent(countExperiment, new ArrayList<>());

        List<List<DataPoint>> targetLists = targetMap.get(countExperiment);

        // Aggiungi una nuova lista se necessario
        while (currentRepetition > targetLists.size()) {
            targetLists.add(new ArrayList<>());
        }

        List<DataPoint> targetList = targetLists.get(currentRepetition - 1);

        for (DataPoint dp : targetList) {
            String existingKey = createKey(dp);
            if (newKey.equals(existingKey)) {
                return;
            }
        }
        targetList.add(newDataPoint);
    }

    private static String createKey(DataPoint dp) {
        return dp.getStartLatitude() + "-" + dp.getStartLongitude() + "-" +
               dp.getEndLatitude() + "-" + dp.getEndLongitude();
    }

    private static Map<Integer, List<List<DataPoint>>> getTargetMap(int listSelector) {
        switch (listSelector) {
            case 1: return dataPointsList1;
            case 2: return dataPointsList2;
            case 4: return dataPointsList4;
            default: throw new IllegalArgumentException("Invalid list selector: " + listSelector);
        }
    }

    public static void exportDataPointsToCSVAndClear(String filenamePrefix) {
        exportAllListsToCSVAndClear(dataPointsList1, filenamePrefix + "-cache1");
        exportAllListsToCSVAndClear(dataPointsList2, filenamePrefix + "-cache2");
        exportAllListsToCSVAndClear(dataPointsList4, filenamePrefix + "-cache4");
    }

    private static void exportAllListsToCSVAndClear(Map<Integer, List<List<DataPoint>>> dataPointsMap, String filenamePrefix) {
        for (Map.Entry<Integer, List<List<DataPoint>>> entry : dataPointsMap.entrySet()) {
            int countExperiment = entry.getKey();
            List<List<DataPoint>> dataPointsLists = entry.getValue();
            int fileCounter = 1;
            for (List<DataPoint> dataPoints : dataPointsLists) {
                if (!dataPoints.isEmpty()) {
                    exportListToCSVAndClear(dataPoints, filenamePrefix + "-" + countExperiment + ".csv");
                    fileCounter++;
                }
            }
        }
    }

    private static void exportListToCSVAndClear(List<DataPoint> dataPoints, String filename) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            writer.println("startLatitude;startLongitude;endLatitude;endLongitude;processTime(ms);processRam(byte);distance(km)");
            for (DataPoint dp : dataPoints) {
                writer.println(dp.getStartLatitude() + ";" + dp.getStartLongitude() + ";" +
                               dp.getEndLatitude() + ";" + dp.getEndLongitude() + ";" +
                               dp.getProcessTime() + ";" + dp.getProcessRam() + ";" +
                               dp.getDistance());
            }
        } catch (IOException e) {
            System.out.println("Errore durante la scrittura del file CSV: " + e.getMessage());
        } finally {
            dataPoints.clear();
        }
    }
}
