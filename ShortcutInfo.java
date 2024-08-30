class ShortcutInfo {
    double precalculatedDistance; // Distanza pre-calcolata da B a C
    double counter; // Contatore di distanza da A a B durante l'esplorazione

    public ShortcutInfo(double precalculatedDistance) {
        this.precalculatedDistance = precalculatedDistance;
        this.counter = 0;
    }
}
