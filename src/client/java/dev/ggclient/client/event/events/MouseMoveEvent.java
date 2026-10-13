package dev.ggclient.client.event.events;

import dev.ggclient.client.event.Event;
import net.minecraft.client.Minecraft;

public record MouseMoveEvent(Minecraft client, double x, double y) implements Event {
}
