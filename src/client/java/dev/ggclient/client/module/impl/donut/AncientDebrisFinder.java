package dev.ggclient.client.module.impl.donut;

import dev.ggclient.client.event.events.TickEvent;
import dev.ggclient.client.event.events.WorldRenderEvent;
import dev.ggclient.client.module.Module;
import dev.ggclient.client.render.Style;
import dev.ggclient.client.setting.BoolSetting;
import dev.ggclient.client.setting.SliderSetting;
import dev.ggclient.client.util.Colors;
import dev.ggclient.client.util.Donut;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public final class AncientDebrisFinder extends Module {
    private static final int COLOR = 0xB5651D;
    private static final int MAX_BOXES = 600;

    private final SliderSetting range = add(new SliderSetting("range", 6, 1, 16));
    private final BoolSetting fill = add(new BoolSetting("fill", true));

    private final Map<Long, List<BlockPos>> cache = new HashMap<>();
    private final Map<Long, Integer> lastScan = new HashMap<>();
    private int tick;

    public AncientDebrisFinder() {
        super("ancientDebrisFinder");
    }

    @Override
    public String info() {
        int n = 0;
        for (List<BlockPos> list : cache.values()) n += list.size();
        return Integer.toString(n);
    }

    @Override
    protected void onEnable() {
        cache.clear(); lastScan.clear(); tick = 0;
        listen(TickEvent.class, this::onTick);
        listen(WorldRenderEvent.class, this::onWorld);
    }

    @Override
    protected void onDisable() { cache.clear(); lastScan.clear(); }

    private void onTick(TickEvent event) {
        if (mc.level == null || mc.player == null) return;
        tick++;
        int r = range.get();
        ChunkPos pc = mc.player.chunkPosition();
        int budget = 4;
        for (int dx = -r; dx <= r && budget > 0; dx++) {
            for (int dz = -r; dz <= r && budget > 0; dz++) {
                int cx = pc.x() + dx, cz = pc.z() + dz;
                long key = Donut.chunkKey(cx, cz);
                Integer last = lastScan.get(key);
                if (last != null && tick - last < 200) continue;
                scan(cx, cz, key);
                lastScan.put(key, tick);
                budget--;
            }
        }
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
    }

    private void scan(int cx, int cz, long key) {
        LevelChunk chunk = Donut.chunk(cx, cz);
        if (chunk == null) { cache.remove(key); return; }
        List<BlockPos> out = new ArrayList<>();
        Predicate<BlockState> wanted = s -> s.is(Blocks.ANCIENT_DEBRIS);
        LevelChunkSection[] sections = chunk.getSections();
        for (int i = 0; i < sections.length; i++) {
            LevelChunkSection sec = sections[i];
            if (sec == null || sec.hasOnlyAir() || !sec.maybeHas(wanted)) continue;
            int baseY = chunk.getSectionYFromSectionIndex(i) << 4;
            for (int x = 0; x < 16; x++)
                for (int y = 0; y < 16; y++)
                    for (int z = 0; z < 16; z++)
                        if (wanted.test(sec.getBlockState(x, y, z)))
                            out.add(new BlockPos((cx << 4) + x, baseY + y, (cz << 4) + z));
        }
        cache.put(key, out);
    }

    private void onWorld(WorldRenderEvent event) {
        if (mc.player == null) return;
        double px = mc.player.getX(), py = mc.player.getY(), pz = mc.player.getZ();
        Style style = Style.stroke(Colors.opaque(COLOR), 2f);
        if (fill.get()) style = style.withFill(Colors.withAlpha(Colors.opaque(COLOR), 0.22f));
        int shown = 0;
        for (List<BlockPos> list : cache.values()) {
            for (BlockPos p : list) {
                if (shown++ >= MAX_BOXES) return;
                double dx = p.getX() + 0.5 - px, dy = p.getY() + 0.5 - py, dz = p.getZ() + 0.5 - pz;
                if (dx * dx + dy * dy + dz * dz > 96.0 * 96.0 * (range.get() / 6.0) * (range.get() / 6.0)) continue;
                event.buffer().box(new AABB(p).inflate(0.002), style);
            }
        }
    }
}
