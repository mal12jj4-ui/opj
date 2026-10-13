package dev.ggclient.client.event.events;

import dev.ggclient.client.event.Event;
import net.minecraft.client.Minecraft;

public record TurnEvent(Minecraft client, float deltaTicks) implements Event {
}
