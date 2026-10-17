package tabletennis;

/** Abstract base: every object in the scene has a position in 3D space (encapsulated state). */
public abstract class GameObject3D implements GameEntity {
    protected double x, y, z;
    protected GameObject3D(double x, double y, double z) { this.x = x; this.y = y; this.z = z; }
    public double getX() { return x; }
    public double getY() { return y; }
    public double getZ() { return z; }
    public void setPosition(double x, double y, double z) { this.x = x; this.y = y; this.z = z; }
}
