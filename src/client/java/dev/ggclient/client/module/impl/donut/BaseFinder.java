package dev.ggclient.client.module.impl.donut;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dev.ggclient.client.event.events.HudRenderEvent;
import dev.ggclient.client.event.events.TickEvent;
import dev.ggclient.client.event.events.WorldRenderEvent;
import dev.ggclient.client.module.Module;
import dev.ggclient.client.render.Style;
import dev.ggclient.client.setting.BoolSetting;
import dev.ggclient.client.setting.EnumSetting;
import dev.ggclient.client.setting.SliderSetting;
import dev.ggclient.client.ui.Theme;
import dev.ggclient.client.util.Colors;
import dev.ggclient.client.util.Donut;
import dev.ggclient.client.util.DonutHud;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/** Scores every loaded chunk for signs of player activity and marks the ones above a threshold. */
public final class BaseFinder extends Module {
    private final BoolSetting storageSignal = add(new BoolSetting("storage", true));
    private final BoolSetting machinerySignal = add(new BoolSetting("machinery", true));
    private final BoolSetting lightSignal = add(new BoolSetting("lights", true));
    private final BoolSetting playerBlockSignal = add(new BoolSetting("playerBlocks", true));
    private final SliderSetting maxY = add(new SliderSetting("maxY", 0, -64, 320));
    private final SliderSetting minScore = add(new SliderSetting("minScore", 10, 1, 100));
    private final SliderSetting range = add(new SliderSetting("range", 8, 1, 16));
    private final EnumSetting alerts = add(new EnumSetting("alerts", 2, "Off", "Chat", "Chat + Sound"));
    private final SliderSetting alertCooldown = add(new SliderSetting("alertCooldown", 30, 1, 300));
    private final SliderSetting maxPanels = add(new SliderSetting("maxPanels", 5, 0, 12));
    private final BoolSetting render = add(new BoolSetting("render", true));
    private final BoolSetting exportSnapshot = add(new BoolSetting("exportSnapshot", false));

    private static final class Info {
        int cx, cz, storage, machinery, lights, player;
        int minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;
        double raw, dist;
        int pct;
        String kind = "Unknown";

        void track(int y) { minY = Math.min(minY, y); maxY = Math.max(maxY, y); }
        double centerX() { return cx * 16 + 8; }
        double centerZ() { return cz * 16 + 8; }
    }

    private final Map<Long, Info> cache = new HashMap<>();
    private final Map<Long, Integer> lastScan = new HashMap<>();
    private final Map<Long, Integer> lastAlert = new HashMap<>();
    private final List<Info> flagged = new ArrayList<>();
    private int tick;

    public BaseFinder() {
        super("baseFinder");
    }

    public boolean hasFlagged() { return !flagged.isEmpty(); }

    @Override
    public String info() { return Integer.toString(flagged.size()); }

    @Override
    protected void onEnable() {
        cache.clear(); lastScan.clear(); lastAlert.clear(); flagged.clear(); tick = 0;
        listen(TickEvent.class, this::onTick);
        listen(WorldRenderEvent.class, this::onWorld);
        listen(HudRenderEvent.class, this::onHud);
    }

    @Override
    protected void onDisable() { flagged.clear(); }

    private static boolean isLight(BlockState s) {
        return s.is(Blocks.TORCH) || s.is(Blocks.WALL_TORCH) || s.is(Blocks.SOUL_TORCH)
                || s.is(Blocks.SOUL_WALL_TORCH) || s.is(Blocks.LANTERN) || s.is(Blocks.SOUL_LANTERN)
                || s.is(Blocks.GLOWSTONE) || s.is(Blocks.SEA_LANTERN) || s.is(Blocks.SHROOMLIGHT)
                || s.is(Blocks.REDSTONE_LAMP);
    }

    private static boolean isPlayerBlock(BlockState s) {
        return s.is(Blocks.COBBLESTONE) || s.is(Blocks.STONE_BRICKS) || s.is(Blocks.BRICKS)
                || s.is(Blocks.GLASS) || s.is(Blocks.LADDER) || s.is(Blocks.SCAFFOLDING)
                || s.is(BlockTags.PLANKS) || s.is(BlockTags.DOORS);
    }

