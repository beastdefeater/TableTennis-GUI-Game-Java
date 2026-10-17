package tabletennis;

import java.awt.geom.Path2D;
import java.awt.geom.Point2D;

/** Perspective camera: turns 3D world points into screen pixels (and back). Encapsulates the maths. */
public class Camera {
    private final double f, cx, horizonY, eyeY, eyeZ;

    public Camera(double focal, double cx, double horizonY, double eyeY, double eyeZ) {
        this.f = focal; this.cx = cx; this.horizonY = horizonY; this.eyeY = eyeY; this.eyeZ = eyeZ;
    }
    public double getEyeZ() { return eyeZ; }
    public double depth(double z) { return z - eyeZ; }
    public double scale(double z) { return f / (z - eyeZ); }                 // pixels per metre at depth z
    public double sx(double x, double z) { return cx + x * scale(z); }
    public double sy(double y, double z) { return horizonY + (eyeY - y) * scale(z); }
    public Point2D.Double project(double x, double y, double z) { return new Point2D.Double(sx(x, z), sy(y, z)); }
    public double worldX(double screenX, double z) { return (screenX - cx) / scale(z); }
    public double worldY(double screenY, double z) { return eyeY - (screenY - horizonY) / scale(z); }

    /** Flat rectangle lying at height y between x0..x1 and z0..z1. */
    public Path2D.Double quad(double x0, double x1, double z0, double z1, double y) {
        return poly(x0, y, z0, x1, y, z0, x1, y, z1, x0, y, z1);
    }
    /** Polygon from consecutive (x,y,z) triplets. */
    public Path2D.Double poly(double... xyz) {
        Path2D.Double p = new Path2D.Double();
        for (int i = 0; i < xyz.length; i += 3) {
            Point2D.Double s = project(xyz[i], xyz[i + 1], xyz[i + 2]);
            if (i == 0) p.moveTo(s.x, s.y); else p.lineTo(s.x, s.y);
        }
        p.closePath();
        return p;
    }
}
