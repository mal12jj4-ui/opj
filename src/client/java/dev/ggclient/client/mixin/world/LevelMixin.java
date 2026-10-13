package dev.ggclient.client.mixin.world;

import dev.ggclient.client.GGClient;
import dev.ggclient.client.module.impl.render.world.WorldModifier;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Level.class)
public abstract class LevelMixin {
    @Inject(method = "getRainLevel", at = @At("RETURN"), cancellable = true)
    private void ggclient$rainLevel(float partialTicks, CallbackInfoReturnable<Float> info) {
        if (!((Object) this instanceof ClientLevel)) return;
        info.setReturnValue(GGClient.MODULES.get(WorldModifier.class).rainLevel(partialTicks, info.getReturnValueF()));
    }

    @Inject(method = "getThunderLevel", at = @At("RETURN"), cancellable = true)
    private void ggclient$thunderLevel(float partialTicks, CallbackInfoReturnable<Float> info) {
        if (!((Object) this instanceof ClientLevel)) return;
        info.setReturnValue(GGClient.MODULES.get(WorldModifier.class).thunderLevel(partialTicks, info.getReturnValueF()));
    }
}
