package dev.ggclient.client.mixin.donut;

import dev.ggclient.client.GGClient;
import dev.ggclient.client.module.impl.render.Freecam;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityTurnMixin {
    // Redirects mouse look to the freecam instead of your player
    @Inject(method = "turn", at = @At("HEAD"), cancellable = true, require = 0)
    private void ggclient$freecamTurn(double yaw, double pitch, CallbackInfo ci) {
        if ((Object) this != Minecraft.getInstance().player) return;
        if (!GGClient.MODULES.isEnabled(Freecam.class)) return;
        GGClient.MODULES.get(Freecam.class).look(yaw, pitch);
        ci.cancel();
    }
}
