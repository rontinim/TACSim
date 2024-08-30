class CacheEntry {
    public double counter;
    public final double precalculatedDistance;
    public double startToCurrentDistance;

    public CacheEntry(double precalculatedDistance) {
        this.counter = 0.0;
        this.precalculatedDistance = precalculatedDistance;
        this.startToCurrentDistance = Double.MAX_VALUE;
    }

    public void resetCounter() {
        this.counter = 0.0;
        this.startToCurrentDistance = Double.MAX_VALUE;
    }
}
