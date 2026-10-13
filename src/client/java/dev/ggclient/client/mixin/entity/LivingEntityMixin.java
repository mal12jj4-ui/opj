package dev.ggclient.client.mixin.entity;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.ggclient.client.GGClient;
import dev.ggclient.client.module.impl.combat.Triggerbot;
import dev.ggclient.client.util.Entities;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @ModifyExpressionValue(method = "jumpFromGround", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getYRot()F"))
    private float ggclient$jumpYaw(float yaw) {
        return GGClient.ROTATIONS.movementYaw((Entity) (Object) this, yaw);
    }

    @ModifyExpressionValue(method = "updateFallFlyingMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getXRot()F"))
    private float ggclient$glidePitch(float pitch) {
        return GGClient.ROTATIONS.movementPitch((Entity) (Object) this, pitch);
    }

    @ModifyExpressionValue(method = "updateFallFlyingMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getLookAngle()Lnet/minecraft/world/phys/Vec3;"))
    private Vec3 ggclient$glideLook(Vec3 look) {
        return GGClient.ROTATIONS.movementLook((Entity) (Object) this, look);
    }

    @Inject(method = "setSprinting", at = @At("HEAD"), cancellable = true)
    private void ggclient$holdSprint(boolean sprinting, CallbackInfo info) {
        if (!sprinting || !Entities.isLocal((Entity) (Object) this)) return;
        if (GGClient.MODULES.get(Triggerbot.class).holdsSprint()) info.cancel();
    }
}
