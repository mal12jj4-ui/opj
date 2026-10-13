package dev.ggclient.client.mixin.render;

import dev.ggclient.client.mixin.accessor.LevelRendererAccessor;
import dev.ggclient.client.render.entity.EntityOutlines;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @Shadow
    @Final
    private Minecraft minecraft;

    @Inject(method = "render(Lnet/minecraft/client/DeltaTracker;Z)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;doEntityOutline()V"))
    private void ggclient$inkOutline(DeltaTracker deltaTracker, boolean renderLevel, CallbackInfo info) {
        if (!EntityOutlines.active()) return;
        GameRenderer renderer = (GameRenderer) (Object) this;
        if (!renderer.gameRenderState().levelRenderState.shouldShowEntityOutlines) return;
        EntityOutlines.render(((LevelRendererAccessor) minecraft.levelRenderer).ggclient$getEntityOutlineTarget(), renderer.mainRenderTarget());
    }
}
