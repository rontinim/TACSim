public class MemoryUsageTracker {
    private static MemoryUsageTracker instance;
    private double totalMemoryUsed = 0;
    private int count = 0;

    private MemoryUsageTracker() {}

    public static synchronized MemoryUsageTracker getInstance() {
        if (instance == null) {
            instance = new MemoryUsageTracker();
        }
        return instance;
    }

    public void addMemoryUsage(long memoryUsed) {
        if (memoryUsed > 0) {
            totalMemoryUsed += memoryUsed;
            count++;
        }
    }

    public double getAverageMemoryUsed() {
        if (count == 0) return 0;
        double average = totalMemoryUsed / count;
        reset();
        return average;
    }

    private void reset() {
        totalMemoryUsed = 0;
        count = 0;
    }
}
