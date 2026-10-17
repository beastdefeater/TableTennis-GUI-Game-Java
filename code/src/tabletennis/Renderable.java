package tabletennis;

import java.awt.Graphics2D;

/** Anything that can paint itself through the 3D camera (interface / abstraction). */
public interface Renderable {
    void draw(Graphics2D g, Camera cam);
}
