package tabletennis;

import java.awt.Color;
import java.util.Random;

/** Computer racquet: predicts the ball, moves at a limited speed, sometimes misjudges (polymorphism). */
public class AIRacquet extends Racquet {
    private final Ball ball;
    private final Random rng = new Random();
    private Difficulty difficulty;
    private double aimOffset;
    private boolean wasIncoming;

    public AIRacquet(int side, Color color, Ball ball, Difficulty difficulty) {
        super(side, color);
        this.ball = ball; this.difficulty = difficulty;
    }
    public void setDifficulty(Difficulty d) { difficulty = d; }

    private boolean incoming() { return side == 1 ? ball.getVz() < -0.01 : ball.getVz() > 0.01; }

    @Override
    protected double targetX() {
        boolean in = incoming();
        if (in && !wasIncoming) aimOffset = (rng.nextDouble() * 2 - 1) * difficulty.getAimError();
        wasIncoming = in;
        if (!in) return 0;
        double predicted = ball.getX() + ball.getVx() * (z - ball.getZ()) / ball.getVz();
        return predicted + aimOffset;
    }
    @Override protected double targetY() { return incoming() ? Theme.clamp(ball.getY(), 0.1, 0.5) : 0.25; }
    @Override protected double maxSpeed() { return difficulty.getAiSpeed(); }
    @Override public double getReach() { return difficulty.getReach(); }

    @Override
    public Shot chooseShot(Ball b, int rally) {
        double tx = (rng.nextDouble() * 2 - 1) * 0.55;
        if (rng.nextDouble() < difficulty.getMissChance()) tx = Math.signum(tx == 0 ? 1 : tx) * 0.95;   // blunder: sends it wide
        double tz = side == 1 ? Court.LENGTH - 0.35 - rng.nextDouble() * 0.35 : 0.3 + rng.nextDouble() * 0.35;
        double flight = Math.max(0.46, difficulty.getFlight() - Math.min(0.12, rally * 0.008));
        return new Shot(tx, tz, flight);
    }
}
