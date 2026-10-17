package tabletennis;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;

/** One confetti / spark dot in screen space. */
public class Particle implements GameEntity {
    private double x, y, vx, vy, life;
    private final double maxLife, size;
    private final Color color;

    public Particle(double x, double y, double vx, double vy, double life, double size, Color color) {
        this.x = x; this.y = y; this.vx = vx; this.vy = vy;
        this.life = this.maxLife = life; this.size = size; this.color = color;
    }
    public boolean isAlive() { return life > 0; }

    @Override public void update(double dt) { life -= dt; vy += 320 * dt; x += vx * dt; y += vy * dt; }

    @Override
    public void draw(Graphics2D g, Camera cam) {
        double t = Math.max(0, life / maxLife), s = size * (0.4 + 0.6 * t);
        g.setColor(Theme.alpha(color, (int) (255 * t)));
        g.fill(new Ellipse2D.Double(x - s / 2, y - s / 2, s, s));
    }
}
