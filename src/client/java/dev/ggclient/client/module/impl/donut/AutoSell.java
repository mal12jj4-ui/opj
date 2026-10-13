package dev.ggclient.client.module.impl.donut;

import dev.ggclient.client.event.events.TickEvent;
import dev.ggclient.client.module.Module;
import dev.ggclient.client.setting.BoolSetting;
import dev.ggclient.client.setting.SliderSetting;
import dev.ggclient.client.util.Donut;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;

public final class AutoSell extends Module {
    private final SliderSetting interval = add(new SliderSetting("interval", 30, 5, 600));
    private final SliderSetting clickDelay = add(new SliderSetting("clickDelay", 3, 1, 20));
    private final BoolSetting skipHotbar = add(new BoolSetting("skipHotbar", true));

    private enum State { IDLE, WAIT_GUI, FILL, CLOSING }

    private State state = State.IDLE;
    private int timer, wait, clicks;

    public AutoSell() {
        super("autoSell");
    }

    @Override
    protected void onEnable() {
        state = State.IDLE;
        timer = interval.get() * 20;
        listen(TickEvent.class, this::onTick);
    }

    @Override
    protected void onDisable() { state = State.IDLE; }

    private boolean sellIndex(int i) {
        return (i >= 9 && i <= 35) || (!skipHotbar.get() && i >= 0 && i <= 8);
    }

    private boolean hasSellable() {
        for (int i = 0; i <= 35; i++) {
            if (sellIndex(i) && !mc.player.getInventory().getItem(i).isEmpty()) return true;
        }
        return false;
    }

    private Slot nextSellable() {
        AbstractContainerMenu menu = mc.player.containerMenu;
        for (Slot s : menu.slots) {
            if (s.container == mc.player.getInventory() && s.hasItem() && sellIndex(s.getContainerSlot())) return s;
        }
        return null;
    }

    private void onTick(TickEvent event) {
        if (mc.player == null || mc.gameMode == null) return;

        switch (state) {
            case IDLE -> {
                if (++timer < interval.get() * 20) return;
                if (Donut.screen() != null) return;
                if (!hasSellable()) { timer = 0; return; }
                mc.player.connection.sendCommand("sell");
                state = State.WAIT_GUI;
                wait = 0;
            }
            case WAIT_GUI -> {
                if (Donut.screen() instanceof ContainerScreen) {
                    state = State.FILL; wait = 0; clicks = 0;
                } else if (++wait > 60) {
                    state = State.IDLE; timer = 0;
                }
            }
            case FILL -> {
                if (!(Donut.screen() instanceof ContainerScreen)) { state = State.IDLE; timer = 0; return; }
                if (++wait < clickDelay.get()) return;
                wait = 0;
                Slot next = nextSellable();
                if (next == null || clicks >= 36) { state = State.CLOSING; wait = 0; return; }
                AbstractContainerMenu menu = mc.player.containerMenu;
                mc.gameMode.handleContainerInput(menu.containerId, next.index, 0, ContainerInput.QUICK_MOVE, mc.player);
                clicks++;
            }
            case CLOSING -> {
                if (++wait >= 5) {
                    mc.player.closeContainer();
                    state = State.IDLE; timer = 0;
                }
            }
        }
    }
}
