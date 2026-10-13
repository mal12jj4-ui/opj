package dev.ggclient.client.mixin.donut;

import dev.ggclient.client.GGClient;
import dev.ggclient.client.module.impl.donut.FarmFpsOptimizer;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ParticleEngine.class)
public abstract class ParticleEngineMixin {
    @Inject(method = "createParticle", at = @At("HEAD"), cancellable = true, require = 0)
    private void ggclient$farmParticles(ParticleOptions options, double x, double y, double z,
                                        double vx, double vy, double vz, CallbackInfoReturnable<Particle> cir) {
        if (!GGClient.MODULES.isEnabled(FarmFpsOptimizer.class)) return;
        if (!GGClient.MODULES.get(FarmFpsOptimizer.class).hidesExplosions()) return;
        if (options.getType() == ParticleTypes.EXPLOSION || options.getType() == ParticleTypes.EXPLOSION_EMITTER) {
            cir.setReturnValue(null);
        }
    }
}
