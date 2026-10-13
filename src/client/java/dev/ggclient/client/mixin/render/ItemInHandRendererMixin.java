package dev.ggclient.client.mixin.render;

import dev.ggclient.client.GGClient;
import dev.ggclient.client.event.events.HandRenderEvent;
import dev.ggclient.client.render.entity.EntityFills;
import dev.ggclient.client.render.entity.EntityOutlines;
import dev.ggclient.client.render.entity.HandBounds;
import net.minecraft.client.renderer.ItemInHandRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public abstract class ItemInHandRendererMixin {
    @Inject(method = "submitHandsWithItems", at = @At("HEAD"))
    private void ggclient$beginHand(CallbackInfo info) {
        EntityFills.beginHand(GGClient.EVENTS.post(new HandRenderEvent()).fill());
        if (EntityOutlines.hand() != null) HandBounds.begin();
    }

    @Inject(method = "submitHandsWithItems", at = @At("RETURN"))
    private void ggclient$endHand(CallbackInfo info) {
        if (EntityOutlines.hand() != null) HandBounds.end(EntityOutlines.hand());
        EntityFills.endHand();
    }
}
