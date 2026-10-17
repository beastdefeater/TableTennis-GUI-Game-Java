package tabletennis;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.util.ArrayDeque;
import java.util.Deque;

/** The ball: simple physics (gravity + bounce), a glowing trail and a soft shadow on the table. */
public class Ball extends GameObject3D {
    public static final double GRAVITY = 9.8, RADIUS = 0.042;
    private double vx, vy, vz;
    private final Deque<double[]> trail = new ArrayDeque<>();

    public Ball() { super(0, 0.3, 0); }
    public double getVx() { return vx; }
    public double getVy() { return vy; }
    public double getVz() { return vz; }
    public void setVelocity(double vx, double vy, double vz) { this.vx = vx; this.vy = vy; this.vz = vz; }

    public void hover(double x, double y, double z) { setPosition(x, y, z); vx = vy = vz = 0; trail.clear(); }

    public void bounceOnTable() { y = 0; vy = -vy * 0.87; vx *= 0.97; vz *= 0.97; }

    @Override
    public void update(double dt) {
        trail.addFirst(new double[]{x, y, z});
        while (trail.size() > 9) trail.removeLast();
        vy -= GRAVITY * dt;
        x += vx * dt; y += vy * dt; z += vz * dt;
    }

    public void drawShadow(Graphics2D g, Camera cam) {
        if (!Court.onTable(x, z) || y < 0) return;
        Point2D.Double p = cam.project(x, 0, z);
        double r = cam.scale(z) * RADIUS * (1 + y * 0.8);
        g.setColor(new Color(0, 20, 60, (int) Math.max(0, 110 - y * 140)));
        g.fill(new Ellipse2D.Double(p.x - r, p.y - r * 0.4, r * 2, r * 0.8));
    }

    @Override
    public void draw(Graphics2D g, Camera cam) {
        if (cam.depth(z) < 0.4) return;
        int i = 0;
        for (double[] t : trail) {
            float k = 1f - i / 9f;
            Point2D.Double p = cam.project(t[0], t[1], t[2]);
            double r = cam.scale(t[2]) * RADIUS * (0.35 + 0.65 * k);
            g.setColor(Theme.alpha(new Color(0xBDEBFF), (int) (70 * k)));
            g.fill(new Ellipse2D.Double(p.x - r, p.y - r, r * 2, r * 2));
            i++;
        }
        Point2D.Double p = cam.project(x, y, z);
        double r = cam.scale(z) * RADIUS;
        g.setColor(new Color(255, 255, 255, 45));
        g.fill(new Ellipse2D.Double(p.x - r * 1.9, p.y - r * 1.9, r * 3.8, r * 3.8));          // glow
        g.setPaint(new RadialGradientPaint((float) (p.x - r * 0.35), (float) (p.y - r * 0.4), (float) (r * 1.6),
                new float[]{0f, 1f}, new Color[]{Color.WHITE, new Color(0xE3E3EC)}));
        g.fill(new Ellipse2D.Double(p.x - r, p.y - r, r * 2, r * 2));
    }
}
