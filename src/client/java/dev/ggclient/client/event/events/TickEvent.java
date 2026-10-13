package dev.ggclient.client.event.events;

import dev.ggclient.client.event.Event;
import net.minecraft.client.Minecraft;

public record TickEvent(Minecraft client) implements Event {
}
