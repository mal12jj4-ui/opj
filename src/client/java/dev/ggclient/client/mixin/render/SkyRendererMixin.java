package dev.ggclient.client.mixin.render;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.ggclient.client.GGClient;
import dev.ggclient.client.module.impl.render.world.Atmosphere;
import dev.ggclient.client.module.impl.render.world.WorldModifier;
import net.minecraft.client.renderer.SkyRenderer;
import net.minecraft.world.level.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SkyRenderer.class)
public abstract class SkyRendererMixin {
    @ModifyExpressionValue(method = "extractRenderState", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/dimension/DimensionType;skybox()Lnet/minecraft/world/level/dimension/DimensionType$Skybox;"))
    private DimensionType.Skybox ggclient$skybox(DimensionType.Skybox original) {
        Atmosphere atmosphere = GGClient.MODULES.get(WorldModifier.class).atmosphere();
        return atmosphere == null || atmosphere.skybox() == null ? original : atmosphere.skybox();
    }
}
