package dev.ggclient.client.mixin.network;

import dev.ggclient.client.GGClient;
import dev.ggclient.client.event.events.AttackEvent;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MultiPlayerGameMode.class)
public abstract class MultiPlayerGameModeMixin {
    @Inject(method = "attack", at = @At("HEAD"))
    private void ggclient$attack(Player player, Entity target, CallbackInfo info) {
        GGClient.EVENTS.post(new AttackEvent(player, target));
    }
}
