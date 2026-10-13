package dev.ggclient.client.mixin.network;

import dev.ggclient.client.util.Hotbar;
import dev.ggclient.client.util.Placement;
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.network.protocol.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientCommonPacketListenerImpl.class)
public abstract class ClientCommonPacketListenerImplMixin {
    @Inject(method = "send(Lnet/minecraft/network/protocol/Packet;)V", at = @At("HEAD"))
    private void ggclient$sent(Packet<?> packet, CallbackInfo info) {
        Hotbar.sent(packet);
        Placement.sent(packet);
    }
}
