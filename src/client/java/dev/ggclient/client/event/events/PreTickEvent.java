package dev.ggclient.client.event.events;

import dev.ggclient.client.event.Event;
import net.minecraft.client.Minecraft;

public record PreTickEvent(Minecraft client) implements Event {
}
