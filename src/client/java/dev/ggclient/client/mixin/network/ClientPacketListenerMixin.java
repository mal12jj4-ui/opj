package dev.ggclient.client.mixin.network;

import dev.ggclient.client.GGClient;
import dev.ggclient.client.event.events.TotemPopEvent;
import dev.ggclient.client.rotation.Rotation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerRotationPacket;
import net.minecraft.world.entity.EntityEvent;
import net.minecraft.world.entity.Relative;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin {
    @Inject(method = "handleMovePlayer", at = @At("TAIL"))
    private void ggclient$syncMove(ClientboundPlayerPositionPacket packet, CallbackInfo info) {
        if (!packet.relatives().contains(Relative.Y_ROT) || !packet.relatives().contains(Relative.X_ROT)) ggclient$sync();
    }

    @Inject(method = "handleRotatePlayer", at = @At("TAIL"))
    private void ggclient$syncRotate(ClientboundPlayerRotationPacket packet, CallbackInfo info) {
        ggclient$sync();
    }

    @Inject(method = "handleEntityEvent", at = @At("HEAD"))
    private void ggclient$totemPop(ClientboundEntityEventPacket packet, CallbackInfo info) {
        Minecraft client = Minecraft.getInstance();
        if (packet.getEventId() != EntityEvent.PROTECTED_FROM_DEATH || client.level == null || !client.isSameThread()) return;
        if (packet.getEntity(client.level) == client.player) GGClient.EVENTS.post(new TotemPopEvent(client));
    }

    @Unique
    private static void ggclient$sync() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) GGClient.ROTATIONS.sync(Rotation.of(player));
    }
}
