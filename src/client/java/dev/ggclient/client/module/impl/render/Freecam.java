package dev.ggclient.client.module.impl.render;

import dev.ggclient.client.event.events.TickEvent;
import dev.ggclient.client.module.Module;
import dev.ggclient.client.setting.BoolSetting;
import dev.ggclient.client.setting.SliderSetting;
import dev.ggclient.client.util.Donut;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public final class Freecam extends Module {
    public double x, y, z;
    public float yaw, pitch;

    private final SliderSetting speed = add(new SliderSetting("speed", 12, 1, 60));
    private final SliderSetting sprintBoost = add(new SliderSetting("sprintBoost", 3, 1, 10));
    private final BoolSetting showPlayer = add(new BoolSetting("showPlayer", true));
    private long lastNanos;

    public Freecam() {
        super("freecam");
    }

    public boolean showPlayer() { return showPlayer.get(); }

    @Override
    protected void onEnable() {
        if (mc.player == null) { setEnabled(false); return; }
        Vec3 p = mc.player.getEyePosition();
        x = p.x; y = p.y; z = p.z;
        yaw = mc.player.getYRot();
        pitch = mc.player.getXRot();
        lastNanos = System.nanoTime();
        listen(TickEvent.class, event -> {
            if (mc.player == null || mc.level == null) setEnabled(false);
        });
    }

    /** Called once per rendered frame from CameraMixin, so movement is smooth. */
    public void updateCamera() {
        long now = System.nanoTime();
        double dt = Math.min((now - lastNanos) / 1_000_000_000.0, 0.1);
        lastNanos = now;
        if (Donut.screen() != null) return;

        double forward = axis(mc.options.keyUp.isDown(), mc.options.keyDown.isDown());
        double strafe = axis(mc.options.keyRight.isDown(), mc.options.keyLeft.isDown());
        double vertical = axis(mc.options.keyJump.isDown(), mc.options.keyShift.isDown());

        double step = speed.get() * (mc.options.keySprint.isDown() ? sprintBoost.get() : 1.0) * dt;
        double rad = Math.toRadians(yaw);
        double sin = Math.sin(rad), cos = Math.cos(rad);

        x += (-sin * forward - cos * strafe) * step;
        z += (cos * forward - sin * strafe) * step;
        y += vertical * step;
    }

    public void look(double dx, double dy) {
        yaw += (float) dx * 0.15f;
        pitch = Mth.clamp(pitch + (float) dy * 0.15f, -90f, 90f);
    }

    private static double axis(boolean positive, boolean negative) {
        return (positive ? 1 : 0) - (negative ? 1 : 0);
    }
}
