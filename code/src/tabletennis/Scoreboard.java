package tabletennis;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/** Score + rally state and the glassy HUD (encapsulation). */
public class Scoreboard implements Renderable {
    public static final int WIN_SCORE = 7;
    private int playerScore, aiScore, rally, bestRally;
    private String leftLabel = "YOU", rightLabel = "CPU";
    private final double centerX, y;

    public Scoreboard(double centerX, double y) { this.centerX = centerX; this.y = y; }

    public int getPlayerScore() { return playerScore; }
    public int getAiScore() { return aiScore; }
    public int getBestRally() { return bestRally; }
    public int total() { return playerScore + aiScore; }
    public void setScores(int p, int a) { playerScore = p; aiScore = a; }
    public void setLabels(String l, String r) { leftLabel = l; rightLabel = r; }
    public void playerScores() { playerScore++; rally = 0; }
    public void aiScores() { aiScore++; rally = 0; }
    public void hit() { rally++; bestRally = Math.max(bestRally, rally); }
    public boolean hasWinner() { return playerScore >= WIN_SCORE || aiScore >= WIN_SCORE; }
    public boolean playerWon() { return playerScore > aiScore; }
    public void reset() { playerScore = aiScore = rally = bestRally = 0; }

    @Override
    public void draw(Graphics2D g, Camera cam) {
        pill(g, centerX - 175, Theme.PINK, leftLabel, playerScore);
        pill(g, centerX + 175, Theme.CYAN, rightLabel, aiScore);
        g.setFont(Theme.font(34f));
        drawCentered(g, ":", centerX, y + 40, Theme.TEXT);
        g.setFont(Theme.font(12f));
        drawCentered(g, "FIRST TO " + WIN_SCORE, centerX, y + 62, Theme.TEXT_DIM);
    }

    private void pill(Graphics2D g, double cx, Color c, String label, int score) {
        RoundRectangle2D r = new RoundRectangle2D.Double(cx - 105, y, 210, 56, 56, 56);
        g.setColor(new Color(21, 16, 46, 190)); g.fill(r);
        g.setColor(c); g.setStroke(new BasicStroke(3f)); g.draw(r);
        g.setFont(Theme.font(18f));  drawCentered(g, label, cx - 50, y + 35, c);
        g.setFont(Theme.font(38f));  drawCentered(g, String.valueOf(score), cx + 38, y + 43, Theme.TEXT);
    }

    public static void drawCentered(Graphics2D g, String s, double cx, double baseline, Color c) {
        g.setColor(c);
        g.drawString(s, (float) (cx - g.getFontMetrics().stringWidth(s) / 2.0), (float) baseline);
    }
    /** Text with a soft dark shadow so it stays readable on the busy 3D scene. */
    public static void shadowCentered(Graphics2D g, String s, double cx, double baseline, Color c) {
        drawCentered(g, s, cx + 1.5, baseline + 1.5, new Color(0, 0, 0, 150));
        drawCentered(g, s, cx, baseline, c);
    }
}
