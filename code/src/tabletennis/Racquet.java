package tabletennis;

import java.awt.*;
import java.awt.geom.*;

/**
 * Abstract racquet. Template method: update() is fixed; subclasses decide WHERE the racquet
 * wants to be (targetX/targetY) and HOW to return the ball (chooseShot) -> polymorphism.
 */
public abstract class Racquet extends GameObject3D {
    public static final double HEAD_R = 0.17;
    public record Shot(double tx, double tz, double flight) { }

    protected final int side;               // +1 = near end (player view), -1 = far end
    private final Color color;
    private double vx, vy, flash;
    private boolean grabbed;
    protected double step;

    protected Racquet(int side, Color color) {
        super(0, 0.3, side == 1 ? -0.3 : Court.LENGTH + 0.3);
        this.side = side; this.color = color;
    }

    protected abstract double targetX();
    protected abstract double targetY();
    protected abstract double maxSpeed();
    public abstract double getReach();
    public abstract Shot chooseShot(Ball ball, int rally);

    public int getSide() { return side; }
    public Color getColor() { return color; }
    public double getVx() { return vx; }
    public double getVy() { return vy; }
    public void flash() { flash = 1; }
    public void setGrabbed(boolean g) { grabbed = g; }
    public boolean isGrabbed() { return grabbed; }
    public void resetPose() { x = 0; y = 0.3; vx = vy = 0; }

    @Override
    public void update(double dt) {
        step = dt;
        if (dt < 1e-6) return;
        double tx = Theme.clamp(targetX(), -1.15, 1.15), ty = Theme.clamp(targetY(), 0.06, 0.85);
        double k = Math.min(1, dt * 14);
        double dx = (tx - x) * k, dy = (ty - y) * k, cap = maxSpeed() * dt;
        if (Math.abs(dx) > cap) dx = Math.signum(dx) * cap;
        if (Math.abs(dy) > cap * 1.5) dy = Math.signum(dy) * cap * 1.5;
        x += dx; y += dy; vx = dx / dt; vy = dy / dt;
        flash = Math.max(0, flash - dt * 5);
    }

    @Override
    public void draw(Graphics2D g, Camera cam) {
        if (cam.depth(z) < 0.5) return;
        Point2D.Double p = cam.project(x, y, z);
        double s = cam.scale(z);
        double r = HEAD_R * s * (1 + 0.10 * flash + (grabbed ? 0.04 : 0));
        double tilt = -0.42 + Theme.clamp(vx * 0.09, -0.3, 0.3);

        g.setColor(new Color(0, 0, 0, 45));                                            // floor shadow
        g.fill(new Ellipse2D.Double(p.x - r * 0.9, p.y + r * 2.3, r * 1.8, r * 0.45));

        AffineTransform old = g.getTransform();
        g.translate(p.x, p.y);
        g.rotate(tilt);

        double hw = r * 0.27;                                                          // handle
        g.setPaint(new GradientPaint((float) -hw, 0, Theme.WOOD_LIGHT, (float) hw, 0, Theme.WOOD_DARK));
        g.fill(new RoundRectangle2D.Double(-hw, r * 0.7, hw * 2, r * 1.55, hw, hw));
        g.setColor(new Color(0, 0, 0, 55));
        g.fill(new RoundRectangle2D.Double(-hw, r * 1.55, hw * 2, r * 0.25, 4, 4));

        g.setPaint(new GradientPaint((float) -r, (float) -r, new Color(0xE8B87A), (float) r, (float) r, new Color(0xA8672E)));
        g.fill(new Ellipse2D.Double(-r, -r, r * 2, r * 2));                            // wooden rim
        double rr = r * 0.9;
        g.setPaint(new RadialGradientPaint((float) (-rr * 0.35), (float) (-rr * 0.4), (float) (rr * 1.5),
                new float[]{0f, 1f}, new Color[]{color.brighter(), color.darker().darker()}));
        g.fill(new Ellipse2D.Double(-rr, -rr, rr * 2, rr * 2));                        // rubber
        g.setColor(new Color(255, 255, 255, 55));
        g.fill(new Ellipse2D.Double(-rr * 0.75, -rr * 0.85, rr * 0.9, rr * 0.45));     // gloss
        g.setColor(new Color(0, 0, 0, 22));
        g.setStroke(new BasicStroke(1f));
        g.draw(new Ellipse2D.Double(-rr * 0.8, -rr * 0.8, rr * 1.6, rr * 1.6));
        if (grabbed) {
            g.setColor(new Color(255, 255, 255, 230));
            g.setStroke(new BasicStroke(3f));
            g.draw(new Ellipse2D.Double(-r - 5, -r - 5, r * 2 + 10, r * 2 + 10));
        }
        g.setTransform(old);
    }
}
