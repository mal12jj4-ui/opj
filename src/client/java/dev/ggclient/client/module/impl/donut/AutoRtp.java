package dev.ggclient.client.module.impl.donut;

import dev.ggclient.client.GGClient;
import dev.ggclient.client.event.events.TickEvent;
import dev.ggclient.client.module.Module;
import dev.ggclient.client.setting.BoolSetting;
import dev.ggclient.client.setting.SliderSetting;
import dev.ggclient.client.util.Donut;
import net.minecraft.world.entity.player.Player;

public final class AutoRtp extends Module {
    private final SliderSetting interval = add(new SliderSetting("interval", 10, 3, 120));
    private final BoolSetting stopOnPlayer = add(new BoolSetting("stopOnPlayer", true));
    private final BoolSetting stopOnBase = add(new BoolSetting("stopOnBase", true));

    private int timer, sinceRtp;

    public AutoRtp() {
        super("autoRtp");
    }

    @Override
    protected void onEnable() {
        timer = interval.get() * 20;
        sinceRtp = 1000;
        listen(TickEvent.class, this::onTick);
    }

    private void onTick(TickEvent event) {
        if (mc.player == null || mc.level == null) { setEnabled(false); return; }
        sinceRtp++;

        if (stopOnPlayer.get()) {
            for (Player p : mc.level.players()) {
                if (p == mc.player || p.isSpectator()) continue;
                Donut.say("\u00a76[Auto RTP] \u00a7fStopped: player " + p.getName().getString() + " nearby");
                setEnabled(false);
                return;
            }
        }

        if (stopOnBase.get() && sinceRtp > 100 && baseFound()) {
            Donut.say("\u00a76[Auto RTP] \u00a7fStopped: base detected");
            setEnabled(false);
            return;
        }

        if (++timer >= interval.get() * 20 && Donut.screen() == null) {
            mc.player.connection.sendCommand("rtp");
            timer = 0;
            sinceRtp = 0;
        }
    }

    static boolean baseFound() {
        BaseFinder bf = GGClient.MODULES.get(BaseFinder.class);
        BaseEsp be = GGClient.MODULES.get(BaseEsp.class);
        return (bf.isEnabled() && bf.hasFlagged()) || (be.isEnabled() && be.hasGroups());
    }
}
