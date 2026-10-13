package dev.ggclient.client.mixin.entity;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.ggclient.client.GGClient;
import dev.ggclient.client.rotation.Rotation;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin {
    @Shadow private float yRotLast;
    @Shadow private float xRotLast;

    @ModifyExpressionValue(method = "sendPosition", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getYRot()F"))
    private float ggclient$sentYaw(float yaw) {
        return GGClient.ROTATIONS.sentYaw(yaw);
    }

    @ModifyExpressionValue(method = "sendPosition", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getXRot()F"))
    private float ggclient$sentPitch(float pitch) {
        return GGClient.ROTATIONS.sentPitch(pitch);
    }

    @Inject(method = "sendPosition", at = @At("TAIL"))
    private void ggclient$sent(CallbackInfo info) {
        GGClient.ROTATIONS.sent(new Rotation(yRotLast, xRotLast));
    }
}
