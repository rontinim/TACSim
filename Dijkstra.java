import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Dijkstra {
    private Graph_Dijkstra graph;
    private Map<Long, Double> distances;
    private Map<Long, Long> previous;
    private Set<Long> visited;
    private PriorityQueue<Node_Dijkstra> queue;
    private static ConcurrentHashMap<String, CacheEntry> shortcutCache1; // cache 1
    private static ConcurrentHashMap<String, Double> distanceCache2; // cache 2
    private static LinkedHashSet<String> mappedEntries; // Set di entry mappate
    private static List<String> differingNodes = new ArrayList<>();

    private static double totalDistanceCache1 = 0.0;
    private static double totalDistanceCache2 = 0.0;
    private static int useCache = 1;

    // contatori per cache hit e miss
    private static int cache1Hits = 0;
    private static int cache1Misses = 0;
    private static int cache2Hits = 0;
    private static int cache2Misses = 0;

    public Dijkstra() {
        this.graph = GraphPersistanSingleton.getInstance();
    }

    private void initializeVariables() {
        distances = new ConcurrentHashMap<>();
        previous = new ConcurrentHashMap<>();
        visited = ConcurrentHashMap.newKeySet();
        queue = new PriorityQueue<>(Comparator.comparingDouble(n -> distances.getOrDefault(n.getId(), Double.MAX_VALUE)));
        for (CacheEntry entry : shortcutCache1.values()) {
            entry.resetCounter();
        }
        mappedEntries.clear();
    }

    public void initializePersistentCaches() {
        shortcutCache1 = new ConcurrentHashMap<>();
        distanceCache2 = new ConcurrentHashMap<>();
        mappedEntries = new LinkedHashSet<>();
    }

    public static void clearStorage() {
        shortcutCache1.clear();
        distanceCache2.clear();
        differingNodes.clear();
        cache1Hits = 0;
        cache1Misses = 0;
        cache2Hits = 0;
        cache2Misses = 0;
    }

    public void setuseCache(int cacheValue) {
        useCache = cacheValue;
    }

    public int getUseCache() {
        return useCache;
    }

    public double calculateDistance(GPSCoordinate source, GPSCoordinate destination) {
        initializeVariables();

        long sourceId = graph.findNodeIdByCoordinates(source.getLatitudine(), source.getLongitudine());
        long destinationId = graph.findNodeIdByCoordinates(destination.getLatitudine(), destination.getLongitudine());

        // verifica della cache diretta
        /*
        double directDistance = checkDirectPath(sourceId, destinationId);
        if (directDistance != -1) {
            if (useCache == 1) {
                cache1Hits++;
            } else if (useCache == 2) {
                cache2Hits++;
            }
            return directDistance;
        }
        */
        distances.put(sourceId, 0.0);
        queue.add(graph.getNode(sourceId));

        boolean hit = false;
        boolean shortcutFound = false;

        // ricerca percorso
        while (!queue.isEmpty() && !shortcutFound) {
            Node_Dijkstra currentNode = queue.poll();

            if (currentNode.getId() == destinationId) {
                double finalDistance = distances.getOrDefault(destinationId, Double.MAX_VALUE);
                if (useCache == 1) {
                    totalDistanceCache1 += finalDistance;
                    updateCacheAndResults(sourceId, destinationId, finalDistance);
                    if (!hit) {
                        cache1Misses++;
                    } else {
                        hit = true;
                    }
                } else if (useCache == 2) {
                    totalDistanceCache2 += finalDistance;
                    updateCacheAndResults(sourceId, destinationId, finalDistance);
                    if (!hit) {
                        cache2Misses++;
                    } else {
                        hit = true;
                    }
                }
                return finalDistance;
            }

            visited.add(currentNode.getId());

            for (Edges_Dijkstra edge : graph.adjacencyList.getOrDefault(currentNode.getId(), new ArrayList<>())) {
                Node_Dijkstra neighbor = edge.destination;
                if (visited.contains(neighbor.getId())) continue;

                double newDist = distances.get(currentNode.getId()) + edge.weight;

                if (useCache == 1) {
                    for (String mappedEntryKey : mappedEntries) {
                        CacheEntry mappedEntry = shortcutCache1.get(mappedEntryKey);
                        if (mappedEntry != null) {
                            mappedEntry.counter += edge.weight;

                            if (mappedEntry.counter > mappedEntry.precalculatedDistance) {
                                double totalDistance = mappedEntry.startToCurrentDistance + mappedEntry.precalculatedDistance;
                                hit = true;
                                cache1Hits++;
                                shortcutFound = true;
                                updateCacheAndResults(sourceId, destinationId, totalDistance);
                                return totalDistance;
                            }
                        }
                    }

                    String cacheKey = neighbor.getId() + "-" + destinationId;
                    CacheEntry entry = shortcutCache1.get(cacheKey);
                    if (entry != null) {
                        entry.startToCurrentDistance = distances.get(currentNode.getId()) + edge.weight;
                        mappedEntries.add(cacheKey);
                    }
                }

                if (useCache == 2) {
                    String cacheKey = neighbor.getId() + "-" + destinationId;
                    double cachedDist = distanceCache2.getOrDefault(cacheKey, -1.0);
                    if (cachedDist != -1.0) {
                        double totalDist = newDist + cachedDist;
                        if (totalDist < distances.getOrDefault(destinationId, Double.MAX_VALUE)) {
                            distances.put(destinationId, totalDist);
                            updateCacheAndResults(sourceId, destinationId, totalDist);
                            hit = true;
                            cache2Hits++;
                            return totalDist;
                        }
                    }
                }

                if (newDist < distances.getOrDefault(neighbor.getId(), Double.MAX_VALUE)) {
                    distances.put(neighbor.getId(), newDist);
                    previous.put(neighbor.getId(), currentNode.getId());
                    queue.add(neighbor);
                }
            }
        }

        double noPathDistance = distances.getOrDefault(destinationId, -1.0);
        if (noPathDistance == -1.0 && !shortcutCache1.isEmpty()) {
            double minAlternativePath = shortcutCache1.values().stream().map(entry -> entry.precalculatedDistance).min(Double::compare).orElse(Double.MAX_VALUE);
            updateCacheAndResults(sourceId, destinationId, minAlternativePath);
            return minAlternativePath;
        }

        if (!hit) {
            if (useCache == 1) {
                cache1Misses++;
            } else if (useCache == 2) {
                cache2Misses++;
            }
        }

        updateCacheAndResults(sourceId, destinationId, noPathDistance);
        return noPathDistance;
    }

    private double checkDirectPath(long sourceId, long destinationId) {
        String cacheKey = sourceId + "-" + destinationId;
        switch (useCache) {
            case 1:
                CacheEntry shortcutEntry = shortcutCache1.get(cacheKey);
                return shortcutEntry != null ? shortcutEntry.precalculatedDistance : -1;
            case 2:
                return distanceCache2.getOrDefault(cacheKey, -1.0);
            default:
                return -1;
        }
    }

    private void updateCacheAndResults(long sourceId, long destinationId, double distance) {
        String cacheKey = sourceId + "-" + destinationId;
        if (useCache == 1) {
            CacheEntry newEntry = new CacheEntry(distance);
            newEntry.startToCurrentDistance = distance;
            shortcutCache1.putIfAbsent(cacheKey, newEntry);
        } else if (useCache == 2) {
            distanceCache2.putIfAbsent(cacheKey, distance);
        }
    }

    public static void exportCacheStatisticsToCSV(String filename) {
        try (FileWriter writer = new FileWriter(filename, true)) {
            writer.append("Cache Type;Hits;Misses\n");
            writer.append("Cache1;").append(String.valueOf(cache1Hits)).append(";").append(String.valueOf(cache1Misses)).append("\n");
            writer.append("Cache2;").append(String.valueOf(cache2Hits)).append(";").append(String.valueOf(cache2Misses)).append("\n");
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void getSize(){
        System.out.println("cache1: " + shortcutCache1.size());
        System.out.println("cacheMappa: " + mappedEntries.size());
        System.out.println("cache2: " + distanceCache2.size());
    }
}