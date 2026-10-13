package dev.ggclient.client.mixin.input;

import com.mojang.blaze3d.platform.InputConstants;
import dev.ggclient.client.GGClient;
import dev.ggclient.client.event.events.PacketProcessEvent;
import dev.ggclient.client.module.impl.combat.AimAssist;
import dev.ggclient.client.util.Clicks;
import dev.ggclient.client.util.Players;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.HitResult;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
    @Shadow public HitResult hitResult;
    @Shadow public MultiPlayerGameMode gameMode;
    @Shadow public LocalPlayer player;

    @Inject(method = "runTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/PacketProcessor;processQueuedPackets()V", shift = At.Shift.AFTER))
    private void ggclient$packetsProcessed(boolean renderLevel, CallbackInfo info) {
        GGClient.EVENTS.post(new PacketProcessEvent((Minecraft) (Object) this));
    }

    @Inject(method = "startAttack", at = @At("HEAD"), cancellable = true)
    private void ggclient$startAttack(CallbackInfoReturnable<Boolean> info) {
        if (ggclient$blocksBreaking() || ggclient$activates(GLFW.GLFW_MOUSE_BUTTON_LEFT)) info.setReturnValue(false);
    }

    @Inject(method = "startUseItem", at = @At("HEAD"), cancellable = true)
    private void ggclient$startUseItem(CallbackInfo info) {
        if (ggclient$activates(GLFW.GLFW_MOUSE_BUTTON_RIGHT) && !Players.consuming(player)) info.cancel();
    }

    @Unique
    private static boolean ggclient$activates(int button) {
        return !Clicks.simulating(button) && GGClient.MODULES.activates(InputConstants.Type.MOUSE.getOrCreate(button));
    }

    @Inject(method = "continueAttack", at = @At("HEAD"), cancellable = true)
    private void ggclient$continueAttack(boolean leftClick, CallbackInfo info) {
        if (ggclient$blocksBreaking()) info.cancel();
    }

    @Unique
    private boolean ggclient$blocksBreaking() {
        if (hitResult == null || hitResult.getType() != HitResult.Type.BLOCK) return false;
        if (!GGClient.MODULES.get(AimAssist.class).blocksBreaking()) return false;
        if (gameMode != null) gameMode.stopDestroyBlock();
        return true;
    }
}
