package dev.ggclient.client.util;

import dev.ggclient.client.render.Draw;
import dev.ggclient.client.render.Text;
import dev.ggclient.client.ui.Theme;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/** Panel and text drawing in the client theme, used by the Donut HUD lists. */
public final class DonutHud {
    public static final float SIZE = 8f;

    private DonutHud() {}

    public static void panel(GuiGraphicsExtractor g, int x, int y, int w, int h) {
        Theme.update();
        Draw.rect(g, x, y, w, h, 5, Theme.SIDEBAR);
    }

    public static void bar(GuiGraphicsExtractor g, int x, int y, int w, int h, float fraction, int color) {
        Draw.rect(g, x, y, w, h, 1, Theme.TRACK);
        int filled = Math.max(1, Math.round(w * Math.max(0f, Math.min(1f, fraction))));
        Draw.rect(g, x, y, filled, h, 1, color);
    }

    public static void text(GuiGraphicsExtractor g, String text, float x, float centerY, int color) {
        Text.draw(g, text, x, Text.centeredBaseline(text, SIZE, centerY), SIZE, color);
    }

    public static float width(String text) { return Text.width(text, SIZE); }
}
