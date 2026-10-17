package tabletennis;

import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.Random;

/** Controller + view: owns every game object (composition) and runs the rules as a state machine. */
public class GamePanel extends JPanel {
    public static final int W = 1000, H = 660;

    private final Camera cam = new Camera(1004, W / 2.0, -70, 1.25, -1.9);
    private final Room room = new Room();
    private final Table table = new Table();
    private final Net net = new Net();
    private final Ball ball = new Ball();
    private final PlayerRacquet humanNear = new PlayerRacquet(1, Theme.RACQUET_RED);
    private final PlayerRacquet humanFar = new PlayerRacquet(-1, Theme.RACQUET_BLUE);
    private final AIRacquet attractNear = new AIRacquet(1, Theme.RACQUET_RED, ball, Difficulty.MEDIUM);
    private final AIRacquet cpuFar = new AIRacquet(-1, Theme.RACQUET_BLUE, ball, Difficulty.MEDIUM);
    private Racquet nearR, farR, lastHitter, server;           // polymorphic references
    private final Scoreboard scoreboard = new Scoreboard(W / 2.0, 16);
    private final ParticleSystem particles = new ParticleSystem();
    private final Random rng = new Random();

    private GameState state = GameState.MENU;
    private Difficulty difficulty = Difficulty.MEDIUM;
    private GameMode mode = GameMode.VS_CPU;
    private double stateTime, shake, bgPhase, toastTime;
    private String toast = "", toastSub = "";
    private int bounces, rally;
    private boolean pointOver;
    private Cursor blankCursor;

    public GamePanel() { this(true); }

