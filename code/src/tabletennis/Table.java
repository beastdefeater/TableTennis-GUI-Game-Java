package tabletennis;

import java.awt.*;
import java.awt.geom.Path2D;

/** The glossy blue table with white lines, apron and legs. */
public class Table implements Renderable {
    @Override
    public void draw(Graphics2D g, Camera cam) {
        double hw = Court.HALF_W, L = Court.LENGTH, fy = Court.FLOOR_Y;

        for (int i = 3; i >= 0; i--) {                                              // soft floor shadow
            g.setColor(new Color(0, 0, 0, 28));
            g.fill(cam.quad(-hw * 1.05 - i * 0.08, hw * 1.05 + i * 0.08, -0.15 - i * 0.08, L + 0.1 + i * 0.08, fy));
        }
        double[] lx = {-hw + 0.16, hw - 0.16}, lz = {0.28, L - 0.28};
        for (double zz : lz) for (double xx : lx) {                                 // legs
            g.setPaint(new GradientPaint((float) cam.sx(xx - 0.04, zz), 0, new Color(0x2B3A4D), (float) cam.sx(xx + 0.04, zz), 0, new Color(0x121B26)));
            g.fill(cam.poly(xx - 0.04, -0.05, zz, xx + 0.04, -0.05, zz, xx + 0.04, fy, zz, xx - 0.04, fy, zz));
        }
        g.setColor(Theme.TABLE_EDGE);                                               // near apron
        g.fill(cam.poly(-hw, 0, 0, hw, 0, 0, hw, -0.07, 0, -hw, -0.07, 0));
        g.setColor(new Color(0, 0, 0, 70));
        g.fill(cam.poly(-hw, -0.045, 0, hw, -0.045, 0, hw, -0.07, 0, -hw, -0.07, 0));

        Path2D.Double top = cam.quad(-hw, hw, 0, L, 0);
        g.setPaint(new GradientPaint(0, (float) cam.sy(0, L), Theme.TABLE_FAR, 0, (float) cam.sy(0, 0), Theme.TABLE_NEAR));
        g.fill(top);

        Shape clip = g.getClip();                                                   // glossy reflection
        g.clip(top);
        g.setPaint(new GradientPaint((float) cam.sx(-hw, 0), 0, new Color(255, 255, 255, 70),
                (float) cam.sx(-hw * 0.1, 0), 0, new Color(255, 255, 255, 0)));
        g.fill(cam.poly(-hw, 0, 0.1, -hw * 0.45, 0, 0.1, -hw * 0.05, 0, L, -hw, 0, L));
        g.setClip(clip);

        g.setColor(new Color(255, 255, 255, 240));                                  // markings
        g.fill(cam.quad(-hw, -hw + 0.02, 0, L, 0));
        g.fill(cam.quad(hw - 0.02, hw, 0, L, 0));
        g.fill(cam.quad(-hw, hw, 0, 0.02, 0));
        g.fill(cam.quad(-hw, hw, L - 0.02, L, 0));
        g.setColor(new Color(255, 255, 255, 215));
        g.fill(cam.quad(-0.006, 0.006, 0, L, 0));
    }
}
