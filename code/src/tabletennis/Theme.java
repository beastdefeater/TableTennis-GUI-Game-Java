package tabletennis;

import java.awt.Color;
import java.awt.Font;

/** Palette + helpers (final utility class with a private constructor). */
public final class Theme {
    public static final Color WALL_TOP = new Color(0x5E0F1C), WALL_BOTTOM = new Color(0xE34A3B);
    public static final Color FLOOR_FAR = new Color(0xF0A04B), FLOOR_NEAR = new Color(0x7A3F1A);
    public static final Color TABLE_FAR = new Color(0x57D2FA), TABLE_NEAR = new Color(0x0A74C2), TABLE_EDGE = new Color(0x06578F);
    public static final Color RACQUET_RED = new Color(0xF0332B), RACQUET_BLUE = new Color(0x2D7BFF);
    public static final Color WOOD_LIGHT = new Color(0xE6B070), WOOD_DARK = new Color(0xA8672E);
    public static final Color PINK = new Color(0xFF4D8D), CYAN = new Color(0x2EF2D0), YELLOW = new Color(0xFFE14D), ORANGE = new Color(0xFF9A3D);
    public static final Color TEXT = Color.WHITE, TEXT_DIM = new Color(0xD9D2F2), NAVY = new Color(0x15102E);

    private Theme() { }
    public static Font font(float size) { return new Font(Font.SANS_SERIF, Font.BOLD, 12).deriveFont(size); }
    public static Color alpha(Color c, int a) { return new Color(c.getRed(), c.getGreen(), c.getBlue(), Math.max(0, Math.min(255, a))); }
    public static double clamp(double v, double lo, double hi) { return Math.max(lo, Math.min(hi, v)); }
}
