package dev.ggclient.client.module.impl.donut;

import dev.ggclient.client.event.events.HudRenderEvent;
import dev.ggclient.client.event.events.TickEvent;
import dev.ggclient.client.event.events.WorldRenderEvent;
import dev.ggclient.client.module.Module;
import dev.ggclient.client.render.Style;
import dev.ggclient.client.setting.BoolSetting;
import dev.ggclient.client.setting.Measure;
import dev.ggclient.client.setting.SliderSetting;
import dev.ggclient.client.ui.Theme;
import dev.ggclient.client.util.Colors;
import dev.ggclient.client.util.Donut;
import dev.ggclient.client.util.DonutHud;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Groups storage and machinery into clusters, boxes them through walls and lists the nearest ones. */
public final class BaseEsp extends Module {
    private static final int COLOR = 0xFF8A3D;

    private final SliderSetting minBlocks = add(new SliderSetting("minBlocks", 24, 1, 200));
    private final SliderSetting clusterRadius = add(new SliderSetting("clusterRadius", 24, 4, 64, Measure.DISTANCE));
    private final SliderSetting range = add(new SliderSetting("range", 308, 32, 512, Measure.DISTANCE));
    private final SliderSetting maxY = add(new SliderSetting("maxY", 0, -64, 320));
    private final SliderSetting maxPanels = add(new SliderSetting("maxPanels", 8, 0, 16));
    private final BoolSetting box = add(new BoolSetting("box", true));
    private final BoolSetting notify = add(new BoolSetting("notify", true));

    private static final class Group {
        AABB box; int score; Vec3 center; double dist; long key;
    }

    private final List<Group> groups = new ArrayList<>();
    private final Set<Long> announced = new HashSet<>();
    private int timer;

    public BaseEsp() {
        super("baseEsp");
    }

    public boolean hasGroups() { return !groups.isEmpty(); }

    @Override
    public String info() { return Integer.toString(groups.size()); }

    @Override
    protected void onEnable() {
        groups.clear(); announced.clear(); timer = 0;
        listen(TickEvent.class, this::onTick);
        listen(WorldRenderEvent.class, this::onWorld);
        listen(HudRenderEvent.class, this::onHud);
    }

    @Override
    protected void onDisable() { groups.clear(); }

    private static int scoreOf(BlockEntity be) {
        if (be instanceof HopperBlockEntity) return 4;
        if (be instanceof ChestBlockEntity || be instanceof BarrelBlockEntity
                || be instanceof ShulkerBoxBlockEntity || be instanceof DispenserBlockEntity) return 3;
        if (be instanceof AbstractFurnaceBlockEntity) return 2;
        return 0;
    }

