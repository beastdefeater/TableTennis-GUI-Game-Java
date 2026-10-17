package tabletennis;

/** Static helper: finds the launch velocity that makes the ball land on (tx, tz) and clear the net. */
public final class ShotCalculator {
    private ShotCalculator() { }

    public static void aim(Ball b, double tx, double tz, double flight) {
        double x0 = b.getX(), y0 = b.getY(), z0 = b.getZ(), g = Ball.GRAVITY;
        double t = flight;
        for (; t <= 1.7; t += 0.04) {
            double vz = (tz - z0) / t, vy = (-y0 + 0.5 * g * t * t) / t;
            double tn = (Court.NET_Z - z0) / vz;
            boolean crosses = tn > 0 && tn < t;
            if (!crosses || y0 + vy * tn - 0.5 * g * tn * tn >= Court.NET_TOP + 0.07) break;
        }
        b.setVelocity((tx - x0) / t, (-y0 + 0.5 * g * t * t) / t, (tz - z0) / t);
    }
}
