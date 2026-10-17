package tabletennis;

import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/** Background: slatted red wall and a warm wooden floor in perspective. */
public class Room implements Renderable {
    private static final double WALL_Z = 5.8;

    @Override
    public void draw(Graphics2D g, Camera cam) {
        int W = GamePanel.W, H = GamePanel.H;
        double wallBottom = cam.sy(Court.FLOOR_Y, WALL_Z);

        g.setPaint(new GradientPaint(0, 0, Theme.WALL_TOP, 0, (float) wallBottom, Theme.WALL_BOTTOM));
        g.fill(new Rectangle2D.Double(0, 0, W, wallBottom));
        for (int x = 0; x < W; x += 26) {                                         // wooden slats
            g.setColor(new Color(0, 0, 0, 80));  g.fillRect(x + 20, 0, 6, (int) wallBottom);
            g.setColor(new Color(255, 255, 255, 22)); g.fillRect(x, 0, 4, (int) wallBottom);
        }
        g.setPaint(new RadialGradientPaint(new Point2D.Double(W / 2.0, wallBottom * 0.75), 460f,
                new float[]{0f, 1f}, new Color[]{new Color(255, 205, 150, 120), new Color(255, 205, 150, 0)}));
        g.fill(new Rectangle2D.Double(0, 0, W, wallBottom));

        g.setPaint(new GradientPaint(0, (float) wallBottom, Theme.FLOOR_FAR, 0, H, Theme.FLOOR_NEAR));
        g.fill(new Rectangle2D.Double(0, wallBottom, W, H - wallBottom));
        g.setStroke(new BasicStroke(1.4f));
        g.setColor(new Color(0, 0, 0, 40));
        for (double x = -9; x <= 9; x += 0.6) {                                    // floor boards
            Point2D.Double a = cam.project(x, Court.FLOOR_Y, WALL_Z), b = cam.project(x, Court.FLOOR_Y, -1.0);
            g.draw(new Line2D.Double(a, b));
        }
        g.setColor(new Color(0, 0, 0, 22));
        for (double z = -1; z < WALL_Z; z += 0.9) {
            double y = cam.sy(Court.FLOOR_Y, z);
            g.draw(new Line2D.Double(0, y, W, y));
        }
        g.setColor(new Color(0, 0, 0, 100));
        g.fill(new Rectangle2D.Double(0, wallBottom - 5, W, 5));                   // skirting
        g.setPaint(new RadialGradientPaint(new Point2D.Double(W / 2.0, wallBottom + 60), 380f,
                new float[]{0f, 1f}, new Color[]{new Color(255, 220, 170, 80), new Color(255, 220, 170, 0)}));
        g.fill(new Rectangle2D.Double(0, wallBottom, W, H - wallBottom));          // floor sheen
    }
}
