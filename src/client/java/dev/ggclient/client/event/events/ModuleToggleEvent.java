package dev.ggclient.client.event.events;

import dev.ggclient.client.event.Event;
import dev.ggclient.client.module.Module;

public record ModuleToggleEvent(Module module, boolean enabled) implements Event {
}
