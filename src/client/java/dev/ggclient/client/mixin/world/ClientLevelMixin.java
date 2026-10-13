package dev.ggclient.client.mixin.world;

import dev.ggclient.client.GGClient;
import dev.ggclient.client.module.impl.render.world.Atmosphere;
import dev.ggclient.client.module.impl.render.world.WorldModifier;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.EndFlashState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientLevel.class)
public abstract class ClientLevelMixin {
    @Inject(method = "getPrecipitationAt", at = @At("RETURN"), cancellable = true)
    private void ggclient$precipitation(BlockPos pos, CallbackInfoReturnable<Biome.Precipitation> info) {
        Atmosphere atmosphere = GGClient.MODULES.get(WorldModifier.class).atmosphere();
        if (atmosphere != null && ((ClientLevel) (Object) this).hasChunkAt(pos)) info.setReturnValue(atmosphere.precipitation());
    }

    @Inject(method = "endFlashState", at = @At("RETURN"), cancellable = true)
    private void ggclient$endFlashState(CallbackInfoReturnable<EndFlashState> info) {
        if (info.getReturnValue() == null) info.setReturnValue(GGClient.MODULES.get(WorldModifier.class).flashes());
    }
}
