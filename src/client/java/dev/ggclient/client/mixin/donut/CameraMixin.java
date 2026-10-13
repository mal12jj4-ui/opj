package dev.ggclient.client.mixin.donut;

import dev.ggclient.client.GGClient;
import dev.ggclient.client.module.impl.render.Freecam;
import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Camera.class)
public abstract class CameraMixin {
    @Shadow protected abstract void setPosition(double x, double y, double z);
    @Shadow protected abstract void setRotation(float yRot, float xRot);

    @Inject(method = "setup", at = @At("TAIL"), require = 0)
    private void ggclient$freecam(CallbackInfo ci) {
        if (!GGClient.MODULES.isEnabled(Freecam.class)) return;
        Freecam fc = GGClient.MODULES.get(Freecam.class);
        fc.updateCamera();
        setPosition(fc.x, fc.y, fc.z);
        setRotation(fc.yaw, fc.pitch);
    }

    // Makes your own player model render while in Freecam
    @Inject(method = "isDetached", at = @At("HEAD"), cancellable = true, require = 0)
    private void ggclient$detached(CallbackInfoReturnable<Boolean> cir) {
        if (GGClient.MODULES.isEnabled(Freecam.class) && GGClient.MODULES.get(Freecam.class).showPlayer()) {
            cir.setReturnValue(true);
        }
    }
}
