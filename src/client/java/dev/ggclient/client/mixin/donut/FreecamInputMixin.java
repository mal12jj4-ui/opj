package dev.ggclient.client.mixin.donut;

import dev.ggclient.client.GGClient;
import dev.ggclient.client.module.impl.render.Freecam;
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.phys.Vec2;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardInput.class)
public abstract class FreecamInputMixin extends ClientInput {
    // Stops your real player from walking around while the camera is detached
    @Inject(method = "tick", at = @At("TAIL"), require = 0)
    private void ggclient$freecamInput(CallbackInfo ci) {
        if (!GGClient.MODULES.isEnabled(Freecam.class)) return;
        keyPresses = new Input(false, false, false, false, false, false, false);
        moveVector = Vec2.ZERO;
    }
}
