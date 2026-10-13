package dev.ggclient.client.mixin.render;

import dev.ggclient.client.GGClient;
import dev.ggclient.client.event.events.EntityRenderStateEvent;
import dev.ggclient.client.render.entity.Filled;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin {
    @Inject(method = "createRenderState(Lnet/minecraft/world/entity/Entity;F)Lnet/minecraft/client/renderer/entity/state/EntityRenderState;", at = @At("RETURN"))
    private void ggclient$extracted(Entity entity, float partialTicks, CallbackInfoReturnable<EntityRenderState> info) {
        EntityRenderState state = info.getReturnValue();
        ((Filled) state).ggclient$setFill(null);
        ((Filled) state).ggclient$setOutline(null);
        GGClient.EVENTS.post(new EntityRenderStateEvent(entity, state, partialTicks));
    }
}
