package tabletennis;

import java.awt.Color;

/** Human racquet: follows the mouse (world target) or the keyboard. */
public class PlayerRacquet extends Racquet {
    private boolean left, right, up, down;
    private Double mouseX, mouseY;
    private double tgtX = 0, tgtY = 0.3;

    public PlayerRacquet(int side, Color color) { super(side, color); }

    public void setMouseTarget(Double wx, Double wy) { mouseX = wx; mouseY = wy; }
    public void setLeft(boolean v)  { left = v;  if (v) release(); }
    public void setRight(boolean v) { right = v; if (v) release(); }
    public void setUp(boolean v)    { up = v;    if (v) release(); }
    public void setDown(boolean v)  { down = v;  if (v) release(); }
    private void release() { if (mouseX != null) { tgtX = mouseX; tgtY = mouseY; } mouseX = null; mouseY = null; }

    @Override public void resetPose() { super.resetPose(); tgtX = 0; tgtY = 0.3; }

    @Override
    protected double targetX() {
        if (mouseX != null) return tgtX = mouseX;
        tgtX += ((right ? 1 : 0) - (left ? 1 : 0)) * 1.9 * step;
        return tgtX = Theme.clamp(tgtX, -1.15, 1.15);
    }
    @Override
    protected double targetY() {
        if (mouseY != null) return tgtY = mouseY;
        tgtY += ((up ? 1 : 0) - (down ? 1 : 0)) * 1.4 * step;
        return tgtY = Theme.clamp(tgtY, 0.06, 0.85);
    }
    @Override protected double maxSpeed() { return 14; }
    @Override public double getReach() { return 0.34; }

    @Override
    public Shot chooseShot(Ball ball, int rally) {
        double tx = Theme.clamp((ball.getX() - x) * 2.2 + getVx() * 0.28, -0.62, 0.62);
        double tz = side == 1 ? Court.LENGTH - 0.42 : 0.42;
        double flight = Math.max(0.5, 0.66 - 0.03 * Math.min(3, Math.abs(getVx())) - Math.min(0.12, rally * 0.008));
        return new Shot(tx, tz, flight);
    }
}
