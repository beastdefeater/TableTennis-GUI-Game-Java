package tabletennis;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/** Composition: owns many Particle objects (generic List). */
public class ParticleSystem implements GameEntity {
    private final List<Particle> particles = new ArrayList<>();
    private final Random rng = new Random();
    private static final Color[] POP = {Theme.PINK, Theme.CYAN, Theme.YELLOW, Theme.ORANGE, Color.WHITE};

    public void burst(double x, double y, Color base, int count) {
        for (int i = 0; i < count; i++) {
            double a = rng.nextDouble() * Math.PI * 2, sp = 60 + rng.nextDouble() * 260;
            Color c = rng.nextInt(3) == 0 ? POP[rng.nextInt(POP.length)] : base;
            particles.add(new Particle(x, y, Math.cos(a) * sp, Math.sin(a) * sp - 80, 0.45 + rng.nextDouble() * 0.5, 4 + rng.nextDouble() * 6, c));
        }
    }
    public void confetti(double width, int count) {
        for (int i = 0; i < count; i++)
            particles.add(new Particle(rng.nextDouble() * width, -10, (rng.nextDouble() - 0.5) * 120, rng.nextDouble() * 120,
                    2.5 + rng.nextDouble(), 6 + rng.nextDouble() * 6, POP[rng.nextInt(POP.length)]));
    }
    public void clear() { particles.clear(); }

    @Override
    public void update(double dt) {
        for (Iterator<Particle> it = particles.iterator(); it.hasNext(); ) {
            Particle p = it.next(); p.update(dt); if (!p.isAlive()) it.remove();
        }
    }
    @Override public void draw(Graphics2D g, Camera cam) { for (Particle p : particles) p.draw(g, cam); }
}
