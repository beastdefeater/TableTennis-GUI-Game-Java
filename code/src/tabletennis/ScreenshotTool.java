package tabletennis;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;

/** Dev helper: renders the game off-screen (no display needed) and saves PNG screenshots. */
public class ScreenshotTool {
    public static void main(String[] args) throws Exception {
        System.setProperty("java.awt.headless", "true");
        File out = new File(args.length > 0 ? args[0] : "../output");
        out.mkdirs();
        for (File f : out.listFiles((d, n) -> n.endsWith(".png"))) f.delete();

        GamePanel p = new GamePanel(false);
        p.setSize(GamePanel.W, GamePanel.H);

        advance(p, 140);
        save(p, new File(out, "01_main_menu.png"));

        p.startMatch(Difficulty.MEDIUM);
        p.mouseMove(360, 480);
        advance(p, 45);
        save(p, new File(out, "02_serve_click_to_serve.png"));

        p.mousePress(380, 470);                         // click = grab racquet + serve
        advance(p, 14);
        save(p, new File(out, "03_mouse_grab_serve.png"));
        p.mouseRelease();

        p.setDemo(true);                                // let the computer play both sides for the demo shots
        waitFor(p, 3, 0.9, 1.9);
        save(p, new File(out, "04_rally_over_net.png"));
        advance(p, 20);
        waitFor(p, 5, 0.2, 0.9);
        save(p, new File(out, "05_rally_incoming.png"));

        p.togglePause();
        save(p, new File(out, "06_paused.png"));

        p.forceFinish(true);
        advance(p, 45);
        save(p, new File(out, "07_game_over_win.png"));

        p.showMenu();
        p.setMode(GameMode.TWO_PLAYER);
        advance(p, 90);
        save(p, new File(out, "08_menu_two_player.png"));
        System.out.println("Screenshots saved to " + out.getAbsolutePath());
    }

    private static void waitFor(GamePanel p, int rally, double z0, double z1) {
        for (int i = 0; i < 20000; i++) {
            p.step(1 / 60.0);
            if (p.getState() == GameState.PLAYING && p.getRally() >= rally && p.getBallZ() > z0 && p.getBallZ() < z1 && p.getBallY() > 0.15) return;
        }
    }
    private static void advance(GamePanel p, int frames) { for (int i = 0; i < frames; i++) p.step(1 / 60.0); }
    private static void save(GamePanel p, File f) throws Exception {
        BufferedImage img = new BufferedImage(GamePanel.W, GamePanel.H, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        p.paint(g);
        g.dispose();
        ImageIO.write(img, "png", f);
    }
}
