package dev.ggclient.client.module.impl.donut;

import dev.ggclient.client.GGClient;
import dev.ggclient.client.event.events.TickEvent;
import dev.ggclient.client.module.Module;
import dev.ggclient.client.setting.SliderSetting;
import dev.ggclient.client.util.Donut;
import net.minecraft.world.phys.Vec3;

/** Loop: /rtp -> dig down -> wait and scan -> repeat, until Base Finder or Base ESP flags something. */
public final class AutoBaseHunt extends Module {
    private final SliderSetting digToY = add(new SliderSetting("digToY", -40, -64, 320));
    private final SliderSetting scanTime = add(new SliderSetting("scanTime", 15, 5, 120));

    private enum State { RTP, WAIT_TP, DIG, SCAN }

    private State state = State.RTP;
    private Vec3 startPos = Vec3.ZERO;
    private int wait, sinceRtp;

    public AutoBaseHunt() {
        super("autoBaseHunt");
    }

    @Override
    protected void onEnable() {
        state = State.RTP; wait = 0; sinceRtp = 1000;
        GGClient.MODULES.get(BaseFinder.class).setEnabled(true);
        GGClient.MODULES.get(BaseEsp.class).setEnabled(true);
        listen(TickEvent.class, this::onTick);
    }

    @Override
    protected void onDisable() {
        GGClient.MODULES.get(AutoDigDown.class).setEnabled(false);
    }

    private void onTick(TickEvent event) {
        if (mc.player == null || mc.level == null) { setEnabled(false); return; }
        sinceRtp++;

        if (sinceRtp > 100 && AutoRtp.baseFound()) {
            Donut.say("\u00a76[Auto Base Hunt] \u00a7fBase detected, hunt stopped");
            setEnabled(false);
            return;
        }

        AutoDigDown dig = GGClient.MODULES.get(AutoDigDown.class);
        switch (state) {
            case RTP -> {
                if (Donut.screen() != null) return;
                startPos = mc.player.position();
                mc.player.connection.sendCommand("rtp");
                sinceRtp = 0; wait = 0;
                state = State.WAIT_TP;
            }
            case WAIT_TP -> {
                if (mc.player.position().distanceTo(startPos) > 64) {
                    dig.setTargetY(digToY.get());
                    dig.setEnabled(true);
                    wait = 0;
                    state = State.DIG;
                } else if (++wait > 400) {
                    state = State.RTP; // teleport never happened, try again
                }
            }
            case DIG -> {
                if (!dig.isEnabled()) { wait = 0; state = State.SCAN; }
            }
            case SCAN -> {
                if (++wait >= scanTime.get() * 20) state = State.RTP;
            }
        }
    }
}