    private void onTick(TickEvent event) {
        if (mc.level == null || mc.player == null) return;
        tick++;

        int r = range.get();
        ChunkPos pc = mc.player.chunkPosition();
        int budget = 3;
        for (int dx = -r; dx <= r && budget > 0; dx++) {
            for (int dz = -r; dz <= r && budget > 0; dz++) {
                int cx = pc.x() + dx, cz = pc.z() + dz;
                long key = Donut.chunkKey(cx, cz);
                Integer last = lastScan.get(key);
                if (last != null && tick - last < 200) continue;
                scanChunk(cx, cz, key);
                lastScan.put(key, tick);
                budget--;
            }
        }

        if (tick % 20 == 0) refresh();

        if (tick % 40 == 0) {
            Iterator<Long> it = cache.keySet().iterator();
            while (it.hasNext()) {
                long k = it.next();
                if (Math.abs(Donut.chunkKeyX(k) - pc.x()) > r + 2 || Math.abs(Donut.chunkKeyZ(k) - pc.z()) > r + 2) {
                    it.remove();
                    lastScan.remove(k);
                }
            }
        }

        if (exportSnapshot.get()) {
            export();
            exportSnapshot.set(false);
        }
    }

    private void scanChunk(int cx, int cz, long key) {
        LevelChunk chunk = Donut.chunk(cx, cz);
        if (chunk == null) { cache.remove(key); return; }

        Info info = new Info();
        info.cx = cx; info.cz = cz;
        int yLimit = maxY.get();

        for (BlockEntity be : new ArrayList<>(chunk.getBlockEntities().values())) {
            BlockPos p = be.getBlockPos();
            if (p.getY() > yLimit) continue;
            if (be instanceof ChestBlockEntity || be instanceof BarrelBlockEntity || be instanceof ShulkerBoxBlockEntity) {
                info.storage++;
            } else if (be instanceof HopperBlockEntity || be instanceof DispenserBlockEntity
                    || be instanceof AbstractFurnaceBlockEntity) {
                info.machinery++;
            } else {
                continue;
            }
            info.track(p.getY());
        }

        Predicate<BlockState> wanted = s -> isLight(s) || isPlayerBlock(s);
        LevelChunkSection[] sections = chunk.getSections();
        for (int i = 0; i < sections.length; i++) {
            LevelChunkSection sec = sections[i];
            if (sec == null || sec.hasOnlyAir()) continue;
            int baseY = chunk.getSectionYFromSectionIndex(i) << 4;
            if (baseY > yLimit || !sec.maybeHas(wanted)) continue;
            for (int x = 0; x < 16; x++) {
                for (int y = 0; y < 16; y++) {
                    int wy = baseY + y;
                    if (wy > yLimit) continue;
                    for (int z = 0; z < 16; z++) {
                        BlockState s = sec.getBlockState(x, y, z);
                        if (isLight(s)) { info.lights++; info.track(wy); }
                        else if (isPlayerBlock(s)) { info.player++; }
                    }
                }
            }
        }
        cache.put(key, info);
    }

    private void refresh() {
        flagged.clear();
        Vec3 pp = mc.player.position();
        double threshold = minScore.get();

        for (Info i : cache.values()) {
            double raw = 0;
            if (storageSignal.get()) raw += i.storage * 3.0;
            if (machinerySignal.get()) raw += i.machinery * 4.0;
            if (lightSignal.get()) raw += i.lights;
            if (playerBlockSignal.get()) raw += i.player * 0.25;
            i.raw = raw;
            if (raw < threshold) continue;

            i.pct = (int) Math.min(99, raw / (threshold * 2.0) * 100.0);
            double st = i.storage * 3.0, ma = i.machinery * 4.0;
            if (st > 0 && st >= raw * 0.4 && st >= ma) i.kind = "Stash";
            else if (ma > 0 && ma >= raw * 0.4) i.kind = "Redstone";
            else i.kind = "Unknown";
            i.dist = Math.hypot(i.centerX() - pp.x, i.centerZ() - pp.z);
            flagged.add(i);
        }
        flagged.sort(Comparator.comparingDouble(i -> i.dist));

        String mode = alerts.selected();
        if (mode.equals("Off")) return;
        int cooldownTicks = alertCooldown.get() * 20;
        for (Info i : flagged) {
            long key = Donut.chunkKey(i.cx, i.cz);
            Integer last = lastAlert.get(key);
            if (last != null && tick - last < cooldownTicks) continue;
            lastAlert.put(key, tick);
            Donut.say("\u00a76[Base Finder] \u00a7f" + i.kind + " " + i.pct + "% at "
                    + (int) i.centerX() + ", " + (int) i.centerZ() + " (Y " + yRange(i) + ") - "
                    + (int) i.dist + "m " + direction(i.centerX() - pp.x, i.centerZ() - pp.z));
            if (mode.equals("Chat + Sound")) mc.player.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
        }
    }

