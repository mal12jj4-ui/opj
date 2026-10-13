package dev.ggclient.client.mixin.network;

import dev.ggclient.client.GGClient;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ServerboundUseItemPacket.class)
public abstract class ServerboundUseItemPacketMixin {
    @ModifyVariable(method = "<init>(Lnet/minecraft/world/InteractionHand;IFF)V", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private static float ggclient$yaw(float yaw) {
        return GGClient.ROTATIONS.sentYaw(yaw);
    }

    @ModifyVariable(method = "<init>(Lnet/minecraft/world/InteractionHand;IFF)V", at = @At("HEAD"), argsOnly = true, ordinal = 1)
    private static float ggclient$pitch(float pitch) {
        return GGClient.ROTATIONS.sentPitch(pitch);
    }
}