    private void onTick(TickEvent event) {
        if (mc.level == null || mc.player == null) return;
        if (timer++ % 20 != 0) return;

        double rng = range.get();
        double rngSq = rng * rng;
        int chunkR = (int) (rng / 16) + 1;
        ChunkPos pc = mc.player.chunkPosition();
        Vec3 pp = mc.player.position();

        // 1. collect scored block entities
        List<int[]> pts = new ArrayList<>();
        outer:
        for (int dx = -chunkR; dx <= chunkR; dx++) {
            for (int dz = -chunkR; dz <= chunkR; dz++) {
                LevelChunk chunk = Donut.chunk(pc.x() + dx, pc.z() + dz);
                if (chunk == null) continue;
                for (BlockEntity be : new ArrayList<>(chunk.getBlockEntities().values())) {
                    int s = scoreOf(be);
                    if (s <= 0) continue;
                    BlockPos p = be.getBlockPos();
                    if (p.getY() > maxY.get()) continue;
                    double ddx = p.getX() + 0.5 - pp.x, ddy = p.getY() + 0.5 - pp.y, ddz = p.getZ() + 0.5 - pp.z;
                    if (ddx * ddx + ddy * ddy + ddz * ddz > rngSq) continue;
                    pts.add(new int[]{p.getX(), p.getY(), p.getZ(), s});
                    if (pts.size() >= 6000) break outer;
                }
            }
        }

        // 2. cluster with union-find over a spatial hash
        int n = pts.size();
        int[] parent = new int[n];
        for (int i = 0; i < n; i++) parent[i] = i;
        int r = clusterRadius.get();
        Map<Long, List<Integer>> cells = new HashMap<>();
        for (int i = 0; i < n; i++) {
            int[] p = pts.get(i);
            cells.computeIfAbsent(cellKey(Math.floorDiv(p[0], r), Math.floorDiv(p[1], r), Math.floorDiv(p[2], r)),
                    k -> new ArrayList<>()).add(i);
        }
        for (int i = 0; i < n; i++) {
            int[] p = pts.get(i);
            int cx = Math.floorDiv(p[0], r), cy = Math.floorDiv(p[1], r), cz = Math.floorDiv(p[2], r);
            for (int ox = -1; ox <= 1; ox++) for (int oy = -1; oy <= 1; oy++) for (int oz = -1; oz <= 1; oz++) {
                List<Integer> bucket = cells.get(cellKey(cx + ox, cy + oy, cz + oz));
                if (bucket == null) continue;
                for (int j : bucket) {
                    if (j <= i) continue;
                    int[] q = pts.get(j);
                    if (Math.abs(p[0] - q[0]) <= r && Math.abs(p[1] - q[1]) <= r && Math.abs(p[2] - q[2]) <= r) {
                        union(parent, i, j);
                    }
                }
            }
        }

        // 3. build groups
        Map<Integer, int[]> acc = new HashMap<>(); // root -> {score, minX, minY, minZ, maxX, maxY, maxZ}
        for (int i = 0; i < n; i++) {
            int[] p = pts.get(i);
            int root = find(parent, i);
            int[] a = acc.get(root);
            if (a == null) {
                acc.put(root, new int[]{p[3], p[0], p[1], p[2], p[0], p[1], p[2]});
            } else {
                a[0] += p[3];
                a[1] = Math.min(a[1], p[0]); a[2] = Math.min(a[2], p[1]); a[3] = Math.min(a[3], p[2]);
                a[4] = Math.max(a[4], p[0]); a[5] = Math.max(a[5], p[1]); a[6] = Math.max(a[6], p[2]);
            }
        }

        List<Group> found = new ArrayList<>();
        for (int[] a : acc.values()) {
            if (a[0] < minBlocks.get()) continue;
            Group g = new Group();
            g.score = a[0];
            g.box = new AABB(a[1], a[2], a[3], a[4] + 1, a[5] + 1, a[6] + 1).inflate(0.1);
            g.center = g.box.getCenter();
            g.dist = g.center.distanceTo(pp);
            g.key = (((long) (a[1] >> 4)) << 32) ^ ((a[3] >> 4) & 0xFFFFFFFFL);
            found.add(g);
        }
        found.sort(Comparator.comparingDouble(g -> g.dist));

        for (Group g : found) {
            if (announced.add(g.key) && notify.get()) {
                Donut.say("\u00a76[Base ESP] \u00a7fGroup (score " + g.score + ") near "
                        + (int) g.center.x + ", " + (int) g.center.y + ", " + (int) g.center.z
                        + " - " + (int) g.dist + "m away");
                mc.player.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
            }
        }

        groups.clear();
        groups.addAll(found);
    }

    private static long cellKey(int x, int y, int z) {
        return ((long) (x & 0x1FFFFF) << 42) | ((long) (y & 0x1FFFFF) << 21) | (z & 0x1FFFFF);
    }

    private static int find(int[] parent, int i) {
        while (parent[i] != i) { parent[i] = parent[parent[i]]; i = parent[i]; }
        return i;
    }

    private static void union(int[] parent, int a, int b) {
        int ra = find(parent, a), rb = find(parent, b);
        if (ra != rb) parent[ra] = rb;
    }

    private void onWorld(WorldRenderEvent event) {
        if (!box.get()) return;
        Style style = Style.stroke(Colors.opaque(COLOR), 2f).withFill(Colors.withAlpha(Colors.opaque(COLOR), 0.18f));
        for (Group g : groups) event.buffer().box(g.box, style);
    }

    private void onHud(HudRenderEvent event) {
        int count = Math.min(maxPanels.get(), groups.size());
        if (count == 0) return;
        GuiGraphicsExtractor g = event.graphics();
        int w = 150, h = 24;
        int x = mc.getWindow().getGuiScaledWidth() - w - 6, y = 6;
        for (int i = 0; i < count; i++) {
            Group grp = groups.get(i);
            DonutHud.panel(g, x, y, w, h);
            DonutHud.text(g, "Base group  " + (int) grp.dist + "m", x + 7, y + 8, Theme.TEXT);
            DonutHud.text(g, "Score " + grp.score + "   Y " + (int) grp.center.y, x + 7, y + 17, Theme.MUTED);
            y += h + 3;
        }
    }
}
