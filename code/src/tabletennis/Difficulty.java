package tabletennis;

/** Computer skill levels: each constant tunes the AI (enum with fields + methods). */
public enum Difficulty {
    //        label     speed m/s  aim error  miss%  reach  flight s
    EASY  ("Easy",      1.5,       0.30,      0.28,  0.20,  0.80),
    MEDIUM("Medium",    2.2,       0.16,      0.13,  0.26,  0.68),
    HARD  ("Hard",      3.2,       0.06,      0.04,  0.32,  0.57);

    private final String label;
    private final double aiSpeed, aimError, missChance, reach, flight;

    Difficulty(String label, double aiSpeed, double aimError, double missChance, double reach, double flight) {
        this.label = label; this.aiSpeed = aiSpeed; this.aimError = aimError;
        this.missChance = missChance; this.reach = reach; this.flight = flight;
    }
    public String getLabel() { return label; }
    public double getAiSpeed() { return aiSpeed; }
    public double getAimError() { return aimError; }
    public double getMissChance() { return missChance; }
    public double getReach() { return reach; }
    public double getFlight() { return flight; }
    public Difficulty next() { return values()[(ordinal() + 1) % values().length]; }
    public Difficulty previous() { return values()[(ordinal() + values().length - 1) % values().length]; }
}