    public GamePanel(boolean autoRun) {
        setPreferredSize(new Dimension(W, H));
        setFocusable(true);
        addKeyListener(new KeyHandler());
        MouseHandler mh = new MouseHandler();
        addMouseListener(mh);
        addMouseMotionListener(mh);
        if (!GraphicsEnvironment.isHeadless())
            blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB), new Point(0, 0), "blank");
        enterMenu();
        if (autoRun) {
            long[] last = {System.nanoTime()};
            new Timer(8, e -> {
                long now = System.nanoTime();
                step(Math.min(0.033, (now - last[0]) / 1e9));
                last[0] = now;
                repaint();
            }).start();
        }
    }

    /* ---------------------------------------------------------------- public API (used by tools) */
    public GameState getState() { return state; }
    public void setMode(GameMode m) { mode = m; }
    public void showMenu() { enterMenu(); }
    public double getBallX() { return ball.getX(); }
    public double getBallY() { return ball.getY(); }
    public double getBallZ() { return ball.getZ(); }
    public int getRally() { return rally; }
    public String getScoreText() { return scoreboard.getPlayerScore() + "-" + scoreboard.getAiScore(); }

    public void setDemo(boolean on) {
        Racquet oldNear = nearR, oldFar = farR;
        nearR = on ? attractNear : humanNear;
        if (on) farR = cpuFar;
        if (server == oldNear) server = nearR;
        if (server == oldFar) server = farR;
    }
    public void mouseMove(int sx, int sy) {
        if (nearR == humanNear && (state == GameState.SERVE || state == GameState.PLAYING))
            humanNear.setMouseTarget(cam.worldX(sx, humanNear.getZ()), cam.worldY(sy, humanNear.getZ()));
    }
    public void mousePress(int sx, int sy) {
        mouseMove(sx, sy);
        if (nearR == humanNear && (state == GameState.SERVE || state == GameState.PLAYING)) {
            humanNear.setGrabbed(true);
            requestServe(humanNear);
        }
    }
    public void mouseRelease() { humanNear.setGrabbed(false); }

    public void forceFinish(boolean playerWins) {
        scoreboard.setScores(playerWins ? Scoreboard.WIN_SCORE : 4, playerWins ? 4 : Scoreboard.WIN_SCORE);
        finishMatch();
    }

    /* ---------------------------------------------------------------- state changes */
    private void enterMenu() {
        state = GameState.MENU;
        scoreboard.reset(); particles.clear();
        attractNear.setDifficulty(Difficulty.MEDIUM); cpuFar.setDifficulty(Difficulty.MEDIUM);
        nearR = attractNear; farR = cpuFar;
        humanNear.setGrabbed(false);
        startAttract();
    }

    private void startAttract() {
        server = rng.nextBoolean() ? nearR : farR;
        lastHitter = null; rally = 0; bounces = 0;
        hoverBall();
        serve();
    }

    public void startMatch(Difficulty d) {
        difficulty = d;
        cpuFar.setDifficulty(d);
        nearR = humanNear;
        farR = mode == GameMode.VS_CPU ? cpuFar : humanFar;
        scoreboard.reset();
        scoreboard.setLabels(mode.getLeftName(), mode.getRightName());
        particles.clear();
        humanNear.resetPose(); humanFar.resetPose(); cpuFar.resetPose(); attractNear.resetPose();
        beginServe();
    }

    private void beginServe() {
        state = GameState.SERVE;
        stateTime = 0; rally = 0; bounces = 0; pointOver = false; lastHitter = null;
        server = ((scoreboard.total() / 2) % 2 == 0) ? nearR : farR;
        hoverBall();
    }

    private void hoverBall() {
        ball.hover(server.getX(), server.getY() + 0.13, server.getZ() + (server.getSide() == 1 ? 0.12 : -0.12));
    }

    private void requestServe(Racquet who) {
        if (state == GameState.SERVE && server == who && who instanceof PlayerRacquet) serve();
    }

    private void serve() {
        pointOver = false; bounces = 0; rally = 0;
        performHit(server);
        if (state != GameState.MENU) state = GameState.PLAYING;
    }

    public void togglePause() {
        if (state == GameState.PLAYING) state = GameState.PAUSED;
        else if (state == GameState.PAUSED) state = GameState.PLAYING;
    }

    private void finishMatch() {
        state = GameState.GAME_OVER; stateTime = 0;
        if (scoreboard.playerWon()) particles.confetti(W, 140);
    }

    /* ---------------------------------------------------------------- simulation */
    public void step(double dt) {
        bgPhase += dt; stateTime += dt;
        shake = Math.max(0, shake - dt * 30);
        toastTime = Math.max(0, toastTime - dt);
        particles.update(dt);
        switch (state) {
            case MENU, PLAYING -> { nearR.update(dt); farR.update(dt); updateRally(dt); }
            case SERVE -> {
                nearR.update(dt); farR.update(dt); hoverBall();
                if (server instanceof AIRacquet && stateTime >= 1.2) serve();
            }
            case GAME_OVER -> { if (scoreboard.playerWon() && rng.nextInt(6) == 0) particles.confetti(W, 6); }
            case PAUSED -> { }
        }
    }

    private void updateRally(double dt) {
        double prevZ = ball.getZ();
        ball.update(dt);

        if (ball.getY() <= 0 && ball.getVy() < 0 && Court.onTable(ball.getX(), ball.getZ())) {
            ball.bounceOnTable();
            int landed = ball.getZ() < Court.NET_Z ? 1 : -1;
            if (landed == lastHitter.getSide()) { if (bounces == 0) { fault(lastHitter, "OUT"); return; } }
            else if (++bounces >= 2) { award(lastHitter, "MISSED"); return; }
        }
        if ((prevZ - Court.NET_Z) * (ball.getZ() - Court.NET_Z) < 0 && ball.getY() < Court.NET_TOP + 0.02
                && Math.abs(ball.getX()) < Court.HALF_W + 0.15) { fault(lastHitter, "NET"); return; }

        checkHit(nearR);
        checkHit(farR);

        Racquet receiver = lastHitter == nearR ? farR : nearR;
        boolean out = ball.getY() < -0.6 || ball.getZ() < -1.7 || ball.getZ() > Court.LENGTH + 1.7 || Math.abs(ball.getX()) > 3.2;
        boolean passed = bounces >= 1 && (receiver.getSide() == 1 ? ball.getZ() < receiver.getZ() - 0.5 : ball.getZ() > receiver.getZ() + 0.5);
        if (out || passed) { if (bounces == 0) fault(lastHitter, "OUT"); else award(lastHitter, "MISSED"); }
    }

    private void checkHit(Racquet r) {
        if (r == lastHitter || bounces < 1) return;
        boolean incoming = r.getSide() == 1 ? ball.getVz() < 0 : ball.getVz() > 0;
        boolean inZone = r.getSide() == 1 ? ball.getZ() < r.getZ() + 0.7 : ball.getZ() > r.getZ() - 0.7;
        if (!incoming || !inZone) return;
        double d = Math.hypot(ball.getX() - r.getX(), (ball.getY() - r.getY()) * 0.7);
        if (d <= r.getReach()) performHit(r);
    }

    private void performHit(Racquet r) {
        Racquet.Shot s = r.chooseShot(ball, rally);
        ShotCalculator.aim(ball, s.tx(), s.tz(), s.flight());
        lastHitter = r; bounces = 0; rally++;
        scoreboard.hit();
        r.flash();
        Point2D.Double p = cam.project(ball.getX(), ball.getY(), ball.getZ());
        particles.burst(p.x, p.y, r.getColor(), 14);
        shake = 3;
    }

    private void fault(Racquet loser, String why) { award(loser == nearR ? farR : nearR, why); }

    private void award(Racquet winner, String why) {
        if (pointOver) return;
        pointOver = true;
        Point2D.Double p = cam.project(ball.getX(), Math.max(0, ball.getY()), ball.getZ());
        particles.burst(p.x, p.y, winner.getColor(), 30);
        if (state == GameState.MENU) { startAttract(); return; }
        boolean nearWins = winner.getSide() == 1;
        if (nearWins) scoreboard.playerScores(); else scoreboard.aiScores();
        toast = (nearWins ? mode.getLeftName() : mode.getRightName()) + " SCORES!";
        toastSub = why; toastTime = 1.6; shake = 7;
        if (scoreboard.hasWinner()) finishMatch(); else beginServe();
    }

    /* ---------------------------------------------------------------- rendering */
    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        updateCursor();

        if (shake > 0.1) g.translate((rng.nextDouble() - 0.5) * shake, (rng.nextDouble() - 0.5) * shake);
        room.draw(g, cam);
        table.draw(g, cam);
        ball.drawShadow(g, cam);
        farR.draw(g, cam);
        boolean ballBeyondNet = ball.getZ() > Court.NET_Z;
        if (ballBeyondNet) ball.draw(g, cam);
        net.draw(g, cam);
        if (!ballBeyondNet) ball.draw(g, cam);
        nearR.draw(g, cam);
        g.setTransform(new java.awt.geom.AffineTransform());

        g.setPaint(new RadialGradientPaint(new Point2D.Double(W / 2.0, H / 2.0), 640f, new float[]{0.55f, 1f},
                new Color[]{new Color(0, 0, 0, 0), new Color(10, 4, 30, 150)}));            // vignette
        g.fill(new Rectangle2D.Double(0, 0, W, H));
        particles.draw(g, cam);

        if (state != GameState.MENU) { scoreboard.draw(g, cam); drawHud(g); }
        switch (state) {
            case MENU -> drawMenu(g);
            case SERVE -> drawServe(g);
            case PAUSED -> drawPaused(g);
            case GAME_OVER -> drawGameOver(g);
            default -> { }
        }
        if (toastTime > 0 && state != GameState.GAME_OVER) drawToast(g);
        g.dispose();
    }

    private void updateCursor() {
        if (GraphicsEnvironment.isHeadless() || blankCursor == null) return;
        boolean playing = (state == GameState.SERVE || state == GameState.PLAYING) && nearR == humanNear;
        Cursor want = playing ? blankCursor : Cursor.getDefaultCursor();
        if (getCursor() != want) setCursor(want);
    }

    private void drawHud(Graphics2D g) {
        g.setFont(Theme.font(13f));
        Scoreboard.shadowCentered(g, mode == GameMode.VS_CPU
                ? "MOUSE moves your racquet   -   CLICK / SPACE to serve   -   P pause   -   ESC menu"
                : "P1: MOUSE or WASD, SPACE serves     P2: ARROWS, ENTER serves     -     P pause   -   ESC menu",
                W / 2.0, H - 18, Color.WHITE);
        if (rally > 2 && state == GameState.PLAYING) {
            g.setFont(Theme.font(18f));
            Scoreboard.shadowCentered(g, "RALLY  x" + rally, W / 2.0, 112, Theme.YELLOW);
        }
    }

    private void drawToast(Graphics2D g) {
        float a = (float) Math.min(1, toastTime / 0.5);
        g.setFont(Theme.font(46f));
        Scoreboard.shadowCentered(g, toast, W / 2.0, 330, Theme.alpha(Theme.YELLOW, (int) (255 * a)));
        g.setFont(Theme.font(18f));
        Scoreboard.shadowCentered(g, toastSub, W / 2.0, 358, Theme.alpha(Color.WHITE, (int) (230 * a)));
    }

    private void card(Graphics2D g, double x, double y, double w, double h) {
        RoundRectangle2D r = new RoundRectangle2D.Double(x, y, w, h, 40, 40);
        g.setColor(new Color(21, 16, 46, 232)); g.fill(r);
        g.setColor(Theme.alpha(Color.WHITE, 70)); g.setStroke(new BasicStroke(2f)); g.draw(r);
        g.setColor(Theme.alpha(Theme.PINK, 200)); g.setStroke(new BasicStroke(3f));
        g.draw(new RoundRectangle2D.Double(x + 5, y + 5, w - 10, h - 10, 34, 34));
    }

    private RoundRectangle2D modeChip(GameMode m) { return new RoundRectangle2D.Double(W / 2.0 + (m.ordinal() == 0 ? -180 : 10), 305, 170, 40, 40, 40); }
    private RoundRectangle2D difficultyChip(Difficulty d) { return new RoundRectangle2D.Double(W / 2.0 + (d.ordinal() - 1) * 150 - 64, 396, 128, 42, 42, 42); }

    private void drawMenu(Graphics2D g) {
        double cx = W / 2.0;
        card(g, cx - 270, 120, 540, 440);
        g.setFont(Theme.font(88f));
        String title = "PING POP";
        Color[] cs = {Theme.PINK, Theme.ORANGE, Theme.YELLOW, Theme.CYAN};
        FontMetrics fm = g.getFontMetrics();
        double x = cx - fm.stringWidth(title) / 2.0;
        for (int i = 0; i < title.length(); i++) {
            String ch = String.valueOf(title.charAt(i));
            double bob = Math.sin(bgPhase * 3 + i * 0.6) * 4;
            g.setColor(new Color(0, 0, 0, 90)); g.drawString(ch, (float) x + 4, (float) (250 + bob + 5));
            g.setColor(cs[i % cs.length]);      g.drawString(ch, (float) x, (float) (250 + bob));
            x += fm.stringWidth(ch);
        }
        g.setFont(Theme.font(17f));
        Scoreboard.drawCentered(g, "T A B L E    T E N N I S    3 D", cx, 284, Theme.TEXT_DIM);

        for (GameMode m : GameMode.values()) {
            RoundRectangle2D chip = modeChip(m);
            boolean sel = m == mode;
            Color c = m == GameMode.VS_CPU ? Theme.CYAN : Theme.PINK;
            if (sel) { g.setColor(c); g.fill(chip); }
            else { g.setColor(Theme.alpha(c, 40)); g.fill(chip); g.setColor(c); g.setStroke(new BasicStroke(2f)); g.draw(chip); }
            g.setFont(Theme.font(17f));
            Scoreboard.drawCentered(g, m.getLabel(), chip.getCenterX(), chip.getCenterY() + 6, sel ? new Color(0x1A1240) : c);
        }
        boolean cpu = mode == GameMode.VS_CPU;
        g.setFont(Theme.font(13f));
        Scoreboard.drawCentered(g, cpu ? "D I F F I C U L T Y" : "D I F F I C U L T Y   ( C P U   O N L Y )", cx, 383, Theme.alpha(Theme.TEXT_DIM, cpu ? 255 : 110));
        Color[] dc = {Theme.CYAN, Theme.YELLOW, Theme.PINK};
        for (Difficulty d : Difficulty.values()) {
            RoundRectangle2D chip = difficultyChip(d);
            boolean sel = d == difficulty;
            Color c = Theme.alpha(dc[d.ordinal()], cpu ? 255 : 90);
            if (sel && cpu) { g.setColor(c); g.fill(chip); }
            else { g.setColor(Theme.alpha(c, 40)); g.fill(chip); g.setColor(c); g.setStroke(new BasicStroke(2f)); g.draw(chip); }
            g.setFont(Theme.font(18f));
            Scoreboard.drawCentered(g, (d.ordinal() + 1) + "  " + d.getLabel().toUpperCase(), chip.getCenterX(), chip.getCenterY() + 6, sel && cpu ? new Color(0x1A1240) : c);
        }
        int pulse = 170 + (int) (85 * Math.sin(bgPhase * 4));
        g.setFont(Theme.font(28f));
        Scoreboard.drawCentered(g, "CLICK or ENTER to PLAY", cx, 484, Theme.alpha(Color.WHITE, pulse));
        g.setFont(Theme.font(13f));
        Scoreboard.drawCentered(g, "Move the MOUSE to swing your racquet, hold click to grab it", cx, 520, Theme.TEXT_DIM);
        Scoreboard.drawCentered(g, "WASD / ARROWS also work  -  P pause  -  ESC menu  -  first to " + Scoreboard.WIN_SCORE, cx, 540, Theme.TEXT_DIM);
    }

    private void drawServe(Graphics2D g) {
        g.setFont(Theme.font(24f));
        int pulse = 190 + (int) (65 * Math.sin(bgPhase * 6));
        String msg;
        if (server instanceof PlayerRacquet) {
            msg = mode == GameMode.VS_CPU ? "CLICK  or  SPACE  to SERVE"
                    : (server == humanNear ? "P1:  CLICK / SPACE  to SERVE" : "P2:  ENTER  to SERVE");
        } else msg = (mode == GameMode.VS_CPU ? "CPU" : "P2") + "  SERVES...";
        Scoreboard.shadowCentered(g, msg, W / 2.0, 250, Theme.alpha(Color.WHITE, pulse));
    }

    private void drawPaused(Graphics2D g) {
        g.setColor(new Color(10, 8, 30, 170)); g.fillRect(0, 0, W, H);
        card(g, W / 2.0 - 200, 240, 400, 170);
        g.setFont(Theme.font(56f));
        Scoreboard.drawCentered(g, "PAUSED", W / 2.0, 320, Theme.YELLOW);
        g.setFont(Theme.font(16f));
        Scoreboard.drawCentered(g, "Press P or click to resume", W / 2.0, 365, Theme.TEXT_DIM);
        Scoreboard.drawCentered(g, "ESC for menu", W / 2.0, 390, Theme.TEXT_DIM);
    }

    private void drawGameOver(Graphics2D g) {
        g.setColor(new Color(10, 8, 30, 165)); g.fillRect(0, 0, W, H);
        particles.draw(g, cam);
        boolean won = scoreboard.playerWon();
        card(g, W / 2.0 - 280, 190, 560, 300);
        g.setFont(Theme.font(64f));
        String headline = mode == GameMode.VS_CPU ? (won ? "YOU WIN!" : "CPU WINS") : (won ? "P1 WINS!" : "P2 WINS!");
        Scoreboard.drawCentered(g, headline, W / 2.0, 285, won ? Theme.YELLOW : Theme.CYAN);
        g.setFont(Theme.font(40f));
        Scoreboard.drawCentered(g, scoreboard.getPlayerScore() + "  -  " + scoreboard.getAiScore(), W / 2.0, 345, Theme.TEXT);
        g.setFont(Theme.font(16f));
        Scoreboard.drawCentered(g, "Longest rally: " + scoreboard.getBestRally() + " hits   -   " + (mode == GameMode.VS_CPU ? difficulty.getLabel() : mode.getLabel()), W / 2.0, 382, Theme.TEXT_DIM);
        int pulse = 170 + (int) (85 * Math.sin(bgPhase * 4));
        g.setFont(Theme.font(22f));
        Scoreboard.drawCentered(g, "ENTER or CLICK  play again", W / 2.0, 430, Theme.alpha(Color.WHITE, pulse));
        g.setFont(Theme.font(14f));
        Scoreboard.drawCentered(g, "ESC  main menu", W / 2.0, 458, Theme.TEXT_DIM);
    }

    /* ---------------------------------------------------------------- input (inner classes) */
    private class KeyHandler extends KeyAdapter {
        @Override public void keyPressed(KeyEvent e) { keys(e.getKeyCode(), true); }
        @Override public void keyReleased(KeyEvent e) { keys(e.getKeyCode(), false); }

        private void keys(int k, boolean down) {
            boolean two = mode == GameMode.TWO_PLAYER && state != GameState.MENU;
            PlayerRacquet arrows = two ? humanFar : humanNear;
            switch (k) {
                case KeyEvent.VK_A -> humanNear.setLeft(down);
                case KeyEvent.VK_D -> humanNear.setRight(down);
                case KeyEvent.VK_W -> humanNear.setUp(down);
                case KeyEvent.VK_S -> humanNear.setDown(down);
                case KeyEvent.VK_LEFT -> arrows.setLeft(down);
                case KeyEvent.VK_RIGHT -> arrows.setRight(down);
                case KeyEvent.VK_UP -> arrows.setUp(down);
                case KeyEvent.VK_DOWN -> arrows.setDown(down);
                default -> { }
            }
            if (!down) return;
            switch (state) {
                case MENU -> {
                    if (k == KeyEvent.VK_M || k == KeyEvent.VK_UP || k == KeyEvent.VK_DOWN) mode = mode.toggle();
                    if (k == KeyEvent.VK_1) difficulty = Difficulty.EASY;
                    if (k == KeyEvent.VK_2) difficulty = Difficulty.MEDIUM;
                    if (k == KeyEvent.VK_3) difficulty = Difficulty.HARD;
                    if (k == KeyEvent.VK_RIGHT) difficulty = difficulty.next();
                    if (k == KeyEvent.VK_LEFT) difficulty = difficulty.previous();
                    if (k == KeyEvent.VK_ENTER || k == KeyEvent.VK_SPACE) startMatch(difficulty);
                }
                case GAME_OVER -> {
                    if (k == KeyEvent.VK_ENTER) startMatch(difficulty);
                    if (k == KeyEvent.VK_ESCAPE) enterMenu();
                }
                case SERVE, PLAYING, PAUSED -> {
                    if (k == KeyEvent.VK_P) togglePause();
                    if (k == KeyEvent.VK_ESCAPE) enterMenu();
                    if (k == KeyEvent.VK_SPACE) requestServe(humanNear);
                    if (k == KeyEvent.VK_ENTER) requestServe(mode == GameMode.TWO_PLAYER ? humanFar : humanNear);
                }
            }
        }
    }

    private class MouseHandler extends MouseAdapter {
        @Override public void mouseMoved(MouseEvent e) { requestFocusInWindow(); mouseMove(e.getX(), e.getY()); }
        @Override public void mouseDragged(MouseEvent e) { mouseMove(e.getX(), e.getY()); }
        @Override public void mouseReleased(MouseEvent e) { mouseRelease(); }
        @Override public void mousePressed(MouseEvent e) {
            requestFocusInWindow();
            switch (state) {
                case SERVE, PLAYING -> mousePress(e.getX(), e.getY());
                case PAUSED -> togglePause();
                case GAME_OVER -> startMatch(difficulty);
                case MENU -> menuClick(e.getX(), e.getY());
            }
        }
        private void menuClick(double mx, double my) {
            for (GameMode m : GameMode.values()) if (modeChip(m).contains(mx, my)) { mode = m; return; }
            if (mode == GameMode.VS_CPU)
                for (Difficulty d : Difficulty.values()) if (difficultyChip(d).contains(mx, my)) { difficulty = d; return; }
            if (Math.abs(mx - W / 2.0) < 190 && my > 452 && my < 500) startMatch(difficulty);
        }
    }
}
