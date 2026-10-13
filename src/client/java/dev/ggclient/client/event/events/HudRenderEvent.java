package dev.ggclient.client.event.events;

import dev.ggclient.client.event.Event;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public record HudRenderEvent(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) implements Event {
}