    private static String yRange(Info i) {
        if (i.minY == Integer.MAX_VALUE) return "?";
        return i.minY == i.maxY ? String.valueOf(i.minY) : i.minY + ".." + i.maxY;
    }

    private static String direction(double dx, double dz) {
        double ang = Math.toDegrees(Math.atan2(dx, -dz));
        if (ang < 0) ang += 360;
        String[] names = {"N", "NE", "E", "SE", "S", "SW", "W", "NW"};
        return names[(int) Math.round(ang / 45.0) % 8];
    }

    private void export() {
        try {
            JsonArray arr = new JsonArray();
            for (Info i : flagged) {
                JsonObject o = new JsonObject();
                o.addProperty("chunkX", i.cx);
                o.addProperty("chunkZ", i.cz);
                o.addProperty("kind", i.kind);
                o.addProperty("percent", i.pct);
                o.addProperty("storage", i.storage);
                o.addProperty("machinery", i.machinery);
                o.addProperty("lights", i.lights);
                o.addProperty("playerBlocks", i.player);
                o.addProperty("y", yRange(i));
                arr.add(o);
            }
            JsonObject root = new JsonObject();
            root.add("detections", arr);
            Files.writeString(mc.gameDirectory.toPath().resolve("ggclient-basefinder-snapshot.json"),
                    new GsonBuilder().setPrettyPrinting().create().toJson(root));
            Donut.say("\u00a76[Base Finder] \u00a7fSnapshot saved (" + arr.size() + " chunks)");
        } catch (Exception e) {
            Donut.say("\u00a76[Base Finder] \u00a7cCould not save snapshot");
        }
    }

    private void onWorld(WorldRenderEvent event) {
        if (!render.get()) return;
        for (Info i : flagged) {
            double y0 = i.minY == Integer.MAX_VALUE ? maxY.get() : i.minY;
            AABB b = new AABB(i.cx * 16, y0, i.cz * 16, i.cx * 16 + 16, y0 + 1, i.cz * 16 + 16);
            int rgb = switch (i.kind) {
                case "Stash" -> 0xFFC857;
                case "Redstone" -> 0xFF5A5A;
                default -> 0x6C8CFF;
            };
            event.buffer().box(b, Style.stroke(Colors.opaque(rgb), 2f).withFill(Colors.withAlpha(Colors.opaque(rgb), 0.18f)));
        }
    }

    private void onHud(HudRenderEvent event) {
        int shown = Math.min(maxPanels.get(), flagged.size());
        if (maxPanels.get() == 0 || mc.player == null) return;
        GuiGraphicsExtractor g = event.graphics();
        Vec3 pp = mc.player.position();
        int x = 6, y = 6, w = 150;
        int h = 20 + shown * 28 + (flagged.size() > shown ? 12 : 0) + 4;
        DonutHud.panel(g, x, y, w, h);
        DonutHud.text(g, "Base Finder", x + 7, y + 9, Theme.ACCENT);
        String n = flagged.size() + " nearby";
        DonutHud.text(g, n, x + w - 7 - DonutHud.width(n), y + 9, Theme.MUTED);

        int ey = y + 22;
        for (int k = 0; k < shown; k++) {
            Info i = flagged.get(k);
            int barColor = i.pct >= 95 ? Theme.ACCENT : 0xFF4DDB6A;
            DonutHud.text(g, i.kind, x + 7, ey + 4, Theme.TEXT);
            String pct = i.pct + "%";
            DonutHud.text(g, pct, x + w - 7 - DonutHud.width(pct), ey + 4, barColor);
            DonutHud.text(g, direction(i.centerX() - pp.x, i.centerZ() - pp.z) + " " + (int) i.dist + "m / Y " + yRange(i),
                    x + 7, ey + 13, Theme.MUTED);
            DonutHud.bar(g, x + 7, ey + 19, w - 14, 2, i.pct / 100f, barColor);
            ey += 28;
        }
        if (flagged.size() > shown) {
            DonutHud.text(g, "+" + (flagged.size() - shown) + " more nearby", x + 7, ey + 4, Theme.MUTED);
        }
    }
}
