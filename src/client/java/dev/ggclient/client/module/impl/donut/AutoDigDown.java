package dev.ggclient.client.module.impl.donut;

import dev.ggclient.client.event.events.TickEvent;
import dev.ggclient.client.module.Module;
import dev.ggclient.client.setting.SliderSetting;
import dev.ggclient.client.util.Donut;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;

public final class AutoDigDown extends Module {
    private final SliderSetting targetY = add(new SliderSetting("targetY", -50, -64, 320));

    public AutoDigDown() {
        super("autoDigDown");
    }

    public void setTargetY(int y) { targetY.set(y); }

    @Override
    protected void onEnable() {
        listen(TickEvent.class, this::onTick);
    }

    private void onTick(TickEvent event) {
        if (mc.player == null || mc.level == null || mc.gameMode == null) { setEnabled(false); return; }
        if (Donut.screen() != null) return;

        if (mc.player.getY() <= targetY.get() + 0.5) {
            Donut.say("\u00a76[Auto Dig Down] \u00a7fReached Y " + targetY.get());
            setEnabled(false);
            return;
        }

        BlockPos feet = mc.player.blockPosition();
        // Safety: never dig toward lava
        for (int i = 1; i <= 3; i++) {
            if (mc.level.getFluidState(feet.below(i)).is(FluidTags.LAVA)) {
                Donut.say("\u00a76[Auto Dig Down] \u00a7fLava below, stopped");
                setEnabled(false);
                return;
            }
        }

        mc.player.setXRot(90f);
        BlockPos below = feet.below();
        if (!mc.level.getBlockState(below).isAir() && mc.level.getFluidState(below).isEmpty()) {
            mc.gameMode.continueDestroyBlock(below, Direction.UP);
            mc.player.swing(InteractionHand.MAIN_HAND);
        }
    }
}
