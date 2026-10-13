package dev.ggclient.client.mixin.input;

import dev.ggclient.client.GGClient;
import dev.ggclient.client.event.events.MouseMoveEvent;
import dev.ggclient.client.event.events.TurnEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public abstract class MouseHandlerMixin {
    @Unique
    private static final float TICKS_PER_SECOND = 20f;

    @Inject(method = "turnPlayer", at = @At("TAIL"))
    private void ggclient$turned(double seconds, CallbackInfo info) {
        GGClient.EVENTS.post(new TurnEvent(Minecraft.getInstance(), (float) seconds * TICKS_PER_SECOND));
    }

    @Inject(method = "handleAccumulatedMovement", at = @At("TAIL"))
    private void ggclient$moved(CallbackInfo info) {
        Minecraft client = Minecraft.getInstance();
        if (client.gui.screen() == null) return;
        MouseHandler handler = (MouseHandler) (Object) this;
        GGClient.EVENTS.post(new MouseMoveEvent(client,
                handler.getScaledXPos(client.getWindow()), handler.getScaledYPos(client.getWindow())));
    }
}
