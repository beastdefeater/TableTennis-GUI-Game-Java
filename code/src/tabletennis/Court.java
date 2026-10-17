package tabletennis;

/** Real table-tennis dimensions in metres (constants only). x: left/right, y: up, z: along the table. */
public final class Court {
    public static final double WIDTH = 1.525, HALF_W = WIDTH / 2, LENGTH = 2.74;
    public static final double NET_Z = LENGTH / 2, NET_TOP = 0.1525, FLOOR_Y = -0.76;
    private Court() { }
    public static boolean onTable(double x, double z) { return Math.abs(x) <= HALF_W && z >= 0 && z <= LENGTH; }
}
