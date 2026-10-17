package tabletennis;

/** Who controls the far racquet (enum with fields). */
public enum GameMode {
    VS_CPU("VS COMPUTER", "YOU", "CPU"),
    TWO_PLAYER("2 PLAYERS", "P1", "P2");

    private final String label, leftName, rightName;
    GameMode(String label, String leftName, String rightName) {
        this.label = label; this.leftName = leftName; this.rightName = rightName;
    }
    public String getLabel() { return label; }
    public String getLeftName() { return leftName; }
    public String getRightName() { return rightName; }
    public GameMode toggle() { return this == VS_CPU ? TWO_PLAYER : VS_CPU; }
}
