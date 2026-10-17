package tabletennis;

/** A renderable object that also changes over time (interface inheritance). */
public interface GameEntity extends Renderable {
    void update(double dt);
}
