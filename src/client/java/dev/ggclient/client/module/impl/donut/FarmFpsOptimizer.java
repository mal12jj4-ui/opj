package dev.ggclient.client.module.impl.donut;

import dev.ggclient.client.module.Module;
import dev.ggclient.client.setting.BoolSetting;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.item.PrimedTnt;

public final class FarmFpsOptimizer extends Module {
    private final BoolSetting xpOrbs = add(new BoolSetting("xpOrbs", true));
    private final BoolSetting fallingBlocks = add(new BoolSetting("fallingBlocks", true));
    private final BoolSetting tnt = add(new BoolSetting("tnt", true));
    private final BoolSetting explosions = add(new BoolSetting("explosions", true));

    public FarmFpsOptimizer() {
        super("farmFpsOptimizer");
    }

    public boolean hides(Entity e) {
        return (xpOrbs.get() && e instanceof ExperienceOrb)
                || (fallingBlocks.get() && e instanceof FallingBlockEntity)
                || (tnt.get() && e instanceof PrimedTnt);
    }

    public boolean hidesExplosions() { return explosions.get(); }
}
