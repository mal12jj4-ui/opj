package dev.ggclient.client.mixin.donut;

import dev.ggclient.client.GGClient;
import dev.ggclient.client.module.impl.donut.FarmFpsOptimizer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRenderDispatcherMixin {
    @Inject(method = "shouldRender", at = @At("HEAD"), cancellable = true, require = 0)
    private void ggclient$farmHide(Entity entity, Frustum frustum, double x, double y, double z,
                                   CallbackInfoReturnable<Boolean> cir) {
        if (!GGClient.MODULES.isEnabled(FarmFpsOptimizer.class)) return;
        if (GGClient.MODULES.get(FarmFpsOptimizer.class).hides(entity)) cir.setReturnValue(false);
    }
}
