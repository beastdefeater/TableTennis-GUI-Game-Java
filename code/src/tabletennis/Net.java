package tabletennis;

import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

/** Mesh net with white tape and metal posts. */
public class Net implements Renderable {
    @Override
    public void draw(Graphics2D g, Camera cam) {
        double z = Court.NET_Z, nx = Court.HALF_W + 0.1, top = Court.NET_TOP, s = cam.scale(z);

        g.setColor(new Color(235, 245, 255, 45));
        g.fill(cam.poly(-nx, 0, z, nx, 0, z, nx, top, z, -nx, top, z));
        g.setStroke(new BasicStroke(1f));
        g.setColor(new Color(235, 242, 255, 140));
        for (double x = -nx; x <= nx + 1e-9; x += 0.035)
            g.draw(new Line2D.Double(cam.project(x, 0, z), cam.project(x, top, z)));
        for (double y = 0; y <= top + 1e-9; y += 0.03)
            g.draw(new Line2D.Double(cam.project(-nx, y, z), cam.project(nx, y, z)));

        g.setStroke(new BasicStroke((float) (s * 0.02), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(Color.WHITE);
        g.draw(new Line2D.Double(cam.project(-nx, top, z), cam.project(nx, top, z)));   // tape
        g.setStroke(new BasicStroke((float) (s * 0.024), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (int sgn = -1; sgn <= 1; sgn += 2) {                                         // posts
            Point2D.Double a = cam.project(sgn * nx, -0.01, z), b = cam.project(sgn * nx, top + 0.03, z);
            g.setColor(new Color(0xD7DCE3));
            g.draw(new Line2D.Double(a, b));
            g.setColor(new Color(0x39414D));
            g.fill(new java.awt.geom.Ellipse2D.Double(a.x - s * 0.03, a.y - s * 0.02, s * 0.06, s * 0.04));
        }
    }
}
