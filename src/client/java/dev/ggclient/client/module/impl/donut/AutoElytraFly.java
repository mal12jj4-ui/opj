package dev.ggclient.client.module.impl.donut;

import dev.ggclient.client.event.events.TickEvent;
import dev.ggclient.client.module.Module;
import dev.ggclient.client.setting.EnumSetting;
import dev.ggclient.client.setting.SliderSetting;
import dev.ggclient.client.util.Donut;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

/** Needs an elytra already equipped and firework rockets in your hotbar. */
public final class AutoElytraFly extends Module {
    private final SliderSetting altitude = add(new SliderSetting("altitude", 150, -64, 320));
    private final EnumSetting heading = add(new EnumSetting("heading", 0, "Current", "North", "South", "East", "West"));
    private final SliderSetting minSpeed = add(new SliderSetting("minSpeed", 30, 10, 60));
    private final SliderSetting boostInterval = add(new SliderSetting("boostInterval", 40, 10, 200));

    private float yawLock;
    private int boostTimer;
    private boolean warnedNoFireworks;

    public AutoElytraFly() {
        super("autoElytraFly");
    }

    @Override
    protected void onEnable() {
        warnedNoFireworks = false;
        boostTimer = 0;
        if (mc.player == null) return;
        if (!mc.player.getItemBySlot(EquipmentSlot.CHEST).is(Items.ELYTRA)) {
            say("Equip an elytra first");
            setEnabled(false);
            return;
        }
        yawLock = switch (heading.selected()) {
            case "North" -> 180f;
            case "South" -> 0f;
            case "East" -> -90f;
            case "West" -> 90f;
            default -> mc.player.getYRot();
        };
        listen(TickEvent.class, this::onTick);
    }

    private void onTick(TickEvent event) {
        if (mc.player == null || mc.level == null) { setEnabled(false); return; }

        ItemStack chest = mc.player.getItemBySlot(EquipmentSlot.CHEST);
        if (!chest.is(Items.ELYTRA)) { say("Elytra no longer equipped"); setEnabled(false); return; }
        if (chest.getMaxDamage() - chest.getDamageValue() < 15) { say("Elytra almost broken, stopping"); setEnabled(false); return; }

        // Take off
        if (!mc.player.isFallFlying()) {
            if (mc.player.onGround()) {
                mc.player.jumpFromGround();
            } else if (mc.player.getDeltaMovement().y < 0) {
                mc.player.connection.send(new ServerboundPlayerCommandPacket(mc.player,
                        ServerboundPlayerCommandPacket.Action.START_FALL_FLYING));
            }
            return;
        }

        // Hold heading and altitude
        mc.player.setYRot(yawLock);
        double dy = altitude.get() - mc.player.getY();
        float desired = (float) Mth.clamp(-dy * 0.6, -45.0, 25.0);
        mc.player.setXRot(Mth.lerp(0.15f, mc.player.getXRot(), desired));

        // Firework boost when slow
        boostTimer++;
        Vec3 v = mc.player.getDeltaMovement();
        double speedPerSecond = Math.hypot(v.x, v.z) * 20.0;
        if (boostTimer >= boostInterval.get() && speedPerSecond < minSpeed.get()) {
            if (useFirework()) boostTimer = 0;
        }
    }

    private boolean useFirework() {
        int slot = -1;
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getItem(i).is(Items.FIREWORK_ROCKET)) { slot = i; break; }
        }
        if (slot < 0) {
            if (!warnedNoFireworks) { say("No fireworks in your hotbar"); warnedNoFireworks = true; }
            return false;
        }
        int previous = mc.player.getInventory().getSelectedSlot();
        mc.player.getInventory().setSelectedSlot(slot);
        mc.gameMode.useItem(mc.player, InteractionHand.MAIN_HAND);
        mc.player.getInventory().setSelectedSlot(previous);
        return true;
    }

    private void say(String msg) { Donut.say("\u00a76[Auto Elytra Fly] \u00a7f" + msg); }
}
