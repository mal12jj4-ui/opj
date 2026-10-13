package dev.ggclient.client.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;

/** Small helpers shared by the Donut modules. */
public final class Donut {
    private Donut() {}

    public static Screen screen() { return Minecraft.getInstance().gui.screen(); }

    public static void say(String message) {
        var player = Minecraft.getInstance().player;
        if (player != null) player.sendSystemMessage(Component.literal(message));
    }

    public static long chunkKey(int cx, int cz) { return ((long) cx << 32) ^ (cz & 0xFFFFFFFFL); }
    public static int chunkKeyX(long key) { return (int) (key >> 32); }
    public static int chunkKeyZ(long key) { return (int) key; }

    /** Loaded client chunk or null. */
    public static LevelChunk chunk(int cx, int cz) {
        var level = Minecraft.getInstance().level;
        if (level == null) return null;
        ChunkAccess c = level.getChunkSource().getChunk(cx, cz, ChunkStatus.FULL, false);
        return c instanceof LevelChunk lc ? lc : null;
    }
}
